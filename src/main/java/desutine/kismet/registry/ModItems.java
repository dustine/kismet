package desutine.kismet.registry;

import desutine.kismet.Kismet;
import desutine.kismet.Reference;
import desutine.kismet.item.ItemKey;
import desutine.kismet.item.ItemKismet;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModItems {
    public static final ItemKismet ITEM_KEY = new ItemKey();

    public static void init() {
        registerItem(ITEM_KEY, Reference.Names.Items.KEY);
    }

    private static void registerItem(ItemKismet item, String name) {
        if (item.getRegistryName() == null)
            item.setRegistryName(name);

        GameRegistry.register(item);
        item.setUnlocalizedName(name);
        Kismet.proxy.registerInventoryModel(item);
    }
}
