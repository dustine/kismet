package com.desutine.kismet.item;

import com.desutine.kismet.item.ItemKismet;
import com.desutine.kismet.item.ItemKey;
import com.desutine.kismet.reference.Items;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModItems {
    public static final ItemKismet kismetKeyItem = new ItemKey();

    public static void init() {
        GameRegistry.registerItem(kismetKeyItem, Items.KEY_NAME);
    }
}
