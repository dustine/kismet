package dustine.kismet.network.message;

import dustine.kismet.Kismet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;

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
        if (Kismet.libraryFactory != null) {
            final boolean sent = Kismet.libraryFactory.sendNextPacket((EntityPlayerMP) player);
            if (!sent) {
                Kismet.libraryFactory.recreateLibrary();
                player.addChatMessage(
                        new TextComponentString("[Kismet] Finished resetting library!"));
            }
        }
    }

    @Override
    protected void handleClientSide(MessageFinishedEnriching message, EntityPlayer player) {
    }
}
