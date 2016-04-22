package desutine.kismet.target;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import desutine.kismet.Kismet;
import desutine.kismet.ModLogger;
import desutine.kismet.server.WSDTargetDatabase;
import desutine.kismet.util.StackHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEmptyDrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
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
import net.minecraftforge.oredict.OreDictionary;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TargetDatabaseBuilder {
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
    private WSDTargetDatabase WSDTargetDatabase;

    private Queue<List<InformedStack>> remainingPackets = new ArrayDeque<>();

    public TargetDatabaseBuilder(WorldServer world) {
        WSDTargetDatabase = desutine.kismet.server.WSDTargetDatabase.get(world);
    }

    /**
     * Generates the target lists within the game.
     *
     * @param player The player entity to use to enrich the state
     */
    public void generateStacks(EntityPlayerMP player) {
        player.addChatMessage(new TextComponentString("[Kismet] Starting target library reset..."));

        WSDTargetDatabase = desutine.kismet.server.WSDTargetDatabase.get(player.worldObj);
        WSDTargetDatabase.setStacks(new HashMap<>());

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
        drops.forEach(addToStackMap(stacks, InformedStack.ObtainableTypes.Mineable));

        silkDrops.forEach(addToStackMap(stacks, InformedStack.ObtainableTypes.Silkable));
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
        final int oldSize = stacks.size();

        final LootTableManager lootTableManager = world.getLootTableManager();
        final List<LootTable> allTables = LootTableList.getAll().stream()
                .map(lootTableManager::getLootTableFromLocation)
                .collect(Collectors.toList());

        // iterate down the JSON tree and fetch what items we can see
        final Set<String> loots = iterateLootJsonTree(allTables);

        // add them to the hashed map, trying to avoid replacing already existing stacks
        loots.forEach(addToStackMap(stacks, InformedStack.ObtainableTypes.Lootable));

        return stacks.size() - oldSize;
//        FluidRegistry.getBucketFluids();
//        UniversalBucket.getFilledBucket()
    }

    private static @NotNull Consumer<String> addToStackMap(Map<String, InformedStack> stacks, InformedStack.ObtainableTypes type) {
        return key -> {
            ItemStack stack = TargetLibraryBuilder.getItemStack(key);
            if (stack != null && stack.getItem() != null) {
                InformedStack wrapper = new InformedStack(stack, type);
                wrapper.setHasSubtypes(true);
                if (stacks.containsKey(key)) {
                    stacks.get(key).setObtainable(type, true);
                } else {
                    stacks.put(key, wrapper);
                }
            }
        };
    }

    private static Set<String> iterateLootJsonTree(List<LootTable> allTables) {
        Set<String> items = new HashSet<>();

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
                    getItemsFromLootEntry(items, entry);
                }
            }
        }
        return items;
    }

    private static void getItemsFromLootEntry(Set<String> items, JsonObject entry) {
        final String name = entry.get("name").getAsString();
        Set<String> variants = new HashSet<>();
        variants.add(name);

        List<Integer> metaValues = new ArrayList<>();
        int maxCount = 1;
        int maxAddCount = 0;
        NBTTagCompound nbt = null;
        if (entry.has("functions")) {
            for (JsonElement aFunction : entry.get("functions").getAsJsonArray()) {
                JsonObject function = aFunction.getAsJsonObject();
                switch (function.get("function").getAsString()) {
                    case "minecraft:furnace_smelt":
                        // change the item to be the smelted version
                        ItemStack stack = TargetLibraryBuilder.getItemStack(name);
                        final ItemStack smeltedStack = FurnaceRecipes.instance().getSmeltingResult(stack);
                        variants.add(StackHelper.toUniqueKey(smeltedStack));
                        break;
                    case "minecraft:looting_enchant":
                        JsonElement count = function.get("count");
                        if (count.isJsonObject()) {
                            JsonObject countRange = count.getAsJsonObject();
                            maxAddCount = countRange.get("max").getAsInt() * 3;
                        } else {
                            maxAddCount = count.getAsInt() * 3;
                        }
                        break;
                    case "minecraft:set_count":
                        count = function.get("count");
                        if (count.isJsonObject()) {
                            JsonObject countRange = count.getAsJsonObject();
                            maxCount = countRange.get("max").getAsInt();
                        } else {
                            maxCount = count.getAsInt();
                        }
                        break;
                    case "minecraft:set_data":
                        JsonElement data = function.get("data");
                        if (data.isJsonObject()) {
                            JsonObject dataRange = data.getAsJsonObject();
                            int min = dataRange.get("min").getAsInt();
                            int max = dataRange.get("max").getAsInt();
                            // max is inclusive
                            IntStream.range(min, max + 1).forEach(metaValues::add);
                        } else {
                            int meta = data.getAsInt();
                            metaValues.add(meta);
                        }
                        break;
                    case "minecraft:set_nbt":
                        JsonElement tag = function.get("tag");
                        try {
                            nbt = JsonToNBT.getTagFromJson(tag.getAsString());
                        } catch (NBTException e) {
                            ModLogger.warning(e);
                        }
                        break;
                    case "minecraft:enchant_randomly":
                    case "minecraft:enchant_with_levels":
                    case "minecraft:set_damage":
                        // ignored
                        break;
                    default:
                        ModLogger.warning("Loot tables: unknown function, " + function.get("function").getAsString());
                        break;
                }
            }
        }

        if (maxCount + maxAddCount <= 0) {
            ModLogger.warning("Loot tables: empty drop," + name + ":" + (maxCount + maxAddCount));
            return;
        }

        // add meta=0 if we didn't get any data
        if (metaValues.isEmpty()) metaValues.add(0);
        // for each data values, add the item string (+nbt if any)
        for (String variant : variants) {
            for (int meta : metaValues) {
                items.add(String.format("%s:%d%s", variant, meta,
                        nbt != null ? ":" + nbt.toString() : ""));
            }
        }
    }

    public boolean sendNextPacket(EntityPlayerMP player) {
        if (remainingPackets == null || remainingPackets.isEmpty()) return false;
        final List<InformedStack> toSend = remainingPackets.poll();
        if (toSend == null) return false;
        Kismet.network.enrichStacks(toSend, player);
        return true;
    }

    public void recreateLibrary() {
        if (WSDTargetDatabase == null) return;
        TargetLibraryBuilder.recreateLibrary(WSDTargetDatabase.getStacks());
    }

}
