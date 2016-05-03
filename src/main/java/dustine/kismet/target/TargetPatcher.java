package dustine.kismet.target;

import com.google.common.collect.ImmutableSet;
import dustine.kismet.Log;
import dustine.kismet.config.ConfigKismetOverride;
import dustine.kismet.util.StackHelper;
import net.minecraft.item.ItemStack;
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
            if (origin == EnumOrigin.FORCED) continue;
            blacklist.put(origin, new HashSet<>());
            overrides.put(origin, new HashSet<>());
        }
    }

    public static Set<String> getOverrides(EnumOrigin origin) {
        if (overrides.containsKey(origin)) {
            return ImmutableSet.copyOf(overrides.get(origin));
        } else return Collections.emptySet();
    }

    public static boolean isBlacklisted(Target target, EnumOrigin origin) {
        return getBlacklist(origin).contains(target.toString());
    }

    public static Set<String> getBlacklist(EnumOrigin origin) {
        if (blacklist.containsKey(origin)) {
            return ImmutableSet.copyOf(blacklist.get(origin));
        } else return Collections.emptySet();
    }

    public static void init() {
        final Map<EnumOrigin, Set<String>> fileBlacklist = ConfigKismetOverride.getBlacklist();
        final Map<EnumOrigin, Set<String>> fileOverrides = ConfigKismetOverride.getOverrides();

        // add blacklist values
        fileBlacklist.forEach((origin, entries) -> entries.forEach(entry -> {
            if (isEntryValid(origin, entry)) {
                blacklist.get(origin).add(entry);
            }
        }));

        // add overrides values, checking for collision
        fileOverrides.forEach((origin, entries) -> entries.forEach(entry -> {
            if (isEntryValid(origin, entry)) {
                if (blacklist.get(origin).contains(entry)) {
                    Log.error(String.format("Found file override %s:%s already in file blacklist, skipping",
                            origin, entry));
                    return;
                }
                overrides.get(origin).add(entry);
            }
        }));
        Log.info("Loaded file overrides into memory");
    }

    private static boolean isEntryValid(EnumOrigin origin, String entry) {
        final ItemStack stack = StackHelper.getItemStack(entry);
        if (stack == null) {
            Log.error(String.format("Skipped file override %s:%s, invalid item", origin, entry));
            return false;
        }
        return true;
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

        private void storeTablesFromLocations(World world, ArrayList<ResourceLocation> remainingTableLocations,
                                              Set<ResourceLocation> entityTableLocations,
                                              Set<ResourceLocation> fishingTableLocations) {
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
