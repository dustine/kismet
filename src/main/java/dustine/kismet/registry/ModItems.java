package dustine.kismet.registry;

import dustine.kismet.Kismet;
import dustine.kismet.Reference;
import dustine.kismet.item.ItemKey;
import dustine.kismet.item.ItemKismet;
import net.minecraftforge.fml.common.registry.GameRegistry;

@GameRegistry.ObjectHolder(Reference.MOD_ID)
public class ModItems {
    public static final ItemKismet KEY = new ItemKey();

    public static void init() {
        registerItem(KEY, Reference.Names.Items.KEY);
    }

    private static void registerItem(ItemKismet item, String name) {
        if (item.getRegistryName() == null)
            item.setRegistryName(name);

        GameRegistry.register(item);
        item.setUnlocalizedName(name);
        Kismet.proxy.registerInventoryModel(item);
    }
}
