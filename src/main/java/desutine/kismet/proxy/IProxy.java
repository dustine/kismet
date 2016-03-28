package desutine.kismet.proxy;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public interface IProxy {
    void addInventoryModels();

    void initConfig();

    void registerTESR();
}
