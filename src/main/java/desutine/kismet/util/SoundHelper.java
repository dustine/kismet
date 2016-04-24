package desutine.kismet.util;

import desutine.kismet.Kismet;
import desutine.kismet.registry.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;

public class SoundHelper {
    public static void onKeyUsage(EntityPlayer player, boolean success) {
        if (success) {
            correctClick(player);
        } else {
            player.renderBrokenItemStack(new ItemStack(ModItems.ITEM_KEY));
        }
    }

    private static void correctClick(EntityPlayer player) {
        player.playSound(SoundEvents.block_stone_button_click_on, 0.8F, 0.8F + Kismet.random.nextFloat() * 0.4F);
    }

    public static void onTargetFulfilled(EntityPlayer player) {
        correctClick(player);
    }
}
