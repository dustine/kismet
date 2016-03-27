package desutine.kismet.common;

import desutine.kismet.reference.Blocks;
import desutine.kismet.reference.Items;
import desutine.kismet.reference.Names;
import desutine.kismet.reference.Reference;
import desutine.kismet.common.tile.TileDisplay;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CommonInit {
    public static void initTileEntities() {
        GameRegistry.registerTileEntity(TileDisplay.class, Reference.MODID + ':' + Names.TT_DISPLAY);
    }

    public static void initBlocks() {
        GameRegistry.registerBlock(Blocks.kismetDisplayBlock, Names.DISPLAY);
    }

    public static void initItems() {
        GameRegistry.registerItem(Items.itemKey, Names.KEY);
    }


}
