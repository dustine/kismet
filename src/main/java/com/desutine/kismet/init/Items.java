package com.desutine.kismet.init;

import com.desutine.kismet.item.ItemKey;
import com.desutine.kismet.item.ItemKismet;
import com.desutine.kismet.reference.Names;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class Items {
    public static final ItemKismet itemKey = new ItemKey();

    public static void init() {
        GameRegistry.registerItem(itemKey, Names.KEY_NAME);
    }

    public static void render() {

        // register inventory models :\
        ModelLoader.setCustomModelResourceLocation(itemKey, 0, new ModelResourceLocation("kismet:key",
                "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Blocks.kismetDisplayBlock), 0, new
                ModelResourceLocation("kismet:display", "inventory"));
    }
}
