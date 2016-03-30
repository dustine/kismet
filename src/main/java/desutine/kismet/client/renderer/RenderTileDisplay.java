package desutine.kismet.client.renderer;

import desutine.kismet.ModLogger;
import desutine.kismet.common.block.BlockDisplay;
import desutine.kismet.common.tile.TileDisplay;
import javafx.scene.shape.VertexFormat;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemSkull;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.List;

public class RenderTileDisplay extends TileEntitySpecialRenderer<TileDisplay> {
    /**
     * Code greatly based on {net.minecraft.client.renderer.entity.RenderItemFrame} (not my fault this class
     * has no documentation)
     */
    @Override
    public void renderTileEntityAt(TileDisplay te, double x, double y, double z, float partialTicks, int destroyStage) {
        if(te == null) return;

        VertexBuffer worldRenderer = Tessellator.getInstance().getBuffer();
        IBlockState state = te.getWorld().getBlockState(te.getPos());

        RenderItem itemRenderer = Minecraft.getMinecraft().getRenderItem();
        EnumFacing direction = state.getValue(BlockDisplay.FACING);
        float facingRot = direction.getHorizontalAngle();
        double push = state.getValue(BlockDisplay.FULFILLED) ? 0.05 : 0.06;
        double xPush = (0.5 + push) * direction.getFrontOffsetX() + 0.5;
        double zPush = (0.5 + push) * direction.getFrontOffsetZ() + 0.5;

        GlStateManager.pushMatrix();

        GlStateManager.translate(x + xPush, y + 0.5, z + zPush);
        GlStateManager.rotate(180 - facingRot, 0, 1, 0);
        GlStateManager.scale(0.5, 0.5, 0.5);


        // item rendering!
        if (te.getTarget() != null) {
//            IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(te.getTarget());
//            ModLogger.info(model.getQuads(null, null, 0));
//            TRSRTransformation

            if (!itemRenderer.shouldRenderItemIn3D(te.getTarget()) || te.getTarget().getItem() instanceof ItemSkull)
            {
                GlStateManager.rotate(180.0F, 0.0F, -1.0F, 0.0F);
            } else {
                GlStateManager.scale(1.25, 1.25, 1.25);
            }

            GlStateManager.pushAttrib();
            RenderHelper.enableStandardItemLighting();
            itemRenderer.renderItem(te.getTarget(), ItemCameraTransforms.TransformType.FIXED);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popAttrib();
        }

        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();

//        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(x + 0.5f, y + 1.2f, z + 0.5f);
        GlStateManager.scale(0.01, 0.01, 0.01);
        float playerAngle = Minecraft.getMinecraft().thePlayer.getRotationYawHead();
        GlStateManager.rotate(-playerAngle, 0, 1, 0);
        GlStateManager.rotate(180, 0, 0, 1);

//        getFontRenderer().drawString("tHIS IS A TEST", (int)x, (int)(y+2), (int)z);
        getFontRenderer().drawString("00:00:23", 0, 2, 0);

        GlStateManager.popMatrix();
    }
}
