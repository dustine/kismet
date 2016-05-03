package dustine.kismet.target;

import dustine.kismet.util.StackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TargetHelper {
    public static void identifyRecipes(Map<String, Target> targetMap) {
        final List<ItemStack> crafts = new ArrayList<>();

        // crafting
        crafts.addAll(CraftingManager.getInstance().getRecipeList().stream()
                .map(IRecipe::getRecipeOutput)
                .collect(Collectors.toList()));
        // furnacing
        crafts.addAll(FurnaceRecipes.instance().getSmeltingList().values());
        // no potions though u_u
        // todo potions are DEFINITELY possible, it's just not obvious at first sight

        crafts.forEach(joinWithTargetMap(targetMap, EnumOrigin.RECIPE));
    }

    public static Consumer<ItemStack> joinWithTargetMap(Map<String, Target> targetMap, EnumOrigin type) {
        return stack -> {
            if (stack == null || stack.getItem() == null) return;
            String key = StackHelper.toUniqueKey(stack);

            joinStackWithTargetMap(targetMap, type, stack, key);
        };
    }

    private static void joinStackWithTargetMap(Map<String, Target> targetMap, EnumOrigin type, ItemStack stack,
                                               String key) {
        Target target = new Target(stack, type);
        target.setHasSubtypes(true);
        if (targetMap.containsKey(key)) {
            targetMap.get(key).setOrigins(type, true);
        } else {
            targetMap.put(key, target);
        }
    }

    public static Consumer<String> addToTargetMap(Map<String, Target> targetMap, EnumOrigin type) {
        return key -> {
            ItemStack stack = StackHelper.getItemStack(key);
            if (stack == null || stack.getItem() == null) return;

            joinStackWithTargetMap(targetMap, type, stack, key);
        };
    }

    public static boolean isEquivalent(Target lhw, Target rhw) {
        return rhw != null && isEquivalent(lhw, rhw.getStack());
    }

    public static boolean isEquivalent(Target lhw, ItemStack rhs) {
        if (lhw == null || rhs == null) return false;
        final ItemStack lhs = lhw.getStack();
        if (lhs == null) return false;
        if (lhs == rhs) return true;
        if (lhs.getItem() != rhs.getItem()) return false;
        // wildcard means metadata doesn't matter (on either side)
        if (lhs.getMetadata() != OreDictionary.WILDCARD_VALUE) {
            if (lhs.getMetadata() != rhs.getMetadata()) {
                return false;
            }
        }

        if (lhw.getHasSubtypes()) {
            // test NBT
            if (lhs.getItem() == null || rhs.getItem() == null) return false;
            String nbtLhs = StackHelper.getNbtKey(lhs);
            String nbtRhs = StackHelper.getNbtKey(rhs);
            return nbtLhs.equals(nbtRhs);
        } else {
            return true;
        }
    }

    public static String toUniqueKey(Target target) {
        if (target == null || !target.hasItem()) return "";
        return StackHelper.toUniqueKey(target.getStack(), target.getHasSubtypes());
    }

    public static Set<String> getMods(Collection<Target> stacks) {
        final Set<String> mods = new HashSet<>();
        for (Target target : stacks) {
            String mod = getMod(target);
            mods.add(mod);
        }
        return mods;
    }

    public static String getMod(Target item) {
        return item.getStack().getItem().getRegistryName().getResourceDomain();
    }
}
