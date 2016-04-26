package dustine.kismet.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Finish Enriching Stacks message
 * <p>
 * Includes: nothing, as it's just an empty message to signal an event ^^"
 */
public class FESMessage implements IMessage {
    public FESMessage() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }
}
