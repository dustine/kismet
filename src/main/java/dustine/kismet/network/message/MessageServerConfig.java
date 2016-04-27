package dustine.kismet.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Map;

/**
 * Send Config to Client message
 * <p>
 * Includes: Common configuration that needs to be synced between dedicated server and clients, such as timedLimit
 */
public class MessageServerConfig extends MessageBase<MessageServerConfig> {
    private Map<String, Object> syncValues;

    public MessageServerConfig() {
    }

    public MessageServerConfig(Map<String, Object> syncValues) {
        this.syncValues = syncValues;
    }

    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    @Override
    protected void handleServerSide(MessageServerConfig message, EntityPlayer player) {
    }

    @Override
    protected void handleClientSide(MessageServerConfig message, EntityPlayer player) {
    }
}
