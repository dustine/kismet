package desutine.kismet.proxy;

import desutine.kismet.ModConfig;
import desutine.kismet.init.ClientInit;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {
  /**
   * Run before anything else. Read your config, create blocks, items, etc, and register them with the GameRegistry
   */
  public void preInit(FMLPreInitializationEvent event) {
    super.preInit(event);
    ModConfig.clientPreInit();

    ClientInit.renderInInventory();
    ClientInit.initTileEntityRenderers();
  }

  /**
   * Do your mod setup. Build whatever data structures you care about. Register recipes,
   * send FMLInterModComms messages to other mods.
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
