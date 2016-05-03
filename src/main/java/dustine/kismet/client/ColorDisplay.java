package dustine.kismet.client;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ColorDisplay implements IBlockColor, IItemColor {
    public ColorDisplay() {
    }

    @Override
    public int colorMultiplier(IBlockState state, IBlockAccess blockAccess, BlockPos pos, int tintIndex) {
        return -1;
    }

    @Override
    public int getColorFromItemstack(ItemStack stack, int tintIndex) {
        return -1;
    }
}
