package com.desutine.kismet;

import com.desutine.kismet.reference.Reference;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static net.minecraftforge.common.config.Configuration.CATEGORY_GENERAL;

/**
 * Mod Configuration
 * Heavily based on TheGreyGhost's MinecraftByExample
 * Source: https://github.com/TheGreyGhost/MinecraftByExample
 */
@SuppressWarnings("WeakerAccess")
public class ModConfig {
    /* START CONFIG FIELDS */

    private static final boolean IS_STRICT_DEFAULT = true;
    private static final String LIST_MODE_DEFAULT = "blacklist";
    private static final String[] LIST_MODE_CHOICES = new String[] {"blacklist", "whitelist"};
    private static final String[] LIST_DEFAULT = new String[] {};
    private static final String CATEGORY_LIST = "list";
    private static boolean hasChill;
    private static boolean hasTimed;
    private static int timeLimit;
    private static boolean isStrict;
    private static String listMode;

    /* END CONFIG FIELDS */
    private static String[] list;
    private static Configuration config;
    public static void preInit() {
        File configFile = new File(Loader.instance().getConfigDir(), Reference.MODID + ".cfg");
        config = new Configuration(configFile);

        syncFromFile();
    }

    /**
     * Loads config from disk, overriding current config
     * Only needed on mod's preInit
     */
    public static void syncFromFile() {
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

    @SuppressWarnings("ConstantConditions")
    private static void syncConfig(boolean loadConfigFromFile, boolean readFieldsFromConfig) {
        // ---- step 1 - load raw values from config file (if loadFromFile true) ---------------------------------------
        if (loadConfigFromFile) {
            config.load();
        }

        // ---- step 2 - define the properties in the config file ------------------------------------------------------
        Pattern listPattern = Pattern.compile("([0-9a-z]+)(:[0-9a-z]+)?");

        boolean HAS_CHILL_DEFAULT = true;
        Property propHasChill = config.get(CATEGORY_GENERAL, "hasChill", HAS_CHILL_DEFAULT, I18n.format("gui.config" +
                ".hasChill" + ".tooltip"))
                .setLanguageKey("gui.config.hasChill")
                .setRequiresMcRestart(true);

        boolean HAS_TIMED_DEFAULT = true;
        Property propHasTimed = config.get(CATEGORY_GENERAL, "hasTimed", HAS_TIMED_DEFAULT, I18n.format
                ("gui.config.hasTimed" + ".tooltip"))
                .setLanguageKey("gui.config.hasTimed")
                .setRequiresMcRestart(true);

        int TIME_LIMIT_DEFAULT = 24000;
        int TIME_LIMIT_MIN = 20;
        int TIME_LIMIT_MAX = Integer.MAX_VALUE;
        Property propTimeLimit = config.get(CATEGORY_GENERAL, "timeLimit", TIME_LIMIT_DEFAULT, I18n
                .format("gui.config.timeLimit" + ".tooltip"))
                .setLanguageKey("gui.config.timeLimit")
                .setMinValue(TIME_LIMIT_MIN)
                .setMaxValue(TIME_LIMIT_MAX);

        Property propIsStrict = config.get(getCategoryList(), "getIsStrict", IS_STRICT_DEFAULT, I18n.format("gui.config" +
                ".list.getIsStrict" + ".tooltip"))
                .setLanguageKey("gui.config.list.getIsStrict");

        Property propListMode = config.get(getCategoryList(), "listMode", LIST_MODE_DEFAULT, I18n.format("gui.config" +
                ".list.listMode" + ".tooltip"))
                .setLanguageKey("gui.config.list.listMode")
                .setValidValues(LIST_MODE_CHOICES);

        Property propList = config.get(getCategoryList(), "list", LIST_DEFAULT, I18n.format("gui.config.list.list" +
                ".tooltip"))
                .setLanguageKey("gui.config.list.list")
                .setValidationPattern(listPattern);

        // config field order, one per category
        List<String> propOrderGeneral = new ArrayList<String>();
        propOrderGeneral.add(propHasChill.getName());
        propOrderGeneral.add(propHasTimed.getName());
        propOrderGeneral.add(propTimeLimit.getName());
        config.setCategoryPropertyOrder(CATEGORY_GENERAL, propOrderGeneral);

        List<String> propOrderList = new ArrayList<String>();
        propOrderList.add(propIsStrict.getName());
        propOrderList.add(propListMode.getName());
        propOrderList.add(propList.getName());
        config.setCategoryPropertyOrder(getCategoryList(), propOrderList);

        // ---- step 3 - read the config property values into the class's variables (if readFieldsFromConfig) ----------

        /*
           As each value is read from the property, it should be checked to make sure it is valid, in case someone
           has manually edited or corrupted the value.  The get() methods don't check that the value is in range even if
           you have specified a MIN and MAX value of the property
        */

        if (readFieldsFromConfig) {
            setHasChill(propHasChill.getBoolean());
            setHasTimed(propHasTimed.getBoolean());
            setIsStrict(propIsStrict.getBoolean());

            setTimeLimit(propTimeLimit.getInt());
            if (getTimeLimit() < TIME_LIMIT_MIN || getTimeLimit() > TIME_LIMIT_MAX) {
                setTimeLimit(TIME_LIMIT_DEFAULT);
            }

            setListMode(propListMode.getString());
            if (!getListMode().equalsIgnoreCase("blacklist") && !getListMode().equalsIgnoreCase("whitelist")) {
                setListMode(LIST_MODE_DEFAULT);
            }

            setList(propList.getStringList());
        }

        // ---- step 4 - write the class's variables back into the config properties and save to disk -------------------

        //  This is done even for a loadFromFile==true, because some of the properties may have been assigned default
        //    values if the file was empty or corrupt.

        propHasChill.set(getHasChill());
        propHasTimed.set(getHasTimed());
        propTimeLimit.set(getTimeLimit());
        propIsStrict.set(getIsStrict());
        propListMode.set(getListMode());
        propList.set(getList());

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static Configuration getConfig() {
        return config;
    }

    public static void clientPreInit() {

        MinecraftForge.EVENT_BUS.register(new ConfigEventHandler());
    }

    /**
     * Save the GUI-stored values, without accessing disk config
     * Not needed to use
     */
    public static void syncFromGUI() {
        syncConfig(false, true);
    }

    /**
     * Saves the config present on the variables in this class
     * Run this method every time you change one of the variables
     */
    public static void syncFromFields() {
        syncConfig(false, false);
    }

    public static boolean getHasChill() {
        return hasChill;
    }

    public static void setHasChill(boolean hasChill) {
        ModConfig.hasChill = hasChill;
        syncFromFields();
    }

    public static boolean getHasTimed() {
        return hasTimed;
    }

    public static void setHasTimed(boolean hasTimed) {
        ModConfig.hasTimed = hasTimed;
        syncFromFields();
    }

    public static int getTimeLimit() {
        return timeLimit;
    }

    public static void setTimeLimit(int timeLimit) {
        ModConfig.timeLimit = timeLimit;
        syncFromFields();
    }

    public static boolean getIsStrict() {
        return isStrict;
    }

    public static void setIsStrict(boolean isStrict) {
        ModConfig.isStrict = isStrict;
        syncFromFields();
    }

    public static String getListMode() {
        return listMode;
    }

    public static void setListMode(String listMode) {
        ModConfig.listMode = listMode;
        syncFromFields();
    }

    public static String[] getList() {
        return list;
    }

    public static void setList(String[] list) {
        ModConfig.list = list;
        syncFromFields();
    }

    public static String getCategoryList() {
        return CATEGORY_LIST;
    }

    private static class ConfigEventHandler {
        /*
         * This class, when instantiated as an object, will listen on the FML
         *  event bus for an OnConfigChangedEvent
         */
        @SubscribeEvent(priority = EventPriority.NORMAL)
        public void onEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (Reference.MODID.equals(event.modID) && !event.isWorldRunning) {
                syncFromGUI();
                if (event.configID != null) {
                    syncFromGUI();
                    ModLogger.info("Config changed on GUI, category " + event.configID);
                } else {
                    ModLogger.info("Config changed on GUI, no category");
                }
            }
        }
    }
}