package com.desutine.kismet.init;

import com.desutine.kismet.reference.Blocks;
import com.desutine.kismet.reference.Items;
import com.desutine.kismet.reference.Names;
import com.desutine.kismet.reference.Reference;
import com.desutine.kismet.tileentity.DisplayTileEntity;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class Init {
    public static void initTileEntities() {
        GameRegistry.registerTileEntity(DisplayTileEntity.class, Reference.MODID + ':' + Names.TT_DISPLAY);
    }

    public static void initBlocks() {
        GameRegistry.registerBlock(Blocks.kismetDisplayBlock, Names.DISPLAY);
    }

    public static void initItems() {
        GameRegistry.registerItem(Items.itemKey, Names.KEY);
    }


}
