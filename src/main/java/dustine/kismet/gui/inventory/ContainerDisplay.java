package dustine.kismet.gui.inventory;

import dustine.kismet.tile.TileDisplay;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ContainerDisplay extends ContainerKismet {
    private final TileDisplay display;

    public ContainerDisplay(InventoryPlayer playerInventory, TileDisplay display) {
        this.display = display;
        if (!display.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            throw new RuntimeException("Tried to open GUI to display without inventory capability");
        }
        final IItemHandler itemHandler = display.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        this.addSlotToContainer(new SlotTarget(itemHandler, 0, 10, 19, (44 * 3.0) / 48));

        addPlayerSlots(playerInventory, 8, 82);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        return null;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return display.isReady();
    }
}
