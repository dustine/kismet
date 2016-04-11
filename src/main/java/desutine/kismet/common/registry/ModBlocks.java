package desutine.kismet.common.registry;

import desutine.kismet.Reference;
import desutine.kismet.common.block.BlockDisplay;
import net.minecraftforge.fml.common.registry.GameRegistry;

@GameRegistry.ObjectHolder(Reference.MOD_ID)
public class ModBlocks {
    public static final BlockDisplay DISPLAY = new BlockDisplay();
//    public static final Block doubleStoneSlab = Blocks.double_stone_slab;

    public static void init() {
        GameRegistry.register(DISPLAY);
    }
}
