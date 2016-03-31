package desutine.kismet.client;

import desutine.kismet.common.block.BlockDisplay;
import desutine.kismet.reference.Colors;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ModBlockColor implements IBlockColor {
    public ModBlockColor() {

    }

    @Override
    public int colorMultiplier(IBlockState state, IBlockAccess p_186720_2_, BlockPos pos, int tintIndex) {
        final boolean fulfilled = state.getValue(BlockDisplay.FULFILLED);
        if (fulfilled) {
            return Colors.LIME;
        } else {
            return Colors.RED;
        }
//        return -1;
    }
}
