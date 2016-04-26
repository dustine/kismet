package dustine.kismet.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Sync Client Tile Display message
 * <p>
 * Includes: NBT data from server-side TileDisplay
 */
public class SCTDMessage implements IMessage {
    public NBTTagCompound nbtTagCompound;

    public SCTDMessage() {
    }

    public SCTDMessage(final TileEntity tileEntity) {
        this.nbtTagCompound = new NBTTagCompound();
        tileEntity.writeToNBT(nbtTagCompound);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        nbtTagCompound = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, nbtTagCompound);
    }
}
