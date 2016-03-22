package com.desutine.kismet.reference;

import com.desutine.kismet.block.DisplayBlock;
import com.desutine.kismet.block.ModBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;

@GameRegistry.ObjectHolder(Reference.MODID)
public class Blocks {
    public static final ModBlock kismetDisplayBlock = new DisplayBlock();

}
