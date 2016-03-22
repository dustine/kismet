package com.desutine.kismet.block;

import com.desutine.kismet.reference.Items;
import com.desutine.kismet.reference.Names;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockDisplay extends ModBlock {
    private static final PropertyInteger STREAK = PropertyInteger.create("streak", 0, 20);
    private static final IUnlistedProperty<Item> TARGET = new IUnlistedProperty<Item>() {
        @Override
        public String getName() {
            return "target";
        }

        @Override
        public boolean isValid(Item s) {
            // todo
            return true;
        }

        @Override
        public Class<Item> getType() {
            return Item.class;
        }

        @Override
        public String valueToString(Item s) {
            return s.getRegistryName();
        }
    };

    public BlockDisplay() {
        super();
        this.setUnlocalizedName(Names.DISPLAY_NAME);

        // declaring properties
        setDefaultState(blockState.getBaseState().withProperty(STREAK, 0));
        setDefaultState(((IExtendedBlockState) blockState.getBaseState())
                .withProperty(TARGET, Items.itemKey)
                .withProperty(STREAK, 0)
        );
    }

    // convert to/from metadata
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(STREAK, meta);
    }

    // convert to/from metadata
    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(STREAK);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return super.getActualState(state, worldIn, pos);
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
        worldIn.setBlockState(pos, state.withProperty(BlockDisplay.STREAK, (state.getValue(BlockDisplay.STREAK) + 1)
                % 21));
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
    }

    // returning block state
    @Override
    protected BlockStateContainer createBlockState() {
        IProperty[] listedProperties = new IProperty[] {STREAK};
        IUnlistedProperty[] unlistedProperties = new IUnlistedProperty[] {TARGET};
        return new ExtendedBlockState(this, listedProperties, unlistedProperties);
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return super.getExtendedState(state, world, pos);
    }
}
