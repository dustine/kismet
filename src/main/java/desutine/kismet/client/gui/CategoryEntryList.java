package desutine.kismet.client.gui;

import desutine.kismet.ModConfig;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.List;

// Config category for general configurations
@SuppressWarnings("WeakerAccess")
public class CategoryEntryList extends GuiConfigEntries.CategoryEntry {
    public CategoryEntryList(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement) {
        super(owningScreen, owningEntryList, configElement);
    }

    @Override
    protected GuiScreen buildChildScreen() {
        Configuration configuration = ModConfig.getConfig();
        ConfigElement category = new ConfigElement(configuration.getCategory(ModConfig.getCategoryList()));
        List<IConfigElement> propertiesOnThisScreen = category.getChildElements();
        // Forge best practices say to put the path to the config file for the category as
        // the title for the category config screen
        String windowTitle = configuration.toString();

        return new GuiConfig(this.owningScreen, propertiesOnThisScreen,
                this.owningScreen.modID,
                ModConfig.getCategoryList(),
                this.configElement.requiresWorldRestart() || this.owningScreen.allRequireWorldRestart,
                this.configElement.requiresMcRestart() || this.owningScreen.allRequireMcRestart,
                windowTitle,
                I18n.format("gui.config.category.list"));
    }
}
