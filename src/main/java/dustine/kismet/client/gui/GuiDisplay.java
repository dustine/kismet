package dustine.kismet.client.gui;

import dustine.kismet.Log;
import dustine.kismet.Reference;
import dustine.kismet.block.BlockTimedDisplay;
import dustine.kismet.inventory.ContainerDisplay;
import dustine.kismet.inventory.SlotTarget;
import dustine.kismet.target.EnumOrigin;
import dustine.kismet.target.InformedStack;
import dustine.kismet.tile.TileDisplay;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.client.config.GuiUtils;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GuiDisplay extends GuiKismet {
    private static final int originIconSize = 12;
    private static final int infoIconSize = 11;
    private static final Rectangle originIconsBoundingBox = new Rectangle(59, 16, 117, 12);
    private static final Point infoIconOrigin = new Point(59, 31);
    private static final Point originIconOrigin = new Point(59, 16);
    private static final Point infoTextOrigin = new Point(72, 33);
    private static final int infoIconTextureX = 176;
    private static final int originIconTextureY = 164;
    private final InventoryPlayer playerInventory;
    private final TileDisplay display;
    private final RenderTargetSlot renderTargetSlot = new RenderTargetSlot(this);
    private final SlotTarget targetSlot;

    public GuiDisplay(InventoryPlayer playerInventory, TileDisplay display) {
        super(new ContainerDisplay(playerInventory, display));
        this.playerInventory = playerInventory;
        this.display = display;
        this.targetSlot = (SlotTarget) this.inventorySlots.getSlot(0);
        // no coincidence the texture statics are the same here, the icon texture sheets are on either edge of the
        // main gui sheet
        this.xSize = 176;
        this.ySize = 164;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        // extra code to render the origin tooltip
        final InformedStack target = this.display.getTarget();
        if (target != null) {
            int relOx = (this.width - this.xSize) / 2;
            int relOy = (this.height - this.ySize) / 2;

            int relMouseX = mouseX - relOx;
            int relMouseY = mouseY - relOy;

            if (originIconsBoundingBox.contains(relMouseX, relMouseY)) {
                final List<EnumOrigin> origins = getOrderedOrigins(target);
                int coord = (relMouseX - (int) (originIconsBoundingBox.getX())) / (originIconSize + 1);
                if (coord >= origins.size()) return;

                GuiUtils.drawHoveringText(Collections.singletonList(getOriginSubtitle(origins.get(coord))),
                        mouseX, mouseY, this.width, this.height, -1, this.fontRendererObj);
            }
        }

        // make the target slot work as if was highlighted
        InventoryPlayer inventoryplayer = this.mc.thePlayer.inventory;
        if (inventoryplayer.getItemStack() == null && this.isMouseOverSlot(targetSlot, mouseX, mouseY) && targetSlot
                .getHasStack()) {
            ItemStack stack = targetSlot.getStack();
            this.renderToolTip(stack, mouseX, mouseY);
        }

//        GlStateManager.pushMatrix();
//        GlStateManager.translate((float)this.guiLeft, (float)this.guiTop, 0.0F);
//        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//        GlStateManager.enableRescaleNormal();
//        int k = 240;
//        int l = 240;
//        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)k, (float)l);
//        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//
//        for (int i1 = 0; i1 < this.inventorySlots.inventorySlots.size(); ++i1)
//        {
//            Slot slot = (Slot)this.inventorySlots.inventorySlots.get(i1);
//            if()
//            this.drawSlot(slot);
//
//            if (this.isMouseOverSlot(slot, mouseX, mouseY) && slot.canBeHovered())
//            {
//                GlStateManager.disableLighting();
//                GlStateManager.disableDepth();
//                int j1 = slot.xDisplayPosition;
//                int k1 = slot.yDisplayPosition;
//                GlStateManager.colorMask(true, true, true, false);
//                this.drawGradientRect(j1, k1, j1 + 16, k1 + 16, -2130706433, -2130706433);
//                GlStateManager.colorMask(true, true, true, true);
//                GlStateManager.enableLighting();
//                GlStateManager.enableDepth();
//            }
//        }
//
//        GlStateManager.popMatrix();

    }

    @Override
    protected void drawItemStack(ItemStack stack, int x, int y, String altText) {
        super.drawItemStack(stack, x, y, altText);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // text tags
        // retrieved from GuiChest so idk what any of these constants do :P
        String s = new TextComponentTranslation("gui.display.name").getFormattedText();
        this.fontRendererObj.drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, 6, Reference.Colors.TEXT_GREY);
        this.fontRendererObj.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, Reference.Colors.TEXT_GREY);

        // text info slots
        int iconLocation = 0;
        if (this.display.getBlockType() instanceof BlockTimedDisplay) {
            // remove the § doo hickeys
            this.fontRendererObj.drawString(this.display.getStylizedDeadline(false), infoTextOrigin.x,
                    infoTextOrigin.y + (infoIconSize + 1) * iconLocation++, Reference.Colors.TEXT_GREY);
        }

        this.fontRendererObj.drawString(this.display.getStylizedKeyChance(), infoTextOrigin.x,
                infoTextOrigin.y + (infoIconSize + 1) * iconLocation++, Reference.Colors.TEXT_GREY);

        this.fontRendererObj.drawString(String.valueOf(this.display.getScore()), infoTextOrigin.x,
                infoTextOrigin.y + (infoIconSize + 1) * iconLocation, Reference.Colors.TEXT_GREY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(Reference.GUI.DISPLAY);
        int relOx = (this.width - this.xSize) / 2;
        int relOy = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(relOx, relOy, 0, 0, this.xSize, this.ySize);

        final InformedStack target = this.display.getTarget();
        if (target != null) {
            int iconLocation = 0;
            // origin icons
            final List<EnumOrigin> origins = getOrderedOrigins(target);

            for (EnumOrigin origin : origins) {
                if (target.hasOrigin(origin)) {
                    this.drawTexturedModalRect(relOx + originIconOrigin.x + originIconSize * iconLocation++, relOy + originIconOrigin.y, originIconSize * origin.ordinal(), originIconTextureY, originIconSize, originIconSize);
                }
            }
        }

        // text info icons
        int iconLocation = 0;
        if (this.display.getBlockType() instanceof BlockTimedDisplay) {
            this.drawTexturedModalRect(relOx + infoIconOrigin.x, relOy + infoIconOrigin.y + (infoIconSize + 1) *
                    iconLocation++, infoIconTextureX, infoIconSize * EnumIcon.TIME.ordinal(), infoIconSize, infoIconSize);
        }

        this.drawTexturedModalRect(relOx + infoIconOrigin.x, relOy + infoIconOrigin.y + (infoIconSize + 1) *
                iconLocation++, infoIconTextureX, infoIconSize * EnumIcon.KEY.ordinal(), infoIconSize, infoIconSize);

        this.drawTexturedModalRect(relOx + infoIconOrigin.x, relOy + infoIconOrigin.y + (infoIconSize + 1) *
                iconLocation, infoIconTextureX, infoIconSize * EnumIcon.SCORE.ordinal(), infoIconSize, infoIconSize);
    }

    @Override
    protected void drawSlot(Slot slotIn) {
        if (slotIn instanceof SlotTarget) {
            this.renderTargetSlot.drawTargetSlot((SlotTarget) slotIn);
        } else {
            super.drawSlot(slotIn);
        }
    }

    @Override
    protected boolean isMouseOverSlot(Slot slotIn, int mouseX, int mouseY) {
        if (slotIn instanceof SlotTarget) {
            final int size = (int) (16 * ((SlotTarget) slotIn).getFactor());
            return this.isPointInRegion(slotIn.xDisplayPosition, slotIn.yDisplayPosition, size, size, mouseX, mouseY);
        } else {
            return super.isMouseOverSlot(slotIn, mouseX, mouseY);
        }
    }

    @Override
    protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type) {
        super.handleMouseClick(slotIn, slotId, mouseButton, type);

        if (slotIn == targetSlot)
            Log.info(type);
    }

    private List<EnumOrigin> getOrderedOrigins(InformedStack target) {
        final List<EnumOrigin> origins = Arrays.stream(EnumOrigin.values())
                .filter(target::hasOrigin)
                .sorted((o1, o2) -> o1.toString().compareTo(o2.toString()))
                .collect(Collectors.toList());
        if (origins.contains(EnumOrigin.FORCED)) {
            origins.remove(EnumOrigin.FORCED);
            origins.add(0, EnumOrigin.FORCED);
        }
        if (origins.contains(EnumOrigin.OTHER)) {
            origins.remove(EnumOrigin.OTHER);
            origins.add(EnumOrigin.OTHER);
        }
        return origins;
    }

    private String getOriginSubtitle(EnumOrigin origin) {
        String key = "gui.display.origin." + origin.toCamelCase();
        return new TextComponentTranslation(key).getFormattedText();
    }

    public float getzLevel() {
        return this.zLevel;
    }

    public void setzLevel(float zLevel) {
        this.zLevel = zLevel;
    }

    public net.minecraft.client.renderer.RenderItem getItemRender() {
        return this.itemRender;
    }
}
