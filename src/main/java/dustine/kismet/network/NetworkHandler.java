package dustine.kismet.network;

import dustine.kismet.Log;
import dustine.kismet.client.target.ClientTargetSender;
import dustine.kismet.network.message.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NetworkHandler {
    private final String name;
    private final SimpleNetworkWrapper channel;
    public ClientTargetSender clientTargetSender;
    private int discriminator;

    public NetworkHandler(String name) {
        // registering channel
        this.name = name;
        this.channel = NetworkRegistry.INSTANCE.newSimpleChannel(name);

        // registering messages
        this.channel.registerMessage(MessageGuiRemoteAction.class, MessageGuiRemoteAction.class,
                getDiscriminator(), Side.SERVER);

        this.channel.registerMessage(MessageKeyUsage.class, MessageKeyUsage.class,
                getDiscriminator(), Side.CLIENT);

        this.channel.registerMessage(MessageServerConfig.class, MessageServerConfig.class,
                getDiscriminator(), Side.CLIENT);

        this.channel.registerMessage(MessageClientTargets.class, MessageClientTargets.class,
                getDiscriminator(), Side.CLIENT);
        this.channel.registerMessage(MessageClientTargets.class, MessageClientTargets.class,
                getDiscriminator(), Side.SERVER);
        this.channel.registerMessage(MessageClientTargetsResponse.class, MessageClientTargetsResponse.class,
                getDiscriminator(), Side.SERVER);

        // the discriminator can't go above 256
        if (this.discriminator >= 256)
            Log.error(String.format("Channel %s over maximum registered messages (should be %d, was %d)", this.name,
                    256, this.discriminator));
    }

    private int getDiscriminator() {
        return this.discriminator++;
    }

    public String getName() {
        return this.name;
    }

    public void sendTo(MessageBase message, EntityPlayerMP player) {
        this.channel.sendTo(message, player);
    }

    public void sendToDimension(MessageBase message, int dimension) {
        this.channel.sendToDimension(message, dimension);
    }

    public void sendToAll(MessageBase message) {
        this.channel.sendToAll(message);
    }

    public void sendToServer(MessageBase message) {
        this.channel.sendToServer(message);
    }
}

