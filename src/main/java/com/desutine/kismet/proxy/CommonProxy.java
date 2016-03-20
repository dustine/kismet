package com.desutine.kismet.proxy;

import com.desutine.kismet.Kismet;
import com.desutine.kismet.ModLogger;
import com.desutine.kismet.event.BlockEventHandler;
import com.desutine.kismet.event.EventHandler;
import com.desutine.kismet.init.Blocks;
import com.desutine.kismet.init.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameData;

public abstract class CommonProxy implements IProxy {
    /**
     * Run before anything else. Read your config, create blocks, items, etc, and register them with the GameRegistry
     *
     * @param event
     */
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        // register eventhandlers
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        MinecraftForge.EVENT_BUS.register(new BlockEventHandler());

        Blocks.init();
        Items.init();
    }

    /**
     * Do your mod setup. Build whatever data structures you care about. Register recipes,
     * send FMLInterModComms messages to other mods.
     *
     * @param event
     */
    @Override
    public void init(FMLInitializationEvent event) {
        ModLogger.info(GameData.getItemRegistry().getRandomObject(Kismet.random).getRegistryName());
    }

    /**
     * Handle interaction with other mods, complete your setup based on this.
     */
    @Override
    public void postInit() {
    }
}
