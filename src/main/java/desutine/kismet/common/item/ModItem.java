package desutine.kismet.common.item;

import desutine.kismet.client.KismetCreativeTab;
import net.minecraft.item.Item;

public class ModItem extends Item {
    public ModItem() {
        super();
//        this.setMaxStackSize(64);
//        this.setCreativeTab();

        setCreativeTab(KismetCreativeTab.KISMET_TAB);
    }
}
