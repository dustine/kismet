package dustine.kismet.world.savedata;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import dustine.kismet.Kismet;
import dustine.kismet.Log;
import dustine.kismet.network.message.MessageClientTargets;
import dustine.kismet.server.command.CommandKismet;
import dustine.kismet.target.EnumOrigin;
import dustine.kismet.target.InformedStack;
import dustine.kismet.target.TargetHelper;
import dustine.kismet.target.TargetLibraryBuilder;
import dustine.kismet.target.TargetPatcher.LootTableSeparator;
import dustine.kismet.util.StackHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEmptyDrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
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
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TargetDatabaseBuilder {
    // "based" on LootTableManager's Gson
    // note: using custom Serializer for LootEntry because of vanilla bug
    private static final Gson gson = (new GsonBuilder())
            .registerTypeAdapter(RandomValueRange.class, new RandomValueRange.Serializer())
            .registerTypeAdapter(LootPool.class, new LootPool.Serializer())
            .registerTypeAdapter(LootTable.class, new LootTable.Serializer())
            .registerTypeHierarchyAdapter(LootEntry.class, new LootEntrySerializerFix()) // hack
            .registerTypeHierarchyAdapter(LootFunction.class, new LootFunctionManager.Serializer())
            .registerTypeHierarchyAdapter(LootCondition.class, new LootConditionManager.Serializer())
            .registerTypeHierarchyAdapter(LootContext.EntityTarget.class, new LootContext.EntityTarget.Serializer())
            .create();
    private static boolean command = false;
    private WSDTargetDatabase targetDatabase;
    private UUID id;
    private List<InformedStack> clientStacks;
    private List<InformedStack> serverStacks;

    public TargetDatabaseBuilder(WorldServer world) {
        this.targetDatabase = WSDTargetDatabase.get(world);
    }

    /**
     * Generates the target lists within the game.
     *
     * @param player  The player entity to use to enrich the state
     * @param command If the build action came from a command or not
     */
    public void build(EntityPlayerMP player, boolean command) {
        TargetDatabaseBuilder.command = command;
        this.targetDatabase = WSDTargetDatabase.get(player.worldObj);
        this.targetDatabase.setDatabase(new HashMap<>());

        final WorldServer world = player.getServerWorld();
//        Map<String, InformedStack> stacks = getRegisteredItems();
        Map<String, InformedStack> stacks = new HashMap<>();
        identifyLoot(world, stacks);
        identifyBlockDrops(world, stacks);
        identifyBuckets(world, stacks);

        stacks.values().forEach(InformedStack::refreshHasSubtypes);
        stacks.values().forEach(InformedStack::seal);

        this.serverStacks = new ArrayList<>(stacks.values());
        this.clientStacks = new ArrayList<>();
        this.id = UUID.randomUUID();

        Kismet.network.sendTo(new MessageClientTargets(this.id), player);
    }

    private static void identifyBuckets(WorldServer world, Map<String, InformedStack> stacks) {
        final List<ItemStack> buckets = new ArrayList<>();
        // add the vanilla buckets
        buckets.add(new ItemStack(Items.LAVA_BUCKET));
        buckets.add(new ItemStack(Items.MILK_BUCKET));
        buckets.add(new ItemStack(Items.WATER_BUCKET));

        if (FluidRegistry.isUniversalBucketEnabled()) {
            final Set<Fluid> bucketFluids = FluidRegistry.getBucketFluids();
            for (Fluid fluid : bucketFluids) {
                final ItemStack bucket = UniversalBucket.getFilledBucket(
                        ForgeModContainer.getInstance().universalBucket, fluid);
                buckets.add(bucket);
            }
        }

        buckets.forEach(TargetHelper.joinWithStackMap(stacks, EnumOrigin.FLUID));
    }

    private static void identifyBlockDrops(World world, Map<String, InformedStack> stacks) {
        final Set<String> drops = new HashSet<>();
        final Set<String> silkDrops = new HashSet<>();
        final FakePlayer fakePlayer = FakePlayerFactory.getMinecraft((WorldServer) world);

        Block.REGISTRY.forEach(block -> {
            // only deals with the bedrock edge case but alas
            if (block instanceof BlockEmptyDrops) return;

            final ImmutableList<IBlockState> validStates = block.getBlockState().getValidStates();

            for (IBlockState state : validStates) {
                // if the block is unbreakable in this state, don't even bother
                if (block.getBlockHardness(state, world, BlockPos.ORIGIN) < 0) continue;

                // check their drops (including if it is silk harvested)
                drops.addAll(getDropsFromState(world, fakePlayer, block, state));
                // test for silk touch
                if (block.canSilkHarvest(world, BlockPos.ORIGIN, state, fakePlayer)) {
                    silkDrops.addAll(getSilkDrops(block, state));
                }
            }
        });

        silkDrops.removeAll(drops);

        // set all of these as obtainable
        drops.forEach(TargetHelper.addToStackMap(stacks, EnumOrigin.BLOCK_DROP));

        silkDrops.forEach(TargetHelper.addToStackMap(stacks, EnumOrigin.SILK_TOUCH));
    }

    private static Set<String> getDropsFromState(World world, FakePlayer fakePlayer, Block block, IBlockState state) {
        Set<String> drops = new HashSet<>();

        // a state machine that loops around while it adds new items to the drops
        for (int fortune = 5; fortune >= 0; --fortune) {
            int size = drops.size();
            int chances = 50;
            do {
                // assuming fortune 5 to get the best drops
                // hoping the block doesn't do stuff diff with lesser fortunes...
                // fixme assume the worse described above and test for diff fortunes
                try {
                    drops.addAll(block.getDrops(world, BlockPos.ORIGIN, state, fortune).stream()
                            .map(StackHelper::toUniqueKey)
                            .collect(Collectors.toList()));
                } catch (Exception e) {
                    Log.error("Error while gathering blocks for " +
                            StackHelper.toUniqueKey(new ItemStack(block)) + state, e);
                    continue;
                }
                if (size != drops.size()) {
                    size = drops.size();
                    chances = 50;
                }
            } while (--chances > 0);
        }

        return drops;
    }

    /**
     * Gets the silk touch drop for this block, under state. Note: function doesn't check if the block+state is actually
     * silk touchable.
     *
     * @param block
     * @param state
     * @return A String set of the unique keys for the drops
     */
    private static Set<String> getSilkDrops(Block block, IBlockState state) {
        Set<String> drops = new HashSet<>();

        ItemStack silkDrop = block.createStackedBlock(state);

        if (silkDrop != null)
            drops.add(StackHelper.toUniqueKey(silkDrop));

        return drops;
    }

    /**
     * @param world
     * @param stacks
     * @return Number of new items added from the loot system
     */
    private static void identifyLoot(WorldServer world, Map<String, InformedStack> stacks) {
        final LootTableSeparator tables = new LootTableSeparator(LootTableList.getAll()).invoke(world);

        // entity loot
        final Set<String> modLoots = iterateLootJsonTree(tables.getEntityTables());
        modLoots.forEach(TargetHelper.addToStackMap(stacks, EnumOrigin.MOB_DROP));

        // fishing loot
        final Set<String> fishingLoots = iterateLootJsonTree(tables.getFishingTables());
        fishingLoots.forEach(TargetHelper.addToStackMap(stacks, EnumOrigin.FISHING));

        // remaining loot
        final Set<String> remainingLoots = iterateLootJsonTree(tables.getRemainingTables());
        remainingLoots.forEach(TargetHelper.addToStackMap(stacks, EnumOrigin.LOOT_TABLE));
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
                        ItemStack stack = StackHelper.getItemStack(name);
                        final ItemStack smeltedStack = FurnaceRecipes.instance().getSmeltingResult(stack);
                        variants.add(smeltedStack.getItem().getRegistryName().toString());
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
                            Log.warning(e);
                        }
                        break;
                    case "minecraft:enchant_randomly":
                    case "minecraft:enchant_with_levels":
                    case "minecraft:set_damage":
                        // ignored
                        break;
                    default:
                        Log.warning("Loot tables: unknown function, " + function.get("function").getAsString());
                        break;
                }
            }
        }

        if (maxCount + maxAddCount <= 0) {
            Log.warning("Loot tables: empty drop," + name + ":" + (maxCount + maxAddCount));
            return;
        }

        // add meta=0 if we didn't get any data
        if (metaValues.isEmpty()) metaValues.add(0);
        // for each data values, add the item TARGET_DATABASE (+nbt if any)
        for (String variant : variants) {
            for (int meta : metaValues) {
                items.add(String.format("%s:%d%s", variant, meta,
                        nbt != null ? ":" + nbt.toString() : ""));
            }
        }
    }

    /**
     * Rebuilds the library with the last used generated target database
     */
    public void tryBuildLibraryWithLastGeneratedDatabase() {
        if (this.targetDatabase == null || !this.targetDatabase.isValid()) return;
        TargetLibraryBuilder.build(this.targetDatabase.getDatabase());
    }

    public void buildServerSide(EntityPlayerMP player) {
        this.targetDatabase = WSDTargetDatabase.get(player.worldObj);
        this.targetDatabase.setDatabase(new HashMap<>());

        Map<String, InformedStack> stacks = getRegisteredItems();
        stacks.forEach((s, stack) -> stack.getStack().setItemDamage(0));

        final WorldServer world = player.getServerWorld();
        identifyLoot(world, stacks);
        identifyBlockDrops(world, stacks);
        identifyBuckets(world, stacks);
        TargetHelper.identifyRecipes(stacks);

        stacks.values().removeIf(stack -> stack.getOrigins().isEmpty());
        stacks.values().forEach(InformedStack::refreshHasSubtypes);
        stacks.values().forEach(InformedStack::seal);

        this.targetDatabase.enrichStacks(stacks.values());
    }

    public static Map<String, InformedStack> getRegisteredItems() {
        final HashMap<String, InformedStack> stacks = new HashMap<>();

        // add clientStacks from ItemRegistry
        for (ResourceLocation loc : Item.REGISTRY.getKeys()) {
            Item item = Item.REGISTRY.getObject(loc);
            ItemStack stack = new ItemStack(item);
            if (stack.getItem() == null) continue;

            stack.setItemDamage(OreDictionary.WILDCARD_VALUE);
            final InformedStack wrapper = new InformedStack(stack);
            wrapper.setHasSubtypes(true);
            stacks.put(wrapper.toString(), wrapper);
        }
        return stacks;
    }

    public void finishBuilding(UUID id, EntityPlayerMP player) {
        this.targetDatabase = WSDTargetDatabase.get(player.worldObj);
        if (!this.id.equals(id)) {
            idError(id, player);
            return;
        }

        final HashMap<String, InformedStack> stacks = new HashMap<>();
        this.clientStacks.forEach(stack -> stacks.put(stack.toString(), stack));

        // join server stacks with client stacks
        for (InformedStack stack : this.serverStacks) {
            final String key = stack.toString();
            if (stacks.containsKey(key)) {
                stacks.put(key, stacks.get(key).joinWith(stack));
                continue;
            }

            // this stack wasn't in client stacks, so try adding it again with hasSubtypes forced to false
            final InformedStack tweakedStack = InformedStack.getUnsealedCopy(stack);
            tweakedStack.setHasSubtypes(false);
            tweakedStack.seal();
            // same code as above here
            final String tweakedKey = tweakedStack.toString();
            if (stacks.containsKey(tweakedKey)) {
                stacks.put(tweakedKey, stacks.get(tweakedKey).joinWith(tweakedStack));
            } else {
                Log.debug("Server-generated target have been forcefully added: " + tweakedStack);
                stacks.put(key, stack);
            }
        }

        this.targetDatabase.enrichStacks(stacks.values());

        if (command) {
            if (TargetDatabaseBuilder.isCommand()) {
                CommandKismet.send(player, "Finished! Refreshing target library now...");
            }
            Log.info("Build target database");
            TargetLibraryBuilder.build(WSDTargetDatabase.get(player.getEntityWorld()));
            if (TargetDatabaseBuilder.isCommand()) {
                CommandKismet.send(player, "Done! Database reset finished.");
            }
        }
    }

    public static boolean isCommand() {
        return command;
    }

    public static void setCommand(boolean command) {
        TargetDatabaseBuilder.command = command;
    }

    private void idError(UUID id, EntityPlayerMP player) {
        this.targetDatabase = WSDTargetDatabase.get(player.worldObj);
        if (command) {
            player.addChatMessage(new TextComponentString("§c%s Error, internal ID mismatch."));
        }
        Log.error(String.format("ID mismatch S:%s C:%s", this.id, id));
    }

    public boolean receiveClientTargets(List<InformedStack> stacks, UUID id, EntityPlayerMP player) {
        this.targetDatabase = WSDTargetDatabase.get(player.worldObj);
        if (!this.id.equals(id)) {
            idError(id, player);
            return false;
        }
        this.clientStacks.addAll(stacks);
        return true;
    }
}
