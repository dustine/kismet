package desutine.kismet.addon;

import desutine.kismet.common.registry.ModBlocks;
import desutine.kismet.server.TargetsWorldSavedData.WrapperTarget;
import mezz.jei.api.*;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IStackHelper;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JEIPlugin
public class AddonJei implements IModPlugin {
    public static IItemListOverlay itemListOverlay;
    public static IRecipeRegistry recipeRegistry;
    public static IStackHelper stackHelper;

    public static void enrich(List<WrapperTarget> stacks) {
        final ArrayList<WrapperTarget> tempWrappers = new ArrayList<>();
        // add all subtypes
        for (WrapperTarget wrapper : stacks) {
            List<ItemStack> subtypes = stackHelper.getSubtypes(wrapper.getStack());
            if (subtypes.size() <= 1) continue;
            // drop the first one as it already exists on the other
            subtypes = subtypes.subList(1, subtypes.size() - 1);
            tempWrappers.addAll(subtypes.stream()
                    .map(stack -> new WrapperTarget(stack, false))
                    .collect(Collectors.toList()));
        }
        stacks.addAll(tempWrappers);

//        // world-gen
//        ClientHelperTarget.identifyWorldGen(oldItems, items);

        // crafting algorithm
        // algorithm 0: if recipe = can be crafted
        for (WrapperTarget wrapper : stacks) {
            // skip the ones already positive
            if (wrapper.isObtainable()) continue;

            // check the categories where this item appears as an output
            for (IRecipeCategory category : recipeRegistry.getRecipeCategoriesWithOutput(wrapper.getStack())) {
                // and check the nr of recipes within
                final List<Object> recipesWithOutput = recipeRegistry.getRecipesWithOutput(category, wrapper.getStack());
                if (recipesWithOutput.size() > 0) {
                    wrapper.setObtainable(true);
                }
            }
        }
    }

    @Override
    public void register(@Nonnull IModRegistry registry) {
        registry.addDescription(new ItemStack(ModBlocks.DISPLAY), "jei.description.block.display");
        stackHelper = registry.getJeiHelpers().getStackHelper();
    }

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime) {
        itemListOverlay = jeiRuntime.getItemListOverlay();
        recipeRegistry = jeiRuntime.getRecipeRegistry();
    }
}
