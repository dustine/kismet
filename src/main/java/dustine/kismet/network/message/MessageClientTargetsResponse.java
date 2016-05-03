package dustine.kismet.network.message;

import dustine.kismet.Kismet;
import dustine.kismet.Log;
import dustine.kismet.target.Target;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessageClientTargetsResponse extends MessageBase<MessageClientTargetsResponse> {
    private List<Target> targets;
    private UUID id;

    @SuppressWarnings("unused") public MessageClientTargetsResponse() {}

    public MessageClientTargetsResponse(List<Target> targets, UUID id) {
        this.targets = targets;
        this.id = id;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.id = NBTUtil.getUUIDFromTag(ByteBufUtils.readTag(buf));
        this.targets = new ArrayList<>();
        while (buf.isReadable()) {
            final NBTTagCompound compound = ByteBufUtils.readTag(buf);
            final Target target = new Target(compound);
            this.targets.add(target);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, NBTUtil.createUUIDTag(this.id));
        for (Target target : this.targets) {
            NBTTagCompound compound = target.serializeNBT();
            ByteBufUtils.writeTag(buf, compound);
        }
        Log.info(buf.array().length + " " + targets.size());
    }

    @Override
    protected void handleServerSide(MessageClientTargetsResponse message, EntityPlayer player) {
        if (Kismet.databaseBuilder != null) {
            if (Kismet.databaseBuilder.receiveClientTargets(message.targets, message.id, (EntityPlayerMP) player))
                Kismet.network.sendTo(new MessageClientTargets(message.id), (EntityPlayerMP) player);
        }
    }

    @Override
    protected void handleClientSide(MessageClientTargetsResponse message, EntityPlayer player) {}
}
