package dustine.kismet.target;

import dustine.kismet.Log;
import dustine.kismet.config.ConfigKismet;
import dustine.kismet.util.StackHelper;
import dustine.kismet.world.savedata.WSDTargetDatabase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nonnull;
import java.util.*;

public class TargetLibraryBuilder {
    public static void build(WSDTargetDatabase targetDatabase) {
        build(targetDatabase.getDatabase());
    }

    /**
     * Recreates the possible target library at TargetLibrary according to the current config. This function reads
     * the existing "stacks" information stored in the @world and rewrites the "forcedStacks" to assure any
     * configuration changes are reflected on the saved data for future use.
     *
     * @param targets
     */
    public static void build(Collection<Target> targets) {
        Map<String, Target> targetMap = getConfigStacks(targets);

        // generated targets filter keys
        final Set<String> filter = new HashSet<>();
        for (String s : ConfigKismet.getGenFilter()) {
            // skip entries that start with an !
            if (s.startsWith("!")) continue;
            filter.add(standardizeFilter(s));
        }
        filter.remove(null);

        targetMap.values().removeIf(target -> !isTarget(target, filter));
        TargetLibrary.setLibrary(new ArrayList<>(targetMap.values()));

        Log.info("Built target library");
    }

    /**
     * Using targets as a initial blueprint, this function loads all the defined stacks in the configurations,
     * such as the hidden lists and the force-add stacks, and joins them into targets.
     *
     * @param targets
     * @return A map from joining targets with the config lists
     */
    public static Map<String, Target> getConfigStacks(Collection<Target> targets) {
        Map<String, Target> targetMap = new HashMap<>();

        // forced stacks, the ones that are added for sure to the filtered stacks
        addConfigForceAdd(targetMap);
        // the overrides, both from file and runtime
        addOverrides(targetMap);

        // add all the targets now to this list
        targets.forEach(target -> {
            String key = target.toString();
            if (targetMap.containsKey(key)) {
                targetMap.put(key, targetMap.get(key).joinWith(target));
            } else {
                targetMap.put(key, target);
            }
        });
        return targetMap;
    }

    private static void addOverrides(Map<String, Target> targetMap) {
        for (EnumOrigin origin : EnumOrigin.values()) {
            for (String item : TargetPatcher.getOverrides(origin)) {
                if (item.startsWith("!") || isMod(item)) continue;

                final ItemStack stack = StackHelper.getItemStack(item);
                if (stack == null) continue;

                // add the entries as subtype-having wrappers
                final Target target = new Target(stack, origin);
                // force hasSubtypes to true if user specified a metadata value
                if (hasMetadata(item))
                    target.setHasSubtypes(true);

                String key = target.toString();
                if (targetMap.containsKey(key)) {
                    targetMap.put(key, targetMap.get(key).joinWith(target));
                } else
                    targetMap.put(key, target);
            }
        }
    }

    private static void addConfigForceAdd(Map<String, Target> targetMap) {
        for (String item : ConfigKismet.getForceAdd()) {
            if (item.startsWith("!") || isMod(item)) continue;

            final ItemStack stack = StackHelper.getItemStack(item);
            if (stack == null) continue;

            // add the entries as subtype-having wrappers
            final Target target = new Target(stack, EnumOrigin.FORCED);
            // force hasSubtypes to true if user specified a metadata value
            if (hasMetadata(item))
                target.setHasSubtypes(true);

            String key = target.toString();
            if (targetMap.containsKey(key)) {
                targetMap.put(key, targetMap.get(key).joinWith(target));
            } else
                targetMap.put(key, target);
        }
    }

    private static boolean hasMetadata(String entry) {
        final String[] split = entry.split(":");
        if (split.length < 3) return false;
        Integer meta = StackHelper.tryParse(split[2]);
        return meta != null;
    }

    private static boolean isMod(String s) {
        return !s.contains(":");
    }

    private static String standardizeFilter(@Nonnull String s) {
        // mod filtering
        final String[] split = s.split(":");
        if (split.length == 1) {
            String mod = split[0];
            // edge case: minecraft isn't recognized as a mod, but it's a resource location so we add it
            if (s.equals("minecraft")) return mod;
            // mod
            return Loader.isModLoaded(mod) ? mod : null;
        }

        // else treat it as item filtering (with possible metadata)
        ItemStack stack;
        stack = StackHelper.getItemStack(s);
        if (stack == null) return null;

        return StackHelper.toUniqueKey(stack, s.split(":", 4).length > 2);
    }

    private static boolean isTarget(Target target, Set<String> filter) {
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

        String name = target.toString();

        // if this target matches exactly one in the forced list, skip it (will be added later)
        // and if it's on the filter list, skip it as well (blacklisted)
        return filter.stream().noneMatch(name::startsWith);
    }
}
