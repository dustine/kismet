package dustine.kismet.util;

import dustine.kismet.Kismet;
import dustine.kismet.Log;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;

/**
 * This class, and its functions, were adapted from mezz's JustEnoughItems's StackHelper, linked bellow.
 * https://github.com/mezz/JustEnoughItems/blob/1.9/src/main/java/mezz/jei/util/StackHelper.java
 * <p>
 * All code wherein under the MIT license (c) 2014-2015 mezz
 */
public class StackHelper {
    public static String toUniqueKey(ItemStack stack) {
        final boolean hasSubtypes = Kismet.proxy.sideSafeHasSubtypes(stack);
        return toUniqueKey(stack, hasSubtypes);
    }

    public static String toUniqueKey(ItemStack stack, boolean hasSubtypes) {
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

    public static ItemStack getItemStack(@Nonnull String s) {
        final String[] split = s.split(":", 4);

        if (split.length < 2) {
            Log.warning("Weird location: " + s);
            return null;
        } else {
            Integer meta = null;
            String nbt = "";
            if (split.length > 2) {
                meta = tryParse(split[2]);
            }
            if (split.length > 3) {
                nbt = split[3];
            }
            return GameRegistry.makeItemStack(split[0] + ":" + split[1], meta != null ? meta : 0, 1, nbt);
        }
    }

    public static Integer tryParse(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
