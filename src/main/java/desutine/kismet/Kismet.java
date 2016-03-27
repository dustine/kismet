package desutine.kismet;

import desutine.kismet.network.PacketHandlerKismet;
import desutine.kismet.proxy.IProxy;
import desutine.kismet.reference.Reference;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.Random;

@Mod(modid = Reference.MODID, version = Reference.VERSION, guiFactory = Reference.GUI_FACTORY_CLASS)
public class Kismet {
    public static final Random random = new Random();
    public static PacketHandlerKismet packetHandler;
    @Mod.Instance(Reference.MODID)
    public static Kismet instance;
    @SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
    public static IProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // get named logger
        Logger.logger = (event.getModLog());

        proxy.preInit(event);
        Logger.info("PreInit done, registered X items, X blocks");
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        // some example code
        System.out.println("DIRT BLOCK >> " + Blocks.dirt.getUnlocalizedName());

        proxy.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit();
    }
}
