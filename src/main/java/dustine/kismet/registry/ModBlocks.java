package dustine.kismet.registry;

import dustine.kismet.Kismet;
import dustine.kismet.Reference;
import dustine.kismet.block.BlockChillDisplay;
import dustine.kismet.block.BlockKismet;
import dustine.kismet.block.BlockTimedDisplay;
import dustine.kismet.item.ItemBlockKismet;
import net.minecraftforge.fml.common.registry.GameRegistry;

@GameRegistry.ObjectHolder(Reference.MOD_ID)
public class ModBlocks {
    public static final BlockTimedDisplay TIMED_DISPLAY = new BlockTimedDisplay();
    public static final BlockChillDisplay CHILL_DISPLAY = new BlockChillDisplay();
//    public static final Block doubleStoneSlab = Blocks.double_stone_slab;

    public static void init() {
        registerBlock(TIMED_DISPLAY, Reference.Names.Blocks.TIMED_DISPLAY);
        registerBlock(CHILL_DISPLAY, Reference.Names.Blocks.CHILL_DISPLAY);
    }

    private static void registerBlock(BlockKismet block, String name) {
        if (block.getRegistryName() == null)
            block.setRegistryName(name);

        GameRegistry.register(block);
        block.setUnlocalizedName(name);
        GameRegistry.register(new ItemBlockKismet(block));
        Kismet.proxy.registerInventoryModel(block);
    }
}
