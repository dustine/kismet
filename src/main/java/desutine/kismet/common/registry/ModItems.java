package desutine.kismet.common.registry;

import desutine.kismet.common.item.ItemBlockKismet;
import desutine.kismet.common.item.ItemKey;
import desutine.kismet.common.item.ItemKismet;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModItems {
    public static final ItemKismet itemKey = new ItemKey();
    public static final ItemBlockKismet itemBlockTimedDisplay = new ItemBlockKismet(ModBlocks.TIMED_DISPLAY);
    public static final ItemBlockKismet itemBlockChillDisplay = new ItemBlockKismet(ModBlocks.CHILL_DISPLAY);

    public static void init() {
        GameRegistry.register(itemKey);

        GameRegistry.register(itemBlockTimedDisplay);
        GameRegistry.register(itemBlockChillDisplay);
    }
}
