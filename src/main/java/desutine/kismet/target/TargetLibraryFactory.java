package desutine.kismet.target;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import desutine.kismet.Kismet;
import desutine.kismet.ModLogger;
import desutine.kismet.common.ConfigKismet;
import desutine.kismet.server.InformedStack;
import desutine.kismet.server.WorldSavedDataTargets;
import desutine.kismet.util.StackHelper;
import desutine.kismet.util.TargetHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEmptyDrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
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
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.common.Loader;
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

    private Queue<List<InformedStack>> remainingPackets = new ArrayDeque<>();

    public TargetLibraryFactory(WorldServer world) {
        worldSavedDataTargets = WorldSavedDataTargets.get(world);
    }

    private static void identifyBlockDrops(World world, Map<String, InformedStack> stacks) {
        // let's now try to get worldgen in this VERY hackish way:
        final Set<String> drops = new HashSet<>();
        final Set<String> silkDrops = new HashSet<>();
        final FakePlayer fakePlayer = FakePlayerFactory.getMinecraft((WorldServer) world);

        Block.blockRegistry.forEach(block -> {
            // only deals with the bedrock edge case but alas
            if (block instanceof BlockEmptyDrops) return;

            final ImmutableList<IBlockState> validStates = block.getBlockState().getValidStates();

            for (IBlockState state : validStates) {
                // check their drops (including if it is silk harvestable)
                drops.addAll(getDropsFromState(world, fakePlayer, block, state));
                // test for silk touchness
                if (block.canSilkHarvest(world, BlockPos.ORIGIN, state, fakePlayer)) {
                    silkDrops.addAll(getSilkDrops(block, state));
                }
            }
        });

        // set all of these as obtainable
        drops.forEach(key -> {
            ItemStack stack = getItemStack(key, false);
            if (stack != null && stack.getItem() != null) {
                InformedStack wrapper = new InformedStack(stack, InformedStack.ObtainableTypes.MINEABLE);
                wrapper.setHasSubtypes(true);
                if (stacks.containsKey(key)) {
                    // NOOP
//                    stacks.get(key).join(wrapper);
                    stacks.get(key).setObtainable(InformedStack.ObtainableTypes.MINEABLE, true);
                } else {
                    stacks.put(key, wrapper);
                }
            }
        });

        silkDrops.forEach(key -> {
            ItemStack stack = getItemStack(key, false);
            if (stack != null && stack.getItem() != null) {
                InformedStack wrapper = new InformedStack(stack, InformedStack.ObtainableTypes.SILKABLE);
                wrapper.setHasSubtypes(true);
                if (stacks.containsKey(key)) {
                    // NOOP
//                    stacks.get(key).join(wrapper);
                    stacks.get(key).setObtainable(InformedStack.ObtainableTypes.SILKABLE, true);
                } else {
                    stacks.put(key, wrapper);
                }
            }
        });
    }

    private static Set<String> getDropsFromState(World world, FakePlayer fakePlayer, Block block, IBlockState state) {
        Set<String> drops = new HashSet<>();
        // if the block is unbreakable in this state, don't even bother
        if (block.getBlockHardness(state, world, BlockPos.ORIGIN) < 0) return drops;

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
                ModLogger.error("Error while gathering blocks for " +
                        StackHelper.toUniqueKey(new ItemStack(block)) + state, e);
                continue;
            }
            if (size != drops.size()) {
                size = drops.size();
                chances = 20;
            }
        } while (--chances > 0);

        return drops;
    }

    /**
     * Gets the silk touch drop for this block, under state.
     * Note: function doesn't check if the block+state is actually silk touchable.
     *
     * @param block
     * @param state
     * @return A String set of the unique keys for the drops
     */
    private static Set<String> getSilkDrops(Block block, IBlockState state) {
        Set<String> drops = new HashSet<>();

        ItemStack silkDrop = block.createStackedBlock(state);
//        Class<?> currentClass = block.getClass();
        // try while we don't have Block.class
//        while (currentClass != null && Block.class.isAssignableFrom(currentClass)) {
//            Method silkDrops = null;
//            try {
//                // as the method is protected, I'll "just" access it with reflection
//                silkDrops = currentClass.getDeclaredMethod("createStackedBlock", IBlockState.class);
//            } catch (SecurityException e) {
//                // no access to the class, abort
//                ModLogger.error("", e);
//                break;
//            } catch (NoSuchMethodException ignored) {
//            }
//
//            if (silkDrops != null) {
//                try {
//                    silkDrops.setAccessible(true);
//                    silkDrop = (ItemStack) silkDrops.invoke(block, state);
//                    // if we reached here, the function was sucessfully invoked
//                    // so we can break the loop and see what we got
//                    break;
//                } catch (IllegalAccessException | InvocationTargetException e) {
//                    // no access to the method, or state is not correctly setup, abort
//                    ModLogger.error("", e);
//                    break;
//                }
//            }
//
//            // loop hasn't terminated, so let's go up one level and try again
//            currentClass = currentClass.getSuperclass();
//        }
        if (silkDrop != null)
            drops.add(StackHelper.toUniqueKey(silkDrop));

        return drops;
    }

    private static Map<String, InformedStack> getRegisteredItems() {
        final HashMap<String, InformedStack> stacks = new HashMap<>();

        // add stacks from ItemRegistery
        for (ResourceLocation loc : Item.itemRegistry.getKeys()) {
            Item item = Item.itemRegistry.getObject(loc);
            ItemStack stack = new ItemStack(item);
            if (stack.getItem() == null) continue;

            stack.setItemDamage(OreDictionary.WILDCARD_VALUE);
            final InformedStack wrapper = new InformedStack(stack);
            stacks.put(wrapper.toString(), wrapper);
        }
        return stacks;
    }

    /**
     * @param world
     * @param stacks
     * @return Number of new items added from the loot system
     */
    private static int identifyLoot(WorldServer world, Map<String, InformedStack> stacks) {
        final List<ItemStack> lootItems = new ArrayList<>();

        final LootTableManager lootTableManager = world.getLootTableManager();
        final List<LootTable> allTables = LootTableList.getAll().stream()
                .map(lootTableManager::getLootTableFromLocation)
                .collect(Collectors.toList());

        // iterate down the JSON tree and fetch what items we can see
        lootItems.addAll(iterateLootJsonTree(allTables));

        final Map<String, InformedStack> addedStacks = new HashMap<>();
        // add them to the hashed map, trying to avoid replacing already existing stacks
        lootItems.forEach(stack -> {
            final InformedStack wrapper = new InformedStack(stack, InformedStack.ObtainableTypes.LOOTABLE);
            wrapper.setHasSubtypes(true);
            String key = wrapper.toString();

            if (stacks.containsKey(key)) {
                // already on original
//                stacks.get(key).join(wrapper);
                stacks.get(key).setObtainable(InformedStack.ObtainableTypes.LOOTABLE, true);
            } else {
                addedStacks.put(key, wrapper);
            }
        });

        stacks.putAll(addedStacks);
        return addedStacks.size();
//        FluidRegistry.getBucketFluids();
//        UniversalBucket.getFilledBucket()
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
                                            items.add(getItemStack(name + ":" + i, false));
                                        }
                                        addedWithMeta = true;
                                    } else {
                                        int meta = data.getAsInt();
                                        items.add(getItemStack(name + ":" + meta, false));
                                        addedWithMeta = true;
                                    }
                            }
                        }
                    }
                    if (!addedWithMeta) {
                        items.add(getItemStack(name + ":0", false));
                    }
                }
            }
        }
        return items;
    }

    /**
     * Recreates the possible target stack library at TargetHelper according to the current config.
     * This function reads the existing "stacks" information stored in the @world and rewrites the "forcedStacks" to
     * assure any configuration changes are reflected on the saved data for future use.
     *
     * @param targets
     */
    public static void recreateLibrary(Collection<InformedStack> targets) {
        Map<String, InformedStack> stacks = new HashMap<>();

        // forced stacks, the ones that are added for sure to the filtered stacks
        addConfigGen(stacks, ConfigKismet.getForceAdd(), InformedStack.ObtainableTypes.FORCED);
        addConfigGen(stacks, ConfigKismet.getHiddenBucketable(), InformedStack.ObtainableTypes.BUCKETABLE);
        addConfigGen(stacks, ConfigKismet.getHiddenCraftable(), InformedStack.ObtainableTypes.CRAFTABLE);
        addConfigGen(stacks, ConfigKismet.getHiddenLootable(), InformedStack.ObtainableTypes.LOOTABLE);
        addConfigGen(stacks, ConfigKismet.getHiddenMineable(), InformedStack.ObtainableTypes.MINEABLE);
        addConfigGen(stacks, ConfigKismet.getHiddenSilkable(), InformedStack.ObtainableTypes.SILKABLE);
        addConfigGen(stacks, ConfigKismet.getHiddenOthers(), InformedStack.ObtainableTypes.OTHERS);
        addConfigGen(stacks, ConfigKismet.getHiddenUnfair(), InformedStack.ObtainableTypes.UNFAIR);

        // add all the targets now to this lis
        targets.forEach(stack -> {
            String key = stack.toString();
            if (stacks.containsKey(key)) {
                stacks.put(key, stacks.get(key).joinWith(stack));
            } else {
                stacks.put(key, stack);
            }
        });

        // generated stacks filter keys
        final Set<String> filter = new HashSet<>();
        List<String> blacklists = new ArrayList<>(ConfigKismet.getHiddenBlacklist());
        blacklists.addAll(ConfigKismet.getGenBlacklist());

        for (String s : blacklists) {
            // skip entries that start with an !
            if (s.startsWith("!")) continue;
            filter.add(standardizeFilter(s));
        }

        // filtered iterable
        List<InformedStack> filteredStacks = new ArrayList<>();

        stacks.values().forEach(wrapper -> {
            if (isTarget(wrapper, filter)) {
                filteredStacks.add(wrapper);
            }
        });

        TargetHelper.setTargetLibrary(filteredStacks);
    }

    private static void addConfigGen(Map<String, InformedStack> forcedStacks, List<String> entries, InformedStack.ObtainableTypes type) {
        for (String entry : entries) {
            // skip entries that start with an !
            // also skip mods on the whitelist as it's only for specific tempStacks
            if (entry.startsWith("!") || isMod(entry)) continue;
            final ItemStack stack = getItemStack(entry, false);
            if (stack == null) continue;

            // add the entries as subtype-having wrappers
            final InformedStack wrapper = new InformedStack(stack, type);
            wrapper.seal();

            String key = wrapper.toString();
            if (forcedStacks.containsKey(key)) {
                forcedStacks.put(key, forcedStacks.get(key).joinWith(wrapper));
            } else
                forcedStacks.put(key, wrapper);
        }
    }

    private static boolean isMod(String s) {
        return !s.contains(":");
    }

    private static ItemStack getItemStack(@Nonnull String s, boolean wildcards) {
        final String[] split = s.split(":");

        if (split.length < 2) {
            ModLogger.warning("Weird location: " + s);
            return null;
        }

        // define item (mod:itemName)
        ItemStack stack;
        ResourceLocation loc = new ResourceLocation(split[0], split[1]);
        if (Item.itemRegistry.getKeys().contains(loc)) {
            stack = new ItemStack(Item.itemRegistry.getObject(loc));
        } else {
            ModLogger.error("Weird location: " + s);
            return null;
        }

        // add metadata
        if (split.length > 2) {
            // input metadata
            Integer meta = tryParse(split[2]);
            if (meta != null) {
                // there's metadata, add it
                stack.setItemDamage(meta);
            } else {
                ModLogger.error(String.format("Weird metadata %s in %s", split[2], s));
                if (wildcards && Kismet.proxy.inferSafeHasSubtypes(stack)) {
                    stack.setItemDamage(OreDictionary.WILDCARD_VALUE);
                }
            }
        } else {
            if (wildcards && Kismet.proxy.inferSafeHasSubtypes(stack)) {
                stack.setItemDamage(OreDictionary.WILDCARD_VALUE);
            }
        }

        // add nbt data
        if (split.length > 3) {
            try {
                NBTTagCompound nbt = JsonToNBT.getTagFromJson(split[3]);
                stack.setTagCompound(nbt);
            } catch (NBTException e) {
                ModLogger.error(String.format("Weird NBT %s in %s", split[3], s), e);
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

    private static String standardizeFilter(@Nonnull String s) {
        // mod filtering
        final String[] split = s.split(":");
        if (split.length == 1) {
            // mod
            // todo check if the mod is present
            // todo check if it's a valid mod name
            return Loader.isModLoaded(split[0]) ? split[0] : null;
        }

        // else treat it as item filtering (with possible metadata)
        ItemStack stack;
        stack = getItemStack(s, true);
        if (stack == null) return null;

        return StackHelper.toUniqueKey(stack);
    }

    private static boolean isTarget(InformedStack wrapper, Set<String> filter) {
        // no nulls pls
        if (wrapper == null || wrapper.getStack() == null) return false;

        // forcefully added stacks always go true, even if they're on the blacklist
        if (wrapper.isObtainable(InformedStack.ObtainableTypes.FORCED)) return true;

        // generation mode dictates if we continue or not
        switch (ConfigKismet.getGenMode()) {
            case NONE:
                return false;
            case FILTERED:
                if (!wrapper.isObtainable()) return false;
                break;
            case ALL:
                break;
            default:
                break;
        }

        String name = wrapper.toString();

        // if this stack matches exactly one in the forced list, skip it (will be added later)
        // and if it's on the filter list, skip it as well (blacklisted)
        return filter.stream().noneMatch(name::startsWith);
    }

    /**
     * Generates the target lists within the game.
     *
     * @param player The player entity to use to enrich the state
     */
    public void generateStacks(EntityPlayerMP player) {
        player.addChatMessage(new TextComponentString("[Kismet] Starting target library reset..."));

        worldSavedDataTargets = WorldSavedDataTargets.get(player.worldObj);
        worldSavedDataTargets.setStacks(new HashMap<>());

        Map<String, InformedStack> stacks = getRegisteredItems();
        identifyLoot(player.getServerForPlayer(), stacks);
        identifyBlockDrops(player.worldObj, stacks);

        // separate the stacks per mod, for smaller packets
        final HashMap<String, List<InformedStack>> modSortedStacks = new HashMap<>();
        for (InformedStack wrapper : stacks.values()) {
            String mod = StackHelper.getMod(wrapper);
            if (!modSortedStacks.containsKey(mod)) {
                final ArrayList<InformedStack> wrappers = new ArrayList<>();
                wrappers.add(wrapper);
                modSortedStacks.put(mod, wrappers);
            } else {
                modSortedStacks.get(mod).add(wrapper);
            }
        }

        remainingPackets.addAll(modSortedStacks.values());
        sendNextPacket(player);
    }

    public boolean sendNextPacket(EntityPlayerMP player) {
        if (remainingPackets == null || remainingPackets.isEmpty()) return false;
        final List<InformedStack> toSend = remainingPackets.poll();
        if (toSend == null) return false;
        Kismet.network.enrichStacks(toSend, player);
        return true;
    }

    public void recreateLibrary() {
        if (worldSavedDataTargets == null) return;
        recreateLibrary(worldSavedDataTargets.getStacks());
    }

}
