package desutine.kismet.common.init;

import desutine.kismet.common.item.ItemKey;
import desutine.kismet.common.item.ItemKismet;
import desutine.kismet.reference.Names;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModItems {
    public static final ItemKismet itemKey = new ItemKey();

    public static void init() {
        GameRegistry.registerItem(itemKey, Names.Items.KEY);
    }
}
