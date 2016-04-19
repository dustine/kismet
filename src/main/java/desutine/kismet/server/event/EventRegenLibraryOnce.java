package desutine.kismet.server.event;

import desutine.kismet.Kismet;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class EventRegenLibraryOnce {

    @SubscribeEvent
    public void onPlayerLoginCleanLibrary(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            Kismet.proxy.cleanTargetLibrary((EntityPlayerMP) event.player);
        }
        MinecraftForge.EVENT_BUS.unregister(this);
    }
}
