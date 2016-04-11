package desutine.kismet.network;

import desutine.kismet.Kismet;
import desutine.kismet.ModLogger;
import desutine.kismet.Reference;
import desutine.kismet.addon.AddonJei;
import desutine.kismet.client.util.ClientTargetHelper;
import desutine.kismet.common.KismetConfig;
import desutine.kismet.common.tile.TileDisplay;
import desutine.kismet.network.packet.MessageKismetConfig;
import desutine.kismet.network.packet.MessageReceiveEnrichedTargets;
import desutine.kismet.network.packet.MessageSendRawTargets;
import desutine.kismet.network.packet.MessageTileEntity;
import desutine.kismet.server.TargetsWorldSavedData.WrapperTarget;
import desutine.kismet.util.TargetHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import java.util.List;

public class NetworkHandlerKismet {
    private static int discriminator;
    private final SimpleNetworkWrapper channel;

    public NetworkHandlerKismet() {
        // registering channel
        channel = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MOD_ID);
        // registering messages
        channel.registerMessage(SyncClientTileDisplay.class, MessageTileEntity.class, getDiscriminator(),
                Side.CLIENT);
        channel.registerMessage(SendConfigToClient.class, MessageKismetConfig.class, getDiscriminator(), Side.CLIENT);
        channel.registerMessage(EnrichItemStacks.class, MessageSendRawTargets.class, getDiscriminator(), Side.CLIENT);
        channel.registerMessage(ReceiveEnrichedItemStacks.class, MessageReceiveEnrichedTargets.class,
                getDiscriminator(), Side.SERVER);
    }

    private static int getDiscriminator() {
        return discriminator++;
    }

    public void syncDisplayTargetToClient(int dimension, TileEntity tileEntity) {
        channel.sendToDimension(new MessageTileEntity(tileEntity), dimension);
    }

    public void sendConfigToClient(EntityPlayerMP player) {
        channel.sendTo(new MessageKismetConfig(), player);
    }

    public void enrichStacks(List<WrapperTarget> stacks, EntityPlayerMP player) {
        channel.sendTo(new MessageSendRawTargets(stacks), player);
    }

    private void sendEnriched(List<WrapperTarget> isObtainable) {
        channel.sendToServer(new MessageReceiveEnrichedTargets(isObtainable));
    }

    @SuppressWarnings("WeakerAccess")
    public static class SyncClientTileDisplay implements IMessageHandler<MessageTileEntity, IMessage> {
        @Override
        public IMessage onMessage(MessageTileEntity message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                int x, y, z;
                x = message.nbtTagCompound.getInteger("x");
                y = message.nbtTagCompound.getInteger("y");
                z = message.nbtTagCompound.getInteger("z");
                BlockPos pos = new BlockPos(x, y, z);

                TileEntity tileEntity = Minecraft.getMinecraft().theWorld.getTileEntity(pos);
                if (tileEntity == null) return;
                if (tileEntity instanceof TileDisplay) {
                    TileDisplay tileDisplay = (TileDisplay) tileEntity;
                    try {
                        tileDisplay.readFromNBT(message.nbtTagCompound);
                    } catch (Throwable te) {
                        ModLogger.error(te);
                    }
                }
            });
            return null;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class SendConfigToClient implements IMessageHandler<MessageKismetConfig, IMessage> {
        @Override
        public IMessage onMessage(MessageKismetConfig message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> KismetConfig.clientSync(message.syncValues));
            return null;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class EnrichItemStacks implements IMessageHandler<MessageSendRawTargets, IMessage> {
        @Override
        public MessageSendRawTargets onMessage(MessageSendRawTargets message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                List<WrapperTarget> isObtainable = message.stacks;
                if (isJeiReady()) {
                    AddonJei.enrich(isObtainable);
                } else {
                    ClientTargetHelper.vanillaEnrich(isObtainable);
                }
                Kismet.packetHandler.sendEnriched(isObtainable);
            });
            return null;
        }

        private boolean isJeiReady() {
            return true;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class ReceiveEnrichedItemStacks implements IMessageHandler<MessageReceiveEnrichedTargets, IMessage> {
        @Override
        public IMessage onMessage(final MessageReceiveEnrichedTargets message, MessageContext ctx) {
            ((WorldServer) (ctx.getServerHandler().playerEntity.worldObj)).addScheduledTask(() -> {
                for (WrapperTarget wrapper : message.stacks) {
//                    ModLogger.trace(StackHelper.stackToString(wrapper.getStack()));
                }
                TargetHelper.enrichItems(message.stacks);
            });
            return null;
        }
    }
}

