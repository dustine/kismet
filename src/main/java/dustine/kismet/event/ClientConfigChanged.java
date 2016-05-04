package dustine.kismet.event;

import dustine.kismet.Kismet;
import dustine.kismet.Log;
import dustine.kismet.Reference;
import dustine.kismet.config.ConfigKismet;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;

public class ClientConfigChanged extends EventSubscriber<ConfigChangedEvent.OnConfigChangedEvent> {
    /*
     * This class, when instantiated as an object, will listen on the FML
     *  event bus for an OnConfigChangedEvent
     */
    public void onEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (Reference.MOD_ID.equals(event.getModID())) {
            ConfigKismet.syncFromGUI();
            final String category = event.getConfigID();
            if (category != null) {
                // force a library refresh if in-world and any changes occurred regarding the target category
                if (category.equals(ConfigKismet.CATEGORY_TARGETS)) {
                    Log.trace("Refreshing target library...");
                    if (Kismet.databaseBuilder != null)
                        Kismet.databaseBuilder.tryBuildLibraryWithLastGeneratedDatabase();
                }
                Log.debug("Config changed on Gui, category " + category);
            } else {
                Log.debug("Config changed on Gui, no category");
            }

            Kismet.proxy.broadcastServerConfig();
        }
    }
}
