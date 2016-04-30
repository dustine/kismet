package dustine.kismet.block;

import dustine.kismet.client.CreativeTabKismet;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockKismet extends Block {
    public BlockKismet() {
        this(Material.ROCK);
    }

    public BlockKismet(Material material) {
        super(material);
        setHardness(5);
        setHarvestLevel("pickaxe", 2);
        setCreativeTab(CreativeTabKismet.KISMET_TAB);
    }
}
