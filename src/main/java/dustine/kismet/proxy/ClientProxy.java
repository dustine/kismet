package dustine.kismet.proxy;

import dustine.kismet.ConfigKismet;
import dustine.kismet.Kismet;
import dustine.kismet.block.BlockKismet;
import dustine.kismet.client.ColorDisplay;
import dustine.kismet.client.render.RenderTileDisplay;
import dustine.kismet.item.ItemKismet;
import dustine.kismet.registry.ModBlocks;
import dustine.kismet.tile.TileDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
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
        blockColors.registerBlockColorHandler(new ColorDisplay(), ModBlocks.TIMED_DISPLAY);
        blockColors.registerBlockColorHandler(new ColorDisplay(), ModBlocks.CHILL_DISPLAY);

        ItemColors itemColors = Minecraft.getMinecraft().getItemColors();
        itemColors.registerItemColorHandler(new ColorDisplay(), ModBlocks.CHILL_DISPLAY);
    }

    @Override
    public void cleanTargetLibrary(EntityPlayerMP player) {
        // yeah, server code on ClientProxy
        // that's because client proxy + isRemote = embedded server thread
        if (player.worldObj.isRemote) return;

        Kismet.databaseBuilder.generateStacks(player);
    }

    @Override
    public void registerInventoryModel(BlockKismet block) {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0,
                new ModelResourceLocation(block.getRegistryName(), "inventory"));
    }

    @Override
    public void registerInventoryModel(ItemKismet item) {
        ModelLoader.setCustomModelResourceLocation(item, 0,
                new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }

    @Override
    public EntityPlayer tryGetEntityPlayerSP() {
        return Minecraft.getMinecraft().thePlayer;
    }
}
