package com.desutine.kismet.client.gui;


import com.desutine.kismet.ModConfig;
import com.desutine.kismet.reference.I18nTags;
import com.desutine.kismet.reference.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.IModGuiFactory;

import net.minecraftforge.fml.client.config.*;
import net.minecraftforge.fml.client.config.GuiConfigEntries.CategoryEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class ModGuiFactory implements IModGuiFactory {

    @Override
    public void initialize(Minecraft minecraftInstance) {

    }

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return ModConfigGui.class;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
        return null;
    }

    public static class ModConfigGui extends GuiConfig {

        public ModConfigGui(GuiScreen parentScreen) {
            super(parentScreen, getConfigElements(), Reference.MODID, false, false, I18n.format(I18nTags
                    .CONFIG_GUI_TITLE));
        }

        private static List<IConfigElement> getConfigElements() {
            // TODO Check FMLConfigGuiFactory.class for the extra "bells" you can add to the config
            List<IConfigElement> list = new ArrayList<IConfigElement>();
//            list.add(new DummyConfigElement.DummyCategoryElement("mainConfig", I18nTags.CONFIG_CATEGORY_MAIN,
//                    CategoryEntryGeneral.class));

            list.add(new DummyConfigElement("hasChill", true, ConfigGuiType.BOOLEAN, "gui.config.hasChill")
                    .setRequiresMcRestart(true));
            list.add(new DummyConfigElement("hasTimed", true, ConfigGuiType.BOOLEAN, "gui.config.hasTimed")
                    .setRequiresMcRestart(true));
            list.add(new DummyConfigElement("timeLimit", 24000, ConfigGuiType.INTEGER, "gui.config.timeLimit"));

            List<IConfigElement> listList = new ArrayList<IConfigElement>();
            Pattern listPattern = Pattern.compile("([0-9a-z]+):?([0-9a-z]*)?");
            listList.add(new DummyConfigElement("isStrict", true, ConfigGuiType.BOOLEAN, "gui.config.list.isStrict"));
            listList.add(new DummyConfigElement("listMode", "blacklist", ConfigGuiType.STRING, "gui.config.list" +
                    ".listMode", new String[] {"blacklist", "whitelist"}));
            listList.add(new DummyConfigElement.DummyListElement("list", new String[] {}, ConfigGuiType.STRING, "gui" +
                    ".config.list.list", listPattern));
            list.add(new DummyConfigElement.DummyCategoryElement(ModConfig.CATEGORY_LIST_NAME, "gui.config" +
                    ".category.list", listList, CategoryEntryGeneral.class));

            return list;
        }

        // Config category for general configurations
        public static class CategoryEntryGeneral extends CategoryEntry {
            public CategoryEntryGeneral(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement) {
                super(owningScreen, owningEntryList, configElement);
            }

            @Override
            protected GuiScreen buildChildScreen() {
                Configuration configuration = ModConfig.getConfig();
                ConfigElement cat_general = new ConfigElement(configuration.getCategory(ModConfig.CATEGORY_LIST_NAME));
                List<IConfigElement> propertiesOnThisScreen = cat_general.getChildElements();
                // Forge best practices say to put the path to the config file for the category as
                // the title for the category config screen
                String windowTitle = configuration.toString();

                return new GuiConfig(this.owningScreen, propertiesOnThisScreen,
                        this.owningScreen.modID,
                        ModConfig.CATEGORY_LIST_NAME,
                        this.configElement.requiresWorldRestart() || this.owningScreen.allRequireWorldRestart,
                        this.configElement.requiresMcRestart() || this.owningScreen.allRequireMcRestart,
                        windowTitle);
            }
        }
    }

}
