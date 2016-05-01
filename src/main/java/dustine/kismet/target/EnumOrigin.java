package dustine.kismet.target;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum EnumOrigin {
    FORCED, OTHER, BLOCK_DROP, FISHING, FLUID, LOOT_TABLE, MOB_DROP, RECIPE, SILK_TOUCH, SHEAR, TRADE;

    /**
     * Returns the elements of EnumOrigins ordered by name, with FORCED (optionally in the ordered collection) in the
     * beginning and OTHER in the end
     *
     * @param forced true to include FORCED in the ordering
     * @return
     */
    public static List<EnumOrigin> getSorted(boolean forced) {
        final List<EnumOrigin> origins = Arrays.stream(EnumOrigin.values())
                .sorted((o1, o2) -> o1.toString().compareTo(o2.toString()))
                .collect(Collectors.toList());

        origins.remove(EnumOrigin.FORCED);
        origins.remove(EnumOrigin.OTHER);
        if (forced) origins.add(0, EnumOrigin.FORCED);
        origins.add(EnumOrigin.OTHER);

        return origins;
    }

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
