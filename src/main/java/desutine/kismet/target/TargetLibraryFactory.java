package desutine.kismet.target;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import desutine.kismet.Kismet;
import desutine.kismet.ModLogger;
import desutine.kismet.common.ConfigKismet;
import desutine.kismet.server.StackWrapper;
import desutine.kismet.server.WorldSavedDataTargets;
import desutine.kismet.util.StackHelper;
import desutine.kismet.util.TargetHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEmptyDrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class TargetLibraryFactory {
    // "based" on LootTableManager's Gson
    // note: using custom Serializer for LootEntry because of vanilla bug
    private static final Gson gson = (new GsonBuilder())
            .registerTypeAdapter(RandomValueRange.class, new RandomValueRange.Serializer())
            .registerTypeAdapter(LootPool.class, new LootPool.Serializer())
            .registerTypeAdapter(LootTable.class, new LootTable.Serializer())
            .registerTypeHierarchyAdapter(LootEntry.class, new LootEntrySerializerFix())
            .registerTypeHierarchyAdapter(LootFunction.class, new LootFunctionManager.Serializer())
            .registerTypeHierarchyAdapter(LootCondition.class, new LootConditionManager.Serializer())
            .registerTypeHierarchyAdapter(LootContext.EntityTarget.class, new LootContext.EntityTarget.Serializer())
            .create();
    private WorldSavedDataTargets worldSavedDataTargets;

    private Queue<List<StackWrapper>> remainingPackets = new ArrayDeque<>();

    public TargetLibraryFactory(WorldServer world) {
        worldSavedDataTargets = WorldSavedDataTargets.get(world);
    }

    private static void identifyBlockDrops(World world, Map<String, StackWrapper> stacks) {
        // let's now try to get worldgen in this VERY hackish way:
        final Set<String> drops = new HashSet<>();

        Block.blockRegistry.forEach(block -> {
            // only deals with the bedrock edge case but alas
            if (block instanceof BlockEmptyDrops) return;
            ItemStack stack = new ItemStack(block);
            final ImmutableList<IBlockState> validStates = block.getBlockState().getValidStates();
            // check their drops (including if it is silk harvestable)
            for (IBlockState state : validStates) {
                // a state machine that loops around while it adds new items to the drops
                int size = drops.size();
                int chances = 20;
                do {
                    // assuming fortune 5 to get the best drops
                    // hoping the block doesn't do stuff diff with lesser fortunes...
                    // fixme assume the worse described above and test for diff fortunes
                    try {
                        drops.addAll(block.getDrops(world, BlockPos.ORIGIN, state, 5).stream()
                                .map(StackHelper::toUniqueKey)
                                .collect(Collectors.toList()));
                    } catch (Exception e) {
                        ModLogger.error("Error while gathering blocks for " + StackHelper.toUniqueKey(stack) + state, e);
                    }
                    if (size != drops.size()) {
                        size = drops.size();
                        chances = 20;
                    }
                } while (--chances > 0);
            }
        });

        // set all of these as obtainable
        drops.forEach(key -> {
            ItemStack stack = getItemStack(key);
            if (stack != null) {
                StackWrapper wrapper = new StackWrapper(stack, true);
                if (stacks.containsKey(key)) {
                    stacks.get(key).joinWith(wrapper);
                } else {
                    stacks.put(key, wrapper);
                }
            }
        });
    }

    /**
     * Generates the target lists within the game.
     *
     * @param player The player entity to use to enrich the state
     */
    public void generateStacks(EntityPlayerMP player) {
        player.addChatMessage(new TextComponentString("[Kismet] Starting target library reset..."));

        if (worldSavedDataTargets == null) {
            worldSavedDataTargets = WorldSavedDataTargets.get(player.worldObj);
        }
        worldSavedDataTargets.setStacks(new HashMap<>());

        Map<String, StackWrapper> stacks = getRegisteredItems();
        identifyLoot(player.getServerForPlayer(), stacks);
//        identifyBlockDrops(player.worldObj, stacks);

        // separate the stacks per mod, for smaller packets
        final HashMap<String, List<StackWrapper>> modSortedStacks = new HashMap<>();
        for (StackWrapper wrapper : stacks.values()) {
            String mod = StackHelper.getMod(wrapper.getStack());
            if (!modSortedStacks.containsKey(mod)) {
                final ArrayList<StackWrapper> wrappers = new ArrayList<>();
                wrappers.add(wrapper);
                modSortedStacks.put(mod, wrappers);
            } else {
                modSortedStacks.get(mod).add(wrapper);
            }
        }

        remainingPackets.addAll(modSortedStacks.values());
        sendNextPacket(player);
    }

    public void sendNextPacket(EntityPlayerMP player) {
        if (remainingPackets == null || remainingPackets.isEmpty()) return;
        final List<StackWrapper> toSend = remainingPackets.poll();
        if (toSend == null) return;
        Kismet.network.enrichStacks(toSend, player);
    }

    private static Map<String, StackWrapper> getRegisteredItems() {
        final HashMap<String, StackWrapper> stacks = new HashMap<>();

        // add stacks from ItemRegistery
        for (ResourceLocation loc : Item.itemRegistry.getKeys()) {
            Item item = Item.itemRegistry.getObject(loc);
            ItemStack stack = new ItemStack(item);
            if (item.getHasSubtypes()) {
                // if it has subtypes, schedule it for enrichment later using the wildcard value
                stack.setItemDamage(OreDictionary.WILDCARD_VALUE);
            }
            if (stack.getItem() == null) continue;

            final StackWrapper wrapper = new StackWrapper(stack);
            stacks.put(wrapper.toString(), wrapper);
        }
        return stacks;
    }

    /**
     * @param world
     * @param stacks
     * @return Number of new items added from the loot system
     */
    private static int identifyLoot(WorldServer world, Map<String, StackWrapper> stacks) {
        final WorldServer[] worldServers = world.getMinecraftServer().worldServers;
//        ModLogger.info(worldServers);

        final List<ItemStack> lootItems = new ArrayList<>();

        final LootTableManager lootTableManager = world.getLootTableManager();
        final List<LootTable> allTables = LootTableList.getAll().stream()
                .map(lootTableManager::getLootTableFromLocation)
                .collect(Collectors.toList());

        // iterate down the JSON tree and fetch what items we can see
        lootItems.addAll(iterateLootJsonTree(allTables));

        final Map<String, StackWrapper> addedStacks = new HashMap<>();
        // add them to the hashed map, trying to avoid replacing already existing stacks
        lootItems.forEach(stack -> {
            final StackWrapper wrapper = new StackWrapper(stack, true);
            String key = wrapper.toString();

            if (stacks.containsKey(key)) {
                // already on original
                stacks.get(key).joinWith(wrapper);
            } else {
                addedStacks.put(key, wrapper);
            }
        });

        stacks.putAll(addedStacks);
        return addedStacks.size();
    }

    private static List<ItemStack> iterateLootJsonTree(List<LootTable> allTables) {
        List<ItemStack> items = new ArrayList<>();

        // iterating down the JSON tree~
        // check http://minecraft.gamepedia.com/Loot_table for more details
        for (LootTable aTable : allTables) {
            final JsonObject table = gson.toJsonTree(aTable, new TypeToken<LootTable>() {
            }.getType()).getAsJsonObject();
            JsonArray pools = table.getAsJsonArray("pools");
            for (JsonElement aPool : pools) {
                JsonArray entries = aPool.getAsJsonObject().getAsJsonArray("entries");
                for (JsonElement anEntry : entries) {
                    JsonObject entry = anEntry.getAsJsonObject();

                    // we only want to deal with item-type entries
                    if (!entry.get("type").getAsString().equals("item")) continue;
                    String name = entry.get("name").getAsString();

                    boolean addedWithMeta = false;
                    if (entry.has("functions")) {
                        for (JsonElement aFunction : entry.get("functions").getAsJsonArray()) {
                            JsonObject function = aFunction.getAsJsonObject();
                            switch (function.get("function").getAsString()) {
                                case "minecraft:set_data":
                                    JsonElement data = function.get("data");
                                    if (data.isJsonObject()) {
                                        JsonObject dataRange = data.getAsJsonObject();
                                        int min = dataRange.get("min").getAsInt();
                                        int max = dataRange.get("max").getAsInt();
                                        for (int i = min; i <= max; i++) {
                                            items.add(getItemStack(name + ":" + i));
                                        }
                                        addedWithMeta = true;
                                    } else {
                                        int meta = data.getAsInt();
                                        items.add(getItemStack(name + ":" + meta));
                                        addedWithMeta = true;
                                    }
                            }
                        }
                    }
                    if (!addedWithMeta) {
                        items.add(getItemStack(name));
                    }
                }
            }
        }
        return items;
    }

    private static ItemStack getItemStack(@Nonnull String s) {
        final String[] split = s.split(":");

        if (split.length < 2) {
            ModLogger.warning("Weird location: " + s);
            return null;
        }

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
                if (stack.getHasSubtypes()) {
                    stack.setItemDamage(OreDictionary.WILDCARD_VALUE);
                }
            }
        } else {
            if (stack.getHasSubtypes()) {
                stack.setItemDamage(OreDictionary.WILDCARD_VALUE);
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
     */
    public void recreateLibrary() {
        if (worldSavedDataTargets == null) return;
        List<ItemStack> forcedStacks = new ArrayList<>();

        // forced stacks, the ones that are added for sure to the filtered stacks
        for (String s : ConfigKismet.getForceAdd()) {
            // skip entries that start with an !
            // also skip mods on the whitelist as it's only for specific tempStacks
            if (s.startsWith("!") || isMod(s)) continue;
            forcedStacks.add(getItemStack(s));
        }

        // forced stacks keys (for easier match-up)
        final Set<String> forced = forcedStacks.stream()
                .map(StackHelper::toUniqueKey)
                .collect(Collectors.toSet());

        // generated stacks filter keys
        final Set<String> filter = new HashSet<>();
        for (String s : ConfigKismet.getGenFilter()) {
            // skip entries that start with an !
            if (s.startsWith("!")) continue;
            filter.add(standardizeFilter(s));
        }

        // filtered iterable
        List<ItemStack> filteredStacks = new ArrayList<>(forcedStacks);

        worldSavedDataTargets.getStacks().forEach(wrapper -> {
            if (isTarget(forced, filter, wrapper)) {
                filteredStacks.add(wrapper.getStack());
            }
        });

        TargetHelper.setTargetLibrary(filteredStacks);
    }

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
//            Loader.isModLoaded()
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

    private boolean isTarget(Set<String> forced, Set<String> filter, StackWrapper wrapper) {
        // no nulls pls
        if (wrapper == null || wrapper.getStack() == null) return false;

        // generation mode dictates if we continue or not
        switch (ConfigKismet.getGenMode()) {
            case DISABLED:
                return false;
            case STRICTLY_OBTAINABLE:
                if (!wrapper.isObtainable()) return false;
                break;
            case ENABLED:
                break;
            default:
                break;
        }

        String name = wrapper.toString();

        // if this stack matches exactly one in the forced list, skip it (will be added later)
        // and if it's on the filter list, skip it as well (blacklisted)
        return forced.stream().noneMatch(name::equals) && filter.stream().noneMatch(name::startsWith);
    }

}
