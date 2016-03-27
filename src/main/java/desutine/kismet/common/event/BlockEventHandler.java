package desutine.kismet.common.event;

import net.minecraft.client.Minecraft;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BlockEventHandler {
    @SubscribeEvent
    public void increaseStreak(PlayerInteractEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
    }
}
