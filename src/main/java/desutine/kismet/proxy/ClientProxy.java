package desutine.kismet.proxy;

import desutine.kismet.ConfigKismet;
import desutine.kismet.Kismet;
import desutine.kismet.block.BlockKismet;
import desutine.kismet.client.BlockColorDisplay;
import desutine.kismet.client.render.RenderTileDisplay;
import desutine.kismet.item.ItemKismet;
import desutine.kismet.registry.ModBlocks;
import desutine.kismet.tile.TileDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientProxy extends CommonProxy {

    @Override
    public void initConfig() {
        super.initConfig();
        ConfigKismet.clientPreInit();
    }

    @Override
    public void registerTESR() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileDisplay.class, new RenderTileDisplay());
    }

    @Override
    public void registerColorHandlers() {
        BlockColors blockColors = Minecraft.getMinecraft().getBlockColors();
        blockColors.registerBlockColorHandler(new BlockColorDisplay(), ModBlocks.TIMED_DISPLAY);
        blockColors.registerBlockColorHandler(new BlockColorDisplay(), ModBlocks.CHILL_DISPLAY);

        ItemColors itemColors = Minecraft.getMinecraft().getItemColors();
        itemColors.registerItemColorHandler(new BlockColorDisplay(), ModBlocks.CHILL_DISPLAY);
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

    @Override
    public void registerInventoryModel(BlockKismet block, String name) {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0,
                new ModelResourceLocation(block.getRegistryName(), "inventory"));
    }

    @Override
    public void registerInventoryModel(ItemKismet item, String name) {
        ModelLoader.setCustomModelResourceLocation(item, 0,
                new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
}
