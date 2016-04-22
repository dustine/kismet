package desutine.kismet.target;

import desutine.kismet.ConfigKismet;
import desutine.kismet.Kismet;
import desutine.kismet.ModLogger;
import desutine.kismet.util.StackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public final class InformedStack implements INBTSerializable<NBTTagCompound> {
    private ItemStack stack;
    private boolean hasSubtypes;
    private boolean sealed;
    private boolean[] obtainable;

    public InformedStack(InformedStack stack) {
        // hackish way of doing a deep copy
        this(stack.serializeNBT());
    }

    public InformedStack(NBTTagCompound nbt) {
        deserializeNBT(nbt);
        if (stack == null)
            throw new MissingStack();
//        this.hasSubtypes = Kismet.proxy.inferSafeHasSubtypes(stack);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("stk"))
            this.stack = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("stk"));
        byte[] byteObtainable = nbt.getByteArray("obt");
        // todo make this into bitshifted bytes for maximum efficiency
        obtainable = new boolean[byteObtainable.length];
        for (int i = 0; i < byteObtainable.length; i++) {
            obtainable[i] = byteObtainable[i] > 0;
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
        byte[] byteObtainable = new byte[obtainable.length];
        for (int i = 0; i < obtainable.length; i++) {
            byteObtainable[i] = (byte) (obtainable[i] ? 1 : 0);
        }
        compound.setByteArray("obt", byteObtainable);
        compound.setBoolean("sub", hasSubtypes);
        compound.setBoolean("sld", sealed);
        return compound;
    }

    public InformedStack(@Nonnull ItemStack stack, ObtainableTypes type) {
        this(stack);
        obtainable[type.ordinal()] = true;
    }

    public InformedStack(@Nonnull ItemStack stack) {
        this.stack = stack;
        this.obtainable = new boolean[ObtainableTypes.values().length];
        this.hasSubtypes = Kismet.proxy.inferSafeHasSubtypes(stack);
    }

    @Override
    public String toString() {
        return StackHelper.toUniqueKey(this);
    }

    public String toCompleteString() {
        return String.format("%s = %s", StackHelper.toUniqueKey(this), getCurrentObtainableTypes());
    }

    private List<ObtainableTypes> getCurrentObtainableTypes() {
        ArrayList<ObtainableTypes> obtainableTypes = new ArrayList<>();
        for (int i = 0; i < ObtainableTypes.values().length; i++) {
            if (obtainable[i]) {
                obtainableTypes.add(ObtainableTypes.values()[i]);
            }
        }
        return obtainableTypes;
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

        for (ObtainableTypes type : rhs.getCurrentObtainableTypes()) {
            informedStack.obtainable[type.ordinal()] = true;
        }
        informedStack.seal();

        return informedStack;
    }

    public void seal() {
        sealed = true;
    }

    public boolean isObtainable() {
        // forced stacks are always obtainable
        if (isObtainable(ObtainableTypes.FORCED))
            return true;

        // if unfair is off, do not set unfair stacks as obtainable
        if (!ConfigKismet.isGenUnfair() && isObtainable(ObtainableTypes.UNFAIR))
            return false;

        // else, check one of the cases one by one: if one is on, check if we're obtainable that way
        if (ConfigKismet.isGenBucketable() && isObtainable(ObtainableTypes.BUCKETABLE))
            return true;
        if (ConfigKismet.isGenCraftable() && isObtainable(ObtainableTypes.CRAFTABLE))
            return true;
        if (ConfigKismet.isGenLootable() && isObtainable(ObtainableTypes.LOOTABLE))
            return true;
        if (ConfigKismet.isGenMineable() && isObtainable(ObtainableTypes.MINEABLE))
            return true;
        if (ConfigKismet.isGenSilkable() && isObtainable(ObtainableTypes.SILKABLE))
            return true;
        if (ConfigKismet.isGenOthers() && isObtainable(ObtainableTypes.OTHERS))
            return true;

        // return false if we deplete all gens
        return false;
    }

    public boolean isObtainable(ObtainableTypes type) {
        return this.obtainable[type.ordinal()];
    }

    public void setObtainable(ObtainableTypes type, boolean obtainable) {
        if (sealed) return;
        this.obtainable[type.ordinal()] = obtainable;
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

    public boolean[] getObtainable() {
        return obtainable;
    }

    public void setObtainable(boolean[] obtainable) {
        if (isSealed()) return;
        this.obtainable = obtainable;
    }

    public boolean isSealed() {
        return sealed;
    }

    public enum ObtainableTypes {
        BUCKETABLE, CRAFTABLE, LOOTABLE, MINEABLE, SILKABLE, OTHERS, UNFAIR, FORCED
    }

    private class MissingStack extends RuntimeException {
    }
}
