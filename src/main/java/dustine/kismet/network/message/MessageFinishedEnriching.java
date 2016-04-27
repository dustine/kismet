package dustine.kismet.network.message;

import dustine.kismet.Kismet;
import dustine.kismet.target.TargetLibraryBuilder;
import dustine.kismet.world.savedata.WSDTargetDatabase;
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
    protected void handleServerSide(MessageFinishedEnriching message, EntityPlayer player) {
        if (Kismet.databaseBuilder != null) {
            final EntityPlayerMP playerMP = (EntityPlayerMP) player;
            final boolean sent = Kismet.databaseBuilder.sendNextPacket(playerMP);
            if (!sent) {
                TargetLibraryBuilder.build(WSDTargetDatabase.get(player.getEntityWorld()));
            }
        }
    }

    @Override
    protected void handleClientSide(MessageFinishedEnriching message, EntityPlayer player) {
    }
}
