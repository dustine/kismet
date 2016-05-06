package dustine.kismet;

import dustine.kismet.gui.ModGuiHandler;
import dustine.kismet.network.NetworkHandler;
import dustine.kismet.proxy.IProxy;
import dustine.kismet.registry.ModBlocks;
import dustine.kismet.registry.ModItems;
import dustine.kismet.registry.ModRecipes;
import dustine.kismet.registry.ModTiles;
import dustine.kismet.server.command.CommandKismet;
import dustine.kismet.server.event.EventOnceFixDatabase;
import dustine.kismet.target.TargetLibrary;
import dustine.kismet.target.TargetPatcher;
import dustine.kismet.world.savedata.TargetDatabaseBuilder;
import dustine.kismet.world.savedata.WSDTargetDatabase;
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
    public static final Random RANDOM = new Random();
    public static NetworkHandler network;
    public static TargetDatabaseBuilder databaseBuilder;

    @Mod.Instance(Reference.MOD_ID)
    public static Kismet instance;

    @SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
    public static IProxy proxy;

    private boolean jeiLoaded;

    private EventOnceFixDatabase eventOnceFixDatabase;

    @EventHandler
    public void preInit(final FMLPreInitializationEvent event) {
        // register logger
        Log.logger = event.getModLog();

        // start network channels
        network = new NetworkHandler(Reference.MOD_ID);

        // load configs
        proxy.initConfig();

        // register blocks, items, tile entities
        ModBlocks.init();
        ModTiles.init();
        ModItems.init();
        proxy.registerTESR();

        // register gui handler
        ModGuiHandler.register();

        // register event handlers
    }

    @EventHandler
    public void init(final FMLInitializationEvent event) {
        // register recipes
        ModRecipes.init();
        TargetPatcher.init();

        this.jeiLoaded = Loader.isModLoaded("JEI");

        // register tints
        proxy.registerColorHandlers();
    }

    @EventHandler
    public void postInit(final FMLPostInitializationEvent event) {}

    @EventHandler
    public void serverStarting(final FMLServerStartingEvent event) {
        // register commands
        event.registerServerCommand(new CommandKismet());

        final WorldServer world = (WorldServer) event.getServer().getEntityWorld();
        databaseBuilder = new TargetDatabaseBuilder(world);
        final WSDTargetDatabase targetDatabase = WSDTargetDatabase.get(world);
        TargetLibrary.build(targetDatabase);

        // register the hook to restart the targetLibrary on single-player
        this.eventOnceFixDatabase = new EventOnceFixDatabase();
        MinecraftForge.EVENT_BUS.register(this.eventOnceFixDatabase);
    }

    @EventHandler
    public void serverStopping(final FMLServerStoppingEvent event) {
        // unregister the thing if it wasn't unfulfilled yet
        MinecraftForge.EVENT_BUS.unregister(Kismet.instance.eventOnceFixDatabase);
    }

    public boolean isJeiLoaded() {
        return this.jeiLoaded;
    }
}
