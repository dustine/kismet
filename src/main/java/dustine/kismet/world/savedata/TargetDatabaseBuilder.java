package dustine.kismet.world.savedata;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import dustine.kismet.Kismet;
import dustine.kismet.Log;
import dustine.kismet.Reference;
import dustine.kismet.network.message.MessageClientTargets;
import dustine.kismet.server.command.CommandKismet;
import dustine.kismet.target.EnumOrigin;
import dustine.kismet.target.Target;
import dustine.kismet.target.TargetLibrary;
import dustine.kismet.target.TargetPatcher.LootTableSeparator;
import dustine.kismet.util.StackHelper;
import dustine.kismet.util.TargetHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEmptyDrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
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
    private List<Target> clientStacks;
    private List<Target> serverStacks;

    public TargetDatabaseBuilder(final WorldServer world) {
        this.targetDatabase = WSDTargetDatabase.get(world);
    }

    /**
     * Generates the target lists within the game.
     *
     * @param player  The player entity to use to enrich the state
     * @param command If the build action came from a command or not
     */
    public void build(final EntityPlayerMP player, final boolean command) {
        TargetDatabaseBuilder.command = command;
        this.targetDatabase = WSDTargetDatabase.get(player.worldObj);
        this.targetDatabase.clearDatabase();
        final Map<String, Target> targetMap = new HashMap<>();

        identifyOriginsServerSide(player, targetMap);

        this.serverStacks = new ArrayList<>(targetMap.values());
        this.clientStacks = new ArrayList<>();
        this.id = UUID.randomUUID();

        Kismet.network.sendTo(new MessageClientTargets(this.id), player);
    }

    private static void identifyOriginsServerSide(final EntityPlayerMP player, final Map<String, Target> targetMap) {
        final WorldServer world = player.getServerWorld();
        identifyLoot(world, targetMap);
        identifyBlockDrops(world, targetMap);
        identifyBuckets(world, targetMap);
        TargetHelper.identifyRecipes(targetMap);

        targetMap.values().removeIf(target -> target.getOrigins().isEmpty());
        targetMap.values().forEach(Target::refreshHasSubtypes);
    }

    private static void identifyBuckets(final WorldServer world, final Map<String, Target> stacks) {
        final List<ItemStack> buckets = new ArrayList<>();
        // add the vanilla buckets
        buckets.add(new ItemStack(Items.LAVA_BUCKET));
        buckets.add(new ItemStack(Items.MILK_BUCKET));
        buckets.add(new ItemStack(Items.WATER_BUCKET));

        if (FluidRegistry.isUniversalBucketEnabled()) {
            final Set<Fluid> bucketFluids = FluidRegistry.getBucketFluids();
            for (final Fluid fluid : bucketFluids) {
                final ItemStack bucket = UniversalBucket.getFilledBucket(
                        ForgeModContainer.getInstance().universalBucket, fluid);
                buckets.add(bucket);
            }
        }

        buckets.forEach(TargetHelper.joinWithTargetMap(stacks, EnumOrigin.FLUID));
    }

    private static void identifyBlockDrops(final World world, final Map<String, Target> stacks) {
        final Set<String> drops = new HashSet<>();
        final Set<String> silkDrops = new HashSet<>();
        final FakePlayer fakePlayer = FakePlayerFactory.getMinecraft((WorldServer) world);

        Block.REGISTRY.forEach(block -> {
            // only deals with the bedrock edge case but alas
            if (block instanceof BlockEmptyDrops) return;

            final ImmutableList<IBlockState> validStates = block.getBlockState().getValidStates();

            for (final IBlockState state : validStates) {
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
        drops.forEach(TargetHelper.addToTargetMap(stacks, EnumOrigin.BLOCK_DROP));

        silkDrops.forEach(TargetHelper.addToTargetMap(stacks, EnumOrigin.SILK_TOUCH));
    }

    private static Set<String> getDropsFromState(final World world, final FakePlayer fakePlayer, final Block block,
                                                 final IBlockState state) {
        final Set<String> drops = new HashSet<>();

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
                } catch (final Exception e) {
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
    private static Set<String> getSilkDrops(final Block block, final IBlockState state) {
        final Set<String> drops = new HashSet<>();

        final ItemStack silkDrop = block.createStackedBlock(state);

        if (silkDrop != null)
            drops.add(StackHelper.toUniqueKey(silkDrop));

        return drops;
    }

    /**
     * @param world
     * @param stacks
     * @return Number of new items added from the loot system
     */
    private static void identifyLoot(final WorldServer world, final Map<String, Target> stacks) {
        final LootTableSeparator tables = new LootTableSeparator(LootTableList.getAll()).invoke(world);

        // entity loot
        final Set<String> modLoots = iterateLootJsonTree(tables.getEntityTables());
        modLoots.forEach(TargetHelper.addToTargetMap(stacks, EnumOrigin.MOB_DROP));

        // fishing loot
        final Set<String> fishingLoots = iterateLootJsonTree(tables.getFishingTables());
        fishingLoots.forEach(TargetHelper.addToTargetMap(stacks, EnumOrigin.FISHING));

        // remaining loot
        final Set<String> remainingLoots = iterateLootJsonTree(tables.getRemainingTables());
        remainingLoots.forEach(TargetHelper.addToTargetMap(stacks, EnumOrigin.LOOT_TABLE));
    }

    private static Set<String> iterateLootJsonTree(final List<LootTable> allTables) {
        final Set<String> items = new HashSet<>();

        // iterating down the JSON tree~
        // check http://minecraft.gamepedia.com/Loot_table for more details
        for (final LootTable aTable : allTables) {
            final JsonObject table = gson.toJsonTree(aTable, new TypeToken<LootTable>() {
            }.getType()).getAsJsonObject();
            final JsonArray pools = table.getAsJsonArray("pools");
            for (final JsonElement aPool : pools) {
                final JsonArray entries = aPool.getAsJsonObject().getAsJsonArray("entries");
                for (final JsonElement anEntry : entries) {
                    final JsonObject entry = anEntry.getAsJsonObject();

                    // we only want to deal with item-type entries
                    if (!entry.get("type").getAsString().equals("item")) continue;
                    getItemsFromLootEntry(items, entry);
                }
            }
        }
        return items;
    }

    private static void getItemsFromLootEntry(final Set<String> items, final JsonObject entry) {
        final String name = entry.get("name").getAsString();
        final Set<String> variants = new HashSet<>();
        variants.add(name);

        final List<Integer> metaValues = new ArrayList<>();
        int maxCount = 1;
        int maxAddCount = 0;
        NBTTagCompound nbt = null;
        JsonElement count;
        if (entry.has("functions")) {
            for (final JsonElement aFunction : entry.get("functions").getAsJsonArray()) {
                final JsonObject function = aFunction.getAsJsonObject();
                switch (function.get("function").getAsString()) {
                    case "minecraft:furnace_smelt":
                        // change the item to be the smelted version
//                        ItemStack stack = StackHelper.getItemStack(name);
//                        final ItemStack smeltedStack = FurnaceRecipes.instance().getSmeltingResult(stack);
//                        variants.add(smeltedStack.getItem().getRegistryName().toString());
                        break;
                    case "minecraft:looting_enchant":
                        count = function.get("count");
                        if (count.isJsonObject()) {
                            final JsonObject countRange = count.getAsJsonObject();
                            maxAddCount = countRange.get("max").getAsInt() * 3;
                        } else {
                            maxAddCount = count.getAsInt() * 3;
                        }
                        break;
                    case "minecraft:set_count":
                        count = function.get("count");
                        if (count.isJsonObject()) {
                            final JsonObject countRange = count.getAsJsonObject();
                            maxCount = countRange.get("max").getAsInt();
                        } else {
                            maxCount = count.getAsInt();
                        }
                        break;
                    case "minecraft:set_data":
                        final JsonElement data = function.get("data");
                        if (data.isJsonObject()) {
                            final JsonObject dataRange = data.getAsJsonObject();
                            final int min = dataRange.get("min").getAsInt();
                            final int max = dataRange.get("max").getAsInt();
                            // max is inclusive
                            IntStream.range(min, max + 1).forEach(metaValues::add);
                        } else {
                            final int meta = data.getAsInt();
                            metaValues.add(meta);
                        }
                        break;
                    case "minecraft:set_nbt":
                        final JsonElement tag = function.get("tag");
                        try {
                            nbt = JsonToNBT.getTagFromJson(tag.getAsString());
                        } catch (final NBTException e) {
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
        for (final String variant : variants) {
            for (final int meta : metaValues) {
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
        TargetLibrary.build(this.targetDatabase.getDatabase());
    }

    public void buildServerSide(final EntityPlayerMP player) {
        this.targetDatabase = WSDTargetDatabase.get(player.worldObj);
        this.targetDatabase.clearDatabase();

        final Map<String, Target> targetMap = getRegisteredItems();
        identifyOriginsServerSide(player, targetMap);

        this.targetDatabase.joinDatabaseWith(targetMap.values());
    }

    public static Map<String, Target> getRegisteredItems() {
        final Map<String, Target> targetMap = new HashMap<>();

        // add clientStacks from ItemRegistry
        for (final ResourceLocation loc : Item.REGISTRY.getKeys()) {
            final Item item = Item.REGISTRY.getObject(loc);
            final ItemStack stack = new ItemStack(item);
            if (stack.getItem() == null) continue;

//            stack.setItemDamage(OreDictionary.WILDCARD_VALUE);
            final Target target = new Target(stack);
            target.setHasSubtypes(true);
            targetMap.put(target.toString(), target);
        }
        return targetMap;
    }

    public void finishBuilding(final UUID id, final EntityPlayerMP player) {
        this.targetDatabase = WSDTargetDatabase.get(player.worldObj);
        if (!this.id.equals(id)) {
            idError(id, player);
            return;
        }

        final Map<String, Target> targetMap = new HashMap<>();
        this.clientStacks.forEach(t -> targetMap.put(t.toString(), t));

        // join server stacks with client stacks
        for (final Target target : this.serverStacks) {
            final String key = target.toString();
            if (targetMap.containsKey(key)) {
                targetMap.put(key, targetMap.get(key).joinWith(target));
                continue;
            }

            // this target wasn't in client stacks, so try adding it again with hasSubtypes forced to false
            final Target tweakedStack = new Target(target);
            tweakedStack.setHasSubtypes(false);
            // same code as above here
            final String tweakedKey = tweakedStack.toString();
            if (targetMap.containsKey(tweakedKey)) {
                targetMap.put(tweakedKey, targetMap.get(tweakedKey).joinWith(tweakedStack));
            } else {
                Log.debug("Server-generated target have been forcefully added: " + tweakedStack);
                targetMap.put(key, target);
            }
        }

        this.targetDatabase.joinDatabaseWith(targetMap.values());

        if (command) {
            CommandKismet.send(player, new TextComponentString("Finished! Refreshing target library now..."));
        }
        Log.info("Build target database");
        TargetLibrary.build(WSDTargetDatabase.get(player.getEntityWorld()));
        if (command) {
            CommandKismet.send(player, new TextComponentString("Done! Database reset finished."));
        }
    }

    public static boolean isCommand() {
        return command;
    }

    public static void setCommand(final boolean command) {
        TargetDatabaseBuilder.command = command;
    }

    private void idError(final UUID id, final EntityPlayerMP player) {
        this.targetDatabase = WSDTargetDatabase.get(player.worldObj);
        if (command) {
            player.addChatMessage(
                    new TextComponentString(String.format("[%s] Error, internal ID mismatch.", Reference.MOD_ID))
                            .setStyle(new Style().setColor(TextFormatting.RED))
            );
        }
        Log.error(String.format("ID mismatch S:%s C:%s", this.id, id));
    }

    public boolean receiveClientTargets(final List<Target> stacks, final UUID id, final EntityPlayerMP player) {
        this.targetDatabase = WSDTargetDatabase.get(player.worldObj);
        if (!this.id.equals(id)) {
            idError(id, player);
            return false;
        }
        this.clientStacks.addAll(stacks);
        return true;
    }
}
