package desutine.kismet.client;

import desutine.kismet.Reference;
import desutine.kismet.common.registry.ModBlocks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class CreativeTabKismet {
    public static final CreativeTabs KISMET_TAB = new CreativeTabs(Reference.MOD_ID.toLowerCase()) {
        @Override
        public Item getTabIconItem() {
            return Item.getItemFromBlock(ModBlocks.CHILL_DISPLAY);
        }
    };
}
