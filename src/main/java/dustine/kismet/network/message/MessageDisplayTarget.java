package dustine.kismet.network.message;

import dustine.kismet.Log;
import dustine.kismet.tile.TileDisplay;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;

/**
 * Sync Client Tile Display message
 * <p>
 * Includes: NBT data from server-side TileDisplay
 */
public class MessageDisplayTarget extends MessageBase<MessageDisplayTarget> {
    private NBTTagCompound compound;

    public MessageDisplayTarget() {
    }

    public MessageDisplayTarget(final TileDisplay display) {
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

    @Override
    protected void handleServerSide(MessageDisplayTarget message, EntityPlayer player) {

    }

    @Override
    protected void handleClientSide(MessageDisplayTarget message, EntityPlayer player) {
        int x, y, z;
        x = message.compound.getInteger("x");
        y = message.compound.getInteger("y");
        z = message.compound.getInteger("z");
        BlockPos pos = new BlockPos(x, y, z);

        TileEntity tile = Minecraft.getMinecraft().theWorld.getTileEntity(pos);
        if (tile == null || !(tile instanceof TileDisplay)) return;

        TileDisplay display = (TileDisplay) tile;
        try {
            display.readFromNBT(message.compound);
        } catch (Throwable te) {
            Log.error(te);
        }
    }
}
