package desutine.kismet.util;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import desutine.kismet.Kismet;
import desutine.kismet.ModLogger;
import desutine.kismet.common.KismetConfig;
import desutine.kismet.server.StackWrapper;
import desutine.kismet.server.WorldSavedDataTargets;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class TargetLibraryFactory {
    // "based" on LootTableManager's Gson
    private static final Gson gson = (new GsonBuilder())
            .registerTypeAdapter(RandomValueRange.class, new RandomValueRange.Serializer())
            .registerTypeAdapter(LootPool.class, new LootPool.Serializer())
            .registerTypeAdapter(LootTable.class, new LootTable.Serializer())
            .registerTypeHierarchyAdapter(LootEntry.class, new LootEntry.Serializer())
            .registerTypeHierarchyAdapter(LootFunction.class, new LootFunctionManager.Serializer())
            .registerTypeHierarchyAdapter(LootCondition.class, new LootConditionManager.Serializer())
            .registerTypeHierarchyAdapter(LootContext.EntityTarget.class, new LootContext.EntityTarget.Serializer())
            .create();
    private static WorldSavedDataTargets worldSavedDataTargets;

    private static boolean isMod(String s) {
        return !s.contains(":");
    }

    private static String standardizeFilter(@Nonnull String s) {
        // try to add metadata
        final String[] split = s.split(":");
        if (split.length == 1) {
            // mod
            // todo check if the mod is present
            // todo check if it's a valid mod name
            return split[0];
        } else if (split.length == 2 || split.length == 3) {
            // item (with possible metadata
            ItemStack stack;
            // if we have metadata, creating a resourceLocation with it is a baaaad idea
            stack = getItemStack(s);
            if (stack == null) return null;

            return StackHelper.toUniqueKey(stack);
        } else {
            ModLogger.warning("Weird location: " + s);
            return null;
        }
    }

    /**
     * Generates the target lists within the game.
     *
     * @param player The player entity to use to enrich the state
     */
    public static void generateStacks(EntityPlayerMP player) {
        if (worldSavedDataTargets == null) {
            worldSavedDataTargets = WorldSavedDataTargets.get(player.worldObj);
        }
        worldSavedDataTargets.setStacks(new ArrayList<>());

        List<StackWrapper> unsortedStacks = new ArrayList<>();
        addRegisteredItems(unsortedStacks);
//        identifyLoot(player.getServerForPlayer(), unsortedStacks);

        // separate the stacks per mod, for smaller packets
        Map<String, List<StackWrapper>> modSortedStacks = new HashMap<>();
        for (StackWrapper wrapper : unsortedStacks) {
            String mod = StackHelper.getMod(wrapper.getStack());
            if (!modSortedStacks.containsKey(mod)) {
                final ArrayList<StackWrapper> wrappers = new ArrayList<>();
                wrappers.add(wrapper);
                modSortedStacks.put(mod, wrappers);
            } else {
                modSortedStacks.get(mod).add(wrapper);
            }
        }

        modSortedStacks.forEach((s, list) -> Kismet.packetHandler.enrichStacks(list, player));
    }

    private static void addRegisteredItems(List<StackWrapper> unsortedStacks) {
        // add stacks from ItemRegistery
        for (ResourceLocation loc : Item.itemRegistry.getKeys()) {
            Item item = Item.itemRegistry.getObject(loc);
            ItemStack stack = new ItemStack(item);
            if (item.getHasSubtypes()) {
                // if it has subtypes, schedule it for enrichment later using the wildcard value
                stack.setItemDamage(OreDictionary.WILDCARD_VALUE);
            }
            if (stack.getItem() == null) continue;
            // false as we're unsure if it's craftable or not yet
            unsortedStacks.add(new StackWrapper(stack, false));
        }
    }

    private static void identifyLoot(WorldServer world, List<StackWrapper> unsortedStacks) {
        final Map<String, StackWrapper> hashedStacks = new HashMap<>();
        unsortedStacks.forEach(wrapper -> {
            String key = StackHelper.toUniqueKey(wrapper.getStack());
            assert !hashedStacks.containsKey(key);
            hashedStacks.put(key, wrapper);
        });

        final LootTableManager lootTableManager = world.getLootTableManager();
        final List<LootTable> allTables = LootTableList.getAll().stream()
                .map(lootTableManager::getLootTableFromLocation)
                .collect(Collectors.toList());

        // iterate down the JSON tree and fetch what items we can see
        final List<ItemStack> newStacks = iterateLootJsonTree(allTables);

        // add them to the hashed map, trying to avoid replacing already existing stacks
        newStacks.forEach(stack -> {
            String key = StackHelper.toUniqueKey(stack);
            if (hashedStacks.containsKey(key)) {
                final StackWrapper wrapper = hashedStacks.get(key);
                wrapper.setObtainable(true);
            } else {
                hashedStacks.put(key, new StackWrapper(stack, true));
            }
        });

        // clear the entry unsortedStacks and dump them afresh again
        unsortedStacks.clear();
        unsortedStacks.addAll(hashedStacks.values());
    }

    private static List<ItemStack> iterateLootJsonTree(List<LootTable> allTables) {
        List<ItemStack> items = new ArrayList<>();

        // iterating down the JSON tree~
        // check http://minecraft.gamepedia.com/Loot_table for more details
        for (LootTable table : allTables) {
            final JsonElement s = gson.toJsonTree(table, new TypeToken<LootTable>() {
            }.getType());
            JsonArray pools = s.getAsJsonObject().get("pools").getAsJsonArray();
            for (JsonElement aPool : pools) {
                JsonArray entries = aPool.getAsJsonObject().get("entries").getAsJsonArray();
                for (JsonElement anEntry : entries) {
                    JsonObject entry = anEntry.getAsJsonObject();
                    // we only want to deal with item-type entries
                    ModLogger.info(entry.get("type"));
                    if (!entry.get("type").getAsString().equals("item")) continue;
                    String name = entry.get("name").getAsString();

                    if (entry.has("functions")) {
                        for (JsonElement aFunction : entry.get("functions").getAsJsonArray()) {
                            JsonObject function = aFunction.getAsJsonObject();
                            switch (function.get("function").getAsString()) {
                                case "set_data":
                                    JsonElement data = function.get("data");
                                    if (data.isJsonObject()) {
                                        JsonObject dataRange = data.getAsJsonObject();
                                        int min = dataRange.get("min").getAsInt();
                                        int max = dataRange.get("max").getAsInt();
                                        for (int i = min; i <= max; i++) {
                                            items.add(getItemStack(name + ":" + i));
                                        }
                                    } else {
                                        int meta = data.getAsInt();
                                        items.add(getItemStack(name + ":" + meta));
                                    }
                            }
                        }
                    } else {
                        items.add(getItemStack(name));
                    }
                }
            }
        }
        return items;
    }

    private static ItemStack getItemStack(@Nonnull String s) {
        final String[] split = s.split(":");

        ItemStack stack;
        ResourceLocation loc = new ResourceLocation(split[0], split[1]);
        if (Item.itemRegistry.getKeys().contains(loc)) {
            stack = new ItemStack(Item.itemRegistry.getObject(loc));
        } else {
            ModLogger.warning("Weird location: " + s);
            return null;
        }

        if (split.length > 2) {
            // input metadata
            Integer meta = tryParse(split[2]);
            if (meta != null) {
                // there's metadata, add it
                stack.setItemDamage(meta);
            } else {
                ModLogger.warning(String.format("Weird metadata %s in %s", split[2], loc));
            }
        }
        return stack;
    }

    private static Integer tryParse(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Recreates the possible target stack library at TargetHelper according to the current config.
     * This function reads the existing "stacks" information stored in the @world and rewrites the "forcedStacks" to
     * assure any configuration changes are reflected on the saved data for future use.
     *
     * @param world The world on to load and save stack information
     */
    public static void generateLibrary(@Nullable World world) {
        if (world == null && worldSavedDataTargets == null) return;

        if (world != null)
            worldSavedDataTargets = WorldSavedDataTargets.get(world);
        final List<ItemStack> forcedStacks = getForcedStacks();

        // forced stacks keys (for easier match-up)
        final List<String> forced = forcedStacks.stream()
                .map(StackHelper::toUniqueKey)
                .collect(Collectors.toList());

        // generated stacks filter keys
        final Set<String> filter = new HashSet<>();
        for (String s : KismetConfig.getForceAdd()) {
            // skip entries that start with an !
            if (s.startsWith("!")) continue;
            filter.add(standardizeFilter(s));
        }

        // filtered iterable
        List<ItemStack> filteredStacks = worldSavedDataTargets.getStacks().stream().filter(wrapper -> {
            // no nulls pls
            if (wrapper == null || wrapper.getStack() == null) return false;

            // generation mode dictates if we continue or not
            switch (KismetConfig.getGenMode()) {
                case DISABLED:
                    return false;
                case STRICTLY_OBTAINABLE:
                    if (!wrapper.isObtainable()) return false;
                    break;
                case ENABLED:
                    break;
            }

            String name = StackHelper.toUniqueKey(wrapper.getStack());

            // if this stack matches exactly one in the forced list, skip it
            if (forced.stream().anyMatch(name::equals)) return false;

            // now we check against the filter itself
            switch (KismetConfig.getFilterMode()) {
                case WHITELIST:
                    return filter.stream().anyMatch(name::startsWith);
                case BLACKLIST:
                    return filter.stream().noneMatch(name::startsWith);
            }

            // fail safe
            return false;
        }).map(StackWrapper::getStack).collect(Collectors.toList());

        filteredStacks.addAll(forcedStacks);

        TargetHelper.setTargetLibrary(filteredStacks);
    }

    private static List<ItemStack> getForcedStacks() {
        List<ItemStack> forcedStacks = new ArrayList<>();

        // whitelist, only create the stacks outright
        for (String s : KismetConfig.getForceAdd()) {
            // skip entries that start with an !
            // also skip mods on the whitelist as it's only for specific tempStacks
            if (s.startsWith("!") || isMod(s)) continue;
            forcedStacks.add(getItemStack(s));
        }

        return forcedStacks;
    }

    public static void clear() {
        worldSavedDataTargets = null;
    }
}
