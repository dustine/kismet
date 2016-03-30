package desutine.kismet.client.renderer;

import desutine.kismet.ModLogger;
import desutine.kismet.common.tile.TileDisplay;
import javafx.scene.shape.VertexFormat;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.List;

public class RendererTileDisplay extends TileEntitySpecialRenderer<TileDisplay> {
    @Override
    public void renderTileEntityAt(TileDisplay te, double x, double y, double z, float partialTicks, int destroyStage) {
        VertexBuffer worldRenderer = Tessellator.getInstance().getBuffer();
        GlStateManager.pushAttrib();
        GlStateManager.pushMatrix();

        GlStateManager.translate(x + 0.5f, y + 0.5f, z - 0.06f);
        GlStateManager.rotate(180, 0, 1, 0);
        GlStateManager.scale(0.5, 0.5, 0.5);


//        TRSRTransformation.
//        ItemCameraTransforms.TransformType.FIXED

        // item rendering!
        if (te.getTarget() != null) {
            IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(te.getTarget());
//            ModLogger.info(model.getQuads(null, null, 0));

            Minecraft.getMinecraft().getRenderItem().renderItem(te.getTarget(), ItemCameraTransforms.TransformType.GUI);
        }

        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }


}
