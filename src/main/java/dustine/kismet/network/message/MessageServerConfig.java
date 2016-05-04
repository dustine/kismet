package dustine.kismet.network.message;

import dustine.kismet.config.ConfigCopy;
import dustine.kismet.config.ConfigKismet;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;

/**
 * Send Config to Client message
 * <p>
 * Includes: Common configuration that needs to be synced between dedicated server and clients, such as timedLimit
 */
public class MessageServerConfig extends MessageBase<MessageServerConfig> {
    private ConfigCopy configCopy;

    public MessageServerConfig() {
    }

    public MessageServerConfig(ConfigCopy configCopy) {
        this.configCopy = configCopy;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        if (buf.readBoolean()) {
            this.configCopy = new ConfigCopy();
            this.configCopy.deserializeNBT(ByteBufUtils.readTag(buf));
        } else {
            this.configCopy = null;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(configCopy != null);
        if (configCopy != null) ByteBufUtils.writeTag(buf, configCopy.serializeNBT());
    }

    @Override
    protected void handleServerSide(MessageServerConfig message, EntityPlayer player) {
    }

    @Override
    protected void handleClientSide(MessageServerConfig message, EntityPlayer player) {
        ConfigKismet.clientSync(message.configCopy);
    }
}
