package desutine.kismet.addon;

import desutine.kismet.tile.TileDisplay;
import mezz.jei.api.IItemListOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.List;

public class JeiIntegration {
    public static boolean doJeiIntegration(TileDisplay te, EntityPlayer playerIn) {
        IItemListOverlay itemList = AddonJei.itemListOverlay;
        if (itemList != null) {
            try {
                String oldFilter = itemList.getFilterText();

                ItemStack stack = te.getTarget().getStack();

                String filter = stack.getDisplayName();
                String mod = stack.getItem().getRegistryName().getResourceDomain();
                filter = String.format("%s @%s", filter, mod);
                if (oldFilter.equalsIgnoreCase(filter)) return false;

                // empty hand = give information about the block
                Minecraft.getMinecraft().displayGuiScreen(new GuiInventory(playerIn));
                itemList.setFilterText(filter);
            } catch (NullPointerException e) {
                return false;
            }
            return true;
        }
        return false;
    }

    public static List<ItemStack> enrich(List<ItemStack> list) {
        return AddonJei.stackHelper.getAllSubtypes(list);
    }
}
