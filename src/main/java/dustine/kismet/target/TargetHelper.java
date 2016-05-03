package dustine.kismet.target;

import dustine.kismet.util.StackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TargetHelper {
    public static void identifyRecipes(Map<String, InformedStack> stacks) {
        final List<ItemStack> crafts = new ArrayList<>();

        // crafting
        crafts.addAll(CraftingManager.getInstance().getRecipeList().stream()
                .map(IRecipe::getRecipeOutput)
                .collect(Collectors.toList()));
        // furnacing
        crafts.addAll(FurnaceRecipes.instance().getSmeltingList().values());
        // no potions though u_u
        // todo potions are DEFINITELY possible, it's just not obvious at first sight

        crafts.forEach(joinWithStackMap(stacks, EnumOrigin.RECIPE));
    }

    public static Consumer<ItemStack> joinWithStackMap(Map<String, InformedStack> stacks, EnumOrigin type) {
        return stack -> {
            if (stack == null || stack.getItem() == null) return;
            String key = StackHelper.toUniqueKey(stack);

            joinStackWithStackMap(stacks, type, stack, key);
        };
    }

    private static void joinStackWithStackMap(Map<String, InformedStack> stacks, EnumOrigin type, ItemStack stack,
                                              String key) {
        InformedStack wrapper = new InformedStack(stack, type);
        wrapper.setHasSubtypes(true);
        if (stacks.containsKey(key)) {
            stacks.get(key).setOrigins(type, true);
        } else {
            stacks.put(key, wrapper);
        }
    }

    public static Consumer<String> addToStackMap(Map<String, InformedStack> stacks, EnumOrigin type) {
        return key -> {
            ItemStack stack = StackHelper.getItemStack(key);
            if (stack == null || stack.getItem() == null) return;

            joinStackWithStackMap(stacks, type, stack, key);
        };
    }
}
