package dustine.kismet.event;

import dustine.kismet.Kismet;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class SyncPlayerConfig extends EventSubscriber<PlayerEvent.PlayerLoggedInEvent> {
    @SubscribeEvent
    public void onEvent(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.player.worldObj.isRemote) {
            // only for safe keeping as this event only happens server side
            Kismet.proxy.sendServerConfig((EntityPlayerMP) event.player);
        }
    }
}
