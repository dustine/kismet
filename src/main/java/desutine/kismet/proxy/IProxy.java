package desutine.kismet.proxy;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public interface IProxy {
  void preInit(FMLPreInitializationEvent event);

  void init(FMLInitializationEvent event);

  void postInit();
}
