package dustine.kismet.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.Map;

/**
 * Send Config to Client message
 * <p>
 * Includes: Common configuration that needs to be synced between dedicated server and clients, such as timedLimit
 */
public class SCTCMessage implements IMessage {
    public Map<String, Object> syncValues;

    public SCTCMessage() {
    }

    public SCTCMessage(Map<String, Object> syncValues) {
        this.syncValues = syncValues;
    }

    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }
}
