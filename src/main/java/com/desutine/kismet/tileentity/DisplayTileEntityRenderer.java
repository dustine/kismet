package com.desutine.kismet.tileentity;

import com.desutine.kismet.ModLogger;
import com.desutine.kismet.reference.Blocks;
import com.desutine.kismet.reference.Items;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.IPerspectiveAwareModel;

public class DisplayTileEntityRenderer extends TileEntitySpecialRenderer<DisplayTileEntity> {

    @Override
    public void renderTileEntityAt(DisplayTileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        // item rendering!
        Minecraft.getMinecraft().getRenderItem().renderItem(new ItemStack(Blocks.kismetDisplayBlock),
                ItemCameraTransforms.TransformType.GUI);

        GlStateManager.pushMatrix();
//
//        GlStateManager.translate(2D, 2D, 2D);

        GlStateManager.popMatrix();

        GlStateManager.popMatrix();
    }
}
