package com.desutine.kismet.tileentity;

import com.desutine.kismet.block.ModBlock;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class ModBlockContainer<T extends TileEntity> extends ModBlock implements ITileEntityProvider {
    public ModBlockContainer(){
        super();
        this.isBlockContainer = true;
    }

    public ModBlockContainer(Material material) {
        super(material);
        this.isBlockContainer = true;
    }

    @Override
    abstract public TileEntity createNewTileEntity(World worldIn, int meta);

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        super.breakBlock(world, pos, state);
        world.removeTileEntity(pos);
    }

    @Override
    public boolean onBlockEventReceived(World worldIn, BlockPos pos, IBlockState state, int eventID, int eventParam) {
        super.onBlockEventReceived(worldIn, pos, state, eventID, eventParam);
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        return tileEntity != null && tileEntity.receiveClientEvent(eventID, eventParam);
    }
}
