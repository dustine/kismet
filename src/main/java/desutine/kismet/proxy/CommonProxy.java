package desutine.kismet.proxy;

import desutine.kismet.common.ModConfig;
import desutine.kismet.common.Init;
import desutine.kismet.Kismet;
import desutine.kismet.ModLogger;
import desutine.kismet.network.ModPacketHandler;
import desutine.kismet.common.event.BlockEventHandler;
import desutine.kismet.common.event.EventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameData;

public abstract class CommonProxy implements IProxy {
    /**
     * Run before anything else. Read your config, create blocks, items, etc, and register them with the GameRegistry
     */
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        // load configs
        ModConfig.preInit();

        // register eventhandlers
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        MinecraftForge.EVENT_BUS.register(new BlockEventHandler());

        // register blocks, items, te
        Init.initBlocks();
        Init.initItems();
        Init.initTileEntities();
    }

    /**
     * Do your mod setup. Build whatever data structures you care about. Register recipes,
     * send FMLInterModComms messages to other mods.
     */
    @Override
    public void init(FMLInitializationEvent event) {
        // readying network stuff
        Kismet.packetHandler = new ModPacketHandler();

        // debug logs
        ModLogger.info(GameData.getItemRegistry().getRandomObject(Kismet.random).getRegistryName());
        ModLogger.info(GameData.getItemRegistry().getKeys().size() + "items?");
    }

    /**
     * Handle interaction with other mods, complete your setup based on this.
     */
    @Override
    public void postInit() {
    }
}
