package desutine.kismet;

import com.google.common.collect.ImmutableList;
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
    private static List<String> hiddenBucketable;
    private static List<String> hiddenCraftable;
    private static List<String> hiddenMineable;
    private static List<String> hiddenSilkable;
    private static List<String> hiddenLootable;
    private static List<String> hiddenOthers;
    private static List<String> hiddenUnfair;
    private static List<String> hiddenBlacklist;
    private static boolean genCraftable;
    private static boolean genMineable;
    private static boolean genBucketable;
    private static boolean genLootable;
    private static boolean genOthers;
    private static boolean genSilkable;
    private static boolean genUnfair;

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
        final Property propChillEnabled = getProperty(categories, CATEGORY_GENERAL, Names.chillEnabled);
        final Property propTimedEnabled = getProperty(categories, CATEGORY_GENERAL, Names.timedEnabled);
        final Property propTimedLimit = getProperty(categories, CATEGORY_GENERAL, Names.timedLimit);

        final Property propHiddenCraftable = getProperty(categories, CATEGORY_TARGETS, Names.hiddenCraftable);
        final Property propHiddenMineable = getProperty(categories, CATEGORY_TARGETS, Names.hiddenMineable);
        final Property propHiddenSilkable = getProperty(categories, CATEGORY_TARGETS, Names.hiddenSilkable);
        final Property propHiddenLootable = getProperty(categories, CATEGORY_TARGETS, Names.hiddenLootable);
        final Property propHiddenBucketable = getProperty(categories, CATEGORY_TARGETS, Names.hiddenBucketable);
        final Property propHiddenOthers = getProperty(categories, CATEGORY_TARGETS, Names.hiddenOthers);
        final Property propHiddenUnfair = getProperty(categories, CATEGORY_TARGETS, Names.hiddenUnfair);

        final Property propGenMode = getProperty(categories, CATEGORY_TARGETS, Names.genMode);

        final Property propGenCraftable = getProperty(categories, CATEGORY_TARGETS, Names.genCraftable);
        final Property propGenMineable = getProperty(categories, CATEGORY_TARGETS, Names.genMineable);
        final Property propGenSilkable = getProperty(categories, CATEGORY_TARGETS, Names.genSilkable);
        final Property propGenLootable = getProperty(categories, CATEGORY_TARGETS, Names.genLootable);
        final Property propGenBucketable = getProperty(categories, CATEGORY_TARGETS, Names.genBucketable);
        final Property propGenOthers = getProperty(categories, CATEGORY_TARGETS, Names.genOthers);
        final Property propGenUnfair = getProperty(categories, CATEGORY_TARGETS, Names.genUnfair);

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

            if (loadConfigFromFile) {
                hiddenBucketable = Arrays.asList(propHiddenBucketable.getStringList());
                hiddenCraftable = Arrays.asList(propHiddenCraftable.getStringList());
                hiddenLootable = Arrays.asList(propHiddenLootable.getStringList());
                hiddenMineable = Arrays.asList(propHiddenMineable.getStringList());
                hiddenSilkable = Arrays.asList(propHiddenSilkable.getStringList());
                hiddenOthers = Arrays.asList(propHiddenOthers.getStringList());
                hiddenUnfair = Arrays.asList(propHiddenUnfair.getStringList());

                hiddenBlacklist = Arrays.asList(propHiddenBlacklist.getStringList());
            }

            genMode = Defaults.genMode;
            String currentGenMode = propGenMode.getString();
            for (EnumGenMode m : EnumGenMode.values()) {
                if (m.toString().equals(currentGenMode)) {
                    genMode = m;
                }
            }

            genBucketable = propGenBucketable.getBoolean();
            genCraftable = propGenCraftable.getBoolean();
            genLootable = propGenLootable.getBoolean();
            genMineable = propGenMineable.getBoolean();
            genSilkable = propGenSilkable.getBoolean();
            genOthers = propGenOthers.getBoolean();
            genUnfair = propGenUnfair.getBoolean();

            genBlacklist = Arrays.asList(propGenBlacklist.getStringList());

            forceAdd = Arrays.asList(propForceAdd.getStringList());
        }

        // ---- step 4 - write the class's variables back into the config properties and save to disk

        //  This is done even for a loadFromFile==true, because some of the properties may have been assigned default
        //    values if the file was empty or corrupt.

        propChillEnabled.set(chillEnabled);
        propTimedEnabled.set(timedEnabled);

        propTimedLimit.set(timedLimit);

        propHiddenBucketable.set(hiddenBucketable.toArray(new String[] {}));
        propHiddenCraftable.set(hiddenCraftable.toArray(new String[] {}));
        propHiddenLootable.set(hiddenLootable.toArray(new String[] {}));
        propHiddenMineable.set(hiddenMineable.toArray(new String[] {}));
        propHiddenOthers.set(hiddenOthers.toArray(new String[] {}));
        propHiddenSilkable.set(hiddenSilkable.toArray(new String[] {}));
        propHiddenUnfair.set(hiddenUnfair.toArray(new String[] {}));

        propGenMode.set(genMode.toString());

        propGenBucketable.set(genBucketable);
        propGenCraftable.set(genCraftable);
        propGenLootable.set(genLootable);
        propGenMineable.set(genMineable);
        propGenOthers.set(genOthers);
        propGenSilkable.set(genSilkable);
        propGenUnfair.set(genUnfair);

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

        Property propHiddenBucketable = config.get(CATEGORY_TARGETS, Names.hiddenBucketable, Defaults.hiddenBucketable)
                .setShowInGui(false);
        catTargets.add(propHiddenBucketable);

        Property propHiddenCraftable = config.get(CATEGORY_TARGETS, Names.hiddenCraftable, Defaults.hiddenCraftable)
                .setShowInGui(false);
        catTargets.add(propHiddenCraftable);

        Property propHiddenLootable = config.get(CATEGORY_TARGETS, Names.hiddenLootable, Defaults.hiddenLootable)
                .setShowInGui(false);
        catTargets.add(propHiddenLootable);

        Property propHiddenMineable = config.get(CATEGORY_TARGETS, Names.hiddenMineable, Defaults.hiddenMineable)
                .setShowInGui(false);
        catTargets.add(propHiddenMineable);

        Property propHiddenSilkable = config.get(CATEGORY_TARGETS, Names.hiddenSilkable, Defaults.hiddenSilkable)
                .setShowInGui(false);
        catTargets.add(propHiddenSilkable);

        Property propHiddenOthers = config.get(CATEGORY_TARGETS, Names.hiddenOthers, Defaults.hiddenOthers)
                .setShowInGui(false);
        catTargets.add(propHiddenOthers);

        Property propHiddenUnfair = config.get(CATEGORY_TARGETS, Names.hiddenUnfair, Defaults.hiddenUnfair)
                .setShowInGui(false);
        catTargets.add(propHiddenUnfair);

        Property propGenMode = config.get(CATEGORY_TARGETS, Names.genMode, Defaults.genMode.toString())
                .setValidValues(genModesValues);
        catTargets.add(propGenMode);

        Property propGenCraftable = config.get(CATEGORY_TARGETS, Names.genCraftable, true);
        Property propGenMineable = config.get(CATEGORY_TARGETS, Names.genMineable, true);
        Property propGenSilkable = config.get(CATEGORY_TARGETS, Names.genSilkable, true);
        Property propGenLootable = config.get(CATEGORY_TARGETS, Names.genLootable, true);
        Property propGenBucketable = config.get(CATEGORY_TARGETS, Names.genBucketable, true);
        Property propGenOthers = config.get(CATEGORY_TARGETS, Names.genOthers, true);
        Property propGenUnfair = config.get(CATEGORY_TARGETS, Names.genUnfair, false);
        catTargets.add(propGenCraftable);
        catTargets.add(propGenMineable);
        catTargets.add(propGenSilkable);
        catTargets.add(propGenLootable);
        catTargets.add(propGenBucketable);
        catTargets.add(propGenOthers);
        catTargets.add(propGenUnfair);

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

    public static void clientSync() {

    }

    public static int getTimedLimit() {
        return timedLimit;
    }

    /**
     * Saves the config present on the variables in this class
     * Run this method every time you change one of the variables
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

    public static boolean isGenUnfair() {
        return genUnfair;
    }

    public static boolean isGenBucketable() {
        return genBucketable;
    }

    public static boolean isGenCraftable() {
        return genCraftable;
    }

    public static boolean isGenLootable() {
        return genLootable;
    }

    public static boolean isGenMineable() {
        return genMineable;
    }

    public static boolean isGenSilkable() {
        return genSilkable;
    }

    public static boolean isGenOthers() {
        return genOthers;
    }

    public static List<String> getHiddenBucketable() {
        return hiddenBucketable;
    }

    public static List<String> getHiddenCraftable() {
        return hiddenCraftable;
    }

    public static List<String> getHiddenLootable() {
        return hiddenLootable;
    }

    public static List<String> getHiddenMineable() {
        return hiddenMineable;
    }

    public static List<String> getHiddenSilkable() {
        return hiddenSilkable;
    }

    public static List<String> getHiddenOthers() {
        return hiddenOthers;
    }

    public static List<String> getHiddenUnfair() {
        return hiddenUnfair;
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
        private static final String hiddenBucketable = "hiddenBucketable";
        private static final String hiddenCraftable = "hiddenCraftable";
        private static final String hiddenLootable = "hiddenLootable";
        private static final String hiddenMineable = "hiddenMineable";
        private static final String hiddenSilkable = "hiddenSilkable";
        private static final String hiddenOthers = "hiddenOthers";
        private static final String hiddenUnfair = "hiddenUnfair";
        private static final String hiddenBlacklist = "hiddenBlacklist";
        private static final String genMode = "genMode";
        private static final String genBucketable = "genBucketable";
        private static final String genCraftable = "genCraftable";
        private static final String genLootable = "genLootable";
        private static final String genMineable = "genMineable";
        private static final String genSilkable = "genSilkable";
        private static final String genOthers = "genOthers";
        private static final String genUnfair = "genUnfair";
        private static final String genBlacklist = "genBlacklist";
        private static final String forceAdd = "forceAdd";
    }

    private static class Defaults {
        private static final EnumGenMode genMode = EnumGenMode.FILTERED;
        private static final boolean chillEnabled = true;
        private static final boolean timedEnabled = true;
        private static final int timedLimit = 24000 / 20 / 60; // a minecraft day
        private static final String[] hiddenBucketable = new String[] {
                "minecraft:milk_bucket"
        };
        private static final String[] hiddenCraftable = new String[] {
                ""
        };
        private static final String[] hiddenLootable = new String[] {
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
        private static final String[] hiddenMineable = new String[] {
                ""
        };
        private static final String[] hiddenSilkable = new String[] {
                "!minecraft:tallgrass:0",
                "!minecraft:tallgrass:1",
                ""
        };
        private static final String[] hiddenOthers = new String[] {
                "minecraft:vine",
                "minecraft:dragon_breath",
                ""
        };
        private static final String[] hiddenUnfair = new String[] {
                "minecraft:dragon_egg",
                ""
        };
        private static final String[] hiddenBlacklist = new String[] {
                // the deprecated wooden slab, still in the game as a block
                "minecraft:stone_slab:2",
                ""
        };
        private static final String[] genFilter = new String[] {
                "minecraft:tipped_arrow",
                "minecraft:splash_potion",
                "minecraft:lingering_potion",
                "minecraft:wool",
                "minecraft:stained_hardened_clay",
                ""
        };
        public static String[] forceAdd = new String[] {
                "minecraft:wool:0"
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