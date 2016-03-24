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

/**
 * Mod Configuration
 * Heavily based on TheGreyGhost's MinecraftByExample
 * Source: https://github.com/TheGreyGhost/MinecraftByExample
 */
public class ModConfig {
    public static final String CATEGORY_LIST_NAME = "category_list";
    public static final String LIST_MODE_DEFAULT = "blacklist";
    public static final String[] LIST_MODE_CHOICES = new String[] {"blacklist", "whitelist"};
    public static final boolean IS_STRICT_DEFAULT = true;
    public static final String[] LIST_DEFAULT = new String[] {};
    // config fields go here
    public static boolean hasChill;
    // end config fields
    public static boolean hasTimed;
    public static int timeLimit;
    public static boolean isStrict;
    public static String listMode;
    public static String[] list;
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

    private static void syncConfig(boolean loadConfigFromFile, boolean readFieldsFromConfig) {
        // ---- step 1 - load raw values from config file (if loadFromFile true) -------------------

		/*Check if this config object is the main config file or a child config
         *For simple config setups, this only matters if you enable global config
		 *	for your config object by using config.enableGlobalConfiguration(),
		 *	this will cause your config file to be 'global.cfg' in the default config directory
		 *  and use it to read/write your config options
		 */
        if (loadConfigFromFile) {
            config.load();
        }

		/* Using language keys are a good idea if you are using a config GUI
         * This allows you to provide "pretty" names for the config properties
		 * 	in a .lang file as well as allow others to provide other localizations
		 *  for your mod
		 * The language key is also used to get the tooltip for your property,
		 * 	the language key for each properties tooltip is langKey + ".tooltip"
		 *  If no tooltip lang key is specified in a .lang file, it will default to
		 *  the property's comment field
		 * prop.setRequiresWorldRestart(true); and prop.setRequiresMcRestart(true);
		 *  can be used to tell Forge if that specific property requires a world
		 *  or complete Minecraft restart, respectively
		 *  Note: if a property requires a world restart it cannot be edited in the
		 *   in-world mod settings (which hasn't been implemented yet by Forge), only
		 *   when a world isn't loaded
		 *   -See the function definitions for more info
		 */


        // ---- step 2 - define the properties in the config file -------------------

        // The following code is used to define the properties in the config file-
        //   their name, type, default / min / max values, a comment.  These affect what is displayed on the GUI.
        // If the file already exists, the property values will already have been read from the file, otherwise they
        //  will be assigned the default value.

        Property propIsStrict = config.get(CATEGORY_LIST_NAME, "isStrict", IS_STRICT_DEFAULT, I18n.format("gui.config.list.isStrict.tooltip"));
        propIsStrict.setLanguageKey("gui.config.list.isStrict");

        // list mode: sets if the item list is either in blacklist or whitelist
        Property propListMode = config.get(CATEGORY_LIST_NAME, "listMode", LIST_MODE_DEFAULT, I18n.format("gui.config.list.listMode.tooltip"));
        propListMode.setLanguageKey("gui.config.list.listMode");

        // the actual item list
        Property propList = config.get(CATEGORY_LIST_NAME, "list", LIST_DEFAULT, I18n.format("gui.config.list.list" +
                ".tooltip"));
        propList.setLanguageKey("gui.config.list.list");

        // config field order, one per category
        List<String> propOrderGeneral = new ArrayList<String>();
        propOrderGeneral.add(propIsStrict.getName());
        propOrderGeneral.add(propListMode.getName());
        propOrderGeneral.add(propList.getName());
        config.setCategoryPropertyOrder(CATEGORY_LIST_NAME, propOrderGeneral);

        // ---- step 3 - read the config property values into the class's variables (if readFieldsFromConfig) -------------------

        // As each value is read from the property, it should be checked to make sure it is valid, in case someone
        //   has manually edited or corrupted the value.  The get() methods don't check that the value is in range even
        //   if you have specified a MIN and MAX value of the property

        if (readFieldsFromConfig) {
            //If getInt cannot get an integer value from the config file value of myInteger (e.g. corrupted file)
            // it will set it to the default value passed to the function

            isStrict = propIsStrict.getBoolean();

            String listModeCandidate = propListMode.getString();
            if (listModeCandidate.equalsIgnoreCase("blacklist") || listModeCandidate.equalsIgnoreCase("whitelist")) {
                listMode = listModeCandidate;
            }

            list = propList.getStringList();
        }

        // ---- step 4 - write the class's variables back into the config properties and save to disk -------------------

        //  This is done even for a loadFromFile==true, because some of the properties may have been assigned default
        //    values if the file was empty or corrupt.

        propIsStrict.set(isStrict);
        propListMode.set(listMode);
        propList.set(list);

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