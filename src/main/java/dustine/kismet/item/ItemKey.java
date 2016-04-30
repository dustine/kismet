package dustine.kismet.item;

import dustine.kismet.Kismet;
import dustine.kismet.block.BlockDisplay;
import dustine.kismet.network.message.MessageKeyUsage;
import dustine.kismet.tile.TileDisplay;
import dustine.kismet.util.SoundHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
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
            boolean action = useKeyOnDisplay(stack, player, world, pos);
            return action ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
        }
        return EnumActionResult.PASS;
    }

    public static boolean useKeyOnDisplay(ItemStack stack, EntityPlayer player, World world, BlockPos pos) {
        // only expend the key if the block is ready
        final TileDisplay tile = (TileDisplay) world.getTileEntity(pos);
        if (tile == null || !tile.isReady()) return false;

        // use the key
        if (!player.isCreative()) stack.stackSize--;
        // being optimistic server-side and assuming the key works (whenever positive or negative)
        if (world.isRemote) return true;

        final boolean fulfilled = world.getBlockState(pos).getValue(BlockDisplay.FULFILLED);
        // if fulfilled, count as a success
        final boolean success = fulfilled || tile.rollForKey();

        if (success) {
            // fulfilled rerolls don't count as a skip
            if (!fulfilled) {
                tile.setSkipped(tile.getSkipped() + 1);
            }
            tile.getNewTarget();
        }

        SoundHelper.onKeyUsage(world, player, pos, success);
        Kismet.network.sendTo(new MessageKeyUsage(pos, success), (EntityPlayerMP) player);
        return true;
    }

}
