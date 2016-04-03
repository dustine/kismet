package desutine.kismet.addon;

import desutine.kismet.common.tile.TileDisplay;
import mezz.jei.api.IItemListOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;

public class JeiIntegration {
    public static boolean doJeiIntegration(TileDisplay te, EntityPlayer playerIn) {
        IItemListOverlay itemList = AddonJei.itemListOverlay;
        if (itemList != null) {
            try {
                String oldFilter = itemList.getFilterText();

                String filter = te.getTarget().getDisplayName();
                String mod = te.getTarget().getItem().getRegistryName().getResourceDomain();
                mod = mod.substring(0, mod.indexOf(":"));
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
}
