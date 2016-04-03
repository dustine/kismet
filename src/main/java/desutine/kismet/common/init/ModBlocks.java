package desutine.kismet.common.init;

import desutine.kismet.common.block.BlockDisplay;
import desutine.kismet.reference.Reference;
import net.minecraftforge.fml.common.registry.GameRegistry;

@GameRegistry.ObjectHolder(Reference.MODID)
public class ModBlocks {
    public static final BlockDisplay DISPLAY = new BlockDisplay();
//    public static final Block doubleStoneSlab = Blocks.double_stone_slab;

    public static void init() {
        GameRegistry.register(DISPLAY);
    }
}
