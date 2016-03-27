package desutine.kismet.item;

import desutine.kismet.KismetCreativeTab;
import net.minecraft.item.Item;

public class ModItem extends Item {
  public ModItem() {
    super();
//        this.setMaxStackSize(64);
//        this.setCreativeTab();

    setCreativeTab(KismetCreativeTab.KISMET_TAB);
  }
}
