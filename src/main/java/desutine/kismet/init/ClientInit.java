package desutine.kismet.init;

import desutine.kismet.reference.Blocks;
import desutine.kismet.reference.Items;
import desutine.kismet.reference.Names;
import desutine.kismet.reference.Reference;
import desutine.kismet.tileentity.DisplayTileEntity;
import desutine.kismet.tileentity.DisplayTileEntityRenderer;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientInit {
    public static void initTileEntityRenderers() {
        // and their renderers too
        ClientRegistry.bindTileEntitySpecialRenderer(DisplayTileEntity.class, new DisplayTileEntityRenderer());
    }

    public static void renderInInventory() {
        // register inventory models :\
        ModelLoader.setCustomModelResourceLocation(Items.itemKey, 0, new ModelResourceLocation(Reference.MODID + ':'
                + Names.KEY, "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Blocks.kismetDisplayBlock), 0, new
                ModelResourceLocation(Reference.MODID + ':' + Names.DISPLAY, "inventory"));
    }
}
