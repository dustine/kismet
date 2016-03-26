package desutine.kismet.client.gui;


import desutine.kismet.ModConfig;
import desutine.kismet.reference.Reference;
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
            super(parentScreen, getConfigElements(), Reference.MODID, false, false, null);

            this.title = ModConfig.getConfig().toString();
            this.titleLine2 = I18n.format("gui.config.category.main");
        }

        private static List<IConfigElement> getConfigElements() {
            // REMINDER Check FMLConfigGuiFactory.class for the extra "bells" you can add to the config
            List<IConfigElement> list = new ArrayList<IConfigElement>();

            Configuration config = ModConfig.getConfig();

            list.addAll(new ConfigElement(config.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements());
            list.add(new DummyConfigElement.DummyCategoryElement(ModConfig.getCategoryList(), "gui.config.category.list", CategoryEntryList.class));

            return list;
        }

        public static class CategoryEntryGeneral extends CategoryEntry {
            public CategoryEntryGeneral(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement) {
                super(owningScreen, owningEntryList, configElement);
            }
        }

    }

}
