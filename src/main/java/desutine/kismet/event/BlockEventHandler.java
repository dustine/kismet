package desutine.kismet.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.WorldAccessContainer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BlockEventHandler {
    @SubscribeEvent
    public void increaseStreak(PlayerInteractEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
    }
}
