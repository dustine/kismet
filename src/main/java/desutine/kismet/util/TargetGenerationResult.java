package desutine.kismet.util;

import net.minecraft.item.ItemStack;

public class TargetGenerationResult {
    private final ItemStack value;
    private final EnumTargetFailure flag;

    public TargetGenerationResult(ItemStack value) {
        if (value.getItem() == null) {
            this.value = null;
            this.flag = EnumTargetFailure.EMPTY_ITEMSTACK;
        } else {
            this.value = value;
            flag = null;
        }
    }

    public TargetGenerationResult(EnumTargetFailure flag) {
        value = null;
        this.flag = flag;
    }

    public boolean hasTarget() {
        return value != null && value.getItem() != null;
    }

    public ItemStack getValue() {
        return value;
    }

    public EnumTargetFailure getFlag() {
        return flag;
    }

    public boolean hasFlag() {
        return flag != null;
    }

    public enum EnumTargetFailure {
        LIST_NOT_READY, NO_TARGETS_AVAILABLE, NO_TARGET_MODS_AVAILABLE, EMPTY_ITEMSTACK
    }
}
