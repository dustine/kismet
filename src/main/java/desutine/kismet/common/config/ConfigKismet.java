package desutine.kismet.common.config;

import com.google.common.collect.ImmutableList;
import desutine.kismet.Kismet;
import desutine.kismet.ModLogger;
import desutine.kismet.reference.Reference;
import net.minecraft.item.ItemStack;
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
    // CONFIG: TARGET LIST
    public static final String CATEGORY_LIST = "list";

    /* START CONFIG FIELDS */
    private static final String[] ADD_MODE_VALUES = Arrays.asList(EnumListMode.values())
            .stream()
            .map(EnumListMode::getValue)
            .collect(Collectors.toList()).toArray(new String[0]);
    private static final int TIMED_LIMIT_DEFAULT = 24000 / 20 / 60;
    private static final int TIMED_LIMIT_MIN = 1;
    // CONFIG: GENERAL
    // enables the chill display (no time limit)
    private static boolean chillEnabled;
    // enables the timed display, with streaks and all
    private static boolean timedEnabled;
    // enables the nuked display, that blows up when unfulfilled
    private static boolean nukedEnabled;
    // time limit between each timed requirement, in minutes
    private static int timedLimit;
    // time limit between each nuked requirement, in minutes
    private static int nukedLimit;
    // if nuking causes block destruction
    private static boolean nukingGriefable;
    // list of blocks that will DEFINITELY be added
    private static String[] whitelist;
    // gamelogic for if it'll add blocks beyond the whitelist, and which
    private static EnumListMode addMode;
    // blacklist for the added blocks
    private static String[] blacklist;
    /* END CONFIG FIELDS */
    private static Configuration config;

    public static void preInit() {
        File configFile = new File(Loader.instance().getConfigDir(), Reference.MODID + ".cfg");
        if (config == null)
            config = new Configuration(configFile);

        // fill internal blacklist
//        BlockListHelper.addToInternalBlacklist(new String[]{"minecraft:air"});

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
        Property propTimedLimit = getProperty(categories, CATEGORY_GENERAL, "timedLimit");
        Property propWhitelist = getProperty(categories, CATEGORY_LIST, "whitelist");
        Property propAddMode = getProperty(categories, CATEGORY_LIST, "addMode");
        Property propBlacklist = getProperty(categories, CATEGORY_LIST, "blacklist");

        // config field order, one per category
        for (String catKey : categories.keySet()) {
            ArrayList<String> propNames = categories.get(catKey).stream()
                    .map(Property::getName)
                    .collect(Collectors.toCollection(ArrayList::new));
            config.setCategoryPropertyOrder(catKey, propNames);
        }

        // ---- step 3 - read the config property values into the class's variables (if readFieldsFromConfig) ----------
        /*
           As each value is read from the property, it should be checked to make sure it is valid, in case someone
           has manually edited or corrupted the value.  The get() methods don't check that the value is in range even if
           you have specified a MIN and MAX value of the property
        */

        if (readFieldsFromConfig) {
            timedLimit = propTimedLimit.getInt();
            if (timedLimit < TIMED_LIMIT_MIN) {
                timedLimit = TIMED_LIMIT_DEFAULT;
            }

            whitelist = propWhitelist.getStringList();

            addMode = EnumListMode.ADD_STRICTLY_OBTAINABLE;
            String currentMode = propAddMode.getString();
            for (EnumListMode m : EnumListMode.values()) {
                if (m.getValue().equalsIgnoreCase(currentMode)) {
                    addMode = m;
                }
            }

            blacklist = propBlacklist.getStringList();
            syncFromFields();
        }

        // ---- step 4 - write the class's variables back into the config properties and save to disk -------------------

        //  This is done even for a loadFromFile==true, because some of the properties may have been assigned default
        //    values if the file was empty or corrupt.

        propTimedLimit.set(timedLimit);
        propWhitelist.set(whitelist);
        propAddMode.set(addMode.getValue());
        propBlacklist.set(blacklist);

        if (config.hasChanged()) {
            config.save();
        }
    }

    private static Property getProperty(Map<String, List<Property>> categories, String category, String propertyName) {
        return categories.get(category).stream()
                .filter(property -> property.getName().equalsIgnoreCase(propertyName))
                .findFirst().orElse(null);
    }


    /**
     * Returns an immutable copy of the properties and their categories within the mod
     *
     * @return
     */
    public static ImmutableList<Property> getImmutableCategory(String category) {
        return ImmutableList.copyOf(getCategories().get(category));
    }

    private static Map<String, List<Property>> getCategories() {
        Map<String, List<Property>> categories = new HashMap<String, List<Property>>();

        // CATEGORY: GENERAL
//        boolean HAS_CHILL_DEFAULT = true;
//        Property propHasChill = config.get(CATEGORY_GENERAL, "chillEnabled", HAS_CHILL_DEFAULT)
//                .setLanguageKey("gui.config.chill")
//                .setRequiresMcRestart(true);
        ArrayList<Property> catGeneral = new ArrayList<>();
        categories.put(CATEGORY_GENERAL, catGeneral);

        Property propTimeLimit = config.get(CATEGORY_GENERAL, "timedLimit", TIMED_LIMIT_DEFAULT)
                .setMinValue(TIMED_LIMIT_MIN);
        catGeneral.add(propTimeLimit);

        // CATEGORY: TARGET_LIST
        ArrayList<Property> catList = new ArrayList<>();
        categories.put(CATEGORY_LIST, catList);

        Pattern listPattern = Pattern.compile("(!?[a-zA-Z]\\w*)((:[a-zA-Z]\\w*)(:\\d+)?)?");

        Property propWhitelist = config.get(CATEGORY_LIST, "whitelist", new String[] {})
                .setValidationPattern(listPattern);
        catList.add(propWhitelist);


        Property propAddMode = config.get(CATEGORY_LIST, "addMode", EnumListMode.ADD_STRICTLY_OBTAINABLE.getValue())
                .setValidValues(ADD_MODE_VALUES)
                .setRequiresWorldRestart(true);
        catList.add(propAddMode);

        Property propBlacklist = config.get(CATEGORY_LIST, "blacklist", new String[] {})
                .setValidationPattern(listPattern);
        catList.add(propBlacklist);

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

    /**
     * Saves the config present on the variables in this class
     * Run this method every time you change one of the variables
     */
    private static void syncFromFields() {
        syncConfig(false, false);
    }

    public static ItemStack generateTarget(HashMap<String, Integer> modWeights, List<ItemStack> lastTargets) {
        return BlockListHelper.generateTarget(modWeights, lastTargets);
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

    public static String[] getWhitelist() {
        return whitelist;
    }

    public static void setWhitelist(String[] whitelist) {
        ConfigKismet.whitelist = whitelist;
    }

    public static EnumListMode getAddMode() {
        return addMode;
    }

    public static void setAddMode(EnumListMode addMode) {
        ConfigKismet.addMode = addMode;
    }

    public static String[] getBlacklist() {
        return blacklist;
    }

    public static void setBlacklist(String[] blacklist) {
        ConfigKismet.blacklist = blacklist;
    }

    public enum EnumListMode {
        WHITELIST_ONLY("Whitelist only"),
        ADD_STRICTLY_OBTAINABLE("Add only obtainable"),
        ADD_ALL_POSSIBLE("Add all others");

        private String value;

        EnumListMode(String value) {
            this.value = value;
        }

        public String getValue() {
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
            if (Reference.MODID.equals(event.getModID())) {
                syncFromGUI();
                if (event.getConfigID() != null) {
                    ModLogger.info("Config changed on GUI, category " + event.getConfigID());
                } else {
                    ModLogger.info("Config changed on GUI, no category");
                }
            }

            // no matter the changes, if server-side we need to update the config
            BlockListHelper.generateInternalList();
        }
    }

    private static class ServerToClientConfigSyncEventHandler {
        @SubscribeEvent
        public void onEvent(PlayerEvent.PlayerLoggedInEvent event) {
            Kismet.proxy.sendConfigToClient(event.player);
        }
    }
}