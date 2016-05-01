package dustine.kismet.config;

import com.google.common.collect.ImmutableMap;
import dustine.kismet.Log;
import dustine.kismet.Reference;
import dustine.kismet.target.EnumOrigin;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Loader;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConfigKismetOverride {
    private static final Map<EnumOrigin, Set<String>> blacklist = new HashMap<>();
    private static final Map<EnumOrigin, Set<String>> overrides = new HashMap<>();

    private static final String[] defaultBlacklistValues = new String[] {
            // serverOnlyReset adds all banners as recipes without NBT, so identity functions fail on these
            "RECIPE:minecraft:banner:1",
            "RECIPE:minecraft:banner:2",
            "RECIPE:minecraft:banner:3",
            "RECIPE:minecraft:banner:4",
            "RECIPE:minecraft:banner:5",
            "RECIPE:minecraft:banner:6",
            "RECIPE:minecraft:banner:7",
            "RECIPE:minecraft:banner:8",
            "RECIPE:minecraft:banner:9",
            "RECIPE:minecraft:banner:10",
            "RECIPE:minecraft:banner:11",
            "RECIPE:minecraft:banner:12",
            "RECIPE:minecraft:banner:13",
            "RECIPE:minecraft:banner:14",
            "RECIPE:minecraft:banner:15",
            // the old un-burnable wooden slab, even without a block form, this still exists as an item
            "BLOCK_DROP:minecraft:stone_slab:2"
    };
    private static final String[] defaultOverridesValues = new String[] {
            // 1/50 chance of dropping means the poisonous potato rarely skips detection
            "BLOCK_DROP:minecraft:poisonous_potato",
            // because it spawns in an item frame, it's not in the loot tables
            "LOOT_TABLE:minecraft:elytra",
            // chicken's egg laying is under a randomTick() function so we can't catch it
            "MOB_DROP:minecraft:egg",
            // for some reason, boss drops aren't in the loot tables. at least not for any of the vanilla bosses.
            "MOB_DROP:minecraft:nether_star",
            // these are the records that can only be obtained by a skellie killing a creeper. weirdly enough loot
            // tables don't have a way to characterize what entity type the killer is, only if it's a player or not,
            // so these just aren't in the loot tables
            "MOB_DROP:minecraft:record_11",
            "MOB_DROP:minecraft:record_blocks",
            "MOB_DROP:minecraft:record_chirp",
            "MOB_DROP:minecraft:record_far",
            "MOB_DROP:minecraft:record_mall",
            "MOB_DROP:minecraft:record_mellohi",
            "MOB_DROP:minecraft:record_stal",
            "MOB_DROP:minecraft:record_strad",
            "MOB_DROP:minecraft:record_wait",
            "MOB_DROP:minecraft:record_ward",
            // for IShearable, atm it's impossible to know the drops. We can guess it's the original block but things
            // like the tallgrass blocks show why it's a bad idea: the two-height variants don't give themselves back
            // but instead two of their smaller version. AUGH. also doesn't even begin to work for entities
            "SHEAR:minecraft:deadbush",
            "SHEAR:minecraft:double_plant:0",
            "SHEAR:minecraft:double_plant:1",
            "SHEAR:minecraft:double_plant:4",
            "SHEAR:minecraft:double_plant:5",
            "SHEAR:minecraft:leaves:0",
            "SHEAR:minecraft:leaves:1",
            "SHEAR:minecraft:leaves:2",
            "SHEAR:minecraft:leaves:3",
            "SHEAR:minecraft:leaves2:0",
            "SHEAR:minecraft:leaves2:1",
            "SHEAR:minecraft:red_mushroom",
            "SHEAR:minecraft:tallgrass:1",
            "SHEAR:minecraft:tallgrass:2",
            "SHEAR:minecraft:vine",
            "SHEAR:minecraft:web",
            "SHEAR:minecraft:wool:0",
            "SHEAR:minecraft:wool:1",
            "SHEAR:minecraft:wool:2",
            "SHEAR:minecraft:wool:3",
            "SHEAR:minecraft:wool:4",
            "SHEAR:minecraft:wool:5",
            "SHEAR:minecraft:wool:6",
            "SHEAR:minecraft:wool:7",
            "SHEAR:minecraft:wool:8",
            "SHEAR:minecraft:wool:9",
            "SHEAR:minecraft:wool:10",
            "SHEAR:minecraft:wool:11",
            "SHEAR:minecraft:wool:12",
            "SHEAR:minecraft:wool:13",
            "SHEAR:minecraft:wool:14",
            "SHEAR:minecraft:wool:15",
            // the algorithm for villager trades is still on the works. may be impossible, may be not
            "TRADE:minecraft:chainmail_boots",
            "TRADE:minecraft:chainmail_chestplate",
            "TRADE:minecraft:chainmail_helmet",
            "TRADE:minecraft:chainmail_leggings",
            "TRADE:minecraft:experience_bottle",
            "TRADE:minecraft:name_tag",
            "TRADE:minecraft:saddle",
            // other misc ones that idk what to file they under, basically "fill bottle with stuff inworld"
            "OTHER:minecraft:dragon_breath",
            "OTHER:minecraft:potion:0"
    };
    private static Configuration config;

    public static void init() {
        File configFile = new File(Loader.instance().getConfigDir(), Reference.MOD_ID + "Override.cfg");
        if (config == null) {
            config = new Configuration(configFile, Reference.VERSION);
        }
        if (!configFile.exists()) {
            reset();
        } else {
            config.load();
            // check for lock, and if not, reset on version mismatch
            final boolean lock = config.get("lock", Configuration.CATEGORY_GENERAL, false).getBoolean();
            if (!lock && !config.getLoadedConfigVersion().equals(config.getDefinedConfigVersion())) {
                reset();
            }
        }

        // load the configs into the patcher
        final String[] fileBlacklist = config.get("blacklist", Configuration.CATEGORY_GENERAL, defaultBlacklistValues)
                .getStringList();
        final String[] fileOverrides = config.get("override", Configuration.CATEGORY_GENERAL, defaultOverridesValues)
                .getStringList();

        for (EnumOrigin origin : EnumOrigin.values()) {
            if (origin == EnumOrigin.FORCED) continue;
            blacklist.put(origin, new HashSet<>());
            overrides.put(origin, new HashSet<>());
        }

        addOverrides("blacklist", blacklist, fileBlacklist);
        addOverrides("overrides", overrides, fileOverrides);
        Log.info(String.format("Found %d blacklisted and %d overridden file entries",
                blacklist.values().stream().map(Set::size).reduce((i, j) -> i + j).orElse(0),
                overrides.values().stream().map(Set::size).reduce((i, j) -> i + j).orElse(0)
        ));

        config.save();
    }

    private static void addOverrides(String name, Map<EnumOrigin, Set<String>> list, String... entries) {
        for (String entry : entries) {
            if (org.apache.commons.lang3.StringUtils.countMatches(entry, ":") < 2) {
                Log.error(String.format("Unrecognized %s file entry %s, skipped", name, entry));
                continue;
            }
            if (entry.startsWith(":")) {
                Log.error(String.format("Origin missing on %s file entry %s, skipped", name, entry));
                continue;
            }

            boolean set = false;
            EnumOrigin origin = EnumOrigin.OTHER;
            final String[] split = entry.split(":", 2);
            String originFlag = split[0];
            String item = split[1];
            for (EnumOrigin o : EnumOrigin.values()) {
                if (o.name().equalsIgnoreCase(originFlag) || o.toCamelCase().equalsIgnoreCase(originFlag)) {
                    origin = o;
                    set = true;
                    break;
                }
            }

            if (origin == EnumOrigin.FORCED) {
                Log.error(String.format("Can't set origin %s on %s file entry %s, skipped", origin, name, entry));
                continue;
            }

            if (!set) {
                Log.warning(String.format("Weird origin on %s file entry %s, added as %s", name, entry, origin));
            }

            list.get(origin).add(item);
        }
    }

    private static void reset() {
        config.setCategoryComment(Configuration.CATEGORY_GENERAL, String.format(
                "This file is meant as a manual override for the obtainability algorithms of the mod; either to add new origins for certain items, or to remove (blacklist) them.\nFields are under the format <ORIGIN:ITEM>, where <ORIGIN> is one of %s and <ITEM> uses the same format as in the normal config file ([mod:item[:meta[:nbt]]]).\n**CAUTION**: because the file is loaded before any mods had a chance of registering themselves and their items, there is no sanitation on these fields for item validity. The mod shouldn't crash but expect console spam over any messed up fields.",
                EnumOrigin.getSorted(true)));

        final ConfigCategory category = config.getCategory(Configuration.CATEGORY_GENERAL);
        category.setRequiresMcRestart(true);

        Property propLock = config.get(Configuration.CATEGORY_GENERAL, "lock", false,
                "If this is set to true, this file won't be reset even when the mod updates");
        propLock.setRequiresMcRestart(true);
        category.put("lock", propLock);

        Property propBlacklist = config.get(Configuration.CATEGORY_GENERAL, "blacklist", defaultBlacklistValues,
                "Items put here, under <ORIGIN:ITEM>, will have the origin forcefully removed from that item when " +
                        "registered as a target. Takes priority over overrides.");
        propBlacklist.setRequiresMcRestart(true);
        category.put("blacklist", propBlacklist);

        Property propOverride = config.get(Configuration.CATEGORY_GENERAL, "overrides", defaultOverridesValues,
                "Items put here, under <ORIGIN:ITEM>, will have the origin forcefully added from that item when " +
                        "registered as a target.");
        propOverride.setRequiresMcRestart(true);
        category.put("overrides", propOverride);
    }

    public static Map<EnumOrigin, Set<String>> getBlacklist() {
        return ImmutableMap.copyOf(blacklist);
    }

    public static Map<EnumOrigin, Set<String>> getOverrides() {
        return ImmutableMap.copyOf(overrides);
    }
}
