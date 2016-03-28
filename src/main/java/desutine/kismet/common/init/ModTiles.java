package desutine.kismet.common.init;

import desutine.kismet.common.tile.TileDisplay;
import desutine.kismet.reference.Names;
import desutine.kismet.reference.Reference;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModTiles {
    public static void init() {
        GameRegistry.registerTileEntity(TileDisplay.class, Reference.MODID + ':' + Names.Tiles.TE_DISPLAY);
    }
}
