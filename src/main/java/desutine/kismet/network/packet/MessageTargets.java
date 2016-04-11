package desutine.kismet.network.packet;

import desutine.kismet.server.TargetsWorldSavedData.WrapperTarget;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.ArrayList;
import java.util.List;

public class MessageTargets implements IMessage {
    public List<WrapperTarget> stacks;

    MessageTargets(List<WrapperTarget> stacks) {
        this.stacks = stacks;
    }

    MessageTargets() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int isObtainableSize = buf.readInt();
        stacks = new ArrayList<>();
        for (int i = 0; i < isObtainableSize; i++) {
            ItemStack stack = ByteBufUtils.readItemStack(buf);
            boolean obtainable = buf.readBoolean();
            stacks.add(new WrapperTarget(stack, obtainable));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(stacks.size());
        for (WrapperTarget wrapper : stacks) {
            ByteBufUtils.writeItemStack(buf, wrapper.getStack());
            buf.writeBoolean(wrapper.isObtainable());
        }
    }
}
