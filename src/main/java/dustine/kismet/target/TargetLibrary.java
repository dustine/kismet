package dustine.kismet.target;

import dustine.kismet.Kismet;
import dustine.kismet.Log;
import dustine.kismet.target.TargetGenerationResult.EnumTargetFailure;
import dustine.kismet.util.StackHelper;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class TargetLibrary {

    private static List<InformedStack> library;

    /**
     * todo rewrite this all and remove comments from function body Pick a RANDOM mod, using as a metric w*#stacks(mod),
     * where w is a non-negative integer related to the previous occurrence of targets of this mod, and #stacks the
     * number of ItemStacks, related to this mod, eligible to be picked as a target.
     * <p>
     * Now weight is calculated as such: - all mods start as weight 1 - when a mod isn't chosen as a target, its weight
     * increases by 1 - if it is chosen, it is reset to 0
     * <p>
     * This makes it so that mods are never* chosen twice in a row, and the longer a mod goes without being picked the
     * higher its chances are. But that's weight alone, what about the item quantity bit? That's to account for how some
     * mods just have more stuff than others. If the randomization is done by weight alone, mods with few stacks will
     * cause those stacks to pop up a lot more frequently than others. This sounds good in first sight, but it would
     * lead to inevitable item repetition, even with the weight counterbalance.
     *
     * @param weights
     * @param lastTargets
     * @return
     */
    public static TargetGenerationResult generateTarget(@Nonnull final Map<String, Integer> weights, @Nonnull List<InformedStack> lastTargets) {
        if (library == null) return new TargetGenerationResult(EnumTargetFailure.LIST_NOT_READY);

        // saving the mod weights before we removed stacks according to previousTargets
        Map<String, Integer> statelessCount = getModItemCount(library);

        // clears old keys from weights (ones not present in the current filtered list)
        weights.keySet().removeIf(key -> !statelessCount.containsKey(key));

        // add missing keys to weights
        statelessCount.keySet().stream()
                .filter(key -> !weights.containsKey(key))
                .forEach(key -> weights.put(key, 1));

        // filter the library so it only has possible targets
        // as in, none of the excludedTargets
        List<InformedStack> targets = library.stream()
                .filter(stack -> lastTargets.stream().noneMatch(stack1 -> StackHelper.isEquivalent(stack, stack1)))
                .collect(Collectors.toList());

        // get a count for the nr of
        Map<String, Integer> count = getModItemCount(targets);

        // get the weight of each mod's stacks, after filtering
        Map<String, Integer> metrics = new HashMap<>();

        // do nrItems * weight on any metrics (explained bellow)
        for (String key : weights.keySet()) {
            // first of all, there never should be negative weights, so let's shift them to zero
            if (weights.get(key) < 0)
                weights.put(key, 0);
            metrics.put(key, count.getOrDefault(key, 0) * weights.get(key));
        }

        EdgeCaseSolver edgeCaseSolver =
                new EdgeCaseSolver(weights, lastTargets, statelessCount, targets, count, metrics).invoke();
        if (!edgeCaseSolver.isSolved())
            return new TargetGenerationResult(EnumTargetFailure.NO_TARGETS_AVAILABLE);

        metrics = edgeCaseSolver.getMetrics();
        targets = edgeCaseSolver.getTargets();

        String targetMod = getWeightedRandomMod(metrics, Kismet.RANDOM);
        // target should always be assigned a value, but just in case...
        if (targetMod == null) {
            Log.error(String.format("Failed to get a targeted mod, from %s and %s", weights, lastTargets));
            return new TargetGenerationResult(EnumTargetFailure.NO_TARGET_MODS_AVAILABLE);
        }

        // increase all weights by one, make the targetMod reset back to 0
        weights.replaceAll((s, integer) -> count.getOrDefault(s, 0) > 0 ? integer + 1 : integer);
        weights.put(targetMod, 0);

        // okay, now we can truly finally pick a target
        // configFilteredItems is already filtered to only have valid stacks so it's just a process of picking a
        // RANDOM index and returning the contents on that index
        final String finalTargetMod = targetMod;
        List<InformedStack> unwrappedStacks = targets.stream()
                .filter(item -> finalTargetMod.equals(StackHelper.getMod(item)))
                .collect(Collectors.toList());
        int index = Kismet.RANDOM.nextInt(unwrappedStacks.size());
        InformedStack newTarget = unwrappedStacks.get(index);

        // finally, we have a target.
        // but ah, before finishing, we have to add the newly generated target to the lastTarget stacks!
        lastTargets.add(newTarget);
        return new TargetGenerationResult(newTarget);
    }

    private static String getWeightedRandomMod(Map<String, Integer> metrics, Random random) {
        int max = metrics.values().stream().reduce(0, (i, j) -> i + j);
        // no edge case so let's move on right along on schedule
        // get a RANDOM number  on [0, max)
        int r = random.nextInt(max);
        // now iterate over the cumulative sum of the weights until you get a sum bigger than r
        int sum = 0;
        for (String mod : metrics.keySet()) {
            sum += metrics.get(mod);
            if (sum > r) {
                return mod;
            }
        }
        return null;
    }

    private static Map<String, Integer> getModItemCount(List<InformedStack> filteredCompleteList) {
        HashMap<String, Integer> map = new HashMap<>();
        for (InformedStack item : filteredCompleteList) {
            String mod = StackHelper.getMod(item);
            if (!map.containsKey(mod)) {
                map.put(mod, 1);
            } else
                map.put(mod, map.get(mod) + 1);
        }
        return map;
    }

    public static List<InformedStack> getLibrary() {
        return library;
    }

    public static void setLibrary(List<InformedStack> library) {
        TargetLibrary.library = library;
    }

    private static class EdgeCaseSolver {
        private final Map<String, Integer> weights;
        private final List<InformedStack> excludedTargets;
        private final Map<String, Integer> statelessCount;
        private final List<InformedStack> targets;
        private final Map<String, Integer> count;
        private final Map<String, Integer> metrics;
        private boolean solved;

        EdgeCaseSolver(Map<String, Integer> weights, List<InformedStack> excludedTargets, Map<String, Integer> statelessCount, List<InformedStack> targets, Map<String, Integer> count, Map<String, Integer> metrics) {
            this.weights = weights;
            this.excludedTargets = excludedTargets;
            this.statelessCount = statelessCount;
            this.targets = targets;
            this.count = count;
            this.metrics = metrics;
        }

        boolean isSolved() {
            return solved;
        }

        List<InformedStack> getTargets() {
            return targets;
        }

        Map<String, Integer> getMetrics() {
            return metrics;
        }

        EdgeCaseSolver invoke() {
            // check if there IS an edge case to begin with
            int max = metrics.values().stream().reduce(0, (i, j) -> i + j);
            if (max > 0) {
                solved = true;
                return this;
            }
            solved = false;

            if (increaseWeights()) return this;
            if (ignorePreviousTargets()) return this;

            return this;
        }

        private boolean increaseWeights() {
            // sum(metrics) = 0, so we're under an edge case
            // we artificially increase the weights (nulls previous weights)
            for (String key : weights.keySet()) {
                int modifiedWeight = weights.get(key);
                if (modifiedWeight <= 0) modifiedWeight = 1;
                metrics.put(key, count.getOrDefault(key, 0) * modifiedWeight);
            }

            int max = metrics.values().stream().reduce(0, (i, j) -> i + j);
            if (max <= 0) return false;

            weights.keySet().stream()
                    .filter(key -> weights.get(key) <= 0 && count.getOrDefault(key, 0) > 0)
                    .forEach(key -> weights.put(key, 1));

            Log.trace("Edge case resolved, all mods with weight 0");

            solved = true;
            return true;
        }

        private boolean ignorePreviousTargets() {
            // sum(metrics*) = 0, so we're under an edge case
            // we discard the previousTargets
            for (String key : weights.keySet()) {
                int modifiedWeight = weights.get(key);
                if (modifiedWeight <= 0) modifiedWeight = 1;
                metrics.put(key, statelessCount.getOrDefault(key, 0) * modifiedWeight);
            }

            int max = metrics.values().stream().reduce(0, (i, j) -> i + j);
            if (max <= 0) return false;

            excludedTargets.clear();

            // reset possible targets to all in the library
            targets.clear();
            targets.addAll(library);
            count.clear();
            count.putAll(statelessCount);

            // reset weights too, seeing that to get here you needed to already consider it broken
            //  as extra incentive, it allows for added randomization, seeing that all the old targets will
            //  be back!
            weights.replaceAll((s, integer) -> 1);

            // and with all it done with, log
            Log.trace("Edge case resolved, cleared previous targets");

            solved = true;
            return true;
        }
    }
}
