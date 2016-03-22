package com.desutine.kismet.item;

import com.desutine.kismet.CreativeTabKismet;
import com.desutine.kismet.reference.Reference;
import net.minecraft.item.Item;

public class ModItem extends Item {
    public ModItem() {
        super();
//        this.setMaxStackSize(64);
//        this.setCreativeTab();

        setCreativeTab(CreativeTabKismet.KISMET_TAB);
    }
}
