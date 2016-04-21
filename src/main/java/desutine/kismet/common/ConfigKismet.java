package desutine.kismet.common;

import com.google.common.collect.ImmutableList;
import desutine.kismet.Kismet;
import desutine.kismet.ModLogger;
import desutine.kismet.Reference;
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
 * Mod Configuration
 * Heavily based on TheGreyGhost's MinecraftByExample
 * Source: https://github.com/TheGreyGhost/MinecraftByExample
 */
public class ConfigKismet {
    public static final String CATEGORY_TARGETS = "targets";

    private static final int TIMED_LIMIT_DEFAULT = 24000 / 20 / 60; // a minecraft day
    private static final int TIMED_LIMIT_MIN = 1;
    private static final String genModeName = "genMode";
    private static final String genFilterName = "genFilter";
    private static final String timedLimitName = "timedLimit";
    private static final String[] GEN_MODE_VALUES = getEnumValues(EnumGenMode.class);
    private static final EnumGenMode GEN_MODE_DEFAULT = EnumGenMode.STRICTLY_OBTAINABLE;
    private static final boolean HAS_CHILL_DEFAULT = true;
    private static final boolean HAS_TIMED_DEFAULT = true;
    private static boolean chillEnabled;
    private static boolean timedEnabled;
    private static int timedLimit;
    private static String[] forceAdd;
    private static EnumGenMode genMode;
    private static String[] genFilter;
    private static Configuration config;
    private static String forceAddName = "forceAdd";
    private static String chillEnabledName = "chillEnabled";
    private static String timedEnabledName = "timedEnabled";
    private static String[] hiddenCraftableDefault = new String[] {
            ""
    };
    private static String[] hiddenMineableDefault = new String[] {
            ""
    };
    private static String[] hiddenSilkableDefault = new String[] {
            "!minecraft:tallgrass:0",
            "!minecraft:tallgrass:1",
            ""
    };
    private static String[] hiddenLootableDefault = new String[] {
            "minecraft:egg",
            "minecraft:elytra",
            "minecraft:record_11",
            "minecraft:record_ward",
            "minecraft:record_far",
            "minecraft:record_blocks",
            "minecraft:record_strad",
            "minecraft:nether_star",
            "minecraft:nether_stal",
            "minecraft:record_chirp",
            "minecraft:record_wait",
            "minecraft:record_mellohi",
            "-minecraft:record_mall",
            ""
    };
    private static String[] hiddenBucketableDefault = new String[] {
            "minecraft:milk_bucket",
            ""
    };
    private static String[] hiddenOthersDefault = new String[] {
            "minecraft:vine",
            "minecraft:dragon_breath",
            ""
    };
    private static String[] genFilterDefault = new String[] {
            "minecraft:tipped_arrow",
            "minecraft:splash_potion",
            "minecraft:lingering_potion",
            "minecraft:wool:*",
            "minecraft:stained_hardened_clay"
    };

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

        // fill internal genFilter
//        TargetHelper.addToInternalBlacklist(new String[]{"minecraft:air"});

        syncFromFile();
    }

    /**
     * Loads config from disk, overriding current config
     * Only needed on mod's preInit
     */
    private static void syncFromFile() {
        syncConfig(true, true);
    }

    /**
     * Synchronise the three copies of the data
     * 1) loadConfigFromFile && readFieldsFromConfig -> initialise everything from the disk file
     * 2) !loadConfigFromFile && readFieldsFromConfig --> copy everything from the config file (altered by GUI)
     * 3) !loadConfigFromFile && !readFieldsFromConfig --> copy everything from the native fields
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
        final Property propChillEnabled = getProperty(categories, CATEGORY_GENERAL, chillEnabledName);
        final Property propTimedEnabled = getProperty(categories, CATEGORY_GENERAL, timedEnabledName);
        final Property propTimedLimit = getProperty(categories, CATEGORY_GENERAL, timedLimitName);

        final Property propGenMode = getProperty(categories, CATEGORY_TARGETS, genModeName);
        final Property propGenFilter = getProperty(categories, CATEGORY_TARGETS, genFilterName);
        final Property propForceAdd = getProperty(categories, CATEGORY_TARGETS, forceAddName);

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
            if (timedLimit < TIMED_LIMIT_MIN) {
                timedLimit = TIMED_LIMIT_DEFAULT;
            }

            hiddenForceAdd = propHiddenForceAdd.getStringList();

            forceAdd = propForceAdd.getStringList();

            genMode = GEN_MODE_DEFAULT;
            String currentGenMode = propGenMode.getString();
            for (EnumGenMode m : EnumGenMode.values()) {
                if (m.toString().equals(currentGenMode)) {
                    genMode = m;
                }
            }

            genFilter = propGenFilter.getStringList();
        }

        // ---- step 4 - write the class's variables back into the config properties and save to disk

        //  This is done even for a loadFromFile==true, because some of the properties may have been assigned default
        //    values if the file was empty or corrupt.

        propChillEnabled.set(chillEnabled);
        propTimedEnabled.set(timedEnabled);
        propTimedLimit.set(timedLimit);
        propHiddenForceAdd.set(hiddenForceAdd);
        propForceAdd.set(forceAdd);
        propGenMode.set(genMode.toString());
        propGenFilter.set(genFilter);

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

        Property propHasChill = config.get(CATEGORY_GENERAL, chillEnabledName, HAS_CHILL_DEFAULT)
                .setRequiresMcRestart(true);
        catGeneral.add(propHasChill);

        Property propHasTimed = config.get(CATEGORY_GENERAL, timedEnabledName, HAS_TIMED_DEFAULT)
                .setRequiresMcRestart(true);
        catGeneral.add(propHasTimed);

        Property propTimeLimit = config.get(CATEGORY_GENERAL, timedLimitName, TIMED_LIMIT_DEFAULT)
                .setMinValue(TIMED_LIMIT_MIN);
        catGeneral.add(propTimeLimit);


        // CATEGORY: TARGET_LIST
        ArrayList<Property> catTargets = new ArrayList<>();
        categories.put(CATEGORY_TARGETS, catTargets);

        // forceAdd doesn't allow just having the mod so it's obligated to have
        Pattern forceAddPattern = Pattern.compile("!?[^A-Z^ \\t\\r\\n\\v\\f]+:\\w+(:\\d+(:\\{.*\\})?)?");
        Pattern filterPattern = Pattern.compile("!?[^A-Z^ \\t\\r\\n\\v\\f]+(:\\w+(:\\d+(:\\{.*\\})?)?)?");

        Property propHiddenCraftable = config.get(CATEGORY_TARGETS, hiddenCraftableName, hiddenCraftableDefault)
                .setShowInGui(false)
                .setRequiresWorldRestart(true);
        catTargets.add(propHiddenCraftable);

        Property propHiddenMineable = config.get(CATEGORY_TARGETS, hiddenMineableName, hiddenMineableDefault)
                .setShowInGui(false)
                .setRequiresWorldRestart(true);
        catTargets.add(propHiddenMineable);

        Property propHiddenSilkable = config.get(CATEGORY_TARGETS, hiddenSilkableName, hiddenSilkableDefault)
                .setShowInGui(false)
                .setRequiresWorldRestart(true);
        catTargets.add(propHiddenSilkable);

        Property propHiddenLootable = config.get(CATEGORY_TARGETS, hiddenLootableName, hiddenLootableDefault)
                .setShowInGui(false)
                .setRequiresWorldRestart(true);
        catTargets.add(propHiddenLootable);

        Property propHiddenBucketable = config.get(CATEGORY_TARGETS, hiddenBucketableName, hiddenBucketableDefault)
                .setShowInGui(false)
                .setRequiresWorldRestart(true);
        catTargets.add(propHiddenBucketable);

        Property propHiddenOthers = config.get(CATEGORY_TARGETS, hiddenOthersName, hiddenOthersDefault)
                .setShowInGui(false)
                .setRequiresWorldRestart(true);
        catTargets.add(propHiddenOthers);

        Property propHiddenUnfair = config.get(CATEGORY_TARGETS, hiddenUnfairName, hiddenUnfairDefault)
                .setShowInGui(false)
                .setRequiresWorldRestart(true);
        catTargets.add(propHiddenUnfair);

        Property propGenMode = config.get(CATEGORY_TARGETS, genModeName, GEN_MODE_DEFAULT.toString())
                .setValidValues(GEN_MODE_VALUES);
        catTargets.add(propGenMode);

        Property propGenCraftable = config.get(CATEGORY_TARGETS, genCraftableName, true);
        Property propGenMineable = config.get(CATEGORY_TARGETS, genMineableName, true);
        Property propGenSilkable = config.get(CATEGORY_TARGETS, genSilkableName, true);
        Property propGenLootable = config.get(CATEGORY_TARGETS, genLootableName, true);
        Property propGenUnfair = config.get(CATEGORY_TARGETS, genUnfairName, false);
        catTargets.add(propGenCraftable);
        catTargets.add(propGenMineable);
        catTargets.add(propGenSilkable);
        catTargets.add(propGenLootable);
        catTargets.add(propGenUnfair);

        Property propGenFilter = config.get(CATEGORY_TARGETS, genFilterName, genFilterDefault)
                .setValidationPattern(filterPattern);
        catTargets.add(propGenFilter);

        Property propForceAdd = config.get(CATEGORY_TARGETS, forceAddName, new String[] {})
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
     * Save the GUI-stored values, without accessing disk config
     * Not needed to use
     */
    private static void syncFromGUI() {
        syncConfig(false, true);
    }

    public static void clientSync(Map<String, Object> syncValues) {

    }

    public static int getTimedLimit() {
        return timedLimit;
    }

    public static void setTimedLimit(int timedLimit) {
        ConfigKismet.timedLimit = timedLimit;
        syncFromFields();
    }

    /**
     * Saves the config present on the variables in this class
     * Run this method every time you change one of the variables
     */
    private static void syncFromFields() {
        syncConfig(false, false);
    }

    public static String[] getForceAdd() {
        return forceAdd;
    }

    public static void setForceAdd(String[] forceAdd) {
        ConfigKismet.forceAdd = forceAdd;
        syncFromFields();
    }

    public static EnumGenMode getGenMode() {
        return genMode;
    }

    public static void setGenMode(EnumGenMode genMode) {
        ConfigKismet.genMode = genMode;
        syncFromFields();
    }

    public static String[] getGenFilter() {
        return genFilter;
    }

    public static void setGenFilter(String[] genFilter) {
        ConfigKismet.genFilter = genFilter;
        syncFromFields();
    }

    public static boolean isChillEnabled() {
        return chillEnabled;
    }

    public static void setChillEnabled(boolean chillEnabled) {
        ConfigKismet.chillEnabled = chillEnabled;
        syncFromFields();
    }

    public static boolean isTimedEnabled() {
        return timedEnabled;
    }

    public static void setTimedEnabled(boolean timedEnabled) {
        ConfigKismet.timedEnabled = timedEnabled;
        syncFromFields();
    }

    public enum EnumGenMode {
        DISABLED("Disabled"),
        STRICTLY_OBTAINABLE("Only obtainable"),
        ENABLED("Enabled");

        private String value;

        EnumGenMode(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
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
                        ModLogger.trace("Updating filtered stacks...");
                        if (Kismet.libraryFactory != null)
                            Kismet.libraryFactory.recreateLibrary();
                    }
                    ModLogger.debug("Config changed on GUI, category " + category);
                } else {
                    ModLogger.debug("Config changed on GUI, no category");
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