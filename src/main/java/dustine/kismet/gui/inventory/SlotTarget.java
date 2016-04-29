package dustine.kismet.gui.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SlotTarget extends SlotItemHandler {
    private final double factor;

    public SlotTarget(IItemHandler itemHandler, int index, int xPosition, int yPosition, double factor) {
        super(itemHandler, index, xPosition, yPosition);
        this.factor = factor;
    }

    public double getFactor() {
        return factor;
    }

    /**
     * Overwritten as we can't put items into the target slot
     *
     * @param stack ItemStack to set slot to
     */
    @Override
    public void putStack(ItemStack stack) {
    }

    @Override
    public boolean canBeHovered() {
        return false;
    }

}
