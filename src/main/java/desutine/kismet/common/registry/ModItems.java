package desutine.kismet.common.registry;

import desutine.kismet.common.item.ItemBlockKismet;
import desutine.kismet.common.item.ItemKey;
import desutine.kismet.common.item.ItemKismet;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModItems {
    public static final ItemKismet itemKey = new ItemKey();
    public static final ItemBlockKismet itemBlockDisplay = new ItemBlockKismet(ModBlocks.DISPLAY);

    public static void init() {
        GameRegistry.register(itemKey);

        GameRegistry.register(itemBlockDisplay);
    }
}
