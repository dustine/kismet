package desutine.kismet.reference;

import desutine.kismet.block.DisplayBlock;
import desutine.kismet.block.ModBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;

@GameRegistry.ObjectHolder(Reference.MODID)
public class Blocks {
  public static final ModBlock kismetDisplayBlock = new DisplayBlock();

}
