package desutine.kismet.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.Map;

public class MessageKismetConfig implements IMessage {
    public Map<String, Object> syncValues;

    public MessageKismetConfig() {
    }

    public MessageKismetConfig(Map<String, Object> syncValues) {
        this.syncValues = syncValues;
    }

    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }
}
