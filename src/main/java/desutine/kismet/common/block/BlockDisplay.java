package desutine.kismet.common.block;

import com.ibm.icu.impl.duration.BasicPeriodFormatterFactory;
import com.ibm.icu.impl.duration.BasicPeriodFormatterService;
import com.ibm.icu.impl.duration.PeriodFormatter;
import desutine.kismet.JeiIntegration;
import desutine.kismet.ModLogger;
import desutine.kismet.common.tile.TileDisplay;
import desutine.kismet.common.init.ModItems;
import desutine.kismet.reference.Names;
import mezz.jei.api.IItemListOverlay;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.color.IBlockColor;
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
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.sql.Time;
import java.time.Duration;

public class BlockDisplay extends ContainerKismet<TileDisplay>{
//    public static final PropertyInteger STREAK = PropertyInteger.create("streak", 0, 20);
    public static final PropertyBool FULFILLED = PropertyBool.create("fulfilled");
    public static final PropertyEnum<EnumFacing> FACING = PropertyEnum.create("facing", EnumFacing.class);

    public BlockDisplay() {
        super();
        this.setUnlocalizedName(Names.Blocks.DISPLAY);

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

        int fulfilled = state.getValue(FULFILLED)? 0b0100 : 0b0000;
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
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        // just consume the event if block is fulfilled
        if(state.getValue(FULFILLED)) return true;

        TileDisplay te = (TileDisplay) worldIn.getTileEntity(pos);
        // check if the item is correct
        if(heldItem != null && te!=null && heldItem.isItemEqual(te.getTarget())){
            // fulfilled target~
            te.setFulfilled(true);
            return true;
        }

        if (worldIn.isRemote){
            // client-only code
            if(te == null || te.getTarget() == null) return false;
            if(heldItem != null) return true;
            IItemListOverlay itemList = JeiIntegration.itemListOverlay;
            if(itemList != null) {
                // JEI integration?
//                Minecraft.getMinecraft().displayGuiScreen(new GuiInventory(playerIn));

                String name=te.getTarget().getDisplayName();
                String mod = te.getTarget().getItem().getRegistryName();
                mod = mod.substring(0, mod.indexOf(":"));
                name = String.format("%s @%s", name, mod);
                itemList.setFilterText(name);
            }
            playerIn.addChatComponentMessage(new TextComponentString(te.getTarget().getDisplayName()));
            long remaining = te.getDeadline() - worldIn.getTotalWorldTime();
            String remainingString = DurationFormatUtils.formatDurationHMS(remaining * (1000/20));
            playerIn.addChatComponentMessage(new TextComponentString(remainingString));
            return false;
        }
        // no clients, the following code is server-only for ~safety~ and synchronicity
        if(te == null) return super.onBlockActivated(worldIn, pos, state, playerIn, hand, null, side, hitX, hitY, hitZ);

        // ken regens the thing
        if(heldItem != null && heldItem.isItemEqual(new ItemStack(ModItems.itemKey))){
            // key = free regen
            te.getNewTarget();
            return true;
        }

        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
    }

    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        final EnumFacing horizontal= placer.getHorizontalFacing().rotateY().rotateY();
        IBlockState state = getStateFromMeta(meta).withProperty(FACING, horizontal);
        int newMeta = getMetaFromState(state);
        return super.onBlockPlaced(worldIn, pos, facing, hitX, hitY, hitZ, newMeta, placer);
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