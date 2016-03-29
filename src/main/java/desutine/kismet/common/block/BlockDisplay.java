package desutine.kismet.common.block;

import desutine.kismet.ModLogger;
import desutine.kismet.client.JeiIntegration;
import desutine.kismet.common.tile.TileDisplay;
import desutine.kismet.common.init.ModItems;
import desutine.kismet.reference.Names;
import mezz.jei.api.IItemListOverlay;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        // correcting state not being correct -_-
        state = worldIn.getBlockState(pos);

        // just consume the event if block is fulfilled
        ModLogger.info(String.format("%s %s %s", state, playerIn, hand
                .toString()));

        TileDisplay te = (TileDisplay) worldIn.getTileEntity(pos);
        // do nothing if tile-entity is borked
        if(te == null) return false;

        // Check if the heldItem is the target
        if(heldItem != null && heldItem.isItemEqual(te.getTarget()) && !state.getValue(FULFILLED)){
            // fulfilled target~
            setTargetAsFulfilled(worldIn, pos);
            return true;
        }

        if(worldIn.isRemote) {
            // logical client
            // try JEI/NEI integration
            if(te.getTarget() == null) return false;
            boolean success = doJeiIntegration(te, playerIn);

            if(!success && heldItem == null && hand==EnumHand.MAIN_HAND){
                // only if right-clicking with an empty hand (main hand to avoid double spam)
                // do we print the extra info
                // todo: I18n these strings
                String targetString = String.format("[Kismet] Current target: %s", te.getTarget().getDisplayName());
                playerIn.addChatComponentMessage(new TextComponentString(targetString).setChatStyle(new Style().setColor(TextFormatting.AQUA)));

                List<ITextComponent> timedTextComponents = new ArrayList<>();
                timedTextComponents.add(new TextComponentString("Target expires in "));

                long remaining = te.getDeadline() - worldIn.getTotalWorldTime();
                String remainingTime = DurationFormatUtils.formatDurationHMS(remaining * (1000/20));
                Style timerStyle = new Style();
                if(remaining <= 15*60*20){
                    if (remaining > 10*60*20) {
                        timerStyle.setColor(TextFormatting.YELLOW);
                    } else if (remaining > 5*60*20) {
                        timerStyle.setColor(TextFormatting.RED);
                    } else {
                        timerStyle.setColor(TextFormatting.RED)
                                .setBold(true);
                    }
                }
                timedTextComponents.add(new TextComponentString(remainingTime).setChatStyle(timerStyle));
                timedTextComponents.add(new TextComponentString(", ongoing streak of "));

                int streak = te.getStreak()/10;
                Style streakStyle = new Style();
                TextFormatting[] colors = new TextFormatting[]{
                        TextFormatting.WHITE,
                        TextFormatting.GREEN,
                        TextFormatting.DARK_BLUE,
                        TextFormatting.LIGHT_PURPLE,
                        TextFormatting.GOLD
                };
                if(streak > colors.length){
                    // set the last colour
                    streakStyle.setColor(colors[colors.length-1]);
                } else {
                    streakStyle.setColor(colors[streak]);
                }
                timedTextComponents.add(new TextComponentString("" + streak).setChatStyle(streakStyle));
                timedTextComponents.add(new TextComponentString(" item(s)"));

                Optional<ITextComponent> result = timedTextComponents.stream().reduce(ITextComponent::appendSibling);
                if(result.isPresent()){
                    playerIn.addChatComponentMessage(result.get());
                }
            }

            return false;
        } else {
            // logical server
            // If right-clicked with the key in any hand (while the target is unfulfilled), regen the item
            if(heldItem != null && heldItem.isItemEqual(new ItemStack(ModItems.itemKey)) && !state.getValue(FULFILLED)){
                // key = free regen
                te.getNewTarget();
                return true;
            }
        }

        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
    }

    private boolean doJeiIntegration(TileDisplay te, EntityPlayer playerIn) {
        IItemListOverlay itemList = JeiIntegration.itemListOverlay;
        if(itemList != null) {
            try {
                String oldFilter = itemList.getFilterText();

                String filter=te.getTarget().getDisplayName();
                String mod = te.getTarget().getItem().getRegistryName();
                mod = mod.substring(0, mod.indexOf(":"));
                filter = String.format("%s @%s", filter, mod);
                if(oldFilter.equalsIgnoreCase(filter)) return false;

                // empty hand = give information about the block
                Minecraft.getMinecraft().displayGuiScreen(new GuiInventory(playerIn));
                itemList.setFilterText(filter);
            } catch (NullPointerException e){
                return false;
            }
            return true;
        }
        return false;
    }

    public void setTargetAsFulfilled(World worldIn, BlockPos pos) {
        TileDisplay te = (TileDisplay) worldIn.getTileEntity(pos);
        te.setStreak(te.getStreak()+1);
        worldIn.setBlockState(pos, worldIn.getBlockState(pos).withProperty(FULFILLED, true));
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