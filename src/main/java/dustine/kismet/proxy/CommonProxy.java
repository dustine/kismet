package dustine.kismet.proxy;

import dustine.kismet.block.BlockKismet;
import dustine.kismet.config.ConfigKismet;
import dustine.kismet.config.ConfigKismetOverride;
import dustine.kismet.item.ItemKismet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public abstract class CommonProxy implements IProxy {
    @Override
    public void initConfig() {
        ConfigKismet.init();
        ConfigKismetOverride.init();
    }

    @Override
    public void registerTESR() {
    }

    @Override
    public void registerColorHandlers() {
    }

    @Override
    public void sendConfigToClient(EntityPlayerMP player) {
    }

    @Override
    public void cleanTargetLibrary(EntityPlayerMP player) {
    }

    @Override
    public boolean sideSafeHasSubtypes(ItemStack stack) {
        return stack.getHasSubtypes();
    }

    @Override
    public void registerInventoryModel(BlockKismet block) {
    }

    @Override
    public void registerInventoryModel(ItemKismet item) {
    }

    @Override
    public EntityPlayer tryGetEntityPlayerSP() {
        return null;
    }
}
