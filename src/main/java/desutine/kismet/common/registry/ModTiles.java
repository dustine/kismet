package desutine.kismet.common.registry;

import desutine.kismet.Reference;
import desutine.kismet.common.tile.TileDisplay;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModTiles {
    public static void init() {
        GameRegistry.registerTileEntity(TileDisplay.class, Reference.MOD_ID + ':' + Reference.Names.Tiles.TE_DISPLAY);
    }
}
