package desutine.kismet.proxy;

import desutine.kismet.Kismet;
import desutine.kismet.block.BlockKismet;
import desutine.kismet.item.ItemKismet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public class ServerProxy extends CommonProxy {
    @Override
    public void initConfig() {
        super.initConfig();
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
        // send the config to the client
        Kismet.network.sendConfigToClient((EntityPlayerMP) player);
    }

    @Override
    public void cleanTargetLibrary(EntityPlayerMP player) {
        // NOOP
    }

    @Override
    public boolean inferSafeHasSubtypes(ItemStack stack) {
        // because the server fails to properly quantify for some subtypes, if they exist
        // we return true by default and let the client-side fix it afterwards
        return true;
    }

    @Override
    public void registerInventoryModel(BlockKismet block, String name) {
        // NOOP
    }

    @Override
    public void registerInventoryModel(ItemKismet item, String name) {
        // NOOP
    }
}
