package dustine.kismet.network.message;

import dustine.kismet.Kismet;
import dustine.kismet.Log;
import dustine.kismet.server.command.CommandKismet;
import dustine.kismet.target.TargetLibraryBuilder;
import dustine.kismet.world.savedata.TargetDatabaseBuilder;
import dustine.kismet.world.savedata.WSDTargetDatabase;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Finish Enriching Stacks message
 * <p>
 * Includes: nothing, as it's just an empty message to signal an event ^^"
 */
public class MessageFinishedEnriching extends MessageBase<MessageFinishedEnriching> {
    public MessageFinishedEnriching() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    @Override
    protected void handleServerSide(MessageFinishedEnriching message, EntityPlayer player) {
        if (Kismet.databaseBuilder != null) {
            final EntityPlayerMP playerMP = (EntityPlayerMP) player;
            final boolean sent = Kismet.databaseBuilder.sendNextPacket(playerMP);
            if (!sent) {
                if (TargetDatabaseBuilder.isCommand()) {
                    CommandKismet.send(player, "Finished! Refreshing target library now...");
                }
                Log.info("Build target database");
                TargetLibraryBuilder.build(WSDTargetDatabase.get(player.getEntityWorld()));
                if (TargetDatabaseBuilder.isCommand()) {
                    CommandKismet.send(player, "Done! Database reset finished.");
                }
            }
        }
    }

    @Override
    protected void handleClientSide(MessageFinishedEnriching message, EntityPlayer player) {
    }
}
