package com.desutine.kismet.init;

import com.desutine.kismet.block.BlockDisplay;
import com.desutine.kismet.block.ModBlock;
import com.desutine.kismet.reference.Names;
import com.desutine.kismet.reference.Reference;
import net.minecraftforge.fml.common.registry.GameRegistry;

@GameRegistry.ObjectHolder(Reference.MODID)
public class Blocks {
    public static final ModBlock kismetDisplayBlock = new BlockDisplay();

    public static void init() {
        GameRegistry.registerBlock(kismetDisplayBlock, Names.DISPLAY_NAME);
    }
}
