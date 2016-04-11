package desutine.kismet.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

public class StackHelper {
    public static boolean isEquivalent(ItemStack lhs, ItemStack rhs) {
        if (lhs == null || rhs == null) return false;
        if (lhs == rhs) return true;
        if (lhs.getItem() != rhs.getItem()) return false;
        // wildcard means metadata doesn't matter (on either side)
        if (lhs.getMetadata() != OreDictionary.WILDCARD_VALUE) {
            if (lhs.getMetadata() != rhs.getMetadata()) {
                return false;
            }
        }

        if (lhs.getHasSubtypes()) {
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

    public static String stackToString(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return "";

        ResourceLocation loc = stack.getItem().getRegistryName();
        if (loc == null) return "";

        StringBuilder result = new StringBuilder(loc.toString());
        final int metadata = stack.getMetadata();
        if (metadata == OreDictionary.WILDCARD_VALUE) {
            return result.toString();
        }

        if (stack.getHasSubtypes()) {
            result.append(":").append(metadata);
            String nbt = getNbtKey(stack);
            if (nbt != null && !nbt.isEmpty()) {
                result.append(":").append(nbt);
            }
        }
        return result.toString();
    }
}
