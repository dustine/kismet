package dustine.kismet.network.packet;

import dustine.kismet.target.InformedStack;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Receive Some Enriched Stacks message Includes: List of Informed Stacks and their correspondent key (obtained from
 * StackHelper) Note: sending the hasSubtypes is necessary because server side ItemStack.getHasSubtypes doesn't account
 * for variants differing singly by NBT
 */
public class RESMessage implements IMessage {
    public List<InformedStack> stacks;

    public RESMessage() {
        super();
    }

    public RESMessage(List<InformedStack> stacks) {
        this.stacks = new ArrayList<>(stacks);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        stacks = new ArrayList<>();
        int stacksSize = buf.readInt();
        for (int i = 0; i < stacksSize; i++) {
//            String key = ByteBufUtils.readUTF8String(buf);
            final NBTTagCompound compound = ByteBufUtils.readTag(buf);
            final InformedStack wrapper = new InformedStack(compound);
            stacks.add(wrapper);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(stacks.size());
        stacks.forEach(wrapper -> {
//            ByteBufUtils.writeUTF8String(buf, key);
            NBTTagCompound compound = wrapper.serializeNBT();
            ByteBufUtils.writeTag(buf, compound);
        });
    }
}
