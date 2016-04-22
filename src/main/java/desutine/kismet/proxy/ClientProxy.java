package desutine.kismet.proxy;

import desutine.kismet.ConfigKismet;
import desutine.kismet.Kismet;
import desutine.kismet.client.BlockColorDisplay;
import desutine.kismet.client.render.RenderTileDisplay;
import desutine.kismet.registry.ModBlocks;
import desutine.kismet.registry.ModItems;
import desutine.kismet.tile.TileDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientProxy extends CommonProxy {
    @Override
    public void addInventoryModels() {
        ModelLoader.setCustomModelResourceLocation(ModItems.itemKey, 0,
                new ModelResourceLocation(ModItems.itemKey.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(ModItems.itemBlockChillDisplay, 0,
                new ModelResourceLocation(ModItems.itemBlockChillDisplay.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(ModItems.itemBlockTimedDisplay, 0,
                new ModelResourceLocation(ModItems.itemBlockTimedDisplay.getRegistryName(), "inventory"));
    }

    @Override
    public void initConfig() {
        ConfigKismet.preInit();
        ConfigKismet.clientPreInit();
    }

    @Override
    public void registerTESR() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileDisplay.class, new RenderTileDisplay());
    }

    @Override
    public void registerColorHandlers() {
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(new BlockColorDisplay(), ModBlocks.TIMED_DISPLAY);
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(new BlockColorDisplay(), ModBlocks.CHILL_DISPLAY);

        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new BlockColorDisplay(), ModBlocks.CHILL_DISPLAY);
    }

    @Override
    public void sendConfigToClient(EntityPlayer player) {
        // NOOP
    }

    @Override
    public void cleanTargetLibrary(EntityPlayerMP player) {
        // yeah, server code on ClientProxy
        // that's because rclient proxy + isRemote = embedded server thread
        if (player.worldObj.isRemote) return;

        Kismet.libraryFactory.generateStacks(player);
    }

    @Override
    public boolean inferSafeHasSubtypes(ItemStack stack) {
        return stack.getHasSubtypes();
    }
}
