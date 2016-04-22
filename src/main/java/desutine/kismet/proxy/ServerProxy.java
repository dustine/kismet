package desutine.kismet.proxy;

import desutine.kismet.ConfigKismet;
import desutine.kismet.Kismet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

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
    public void registerColorHandlers() {
        // NOOP
    }

    @Override
    public void sendConfigToClient(EntityPlayer player) {
        Kismet.network.sendConfigToClient((EntityPlayerMP) player);
    }

    @Override
    public void cleanTargetLibrary(EntityPlayerMP player) {
        // NOOP
    }

    @Override
    public boolean inferSafeHasSubtypes(ItemStack stack) {
        return true;
    }
}
