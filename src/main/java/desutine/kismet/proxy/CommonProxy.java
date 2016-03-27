package desutine.kismet.proxy;

import desutine.kismet.common.ConfigKismet;
import desutine.kismet.common.CommonInit;
import desutine.kismet.Kismet;
import desutine.kismet.Logger;
import desutine.kismet.network.PacketHandlerKismet;
import desutine.kismet.common.event.EventHandlerBlock;
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
        ConfigKismet.preInit();

        // register eventhandlers
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        MinecraftForge.EVENT_BUS.register(new EventHandlerBlock());

        // register blocks, items, te
        CommonInit.initBlocks();
        CommonInit.initItems();
        CommonInit.initTileEntities();
    }

    /**
     * Do your mod setup. Build whatever data structures you care about. Register recipes,
     * send FMLInterModComms messages to other mods.
     */
    @Override
    public void init(FMLInitializationEvent event) {
        // readying network stuff
        Kismet.packetHandler = new PacketHandlerKismet();

        // debug logs
        Logger.info(GameData.getItemRegistry().getRandomObject(Kismet.random).getRegistryName());
        Logger.info(GameData.getItemRegistry().getKeys().size() + "items?");
    }

    /**
     * Handle interaction with other mods, complete your setup based on this.
     */
    @Override
    public void postInit() {
    }
}
