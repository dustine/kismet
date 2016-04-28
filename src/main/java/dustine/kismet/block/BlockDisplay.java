package dustine.kismet.block;

import dustine.kismet.Kismet;
import dustine.kismet.gui.GuiHandler;
import dustine.kismet.tile.TileDisplay;
import dustine.kismet.util.SoundHelper;
import dustine.kismet.util.StackHelper;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockDisplay extends BlockContainerKismet<TileDisplay> {
    public static final PropertyBool READY = PropertyBool.create("ready");
    public static final PropertyEnum<EnumFacing> FACING = PropertyEnum.create("facing", EnumFacing.class);
    public static final PropertyBool FULFILLED = PropertyBool.create("fulfilled");
    private static final double slabSize = 3 / 16.0;
    private static final AxisAlignedBB upAABB = new AxisAlignedBB(0, 0, 0, 1, slabSize, 1);
    private static final AxisAlignedBB downAABB = new AxisAlignedBB(0, 1 - slabSize, 0, 1, 1, 1);
    private static final AxisAlignedBB southAABB = new AxisAlignedBB(0, 0, 0, 1, 1, slabSize);
    private static final AxisAlignedBB northAABB = new AxisAlignedBB(0, 0, 1 - slabSize, 1, 1, 1);
    private static final AxisAlignedBB eastAABB = new AxisAlignedBB(0, 0, 0, slabSize, 1, 1);
    private static final AxisAlignedBB westAABB = new AxisAlignedBB(1 - slabSize, 0, 0, 1, 1, 1);

    public BlockDisplay() {
        super();

        // declaring properties
        setDefaultState(this.blockState.getBaseState()
                .withProperty(READY, false)
                .withProperty(FACING, EnumFacing.NORTH)
                .withProperty(FULFILLED, false));
    }


    @Override
    public TileDisplay createNewTileEntity(World worldIn, int meta) {
        return new TileDisplay();
    }

    // convert from metadata
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
                .withProperty(FACING, EnumFacing.VALUES[meta & 0b0111])
                .withProperty(BlockDisplay.FULFILLED, (meta & 0b1000) == 0b1000);
    }

    // convert to metadata
    @Override
    public int getMetaFromState(IBlockState state) {
        final int facing = state.getValue(FACING).getIndex() & 0b0111;

        int fulfilled = state.getValue(BlockDisplay.FULFILLED) ? 0b1000 : 0b0000;

        return facing + fulfilled;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        IBlockState newState = super.getActualState(state, worldIn, pos);
        TileDisplay tile = (TileDisplay) worldIn.getTileEntity(pos);
        if (tile != null) {
            newState = newState.withProperty(READY, tile.isReady());
        }
        return newState;
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

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        final EnumFacing facing = state.getValue(FACING);
        final AxisAlignedBB axisAlignedBB;
        switch (facing) {
            case DOWN:
                axisAlignedBB = downAABB;
                break;
            case UP:
                axisAlignedBB = upAABB;
                break;
            case NORTH:
                axisAlignedBB = northAABB;
                break;
            case SOUTH:
                axisAlignedBB = southAABB;
                break;
            case WEST:
                axisAlignedBB = westAABB;
                break;
            case EAST:
                axisAlignedBB = eastAABB;
                break;
            default:
                return super.getBoundingBox(state, source, pos);
        }
        return axisAlignedBB;
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
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        // correcting state -_-
        state = getActualState(world.getBlockState(pos), world, pos);

        TileDisplay te = (TileDisplay) world.getTileEntity(pos);
        // do nothing if tile-entity is borked
        if (te == null) return false;

        // Check if the heldItem is the target
        if (heldItem != null && StackHelper.isEquivalent(te.getTarget(), heldItem)) {
            // fulfilled target~
            setTargetAsFulfilled(world, pos, state, player);
            // because of bug
            return true;
        }

        if (hand == EnumHand.OFF_HAND && !world.isRemote) {
            player.openGui(Kismet.instance, GuiHandler.EnumGuiID.DISPLAY.ordinal(), world, pos.getX(), pos.getY(), pos.getZ());
        }

        return false;
    }

    public void setTargetAsFulfilled(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        if (state.getValue(FULFILLED)) return;

        SoundHelper.onTargetFulfilled(world, player, pos);
        world.setBlockState(pos, state.withProperty(BlockDisplay.FULFILLED, true));
        TileDisplay te = (TileDisplay) world.getTileEntity(pos);
        te.setSkipped(te.getSkipped() - 1);
        te.setScore(te.getScore() + 1);
    }

    @Override
    public IBlockState onBlockPlaced(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, facing);
    }

    // returning block state
    @Override
    protected BlockStateContainer createBlockState() {
        IProperty[] listedProperties = new IProperty[] {FULFILLED, FACING, READY};
        IUnlistedProperty[] unlistedProperties = new IUnlistedProperty[] {};
        return new ExtendedBlockState(this, listedProperties, unlistedProperties);
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }
}