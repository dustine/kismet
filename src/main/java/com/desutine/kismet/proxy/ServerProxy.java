package com.desutine.kismet.proxy;

import com.desutine.kismet.init.Items;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ServerProxy extends CommonProxy {
    /**
     * Run before anything else. Read your config, create blocks, items, etc, and register them with the GameRegistry
     *
     * @param event
     */
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
    }

    /**
     * Do your mod setup. Build whatever data structures you care about. Register recipes,
     * send FMLInterModComms messages to other mods.
     *
     * @param event
     */
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }

    /**
     * Handle interaction with other mods, complete your setup based on this.
     */
    public void postInit() {
        super.postInit();
    }
}
