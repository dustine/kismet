package desutine.kismet.init;

import desutine.kismet.reference.Blocks;
import desutine.kismet.reference.Items;
import desutine.kismet.reference.Names;
import desutine.kismet.reference.Reference;
import desutine.kismet.tileentity.DisplayTileEntity;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class Init {
  public static void initTileEntities() {
    GameRegistry.registerTileEntity(DisplayTileEntity.class, Reference.MODID + ':' + Names.TT_DISPLAY);
  }

  public static void initBlocks() {
    GameRegistry.registerBlock(Blocks.kismetDisplayBlock, Names.DISPLAY);
  }

  public static void initItems() {
    GameRegistry.registerItem(Items.itemKey, Names.KEY);
  }


}
