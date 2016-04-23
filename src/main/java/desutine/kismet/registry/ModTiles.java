package desutine.kismet.registry;

import desutine.kismet.Reference;
import desutine.kismet.tile.TileDisplay;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModTiles {
    public static void init() {
        GameRegistry.registerTileEntity(TileDisplay.class, Reference.MOD_ID + ':' + Reference.Names.Tiles.TE_DISPLAY);
    }
}
