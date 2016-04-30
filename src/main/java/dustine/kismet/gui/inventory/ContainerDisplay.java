package dustine.kismet.gui.inventory;

import dustine.kismet.Kismet;
import dustine.kismet.item.ItemKey;
import dustine.kismet.network.message.MessageGuiRemoteAction;
import dustine.kismet.tile.TileDisplay;
import dustine.kismet.util.SoundHelper;
import dustine.kismet.util.StackHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.awt.*;

public class ContainerDisplay extends ContainerKismet {
    private final TileDisplay display;
    private final SlotTarget targetSlot;

    public ContainerDisplay(InventoryPlayer playerInventory, TileDisplay display) {
        this.display = display;
        if (!display.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            throw new RuntimeException("Tried to open GUI to display without inventory capability");
        }
        final IItemHandler itemHandler = display.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        this.targetSlot = new SlotTarget(itemHandler, 0, new Point(8, 17), 3 * 16,
                new Point(10, 19), 44);

        this.addSlotToContainer(this.targetSlot);
        this.setGuiSlots(1);

        addPlayerSlots(playerInventory, 8, 82);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack stack;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack originalStack = slot.getStack();
            stack = originalStack.copy();

            if (index >= getGuiSlots()) {
                int localSlot = slot.slotNumber - 1;

                emulateItemRightClick(playerIn, stack, localSlot + 9);
            }

            if (stack.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.putStack(stack);
            }

            if (originalStack.stackSize != stack.stackSize) {
                slot.onPickupFromSlot(playerIn, originalStack);
            }
        }

        return null;
    }

    /**
     * Emulates right clicking the display with the stack from the GUI. Only "works" on client side, but does the
     * minimum to avoid desyncs (most of the work is done server side anyway so desyncs would be stuff reverting back,
     * nothing serious).
     *
     * @param player    the player entity
     * @param slotIndex index of slot containing item (or negative for held item)
     */
    public void emulateItemRightClick(EntityPlayer player, ItemStack stack, int slotIndex) {
//        final InventoryPlayer inventory = player.inventory;
        if (!player.worldObj.isRemote) return;

        if (this.targetSlot != null && stack != null) {
            // and do the action on the server
            Kismet.network.sendToServer(new MessageGuiRemoteAction(this.display.getPos(), slotIndex));

            if (!this.display.isFulfilled() && StackHelper.isEquivalent(display.getTarget(), stack)) {
                SoundHelper.onTargetFulfilled(this.display.getWorld(), player, this.display.getPos());
            }
            // decrease a key amount if it's a key
            if (!player.isCreative() && stack.getItem() instanceof ItemKey) {
                --stack.stackSize;
            }
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return this.display.isReady();
    }
}
