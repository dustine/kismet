package desutine.kismet.client;

import desutine.kismet.common.init.ModBlocks;
import desutine.kismet.reference.Reference;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class CreativeTabKismet {
    public static final CreativeTabs KISMET_TAB = new CreativeTabs(Reference.MODID.toLowerCase()) {
        @Override
        public Item getTabIconItem() {
            return Item.getItemFromBlock(ModBlocks.kismetDisplayBlock);
        }
    };
}
