package dustine.kismet.event;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

public abstract class EventSubscriber<E extends Event> {
    public void init() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public abstract void onEvent(E event);
}
