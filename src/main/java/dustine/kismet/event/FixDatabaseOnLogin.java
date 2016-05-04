package dustine.kismet.event;

import dustine.kismet.Kismet;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class FixDatabaseOnLogin extends EventSubscriber<PlayerEvent.PlayerLoggedInEvent> implements IUnregisterable {
    @SubscribeEvent
    public void onEvent(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            Kismet.proxy.cleanTargetLibrary((EntityPlayerMP) event.player);
        }
        unregister();
    }

    @Override public void unregister() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }
}
