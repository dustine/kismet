package desutine.kismet.client.render;

import desutine.kismet.common.block.BlockDisplay;
import desutine.kismet.common.tile.TileDisplay;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemSkull;
import net.minecraft.util.EnumFacing;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RenderTileDisplay extends TileEntitySpecialRenderer<TileDisplay> {
    /**
     * Code greatly based on {net.minecraft.client.renderer.entity.RenderItemFrame} (not my fault this class
     * has no documentation)
     */
    @Override
    public void renderTileEntityAt(TileDisplay te, double x, double y, double z, float partialTicks, int destroyStage) {
        if (te == null) return;

        // if we don't have a fulfilled target, show it
//        if (!te.isFulfilled()) {
            renderTargetItem(te, x, y, z);
//        }

        // always show the label above though
        // BUT make it snazzy: only show the streak if 2+
        final float textBoxOffset = 0.4f;
        if (te.getStreak() > 1) {
            renderTextBox(x, y + textBoxOffset, z,
                    Arrays.asList("Streak: " + te.getStylizedStreak(), te.getStylizedDeadline()));
        } else {
            renderTextLabel(x, y + textBoxOffset, z, te.getStylizedDeadline());
        }
    }

    private void renderTargetItem(TileDisplay te, double x, double y, double z) {
        VertexBuffer worldRenderer = Tessellator.getInstance().getBuffer();
        IBlockState state = te.getWorld().getBlockState(te.getPos());

        RenderItem itemRenderer = Minecraft.getMinecraft().getRenderItem();
        EnumFacing direction = state.getValue(BlockDisplay.FACING);
        float facingRot = direction.getHorizontalAngle();
        double push = (state.getValue(BlockDisplay.FULFILLED) ? -0.03125 : 0.03125);
        double xPush = (0.5 + push) * direction.getFrontOffsetX() + 0.5;
        double zPush = (0.5 + push) * direction.getFrontOffsetZ() + 0.5;

        GlStateManager.pushMatrix();

        GlStateManager.translate(x + xPush, y + 0.5, z + zPush);
        GlStateManager.rotate(180 - facingRot, 0, 1, 0);
        GlStateManager.scale(0.5, 0.5, 0.5);

        // item rendering!
        if (te.getTarget() != null) {
            if (!itemRenderer.shouldRenderItemIn3D(te.getTarget()) || te.getTarget().getItem() instanceof ItemSkull) {
                GlStateManager.rotate(180.0F, 0.0F, -1.0F, 0.0F);
            } else {
                GlStateManager.scale(1.25, 1.25, 1.25);
            }

//            if (itemRenderer.shouldRenderItemIn3D(te.getTarget())) {
//                switch (direction){
//                    case DOWN:
//                        break;
//                    case UP:
//                        break;
//                    case NORTH:
//                        GlStateManager.scale(0.02, 1, 1);
//                        break;
//                    case SOUTH:
//                        GlStateManager.scale(direction.getFrontOffsetX(), 1, direction.getFrontOffsetZ());
//                        break;
//                    case WEST:
//                        GlStateManager.scale(direction.getFrontOffsetX(), 1, direction.getFrontOffsetZ());
//                        break;
//                    case EAST:
//                        GlStateManager.scale(direction.getFrontOffsetX(), 1, direction.getFrontOffsetZ());
//                        break;
//                }
//            }

//            GlStateManager.disableLighting();
            GlStateManager.pushAttrib();
            RenderHelper.enableStandardItemLighting();
            itemRenderer.renderItem(te.getTarget(), ItemCameraTransforms.TransformType.FIXED);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popAttrib();
            GlStateManager.enableLighting();
        }

        GlStateManager.popMatrix();
    }

    private void renderTextBox(double x, double y, double z, List<String> lines) {
        // text render happens in the opposite order ^^"
        Collections.reverse(lines);
        // start the rendering
        GlStateManager.pushMatrix();

        // move stuff to right above the block
        GlStateManager.translate(x + 0.5f, y + 1f, z + 0.5f);
        // sets the normal vector of the lightcast aka light angle
//        GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);

        // rotate so it faces the player
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        float playerAngle = renderManager.playerViewY;
        GlStateManager.rotate(-playerAngle, 0.0F, 1.0F, 0.0F);
        // take into account 3rd person view
        GlStateManager.rotate((float) (renderManager.options.thirdPersonView == 2 ? -1 : 1) * renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        // one more rotate of my own so the text doesn't render backwards
        GlStateManager.rotate(180, 0, 0, 1);
        // scale it so IT DOESN'T TAKE THE WHOLE SKY, OMG
        GlStateManager.scale(0.025, 0.025, 0.025);

        GlStateManager.disableLighting();

        GlStateManager.depthMask(true);
        GlStateManager.disableDepth();

        // target string
        FontRenderer fontRenderer = getFontRenderer();

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        GlStateManager.disableTexture2D();
        // all this is to draw the dark box behind the name
        int stringWidth = fontRenderer.getStringWidth(
                lines.stream()
                        .max((o1, o2) -> fontRenderer.getStringWidth(o1) - fontRenderer.getStringWidth(o2))
                        .orElseGet(() -> "")) / 2;

        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexBuffer = tessellator.getBuffer();
        // this sets the format of the vertexBuffer : position,color
        // the coordinates are 4 because we're defining a rectangle
        // no weird coordinates because we already moved everything up on the tranforms
        vertexBuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vertexBuffer.pos(-stringWidth - 1, -(9 * lines.size() - 8), 0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
        vertexBuffer.pos(-stringWidth - 1, 8, 0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
        vertexBuffer.pos(stringWidth + 1, 8, -0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
        vertexBuffer.pos(stringWidth + 1, -(9 * lines.size() - 8), 0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();

        for (int i = 0; i < lines.size(); i++)
            fontRenderer.drawString(lines.get(i), -fontRenderer.getStringWidth(lines.get(i)) / 2, -(i * 9), 0xFFFFFF);
//        fontRenderer.FONT_HEIGHT

        GlStateManager.enableDepth();

        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
//        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        // finish the GL rendering
        GlStateManager.popMatrix();
    }

    private void renderTextLabel(double x, double y, double z, String str) {
    /*
    Next up: rendering the string above the block
    code shamesly taken from source game on Render::renderLivingLabel
     */
        renderTextBox(x, y, z, Collections.singletonList(str));
    }
}
