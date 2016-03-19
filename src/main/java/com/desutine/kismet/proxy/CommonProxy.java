package com.desutine.kismet.proxy;

import com.desutine.kismet.block.ModBlocks;
import com.desutine.kismet.item.ModItems;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public abstract class CommonProxy implements IProxy {
    /**
     * Run before anything else. Read your config, create blocks, items, etc, and register them with the GameRegistry
     * @param event
     */
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        ModItems.init();
        ModBlocks.init();
    }

    /**
     * Do your mod setup. Build whatever data structures you care about. Register recipes,
     * send FMLInterModComms messages to other mods.
     * @param event
     */
    @Override
    public void init(FMLInitializationEvent event) {
    }

    /**
     * Handle interaction with other mods, complete your setup based on this.
     */
    @Override
    public void postInit() {
    }
}
