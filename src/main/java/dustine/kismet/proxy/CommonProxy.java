package dustine.kismet.proxy;

import dustine.kismet.ConfigKismet;

public abstract class CommonProxy implements IProxy {
    @Override
    public void initConfig() {
        ConfigKismet.preInit();
    }
}
