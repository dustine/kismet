package dustine.kismet.block;

import dustine.kismet.tile.TileDisplay;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class BlockTimedDisplay extends BlockDisplay {

    public BlockTimedDisplay() {
        super();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        final boolean parentResult = super.onBlockActivated(world, pos, state, player, hand, heldItem, side,
                hitX, hitY, hitZ);

        if (parentResult) return true;

        TileDisplay te = (TileDisplay) world.getTileEntity(pos);
        if (te == null) return false;
        if (te.getTarget() == null) return false;
        if (!te.getTarget().hasItem()) return false;

        // logical client
        if (world.isRemote) {
            // only on main hand to avoid spam
            if (hand == EnumHand.MAIN_HAND) {
                // todo: I18n these strings
                String targetString;

                if (state.getValue(FULFILLED)) {
                    // add the streak, or not, if it is 2+
                    if (te.getScore() > 1) {
                        targetString = String.format("[Kismet] Target §afulfilled§r (streak: %s), next target in %s",
                                te.getStylizedScore(), te.getStylizedDeadline());
                    } else {
                        targetString = String.format("[Kismet] Target §afulfilled§r, next target in %s",
                                te.getStylizedDeadline());
                    }

                    player.addChatComponentMessage(new TextComponentString(targetString));
                } else {
                    // special highlight on the target, to make it pop out
                    targetString = String.format("[Kismet] Current target: §b§o%s",
                            te.getTarget().getStack().getDisplayName());

                    player.addChatComponentMessage(new TextComponentString(targetString));

                    // if it isn't fulfilled, the extra info goes into a second line
                    // streak goes in if 2+
                    String timedString;
                    if (te.getScore() > 1) {
                        timedString = String.format("Target expires in %s, ongoing streak of %s item(s)",
                                te.getStylizedDeadline(), te.getStylizedScore());
                    } else {
                        timedString = String.format("Target expires in %s", te.getStylizedDeadline());
                    }

                    player.addChatComponentMessage(new TextComponentString(timedString));
                }

                // try JEI/NEI integration
//                boolean success = JeiIntegration.doJeiIntegration(te, playerIn);
            }
        }

        return false;
    }
}
