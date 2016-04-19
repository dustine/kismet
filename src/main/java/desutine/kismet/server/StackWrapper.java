package desutine.kismet.server;

import desutine.kismet.ModLogger;
import desutine.kismet.util.StackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;

public final class StackWrapper implements INBTSerializable<NBTTagCompound> {
    private ItemStack stack;
    private boolean obtainable;

    public StackWrapper(@Nonnull ItemStack stack) {
        this.stack = stack;
    }

    public StackWrapper(NBTTagCompound nbt) {
        deserializeNBT(nbt);
        if (stack == null)
            throw new MissingStack();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("stack"))
            this.stack = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("stack"));
        obtainable = nbt.getBoolean("obtainable");
    }

    public StackWrapper(@Nonnull ItemStack stack, boolean obtainable) {
        this.stack = stack;
        this.obtainable = obtainable;
    }

    public static StackWrapper newFromNbt(NBTTagCompound nbt) {
        return new StackWrapper(nbt);
    }

    @Override
    public String toString() {
        return StackHelper.toUniqueKey(stack);
    }

    public String toCompleteString() {
        return String.format("%s=%s", StackHelper.toUniqueKey(stack), obtainable);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setTag("stack", stack.writeToNBT(new NBTTagCompound()));
        compound.setBoolean("obtainable", obtainable);
        return compound;
    }

    public void joinWith(StackWrapper wrapper) {
        // if they're not equal, log it and ignore
        if (!StackHelper.isEquivalent(this.getStack(), wrapper.getStack())) {
            ModLogger.warning(String.format("Tried to join distinct stacks: %s and %s",
                    StackHelper.toUniqueKey(this.getStack()),
                    StackHelper.toUniqueKey(wrapper.getStack())
            ));
        }

        this.obtainable |= wrapper.isObtainable();
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

    private class MissingStack extends RuntimeException {
    }
}
