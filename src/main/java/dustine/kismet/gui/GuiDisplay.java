package dustine.kismet.gui;

import dustine.kismet.inventory.ContainerDisplay;
import dustine.kismet.tile.TileDisplay;
import net.minecraft.entity.player.InventoryPlayer;

public class GuiDisplay extends GuiKismet {
    public GuiDisplay(InventoryPlayer playerInventory, TileDisplay tile) {
        super(new ContainerDisplay(playerInventory, tile));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {

    }


}
