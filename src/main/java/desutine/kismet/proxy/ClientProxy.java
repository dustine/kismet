package desutine.kismet.proxy;

import desutine.kismet.Reference;
import desutine.kismet.client.BlockColorDisplay;
import desutine.kismet.client.render.RenderTileDisplay;
import desutine.kismet.common.KismetConfig;
import desutine.kismet.common.registry.ModBlocks;
import desutine.kismet.common.registry.ModItems;
import desutine.kismet.common.tile.TileDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientProxy extends CommonProxy {
    @Override
    public void addInventoryModels() {
        ModelLoader.setCustomModelResourceLocation(ModItems.itemKey, 0, new ModelResourceLocation(Reference.MOD_ID + ':'
                + Reference.Names.Items.KEY, "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.DISPLAY), 0, new
                ModelResourceLocation(ModBlocks.DISPLAY.getRegistryName().toString(), "inventory"));
    }

    @Override
    public void initConfig() {
        KismetConfig.preInit();
        KismetConfig.clientPreInit();
    }

    @Override
    public void registerTESR() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileDisplay.class, new RenderTileDisplay());
    }

    @Override
    public void registerBlockItemColor() {
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(new BlockColorDisplay(), ModBlocks
                .DISPLAY);
    }

    @Override
    public void sendConfigToClient(EntityPlayer player) {
        // NOOP
    }
}
