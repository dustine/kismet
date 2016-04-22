package desutine.kismet.registry;

import desutine.kismet.Reference;
import desutine.kismet.block.BlockChillDisplay;
import desutine.kismet.block.BlockTimedDisplay;
import net.minecraftforge.fml.common.registry.GameRegistry;

@GameRegistry.ObjectHolder(Reference.MOD_ID)
public class ModBlocks {
    public static final BlockTimedDisplay TIMED_DISPLAY = new BlockTimedDisplay();
    public static final BlockChillDisplay CHILL_DISPLAY = new BlockChillDisplay();
//    public static final Block doubleStoneSlab = Blocks.double_stone_slab;

    public static void init() {
        GameRegistry.register(TIMED_DISPLAY);
        GameRegistry.register(CHILL_DISPLAY);
    }
}
