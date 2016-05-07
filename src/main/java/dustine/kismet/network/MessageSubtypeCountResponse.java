package dustine.kismet.network;

import dustine.kismet.network.message.MessageBase;
import dustine.kismet.server.command.CCBlock;
import dustine.kismet.server.command.CCForce;
import dustine.kismet.server.command.CCPardon;
import dustine.kismet.server.command.CCUnforce;
import dustine.kismet.util.StackHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import static dustine.kismet.server.command.CCSuperStackEntry.EnumCommandType;

public class MessageSubtypeCountResponse extends MessageBase<MessageSubtypeCountResponse> {
    private ItemStack item;
    private EnumCommandType type;
    private int size;

    public MessageSubtypeCountResponse() {}

    public MessageSubtypeCountResponse(ItemStack item, EnumCommandType type, int size) {
        this.item = item;
        this.type = type;
        this.size = size;
    }

    @Override public void fromBytes(ByteBuf buf) {
        this.item = ByteBufUtils.readItemStack(buf);
        this.type = EnumCommandType.values()[buf.readInt()];
        this.size = buf.readInt();
    }

    @Override public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeItemStack(buf, this.item);
        buf.writeInt(this.type.ordinal());
        buf.writeInt(this.size);
    }

    @Override protected void handleServerSide(MessageSubtypeCountResponse message, EntityPlayer player) {
        final String entry = StackHelper.toUniqueKey(message.item, message.size > 1);

        switch (message.type) {
            case BLOCK:
                CCBlock.staticProcessEntry(player, entry);
                break;
            case PARDON:
                CCPardon.staticProcessEntry(player, entry);
                break;
            case FORCE:
                CCForce.staticProcessEntry(player, entry);
                break;
            case UNFORCE:
                CCUnforce.staticProcessEntry(player, entry);
                break;
        }
    }

    @Override protected void handleClientSide(MessageSubtypeCountResponse message, EntityPlayer player) {}
}
