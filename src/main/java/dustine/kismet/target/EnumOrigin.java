package dustine.kismet.target;

public enum EnumOrigin {
    FORCED, OTHER, BLOCK_DROP, FISHING, FLUID, LOOT_TABLE, MOB_DROP, RECIPE, SILK_TOUCH, SHEAR, TRADE;

    public String getName() {
        return this.name();
    }
}
