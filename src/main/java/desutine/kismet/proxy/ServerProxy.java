package desutine.kismet.proxy;

import desutine.kismet.Kismet;
import desutine.kismet.common.KismetConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class ServerProxy extends CommonProxy {
    @Override
    public void addInventoryModels() {
        // NOOP
    }

    @Override
    public void initConfig() {
        KismetConfig.preInit();
    }

    @Override
    public void registerTESR() {
        // NOOP
    }

    @Override
    public void registerBlockItemColor() {
        // NOOP
    }

    @Override
    public void sendConfigToClient(EntityPlayer player) {
        Kismet.packetHandler.sendConfigToClient((EntityPlayerMP) player);
    }
}
