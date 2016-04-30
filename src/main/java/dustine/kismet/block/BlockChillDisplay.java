package dustine.kismet.block;

import dustine.kismet.tile.TileDisplay;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockChillDisplay extends BlockDisplay {
    public BlockChillDisplay() {
        super();
    }

    @Override
    public void setTargetAsFulfilled(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        super.setTargetAsFulfilled(world, pos, state, player);

        TileDisplay te = (TileDisplay) world.getTileEntity(pos);
        te.getNewTarget();
    }
}
