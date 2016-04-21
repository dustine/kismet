package desutine.kismet.server;

import desutine.kismet.Kismet;
import desutine.kismet.ModLogger;
import desutine.kismet.util.StackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;

public final class InformedStack implements INBTSerializable<NBTTagCompound> {
    private ItemStack stack;
    private boolean obtainable;
    private boolean hasSubtypes;
    private boolean sealed;

    public InformedStack(NBTTagCompound nbt) {
        deserializeNBT(nbt);
        if (stack == null)
            throw new MissingStack();
//        this.hasSubtypes = Kismet.proxy.inferSafeHasSubtypes(stack);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("stack"))
            this.stack = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("stack"));
        obtainable = nbt.getBoolean("obtainable");
        hasSubtypes = nbt.getBoolean("hasSubtypes");
        sealed = nbt.getBoolean("sealed");
    }

    public InformedStack(@Nonnull ItemStack stack, boolean obtainable) {
        this(stack);
        this.obtainable = obtainable;
    }

    public InformedStack(@Nonnull ItemStack stack) {
        this.stack = stack;
        this.hasSubtypes = Kismet.proxy.inferSafeHasSubtypes(stack);
    }

    @Override
    public String toString() {
        return StackHelper.toUniqueKey(this);
    }

    public String toCompleteString() {
        return String.format("%s=%s", StackHelper.toUniqueKey(this), obtainable);
    }

    public NBTTagCompound writeToNBT() {
        return serializeNBT();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setTag("stack", stack.writeToNBT(new NBTTagCompound()));
        compound.setBoolean("obtainable", obtainable);
        compound.setBoolean("hasSubtypes", hasSubtypes);
        compound.setBoolean("sealed", sealed);
        return compound;
    }

    public void joinWith(InformedStack wrapper) {
        if (!this.sealed && !wrapper.isSealed()) {
            ModLogger.warning(String.format("Tried to join unsealed wrappers %s %s", this, wrapper));
        }
        // if they're not equal, log it and ignore
        if (!StackHelper.isEquivalent(this, wrapper)) {
            ModLogger.error(String.format("Tried to join distinct stacks %s %s",
                    StackHelper.toUniqueKey(this),
                    StackHelper.toUniqueKey(wrapper)
            ));
            return;
        }

        this.obtainable |= wrapper.isObtainable();
        this.hasSubtypes |= wrapper.getHasSubtypes();
        this.seal();
    }

    public void seal() {
        sealed = true;
    }

    public boolean isObtainable() {
        return obtainable;
    }

    public void setObtainable(boolean obtainable) {
        if (sealed) return;
        this.obtainable = obtainable;
    }

    public boolean getHasSubtypes() {
        return hasSubtypes;
    }

    public void setHasSubtypes(boolean hasSubtypes) {
        if (sealed) return;
        this.hasSubtypes = hasSubtypes;
    }

    public boolean isSealed() {
        return sealed;
    }

    public ItemStack getStack() {
        return stack;
    }

    public boolean hasItem() {
        return stack != null && stack.getItem() != null;
    }

    private class MissingStack extends RuntimeException {
    }
}
