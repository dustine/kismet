package desutine.kismet.client.render;

import desutine.kismet.common.block.BlockDisplay;
import desutine.kismet.common.block.BlockTimedDisplay;
import desutine.kismet.common.tile.TileDisplay;
import desutine.kismet.server.InformedStack;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
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

        // check if we have a blockTimedDisplay, and if so, its status on fulfillment
        final Boolean fulfilled = getWorld().getBlockState(te.getPos()).getValue(BlockDisplay.FULFILLED);

        // if we don't have a fulfilled target, show it
        if (!fulfilled) {
            renderTargetItem(te, x, y, z, partialTicks);
        }

        List<String> lines = new ArrayList<>();
        if (te.getBlockType() instanceof BlockTimedDisplay) {
            // BUT make it snazzy: only show the streak if 2+
            if (te.getScore() > 1) {
                lines.add(I18n.format("tile.timedDisplay.tag.streak", te.getStylizedScore()));
            }
            if (!fulfilled) {
                lines.add(te.getStylizedDeadline());
            } else {
                lines.add(I18n.format("tile.timedDisplay.tag.done"));
            }
        } else {
            lines.add(I18n.format("tile.chillDisplay.tag.score", te.getStylizedScore()));
        }

        // always show the label above though
        if (!lines.isEmpty()) {
            final float textBoxOffset = 0.4f;
            renderTextBox(te, x, y + textBoxOffset, z, lines);
        }
    }

    private void renderTargetItem(TileDisplay te, double x, double y, double z, float partialTicks) {
        double loopTime = 10;
        double tick = (getWorld().getTotalWorldTime() + partialTicks) % (20 * loopTime) / (20 * loopTime);

        IBlockState state = te.getWorld().getBlockState(te.getPos());

        RenderItem itemRenderer = Minecraft.getMinecraft().getRenderItem();
        EnumFacing direction = state.getValue(BlockDisplay.FACING);
//        float facingRot = direction.getHorizontalAngle();
//        double push = (state.getValue(BlockDisplay.FULFILLED) ? -0.03125 : 0.03125);
        double xPush = 0.1 * direction.getFrontOffsetX() + 0.5;
        double yPush = 0.1 * direction.getFrontOffsetY() + 0.5;
        double zPush = 0.1 * direction.getFrontOffsetZ() + 0.5;

        float facingRot = (float) (tick * 360);

        GlStateManager.pushMatrix();

        GlStateManager.translate(x + xPush, y + yPush, z + zPush);
        GlStateManager.rotate(facingRot, 0, 1, 0);

        // item rendering!
        final InformedStack target = te.getTarget();
        if (target != null && target.hasItem()) {
            ItemStack stack = target.getStack();
            if (!itemRenderer.shouldRenderItemIn3D(stack) || stack.getItem() instanceof ItemSkull) {
                GlStateManager.rotate(180, 0, -1, 0);
                GlStateManager.scale(0.5, 0.5, 0.5);
            } else {
                GlStateManager.scale(0.75, 0.75, 0.75);
            }

            GlStateManager.disableLighting();

            GlStateManager.pushAttrib();
            RenderHelper.enableStandardItemLighting();
            itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popAttrib();

            GlStateManager.enableLighting();
        }

        GlStateManager.popMatrix();
    }

    private void renderTextBox(TileDisplay te, double x, double y, double z, List<String> lines) {
        // text render happens in the opposite order ^^"
        Collections.reverse(lines);
        // start the rendering
        GlStateManager.pushMatrix();

        // move stuff to right above the item
        IBlockState state = te.getWorld().getBlockState(te.getPos());
        EnumFacing direction = state.getValue(BlockDisplay.FACING);
        double xPush = 0.1 * direction.getFrontOffsetX() + 0.5;
        double yPush = 0.1 * direction.getFrontOffsetY() + 0.6;
        double zPush = 0.1 * direction.getFrontOffsetZ() + 0.5;

        GlStateManager.translate(x + xPush, y + yPush, z + zPush);

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

        // finish the GL rendering
        GlStateManager.popMatrix();
    }

    private void renderTextLabel(TileDisplay te, double x, double y, double z, String str) {
    /*
    Next up: rendering the string above the block
    code shamesly taken from source game on Render::renderLivingLabel
     */
        renderTextBox(te, x, y, z, Collections.singletonList(str));
    }
}
