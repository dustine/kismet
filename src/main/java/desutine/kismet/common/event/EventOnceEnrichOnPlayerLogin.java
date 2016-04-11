package desutine.kismet.common.event;

import desutine.kismet.Kismet;
import desutine.kismet.util.TargetHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class EventOnceEnrichOnPlayerLogin {
    private static EventOnceEnrichOnPlayerLogin instance = new EventOnceEnrichOnPlayerLogin();

    private EventOnceEnrichOnPlayerLogin() {
    }

    public static EventOnceEnrichOnPlayerLogin getInstance() {
        return instance;
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        // this event only runs server-side
        WorldServer world = (WorldServer) event.player.worldObj;
        if (Kismet.command.checkPermission(world.getMinecraftServer(), event.player)) {
            // only run this once
            // fixme the event should only be dropped once it succeeds
            MinecraftForge.EVENT_BUS.unregister(this);

            TargetHelper.generateStacks((EntityPlayerMP) event.player);
        }

    }
}
