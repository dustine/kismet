package dustine.kismet.client.gui;

import dustine.kismet.Reference;
import dustine.kismet.block.BlockTimedDisplay;
import dustine.kismet.gui.inventory.ContainerDisplay;
import dustine.kismet.gui.inventory.SlotTarget;
import dustine.kismet.item.ItemKey;
import dustine.kismet.target.EnumOrigin;
import dustine.kismet.target.InformedStack;
import dustine.kismet.tile.TileDisplay;
import dustine.kismet.util.StackHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.client.config.GuiUtils;

import java.awt.*;
import java.io.IOException;
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
        final InformedStack target = this.display.getTarget();

        super.drawScreen(mouseX, mouseY, partialTicks);
        int relOx = (this.width - this.xSize) / 2;
        int relOy = (this.height - this.ySize) / 2;

        // extra code to render the origin tooltip
        if (target != null) {

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
        if (inventoryplayer.getItemStack() == null && this.isMouseOverSlot(this.targetSlot, mouseX, mouseY) &&
                this.targetSlot.getHasStack()) {
            ItemStack stack = this.targetSlot.getStack();
            this.renderToolTip(stack, mouseX, mouseY);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // text tags
        // retrieved from GuiChest so idk what any of these constants do :P
        String s = new TextComponentTranslation("gui.display.name").getFormattedText();
        this.fontRendererObj.drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, 6,
                Reference.Colors.TEXT_GREY);
        this.fontRendererObj.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8,
                this.ySize - 96 + 2, Reference.Colors.TEXT_GREY);

        // text info slots
        int iconLocation = 0;
        if (this.display.getBlockType() instanceof BlockTimedDisplay) {
            this.fontRendererObj.drawString(this.display.getStylizedDeadline(false),
                    infoTextOrigin.x,
                    infoTextOrigin.y + (infoIconSize + 1) * iconLocation++,
                    Reference.Colors.TEXT_GREY);
        }

        this.fontRendererObj.drawString(this.display.getStylizedKeyChance(),
                infoTextOrigin.x,
                infoTextOrigin.y + (infoIconSize + 1) * iconLocation++,
                Reference.Colors.TEXT_GREY);

        this.fontRendererObj.drawString(String.valueOf(this.display.getScore()),
                infoTextOrigin.x,
                infoTextOrigin.y + (infoIconSize + 1) * iconLocation,
                Reference.Colors.TEXT_GREY);

        // target slot highlight
        if (shouldTargetSlotHighlight(mouseX, mouseY)) {
            this.mc.getTextureManager().bindTexture(Reference.GUI.HIGHLIGHT);
            int slotX = this.targetSlot.getRealO().x;
            int slotY = this.targetSlot.getRealO().y;
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.colorMask(true, true, true, false);
            final int size = (int) (16 * this.targetSlot.getRealFactor());
            this.drawGradientRect(slotX, slotY, slotX + size, slotY + size, -2130706433, -2130706433);
            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
        }
    }

    private boolean shouldTargetSlotHighlight(int mouseX, int mouseY) {
        final ItemStack stack = this.playerInventory.getItemStack();
        return stack != null && isMouseOverSlot(this.targetSlot, mouseX, mouseY) &&
                (stack.getItem() instanceof ItemKey || StackHelper.isEquivalent(this.display.getTarget(), stack));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        final InformedStack target = this.display.getTarget();
        int relOx = (this.width - this.xSize) / 2;
        int relOy = (this.height - this.ySize) / 2;

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(Reference.GUI.DISPLAY);
        this.drawTexturedModalRect(relOx, relOy, 0, 0, this.xSize, this.ySize);

        if (target != null) {
            int iconLocation = 0;
            // origin icons
            final List<EnumOrigin> origins = getOrderedOrigins(target);

            for (EnumOrigin origin : origins) {
                if (target.hasOrigin(origin)) {
                    this.drawTexturedModalRect(relOx + originIconOrigin.x + originIconSize * iconLocation++,
                            relOy + originIconOrigin.y, originIconSize * origin.ordinal(), originIconTextureY,
                            originIconSize, originIconSize);
                }
            }
        }

        // text info icons
        int iconLocation = 0;
        if (this.display.getBlockType() instanceof BlockTimedDisplay) {
            this.drawTexturedModalRect(relOx + infoIconOrigin.x,
                    relOy + infoIconOrigin.y + (infoIconSize + 1) * iconLocation++,
                    infoIconTextureX,
                    infoIconSize * EnumIcon.TIME.ordinal(),
                    infoIconSize,
                    infoIconSize);
        }

        this.drawTexturedModalRect(relOx + infoIconOrigin.x,
                relOy + infoIconOrigin.y + (infoIconSize + 1) * iconLocation++,
                infoIconTextureX,
                infoIconSize * EnumIcon.KEY.ordinal(),
                infoIconSize,
                infoIconSize);

        this.drawTexturedModalRect(relOx + infoIconOrigin.x,
                relOy + infoIconOrigin.y + (infoIconSize + 1) * iconLocation,
                infoIconTextureX,
                infoIconSize * EnumIcon.SCORE.ordinal(),
                infoIconSize,
                infoIconSize);

        // draw the cyan background if it equals the target or target is fulfilled
        this.mc.getTextureManager().bindTexture(Reference.GUI.HIGHLIGHT);

        for (int i1 = 0; i1 < this.inventorySlots.inventorySlots.size(); ++i1) {
            Slot slot = this.inventorySlots.inventorySlots.get(i1);
            if (slot == this.targetSlot) {
                if (this.display.isFulfilled()) {
                    final SlotTarget slotTarget = (SlotTarget) slot;
                    final int slotSize = (int) (16 * slotTarget.getRealFactor());
                    int slotX = relOx + slotTarget.getRealO().x;
                    int slotY = relOy + slotTarget.getRealO().y;
                    this.drawTexturedModalRect(slotX, slotY, 0, 0, slotSize, slotSize);
                }
            } else if (!this.display.isFulfilled() && slot.getHasStack() && target != null &&
                    StackHelper.isEquivalent(target, slot.getStack())) {
                int slotX = relOx + slot.xDisplayPosition;
                int slotY = relOy + slot.yDisplayPosition;
                this.drawTexturedModalRect(slotX, slotY, 0, 0, 16, 16);
            }
        }
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
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (isMouseOverSlot(targetSlot, mouseX, mouseY) && mouseButton == 0 && playerInventory.getItemStack() != null) {
            final ItemStack stack = playerInventory.getItemStack();
            ((ContainerDisplay) inventorySlots).emulateItemRightClick(playerInventory.player, stack, -1);
            if (stack.stackSize <= 0) {
                playerInventory.setItemStack(null);
            }
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

    float getZLevel() {
        return this.zLevel;
    }

    void setZLevel(float zLevel) {
        this.zLevel = zLevel;
    }

    net.minecraft.client.renderer.RenderItem getItemRender() {
        return this.itemRender;
    }

    Minecraft getMc() {
        return this.mc;
    }
}
