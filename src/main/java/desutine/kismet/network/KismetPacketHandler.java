package desutine.kismet.network;

import desutine.kismet.ModLogger;
import desutine.kismet.reference.Reference;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class KismetPacketHandler {

    public KismetPacketHandler() {
        // registering channel
        channel = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MODID);
        // registering messages
        channel.registerMessage(SyncClientTileDisplay.class, SyncTileEntityNBTMessage.class, getDiscriminator(),
                Side.CLIENT);
    }

    private final SimpleNetworkWrapper channel;
    private static int discriminator;

    public void syncDisplayTargetToClient(int dimension, TileEntity tileEntity){
        channel.sendToDimension(new SyncTileEntityNBTMessage(tileEntity), dimension);
    }

    private int getDiscriminator() {
        return discriminator++;
    }

    public static class SyncClientTileDisplay implements IMessageHandler<SyncTileEntityNBTMessage,
            IMessage> {
        @Override
        public IMessage onMessage(final SyncTileEntityNBTMessage message, MessageContext ctx) {
            if(message == null) return null;

            Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                @Override
                public void run() {
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
                }
            });
            return null;
        }
    }

    public static class SyncTileEntityNBTMessage implements IMessage {

        public NBTTagCompound nbtTagCompound;

        public SyncTileEntityNBTMessage() {
        }

        public SyncTileEntityNBTMessage(final TileEntity tileEntity) {
            this.nbtTagCompound = new NBTTagCompound();
            tileEntity.writeToNBT(nbtTagCompound);
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            nbtTagCompound = ByteBufUtils.readTag(buf);
        }

        @Override
        public void toBytes(ByteBuf buf) {
            ByteBufUtils.writeTag(buf, nbtTagCompound);
        }
    }
}

