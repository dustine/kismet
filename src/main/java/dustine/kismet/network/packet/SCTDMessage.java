package dustine.kismet.network.packet;

import dustine.kismet.tile.TileDisplay;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Sync Client Tile Display message
 * <p>
 * Includes: NBT data from server-side TileDisplay
 */
public class SCTDMessage implements IMessage {
    public NBTTagCompound compound;

    public SCTDMessage() {
    }

    public SCTDMessage(final TileDisplay display) {
        this.compound = new NBTTagCompound();
        display.writeToNBT(compound);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        compound = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, compound);
    }
}
