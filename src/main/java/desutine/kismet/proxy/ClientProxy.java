package desutine.kismet.proxy;

import desutine.kismet.client.renderer.RendererTileDisplay;
import desutine.kismet.common.config.ConfigKismet;
import desutine.kismet.reference.Names;
import desutine.kismet.reference.Reference;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientProxy extends CommonProxy {
    @Override
    public void addInventoryModels() {
                + Names.Items.KEY, "inventory"));
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
}
