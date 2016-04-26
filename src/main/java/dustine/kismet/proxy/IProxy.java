package dustine.kismet.proxy;

import dustine.kismet.block.BlockKismet;
import dustine.kismet.item.ItemKismet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public interface IProxy {

    void initConfig();

    void registerTESR();

    void registerColorHandlers();

    void sendConfigToClient(EntityPlayer player);

    void cleanTargetLibrary(EntityPlayerMP player);

    boolean inferSafeHasSubtypes(ItemStack stack);

    void registerInventoryModel(BlockKismet block);

    void registerInventoryModel(ItemKismet item);
}
