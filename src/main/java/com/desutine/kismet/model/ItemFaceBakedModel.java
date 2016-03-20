//package com.desutine.kismet.model;
//
//import com.desutine.kismet.block.BlockDisplay;
//import com.desutine.kismet.init.Blocks;
//import net.minecraft.block.Block;
//import net.minecraft.block.state.IBlockState;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.renderer.block.model.*;
//import net.minecraft.client.renderer.texture.TextureAtlasSprite;
//import net.minecraft.item.Item;
//import net.minecraft.util.EnumFacing;
//import net.minecraftforge.client.model.IModel;
//import net.minecraftforge.client.model.ModelLoader;
//import net.minecraftforge.client.model.ModelLoaderRegistry;
//import net.minecraftforge.common.property.IExtendedBlockState;
//import net.minecraftforge.fml.common.FMLLog;
//import net.minecraftforge.fml.common.registry.GameRegistry;
//import org.apache.logging.log4j.Level;
//
//import java.util.List;
//
//public class ItemFaceBakedModel implements IBakedModel {
//    @Override
//    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
////        ModelLoaderRegistry.getModel()
//
//        Item item = ((IExtendedBlockState)state).getValue(BlockDisplay.TARGET);
//        Minecraft mc = Minecraft.getMinecraft();
//        ModelManager mcmm = mc.getBlockRendererDispatcher().getBlockModelShapes().getModelManager();
//
//        try {
//            // try to access the inventory display of the thing
//            IBakedModel model = mcmm.getModel(new ModelResourceLocation(item.getRegistryName(), "inventory"));
//            return model.getQuads;
//        } catch (Exception e) {
//            FMLLog.log(Level.ERROR, e, "", "");
//        }
//        return null;
//    }
//
//    @Override
//    public boolean isAmbientOcclusion() {
//        return false;
//    }
//
//    @Override
//    public boolean isGui3d() {
//        return false;
//    }
//
//    @Override
//    public boolean isBuiltInRenderer() {
//        return false;
//    }
//
//    @Override
//    public TextureAtlasSprite getParticleTexture() {
//        // TODO - see if i broke something
//        return null;
//    }
//
//    @Override
//    public ItemCameraTransforms getItemCameraTransforms() {
//        return null;
//    }
//
//    @Override
//    public ItemOverrideList getOverrides() {
//        return null;
//    }
//}
