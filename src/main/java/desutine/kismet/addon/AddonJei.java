package desutine.kismet.addon;

import desutine.kismet.ModLogger;
import desutine.kismet.common.registry.ModBlocks;
import desutine.kismet.server.StackWrapper;
import mezz.jei.api.*;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IStackHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEmptyDrops;
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

    public static List<StackWrapper> enrich(List<StackWrapper> stacks) {
        final Map<String, StackWrapper> mappedStacks = new HashMap<>();
        stacks.forEach(wrapper -> mappedStacks.put(wrapper.toString(), wrapper));

        unfoldSubtypes(mappedStacks);

        // remove wildcard stacks
        mappedStacks.values().removeIf(wrapper -> wrapper.getStack().getMetadata() == OreDictionary.WILDCARD_VALUE);

        setCraftableFlag(mappedStacks.values());
        addSilkTouchable(mappedStacks);

        return new ArrayList<>(mappedStacks.values());
    }

    private static void addSilkTouchable(Map<String, StackWrapper> mappedStacks) {
        Set<ItemStack> toAdd = new HashSet<>();

        for (StackWrapper wrapper : mappedStacks.values()) {

            final Block block = Block.getBlockFromItem(wrapper.getStack().getItem());

            // add block as itself if it can be silk-harvestable ...?
            if (block != null && !(block instanceof BlockEmptyDrops)) {
                // if the block is breakable and can be silk touched
//                block.getBlockState().getValidStates().stream()
//                        .filter(state -> block.getBlockHardness(state, null, null) > 0 &&
//                                block.canSilkHarvest(null, null, state, null))
//                        .forEach(state -> {
//                            new BlockSilkDropGetter(block).get
//                            block.createStackedBlock(state);
//                            wrapper.setObtainable(true);
//                        });
            }
        }
    }

    private static void setCraftableFlag(Collection<StackWrapper> stacks) {
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

    private static void unfoldSubtypes(Map<String, StackWrapper> stacks) {
        final Map<String, StackWrapper> subtypeStacks = new HashMap<>();
        // add all subtypes
        for (StackWrapper wrapper : stacks.values()) {
            List<StackWrapper> subtypes = stackHelper.getSubtypes(wrapper.getStack()).stream()
                    .map(StackWrapper::new)
                    .collect(Collectors.toList());
            // check if the subtype stack is already in the stacks
            // using a set of all unique keys for the items
            for (StackWrapper subtype : subtypes) {
                String key = subtype.toString();
                if (stacks.containsKey(key)) {
                    // original stacks had this item already, join them
                    stacks.get(key).joinWith(subtype);
                } else {
                    if (subtypeStacks.containsKey(key)) {
                        // it's already on the added subtypes... somehow
                        ModLogger.warning("Subtype was added more than once: " + key);
                    } else {
                        subtypeStacks.put(key, subtype);
                    }
                }
            }
        }

        stacks.putAll(subtypeStacks);
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
