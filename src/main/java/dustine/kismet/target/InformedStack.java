package dustine.kismet.target;

import com.google.common.collect.Lists;
import dustine.kismet.ConfigKismet;
import dustine.kismet.Log;
import dustine.kismet.util.StackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import java.util.*;

public final class InformedStack implements INBTSerializable<NBTTagCompound> {
    private ItemStack stack;
    private boolean hasSubtypes = true;
    private boolean sealed;
    private Set<EnumOrigin> origins;

    public InformedStack(InformedStack stack) {
        // hackish way of doing a deep copy
        this(stack.serializeNBT());
    }

    public InformedStack(NBTTagCompound nbt) {
        deserializeNBT(nbt);
//        this.hasSubtypes = Kismet.proxy.sideSafeHasSubtypes(stack);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.stack = nbt.hasKey("stk") ? ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("stk")) : null;
        this.origins = new HashSet<>();
        int bitwiseObtainable = nbt.getInteger("obt");
        for (EnumOrigin type : EnumOrigin.values()) {
            if ((bitwiseObtainable & 1) != 0)
                this.origins.add(type);
            bitwiseObtainable >>= 1;
        }
        this.hasSubtypes = nbt.getBoolean("sub");
        this.sealed = nbt.getBoolean("sld");
    }

    @Override
    public NBTTagCompound serializeNBT() {
        final NBTTagCompound compound = new NBTTagCompound();
        if (this.stack != null) {
            compound.setTag("stk", this.stack.writeToNBT(new NBTTagCompound()));
        }
        int bitwiseObtainable = 0;
        for (EnumOrigin type : Lists.reverse(Arrays.asList(EnumOrigin.values()))) {
            bitwiseObtainable <<= 1;
            bitwiseObtainable |= hasOrigin(type) ? 1 : 0;
        }
        compound.setInteger("obt", bitwiseObtainable);
        compound.setBoolean("sub", this.hasSubtypes);
        compound.setBoolean("sld", this.sealed);
        return compound;
    }

    public boolean hasOrigin(EnumOrigin type) {
        return this.origins.contains(type);
    }

    public InformedStack(@Nonnull ItemStack stack, EnumOrigin type) {
        this(stack);
        setOrigins(type, true);
    }

    public InformedStack(@Nonnull ItemStack stack) {
        this.stack = stack;
        this.origins = new HashSet<>();
        this.hasSubtypes = stack.getHasSubtypes();
    }

    public void setOrigins(EnumOrigin type, boolean obtainable) {
        if (this.sealed) return;
        if (obtainable) {
            this.origins.add(type);
        } else {
            this.origins.remove(type);
        }
    }

    @Override
    public String toString() {
        return StackHelper.toUniqueKey(this);
    }

    public void refreshHasSubtypes() {
        if (this.sealed) return;
        this.hasSubtypes = this.stack.getHasSubtypes();
    }

    public NBTTagCompound writeToNBT() {
        return serializeNBT();
    }

    public InformedStack joinWith(InformedStack rhs) {
        return InformedStack.join(this, rhs);
    }

    public static InformedStack join(InformedStack lhs, InformedStack rhs) {
        if (!lhs.sealed && !rhs.sealed) {
            Log.error(String.format("Tried to join unsealed wrappers %s %s", lhs, rhs));
            return null;
        }
        // if they're not equal, log it and ignore
        if (!StackHelper.isEquivalent(lhs, rhs)) {
            Log.error(String.format("Tried to join distinct stacks %s %s", lhs, rhs));
            return null;
        }

        if (lhs.hasSubtypes != rhs.hasSubtypes) {
            Log.warning(String.format("Stacks have different subtype state %s %s", lhs.getHasSubtypes(), rhs.getHasSubtypes()));
        }

        // create a new informedStack via deep copy
        InformedStack informedStack = new InformedStack(lhs);
        informedStack.origins.addAll(rhs.getCurrentObtainableTypes());
        informedStack.seal();

        return informedStack;
    }

    public List<EnumOrigin> getCurrentObtainableTypes() {
        return new ArrayList<>(this.origins);
    }

    public void seal() {
        this.sealed = true;
    }

    public boolean getHasSubtypes() {
        return this.hasSubtypes;
    }

    public void setHasSubtypes(boolean hasSubtypes) {
        if (this.sealed) return;
        this.hasSubtypes = hasSubtypes;
    }

    public boolean isObtainable() {
        // forced stacks are always obtainable
        if (hasOrigin(EnumOrigin.FORCED))
            return true;

        // else, check one of the cases one by one: if one is on, check if we're origins that way
        for (EnumOrigin origin : EnumOrigin.values()) {
            if (origin.equals(EnumOrigin.FORCED)) continue;
            if (ConfigKismet.isGenFlag(origin) && hasOrigin(origin))
                return !TargetPatcher.isBlacklisted(this, origin);
        }

        // return false if we deplete all gens
        return false;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public boolean hasItem() {
        return this.stack != null && this.stack.getItem() != null;
    }

    public Set<EnumOrigin> getOrigins() {
        return this.origins;
    }

    public void setOrigins(Set<EnumOrigin> obtainable) {
        if (isSealed()) return;
        this.origins = obtainable;
    }

    public boolean isSealed() {
        return this.sealed;
    }

}
