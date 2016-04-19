package desutine.kismet.util;

import desutine.kismet.Kismet;
import desutine.kismet.ModLogger;
import desutine.kismet.util.TargetGenerationResult.EnumTargetFailure;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class TargetHelper {

    private static List<ItemStack> targetLibrary;

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
    public static TargetGenerationResult generateTarget(@Nonnull final Map<String, Integer> weights, @Nonnull List<ItemStack> lastTargets) {
        if (targetLibrary == null) return new TargetGenerationResult(EnumTargetFailure.LIST_NOT_READY);

        // saving the mod weights before we removed stacks according to previousTargets
        Map<String, Integer> statelessCount = getModItemCount(targetLibrary);

        // add missing keys to weights
        // using statelessCount because it's the one with less filtering, and hence more possible mod keys
        //  this is important in case of an edge case, seen bellow
        //  also, using weight 1 as this unknown mod could have been added since last time the array was updated
        // sidenote: ty IDEA for the lambdas, omgosh do I love them
        statelessCount.keySet().stream()
                .filter(key -> !weights.containsKey(key))
                .forEach(key -> weights.put(key, 1));

        // clears old keys (ones not present in the current filtered list)
        weights.keySet().removeIf(key -> !statelessCount.containsKey(key));

        // remove the previous targets too while you're at it~
        List<ItemStack> targets = targetLibrary.stream()
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
                    return new TargetGenerationResult(EnumTargetFailure.NO_TARGETS_AVAILABLE);
                } else {
                    // solution to edge case: remove lastTargets, recalculate everything

                    // nuke lastTargets... except for the newest target, to avoid having the same item twice in a row
                    ItemStack previousTarget = lastTargets.get(lastTargets.size() - 1);
                    lastTargets.clear();
                    lastTargets.add(previousTarget);

                    // and to properly remove it, we have to take it out from configFilteredItems
                    // we can use this time to put them into targets too
                    targets = targetLibrary.stream()
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
            return new TargetGenerationResult(EnumTargetFailure.NO_TARGET_MODS_AVAILABLE);
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
                .filter(item -> StackHelper.getMod(item).equalsIgnoreCase(finalTarget))
                .collect(Collectors.toList());
        int targetIndex = random.nextInt(decapsulatedTargets.size());
        ItemStack newTarget = decapsulatedTargets.get(targetIndex);

        // finally, we have a target.
        // but ah, before finishing, we have to add the newly generated target to the lastTarget stacks!
        lastTargets.add(newTarget);
        return new TargetGenerationResult(newTarget);
    }

    private static Map<String, Integer> getModItemCount(Iterable<ItemStack> filteredCompleteList) {
        HashMap<String, Integer> map = new HashMap<>();
        for (ItemStack item : filteredCompleteList) {
            String mod = StackHelper.getMod(item);
            if (!map.containsKey(mod)) {
                map.put(mod, 1);
            } else
                map.put(mod, map.get(mod) + 1);
        }
        return map;
    }

    public static List<ItemStack> getTargetLibrary() {
        return targetLibrary;
    }

    public static void setTargetLibrary(List<ItemStack> targetLibrary) {
        TargetHelper.targetLibrary = targetLibrary;
    }
}
