package dustine.kismet.target;

import dustine.kismet.ConfigKismet;
import dustine.kismet.Kismet;
import dustine.kismet.Log;
import dustine.kismet.util.StackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.*;

public class TargetLibraryBuilder {
    /**
     * Recreates the possible target stack library at TargetLibrary according to the current config. This function reads
     * the existing "stacks" information stored in the @world and rewrites the "forcedStacks" to assure any
     * configuration changes are reflected on the saved data for future use.
     *
     * @param targets
     */
    public static void recreateLibrary(Collection<InformedStack> targets) {
        Map<String, InformedStack> stacks = getConfigStacks(targets);

        // generated stacks filter keys
        final Set<String> filter = new HashSet<>();
        for (String s : ConfigKismet.getHiddenBlacklist()) {
            // skip entries that start with an !
            if (s.startsWith("!")) continue;
            filter.add(standardizeFilter(s));
        }
        for (String s : ConfigKismet.getGenBlacklist()) {
            // skip entries that start with an !
            if (s.startsWith("!")) continue;
            filter.add(standardizeFilter(s));
        }
        filter.remove(null);

        stacks.values().removeIf(wrapper -> !isTarget(wrapper, filter));

        TargetLibrary.setLibrary(new ArrayList<>(stacks.values()));
    }

    /**
     * Using originalStacks as a initial blueprint, this function loads all the defined stacks in the configurations,
     * such as the hidden lists and the force-add stacks, and joins them into originalStacks.
     *
     * @param originalStacks
     * @return A map from joining originalStacks with the config lists
     */
    public static Map<String, InformedStack> getConfigStacks(Collection<InformedStack> originalStacks) {
        Map<String, InformedStack> stacks = new HashMap<>();

        // forced stacks, the ones that are added for sure to the filtered stacks
        for (InformedStack.ObtainableTypes type : InformedStack.ObtainableTypes.values()) {
            addConfigGen(stacks, type);
        }

        // add all the targets now to this list
        originalStacks.forEach(stack -> {
            String key = stack.toString();
            if (stacks.containsKey(key)) {
                stacks.put(key, stacks.get(key).joinWith(stack));
            } else {
                stacks.put(key, stack);
            }
        });
        return stacks;
    }

    private static void addConfigGen(Map<String, InformedStack> stacks, InformedStack.ObtainableTypes type) {
        for (String entry : ConfigKismet.getGenList(type)) {
            // skip entries that start with an !
            // also skip mods on the whitelist as it's only for specific tempStacks
            if (entry.startsWith("!") || isMod(entry)) continue;
            final ItemStack stack = getItemStack(entry);
            if (stack == null) continue;

            // add the entries as subtype-having wrappers
            final InformedStack wrapper = new InformedStack(stack, type);
            // force hasSubtypes to true if user specified a metadata value
            if (hasMetadata(entry))
                wrapper.setHasSubtypes(true);
            wrapper.seal();

            String key = wrapper.toString();
            if (stacks.containsKey(key)) {
                stacks.put(key, stacks.get(key).joinWith(wrapper));
            } else
                stacks.put(key, wrapper);
        }
    }

    private static boolean hasMetadata(String entry) {
        final String[] split = entry.split(":");
        if (split.length < 3) return false;
        Integer meta = tryParse(split[2]);
        return meta != null;
    }

    private static boolean isMod(String s) {
        return !s.contains(":");
    }

    static ItemStack getItemStack(@Nonnull String s) {
        return getItemStack(s, false);
    }

    private static ItemStack getItemStack(@Nonnull String entry, boolean wildcards) {
        final String[] split = entry.split(":");

        if (split.length < 2) {
            Log.warning("Weird location: " + entry);
            return null;
        }

        // define item (mod:itemName)
        ItemStack stack;
        ResourceLocation loc = new ResourceLocation(split[0], split[1]);
        if (Item.REGISTRY.getKeys().contains(loc)) {
            stack = new ItemStack(Item.REGISTRY.getObject(loc));
        } else {
            Log.error("Weird location: " + entry);
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
                Log.error(String.format("Weird metadata %s in %s", split[2], entry));
                if (wildcards && Kismet.proxy.sideSafeHasSubtypes(stack)) {
                    stack.setItemDamage(OreDictionary.WILDCARD_VALUE);
                }
            }
        } else {
            if (wildcards && Kismet.proxy.sideSafeHasSubtypes(stack)) {
                stack.setItemDamage(OreDictionary.WILDCARD_VALUE);
            }
        }

        // add nbt data
        if (split.length > 3) {
            try {
                NBTTagCompound nbt = JsonToNBT.getTagFromJson(split[3]);
                stack.setTagCompound(nbt);
            } catch (NBTException e) {
                Log.error(String.format("Weird NBT %s in %s", split[3], entry), e);
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
            String mod = split[0];
            // edge case: minecraft isn't recognized as a mod, but it's a resource location so we add it
            if (s.equals("minecraft")) return mod;
            // mod
            return Loader.isModLoaded(mod) ? mod : null;
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
        if (wrapper.isObtainable(InformedStack.ObtainableTypes.Forced)) return true;

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
}
