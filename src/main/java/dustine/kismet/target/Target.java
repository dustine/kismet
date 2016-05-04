package dustine.kismet.target;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import dustine.kismet.Log;
import dustine.kismet.config.ConfigKismet;
import dustine.kismet.util.StackHelper;
import dustine.kismet.util.TargetHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import java.util.*;

public final class Target implements INBTSerializable<NBTTagCompound> {
    private ItemStack stack;
    private boolean hasSubtypes = true;
    private Set<EnumOrigin> origins;

    public Target(Target target) {
        // hackish way of doing a deep copy
        this(target.serializeNBT());
    }

    public Target(NBTTagCompound nbt) {
        deserializeNBT(nbt);
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        this.origins = new HashSet<>();
        int bitwiseObtainable = compound.getInteger("o");
        for (EnumOrigin type : EnumOrigin.values()) {
            if ((bitwiseObtainable & (1 << type.ordinal())) != 0)
                this.origins.add(type);
        }

        String key = compound.getString("s");
        this.stack = StackHelper.getItemStack(key);

        this.hasSubtypes = key.split(":").length > 2;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        final NBTTagCompound compound = new NBTTagCompound();

        int bitwiseObtainable = 0;
        for (EnumOrigin type : Lists.reverse(Arrays.asList(EnumOrigin.values()))) {
            bitwiseObtainable |= hasOrigin(type) ? 1 << type.ordinal() : 0;
        }
        compound.setInteger("o", bitwiseObtainable);

        compound.setString("s", this.toString());

        return compound;
    }

    public boolean hasOrigin(EnumOrigin type) {
        return this.origins.contains(type);
    }

    @Override
    public String toString() {
        return StackHelper.toUniqueKey(getStack(), getHasSubtypes());
    }

    public boolean getHasSubtypes() {
        return this.hasSubtypes;
    }

    public void setHasSubtypes(boolean hasSubtypes) {
        this.hasSubtypes = hasSubtypes;
    }

    public ItemStack getStack() {
        return ItemStack.copyItemStack(this.stack);
    }

    public Target(@Nonnull ItemStack stack, EnumOrigin type) {
        this(stack);
        setOrigins(type, true);
    }

    public Target(@Nonnull ItemStack stack) {
        this.stack = stack;
        this.origins = new HashSet<>();
        this.hasSubtypes = stack.getHasSubtypes();
    }

    public void setOrigins(EnumOrigin type, boolean obtainable) {
        if (obtainable) {
            this.origins.add(type);
        } else {
            this.origins.remove(type);
        }
    }

    public void refreshHasSubtypes() {
        this.hasSubtypes = this.stack.getHasSubtypes();
    }

    public NBTTagCompound writeToNBT() {
        return serializeNBT();
    }

    public Target joinWith(Target rhs) {
        return Target.join(this, rhs);
    }

    public static Target join(Target lhs, Target rhs) {
        // if they're not equal, log it and ignore
        if (!TargetHelper.isEquivalent(lhs, rhs)) {
            Log.error(String.format("Tried to join distinct targets %s %s", lhs, rhs));
            return null;
        }

        if (lhs.hasSubtypes != rhs.hasSubtypes) {
            Log.warning(String.format("Targets have different subtype state %s %s", lhs.getHasSubtypes(),
                    rhs.getHasSubtypes()));
        }

        // create a new target via deep copy
        Target target = new Target(lhs);
        target.origins.addAll(rhs.getCurrentObtainableTypes());

        return target;
    }

    public List<EnumOrigin> getCurrentObtainableTypes() {
        return new ArrayList<>(this.origins);
    }

    public boolean isObtainable() {
        // forced stacks are always obtainable
        if (hasOrigin(EnumOrigin.FORCED))
            return true;

        // else, check one of the cases one by one: if one is on, check if we're origins that way
        for (EnumOrigin origin : EnumOrigin.values()) {
            if (origin.equals(EnumOrigin.FORCED)) continue;
            // if the origin is allowed by config
            // if this target has that origin
            // and if this target isn't blacklisted (origin removed)
            if (ConfigKismet.isGenFlag(origin) && hasOrigin(origin) && !TargetPatcher.isBlacklisted(this, origin))
                return true;
        }

        // return false if we deplete all gens
        return false;
    }

    public boolean hasItem() {
        return this.stack != null && this.stack.getItem() != null;
    }

    public Set<EnumOrigin> getOrigins() {
        return ImmutableSet.copyOf(this.origins);
    }

    public void setOrigins(Set<EnumOrigin> obtainable) {
        this.origins = new HashSet<>(obtainable);
    }

}
