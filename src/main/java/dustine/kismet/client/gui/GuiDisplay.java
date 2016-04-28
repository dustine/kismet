package dustine.kismet.client.gui;

import dustine.kismet.Reference;
import dustine.kismet.block.BlockTimedDisplay;
import dustine.kismet.inventory.ContainerDisplay;
import dustine.kismet.target.EnumOrigin;
import dustine.kismet.target.InformedStack;
import dustine.kismet.tile.TileDisplay;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GuiDisplay extends GuiKismet {
    private final InventoryPlayer playerInventory;
    private final TileDisplay tile;

    public GuiDisplay(InventoryPlayer playerInventory, TileDisplay tile) {
        super(new ContainerDisplay(playerInventory, tile));
        this.playerInventory = playerInventory;
        this.tile = tile;
        this.xSize = 176;
        this.ySize = 164;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // text tags
        String s = new TextComponentTranslation("gui.display.name").getFormattedText();
        this.fontRendererObj.drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, 6, Reference.Colors.TEXT_GREY);
        this.fontRendererObj.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, Reference.Colors.TEXT_GREY);


    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(Reference.GUI.DISPLAY);
        int topLeftX = (this.width - this.xSize) / 2;
        int topLeftY = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(topLeftX, topLeftY, 0, 0, this.xSize, this.ySize);

        int relativeOx = (this.width - this.xSize) / 2;
        int relativeOy = (this.height - this.ySize) / 2;

        final InformedStack target = this.tile.getTarget();
        if (target != null) {
            int iconLocation = 0;
            // origin icons
            if (target.hasOrigin(EnumOrigin.FORCED)) {
                this.drawTexturedModalRect(relativeOx + 59 + 12 * iconLocation, relativeOy + 16,
                        12 * EnumOrigin.FORCED.ordinal(), 164, 12, 12);
                ++iconLocation;
            }

            final List<EnumOrigin> origins = Arrays.stream(EnumOrigin.values())
                    .filter(o -> o != EnumOrigin.FORCED && o != EnumOrigin.OTHER)
                    .sorted((o1, o2) -> o1.toString().compareTo(o2.toString()))
                    .collect(Collectors.toList());

            for (EnumOrigin origin : origins) {
                if (target.hasOrigin(origin)) {
                    this.drawTexturedModalRect(relativeOx + 59 + 12 * iconLocation, relativeOy + 16,
                            12 * origin.ordinal(), 164, 12, 12);
                    ++iconLocation;
                }
            }

            if (target.hasOrigin(EnumOrigin.OTHER)) {
                this.drawTexturedModalRect(relativeOx + 59 + 12 * iconLocation, relativeOy + 16,
                        12 * EnumOrigin.OTHER.ordinal(), 164, 12, 12);
//                ++iconLocation;
            }
        }

        // text info slots + icons
        if (this.tile.getBlockType() instanceof BlockTimedDisplay) {

        } else {

        }
    }
}
