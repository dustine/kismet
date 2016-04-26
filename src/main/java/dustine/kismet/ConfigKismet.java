package dustine.kismet;

import com.google.common.collect.ImmutableList;
import dustine.kismet.target.InformedStack.ObtainableTypes;
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
    private static List<String> genBlacklist;

    private static List<String> hiddenBlacklist;

    private static Map<ObtainableTypes, Boolean> genFlags;
    private static Map<ObtainableTypes, List<String>> genLists;

    public static List<String> getHiddenBlacklist() {
        return hiddenBlacklist;
    }

    private static String[] getEnumValues(Class<? extends Enum<?>> e) {
        return Arrays.asList(e.getEnumConstants())
                .stream()
                .map(Object::toString)
                .collect(Collectors.toList()).toArray(new String[0]);
    }

    public static void preInit() {
        File configFile = new File(Loader.instance().getConfigDir(), Reference.MOD_ID + ".cfg");
        if (config == null)
            config = new Configuration(configFile);

        MinecraftForge.EVENT_BUS.register(new ServerToClientConfigSyncEventHandler());

        config.setCategoryComment(Configuration.CATEGORY_GENERAL, "General settings regarding the mod, such as " +
                "activating or deactivating recipes and setting timer durations.");

        config.setCategoryComment(Configuration.CATEGORY_GENERAL, "General settings regarding the mod, such as " +
                "activating or deactivating recipes and setting timer durations.");

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

        final Map<ObtainableTypes, Property> propGenLists = new HashMap<>();
        final Map<ObtainableTypes, Property> propGenFlags = new HashMap<>();
        for (ObtainableTypes type : ObtainableTypes.values()) {
            // skip the forced flag
            if (type.equals(ObtainableTypes.Forced)) continue;
            final String typeName = getTypeName(type);
            propGenLists.put(type, getProperty(categories, CATEGORY_TARGETS, "hidden" + typeName));
            propGenFlags.put(type, getProperty(categories, CATEGORY_TARGETS, "gen" + typeName));
        }

        final Property propGenMode = getProperty(categories, CATEGORY_TARGETS, Names.genMode);

        final Property propGenBlacklist = getProperty(categories, CATEGORY_TARGETS, Names.genBlacklist);
        final Property propHiddenBlacklist = getProperty(categories, CATEGORY_TARGETS, Names.hiddenBlacklist);
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

            genLists = new HashMap<>();
            genFlags = new HashMap<>();
            for (ObtainableTypes type : ObtainableTypes.values()) {
                // skip the forced flag
                if (type.equals(ObtainableTypes.Forced)) continue;
                genLists.put(type, Arrays.asList(propGenLists.get(type).getStringList()));
                genFlags.put(type, propGenFlags.get(type).getBoolean());
            }

            genMode = Defaults.genMode;
            String currentGenMode = propGenMode.getString();
            for (EnumGenMode m : EnumGenMode.values()) {
                if (m.toString().equals(currentGenMode)) {
                    genMode = m;
                }
            }

            genBlacklist = Arrays.asList(propGenBlacklist.getStringList());
            hiddenBlacklist = Arrays.asList(propHiddenBlacklist.getStringList());
            forceAdd = Arrays.asList(propForceAdd.getStringList());
        }

        // ---- step 4 - write the class's variables back into the config properties and save to disk

        //  This is done even for a loadFromFile==true, because some of the properties may have been assigned default
        //    values if the file was empty or corrupt.

        propChillEnabled.set(chillEnabled);
        propTimedEnabled.set(timedEnabled);

        propTimedLimit.set(timedLimit);

        for (ObtainableTypes type : ObtainableTypes.values()) {
            // skip the forced flag
            if (type.equals(ObtainableTypes.Forced)) continue;
            propGenLists.get(type).set(genLists.get(type).toArray(new String[] {}));
            propGenFlags.get(type).set(genFlags.get(type));
        }

        propGenMode.set(genMode.toString());

        propGenBlacklist.set(genBlacklist.toArray(new String[] {}));
        propHiddenBlacklist.set(hiddenBlacklist.toArray(new String[] {}));
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

        // CATEGORY: GENERAL
        ArrayList<Property> catGeneral = new ArrayList<>();
        categories.put(CATEGORY_GENERAL, catGeneral);

        Property propHasChill = config.get(CATEGORY_GENERAL, Names.chillEnabled, Defaults.chillEnabled)
                .setRequiresMcRestart(true);
        catGeneral.add(propHasChill);

        Property propHasTimed = config.get(CATEGORY_GENERAL, Names.timedEnabled, Defaults.timedEnabled)
                .setRequiresMcRestart(true);
        catGeneral.add(propHasTimed);

        Property propTimeLimit = config.get(CATEGORY_GENERAL, Names.timedLimit, Defaults.timedLimit)
                .setMinValue(timedLimitMin);
        catGeneral.add(propTimeLimit);


        // CATEGORY: TARGET_LIST
        ArrayList<Property> catTargets = new ArrayList<>();
        categories.put(CATEGORY_TARGETS, catTargets);

        // forceAdd doesn't allow just having the mod so it's obligated to have
        Pattern forceAddPattern = Pattern.compile("!?[^A-Z^ \\t\\r\\n\\v\\f]+:\\w+(:\\d+(:\\{.*\\})?)?");
        Pattern blacklistPattern = Pattern.compile("!?[^A-Z^ \\t\\r\\n\\v\\f]+(:\\w+(:\\d+(:\\{.*\\})?)?)?");

        List<Property> catGenLists = new ArrayList<>();
        List<Property> catGenFlags = new ArrayList<>();
        for (ObtainableTypes type : ObtainableTypes.values()) {
            if (type.equals(ObtainableTypes.Forced)) continue;
            Property propGenList = config.get(CATEGORY_TARGETS, "hidden" + getTypeName(type), Defaults.genLists.get
                    (type)).setShowInGui(false);
            catGenLists.add(propGenList);

            Property propGenFlag = config.get(CATEGORY_TARGETS, "gen" + getTypeName(type), Defaults.genFlags.get(type));
            catGenFlags.add(propGenFlag);
        }

        catTargets.addAll(catGenLists);

        Property propGenMode = config.get(CATEGORY_TARGETS, Names.genMode, Defaults.genMode.toString())
                .setValidValues(genModesValues);
        catTargets.add(propGenMode);

        catTargets.addAll(catGenFlags);

        Property propGenBlacklist = config.get(CATEGORY_TARGETS, Names.genBlacklist, Defaults.genFilter)
                .setValidationPattern(blacklistPattern);
        catTargets.add(propGenBlacklist);

        Property propHiddenBlacklist = config.get(CATEGORY_TARGETS, Names.hiddenBlacklist, Defaults.hiddenBlacklist)
                .setShowInGui(false);
        catTargets.add(propHiddenBlacklist);

        Property propForceAdd = config.get(CATEGORY_TARGETS, Names.forceAdd, Defaults.forceAdd)
                .setValidationPattern(forceAddPattern);
        catTargets.add(propForceAdd);

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

    private static String getTypeName(ObtainableTypes type) {
        return type.toString().substring(0, 1).toUpperCase() +
                type.toString().substring(1).toLowerCase();
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

    public static List<String> getGenBlacklist() {
        return genBlacklist;
    }

    public static boolean isChillEnabled() {
        return chillEnabled;
    }

    public static boolean isTimedEnabled() {
        return timedEnabled;
    }

    public static List<String> getGenList(ObtainableTypes type) {
        if (type.equals(ObtainableTypes.Forced)) return forceAdd;
        return genLists.get(type);
    }

    public static boolean isGenFlag(ObtainableTypes type) {
        if (type.equals(ObtainableTypes.Forced)) {
            Log.warning("Tried to check if Forced are allowed");
            return true;
        }
        return genFlags.get(type);
    }

    public static boolean isGenUnfair() {
        return genFlags.get(ObtainableTypes.Unfair);
    }


    public enum EnumGenMode {
        NONE("None"),
        FILTERED("Filtered only"),
        ALL("All");

        private String value;

        EnumGenMode(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private static class Names {
        private static final String chillEnabled = "chillEnabled";
        private static final String timedLimit = "timedLimit";
        private static final String timedEnabled = "timedEnabled";
        private static final String hiddenBlacklist = "hiddenBlacklist";
        private static final String genMode = "genMode";
        private static final String genBlacklist = "genBlacklist";
        private static final String forceAdd = "forceAdd";
    }

    private static class Defaults {
        static final EnumGenMode genMode = EnumGenMode.FILTERED;
        static final boolean chillEnabled = true;
        static final boolean timedEnabled = true;
        static final int timedLimit = 24000 / 20 / 60; // a minecraft day
        static final String[] hiddenBucketable = new String[] {
                "minecraft:milk_bucket",
                ""
        };
        static final String[] hiddenCraftable = new String[] {
                ""
        };
        static final String[] hiddenLootable = new String[] {
                "minecraft:egg",
                "minecraft:elytra",
                "minecraft:nether_star",
                "minecraft:record_11",
                "minecraft:record_blocks",
                "minecraft:record_mall",
                "minecraft:record_wait",
                "minecraft:record_stal",
                "minecraft:record_ward",
                "minecraft:record_far",
                "minecraft:record_strad",
                "minecraft:record_chirp",
                "minecraft:record_mellohi",
                ""
        };
        static final String[] hiddenMineable = new String[] {
                ""
        };
        static final String[] hiddenSilkable = new String[] {
                ""
        };
        static final String[] hiddenOthers = new String[] {
                "minecraft:vine",
                "minecraft:dragon_breath",
                ""
        };
        static final String[] hiddenUnfair = new String[] {
                "minecraft:dragon_egg",
                ""
        };
        static final String[] hiddenBlacklist = new String[] {
                // the deprecated wooden slab, still in the game as a block
                "minecraft:stone_slab:2",
                ""
        };
        static final String[] genFilter = new String[] {
                "minecraft:tipped_arrow",
                "minecraft:splash_potion",
                "minecraft:lingering_potion",
                "minecraft:wool",
                "minecraft:stained_hardened_clay",
                "minecraft:carpet",
                "minecraft:stained_glass",
                "minecraft:stained_glass_pane"
        };
        static String[] forceAdd = new String[] {
                "minecraft:wool:0",
                "minecraft:carpet:0"
        };
        static Map<ObtainableTypes, String[]> genLists = new HashMap<>();
        static Map<ObtainableTypes, Boolean> genFlags = new HashMap<>();

        static {
            Defaults.genLists.put(ObtainableTypes.Unfair, Defaults.hiddenUnfair);
            Defaults.genLists.put(ObtainableTypes.Others, Defaults.hiddenOthers);
            Defaults.genLists.put(ObtainableTypes.Bucketable, Defaults.hiddenBucketable);
            Defaults.genLists.put(ObtainableTypes.Craftable, Defaults.hiddenCraftable);
            Defaults.genLists.put(ObtainableTypes.Lootable, Defaults.hiddenLootable);
            Defaults.genLists.put(ObtainableTypes.Mineable, Defaults.hiddenMineable);
            Defaults.genLists.put(ObtainableTypes.Silkable, Defaults.hiddenSilkable);

            Defaults.genFlags.put(ObtainableTypes.Unfair, false);
            Defaults.genFlags.put(ObtainableTypes.Others, true);
            Defaults.genFlags.put(ObtainableTypes.Bucketable, true);
            Defaults.genFlags.put(ObtainableTypes.Craftable, true);
            Defaults.genFlags.put(ObtainableTypes.Lootable, true);
            Defaults.genFlags.put(ObtainableTypes.Mineable, true);
            Defaults.genFlags.put(ObtainableTypes.Silkable, true);
        }
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
                        Log.trace("Updating filtered stacks...");
                        if (Kismet.libraryFactory != null)
                            Kismet.libraryFactory.recreateLibrary();
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
            Kismet.proxy.sendConfigToClient(event.player);
        }
    }
}