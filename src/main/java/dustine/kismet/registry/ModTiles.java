package dustine.kismet.registry;

import dustine.kismet.Reference;
import dustine.kismet.tile.TileDisplay;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModTiles {
    public static void init() {
        GameRegistry.registerTileEntity(TileDisplay.class, Reference.MOD_ID + ':' + Reference.Names.Tiles.TILE_DISPLAY);
    }
}
