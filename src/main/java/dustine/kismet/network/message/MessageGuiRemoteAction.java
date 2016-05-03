package dustine.kismet.network.message;

import dustine.kismet.block.BlockDisplay;
import dustine.kismet.item.ItemKey;
import dustine.kismet.item.ItemKismet;
import dustine.kismet.target.TargetHelper;
import dustine.kismet.tile.TileDisplay;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class MessageGuiRemoteAction extends MessageBase<MessageGuiRemoteAction> {
    private BlockPos pos;
    private int slotIndex;

    public MessageGuiRemoteAction() {
    }

    public MessageGuiRemoteAction(BlockPos pos, int slotIndex) {
        super();
        this.pos = pos;
        this.slotIndex = slotIndex;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        final NBTTagCompound posCompound = ByteBufUtils.readTag(buf);
        this.pos = NBTUtil.getPosFromTag(posCompound);
        this.slotIndex = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, NBTUtil.createPosTag(this.pos));
        buf.writeInt(this.slotIndex);
    }

    @Override
    protected void handleServerSide(MessageGuiRemoteAction message, EntityPlayer player) {
        ItemStack stack;
        if (message.slotIndex < 0) {
            // player holding stack
            stack = player.inventory.getItemStack();
        } else {
            // stack from player inventory
            final Slot slot = player.inventoryContainer.getSlot(message.slotIndex);
            stack = slot.getStack();
        }

        if (stack == null || message.pos == null) return;

        final World world = player.getEntityWorld();
        final TileEntity tile = world.getTileEntity(message.pos);

        if (tile != null && tile instanceof TileDisplay) {
            final TileDisplay display = (TileDisplay) tile;
            final BlockDisplay block = (BlockDisplay) display.getBlockType();

            if (TargetHelper.isEquivalent(display.getTarget(), stack)) {
                // fulfill target
                block.setTargetAsFulfilled(world, message.pos, world.getBlockState(message.pos), player);
            } else if (stack.getItem() instanceof ItemKismet) {
                // use key
                // after fulfilling target in case the key IS a target
                ItemKey.useKeyOnDisplay(stack, player, world, message.pos);

                // remove the key stack if it became empty
                if (stack.stackSize <= 0) {
                    if (message.slotIndex < 0) {
                        player.inventory.setItemStack(null);
                    } else {
                        player.inventoryContainer.putStackInSlot(message.slotIndex, null);
                    }
                }
            }
        }
    }

    @Override
    protected void handleClientSide(MessageGuiRemoteAction message, EntityPlayer player) {
    }
}
