package desutine.kismet.common.event;

import desutine.kismet.ModLogger;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventHandler {
    @SubscribeEvent
    public void addISmartModel(ModelBakeEvent event) {
        ModLogger.info("ModelBakeEvent evented");
//        event.getModelRegistry().putObject(new ModelResourceLocation("kismet:"), new ItemFaceBakedModel() {
//        });
    }
}
