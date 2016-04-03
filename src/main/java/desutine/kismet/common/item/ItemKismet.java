package desutine.kismet.common.item;

import desutine.kismet.client.CreativeTabKismet;
import desutine.kismet.reference.Reference;
import net.minecraft.item.Item;

public class ItemKismet extends Item {
    public ItemKismet(String name) {
        super();

        setRegistryName(Reference.MODID, name);
        setUnlocalizedName(name);
        // this.setMaxStackSize(64);

        setCreativeTab(CreativeTabKismet.KISMET_TAB);
    }
}
