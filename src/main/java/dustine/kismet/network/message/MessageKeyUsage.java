package dustine.kismet.network.message;

import dustine.kismet.util.SoundHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;

/**
 * Report back to client the success/failure of using the key
 * <p>
 * Includes: success flag
 */
public class MessageKeyUsage extends MessageBase<MessageKeyUsage> {
    private boolean success;
    private BlockPos pos;

    public MessageKeyUsage() {
    }

    public MessageKeyUsage(BlockPos pos, boolean success) {
        this.pos = pos;
        this.success = success;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = NBTUtil.getPosFromTag(ByteBufUtils.readTag(buf));
        this.success = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, NBTUtil.createPosTag(this.pos));
        buf.writeBoolean(this.success);
    }

    @Override
    protected void handleServerSide(MessageKeyUsage message, EntityPlayer player) {

    }

    @Override
    protected void handleClientSide(MessageKeyUsage message, EntityPlayer player) {
        SoundHelper.onKeyUsage(Minecraft.getMinecraft().theWorld, Minecraft.getMinecraft().thePlayer,
                message.pos, message.success);
    }
}
