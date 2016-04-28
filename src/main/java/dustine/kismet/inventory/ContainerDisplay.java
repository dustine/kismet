package dustine.kismet.inventory;

import dustine.kismet.tile.TileDisplay;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerDisplay extends ContainerKismet {
    private final TileDisplay display;

    public ContainerDisplay(InventoryPlayer playerInventory, TileDisplay display) {
        this.display = display;
        if (!display.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            throw new RuntimeException("Tried to open GUI to display without inventory capability");
        }
        final IItemHandler itemHandler = display.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        this.addSlotToContainer(new SlotItemHandler(itemHandler, 0, 81, 33) {
            /**
             * Overwritten as we can't put items into the target slot
             *
             * @param stack ItemStack to set slot to
             */
            @Override
            public void putStack(ItemStack stack) {
            }
        });

        addPlayerSlots(playerInventory, 8, 84);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        return super.transferStackInSlot(playerIn, index);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }
}
