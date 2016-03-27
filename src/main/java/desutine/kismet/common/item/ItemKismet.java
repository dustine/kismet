package desutine.kismet.common.item;

import desutine.kismet.client.CreativeTabKismet;
import net.minecraft.item.Item;

public class ItemKismet extends Item {
    public ItemKismet() {
        super();
//        this.setMaxStackSize(64);
//        this.setCreativeTab();

        setCreativeTab(CreativeTabKismet.KISMET_TAB);
    }
}
