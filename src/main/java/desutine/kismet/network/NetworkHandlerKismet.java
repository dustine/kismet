package desutine.kismet.network;

import desutine.kismet.Kismet;
import desutine.kismet.ModLogger;
import desutine.kismet.Reference;
import desutine.kismet.addon.AddonJei;
import desutine.kismet.client.util.ClientTargetHelper;
import desutine.kismet.common.ConfigKismet;
import desutine.kismet.common.tile.TileDisplay;
import desutine.kismet.network.packet.MessageKismetConfig;
import desutine.kismet.network.packet.MessageReceiveEnrichedTargets;
import desutine.kismet.network.packet.MessageSendRawTargets;
import desutine.kismet.network.packet.MessageTileEntity;
import desutine.kismet.server.StackWrapper;
import desutine.kismet.server.WorldSavedDataTargets;
import desutine.kismet.util.StackHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public void enrichStacks(List<StackWrapper> targets, EntityPlayerMP player) {
        channel.sendTo(new MessageSendRawTargets(targets), player);
    }

    private void sendEnriched(List<StackWrapper> targets) {
        channel.sendToServer(new MessageReceiveEnrichedTargets(targets));
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
            Minecraft.getMinecraft().addScheduledTask(() -> ConfigKismet.clientSync(message.syncValues));
            return null;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class EnrichItemStacks implements IMessageHandler<MessageSendRawTargets, IMessage> {
        @Override
        public MessageSendRawTargets onMessage(MessageSendRawTargets message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                List<StackWrapper> targets = message.stacks;
                if (isJeiReady()) {
                    targets = AddonJei.enrich(message.stacks);
                } else {
                    targets = ClientTargetHelper.vanillaEnrich(message.stacks);
                }
                Kismet.network.sendEnriched(targets);
            });
            return null;
        }

        private boolean isJeiReady() {
            return AddonJei.recipeRegistry != null && AddonJei.stackHelper != null;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class ReceiveEnrichedItemStacks implements IMessageHandler<MessageReceiveEnrichedTargets, IMessage> {
        @Override
        public IMessage onMessage(final MessageReceiveEnrichedTargets message, MessageContext ctx) {
            final WorldServer world = (WorldServer) (ctx.getServerHandler().playerEntity.worldObj);
            world.addScheduledTask(() -> {
                final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
                if (Kismet.libraryFactory != null) {
                    final WorldSavedDataTargets wsdStacks = WorldSavedDataTargets.get(world);
                    int skipped = wsdStacks.enrichStacks(message.stacks);
                    Kismet.libraryFactory.recreateLibrary();

                    player.addChatMessage(
                            new TextComponentString(String.format("[Kismet] Added %d targets, mods:%s", message
                                    .stacks.size() - skipped, getMods(message.stacks))));
                }

                Kismet.libraryFactory.sendNextPacket(player);
            });
            return null;
        }

        private Set<String> getMods(List<StackWrapper> stacks) {
            final Set<String> mods = new HashSet<>();
            for (StackWrapper wrapper : stacks) {
                String mod = StackHelper.getMod(wrapper.getStack());
                mods.add(mod);
            }
            return mods;
        }
    }
}

