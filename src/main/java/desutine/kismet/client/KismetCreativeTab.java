package desutine.kismet.client;

import desutine.kismet.reference.Blocks;
import desutine.kismet.reference.Reference;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class KismetCreativeTab {
    public static final CreativeTabs KISMET_TAB = new CreativeTabs(Reference.MODID.toLowerCase()) {
        @Override
        public Item getTabIconItem() {
            return Item.getItemFromBlock(Blocks.kismetDisplayBlock);
        }
    };
}
