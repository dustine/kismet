package dustine.kismet.addon;

import dustine.kismet.Log;
import dustine.kismet.registry.ModBlocks;
import dustine.kismet.target.EnumOrigin;
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
            if (wrapper.hasOrigin(EnumOrigin.RECIPE)) continue;

            // check the categories where this item appears as an output
            for (IRecipeCategory category : recipeRegistry.getRecipeCategoriesWithOutput(wrapper.getStack())) {
                // and check the nr of recipes within
                final List<Object> recipesWithOutput = recipeRegistry.getRecipesWithOutput(category, wrapper.getStack());
                if (recipesWithOutput.size() > 0) {
                    wrapper.setOrigins(EnumOrigin.RECIPE, true);
                }
            }
        }
    }

    private static Map<String, InformedStack> unfoldSubtypes(InformedStack wrapper) {
        final Map<String, InformedStack> subtypeStacks = new HashMap<>();
        final ItemStack stack = ItemStack.copyItemStack(wrapper.getStack());
        stack.setItemDamage(OreDictionary.WILDCARD_VALUE);

        // unfold the wildcard stack into the subtypes
        List<InformedStack> subtypes = stackHelper.getSubtypes(stack).stream()
                .map(InformedStack::new)
                .collect(Collectors.toList());

        // force the subtypes listing because, y'know, we have a list of subtypes!!
        wrapper.setHasSubtypes(subtypes.size() > 1);
        subtypes.forEach(subtype -> subtype.setHasSubtypes(subtypes.size() > 1));

        if (!wrapper.getHasSubtypes()) {
            final InformedStack unfoldedStack = subtypes.get(0);
            unfoldedStack.setOrigins(wrapper.getOrigins());
            subtypeStacks.put(unfoldedStack.toString(), unfoldedStack);
            return subtypeStacks;
        }

        // readd the original wrapper to the list if it doesn't have the wildcard value
        if (wrapper.getStack().getMetadata() != OreDictionary.WILDCARD_VALUE) {
            subtypeStacks.put(wrapper.toString(), wrapper);
        } else {
            if (!wrapper.getOrigins().isEmpty()) {
                Log.warning("Discarted obtainability data from unfolded wrapper " + wrapper + wrapper.getOrigins());
            }
        }

        // add all subtypes to the mapped list
        for (InformedStack newWrapper : subtypes) {
            // add the obtainability of the original wrapper into wrapper:0 (metadata 0)
//            if (StackHelper.isEquivalent(wrapper, newWrapper)) {
//                newWrapper.setOrigins(new HashSet<>(wrapper.getOrigins()));
//            }

            // check if the subtype stack is already in the stacks
            // using a set of all unique keys for the items
            String key = newWrapper.toString();
            if (subtypeStacks.containsKey(key) && !StackHelper.isEquivalent(wrapper, newWrapper)) {
                // original stacks had this item already, join them
                // this will emit a warning message as it's not supposed to happen but at least nothing is lost
                Log.warning(String.format("Tried to register subtype twice %s %s", subtypeStacks.get(key),
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
