package desutine.kismet.client;

import desutine.kismet.Reference;
import desutine.kismet.common.block.BlockDisplay;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockColorDisplay implements IBlockColor {
    public BlockColorDisplay() {

    }

    @Override
    public int colorMultiplier(IBlockState state, IBlockAccess blockAccess, BlockPos pos, int tintIndex) {
        switch (tintIndex) {
            case 0:
                final boolean fulfilled = state.getValue(BlockDisplay.FULFILLED);
                if (fulfilled) {
                    return Reference.Colors.LIME;
                }
                return Reference.Colors.RED;

        }

        return -1;
    }
}
