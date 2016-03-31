package desutine.kismet.proxy;

import desutine.kismet.common.config.ConfigKismet;

public class ServerProxy extends CommonProxy {
    @Override
    public void addInventoryModels() {
        // NOOP
    }

    @Override
    public void initConfig() {
        ConfigKismet.preInit();
    }

    @Override
    public void registerTESR() {
        // NOOP
    }

    @Override
    public void registerBlockItemColor() {
        // NOOP
    }

}
