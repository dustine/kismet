package dustine.kismet.client.target;

import dustine.kismet.Log;
import dustine.kismet.target.InformedStack;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClientTargetHelper {
    public static void setCraftingFlags(InformedStack stack) {
        throw new NotImplementedException();
    }

    public static Map<String, InformedStack> unfoldSubtypes() {
        Map<String, InformedStack> stacks = new HashMap<>();
        // get registered items
        for (ResourceLocation loc : Item.REGISTRY.getKeys()) {
            Item item = Item.REGISTRY.getObject(loc);
            ItemStack stack = new ItemStack(item);
            if (stack.getItem() == null) continue;
            // unfold the item into subtypes
            final List<ItemStack> subtypes = getSubtypes(item);
            // turn them into informed stacks with subtype count info
            for (ItemStack s : subtypes) {
                final InformedStack informedStack = new InformedStack(s);
                informedStack.setHasSubtypes(subtypes.size() > 1);
                stacks.put(informedStack.toString(), informedStack);
            }
        }
        return stacks;
    }

    private static List<ItemStack> getSubtypes(@Nonnull final Item item) {
        List<ItemStack> itemStacks = new ArrayList<>();

        for (CreativeTabs itemTab : item.getCreativeTabs()) {
            List<ItemStack> subItems = new ArrayList<>();
            try {
                item.getSubItems(item, itemTab, subItems);
            } catch (RuntimeException | LinkageError e) {
                Log.error("Item.getSubItems(" + item + ")", e);
            }
            itemStacks.addAll(subItems.stream()
                    .filter(subItem -> subItem != null && subItem.getItem() != null)
                    .collect(Collectors.toList())
            );
        }

        return itemStacks;
    }


}
