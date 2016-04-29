package dustine.kismet.target;

import com.google.common.collect.ImmutableSet;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Includes hardcoded and added by API changes to the target database/library generation
 */
public class TargetPatcher {
    private static final Map<EnumOrigin, Set<String>> blacklist = new HashMap<>();
    private static final Map<EnumOrigin, Set<String>> overrides = new HashMap<>();

    static {
        for (EnumOrigin origin : EnumOrigin.values()) {
            blacklist.put(origin, new HashSet<>());
            overrides.put(origin, new HashSet<>());
        }
        vanillaOverrides();
    }

    private static void vanillaOverrides() {
        /// BLACKLIST ORIGINS
        // the wooden slab that doesn't burn, still is counted as a valid BLOCK_DROP
        blacklist.get(EnumOrigin.BLOCK_DROP).add("minecraft:stone_slab:2");

        /// OVERRIDE (WHITELIST?) ORIGINS
        // a poisonous potato is such a rare drop (1/50) that it can be overlooked
        overrides.get(EnumOrigin.BLOCK_DROP).add("minecraft:poisonous_potato");

        // because the elytra spawns in an item frame, it isn't on the loot tables
        overrides.get(EnumOrigin.LOOT_TABLE).add("minecraft:elytra");

        // all these are drops not in the loot tables, for some reason or another
        // examples: RANDOM tick drops (egg), boss loot (nether star), mob killing mob (records)
        overrides.get(EnumOrigin.MOB_DROP).addAll(Arrays.asList(
                "minecraft:egg",
                "minecraft:nether_star",
                "minecraft:record_11", "minecraft:record_blocks", "minecraft:record_chirp", "minecraft:record_far", "minecraft:record_mall", "minecraft:record_mellohi", "minecraft:record_stal", "minecraft:record_strad", "minecraft:record_wait", "minecraft:record_ward"
        ));

        // target cataloguing engine doesn't do shearing yet because of reasons.
        // reasons being that it's impossible to know the drops, we can guess it's the original block but...
        // also doesn't work for entities, so we hard-code the vanilla values
        overrides.get(EnumOrigin.SHEAR).addAll(Arrays.asList(
                "minecraft:deadbush",
                "minecraft:double_plant:0", "minecraft:double_plant:1", "minecraft:double_plant:4", "minecraft:double_plant:5",
                "minecraft:leaves:0", "minecraft:leaves:1", "minecraft:leaves:2", "minecraft:leaves:3",
                "minecraft:leaves2:0", "minecraft:leaves2:1",
                "minecraft:red_mushroom",
                "minecraft:tallgrass:1", "minecraft:tallgrass:2",
                "minecraft:vine",
                "minecraft:web",
                "minecraft:wool:0", "minecraft:wool:1", "minecraft:wool:2", "minecraft:wool:3", "minecraft:wool:4", "minecraft:wool:5", "minecraft:wool:6", "minecraft:wool:7", "minecraft:wool:8", "minecraft:wool:9", "minecraft:wool:10", "minecraft:wool:11", "minecraft:wool:12", "minecraft:wool:13", "minecraft:wool:14", "minecraft:wool:15"
        ));

        // target cataloguing engine doesn't do shearing yet because of reasons.
        // reasons being that i haven't understood the VillageProfession system yet, and if it is breakable
        overrides.get(EnumOrigin.TRADE).addAll(Arrays.asList(
                "minecraft:chainmail_boots",
                "minecraft:chainmail_chestplate",
                "minecraft:chainmail_helmet",
                "minecraft:chainmail_leggings",
                "minecraft:experience_bottle",
                "minecraft:name_tag",
                "minecraft:saddle"
        ));

        // some weird remaining things that the generators didn't catch
        // for now, only bottled stuff (resulting item isn't a bucket so it isn't counted)
        // (and dragon breath is not a fluid, yet. probably there's a mod out there for it)
        overrides.get(EnumOrigin.OTHER).addAll(Arrays.asList(
                "minecraft:dragon_breath",
                "minecraft:potion:0"
        ));
    }

    public static Set<String> getOverrides(EnumOrigin origin) {
        if (overrides.containsKey(origin)) {
            return ImmutableSet.copyOf(overrides.get(origin));
        } else return Collections.emptySet();
    }

    public static boolean isBlacklisted(InformedStack stack, EnumOrigin origin) {
        return getBlacklist(origin).contains(stack.toString());
    }

    public static Set<String> getBlacklist(EnumOrigin origin) {
        if (blacklist.containsKey(origin)) {
            return ImmutableSet.copyOf(blacklist.get(origin));
        } else return Collections.emptySet();
    }

    public static class LootTableSeparator {
        private Set<ResourceLocation> lootTables;
        private List<LootTable> fishingTables;
        private List<LootTable> remainingTables;
        private List<LootTable> entityTables;

        public LootTableSeparator(Set<ResourceLocation> lootTables) {
            this.lootTables = lootTables;
        }

        public List<LootTable> getFishingTables() {
            return this.fishingTables;
        }

        public List<LootTable> getEntityTables() {
            return this.entityTables;
        }

        public List<LootTable> getRemainingTables() {
            return this.remainingTables;
        }

        public LootTableSeparator invoke(World world) {
            final ArrayList<ResourceLocation> remainingTableLocations = new ArrayList<>(this.lootTables);

            // entity loot: "entity" is somewhere in the name or directory
            final Set<ResourceLocation> entityTableLocations = this.lootTables.stream()
                    .filter(loc -> loc.getResourcePath().contains("entity") ||
                            loc.getResourcePath().contains("entities")
                    ).collect(Collectors.toSet());
            remainingTableLocations.removeAll(entityTableLocations);

            // fishing loot: only includes vanilla fishing loot locations
            final Set<ResourceLocation> fishingTableLocations = this.lootTables.stream()
                    .filter(loc -> loc.getResourceDomain().equals("minecraft"))
                    .filter(loc -> loc.getResourcePath().startsWith("gameplay/fishing"))
                    .collect(Collectors.toSet());
            remainingTableLocations.removeAll(fishingTableLocations);

            // convert table locations into the actual tables
            storeTablesFromLocations(world, remainingTableLocations, entityTableLocations, fishingTableLocations);

            return this;
        }

        private void storeTablesFromLocations(World world, ArrayList<ResourceLocation> remainingTableLocations, Set<ResourceLocation> entityTableLocations, Set<ResourceLocation> fishingTableLocations) {
            final LootTableManager lootTableManager = world.getLootTableManager();

            this.entityTables = entityTableLocations.stream()
                    .map(lootTableManager::getLootTableFromLocation)
                    .collect(Collectors.toList());

            this.fishingTables = fishingTableLocations.stream()
                    .map(lootTableManager::getLootTableFromLocation)
                    .collect(Collectors.toList());

            this.remainingTables = remainingTableLocations.stream()
                    .map(lootTableManager::getLootTableFromLocation)
                    .collect(Collectors.toList());
        }
    }
}
