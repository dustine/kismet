package desutine.kismet.network;

import desutine.kismet.ModLogger;
import desutine.kismet.common.config.ConfigKismet;
import desutine.kismet.network.packet.MessageKismetConfig;
import desutine.kismet.network.packet.MessageTileEntity;
import desutine.kismet.reference.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class KismetPacketHandler {
    private static int discriminator;
    private final SimpleNetworkWrapper channel;
    public KismetPacketHandler() {
        // registering channel
        channel = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MODID);
        // registering messages
        channel.registerMessage(SyncClientTileDisplay.class, MessageTileEntity.class, getDiscriminator(),
                Side.CLIENT);
        channel.registerMessage(SendConfigToClient.class, MessageKismetConfig.class, getDiscriminator(), Side.CLIENT);
    }

    private int getDiscriminator() {
        return discriminator++;
    }

    public void syncDisplayTargetToClient(int dimension, TileEntity tileEntity) {
        channel.sendToDimension(new MessageTileEntity(tileEntity), dimension);
    }

    public void sendConfigToClient(EntityPlayerMP player) {
        channel.sendTo(new MessageKismetConfig(), player);
    }

    @SuppressWarnings("WeakerAccess")
    public static class SyncClientTileDisplay implements IMessageHandler<MessageTileEntity, IMessage> {
        @Override
        public IMessage onMessage(final MessageTileEntity message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                int x, y, z;
                x = message.nbtTagCompound.getInteger("x");
                y = message.nbtTagCompound.getInteger("y");
                z = message.nbtTagCompound.getInteger("z");
                BlockPos pos = new BlockPos(x, y, z);

                TileEntity tileEntity = Minecraft.getMinecraft().theWorld.getTileEntity(pos);
                if (tileEntity == null) return;

                try {
                    tileEntity.readFromNBT(message.nbtTagCompound);
                } catch (Throwable te) {
                    ModLogger.error(te);
                }
            });
            return null;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class SendConfigToClient implements IMessageHandler<MessageKismetConfig, IMessage> {
        @Override
        public IMessage onMessage(MessageKismetConfig message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> ConfigKismet.clientSync(message.syncValues));
            return null;
        }
    }
}

