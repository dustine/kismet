package dustine.kismet.network;

import dustine.kismet.Log;
import dustine.kismet.Reference;
import dustine.kismet.network.message.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NetworkHandler {
    private static int channelDiscriminator = 0;
    private final String name;
    private final SimpleNetworkWrapper channel;
    private int discriminator;

    public NetworkHandler() {
        // registering channel
        this.name = getChannelId();
        this.channel = NetworkRegistry.INSTANCE.newSimpleChannel(name);

        // registering messages
        this.channel.registerMessage(MessageDisplayTarget.class, MessageDisplayTarget.class, getDiscriminator(), Side.CLIENT);

        this.channel.registerMessage(MessageKeyUsage.class, MessageKeyUsage.class, getDiscriminator(), Side.CLIENT);

        this.channel.registerMessage(MessageServerConfig.class, MessageServerConfig.class, getDiscriminator(), Side.CLIENT);

        this.channel.registerMessage(MessageEnrichStacks.class, MessageEnrichStacks.class, getDiscriminator(), Side.CLIENT);
        this.channel.registerMessage(MessageEnrichStacks.class, MessageEnrichStacks.class, getDiscriminator(), Side.SERVER);

        this.channel.registerMessage(MessageFinishedEnriching.class, MessageFinishedEnriching.class, getDiscriminator(), Side.SERVER);

        // the discriminator can't go above 256
        if (discriminator >= 256)
            Log.error(String.format("Channel %s over maximum registered messages (should be %d, was %d)", name, 256, discriminator));
    }

    private int getDiscriminator() {
        return discriminator++;
    }

    private static String getChannelId() {
        return Reference.MOD_ID + '_' + channelDiscriminator++;
    }

    public String getName() {
        return name;
    }

    public void sendTo(MessageBase message, EntityPlayerMP player) {
        this.channel.sendTo(message, player);
    }

    public void sendToDimension(MessageBase message, int dimension) {
        this.channel.sendToDimension(message, dimension);
    }

    public void sendToServer(MessageBase message) {
        this.channel.sendToServer(message);
    }
}

