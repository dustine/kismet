package desutine.kismet.common.init;

import desutine.kismet.common.block.BlockDisplay;
import desutine.kismet.common.block.BlockKismet;
import desutine.kismet.reference.Names;
import desutine.kismet.reference.Reference;
import net.minecraftforge.fml.common.registry.GameRegistry;

@GameRegistry.ObjectHolder(Reference.MODID)
public class ModBlocks {
    public static final BlockKismet DISPLAY = new BlockDisplay();
//    public static final Block doubleStoneSlab = Blocks.double_stone_slab;

    public static void init() {
        GameRegistry.registerBlock(DISPLAY, Names.Blocks.DISPLAY);
    }
}
