package desutine.kismet.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

public class ItemBlockKismet extends ItemBlock {
    public ItemBlockKismet(Block block) {
        super(block);
        String name = block.getRegistryName().getResourcePath();
        setRegistryName(name);
        setUnlocalizedName(name);
    }
}
