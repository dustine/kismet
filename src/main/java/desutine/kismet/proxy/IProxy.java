package desutine.kismet.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public interface IProxy {
    void addInventoryModels();

    void initConfig();

    void registerTESR();

    void registerColorHandlers();

    void sendConfigToClient(EntityPlayer player);

    void cleanTargetLibrary(EntityPlayerMP player);

    boolean inferSafeHasSubtypes(ItemStack stack);
}
