package dustine.kismet.network;

import dustine.kismet.Kismet;
import dustine.kismet.ModLogger;
import dustine.kismet.Reference;
import dustine.kismet.addon.AddonJei;
import dustine.kismet.block.BlockDisplay;
import dustine.kismet.client.util.ClientTargetHelper;
import dustine.kismet.network.packet.*;
import dustine.kismet.target.InformedStack;
import dustine.kismet.tile.TileDisplay;
import dustine.kismet.util.SoundHelper;
import dustine.kismet.world.savedata.WSDTargetDatabase;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
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

import java.util.List;

public class NetworkHandlerKismet {
    private static int discriminator;
    private final SimpleNetworkWrapper channel;

    public NetworkHandlerKismet() {
        // registering channel
        this.channel = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MOD_ID);
        // registering messages
        this.channel.registerMessage(SyncClientTileDisplay.class, SCTDMessage.class, getDiscriminator(), Side.CLIENT);
        this.channel.registerMessage(AttemptKeyRoll.class, AKRMessage.class, getDiscriminator(), Side.SERVER);
        this.channel.registerMessage(KeyRollResponse.class, KRRMessage.class, getDiscriminator(), Side.CLIENT);

        this.channel.registerMessage(SendConfigToClient.class, SCTCMessage.class, getDiscriminator(), Side.CLIENT);

        this.channel.registerMessage(EnrichStackList.class, ESLMessage.class, getDiscriminator(), Side.CLIENT);
        this.channel.registerMessage(ReceiveEnrichedStacks.class, RESMessage.class, getDiscriminator(), Side.SERVER);
        this.channel.registerMessage(FinishedEnrichingStacks.class, FESMessage.class, getDiscriminator(), Side.SERVER);
    }

    private static int getDiscriminator() {
        return discriminator++;
    }

    public void syncDisplayTargetToClient(TileDisplay display) {
        int dimension = display.getWorld().provider.getDimension();
        this.channel.sendToDimension(new SCTDMessage(display), dimension);
    }

    public void sendConfigToClient(EntityPlayerMP player) {
        this.channel.sendTo(new SCTCMessage(), player);
    }

    public void enrichStacks(List<InformedStack> targets, EntityPlayerMP player) {
        this.channel.sendTo(new ESLMessage(targets), player);
    }

    public void attemptKeyUsage(TileDisplay te, ItemStack key) {
        this.channel.sendToServer(new AKRMessage(te.getPos(), key));
    }

    @SuppressWarnings("WeakerAccess")
    public static class SyncClientTileDisplay implements IMessageHandler<SCTDMessage, IMessage> {
        @Override
        public IMessage onMessage(SCTDMessage message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                int x, y, z;
                x = message.compound.getInteger("x");
                y = message.compound.getInteger("y");
                z = message.compound.getInteger("z");
                BlockPos pos = new BlockPos(x, y, z);

                TileEntity tileEntity = Minecraft.getMinecraft().theWorld.getTileEntity(pos);
                if (tileEntity == null) return;
                if (tileEntity instanceof TileDisplay) {
                    TileDisplay tileDisplay = (TileDisplay) tileEntity;
                    try {
                        tileDisplay.readFromNBT(message.compound);
                    } catch (Throwable te) {
                        ModLogger.error(te);
                    }
                }
            });
            return null;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class SendConfigToClient implements IMessageHandler<SCTCMessage, IMessage> {
        @Override
        public IMessage onMessage(SCTCMessage message, MessageContext ctx) {
//            Minecraft.getMinecraft().addScheduledTask(() -> ConfigKismet.clientSync(message.syncValues));
            return null;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class EnrichStackList implements IMessageHandler<ESLMessage, IMessage> {
        @Override
        public ESLMessage onMessage(ESLMessage message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                for (InformedStack stack : message.stacks) {
                    List<InformedStack> targets;
                    if (Kismet.instance.isJeiLoaded()) {
                        targets = AddonJei.enrich(stack);
                    } else {
                        targets = ClientTargetHelper.vanillaEnrich(stack);
                    }
                    targets.forEach(InformedStack::seal);
                    Kismet.network.channel.sendToServer(new RESMessage(targets));
                }
                Kismet.network.channel.sendToServer(new FESMessage());
            });
            return null;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class ReceiveEnrichedStacks implements IMessageHandler<RESMessage, IMessage> {
        @Override
        public IMessage onMessage(final RESMessage message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            final WorldServer world = (WorldServer) (player.worldObj);
            world.addScheduledTask(() -> {
                if (Kismet.libraryFactory != null) {
                    final WSDTargetDatabase wsdStacks = WSDTargetDatabase.get(world);
                    wsdStacks.enrichStacks(message.stacks);
                }
            });
            return null;
        }

    }

    @SuppressWarnings("WeakerAccess")
    public static class FinishedEnrichingStacks implements IMessageHandler<FESMessage, IMessage> {
        @Override
        public IMessage onMessage(FESMessage message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            final WorldServer world = (WorldServer) (player.worldObj);
            world.addScheduledTask(() -> {
                if (Kismet.libraryFactory != null) {
                    final boolean sent = Kismet.libraryFactory.sendNextPacket(player);
                    if (!sent) {
                        Kismet.libraryFactory.recreateLibrary();
                        player.addChatMessage(
                                new TextComponentString("[Kismet] Finished resetting library!"));
                    }
                }
            });
            return null;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class AttemptKeyRoll implements IMessageHandler<AKRMessage, IMessage> {
        @Override
        public IMessage onMessage(AKRMessage message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            final WorldServer world = (WorldServer) (player.worldObj);
            world.addScheduledTask(() -> {
                final TileDisplay tile = (TileDisplay) world.getTileEntity(message.pos);
                if (tile == null) return;

                final boolean fulfilled = world.getBlockState(message.pos)
                        .getValue(BlockDisplay.FULFILLED);
                // if fulfilled, count as a success
                final boolean success = fulfilled || tile.rollForKey();

                if (success) {
                    // fulfilled rerolls don't count as a skip
                    if (!fulfilled)
                        tile.setSkipped(tile.getSkipped() + 1);
                    tile.getNewTarget();
                }

                Kismet.network.channel.sendTo(new KRRMessage(success), player);
                SoundHelper.onKeyUsage(player, success);
            });
            return null;
        }
    }

    public static class KeyRollResponse implements IMessageHandler<KRRMessage, IMessage> {
        @Override
        public IMessage onMessage(KRRMessage message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> SoundHelper.onKeyUsage(Minecraft.getMinecraft().thePlayer, message.success));
            return null;
        }
    }

}

