package dustine.kismet.network.message;

import dustine.kismet.Kismet;
import dustine.kismet.addon.AddonJei;
import dustine.kismet.client.util.ClientTargetHelper;
import dustine.kismet.target.InformedStack;
import dustine.kismet.world.savedata.WSDTargetDatabase;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Enrich Stack List message
 * <p>
 * Includes: a InformedStack list to be enriched clientside
 */
public class MessageEnrichStacks extends MessageBase<MessageEnrichStacks> {
    private List<InformedStack> stacks;

    public MessageEnrichStacks() {
    }

    public MessageEnrichStacks(List<InformedStack> stacks) {
        this.stacks = stacks;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.stacks = new ArrayList<>();
        int stacksSize = buf.readInt();
        for (int i = 0; i < stacksSize; i++) {
            final NBTTagCompound compound = ByteBufUtils.readTag(buf);
            final InformedStack wrapper = new InformedStack(compound);
            this.stacks.add(wrapper);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.stacks.size());
        for (InformedStack wrapper : this.stacks) {
            NBTTagCompound compound = wrapper.serializeNBT();
            ByteBufUtils.writeTag(buf, compound);
        }
    }

    @Override
    protected void handleServerSide(MessageEnrichStacks message, EntityPlayer player) {
        if (Kismet.databaseBuilder != null) {
            final WSDTargetDatabase wsdStacks = WSDTargetDatabase.get(player.worldObj);
            wsdStacks.enrichStacks(message.stacks);
        }
    }

    @Override
    protected void handleClientSide(MessageEnrichStacks message, EntityPlayer player) {
        for (InformedStack stack : message.stacks) {
            List<InformedStack> targets;
            if (Kismet.instance.isJeiLoaded()) {
                targets = AddonJei.enrich(stack);
            } else {
                targets = ClientTargetHelper.vanillaEnrich(stack);
            }
            targets.forEach(InformedStack::seal);
            Kismet.network.sendToServer(new MessageEnrichStacks(targets));
        }
        Kismet.network.sendToServer(new MessageFinishedEnriching());
    }
}
