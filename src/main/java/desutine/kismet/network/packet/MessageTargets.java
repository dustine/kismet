package desutine.kismet.network.packet;

import desutine.kismet.server.StackWrapper;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.ArrayList;
import java.util.List;

public class MessageTargets implements IMessage {
    public List<StackWrapper> stacks;

    MessageTargets() {
    }

    MessageTargets(List<StackWrapper> stacks) {
        this.stacks = stacks;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        stacks = new ArrayList<>();
        int stacksSize = buf.readInt();
        for (int i = 0; i < stacksSize; i++) {
            ItemStack stack = ByteBufUtils.readItemStack(buf);
            boolean obtainable = buf.readBoolean();
            stacks.add(new StackWrapper(stack, obtainable));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(stacks.size());
        for (StackWrapper wrapper : stacks) {
            ByteBufUtils.writeItemStack(buf, wrapper.getStack());
            buf.writeBoolean(wrapper.isObtainable());
        }
    }
}
