package dustine.kismet.proxy;

import dustine.kismet.Kismet;
import dustine.kismet.network.message.MessageServerConfig;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public class ServerProxy extends CommonProxy {
    @Override
    public void sendConfigToClient(EntityPlayerMP player) {
        // send the config to the client
        Kismet.network.sendTo(new MessageServerConfig(), player);
    }

    @Override
    public boolean sideSafeHasSubtypes(ItemStack stack) {
        // because the server fails to properly quantify for some subtypes, if they exist
        // we return true by default and let the client-side fix it afterwards
        return true;
    }
}
