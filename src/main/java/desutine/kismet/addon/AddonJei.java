package desutine.kismet.addon;

import desutine.kismet.common.registry.ModBlocks;
import desutine.kismet.server.StackWrapper;
import desutine.kismet.util.StackHelper;
import mezz.jei.api.*;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IStackHelper;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@JEIPlugin
public class AddonJei implements IModPlugin {
    public static IItemListOverlay itemListOverlay;
    public static IRecipeRegistry recipeRegistry;
    public static IStackHelper stackHelper;

    public static void enrich(List<StackWrapper> stacks) {
        final Set<String> stackKeys = stacks.stream()
                .map(wrapper -> StackHelper.toUniqueKey(wrapper.getStack()))
                .collect(Collectors.toSet());

        final ArrayList<StackWrapper> subtypeStacks = new ArrayList<>();
        // add all subtypes
        for (StackWrapper wrapper : stacks) {
            List<ItemStack> subtypes = stackHelper.getSubtypes(wrapper.getStack());
            // check if the subtype stack is already in the stacks
            // using a set of all unique keys for the items
            for (ItemStack stack : subtypes) {
                String name = StackHelper.toUniqueKey(stack);
                if (!stackKeys.contains(name))
                    subtypeStacks.add(new StackWrapper(stack, false));
            }
        }
        stacks.addAll(subtypeStacks);

        // crafting algorithm
        // algorithm 0: if recipe = can be crafted
        for (StackWrapper wrapper : stacks) {
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
