package desutine.kismet.common.item;

import desutine.kismet.Reference;
import desutine.kismet.client.CreativeTabKismet;
import net.minecraft.item.Item;

public class ItemKismet extends Item {
    public ItemKismet(String name) {
        super();

        setRegistryName(Reference.MOD_ID.toLowerCase(), name);
        setUnlocalizedName(name);
        // this.setMaxStackSize(64);

        setCreativeTab(CreativeTabKismet.KISMET_TAB);
    }
}
