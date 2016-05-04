package dustine.kismet.addon;

import dustine.kismet.Log;
import dustine.kismet.registry.ModBlocks;
import dustine.kismet.registry.ModItems;
import dustine.kismet.target.EnumOrigin;
import dustine.kismet.target.Target;
import mezz.jei.api.*;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

@JEIPlugin
public class AddonJei implements IModPlugin {
    public static IRecipeRegistry recipeRegistry;

    public static void setCraftingFlags(Target target) {
        // check the categories where this item appears as an output
        for (IRecipeCategory category : recipeRegistry.getRecipeCategoriesWithOutput(target.getStack())) {
            // and check the nr of recipes within
            final List<Object> recipesWithOutput =
                    recipeRegistry.getRecipesWithOutput(category, target.getStack());
            if (recipesWithOutput.size() > 0) {
//                Log.info(category.getUid());
                target.setOrigins(EnumOrigin.RECIPE, true);
            } else {
                Log.info("why you inconsistent NEI");
            }
        }
    }

    @Override
    public void register(@Nonnull IModRegistry registry) {
        registry.addDescription(new ItemStack(ModBlocks.CHILL_DISPLAY), "jei.description.tile.chillDisplay");
        registry.addDescription(new ItemStack(ModBlocks.TIMED_DISPLAY), "jei.description.tile.timedDisplay");
        registry.addDescription(new ItemStack(ModItems.KEY), "jei.description.item.key");
    }

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime) {
        recipeRegistry = jeiRuntime.getRecipeRegistry();
    }
}
