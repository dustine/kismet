package desutine.kismet.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Generate Skipped Target message
 * Notifies the server to generate a new target because of a successful keyroll
 * Includes: the new value for skipped goals
 */
public class GSTMessage implements IMessage {
    public BlockPos pos;
    public int skipped;

    @SuppressWarnings("unused")
    public GSTMessage() {
    }

    public GSTMessage(BlockPos pos, int skipped) {
        this.pos = pos;
        this.skipped = skipped;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        final NBTTagCompound compound = ByteBufUtils.readTag(buf);
        pos = NBTUtil.getPosFromTag(compound);
        skipped = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, NBTUtil.createPosTag(pos));
        buf.writeInt(skipped);
    }
}
