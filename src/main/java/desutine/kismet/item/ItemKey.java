package desutine.kismet.item;

import desutine.kismet.block.BlockDisplay;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemKey extends ItemKismet {
    public ItemKey() {
        super();
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        final Block block = world.getBlockState(pos).getBlock();
        if (block instanceof BlockDisplay) {
            // only expend the key if the block is ready
            final Boolean isReady = block.getActualState(world.getBlockState(pos), world, pos)
                    .getValue(BlockDisplay.READY);
            if (isReady) {
                stack.stackSize--;
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.PASS;
    }
}
