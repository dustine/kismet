package desutine.kismet.reference;

import desutine.kismet.common.block.BlockDisplay;
import desutine.kismet.common.block.BlockKismet;
import net.minecraftforge.fml.common.registry.GameRegistry;

@GameRegistry.ObjectHolder(Reference.MODID)
public class Blocks {
    public static final BlockKismet kismetDisplayBlock = new BlockDisplay();

}
