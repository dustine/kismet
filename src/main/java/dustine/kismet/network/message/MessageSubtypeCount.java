package dustine.kismet.network.message;

import dustine.kismet.Kismet;
import dustine.kismet.client.target.ClientTargetHelper;
import dustine.kismet.network.MessageSubtypeCountResponse;
import dustine.kismet.server.command.CCSuperStackEntry;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class MessageSubtypeCount extends MessageBase<MessageSubtypeCount> {
    private ItemStack item;
    private CCSuperStackEntry.EnumCommandType type;

    public MessageSubtypeCount() {}

    public MessageSubtypeCount(ItemStack item, CCSuperStackEntry.EnumCommandType type) {
        super();
        this.item = item;
        this.type = type;
    }

    @Override public void fromBytes(ByteBuf buf) {
        this.item = ByteBufUtils.readItemStack(buf);
        this.type = CCSuperStackEntry.EnumCommandType.values()[buf.readInt()];
    }

    @Override public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeItemStack(buf, this.item);
        buf.writeInt(this.type.ordinal());
    }

    @Override protected void handleServerSide(MessageSubtypeCount message, EntityPlayer player) {}

    @Override protected void handleClientSide(MessageSubtypeCount message, EntityPlayer player) {
        final int size = ClientTargetHelper.getSubtypes(message.item.getItem()).size();
        Kismet.network.sendToServer(new MessageSubtypeCountResponse(message.item, message.type, size));
    }

}
