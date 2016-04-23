package desutine.kismet;

import desutine.kismet.network.NetworkHandlerKismet;
import desutine.kismet.proxy.IProxy;
import desutine.kismet.registry.ModBlocks;
import desutine.kismet.registry.ModItems;
import desutine.kismet.registry.ModRecipes;
import desutine.kismet.registry.ModTiles;
import desutine.kismet.server.CommandKismet;
import desutine.kismet.server.event.EventRegenLibraryOnce;
import desutine.kismet.target.TargetDatabaseBuilder;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

import java.util.Random;

@Mod(modid = Reference.MOD_ID, name = Reference.Names.MOD, version = Reference.VERSION, guiFactory = Reference.GUI_FACTORY_CLASS)
public class Kismet {
    public static final Random random = new Random();
    public final static CommandKismet command = new CommandKismet();
    public static NetworkHandlerKismet network;
    public static TargetDatabaseBuilder libraryFactory;
    @Mod.Instance(Reference.MOD_ID)
    public static Kismet instance;
    @SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
    public static IProxy proxy;
    private boolean jeiLoaded;
    private EventRegenLibraryOnce eventRegenLibraryOnce;

    /**
     * Run before anything else. Read your config, create blocks, items, etc, and register them with the GameRegistry
     */
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // register logger
        ModLogger.logger = event.getModLog();

        // load configs
        proxy.initConfig();

        // register blocks, items, tile entities
        ModBlocks.init();
        ModItems.init();
        ModTiles.init();
        proxy.registerTESR();

        // register eventhandlers
        MinecraftForge.EVENT_BUS.register(new desutine.kismet.event.EventHandler());

        // start network channels
        network = new NetworkHandlerKismet();
    }

    /**
     * Do your mod setup. Build whatever data structures you care about. Register recipes,
     * send FMLInterModComms messages to other mods.
     */
    @EventHandler
    public void init(FMLInitializationEvent event) {
        // register recipes
        ModRecipes.init();

        jeiLoaded = Loader.isModLoaded("JEI");

        // register tints
        proxy.registerColorHandlers();
    }

    /**
     * Handle interaction with other mods, complete your setup based on this.
     */
//    @EventHandler
//    public void postInit(FMLPostInitializationEvent event) {
//    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        // register commands
        event.registerServerCommand(command);

        libraryFactory = new TargetDatabaseBuilder((WorldServer) event.getServer().getEntityWorld());

        // register the hook to restart the targetLibrary on single-player
        eventRegenLibraryOnce = new EventRegenLibraryOnce();
        MinecraftForge.EVENT_BUS.register(eventRegenLibraryOnce);
    }

    @EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        // unregister the thing if it wasn't unfulfilled yet
        MinecraftForge.EVENT_BUS.unregister(Kismet.instance.eventRegenLibraryOnce);
    }

    public boolean isJeiLoaded() {
        return jeiLoaded;
    }
}
