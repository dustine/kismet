package desutine.kismet.common.init;

import desutine.kismet.common.item.ItemBlockDisplay;
import desutine.kismet.common.item.ItemKey;
import desutine.kismet.common.item.ItemKismet;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModItems {
    public static final ItemKismet itemKey = new ItemKey();
    public static final ItemBlockDisplay itemBlockDisplay = new ItemBlockDisplay();

    public static void init() {
        GameRegistry.register(itemKey);

        GameRegistry.register(itemBlockDisplay);
    }
}
