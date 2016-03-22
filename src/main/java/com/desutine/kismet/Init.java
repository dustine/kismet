package com.desutine.kismet;

import com.desutine.kismet.reference.Blocks;
import com.desutine.kismet.reference.Items;
import com.desutine.kismet.reference.Names;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class Init {
    public static void initBlocks() {
        GameRegistry.registerBlock(Blocks.kismetDisplayBlock, Names.DISPLAY_NAME);
    }

    public static void initItems() {
        GameRegistry.registerItem(Items.itemKey, Names.KEY_NAME);
    }

    public static void renderInInventory() {

        // register inventory models :\
        ModelLoader.setCustomModelResourceLocation(Items.itemKey, 0, new ModelResourceLocation("kismet:key",
                "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Blocks.kismetDisplayBlock), 0, new
                ModelResourceLocation("kismet:display", "inventory"));
    }
}
