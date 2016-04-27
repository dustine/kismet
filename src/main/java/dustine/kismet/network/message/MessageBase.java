package dustine.kismet.network.message;

import dustine.kismet.Kismet;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Based on MineMarteen's MessageBase, updated to 1.9 On the genius idea of making messages handle themselves
 */
public abstract class MessageBase<REQ extends IMessage> implements IMessage, IMessageHandler<REQ, IMessage> {
    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    @Override
    public IMessage onMessage(REQ message, MessageContext ctx) {
        if (ctx.side.isServer()) {
            final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            final WorldServer world = player.getServerWorld();
            world.addScheduledTask(() -> this.handleServerSide(message, player));
        } else {
            final EntityPlayer player = Kismet.proxy.tryGetEntityPlayerSP();
            if (player != null) {
                Minecraft.getMinecraft().addScheduledTask(() -> this.handleClientSide(message, player));
            }
        }
        return null;
    }

    /**
     * Handles a received REQ message on the server side
     *
     * @param message The received message
     * @param player  The player that sent the message
     */
    protected abstract void handleServerSide(REQ message, EntityPlayer player);

    /**
     * Handles a received REQ message on the client side
     *
     * @param message The received message
     * @param player
     */
    protected abstract void handleClientSide(REQ message, EntityPlayer player);
}
