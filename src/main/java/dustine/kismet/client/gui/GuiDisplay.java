package dustine.kismet.client.gui;

import dustine.kismet.Reference;
import dustine.kismet.block.BlockTimedDisplay;
import dustine.kismet.gui.inventory.ContainerDisplay;
import dustine.kismet.gui.inventory.SlotTarget;
import dustine.kismet.target.EnumOrigin;
import dustine.kismet.target.Target;
import dustine.kismet.tile.TileDisplay;
import dustine.kismet.util.TargetHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.client.config.GuiUtils;

import java.awt.*;
import java.io.IOException;
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
    private final InventoryPlayer playerInventory;
    private final TileDisplay display;
    private final RenderTargetSlot renderTargetSlot = new RenderTargetSlot(this);
    private final SlotTarget targetSlot;

    public GuiDisplay(InventoryPlayer playerInventory, TileDisplay display) {
        super(new ContainerDisplay(playerInventory, display));
        this.playerInventory = playerInventory;
        this.display = display;
        this.targetSlot = (SlotTarget) this.inventorySlots.getSlot(0);
        this.xSize = 176;
        this.ySize = 164;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        final Target target = this.display.getTarget();

        super.drawScreen(mouseX, mouseY, partialTicks);

        int relOx = (this.width - this.xSize) / 2;
        int relOy = (this.height - this.ySize) / 2;

        // extra code to render the origin tooltip
        if (target != null) {
            int relMouseX = mouseX - relOx;
            int relMouseY = mouseY - relOy;

            if (originIconsBoundingBox.contains(relMouseX, relMouseY)) {
                final List<EnumOrigin> origins = getOrderedOrigins(target);
                int ordinal = (relMouseX - (int) (originIconsBoundingBox.getX())) / (originIconSize + 1);
                if (ordinal >= origins.size()) return;

                GuiUtils.drawHoveringText(Collections.singletonList(getOriginSubtitle(origins.get(ordinal))),
                        mouseX, mouseY, this.width, this.height, -1, this.fontRendererObj);
            }
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
        String score = String.valueOf(this.display.getScore());
        if (this.display.getBlockType() instanceof BlockTimedDisplay) {
            this.fontRendererObj.drawString(this.display.getStylizedDeadline(false),
                    infoTextOrigin.x,
                    infoTextOrigin.y + (infoIconSize + 1) * iconLocation++,
                    Reference.Colors.TEXT_GREY);
            score = String.format("%s (%d)", score, this.display.getHighScore());
        }

        this.fontRendererObj.drawString(this.display.getStylizedKeyChance(),
                infoTextOrigin.x,
                infoTextOrigin.y + (infoIconSize + 1) * iconLocation++,
                Reference.Colors.TEXT_GREY);

        this.fontRendererObj.drawString(score,
                infoTextOrigin.x,
                infoTextOrigin.y + (infoIconSize + 1) * iconLocation,
                Reference.Colors.TEXT_GREY);

        // target slot highlight (beyond the vanilla 16x16)
        if (isMouseOverSlot(this.targetSlot, mouseX, mouseY)) {
            int slotX = this.targetSlot.xDisplayPosition;
            int slotY = this.targetSlot.yDisplayPosition;
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.colorMask(true, true, true, false);
            final int size = this.targetSlot.getSlotSize();
            this.drawGradientRect(slotX + 16, slotY, slotX + size, slotY + size, -2130706433, -2130706433);
            this.drawGradientRect(slotX, slotY + 16, slotX + 16, slotY + size, -2130706433, -2130706433);
            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        final Target target = this.display.getTarget();
        int relOx = (this.width - this.xSize) / 2;
        int relOy = (this.height - this.ySize) / 2;

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        final TextureManager textureManager = this.mc.getTextureManager();
        textureManager.bindTexture(Reference.Gui.DISPLAY);
        this.drawTexturedModalRect(relOx, relOy, 0, 0, this.xSize, this.ySize);

        // origin icons
        if (target != null) {
            int iconLocation = 0;
            final List<EnumOrigin> origins = getOrderedOrigins(target);

            for (EnumOrigin origin : origins) {
                if (target.hasOrigin(origin)) {
                    textureManager.bindTexture(new ResourceLocation(
                            String.format(Reference.Gui.ORIGIN, origin.toCamelCase())));
                    this.drawTexturedModalRect(relOx + originIconOrigin.x + originIconSize * iconLocation++,
                            relOy + originIconOrigin.y, 0, 0, originIconSize, originIconSize);
                }
            }
        }

        // text info icons
        int iconLocation = 0;
        textureManager.bindTexture(Reference.Gui.TIME);
        if (this.display.getBlockType() instanceof BlockTimedDisplay) {
            this.drawTexturedModalRect(relOx + infoIconOrigin.x,
                    relOy + infoIconOrigin.y + (infoIconSize + 1) * iconLocation++,
                    0, 0, infoIconSize, infoIconSize);
        }

        textureManager.bindTexture(Reference.Gui.KEYS);
        this.drawTexturedModalRect(relOx + infoIconOrigin.x,
                relOy + infoIconOrigin.y + (infoIconSize + 1) * iconLocation++,
                0, 0, infoIconSize, infoIconSize);

        textureManager.bindTexture(Reference.Gui.SCORE);
        this.drawTexturedModalRect(relOx + infoIconOrigin.x,
                relOy + infoIconOrigin.y + (infoIconSize + 1) * iconLocation,
                0, 0, infoIconSize, infoIconSize);

        // draw a background highlight under a slot if it equals the target
        // or under the target slot if it is fulfilled
        textureManager.bindTexture(Reference.Gui.HIGHLIGHT);

        for (int i1 = 0; i1 < this.inventorySlots.inventorySlots.size(); ++i1) {
            Slot slot = this.inventorySlots.inventorySlots.get(i1);
            int slotX = relOx + slot.xDisplayPosition;
            int slotY = relOy + slot.yDisplayPosition;
            if (slot == this.targetSlot) {
                if (this.display.isFulfilled()) {
                    final SlotTarget slotTarget = (SlotTarget) slot;
                    final int slotSize = slotTarget.getSlotSize();
                    this.drawTexturedModalRect(slotX, slotY, 0, 0, slotSize, slotSize);
                }
            } else if (!this.display.isFulfilled() && slot.getHasStack() && target != null &&
                    TargetHelper.isEquivalent(target, slot.getStack())) {
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
        if (isMouseOverSlot(this.targetSlot, mouseX, mouseY) && mouseButton == 0 && this.playerInventory
                .getItemStack() != null) {
            final ItemStack stack = this.playerInventory.getItemStack();
            ((ContainerDisplay) this.inventorySlots).emulateItemRightClick(this.playerInventory.player, stack, -1);
            if (stack.stackSize <= 0) {
                this.playerInventory.setItemStack(null);
            }
        }
    }

    @Override
    protected boolean isMouseOverSlot(Slot slotIn, int mouseX, int mouseY) {
        if (slotIn instanceof SlotTarget) {
            final int size = ((SlotTarget) slotIn).getSlotSize();
            return this.isPointInRegion(slotIn.xDisplayPosition, slotIn.yDisplayPosition, size, size, mouseX, mouseY);
        } else {
            return super.isMouseOverSlot(slotIn, mouseX, mouseY);
        }
    }

    private List<EnumOrigin> getOrderedOrigins(Target target) {
        return EnumOrigin.getSorted(true).stream().filter(target::hasOrigin).collect(Collectors.toList());
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
