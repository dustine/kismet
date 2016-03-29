package desutine.kismet;

import desutine.kismet.common.config.BlockListHelper;
import desutine.kismet.common.init.ModBlocks;
import desutine.kismet.common.init.ModItems;
import desutine.kismet.common.init.ModRecipes;
import desutine.kismet.common.init.ModTiles;
import desutine.kismet.network.KismetPacketHandler;
import desutine.kismet.proxy.IProxy;
import desutine.kismet.reference.Reference;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameData;

import java.util.Random;

@Mod(modid = Reference.MODID, version = Reference.VERSION, guiFactory = Reference.GUI_FACTORY_CLASS)
public class Kismet {
    public static final Random random = new Random();
    public static KismetPacketHandler packetHandler;
    @Mod.Instance(Reference.MODID)
    public static Kismet instance;
    @SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
    public static IProxy proxy;

    /**
     * Run before anything else. Read your config, create blocks, items, etc, and register them with the GameRegistry
     */
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // register logger
        ModLogger.logger = (event.getModLog());

        // load configs
        proxy.initConfig();

        // register blocks, items, tile entities
        ModBlocks.init();
        ModItems.init();
        proxy.addInventoryModels();
        ModTiles.init();
        proxy.registerTESR();

        // register eventhandlers
        MinecraftForge.EVENT_BUS.register(new desutine.kismet.common.event.EventHandler());
//        MinecraftForge.EVENT_BUS.register(new EventHandlerBlock());

        // start network channels
        packetHandler = new KismetPacketHandler();
    }

    /**
     * Do your mod setup. Build whatever data structures you care about. Register recipes,
     * send FMLInterModComms messages to other mods.
     */
    @EventHandler
    public void init(FMLInitializationEvent event) {
        // register recipes
        ModRecipes.init();

        // register tints
        proxy.registerBlockItemColor();

        // debug logs
        ModLogger.info(GameData.getItemRegistry().getRandomObject(Kismet.random).getRegistryName());
        ModLogger.info(GameData.getItemRegistry().getKeys().size() + "items?");
    }

    /**
     * Handle interaction with other mods, complete your setup based on this.
     */
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        // finish generating item tree
        BlockListHelper.generateInternalList();
    }
}
