package desutine.kismet.proxy;

import desutine.kismet.ConfigKismet;

public abstract class CommonProxy implements IProxy {
    @Override
    public void initConfig() {
        ConfigKismet.preInit();
    }
}
