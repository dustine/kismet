package dustine.kismet.target;

import com.google.common.collect.Lists;
import dustine.kismet.ConfigKismet;
import dustine.kismet.ModLogger;
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
    private Set<ObtainableTypes> obtainable;

    public InformedStack(InformedStack stack) {
        // hackish way of doing a deep copy
        this(stack.serializeNBT());
    }

    public InformedStack(NBTTagCompound nbt) {
        deserializeNBT(nbt);
//        this.hasSubtypes = Kismet.proxy.inferSafeHasSubtypes(stack);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.stack = nbt.hasKey("stk") ? ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("stk")) : null;
        obtainable = new HashSet<>();
        int bitwiseObtainable = nbt.getInteger("obt");
        for (ObtainableTypes type : ObtainableTypes.values()) {
            if ((bitwiseObtainable & 1) != 0)
                obtainable.add(type);
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
        for (ObtainableTypes type : Lists.reverse(Arrays.asList(ObtainableTypes.values()))) {
            bitwiseObtainable <<= 1;
            bitwiseObtainable |= isObtainable(type) ? 1 : 0;
        }
        compound.setInteger("obt", bitwiseObtainable);
        compound.setBoolean("sub", hasSubtypes);
        compound.setBoolean("sld", sealed);
        return compound;
    }

    public boolean isObtainable(ObtainableTypes type) {
        return this.obtainable.contains(type);
    }

    public InformedStack(@Nonnull ItemStack stack, ObtainableTypes type) {
        this(stack);
        setObtainable(type, true);
    }

    public InformedStack(@Nonnull ItemStack stack) {
        this.stack = stack;
        this.obtainable = new HashSet<>();
        this.hasSubtypes = stack.getHasSubtypes();
    }

    public void setObtainable(ObtainableTypes type, boolean obtainable) {
        if (sealed) return;
        if (obtainable) {
            this.obtainable.add(type);
        } else {
            this.obtainable.remove(type);
        }
    }

    @Override
    public String toString() {
        return StackHelper.toUniqueKey(this);
    }

    public String toCompleteString() {
        return String.format("%s %s", StackHelper.toUniqueKey(this), getCurrentObtainableTypes());
    }

    private List<ObtainableTypes> getCurrentObtainableTypes() {
        return new ArrayList<>(this.obtainable);
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
            ModLogger.error(String.format("Tried to join unsealed wrappers %s %s", lhs, rhs));
            return null;
        }
        // if they're not equal, log it and ignore
        if (!StackHelper.isEquivalent(lhs, rhs)) {
            ModLogger.error(String.format("Tried to join distinct stacks %s %s", lhs, rhs));
            return null;
        }

        // create a new informedStack via deep copy
        InformedStack informedStack = new InformedStack(lhs);
        informedStack.obtainable.addAll(rhs.getCurrentObtainableTypes());
        informedStack.seal();

        return informedStack;
    }

    public void seal() {
        sealed = true;
    }

    public boolean isObtainable() {
        // forced stacks are always obtainable
        if (isObtainable(ObtainableTypes.Forced))
            return true;

        // if unfair is off, do not set unfair stacks as obtainable
        if (!ConfigKismet.isGenUnfair() && isObtainable(ObtainableTypes.Unfair))
            return false;

        // else, check one of the cases one by one: if one is on, check if we're obtainable that way
        for (ObtainableTypes type : ObtainableTypes.values()) {
            if (type.equals(ObtainableTypes.Forced) || type.equals(ObtainableTypes.Unfair)) continue;
            if (ConfigKismet.isGenFlag(type) && isObtainable(type)) return true;
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

    public Set<ObtainableTypes> getObtainable() {
        return obtainable;
    }

    public void setObtainable(Set<ObtainableTypes> obtainable) {
        if (isSealed()) return;
        this.obtainable = obtainable;
    }

    public boolean isSealed() {
        return sealed;
    }

    public enum ObtainableTypes {
        Forced, Unfair, Others, Bucketable, Craftable, Lootable, Mineable, Silkable
    }
}
