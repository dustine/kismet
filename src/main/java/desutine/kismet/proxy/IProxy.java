package desutine.kismet.proxy;

import net.minecraft.entity.player.EntityPlayer;

public interface IProxy {
    void addInventoryModels();

    void initConfig();

    void registerTESR();

    void registerBlockItemColor();

    void sendConfigToClient(EntityPlayer player);
}
