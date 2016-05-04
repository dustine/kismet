package dustine.kismet.client.target;

import dustine.kismet.Kismet;
import dustine.kismet.Log;
import dustine.kismet.addon.AddonJei;
import dustine.kismet.network.message.MessageClientTargets;
import dustine.kismet.network.message.MessageClientTargetsResponse;
import dustine.kismet.target.Target;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTUtil;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class ClientTargetSender {
    private final List<Target> targets;
    private final UUID id;

    public ClientTargetSender(UUID id) {
        this.id = id;
        this.targets = new ArrayList<>(ClientTargetHelper.unfoldSubtypes().values());
    }

    public UUID getId() {
        return this.id;
    }

    public ClientTargetSender invoke() {
        if (this.targets.isEmpty()) {
            Kismet.network.sendToServer(new MessageClientTargets(this.id));
        } else {
            final List<Target> toSend = new ArrayList<>();
            final CountingOutputStream counter = new CountingOutputStream();
            final DataOutputStream stream = new DataOutputStream(counter);
            final Iterator<Target> itr = this.targets.iterator();

            try {
                CompressedStreamTools.write(NBTUtil.createUUIDTag(this.id), stream);
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (itr.hasNext()) {
                final Target target = itr.next();

                // add crafting flags
                if (Kismet.instance.isJeiLoaded()) {
                    AddonJei.setCraftingFlags(target);
                }

                try {
                    CompressedStreamTools.write(target.writeToNBT(), stream);
                    if (counter.getCount() >= Short.MAX_VALUE) {
                        // client -> servers have a max size of Short.MAX_VALUE, in bytes
                        // so we add targets until we're juuuust over the min size
                        if (toSend.isEmpty()) {
                            // ... we haven't added any to toSend, this single target is BIGGER than the limit
                            Log.error(String.format("This target is just too damn big to send by network: %s", target));
                            itr.remove();
                        }
                        // limit reached, break and send
                        break;
                    }

                    // add to toSend
                    toSend.add(target);
                } catch (IOException e) {
                    Log.error(target.toString(), e);
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
