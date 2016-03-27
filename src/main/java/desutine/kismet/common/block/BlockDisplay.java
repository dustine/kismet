package desutine.kismet.common.block;

import desutine.kismet.common.tile.TileDisplay;
import desutine.kismet.reference.Items;
import desutine.kismet.reference.Names;
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
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockDisplay extends ContainerKismet<TileDisplay> {
    public static final PropertyInteger STREAK = PropertyInteger.create("streak", 0, 20);
    public static final PropertyBool FULFILLED = PropertyBool.create("fulfilled");

    public BlockDisplay() {
        super();
        this.setUnlocalizedName(Names.DISPLAY);

        // declaring properties
        setDefaultState(blockState.getBaseState()
                .withProperty(STREAK, 0)
                .withProperty(FULFILLED, false));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileDisplay();
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
        TileDisplay tileEntity = (TileDisplay) worldIn.getTileEntity(pos);
        if (tileEntity != null)
            return tileEntity.enrichState(newState);
        else return newState;
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
        TileDisplay te = (TileDisplay) worldIn.getTileEntity(pos);

        // no clients, the following code is server-only for ~safety~ and synchronicity
        if (worldIn.isRemote){
            playerIn.addChatComponentMessage(new TextComponentString(te.getTarget().getDisplayName()));
            return true;
        }

        if(te == null) return super.onBlockActivated(worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ);

        if(heldItem != null && heldItem.isItemEqual(new ItemStack(Items.itemKey))){
            // key = free regen
            te.getNewTarget();
            playerIn.addChatComponentMessage(new TextComponentString(te.getTarget().getDisplayName()));
        }


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