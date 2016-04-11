package desutine.kismet.common.block;

import desutine.kismet.Reference;
import desutine.kismet.client.CreativeTabKismet;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;

public class BlockKismet extends Block {
    public BlockKismet(String name) {
        this(name, Material.rock);
    }

    public BlockKismet(String name, Material material) {
        super(material);
        setRegistryName(new ResourceLocation(Reference.MOD_ID + ':' + name));
        setUnlocalizedName(name);
        setCreativeTab(CreativeTabKismet.KISMET_TAB);
    }
}