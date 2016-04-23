package desutine.kismet.util;

import desutine.kismet.Kismet;
import desutine.kismet.target.InformedStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This class, and its functions, were adapted from mezz's JustEnoughItems's StackHelper, linked bellow.
 * https://github.com/mezz/JustEnoughItems/blob/1.9/src/main/java/mezz/jei/util/StackHelper.java
 * <p>
 * All code wherein under the MIT license (c) 2014-2015 mezz
 */
public class StackHelper {
    public static boolean isEquivalent(InformedStack lhw, InformedStack rhw) {
        return rhw != null && isEquivalent(lhw, rhw.getStack());
    }

    public static boolean isEquivalent(InformedStack lhw, ItemStack rhs) {
        if (lhw == null || rhs == null) return false;
        final ItemStack lhs = lhw.getStack();
        if (lhs == null) return false;
        if (lhs == rhs) return true;
        if (lhs.getItem() != rhs.getItem()) return false;
        // wildcard means metadata doesn't matter (on either side)
        if (lhs.getMetadata() != OreDictionary.WILDCARD_VALUE) {
            if (lhs.getMetadata() != rhs.getMetadata()) {
                return false;
            }
        }

        if (lhw.getHasSubtypes()) {
            // test NBT
            if (lhs.getItem() == null || rhs.getItem() == null) return false;
            String nbtLhs = getNbtKey(lhs);
            String nbtRhs = getNbtKey(rhs);
            return nbtLhs.equals(nbtRhs);
        } else {
            return true;
        }
    }

    private static String getNbtKey(ItemStack item) {
        if (item.hasTagCompound()) {
            NBTTagCompound nbtTagCompound;
            // fixme no consideration for nbt tags that mean nothing ._."
            nbtTagCompound = item.getTagCompound();

            if (nbtTagCompound != null && !nbtTagCompound.hasNoTags()) {
                return nbtTagCompound.toString();
            }
        }
        return "";
    }

    public static String toUniqueKey(InformedStack wrapper) {
        if (wrapper == null || !wrapper.hasItem()) return "";
        return toUniqueKey(wrapper.getStack(), wrapper.getHasSubtypes());
    }

    private static String toUniqueKey(ItemStack stack, boolean hasSubtypes) {
        if (stack == null || stack.getItem() == null) return "";

        ResourceLocation loc = stack.getItem().getRegistryName();
        if (loc == null) return "";

        StringBuilder result = new StringBuilder(loc.toString());
        final int metadata = stack.getMetadata();
        if (metadata == OreDictionary.WILDCARD_VALUE) {
            return result.toString();
        }

        if (hasSubtypes) {
            result.append(":").append(metadata);
            String nbt = getNbtKey(stack);
            if (nbt != null && !nbt.isEmpty()) {
                result.append(":").append(nbt);
            }
        }
        return result.toString();
    }

    public static String toUniqueKey(ItemStack stack) {
        final boolean hasSubtypes = Kismet.proxy.inferSafeHasSubtypes(stack);
        return toUniqueKey(stack, hasSubtypes);
    }

    public static Set<String> getMods(Collection<InformedStack> stacks) {
        final Set<String> mods = new HashSet<>();
        for (InformedStack wrapper : stacks) {
            String mod = getMod(wrapper);
            mods.add(mod);
        }
        return mods;
    }

    public static String getMod(InformedStack item) {
        return item.getStack().getItem().getRegistryName().getResourceDomain();
    }
}
