package dustine.kismet.target;

public enum EnumOrigin {
    FORCED, OTHER, BLOCK_DROP, FISHING, FLUID, LOOT_TABLE, MOB_DROP, RECIPE, SILK_TOUCH, SHEAR, TRADE;

    public String getName() {
        return this.name();
    }

    public String toCamelCase() {
        boolean wordBoundary = false;
        final StringBuilder builder = new StringBuilder();
        for (char s : this.name().toCharArray()) {
            if (s == '_') {
                wordBoundary = true;
                continue;
            }
            if (wordBoundary) {
                wordBoundary = false;
                builder.append(s);
            } else {
                builder.append(Character.toLowerCase(s));
            }
        }
        return builder.toString();
    }
}
