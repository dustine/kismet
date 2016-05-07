package dustine.kismet.network;

import dustine.kismet.config.ConfigKismet;
import dustine.kismet.network.message.MessageBase;
import dustine.kismet.server.command.CommandKismet;
import dustine.kismet.target.TargetLibrary;
import dustine.kismet.util.StackHelper;
import dustine.kismet.world.savedata.WSDTargetDatabase;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.ArrayList;
import java.util.List;

import static dustine.kismet.network.message.MessageSubtypeCount.MessageCommandStackType;

public class MessageSubtypeCountResponse extends MessageBase<MessageSubtypeCountResponse> {
    private ItemStack item;
    private MessageCommandStackType type;
    private int size;

    public MessageSubtypeCountResponse() {}

    public MessageSubtypeCountResponse(ItemStack item, MessageCommandStackType type, int size) {
        this.item = item;
        this.type = type;
        this.size = size;
    }

    @Override public void fromBytes(ByteBuf buf) {
        this.item = ByteBufUtils.readItemStack(buf);
        this.type = MessageCommandStackType.values()[buf.readInt()];
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
                final List<String> genFilter = new ArrayList<>(ConfigKismet.getGenFilter());
                if (genFilter.contains(entry)) {
                    CommandKismet.sendError(player,
                            new TextComponentString("Blocked entry already present in gen target filter")
                    );
                    return;
                }
                ConfigKismet.addToGenFilter(entry);
                TargetLibrary.build(WSDTargetDatabase.get(player.getEntityWorld()));
                CommandKismet.send(player,
                        new TextComponentString(String.format("Added %s to gen target filter", entry))
                );
                break;
            case PARDON:
                break;
            case FORCE:
                break;
            case UNFORCE:
                break;
        }
    }

    @Override protected void handleClientSide(MessageSubtypeCountResponse message, EntityPlayer player) {}
}
