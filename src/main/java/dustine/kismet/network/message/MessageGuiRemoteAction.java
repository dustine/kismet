package dustine.kismet.network.message;

import dustine.kismet.block.BlockDisplay;
import dustine.kismet.tile.TileDisplay;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class MessageGuiRemoteAction extends MessageBase<MessageGuiRemoteAction> {
    private BlockPos pos;
    private ItemStack stack;

    public MessageGuiRemoteAction() {
    }

    public MessageGuiRemoteAction(BlockPos pos, ItemStack stack) {
        super();
        this.pos = pos;
        this.stack = stack;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        final NBTTagCompound posCompound = ByteBufUtils.readTag(buf);
        this.pos = NBTUtil.getPosFromTag(posCompound);
        final NBTTagCompound stackCompound = ByteBufUtils.readTag(buf);
        this.stack = ItemStack.loadItemStackFromNBT(stackCompound);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, NBTUtil.createPosTag(this.pos));
        ByteBufUtils.writeTag(buf, this.stack.serializeNBT());
    }

    @Override
    protected void handleServerSide(MessageGuiRemoteAction message, EntityPlayer player) {
        final World world = player.getEntityWorld();
        final TileEntity tile = world.getTileEntity(message.pos);
        if (tile != null && tile instanceof TileDisplay) {
            final TileDisplay display = (TileDisplay) tile;
            final BlockDisplay block = (BlockDisplay) display.getBlockType();

            block.tryFulfillTarget(world, message.pos, world.getBlockState(message.pos), player, message.stack, display);
        }
    }

    @Override
    protected void handleClientSide(MessageGuiRemoteAction message, EntityPlayer player) {
    }
}
