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
        origins = new HashSet<>();
        int bitwiseObtainable = nbt.getInteger("obt");
        for (EnumOrigin type : EnumOrigin.values()) {
            if ((bitwiseObtainable & 1) != 0)
                origins.add(type);
            bitwiseObtainable >>= 1;
        }
        hasSubtypes = nbt.getBoolean("sub");
        sealed = nbt.getBoolean("sld");
    }

    @Override
    public NBTTagCompound serializeNBT() {
        final NBTTagCompound compound = new NBTTagCompound();
        if (stack != null) {
            compound.setTag("stk", stack.writeToNBT(new NBTTagCompound()));
        }
        int bitwiseObtainable = 0;
        for (EnumOrigin type : Lists.reverse(Arrays.asList(EnumOrigin.values()))) {
            bitwiseObtainable <<= 1;
            bitwiseObtainable |= hasOrigin(type) ? 1 : 0;
        }
        compound.setInteger("obt", bitwiseObtainable);
        compound.setBoolean("sub", hasSubtypes);
        compound.setBoolean("sld", sealed);
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
        if (sealed) return;
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
        if (sealed) return;
        hasSubtypes = stack.getHasSubtypes();
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
        sealed = true;
    }

    public boolean isObtainable() {
        // forced stacks are always obtainable
        if (hasOrigin(EnumOrigin.FORCED))
            return true;

        // else, check one of the cases one by one: if one is on, check if we're origins that way
        for (EnumOrigin origin : EnumOrigin.values()) {
            if (origin.equals(EnumOrigin.FORCED)) continue;
            if (ConfigKismet.isGenFlag(origin) && hasOrigin(origin)) return true;
        }

        // return false if we deplete all gens
        return false;
    }

    public boolean getHasSubtypes() {
        return hasSubtypes;
    }

    public void setHasSubtypes(boolean hasSubtypes) {
        if (sealed) return;
        this.hasSubtypes = hasSubtypes;
    }

    public ItemStack getStack() {
        return stack;
    }

    public boolean hasItem() {
        return stack != null && stack.getItem() != null;
    }

    public Set<EnumOrigin> getOrigins() {
        return origins;
    }

    public void setOrigins(Set<EnumOrigin> obtainable) {
        if (isSealed()) return;
        this.origins = obtainable;
    }

    public boolean isSealed() {
        return sealed;
    }

    public enum EnumOrigin {
        FORCED, OTHER, FLUID, RECIPE, LOOT_TABLE, BLOCK_DROPS, SILK_TOUCH;

        public String getName() {
            return this.name();
        }
    }
}
