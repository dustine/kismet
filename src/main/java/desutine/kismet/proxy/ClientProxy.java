package desutine.kismet.proxy;

import desutine.kismet.client.JeiIntegration;
import desutine.kismet.client.ModBlockColor;
import desutine.kismet.client.renderer.RendererTileDisplay;
import desutine.kismet.common.config.ConfigKismet;
import desutine.kismet.common.init.ModItems;
import desutine.kismet.common.tile.TileDisplay;
import desutine.kismet.common.init.ModBlocks;
import desutine.kismet.reference.Names;
import desutine.kismet.reference.Reference;
import mezz.jei.api.IItemListOverlay;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.apache.commons.lang3.time.DurationFormatUtils;

public class ClientProxy extends CommonProxy {
    @Override
    public void addInventoryModels() {
        ModelLoader.setCustomModelResourceLocation(ModItems.itemKey, 0, new ModelResourceLocation(Reference.MODID + ':'
                + Names.Items.KEY, "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.DISPLAY), 0, new
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
                .DISPLAY);
    }
}
