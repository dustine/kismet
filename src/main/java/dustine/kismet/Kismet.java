package dustine.kismet;

import dustine.kismet.network.NetworkHandler;
import dustine.kismet.proxy.IProxy;
import dustine.kismet.registry.ModBlocks;
import dustine.kismet.registry.ModItems;
import dustine.kismet.registry.ModRecipes;
import dustine.kismet.registry.ModTiles;
import dustine.kismet.server.CommandKismet;
import dustine.kismet.server.event.EventRegenLibraryOnce;
import dustine.kismet.target.TargetDatabaseBuilder;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;

import java.util.Random;

@Mod(modid = Reference.MOD_ID, name = Reference.Names.MOD, version = Reference.VERSION, guiFactory = Reference.GUI_FACTORY_CLASS)
public class Kismet {
    public static final Random random = new Random();
    public final static CommandKismet command = new CommandKismet();
    public static NetworkHandler network;
    public static TargetDatabaseBuilder libraryFactory;
    @Mod.Instance(Reference.MOD_ID)
    public static Kismet instance;
    @SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
    public static IProxy proxy;
    private boolean jeiLoaded;
    private EventRegenLibraryOnce eventRegenLibraryOnce;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // register logger
        Log.logger = event.getModLog();

        // load configs
        proxy.initConfig();

        // register blocks, items, tile entities
        ModBlocks.init();
        ModItems.init();
        ModTiles.init();
        proxy.registerTESR();

        // register event handlers


        // start network channels
        network = new NetworkHandler();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        // register recipes
        ModRecipes.init();

        this.jeiLoaded = Loader.isModLoaded("JEI");

        // register tints
        proxy.registerColorHandlers();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        // register commands
        event.registerServerCommand(command);

        libraryFactory = new TargetDatabaseBuilder((WorldServer) event.getServer().getEntityWorld());

        // register the hook to restart the targetLibrary on single-player
        this.eventRegenLibraryOnce = new EventRegenLibraryOnce();
        MinecraftForge.EVENT_BUS.register(this.eventRegenLibraryOnce);
    }

    @EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        // unregister the thing if it wasn't unfulfilled yet
        MinecraftForge.EVENT_BUS.unregister(Kismet.instance.eventRegenLibraryOnce);
    }

    public boolean isJeiLoaded() {
        return this.jeiLoaded;
    }
}
