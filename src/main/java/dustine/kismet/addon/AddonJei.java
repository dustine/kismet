package dustine.kismet.addon;

import dustine.kismet.Log;
import dustine.kismet.registry.ModBlocks;
import dustine.kismet.registry.ModItems;
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
        // unfold into the subtypes
        final Map<String, InformedStack> mappedStacks = unfoldSubtypes(stack);
        // add the craftable flag
        setCraftingFlags(mappedStacks.values());

        return new ArrayList<>(mappedStacks.values());
    }

    private static void setCraftingFlags(Collection<InformedStack> stacks) {
        // crafting algorithm
        // if recipe = can be crafted
        for (InformedStack wrapper : stacks) {
            // skip the ones already positive
            if (wrapper.hasOrigin(EnumOrigin.RECIPE)) continue;
            // check the categories where this item appears as an output
            for (IRecipeCategory category : recipeRegistry.getRecipeCategoriesWithOutput(wrapper.getStack())) {
                // and check the nr of recipes within
                final List<Object> recipesWithOutput =
                        recipeRegistry.getRecipesWithOutput(category, wrapper.getStack());
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

        // re-add the original wrapper to the list if it doesn't have the wildcard value
        if (wrapper.getStack().getMetadata() != OreDictionary.WILDCARD_VALUE) {
            subtypeStacks.put(wrapper.toString(), wrapper);
        } else {
            if (!wrapper.getOrigins().isEmpty()) {
                Log.warning("Discarted obtainability data from unfolded wrapper " + wrapper + wrapper.getOrigins());
            }
        }

        // add all subtypes to the mapped list
        for (InformedStack newWrapper : subtypes) {
            String key = newWrapper.toString();
            // check for collisions (excluding the original stack)
            if (subtypeStacks.containsKey(key)) {
                if (!StackHelper.isEquivalent(wrapper, newWrapper)) {
                    Log.warning(String.format("Tried to register subtype twice %s %s",
                            subtypeStacks.get(key), newWrapper));
                }
            } else {
                subtypeStacks.put(key, newWrapper);
            }
        }

        return subtypeStacks;
    }

    public static void setCraftingFlags(InformedStack stack) {
//        if (stack.hasOrigin(EnumOrigin.RECIPE)) return;
        // check the categories where this item appears as an output
        for (IRecipeCategory category : recipeRegistry.getRecipeCategoriesWithOutput(stack.getStack())) {
            // and check the nr of recipes within
            final List<Object> recipesWithOutput =
                    recipeRegistry.getRecipesWithOutput(category, stack.getStack());
            if (recipesWithOutput.size() > 0) {
//                Log.info(category.getUid());
                stack.setOrigins(EnumOrigin.RECIPE, true);
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
        stackHelper = registry.getJeiHelpers().getStackHelper();
    }

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime) {
        itemListOverlay = jeiRuntime.getItemListOverlay();
        recipeRegistry = jeiRuntime.getRecipeRegistry();
    }
}
