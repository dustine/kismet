package dustine.kismet.target;

public class TargetResult {
    private final Target value;
    private final EnumTargetFailure flag;

    public TargetResult(Target value) {
        if (value == null || !value.hasItem()) {
            this.value = null;
            this.flag = EnumTargetFailure.EMPTY_STACK;
        } else {
            this.value = value;
            flag = null;
        }
    }

    public TargetResult(EnumTargetFailure flag) {
        value = null;
        this.flag = flag;
    }

    public boolean hasTarget() {
        return value != null && value.hasItem();
    }

    public Target getValue() {
        return value;
    }

    public EnumTargetFailure getFlag() {
        return flag;
    }

    public boolean hasFlag() {
        return flag != null;
    }

    public enum EnumTargetFailure {
        LIST_NOT_READY, NO_TARGETS_AVAILABLE, NO_TARGET_MODS_AVAILABLE, EMPTY_STACK
    }
}
