package dustine.kismet.addon;

import dustine.kismet.ModLogger;
import dustine.kismet.registry.ModBlocks;
import dustine.kismet.target.InformedStack;
import dustine.kismet.util.StackHelper;
import mezz.jei.api.*;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

@JEIPlugin
public class AddonJei implements IModPlugin {
    public static IItemListOverlay itemListOverlay;
    public static IRecipeRegistry recipeRegistry;
    public static IStackHelper stackHelper;

    public static List<InformedStack> enrich(InformedStack stack) {
        // fix hasSubtypes
        stack.refreshHasSubtypes();
        final Map<String, InformedStack> mappedStacks = unfoldSubtypes(stack);
        // remove wildcard stacks, if any remain somehow
        mappedStacks.values().removeIf(wrapper -> wrapper.getStack().getMetadata() == OreDictionary.WILDCARD_VALUE);
        // add the craftable flag
        setCraftableFlag(mappedStacks.values());

        return new ArrayList<>(mappedStacks.values());
    }

    private static void setCraftableFlag(Collection<InformedStack> stacks) {
        // crafting algorithm
        // if recipe = can be crafted
        for (InformedStack wrapper : stacks) {
            // skip the ones already positive
            if (wrapper.isObtainable(InformedStack.ObtainableTypes.Craftable)) continue;

            // check the categories where this item appears as an output
            for (IRecipeCategory category : recipeRegistry.getRecipeCategoriesWithOutput(wrapper.getStack())) {
                if (wrapper.isObtainable(InformedStack.ObtainableTypes.Craftable)) break;
                // and check the nr of recipes within
                final List<Object> recipesWithOutput = recipeRegistry.getRecipesWithOutput(category, wrapper.getStack());
                if (recipesWithOutput.size() > 0) {
                    wrapper.setObtainable(InformedStack.ObtainableTypes.Craftable, true);
                }
            }
        }
    }

    private static Map<String, InformedStack> unfoldSubtypes(InformedStack wrapper) {
        final Map<String, InformedStack> subtypeStacks = new HashMap<>();
        final ItemStack stack = wrapper.getStack();

        // skip if we don't have a wildcard stack
        if (wrapper.getStack().getMetadata() != OreDictionary.WILDCARD_VALUE) {
            assert stackHelper.getSubtypes(stack).size() < 2;
            subtypeStacks.put(wrapper.toString(), wrapper);
            return subtypeStacks;
        }

        // unfold the wildcard stack into the subtypes
        List<InformedStack> subtypes = stackHelper.getSubtypes(stack).stream()
                .map(InformedStack::new)
                .collect(Collectors.toList());

        // check for subtypes. because, y'know, we have a list of subtypes
        if (subtypes.size() < 2) {
            subtypes.forEach(InformedStack::refreshHasSubtypes);
        } else {
            subtypes.forEach(subtype -> subtype.setHasSubtypes(true));
        }

        // add all subtypes to the mapped list
        for (InformedStack newWrapper : subtypes) {
            // add the obtainability of the original wrapper into wrapper:0 (metadata 0)
            if (StackHelper.isEquivalent(wrapper, newWrapper)) {
                newWrapper.setObtainable(wrapper.getObtainable());
            }

            // check if the subtype stack is already in the stacks
            // using a set of all unique keys for the items
            String key = newWrapper.toString();
            if (subtypeStacks.containsKey(key)) {
                // original stacks had this item already, join them
                // this will emit a warning message as it's not supposed to happen but at least nothing is lost
                ModLogger.warning(String.format("Tried to register subtype twice %s %s", subtypeStacks.get(key),
                        newWrapper));
            }
            subtypeStacks.put(key, newWrapper);
        }

        return subtypeStacks;
    }

    @Override
    public void register(@Nonnull IModRegistry registry) {
        registry.addDescription(new ItemStack(ModBlocks.CHILL_DISPLAY), "jei.description.block.display");
        stackHelper = registry.getJeiHelpers().getStackHelper();
    }

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime) {
        itemListOverlay = jeiRuntime.getItemListOverlay();
        recipeRegistry = jeiRuntime.getRecipeRegistry();
    }
}
