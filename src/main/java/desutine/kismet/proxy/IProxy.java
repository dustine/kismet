package desutine.kismet.proxy;

import desutine.kismet.block.BlockKismet;
import desutine.kismet.item.ItemKismet;
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

    void registerInventoryModel(BlockKismet block, String name);

    void registerInventoryModel(ItemKismet item, String name);
}
