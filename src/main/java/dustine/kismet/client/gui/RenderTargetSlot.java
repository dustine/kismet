package dustine.kismet.client.gui;

import dustine.kismet.gui.inventory.SlotTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ReportedException;
import net.minecraftforge.client.ForgeHooksClient;

/**
 * The code from {@link net.minecraft.client.renderer.RenderItem} and {@link GuiContainer} but changed to render instead
 * a TargetItemSlot (view-only slot that has a variable size)
 */
class RenderTargetSlot {
    private final GuiDisplay guiDisplay;

    RenderTargetSlot(GuiDisplay guiDisplay) {
        this.guiDisplay = guiDisplay;
    }

    /**
     * Code from {@link GuiContainer} Largely changed (mostly cutting off unnecessary code)
     *
     * @param slot the target slot to be rendered
     */
    void drawTargetSlot(SlotTarget slot) {
        int x = slot.xDisplayPosition;
        int y = slot.yDisplayPosition;
        int size = (int) (16 * slot.getFactor());
        ItemStack stack = slot.getStack();
        final Minecraft minecraft = this.guiDisplay.getMc();
        minecraft.thePlayer.inventory.getItemStack();
        boolean renderItem = stack != null;

        // all code that should be here from the main function was removed as it was dealing with rendering item
        // behaviour related to the player changing the slot contents. as the target slot is just a view, all of it
        // wasn't necessary

        this.guiDisplay.setZLevel(100.0F);
        this.guiDisplay.setZLevel(100.0F);

        if (renderItem) {
            GlStateManager.enableDepth();

            // can't use the default item renderer here as it assumes the slot size is 16
//            this.itemRender.renderItemAndEffectIntoGUI(this.mc.thePlayer, itemstack, i, j);
            renderItemAndEffectIntoGUI(minecraft.thePlayer, stack, slot.getFactor(), x, y);

            // not needed as we won't have a stack size and/or damage on the item stack
        }

        this.guiDisplay.setZLevel(0.0F);
        this.guiDisplay.setZLevel(0.0F);
    }

    /**
     * Code from {@link net.minecraft.client.renderer.RenderItem} Beyond adding the factor, did some 1.8 lambda instant
     * transforms that I'll revert later when I port it back to 1.7 or so
     */
    private void renderItemAndEffectIntoGUI(EntityPlayerSP player, ItemStack stack, double factor, int x, int y) {
        if (stack != null && stack.getItem() != null) {
            this.guiDisplay.setZLevel(this.guiDisplay.getZLevel() + 50.0F);

            try {
                renderItemModelIntoGUI(stack, factor, x, y, this.guiDisplay.getItemRender().getItemModelWithOverrides(stack, null, player));
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering item");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Item being rendered");
                crashreportcategory.addCrashSectionCallable("Item Type", () -> String.valueOf(stack.getItem()));
                crashreportcategory.addCrashSectionCallable("Item Aux", () -> String.valueOf(stack.getMetadata()));
                crashreportcategory.addCrashSectionCallable("Item NBT", () -> String.valueOf(stack.getTagCompound()));
                crashreportcategory.addCrashSectionCallable("Item Foil", () -> String.valueOf(stack.hasEffect()));
                throw new ReportedException(crashreport);
            }

            this.guiDisplay.setZLevel(this.guiDisplay.getZLevel() - 50.0F);
        }
    }

    /**
     * Code from {@link net.minecraft.client.renderer.RenderItem} The exact same, except for accounting the scale
     * factor
     */
    private void renderItemModelIntoGUI(ItemStack stack, double factor, int x, int y, IBakedModel model) {
        final TextureManager textureManager = this.guiDisplay.getMc().getTextureManager();

        GlStateManager.pushMatrix();
        textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        setupGuiTransform(factor, x, y, model.isGui3d());
        model = ForgeHooksClient.handleCameraTransforms(model, ItemCameraTransforms.TransformType.GUI, false);
        this.guiDisplay.getItemRender().renderItem(stack, model);
        GlStateManager.disableAlpha();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();
        GlStateManager.popMatrix();
        textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
    }

    /**
     * Code from {@link net.minecraft.client.renderer.RenderItem} Here's where the scaling ACTUALLY happens in ALL of
     * the {@link net.minecraft.client.renderer.RenderItem}
     */
    private void setupGuiTransform(double factor, int x, int y, boolean isGui3d) {
        GlStateManager.translate((float) x, (float) y, 100.0F + this.guiDisplay.getZLevel());
        GlStateManager.translate(8.0F * factor, 8.0F * factor, 0.0F);
        GlStateManager.scale(1.0F, -1.0F, 1.0F);
        GlStateManager.scale(16.0F * factor, 16.0F * factor, 16.0F * factor);

        if (isGui3d) {
            GlStateManager.enableLighting();
        } else {
            GlStateManager.disableLighting();
        }
    }
}