package desutine.kismet.block;

import desutine.kismet.KismetCreativeTab;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class ModBlock extends Block {
  public ModBlock() {
    this(Material.rock);
  }

  public ModBlock(Material material) {
    super(material);
    setCreativeTab(KismetCreativeTab.KISMET_TAB);
  }
}