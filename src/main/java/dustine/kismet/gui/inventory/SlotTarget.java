package dustine.kismet.gui.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.awt.*;

public class SlotTarget extends SlotItemHandler {
    private final int iconSize;
    private final Point iconOrigin;
    private final int slotSize;

    public SlotTarget(IItemHandler itemHandler, int index, Point slotOrigin, int slotSize, Point iconOrigin, int
            iconFactor) {
        super(itemHandler, index, slotOrigin.x, slotOrigin.y);
        this.iconSize = iconFactor;
        this.iconOrigin = iconOrigin;
        this.slotSize = slotSize;
    }

    public int getIconSize() {
        return this.iconSize;
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
        return true;
    }

    public int getSlotSize() {
        return this.slotSize;
    }

    public Point getIconOrigin() {
        return iconOrigin;
    }
}
