package com.desutine.kismet.init;

import com.desutine.kismet.reference.Blocks;
import com.desutine.kismet.reference.Items;
import com.desutine.kismet.reference.Names;
import com.desutine.kismet.reference.Reference;
import com.desutine.kismet.tileentity.DisplayTileEntity;
import com.desutine.kismet.tileentity.DisplayTileEntityRenderer;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientInit {
    public static void initTileEntityRenderers() {
        // and their renderers too
        ClientRegistry.bindTileEntitySpecialRenderer(DisplayTileEntity.class, new DisplayTileEntityRenderer());
    }

    public static void renderInInventory() {
        // register inventory models :\
        ModelLoader.setCustomModelResourceLocation(Items.itemKey, 0, new ModelResourceLocation(Reference.MODID + ':'
                + Names.KEY, "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Blocks.kismetDisplayBlock), 0, new
                ModelResourceLocation(Reference.MODID + ':' + Names.DISPLAY, "inventory"));
    }
}
