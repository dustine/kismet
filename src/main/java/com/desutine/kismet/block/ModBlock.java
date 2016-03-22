package com.desutine.kismet.block;

import com.desutine.kismet.CreativeTabKismet;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class ModBlock extends Block {
    public ModBlock() {
        this(Material.rock);
    }

    public ModBlock(Material material) {
        super(material);
        setCreativeTab(CreativeTabKismet.KISMET_TAB);
    }
}
