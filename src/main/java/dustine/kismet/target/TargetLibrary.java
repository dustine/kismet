package dustine.kismet.target;

import dustine.kismet.Kismet;
import dustine.kismet.Log;
import dustine.kismet.config.ConfigKismet;
import dustine.kismet.util.StackHelper;
import dustine.kismet.util.TargetHelper;
import dustine.kismet.world.savedata.WSDTargetDatabase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class TargetLibrary {
    private static List<Target> library;

    /**
     * Picks a random Target from the target library (filtered database), taking into account the given map of desired
     * mod weights and a list of any previously chosen targets. This function has collateral on the parameters, that is,
     * the previous target's list has the generated Target, if any, added to it, and mod weights is also updated
     * accordingly, as detailed bellow.
     * <p>
     * Mod weights are needed in target selection as the algorithm uses a weighted random distribution when choosing
     * what mod to pick the target from (the target selection within the mod itself is plainly random). The actual
     * weights used in mod selections consists of the given mod weight times the number of possible targets, in the
     * target library, from said mod. In other words: weight(mod) * #targets(mod).
     * <p>
     * As collateral, the parameters are updated accordingly to the target generation result: <br> - The generated
     * target is added to the end of the target history list (keeping a target chronology if the function on further
     * function calls); <br> - All unpicked mod weights are increased by one (to increase their likelihood in the
     * future), and the picked mod has their score reduced to zero, to assure it's not chosen the next function call.
     * Besides that, the map indexes are updated if any mods were removed/added to the game by respectively
     * removing/adding them with a weight of one.
     * <p>
     * If, for some reason or another, generation is impossible under the given parameters, the function internally
     * reruns itself after adjusting the entry parameter to more generic, and henceforth more likely to succeed
     * parameters: first adjusting mod weights (all non-positive weights are increased to one), and secondly the
     * previous targets list (list is cleared). These edge case solvers are only applied if the previous one failed in
     * the target selection process. If all else fails, the target is returned as null. These adjustments also count as
     * collateral.
     *
     * @param weights Map of mod weights
     * @param history List of previously chosen targets
     * @return The next possible target, or null in case of failure
     */
    public static Target generateTarget(@Nonnull final Map<String, Integer> weights,
                                        @Nonnull final List<Target> history) {
        if (library == null) {
            Log.error("Target library isn't ready or was forcefully set to null");
            return null;
        }

        // clears old keys from weights (ones not loaded)
        weights.keySet().removeIf(key -> !Loader.isModLoaded(key));

        final EdgeCaseSolver edgeCaseSolver = new EdgeCaseSolver(weights, history).invoke();
        if (!edgeCaseSolver.isSolved()) {
            Log.error(String.format("Target generation failed, edge case unresolved, from %s and %s",
                    weights, history));
            return null;
        }

        final Map<String, Integer> count = edgeCaseSolver.getCount();
        final Map<String, Integer> metrics = edgeCaseSolver.getMetrics();
        final List<Target> targets = edgeCaseSolver.getTargets();

        final String targetMod = getWeightedRandomMod(metrics);
        // target should always be assigned a value, but just in case...
        if (targetMod == null || targetMod.isEmpty() || !count.containsKey(targetMod) || count.get(targetMod) <= 0) {
            Log.error(String.format("Failed to get a targeted mod (%s) after edge case verification, from %s and %s",
                    targetMod, weights, history));
            return null;
        }

        // okay, now we can truly finally pick a target
        // configFilteredItems is already filtered to only have valid stacks so it's just a process of picking a
        // RANDOM index and returning the contents on that index
        final List<Target> unwrappedStacks = targets.stream()
                .filter(item -> targetMod.equals(TargetHelper.getMod(item)))
                .collect(Collectors.toList());
        final int index = Kismet.RANDOM.nextInt(unwrappedStacks.size());
        final Target newTarget = unwrappedStacks.get(index);

        // finally, we have a target.
        // but ah, before finishing, we have to update the entry parameters!
        // increase all weights by one, make the targetMod reset back to 0
        weights.replaceAll((s, integer) -> ++integer);
        weights.put(targetMod, 0);
        // add the generated target to the target history
        history.add(newTarget);
        return newTarget;
    }

    private static String getWeightedRandomMod(final Map<String, Integer> metrics) {
        final int max = metrics.values().stream().reduce(0, (i, j) -> i + j);
        // no edge case so let's move on right along on schedule
        // get a RANDOM number  on [0, max)
        final int r = Kismet.RANDOM.nextInt(max);
        // now iterate over the cumulative sum of the weights until you get a sum bigger than r
        int sum = 0;
        for (final String mod : metrics.keySet()) {
            sum += metrics.get(mod);
            if (sum > r) {
                return mod;
            }
        }
        return null;
    }

    private static Map<String, Integer> getModItemCount(final List<Target> filteredCompleteList) {
        final HashMap<String, Integer> map = new HashMap<>();
        for (final Target item : filteredCompleteList) {
            final String mod = TargetHelper.getMod(item);
            if (!map.containsKey(mod)) {
                map.put(mod, 1);
            } else
                map.put(mod, map.get(mod) + 1);
        }
        return map;
    }

    /**
     * Returns the current target library.
     *
     * @return Target library
     */
    public static List<Target> getLibrary() {
        return library;
    }

    /**
     * Builds the library using the given target database as a source.
     *
     * @param targetDatabase The target database source
     */
    public static void build(final WSDTargetDatabase targetDatabase) {
        build(targetDatabase.getDatabase());
    }

    /**
     * Builds the library using the given target map as a source. This function has no collaterals (won't change
     * targets).
     *
     * @param targets The target map source
     */
    public static void build(final Map<String, Target> targets) {
        final HashMap<String, Target> targetMap = new HashMap<>(targets);

        // generated targets filter keys
        final Set<String> filter = new HashSet<>();
        for (final String s : ConfigKismet.getGenFilter()) {
            // skip entries that start with an !
            if (s.startsWith("!")) continue;
            filter.add(standardizeFilter(s));
        }
        filter.remove(null);

        targetMap.values().removeIf(target -> !isTarget(target, filter));
        library = new ArrayList<>(targetMap.values());

        Log.info("Built target library");
    }

    private static String standardizeFilter(@Nonnull final String s) {
        // mod filtering
        final String[] split = s.split(":");
        if (split.length == 1) {
            final String mod = split[0];
            // edge case: minecraft isn't recognized as a mod, but it's a resource location so we add it
            if (s.equals("minecraft")) return mod;
            // mod
            return Loader.isModLoaded(mod) ? mod : null;
        }

        // else treat it as item filtering (with possible metadata)
        final ItemStack stack;
        stack = StackHelper.getItemStack(s);
        if (stack == null) return null;

        return StackHelper.toUniqueKey(stack, s.split(":", 4).length > 2);
    }

    private static boolean isTarget(final Target target, final Set<String> filter) {
        // no nulls pls
        if (target == null || target.getStack() == null) return false;

        // forcefully added stacks always go true, even if they're on the blacklist
        if (target.hasOrigin(EnumOrigin.FORCED)) return true;

        // generation mode dictates if we continue or not
        switch (ConfigKismet.getGenMode()) {
            case NONE:
                return false;
            case FILTERED:
                if (!target.isObtainable()) return false;
                break;
            case ALL:
                break;
            default:
                break;
        }

        final String name = target.toString();

        // if this target matches exactly one in the forced list, skip it (will be added later)
        // and if it's on the filter list, skip it as well (blacklisted)
        return filter.stream().noneMatch(name::startsWith);
    }

    private static class EdgeCaseSolver {
        private final Map<String, Integer> weights;
        private final List<Target> history;
        private final Map<String, Integer> statelessCount;
        private final List<Target> targets;
        private final Map<String, Integer> count;
        private final Map<String, Integer> metrics;
        private boolean solved;

        EdgeCaseSolver(final Map<String, Integer> weights, final List<Target> history) {
            this.weights = weights;
            this.history = history;

            this.metrics = new HashMap<>();
            this.statelessCount = getModItemCount(library);
            // filter the library so it only has possible targets
            // as in, none of the history
            // fixme: O(n^2), seriously??
            this.targets = library.stream()
                    .filter(target -> history.stream()
                            .noneMatch(prev -> TargetHelper.isEquivalent(target, prev)))
                    .collect(Collectors.toList());
            this.count = getModItemCount(this.targets);
        }

        boolean isSolved() {
            return this.solved;
        }

        List<Target> getTargets() {
            return this.targets;
        }

        Map<String, Integer> getMetrics() {
            return this.metrics;
        }

        EdgeCaseSolver invoke() {
            // before checking the edge cases, let's fix-up the entry parameters
            // add missing keys to weights
            this.statelessCount.keySet().stream()
                    .filter(key -> !this.weights.containsKey(key))
                    .forEach(key -> this.weights.put(key, 1));

            // generate metrics with the entry parameters
            for (final String key : this.weights.keySet()) {
                // first of all, there never should be negative weights, so let's shift them already to zero
                if (this.weights.get(key) < 0)
                    this.weights.put(key, 0);
                this.metrics.put(key, this.count.getOrDefault(key, 0) * this.weights.get(key));
            }

            // check if there IS an edge case to begin with
            final int max = this.metrics.values().stream().reduce(0, (i, j) -> i + j);
            if (max > 0) {
                this.solved = true;
                return this;
            }
            this.solved = false;

            if (increaseWeights()) return this;
            if (ignorePreviousTargets()) return this;

            return this;
        }

        private boolean increaseWeights() {
            // sum(metrics) = 0, so we're under an edge case
            // we artificially increase the weights (nulls previous weights)
            this.weights.replaceAll((k, w) -> w <= 0 ? 1 : w);
            this.metrics.replaceAll((k, m) -> this.count.getOrDefault(k, 0) * this.weights.get(k));

            final int max = this.metrics.values().stream().reduce(0, (i, j) -> i + j);
            if (max <= 0) return false;

            // targets and count don't need to be touched so we're clear to simply return

            Log.trace("Edge case resolved, all mods with weight 1+");

            this.solved = true;
            return true;
        }

        private boolean ignorePreviousTargets() {
            // sum(metrics) = 0, so we're under an edge case
            // we discard the previousTargets
            this.metrics.replaceAll((k, m) -> this.statelessCount.getOrDefault(k, 0) * this.weights.get(k));

            final int max = this.metrics.values().stream().reduce(0, (i, j) -> i + j);
            if (max <= 0) return false;

            // clear the target history
            this.history.clear();

            // reset possible targets to all in the library
            this.targets.clear();
            this.targets.addAll(library);
            this.count.clear();
            this.count.putAll(this.statelessCount);

            // and with all it done with, log
            Log.trace("Edge case resolved, cleared previous targets");

            this.solved = true;
            return true;
        }

        Map<String, Integer> getCount() {
            return this.count;
        }
    }
}
