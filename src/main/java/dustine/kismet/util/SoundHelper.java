package dustine.kismet.util;

import dustine.kismet.Kismet;
import dustine.kismet.registry.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SoundHelper {
    public static void onKeyUsage(World world, EntityPlayer player, BlockPos pos, boolean success) {
        if (success) {
            correctClick(world, player, pos);
        } else {
            player.renderBrokenItemStack(new ItemStack(ModItems.KEY));
        }
    }

    private static void correctClick(World world, EntityPlayer player, BlockPos pos) {
        // reusing sounds because it works.
        world.playSound(player, pos, SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, SoundCategory.BLOCKS, 0.8F, 0.8F +
                Kismet.random.nextFloat() * 0.4F);
    }

    public static void onTargetFulfilled(World world, EntityPlayer player, BlockPos pos) {
        correctClick(world, player, pos);
    }
}
