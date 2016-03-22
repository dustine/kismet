package com.desutine.kismet.block;

import com.desutine.kismet.reference.Names;
import com.desutine.kismet.tileentity.ModBlockContainer;
import com.desutine.kismet.tileentity.TileEntityDisplay;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockDisplay extends ModBlockContainer<TileEntityDisplay> {
    public static final PropertyInteger STREAK = PropertyInteger.create("streak", 0, 20);
//    private static final IUnlistedProperty<Item> TARGET = new IUnlistedProperty<Item>() {
//        @Override
//        public String getName() {
//            return "target";
//        }
//
//        @Override
//        public boolean isValid(Item s) {
//            // todo
//            return true;
//        }
//
//        @Override
//        public Class<Item> getType() {
//            return Item.class;
//        }
//
//        @Override
//        public String valueToString(Item s) {
//            return s.getRegistryName();
//        }
//    };
    public static final PropertyBool FULFILLED = PropertyBool.create("fulfilled");

    public BlockDisplay() {
        super();
        this.setUnlocalizedName(Names.DISPLAY_NAME);

        // declaring properties
        setDefaultState(blockState.getBaseState()
            .withProperty(STREAK, 0)
            .withProperty(FULFILLED, false));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityDisplay();
    }

    // convert to/from metadata
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState();
    }

    // metadata doesn't matter in this case
    // convert to/from metadata
    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        IBlockState newState = super.getActualState(state, worldIn, pos);
        TileEntityDisplay tileEntity = (TileEntityDisplay) worldIn.getTileEntity(pos);
        return tileEntity.enrichState(state);
    }

    // used by the renderer to control lighting and visibility of other blocks, also by
    // (eg) wall or fence to control whether the fence joins itself to this block
    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    // used by the renderer to control lighting and visibility of other blocks.
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if(worldIn.isRemote) return true;

        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
    }

    // returning block state
    @Override
    protected BlockStateContainer createBlockState() {
        IProperty[] listedProperties = new IProperty[] {STREAK, FULFILLED};
        IUnlistedProperty[] unlistedProperties = new IUnlistedProperty[] {};
        return new ExtendedBlockState(this, listedProperties, unlistedProperties);
    }


    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return super.onBlockPlaced(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }
}