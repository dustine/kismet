package desutine.kismet.proxy;

import desutine.kismet.client.ModBlockColor;
import desutine.kismet.client.renderer.RendererTileDisplay;
import desutine.kismet.common.block.BlockKismet;
import desutine.kismet.common.config.ConfigKismet;
import desutine.kismet.common.init.ModItems;
import desutine.kismet.common.tile.TileDisplay;
import desutine.kismet.common.init.ModBlocks;
import desutine.kismet.reference.Names;
import desutine.kismet.reference.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientProxy extends CommonProxy {
    @Override
    public void addInventoryModels() {
        ModelLoader.setCustomModelResourceLocation(ModItems.itemKey, 0, new ModelResourceLocation(Reference.MODID + ':'
                + Names.Items.KEY, "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.kismetDisplayBlock), 0, new
                ModelResourceLocation(Reference.MODID + ':' + Names.Blocks.DISPLAY, "inventory"));
    }

    @Override
    public void initConfig() {
        ConfigKismet.preInit();
        ConfigKismet.clientPreInit();
    }

    @Override
    public void registerTESR() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileDisplay.class, new RendererTileDisplay());
    }

    @Override
    public void registerBlockItemColor() {
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(new ModBlockColor(), ModBlocks
                .kismetDisplayBlock);
    }
}
