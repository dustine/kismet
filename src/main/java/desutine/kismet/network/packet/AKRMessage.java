package desutine.kismet.network.packet;

import desutine.kismet.item.ItemKey;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Attempt Key Usage message Notifies the server to try to use the Includes: the new value for skipped goals
 */
public class AKRMessage implements IMessage {
    public BlockPos pos;
    public ItemStack key;

    @SuppressWarnings("unused")
    public AKRMessage() {
    }

    public AKRMessage(BlockPos pos, ItemStack key) {
        this.pos = pos;
        if (!(key.getItem() instanceof ItemKey)) {
            throw new IllegalArgumentException(key.toString());
        }
        this.key = key;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        final NBTTagCompound compound = ByteBufUtils.readTag(buf);
        this.pos = NBTUtil.getPosFromTag(compound);
        this.key = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, NBTUtil.createPosTag(this.pos));
        ByteBufUtils.writeItemStack(buf, key);
    }
}
