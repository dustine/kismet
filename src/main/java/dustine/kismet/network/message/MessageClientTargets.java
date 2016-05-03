package dustine.kismet.network.message;

import dustine.kismet.Kismet;
import dustine.kismet.client.target.ClientTargetSender;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTUtil;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.UUID;

public class MessageClientTargets extends MessageBase<MessageClientTargets> {
    private UUID id;

    public MessageClientTargets() {}

    public MessageClientTargets(UUID id) {
        this.id = id;
    }

    @Override public void fromBytes(ByteBuf buf) {
        this.id = NBTUtil.getUUIDFromTag(ByteBufUtils.readTag(buf));
    }

    @Override public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, NBTUtil.createUUIDTag(this.id));
    }

    @Override protected void handleServerSide(MessageClientTargets message, EntityPlayer player) {
        if (Kismet.databaseBuilder != null) {
            Kismet.databaseBuilder.finishBuilding(message.id, (EntityPlayerMP) player);
        }
    }

    @Override protected void handleClientSide(MessageClientTargets message, EntityPlayer player) {
        final ClientTargetSender sender = Kismet.network.clientTargetSender;
        if (sender == null || !sender.getId().equals(message.id)) {
            // start new request
            Kismet.network.clientTargetSender = new ClientTargetSender(message.id).invoke();
        } else {
            sender.invoke();
        }
    }
}
