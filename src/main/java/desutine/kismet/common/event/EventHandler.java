package desutine.kismet.common.event;

import desutine.kismet.Logger;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EventHandler {
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void addISmartModel(ModelBakeEvent event) {
        Logger.info("ModelBakeEvent evented");
//        event.getModelRegistry().putObject(new ModelResourceLocation("kismet:"), new ItemFaceBakedModel() {
//        });
    }
}
