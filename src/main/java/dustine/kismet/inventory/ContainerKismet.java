package dustine.kismet.inventory;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

public abstract class ContainerKismet extends Container {
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
}
