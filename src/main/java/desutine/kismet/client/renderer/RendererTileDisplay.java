package desutine.kismet.client.renderer;

import desutine.kismet.common.tile.TileDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class RendererTileDisplay extends TileEntitySpecialRenderer<TileDisplay> {
    @Override
    public void renderTileEntityAt(TileDisplay te, double x, double y, double z, float partialTicks, int destroyStage) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        // item rendering!
        if (te.getTarget() != null) {
            Minecraft.getMinecraft().getRenderItem().renderItem(te.getTarget(),
                    ItemCameraTransforms.TransformType.GUI);
        }

        GlStateManager.popMatrix();
    }


}
