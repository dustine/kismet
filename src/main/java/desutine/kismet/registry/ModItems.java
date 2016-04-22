package desutine.kismet.registry;

import desutine.kismet.item.ItemBlockKismet;
import desutine.kismet.item.ItemKey;
import desutine.kismet.item.ItemKismet;
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
