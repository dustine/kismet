package dustine.kismet.client.target;

import dustine.kismet.Kismet;
import dustine.kismet.Log;
import dustine.kismet.addon.AddonJei;
import dustine.kismet.network.message.MessageClientTargets;
import dustine.kismet.network.message.MessageClientTargetsResponse;
import dustine.kismet.target.InformedStack;
import net.minecraft.nbt.CompressedStreamTools;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class ClientTargetSender {
    private final List<InformedStack> stacks;
    private final UUID id;

    public ClientTargetSender(UUID id) {
        this.id = id;
        this.stacks = new ArrayList<>(ClientTargetHelper.unfoldSubtypes().values());
    }

    public UUID getId() {
        return this.id;
    }

    public ClientTargetSender invoke() {
        if (this.stacks.isEmpty()) {
            Kismet.network.sendToServer(new MessageClientTargets(this.id));
        } else {
            final List<InformedStack> toSend = new ArrayList<>();
            final CountingOutputStream counter = new CountingOutputStream();
            final DataOutputStream stream = new DataOutputStream(counter);
            final Iterator<InformedStack> itr = this.stacks.iterator();

            while (itr.hasNext()) {
                final InformedStack stack = itr.next();

                // add crafting flags
                if (Kismet.instance.isJeiLoaded()) {
                    AddonJei.setCraftingFlags(stack);
                } else {
                    ClientTargetHelper.setCraftingFlags(stack);
                }

                try {
                    CompressedStreamTools.writeCompressed(stack.writeToNBT(), stream);
                    if (counter.getCount() >= Short.MAX_VALUE) {
                        if (toSend.isEmpty()) {
                            // ... we haven't added any to toSend, this single stack is BIGGER than the limit
                            Log.error(String.format("This stack is just too damn big to send by network: %s", stack));
                            itr.remove();
                        }
                        // limit reached, break and send
                        break;
                    }

                    // add to toSend
                    toSend.add(stack);
                } catch (IOException e) {
                    Log.error(stack.toString(), e);
                }

                itr.remove();
            }

            Kismet.network.sendToServer(new MessageClientTargetsResponse(toSend, this.id));
        }

        return this;
    }

    // http://stackoverflow.com/questions/19852460/get-size-of-string-w-encoding-in-bytes-without-converting-to-byte
    private class CountingOutputStream extends OutputStream {
        private int count;

        @Override public void write(int i) {
            ++this.count;
        }

        @Override public void write(byte[] b) {
            if (b == null)
                throw new NullPointerException();
            this.count += b.length;
        }

        @Override public void write(byte[] b, int offset, int len) {
            if (b == null)
                throw new NullPointerException();
            this.count += len;
        }

        int getCount() {
            return this.count;
        }

        void reset() {
            this.count = 0;
        }
    }
}
