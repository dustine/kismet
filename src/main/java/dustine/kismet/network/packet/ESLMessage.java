package dustine.kismet.network.packet;

import dustine.kismet.target.InformedStack;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Enrich Stack List message Includes: a InformedStack list to be enriched clientside
 */
public class ESLMessage implements IMessage {
    public List<InformedStack> stacks;

    public ESLMessage() {
    }

    public ESLMessage(List<InformedStack> stacks) {
        this.stacks = stacks;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        stacks = new ArrayList<>();
        int stacksSize = buf.readInt();
        for (int i = 0; i < stacksSize; i++) {
            final NBTTagCompound compound = ByteBufUtils.readTag(buf);
            final InformedStack wrapper = new InformedStack(compound);
            stacks.add(wrapper);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(stacks.size());
        for (InformedStack wrapper : stacks) {
            NBTTagCompound compound = wrapper.serializeNBT();
            ByteBufUtils.writeTag(buf, compound);
        }
    }
}
