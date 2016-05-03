package dustine.kismet.config;

import com.google.common.collect.ImmutableList;
import dustine.kismet.Kismet;
import dustine.kismet.Log;
import dustine.kismet.Reference;
import dustine.kismet.target.EnumOrigin;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.minecraftforge.common.config.Configuration.CATEGORY_GENERAL;

/**
 * Mod Configuration Heavily based on TheGreyGhost's MinecraftByExample Source: https://github.com/TheGreyGhost/MinecraftByExample
 */
public final class ConfigKismet {
    public static final String CATEGORY_TARGETS = "targets";
    private static final int timedLimitMin = 1;
    private static final String[] genModesValues = getEnumValues(EnumGenMode.class);
    private static Configuration config;
    private static boolean chillEnabled;
    private static boolean timedEnabled;
    private static int timedLimit;
    private static List<String> forceAdd;
    private static EnumGenMode genMode;
    private static List<String> genFilter;

    private static Map<EnumOrigin, Boolean> genFlags;

    private static String[] getEnumValues(Class<? extends Enum<?>> e) {
        return Arrays.asList(e.getEnumConstants())
                .stream()
                .map(Object::toString)
                .collect(Collectors.toList()).toArray(new String[0]);
    }

    public static void init() {
        File configFile = new File(Loader.instance().getConfigDir(), Reference.MOD_ID + ".cfg");
        if (config == null)
            config = new Configuration(configFile);

        MinecraftForge.EVENT_BUS.register(new ServerToClientConfigSyncEventHandler());

        config.setCategoryComment(Configuration.CATEGORY_GENERAL, "General settings regarding the mod, such as " +
                "activating or deactivating recipes and setting timer durations.");

        config.setCategoryComment(CATEGORY_TARGETS, "Settings regarding which items the mod will use as targets " +
                "(goal of obtaining the item), and which algorithms and metrics to use to compile these targets.");

        syncFromFile();
    }

    /**
     * Loads config from disk, overriding current config Only needed on mod's preInit
     */
    private static void syncFromFile() {
        syncConfig(true, true);
    }

    /**
     * Synchronise the three copies of the data 1) loadConfigFromFile && readFieldsFromConfig -> initialise everything
     * from the disk file 2) !loadConfigFromFile && readFieldsFromConfig --> copy everything from the config file
     * (altered by GUI) 3) !loadConfigFromFile && !readFieldsFromConfig --> copy everything from the native fields
     *
     * @param loadConfigFromFile   if true, load the config field from the config file on disk
     * @param readFieldsFromConfig if true, reload the member variables from the config field
     */

    private static void syncConfig(boolean loadConfigFromFile, boolean readFieldsFromConfig) {
        // ---- step 1 - load raw values from config file (if loadFromFile true)
        if (loadConfigFromFile) {
            config.load();
        }

        // ---- step 2 - define the properties in the config file
        final Map<String, List<Property>> categories = getCategories();
        final Property propChillEnabled = getProperty(categories, CATEGORY_GENERAL, Names.chillEnabled);
        final Property propTimedEnabled = getProperty(categories, CATEGORY_GENERAL, Names.timedEnabled);
        final Property propTimedLimit = getProperty(categories, CATEGORY_GENERAL, Names.timedLimit);

        final Map<EnumOrigin, Property> propGenFlags = new HashMap<>();

        for (EnumOrigin type : EnumOrigin.values()) {
            // skip the forced flag
            if (type.equals(EnumOrigin.FORCED)) continue;
            propGenFlags.put(type, getProperty(categories, CATEGORY_TARGETS, "gen" + getTypeName(type)));
        }

        final Property propGenMode = getProperty(categories, CATEGORY_TARGETS, Names.genMode);

        final Property propGenBlacklist = getProperty(categories, CATEGORY_TARGETS, Names.genBlacklist);
        final Property propForceAdd = getProperty(categories, CATEGORY_TARGETS, Names.forceAdd);

        // config field order, one per category
        for (String catKey : categories.keySet()) {
            ArrayList<String> propNames = categories.get(catKey).stream()
                    .map(Property::getName)
                    .collect(Collectors.toCollection(ArrayList::new));
            config.setCategoryPropertyOrder(catKey, propNames);
        }

        // ---- step 3 - read the config property values into the class's variables (if readFieldsFromConfig)
        /*
           As each value is read from the property, it should be checked to make sure it is valid, in case someone
           has manually edited or corrupted the value.  The get() methods don't check that the value is in range even if
           you have specified a MIN and MAX value of the property
        */

        if (readFieldsFromConfig) {
            chillEnabled = propChillEnabled.getBoolean();
            timedEnabled = propTimedEnabled.getBoolean();

            timedLimit = propTimedLimit.getInt();
            if (timedLimit < timedLimitMin) {
                timedLimit = Defaults.timedLimit;
            }
            genFlags = new HashMap<>();

            for (EnumOrigin type : EnumOrigin.values()) {
                // skip the forced flag
                if (type.equals(EnumOrigin.FORCED)) continue;
                genFlags.put(type, propGenFlags.get(type).getBoolean());
            }

            genMode = Defaults.genMode;
            String currentGenMode = propGenMode.getString();
            for (EnumGenMode m : EnumGenMode.values()) {
                if (m.toString().equals(currentGenMode)) {
                    genMode = m;
                }
            }

            genFilter = Arrays.asList(propGenBlacklist.getStringList());
            forceAdd = Arrays.asList(propForceAdd.getStringList());
        }

        // ---- step 4 - write the class's variables back into the config properties and save to disk

        //  This is done even for a loadFromFile==true, because some of the properties may have been assigned default
        //    values if the file was empty or corrupt.

        propChillEnabled.set(chillEnabled);
        propTimedEnabled.set(timedEnabled);

        propTimedLimit.set(timedLimit);

        for (EnumOrigin type : EnumOrigin.values()) {
            // skip the forced flag
            if (type.equals(EnumOrigin.FORCED)) continue;
            propGenFlags.get(type).set(genFlags.get(type));
        }

        propGenMode.set(genMode.toString());

        propGenBlacklist.set(genFilter.toArray(new String[] {}));
        propForceAdd.set(forceAdd.toArray(new String[] {}));

        if (config.hasChanged()) {
            config.save();
        }
    }

    private static Property getProperty(Map<String, List<Property>> categories, String category, String propertyName) {
        return categories.get(category).stream()
                .filter(property -> property.getName().equalsIgnoreCase(propertyName))
                .findFirst().orElse(null);
    }

    private static Map<String, List<Property>> getCategories() {
        Map<String, List<Property>> categories = new HashMap<>();
        addCatGeneralProperties(categories);
        addCatTargetsProperties(categories);

        // final operation: adding language keys
        for (String categoryName : categories.keySet()) {
            // adds the language keys according to the pattern "gui.config.catName.propName"
            // the used keys are equivalent to the names in this case
            List<Property> category = categories.get(categoryName);
            for (Property prop : category) {
                prop.setLanguageKey(String.format("gui.config.%s.%s", categoryName, prop.getName()));
            }
        }

        return categories;
    }

    private static void addCatTargetsProperties(Map<String, List<Property>> categories) {
        // CATEGORY: TARGET_LIST
        ArrayList<Property> catTargets = new ArrayList<>();

        // forceAdd doesn't allow just having the mod so it's obligated to have
        Pattern forceAddPattern = Pattern.compile("!?[^A-Z^ \\t\\r\\n\\v\\f]+:\\w+(:\\d+(:\\{.*\\})?)?");
        Pattern blacklistPattern = Pattern.compile("!?[^A-Z^ \\t\\r\\n\\v\\f]+(:\\w+(:\\d+(:\\{.*\\})?)?)?");

        Property propForceAdd = config.get(CATEGORY_TARGETS, Names.forceAdd, Defaults.forceAdd)
                .setValidationPattern(forceAddPattern);
        propForceAdd.setComment("List of items that will be considered as possible targets, ignoring any generation " +
                "settings");
        catTargets.add(propForceAdd);

        Property propGenMode = config.get(CATEGORY_TARGETS, Names.genMode, Defaults.genMode.toString())
                .setValidValues(genModesValues);
        propGenMode.setComment("Defines if the mod will register possible targets from the game registries, either " +
                "none at all, only ones with origin according to genFlags (ex: items in a crafting recipe of any " +
                "kind are flagged as 'recipe') or any valid minecraft item at all, respectively");
        catTargets.add(propGenMode);

        addGenFlagProperties(catTargets);

        Property propGenBlacklist = config.get(CATEGORY_TARGETS, Names.genBlacklist, Defaults.genBlacklist)
                .setValidationPattern(blacklistPattern);
        propGenBlacklist.setComment("List of items that will be ignored on target selection");
        catTargets.add(propGenBlacklist);

        categories.put(CATEGORY_TARGETS, catTargets);
    }

    private static void addGenFlagProperties(ArrayList<Property> catTargets) {
        List<Property> catGenFlags = new ArrayList<>();
        for (EnumOrigin type : EnumOrigin.getSorted(false)) {
            Property propGenFlag = config.get(CATEGORY_TARGETS, "gen" + getTypeName(type), true);
            propGenFlag.setComment(getComment(type));
            catGenFlags.add(propGenFlag);
        }
        catTargets.addAll(catGenFlags);
    }

    private static String getComment(EnumOrigin type) {
        switch (type) {
            case FORCED:
                return "Uh, this isn't supposed to happen, FORCED shouldn't ever be in the config";
            case OTHER:
                return "Allows miscellaneous items as targets";
            case FLUID:
                return "Allows fluids, in bucket form, as targets";
            case RECIPE:
                return "Allows items with associated crafting/smelting/mod recipes as targets";
            case LOOT_TABLE:
                return "Allows items present in the loot tables (chest loot) as targets";
            case MOB_DROP:
                return "Allows items obtainable from mobs or animals (loot tables) as targets";
            case TRADE:
                return "Allows items resulting from villager trading as targets";
            case BLOCK_DROP:
                return "Allows items that are dropped from breaking blocks as targets";
            case SILK_TOUCH:
                return "Allows blocks that can be mined with silk touch as targets";
            case SHEAR:
                return "Allows shearable blocks as targets";
            case FISHING:
                return "Allows fishing results (loot tables) as targets";
            default:
                return "Uh, this isn't supposed to happen, this is the default comment";
        }
    }

    /**
     * Returns a origin name, which normally is in THIS_FORMAT, into thisFormat
     *
     * @param origin Origin
     * @return A camelCase origin name
     */
    private static String getTypeName(EnumOrigin origin) {
        final String camelCase = origin.toCamelCase();
        return camelCase.substring(0, 1).toUpperCase() + camelCase.substring(1);
    }

    private static void addCatGeneralProperties(Map<String, List<Property>> categories) {
        // CATEGORY: GENERAL
        ArrayList<Property> catGeneral = new ArrayList<>();

        Property propHasChill = config.get(CATEGORY_GENERAL, Names.chillEnabled, Defaults.chillEnabled)
                .setRequiresMcRestart(true);
        propHasChill.setComment("Set to true to enable the crafting recipe to the Kismet Display (the one without a " +
                "timer)");
        catGeneral.add(propHasChill);

        Property propHasTimed = config.get(CATEGORY_GENERAL, Names.timedEnabled, Defaults.timedEnabled)
                .setRequiresMcRestart(true);
        propHasTimed.setComment("Set to true to enable the crafting recipe to the Timed Kismet Display (the one with " +
                "a timer)");
        catGeneral.add(propHasTimed);

        Property propTimeLimit = config.get(CATEGORY_GENERAL, Names.timedLimit, Defaults.timedLimit)
                .setMinValue(timedLimitMin);
        propTimeLimit.setComment("The time limit for each goal in the Timed Kismet Display, in minutes");
        catGeneral.add(propTimeLimit);

        categories.put(CATEGORY_GENERAL, catGeneral);
    }

    /**
     * Returns an immutable copy of the properties and their categories within the mod
     *
     * @return An immutable list of all properties in this config
     */
    public static ImmutableList<Property> getImmutableCategory(String category) {
        return ImmutableList.copyOf(getCategories().get(category));
    }

    public static Configuration getConfig() {
        return config;
    }

    public static void clientPreInit() {

        MinecraftForge.EVENT_BUS.register(new ClientConfigEventHandler());
    }

    /**
     * Save the GUI-stored values, without accessing disk config Not needed to use
     */
    private static void syncFromGUI() {
        syncConfig(false, true);
    }

    public static void clientSync() {

    }

    public static int getTimedLimit() {
        return timedLimit;
    }

    /**
     * Saves the config present on the variables in this class Run this method every time you change one of the
     * variables
     */
    private static void syncFromFields() {
        syncConfig(false, false);
    }

    public static List<String> getForceAdd() {
        return forceAdd;
    }

    public static EnumGenMode getGenMode() {
        return genMode;
    }

    public static List<String> getGenFilter() {
        return ImmutableList.copyOf(genFilter);
    }

    public static boolean isChillEnabled() {
        return chillEnabled;
    }

    public static boolean isTimedEnabled() {
        return timedEnabled;
    }

    public static boolean isGenFlag(EnumOrigin type) {
        if (type.equals(EnumOrigin.FORCED)) {
            Log.warning("Tried to check if FORCED are allowed");
            return true;
        }
        return genFlags.get(type);
    }

    public enum EnumGenMode {
        NONE("None"),
        FILTERED("Filtered"),
        ALL("All");

        private String value;

        EnumGenMode(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }

    private static class Names {
        private static final String chillEnabled = "chillEnabled";
        private static final String timedLimit = "timedLimit";
        private static final String timedEnabled = "timedEnabled";
        private static final String genMode = "genMode";
        private static final String genBlacklist = "genFilter";
        private static final String forceAdd = "forceAdd";
    }

    private static class Defaults {
        static final EnumGenMode genMode = EnumGenMode.FILTERED;
        static final boolean chillEnabled = true;
        static final boolean timedEnabled = true;
        static final int timedLimit = 24000 / 20 / 60; // a minecraft day
        static final String[] genBlacklist = new String[] {
                "kismet",
                "minecraft:carpet",
                "minecraft:dragon_egg",
                "minecraft:lingering_potion",
                "minecraft:splash_potion",
                "minecraft:stained_glass",
                "minecraft:stained_glass_pane",
                "minecraft:stained_hardened_clay",
                "minecraft:tipped_arrow",
                "minecraft:wool"
        };
        static String[] forceAdd = new String[] {
                "minecraft:wool:0",
                "minecraft:carpet:0"
        };
    }

    private static class ClientConfigEventHandler {
        /*
         * This class, when instantiated as an object, will listen on the FML
         *  event bus for an OnConfigChangedEvent
         */
        @SubscribeEvent(priority = EventPriority.NORMAL)
        public void onEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (Reference.MOD_ID.equals(event.getModID())) {
                syncFromGUI();
                final String category = event.getConfigID();
                if (category != null) {
                    // force a library refresh if in-world and any changes occured regarding the target category
                    if (category.equals(ConfigKismet.CATEGORY_TARGETS)) {
                        Log.trace("Refreshing target library...");
                        if (Kismet.databaseBuilder != null)
                            Kismet.databaseBuilder.tryBuildLibraryWithLastGeneratedDatabase();
                    }
                    Log.debug("Config changed on GUI, category " + category);
                } else {
                    Log.debug("Config changed on GUI, no category");
                }
            }
        }
    }

    private static class ServerToClientConfigSyncEventHandler {
        @SubscribeEvent
        public void onEvent(PlayerEvent.PlayerLoggedInEvent event) {
            Kismet.proxy.sendConfigToClient((EntityPlayerMP) event.player);
        }
    }
}