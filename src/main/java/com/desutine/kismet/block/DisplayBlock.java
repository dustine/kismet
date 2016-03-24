package com.desutine.kismet.block;

import com.desutine.kismet.reference.Names;
import com.desutine.kismet.tileentity.DisplayTileEntity;
import com.desutine.kismet.tileentity.ModBlockContainer;
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

public class DisplayBlock extends ModBlockContainer<DisplayTileEntity> {
    public static final PropertyInteger STREAK = PropertyInteger.create("streak", 0, 20);

    public static final PropertyBool FULFILLED = PropertyBool.create("fulfilled");

    public DisplayBlock() {
        super();
        this.setUnlocalizedName(Names.DISPLAY);

        // declaring properties
        setDefaultState(blockState.getBaseState()
                .withProperty(STREAK, 0)
                .withProperty(FULFILLED, false));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new DisplayTileEntity();
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
        DisplayTileEntity tileEntity = (DisplayTileEntity) worldIn.getTileEntity(pos);
        return tileEntity.enrichState(newState);
    }

    // used by the renderer to control lighting and visibility of other blocks, also by
    // (eg) wall or fence to control whether the fence joins itself to this block
    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
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
        if (worldIn.isRemote) return true;

        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
    }

    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return super.onBlockPlaced(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);
    }

    // returning block state
    @Override
    protected BlockStateContainer createBlockState() {
        IProperty[] listedProperties = new IProperty[] {STREAK, FULFILLED};
        IUnlistedProperty[] unlistedProperties = new IUnlistedProperty[] {};
        return new ExtendedBlockState(this, listedProperties, unlistedProperties);
    }
}