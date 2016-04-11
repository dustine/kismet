package desutine.kismet.common.block;

import desutine.kismet.Reference;
import desutine.kismet.addon.JeiIntegration;
import desutine.kismet.common.registry.ModItems;
import desutine.kismet.common.tile.TileDisplay;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
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
    //    public static final PropertyInteger STREAK = PropertyInteger.create("streak", 0, 20);
    public static final PropertyBool FULFILLED = PropertyBool.create("fulfilled");
    public static final PropertyEnum<EnumFacing> FACING = PropertyEnum.create("facing", EnumFacing.class);

    public BlockDisplay() {
        super(Reference.Names.Blocks.DISPLAY);

        // declaring properties
        setDefaultState(blockState.getBaseState()
//                .withProperty(STREAK, 0)
                .withProperty(FULFILLED, false)
                .withProperty(FACING, EnumFacing.NORTH));
    }


    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileDisplay();
    }

    // convert from metadata
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
                .withProperty(FULFILLED, ((meta & 0b0100) == 0b0100))
                .withProperty(FACING, EnumFacing.HORIZONTALS[meta & 0b0011]);
    }

    // convert to metadata
    @Override
    public int getMetaFromState(IBlockState state) {
        int facing = state.getValue(FACING).getHorizontalIndex();
        facing &= 0b0011;

        int fulfilled = state.getValue(FULFILLED) ? 0b0100 : 0b0000;
        fulfilled &= 0b0100;

        return facing + fulfilled;
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
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
        // no placing blocks!
//        return false;
        return super.canPlaceBlockOnSide(worldIn, pos, side);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        // correcting state not being correct -_-
        state = worldIn.getBlockState(pos);

        TileDisplay te = (TileDisplay) worldIn.getTileEntity(pos);
        // do nothing if tile-entity is borked
        if (te == null) return false;

        // Check if the heldItem is the target
        if (heldItem != null && heldItem.isItemEqual(te.getTarget()) && !state.getValue(FULFILLED)) {
            // fulfilled target~
            setTargetAsFulfilled(worldIn, pos);
            return true;
        }

        if (worldIn.isRemote) {
            // logical client
            // no target = no gain
            if (te.getTarget() == null) return false;
            // okay so, if right-clicked with a Kismetic Key, while it not being the target, because stuff happens at
            // server-side, return true
            if (heldItem != null && heldItem.isItemEqual(new ItemStack(ModItems.itemKey)) && !state.getValue(FULFILLED)) {
                return true;
            }

            if (hand == EnumHand.MAIN_HAND) {
                // only on main hand to avoid spam
                // todo: I18n these strings
                String targetString;

                if (state.getValue(FULFILLED)) {
                    // add the streak, or not, if it is 2+
                    if (te.getStreak() > 1) {
                        targetString = String.format("[Kismet] Target §afulfilled§r (streak: %s), next target in %s",
                                te.getStylizedStreak(), te.getStylizedDeadline());
                    } else {
                        targetString = String.format("[Kismet] Target §afulfilled§r, next target in %s",
                                te.getStylizedDeadline());
                    }

                    playerIn.addChatComponentMessage(new TextComponentString(targetString));
                } else {
                    // special highlight on the target, to make it pop out
                    targetString = String.format("[Kismet] Current target: §b§o%s",
                            te.getTarget().getDisplayName());

                    playerIn.addChatComponentMessage(new TextComponentString(targetString));

                    // if it isn't fulfilled, the extra info goes into a second line
                    // streak goes in if 2+
                    String timedString;
                    if (te.getStreak() > 1) {
                        timedString = String.format("Target expires in %s, ongoing streak of %s item(s)",
                                te.getStylizedDeadline(), te.getStylizedStreak());
                    } else {
                        timedString = String.format("Target expires in %s", te.getStylizedDeadline());
                    }

                    playerIn.addChatComponentMessage(new TextComponentString(timedString));
                }

                // try JEI/NEI integration
                boolean success = JeiIntegration.doJeiIntegration(te, playerIn);
            }

            return false;
        } else {
            // logical server
            // If right-clicked with the key in any hand (while the target is unfulfilled), regen the item
            if (heldItem != null && heldItem.isItemEqual(new ItemStack(ModItems.itemKey)) && !state.getValue(FULFILLED)) {
                // key = free regen
                te.getNewTarget();
                return true;
            }
        }

        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
    }

    public void setTargetAsFulfilled(World worldIn, BlockPos pos) {
        TileDisplay te = (TileDisplay) worldIn.getTileEntity(pos);
        te.setStreak(te.getStreak() + 1);
        worldIn.setBlockState(pos, worldIn.getBlockState(pos).withProperty(FULFILLED, true));
    }

    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    // returning block state
    @Override
    protected BlockStateContainer createBlockState() {
//        IProperty[] listedProperties = new IProperty[] {STREAK, FULFILLED, FACING};
        IProperty[] listedProperties = new IProperty[] {FULFILLED, FACING};
        IUnlistedProperty[] unlistedProperties = new IUnlistedProperty[] {};
        return new ExtendedBlockState(this, listedProperties, unlistedProperties);
    }
}