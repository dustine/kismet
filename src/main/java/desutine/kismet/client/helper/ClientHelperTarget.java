package desutine.kismet.client.helper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.item.ItemStack;

import java.util.Map;
import java.util.Set;

public class ClientHelperTarget {
    public static void identifyWorldGen(Set<ItemStack> items, Map<ItemStack, Boolean> isObtainable) {
        final WorldClient world = Minecraft.getMinecraft().theWorld;
//        ModLogger.info(world.getWorldInfo().getGeneratorOptions());
    }
}
