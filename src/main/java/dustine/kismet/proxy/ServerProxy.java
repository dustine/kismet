package dustine.kismet.proxy;

import dustine.kismet.Kismet;
import dustine.kismet.config.ConfigCopy;
import dustine.kismet.network.message.MessageServerConfig;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public class ServerProxy extends CommonProxy {
    @Override
    public boolean sideSafeHasSubtypes(ItemStack stack) {
        // because the server fails to properly quantify for some subtypes, if they exist
        // we return true by default and let the client-side fix it afterwards
        return true;
    }

    @Override public void broadcastServerConfig() {
        Kismet.network.sendToAll(new MessageServerConfig(new ConfigCopy()));
    }

    @Override public void sendServerConfig(EntityPlayerMP player) {
        Kismet.network.sendTo(new MessageServerConfig(new ConfigCopy()), player);
    }
}
