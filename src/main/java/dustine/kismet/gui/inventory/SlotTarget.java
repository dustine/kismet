package dustine.kismet.gui.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.awt.*;

public class SlotTarget extends SlotItemHandler {
    private final double factor;
    private Point realXY;
    private double realFactor;

    public SlotTarget(IItemHandler itemHandler, int index, Point xY, double factor) {
        this(itemHandler, index, xY, factor, xY, factor);
    }

    public SlotTarget(IItemHandler itemHandler, int index, Point iconXY, double iconFactor, Point realXY, double
            realFactor) {
        super(itemHandler, index, iconXY.x, iconXY.y);
        this.factor = iconFactor;
        this.realXY = realXY;
        this.realFactor = realFactor;
    }

    public double getFactor() {
        return this.factor;
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

    public double getRealFactor() {
        return this.realFactor;
    }

    public Point getRealO() {
        return this.realXY;
    }
}
