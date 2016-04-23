package desutine.kismet.client.gui;


import com.google.common.collect.ImmutableList;
import desutine.kismet.ConfigKismet;
import desutine.kismet.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.DummyConfigElement.DummyCategoryElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiConfigEntries.CategoryEntry;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @SuppressWarnings("WeakerAccess")
    public static class ModConfigGui extends GuiConfig {

        public ModConfigGui(GuiScreen parentScreen) {
            super(parentScreen, getConfigElements(Configuration.CATEGORY_GENERAL), Reference.MOD_ID, false, false, null);

            this.title = ConfigKismet.getConfig().toString();
            this.titleLine2 = I18n.format("gui.config.category.main");
        }

        private static List<IConfigElement> getConfigElements(String category) {
            // REMINDER Check FMLConfigGuiFactory.class for the extra "bells" you can add to the config
            List<IConfigElement> list = new ArrayList<>();

            final ImmutableList<Property> catGeneral = ConfigKismet.getImmutableCategory(category);
            list.addAll(catGeneral.stream().map(ConfigElement::new).collect(Collectors.toList()));

            if (category.equalsIgnoreCase(Configuration.CATEGORY_GENERAL)) {
                // sub-categories
                list.add(new DummyCategoryElement(
                        ConfigKismet.CATEGORY_TARGETS, "gui.config.category." + ConfigKismet.CATEGORY_TARGETS,
                        CategoryEntryTargets.class));
            }

            return list;
        }

//        public static class CategoryEntryGeneral extends CategoryEntry {
//            public CategoryEntryGeneral(GuiConfig owningScreen, GuiConfigEntries owningEntryList,
//                                        IConfigElement configElement) {
//                super(owningScreen, owningEntryList, configElement);
//            }
//        }

        // Config category for category:targets configurations
        @SuppressWarnings("WeakerAccess")
        public static class CategoryEntryTargets extends CategoryEntry {
            public CategoryEntryTargets(GuiConfig owningScreen, GuiConfigEntries owningEntryList,
                                        IConfigElement configElement) {
                super(owningScreen, owningEntryList, configElement);
            }

            @Override
            protected GuiScreen buildChildScreen() {
                // Forge best practices say to put the path to the config file for the category as the title for the
                // category config screen
                Configuration configuration = ConfigKismet.getConfig();
                String windowTitle = configuration.toString();

                return new GuiConfig(this.owningScreen, getConfigElements(ConfigKismet.CATEGORY_TARGETS),
                        this.owningScreen.modID,
                        ConfigKismet.CATEGORY_TARGETS,
                        this.configElement.requiresWorldRestart() || this.owningScreen.allRequireWorldRestart,
                        this.configElement.requiresMcRestart() || this.owningScreen.allRequireMcRestart,
                        windowTitle,
                        I18n.format("gui.config.category." + ConfigKismet.CATEGORY_TARGETS));
            }
        }
    }

}
