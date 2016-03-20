package com.desutine.kismet;

import com.desutine.kismet.init.Blocks;
import com.desutine.kismet.reference.Reference;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class CreativeTabKismet {
    public static final CreativeTabs KISMET_TAB = new CreativeTabs(Reference.MODID) {
        @Override
        public Item getTabIconItem() {
            return Item.getItemFromBlock(Blocks.kismetDisplayBlock);
        }

        @Override
        public String getTranslatedTabLabel() {
            return Reference.MOD_NAME;
        }
    };
}
