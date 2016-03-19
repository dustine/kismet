package com.desutine.kismet.block;

import com.desutine.kismet.block.BlockKismet;
import com.desutine.kismet.block.BlockDisplay;
import com.desutine.kismet.reference.Blocks;
import com.desutine.kismet.reference.Reference;
import net.minecraftforge.fml.common.registry.GameRegistry;

@GameRegistry.ObjectHolder(Reference.MODID)
public class ModBlocks {
    public static final BlockKismet kismetDisplayBlock = new BlockDisplay();

    public static void init() {
        GameRegistry.registerBlock(kismetDisplayBlock, Blocks.DISPLAY_NAME);
    }
}
