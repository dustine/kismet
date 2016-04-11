package desutine.kismet.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import desutine.kismet.Kismet;
import desutine.kismet.ModLogger;
import desutine.kismet.common.KismetConfig;
import desutine.kismet.server.TargetsWorldSavedData;
import desutine.kismet.server.TargetsWorldSavedData.WrapperTarget;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class TargetHelper {
    //    private static boolean isReady;
    // "based" on LootTableManager's Gson
    private static final Gson gson = (new GsonBuilder()).registerTypeAdapter(RandomValueRange.class, new RandomValueRange.Serializer()).registerTypeAdapter(LootPool.class, new LootPool.Serializer()).registerTypeAdapter(LootTable.class, new LootTable.Serializer()).registerTypeHierarchyAdapter(LootEntry.class, new LootEntry.Serializer()).registerTypeHierarchyAdapter(LootFunction.class, new LootFunctionManager.Serializer()).registerTypeHierarchyAdapter(LootCondition.class, new LootConditionManager.Serializer()).registerTypeHierarchyAdapter(LootContext.EntityTarget.class, new LootContext.EntityTarget.Serializer()).create();
    // private static Set<ItemStack> tempStacks = new HashSet<>();
    private static List<WrapperTarget> tempStacks;
    private static List<ItemStack> filteredStacks;
    private static int remainingPackets;
    private static TargetsWorldSavedData targetsWorldSavedData;

    /**
     * todo rewrite this all and remove comments from function body
     * Pick a random mod, using as a metric w*#stacks(mod), where w is a non-negative integer related to the previous
     * occurence of targets of this mod, and #stacks the number of ItemStacks, related to this mod, eligable to be
     * picked as a target.
     * <p>
     * Now weight is calculated as such:
     * - all mods start as weight 1
     * - when a mod isn't chosen as a target, its weight increases by 1
     * - if it is chosen, it is reset to 0
     * <p>
     * This makes it so that mods are never* chosen twice in a row, and the longer a mod goes without being picked the higher its chances are.
     * But that's weight alone, what about the item quantity bit? That's to account for how some mods just have more stuff than others. If the randomization is done by weight alone, mods with few stacks will cause those stacks to pop up a lot more frequently than others. This sounds good in first sight, but it would lead to inevitable item repetition, even with the weight counterbalance.
     *
     * @param weights
     * @param lastTargets
     * @return
     */
    public static Target generateTarget(@Nonnull final Map<String, Integer> weights, @Nonnull List<ItemStack> lastTargets) {
        if (filteredStacks == null) return new Target(Target.EnumTargetFailure.LIST_NOT_READY);

        // saving the mod weights before we removed stacks according to previousTargets
        Map<String, Integer> statelessCount = getModItemCount(filteredStacks);

        // add missing keys to weights
        // using statelessCount because it's the one with less filtering, and hence more possible mod keys
        //  this is important in case of an edge case, seen bellow
        //  also, using weight 1 as this unknown mod could have been added since last time the array was updated
        // sidenote: ty IDEA for the lambdas, omgosh do I love them
        statelessCount.keySet().stream()
                .filter(key -> !weights.containsKey(key))
                .forEach(key -> weights.put(key, 1));
        // clears old keys (ones not present in the current filtered list)
        weights.keySet().stream()
                .filter(key -> !statelessCount.containsKey(key))
                .forEach(weights::remove);

        // remove the previous targets too while you're at it~
        List<ItemStack> targets = filteredStacks.stream()
                .filter(stack -> lastTargets.stream().noneMatch(stack1 -> StackHelper.isEquivalent(stack, stack1)))
                .collect(Collectors.toList());

        // get the weight of each mod's stacks, after filtering
        Map<String, Integer> metrics = new HashMap<>();
        // and save a copy of only the item count, just in case
        // todo check if a shallow copy is enough here
        Map<String, Integer> count = getModItemCount(targets);

        // do nrItems * weight on any metrics (explained bellow)
        for (String key : weights.keySet()) {
            // first of all, there never should be negative weights, so let's shift them to zero
            if (weights.get(key) < 0)
                weights.put(key, 0);
            if (count.containsKey(key)) {
                metrics.put(key, count.get(key) * weights.get(key));
            } else {
                metrics.put(key, 0);
                count.put(key, 0);
            }
        }

        Random random = Kismet.random;

        // get max weights
        int max = metrics.values().stream().reduce(0, (i, j) -> i + j);
        if (max <= 0) {
            // sum(metrics) = 0, so we're under an edge case
            // we artificially increase the weights (nulls last

            HashMap<String, Integer> statelessMetrics = new HashMap<>();
            for (String key : weights.keySet()) {
                int modifiedWeight = weights.get(key);
                // why increase positive weights? the issue's on the zero ones
                // contingency: while negative weights should never happen, let's fix them up too (again)
                //  no extra cycles wasted anyway, and if this is properly optimized by the JVM, it takes the same
                //  number of instructions too
                if (modifiedWeight <= 0) modifiedWeight = 1;
                // we change both now because later, if this process failed, the next test(s) would require to increase
                //  statelessMetrics manually anyway
                if (!statelessCount.containsKey(key))
                    statelessCount.put(key, 1);
                statelessMetrics.put(key, statelessCount.get(key) * modifiedWeight);
                metrics.put(key, count.get(key) * modifiedWeight);
            }

            max = metrics.values().stream().reduce(0, (i, j) -> i + j);
            if (max <= 0) {
                // max is still 0
                // so we have ruled out outrun mods (weight's fault). now, the blame shifts into the previousTargets
                //  (modCount's fault). thankfully, we saved up the unfiltered stacks above, didn't we? .u.
                // now, logic would say we removed the oldest targets and moved forward, but there's a problem that
                //  appears from this. the new sequence of targets would closely match the old one, seeing that we
                //  already ruled out mod burnout previously. so best to ignore lastTargets completely, and if that
                //  solves it, nuke it completely and start over. there's a chance some blocks are repeated but hey,
                //  what's the fun of randomness if stuff isn't repeated?
                metrics = statelessCount;

                max = metrics.values().stream().reduce(0, (i, j) -> i + j);
                if (max <= 1) {
                    // we ran out of targets completely, report failure
                    return new Target(Target.EnumTargetFailure.NO_TARGETS_AVAILABLE);
                } else {
                    // solution to edge case: remove lastTargets, recalculate everything

                    // nuke lastTargets... except for the newest target, to avoid having the same item twice in a row
                    ItemStack previousTarget = lastTargets.get(lastTargets.size() - 1);
                    lastTargets.clear();
                    lastTargets.add(previousTarget);

                    // and to properly remove it, we have to take it out from configFilteredItems
                    // we can use this time to put them into targets too
                    targets = filteredStacks.stream()
                            .filter(item -> !StackHelper.isEquivalent(item, previousTarget))
                            .collect(Collectors.toList());

                    // reset weights too, seeing that to get here you needed to already consider it broken
                    //  as extra incentive, it allows for added randomization, seeing that all the old targets will
                    //  be back!
                    metrics = statelessMetrics;
                    weights.clear();
                    weights.replaceAll((s, integer) -> 1);

                    // and with all it done with, log
                    ModLogger.trace("Edge case resolved, too many targets");
                }

            } else {
                // well this contingency worked, so let's reset the variables to a workable state
                // so any of the following logic and/or cycles doesn't require any extra logic in it

                // starting with adding one to each modWeight with any modCount
                weights.keySet().stream()
                        .filter(key -> weights.get(key) <= 0 && count.get(key) > 0)
                        .forEach(key -> weights.put(key, 1));

                // metrics was already altered above so that one's ready to go
                // let's just log it
                ModLogger.trace("Edge case resolved, all mods with weight 0");
            }
        }

        // no edge case so let's move on right along on schedule
        // get a random number  on [0, max)
        int r = random.nextInt(max);
        // now iterate over the cumulative sum of the weights until you get a sum bigger than r
        int sum = 0;
        String targetMod = null;
        for (String mod : metrics.keySet()) {
            sum += metrics.get(mod);
            if (sum > r) {
                targetMod = mod;
                break;
            }
        }
        // target should always be assigned a value, but just in case...
        if (targetMod == null) {
            ModLogger.error(String.format("Failed to get a targeted mod, from %s and %s", weights, lastTargets));
            return new Target(Target.EnumTargetFailure.NO_TARGET_MODS_AVAILABLE);
        }
        // a mod has been chosen, now time to get some random block from within!
        //   oh but first we update the mod weights, so that they all increase by 1 EXCEPT the chosen one, that one
        //   reset to zero
        for (String key : weights.keySet()) {
            if (key.equalsIgnoreCase(targetMod))
                weights.put(key, 0);
            else {
                // only increase by 1 if the mod still has stacks to offer
                if (count.containsKey(key) && count.get(key) > 0)
                    weights.put(key, weights.get(key) + 1);
            }
        }

        // okay, now we can truly finally pick a target
        // configFilteredItems is already filtered to only have valid stacks so it's just a process of picking a
        // random index and returning the contents on that index
        final String finalTarget = targetMod;
        List<ItemStack> decapsulatedTargets = targets.stream()
                .filter(item -> getMod(item).equalsIgnoreCase(finalTarget))
                .collect(Collectors.toList());
        int targetIndex = random.nextInt(decapsulatedTargets.size());
        ItemStack newTarget = decapsulatedTargets.get(targetIndex);

        // finally, we have a target.
        // but ah, before finishing, we have to add the newly generated target to the lastTarget stacks!
        lastTargets.add(newTarget);
        return new Target(newTarget);
    }

    private static Map<String, Integer> getModItemCount(Iterable<ItemStack> filteredCompleteList) {
        HashMap<String, Integer> map = new HashMap<>();
        for (ItemStack item : filteredCompleteList) {
            String mod = getMod(item);
            if (!map.containsKey(mod)) {
                map.put(mod, 1);
            } else
                map.put(mod, map.get(mod) + 1);
        }
        return map;
    }

    private static String getMod(ItemStack item) {
        return item.getItem().getRegistryName().getResourceDomain();
    }

    private static boolean isMod(String s) {
        return !s.contains(":");
    }

    private static String standardizeFilter(@Nonnull String s, boolean wildcard) {
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
            stack = getItemStack(s, wildcard);
            if (stack == null) return null;

            return StackHelper.stackToString(stack);
        } else {
            ModLogger.warning("Weird location: " + s);
            return null;
        }
    }

    private static ItemStack getItemStack(@Nonnull String s, boolean wildcard) {
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
                // metadata ignored
                if (wildcard) addWildcardIfSubtypes(stack);
            }
        } else {
            // no inputted metadata
            if (wildcard) addWildcardIfSubtypes(stack);
        }
        return stack;
    }

    private static void addWildcardIfSubtypes(ItemStack stack) {
        if (stack.getHasSubtypes()) {
            // if we have subtypes, the rule means all variations are allowed
            stack.setItemDamage(OreDictionary.WILDCARD_VALUE);
        }
    }

    private static Integer tryParse(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Generates the target lists within the game.
     *
     * @param player The player entity to use to enrich the state
     */
    public static void generateStacks(EntityPlayerMP player) {
        boolean generating = true;
        tempStacks = new ArrayList<>();
        addRegisteredItems();
        identifyLoot();

        // separate the stacks per mod, for smaller packets
        Map<String, List<WrapperTarget>> modSortedStacks = new HashMap<>();
        for (WrapperTarget wrapper : tempStacks) {
            String mod = getMod(wrapper.getStack());
            if (!modSortedStacks.containsKey(mod)) {
                final ArrayList<WrapperTarget> wrappers = new ArrayList<>();
                wrappers.add(wrapper);
                modSortedStacks.put(mod, wrappers);
            } else {
                modSortedStacks.get(mod).add(wrapper);
            }
        }
        tempStacks.clear();

//        modSortedStacks.values().stream().map(List::size).reduce((i, j) -> i+j);
        remainingPackets = modSortedStacks.size();
        modSortedStacks.forEach((s, list) -> {
            targetsWorldSavedData = TargetsWorldSavedData.get(player.getServerForPlayer());
            Kismet.packetHandler.enrichStacks(list, player);
        });
    }

    private static void addRegisteredItems() {
        // add stacks from ItemRegistery
        for (ResourceLocation loc : Item.itemRegistry.getKeys()) {
            Item item = Item.itemRegistry.getObject(loc);
            ItemStack stack = new ItemStack(item);
            if (item.getHasSubtypes()) {
                // if it has subtypes, be a lovely dear and schedule it for enrichment later
                // using the wildcard value
                stack.setItemDamage(OreDictionary.WILDCARD_VALUE);
            }
            if (stack.getItem() == null) continue;
            // false as we're unsure if it's craftable or not yet
            tempStacks.add(new WrapperTarget(stack, false));
        }
    }

    private static void identifyLoot() {
        final WorldServer world = DimensionManager.getWorld(0);
        final LootTableManager lootTableManager = world.getLootTableManager();
        final List<LootTable> allTables = LootTableList.getAll().stream()
                .map(lootTableManager::getLootTableFromLocation)
                .collect(Collectors.toList());

        for (LootTable table : allTables) {
//            tableSerializer = new LootTable.Serializer();
            final JsonElement s = gson.toJsonTree(table, new TypeToken<LootTable>() {
            }.getType());
//            ModLogger.info(s);
        }
    }

    public static void enrichItems(List<WrapperTarget> newStacks) {
        TargetHelper.tempStacks.addAll(newStacks);
        --remainingPackets;
        if (remainingPackets <= 0) {
            targetsWorldSavedData.setStacks(tempStacks);
            refreshFilteredStacks();
        }
    }

    public static void refreshFilteredStacks() {
        updateForcedStacks();

        final List<ItemStack> forcedStacks = targetsWorldSavedData.getForcedStacks();
        // whitelist (based from forcedStacks)
        final List<String> whitelist = forcedStacks.stream()
                .map(StackHelper::stackToString)
                .collect(Collectors.toList());

        // blacklist
        final Set<String> blacklist = new HashSet<>();
        for (String s : KismetConfig.getWhitelist()) {
            // skip entries that start with an !
            if (s.startsWith("!")) continue;
            blacklist.add(standardizeFilter(s, true));
        }

        // filtered iterable
        List<ItemStack> newFilteredStacks = targetsWorldSavedData.getStacks().stream().filter(wrapper -> {
            // no nulls pls
            if (wrapper == null) return false;

            String name = StackHelper.stackToString(wrapper.getStack());
            // if it's on the whitelist, return false, as they will be on the filter already
            // but it needs to be an exact match, to avoid duplicates and/or missing stacks
            if (whitelist.stream().anyMatch(name::equals)) return false;
            // and if it is on the blacklist, return false
            if (blacklist.stream().anyMatch(name::startsWith)) return false;
            // else it depends on the addMode if it gets added
            switch (KismetConfig.getAddMode()) {
                case WHITELIST_ONLY:
                    return false;
                case ADD_STRICTLY_OBTAINABLE:
                    return wrapper.isObtainable();
                case ADD_ALL_POSSIBLE:
                    return true;
            }
            // fail safe
            return false;
        }).map(WrapperTarget::getStack).collect(Collectors.toList());

        newFilteredStacks.addAll(forcedStacks);

        filteredStacks = newFilteredStacks;
    }

    private static void updateForcedStacks() {
        List<ItemStack> forcedStacks = new ArrayList<>();

        // whitelist, only create the stacks outright
        for (String s : KismetConfig.getWhitelist()) {
            // skip entries that start with an !
            // also skip mods on the whitelist as it's only for specific tempStacks
            if (s.startsWith("!") || isMod(s)) continue;
            forcedStacks.add(getItemStack(s, false));
        }

        targetsWorldSavedData.setForcedStacks(forcedStacks);
    }

}
