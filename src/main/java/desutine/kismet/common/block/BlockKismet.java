package desutine.kismet.common.block;

import desutine.kismet.client.CreativeTabKismet;
import desutine.kismet.reference.Reference;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockKismet extends Block {
    public BlockKismet(String name) {
        this(name, Material.rock);
    }

    public BlockKismet(String name, Material material) {
        super(material);
        setRegistryName(Reference.MODID, name);
        setUnlocalizedName(name);
        setCreativeTab(CreativeTabKismet.KISMET_TAB);
    }
}
