package desutine.kismet.server;

import desutine.kismet.util.StackHelper;
import net.minecraft.item.ItemStack;

public final class StackWrapper {
    private final ItemStack stack;
    private boolean obtainable;

    public StackWrapper(ItemStack stack, boolean obtainable) {
        this.stack = stack;
        this.obtainable = obtainable;
    }

    public ItemStack getStack() {
        return stack;
    }

    public boolean isObtainable() {
        return obtainable;
    }

    public void setObtainable(boolean obtainable) {
        this.obtainable = obtainable;
    }

    @Override
    public String toString() {
        return StackHelper.toUniqueKey(stack) + "=" + obtainable;
    }
}
