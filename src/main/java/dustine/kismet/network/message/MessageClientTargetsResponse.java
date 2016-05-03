package dustine.kismet.network.message;

import dustine.kismet.Kismet;
import dustine.kismet.target.InformedStack;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Enrich Stack List message
 * <p>
 * Includes: a InformedStack list to be enriched clientside
 */
public class MessageClientTargetsResponse extends MessageBase<MessageClientTargetsResponse> {
    private List<InformedStack> stacks;
    private UUID id;

    @SuppressWarnings("unused") public MessageClientTargetsResponse() {}

    public MessageClientTargetsResponse(List<InformedStack> stacks, UUID id) {
        this.stacks = stacks;
        this.id = id;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.id = NBTUtil.getUUIDFromTag(ByteBufUtils.readTag(buf));
        this.stacks = new ArrayList<>();
        while (buf.isReadable()) {
            final NBTTagCompound compound = ByteBufUtils.readTag(buf);
            final InformedStack wrapper = new InformedStack(compound);
            this.stacks.add(wrapper);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, NBTUtil.createUUIDTag(this.id));
        for (InformedStack wrapper : this.stacks) {
            NBTTagCompound compound = wrapper.serializeNBT();
            ByteBufUtils.writeTag(buf, compound);
        }
    }

    @Override
    protected void handleServerSide(MessageClientTargetsResponse message, EntityPlayer player) {
        if (Kismet.databaseBuilder != null) {
            if (Kismet.databaseBuilder.receiveClientTargets(message.stacks, message.id, (EntityPlayerMP) player))
                Kismet.network.sendTo(new MessageClientTargets(message.id), (EntityPlayerMP) player);
        }
    }

    @Override
    protected void handleClientSide(MessageClientTargetsResponse message, EntityPlayer player) {}
}
