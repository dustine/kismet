package dustine.kismet.gui.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public abstract class ContainerKismet extends Container {
    protected static int PLAYER_SLOTS = 36;
    private int guiSlots = 0;

    protected void addPlayerSlots(InventoryPlayer playerInventory, int x, int y) {
        // inventory
        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                this.addSlotToContainer(new Slot(playerInventory, column + row * 9 + 9, x + column * 18, y + row * 18));
            }
        }

        // hot bar
        for (int column = 0; column < 9; ++column) {
            this.addSlotToContainer(new Slot(playerInventory, column, x + column * 18, y + 58));
        }
    }

    /**
     * Needed for the shift-click
     *
     * @param playerIn
     * @param index
     * @return null
     */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack stack = null;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack originalStack = slot.getStack();
            stack = originalStack.copy();

            if (index < getGuiSlots()) {
                if (!this.mergeItemStack(originalStack, getGuiSlots(), PLAYER_SLOTS + getGuiSlots(), true)) {
                    return null;
                }
            } else {
                if (!this.mergeItemStack(originalStack, 0, getGuiSlots(), false)) {
                    return null;
                }
            }

            if (originalStack.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }

            if (originalStack.stackSize == stack.stackSize) {
                return null;
            }

            slot.onPickupFromSlot(playerIn, originalStack);
        }

        return stack;
    }

    public int getGuiSlots() {
        return this.guiSlots;
    }

    public void setGuiSlots(int guiSlots) {
        this.guiSlots = guiSlots;
    }
}
