package desutine.kismet.addon;

import mezz.jei.api.*;

import javax.annotation.Nonnull;

@JEIPlugin
public class AddonJei implements IModPlugin {
    public static IItemListOverlay itemListOverlay;

    @Override
    public void register(@Nonnull IModRegistry registry) {
    }

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime) {
        itemListOverlay = jeiRuntime.getItemListOverlay();
    }
}
