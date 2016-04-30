package dustine.kismet.block;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class BlockContainerKismet<T extends TileEntity> extends BlockKismet implements ITileEntityProvider {
    public BlockContainerKismet() {
        this(Material.ROCK);
    }

    public BlockContainerKismet(Material material) {
        super(material);
        this.isBlockContainer = true;
    }

    @Override
    abstract public T createNewTileEntity(World worldIn, int meta);

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        super.breakBlock(world, pos, state);
        world.removeTileEntity(pos);
    }

    /**
     * Called on both Client and Server when World#addBlockEvent is called
     */
    @Override
    public boolean onBlockEventReceived(World world, BlockPos pos, IBlockState state, int eventID, int eventParam) {
        super.onBlockEventReceived(world, pos, state, eventID, eventParam);
        TileEntity tileEntity = world.getTileEntity(pos);
        return tileEntity != null && tileEntity.receiveClientEvent(eventID, eventParam);
    }
}
