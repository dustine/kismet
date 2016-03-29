package desutine.kismet.common.config;

import com.ibm.icu.impl.duration.impl.DataRecord;
import desutine.kismet.ModLogger;
import desutine.kismet.common.init.ModItems;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.CheckForNull;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlockListHelper {
    // some items outright crash the game if tried to render so let's remove them!
    private static Set<ItemStack> internalBlacklist = new HashSet<ItemStack>() {};
    private static HashSet<InformedItemStack> completeList;

    public static ItemStack generateTarget(HashMap<String, Integer> modWeights, List<ItemStack> lastTargets) {
        // declared this set outside for performance reasons, less gc required
        final Set<String> configModList = new HashSet<>();
        final Set<ItemStack> configList = new HashSet<>();
        for (String s : ConfigKismet.getList()) {
            if (isMod(s))
                configModList.add(s);
            else
                configList.add(processName(s));
        }

        // filtered iterable
        List<InformedItemStack> filteredCompleteList = completeList.stream().filter(input -> {
            // no nulls pls
            if (input == null) return false;
            if (input.item == null) return false;
            if (input.item.getItem() == null) return false;
            // if strict, it's a no-go
            if (ConfigKismet.isStrict() && !input.isObtainable()) return false;

            // list compare
            String mod = input.getMod();
            switch (ConfigKismet.getListMode()) {
                case "blacklist":
                    return !configModList.contains(mod) && !configList.contains(input.item);
                case "whitelist":
                    return configModList.contains(mod) || configList.contains(input.item);
            }
            return false;
        }).collect(Collectors.toList());

        // get the count of each mod's items, after filtering
        HashMap<String, Integer> listModWeights = getListModWeights(filteredCompleteList);

        // add missing keys to modWeights
        for (String key : listModWeights.keySet()) {
            if (!modWeights.containsKey(key)) {
                modWeights.put(key, 1);
            }
        }

        // do nrItems * weight on the listModWeights
        for (String key : modWeights.keySet()) {
            listModWeights.put(key, listModWeights.get(key) * modWeights.get(key));
        }

        /*
            Pick a random mod, using as a metric w*#items(mod)
            In other words, a given integer, called weight, times the number of valid items given by the mod in question

            Now weight is calculated as such:
            - all mods start as weight 1
            - when a mod isn't chosen as a target, its weight increases by 1
            - if it is chosen, it is reset to 0

            This makes it so that mods are never* chosen twice in a row, and the longer a mod goes without being picked
            the higher its chances are.

            But that's weight alone, what about the item quantity bit? That's to account for how some mods just have
            more stuff than others. If the randomization is done by weight alone, mods with few items will cause
            those items to pop up a lot more frequently than others. This sounds good in first sight, but it would
            lead to possible item repetition, even with the weight counterbalance.
         */
        Random random = new Random();

        // get max weights
        int max = 0;
        for (String key :
                listModWeights.keySet()) {
            max += listModWeights.get(key);
        }

        if(max == 0){
            // no weights! that's bad! but there's a salvo; maybe it just happened that all mods with items were
            // given a weight of 0. so we recalculate lisModWeights, not using w (so basically doing a weightless
            // randomization)
            listModWeights = getListModWeights(filteredCompleteList);
            max = 0;
            for (String key :
                    listModWeights.keySet()) {
                max += listModWeights.get(key);
            }

            if(max == 0){
                // max is still 0, which means we have absolutely no blocks to add as a target. this is horrible!
                // let's _log it and force the target to be some loser unobtainable item
                ModLogger.error("No targets to pick from!");
                // todo That said loser item, using itemKey for now
                return new ItemStack(ModItems.itemKey);
            }
            // if we still have weights, we continue as normal
        }
        // get a random number  on [0, max)
        int r = random.nextInt(max);
        // now iterate over the cumulative sum of the modWeights until you get a sum bigger than r
        int sum = 0;
        String target = null;
        for(String mod: listModWeights.keySet()){
            sum += listModWeights.get(mod);
            if(sum > r){
                target = mod;
                break;
            }
        }
        // target should always be assigned a value, but just in case...
        if(target == null){
            ModLogger.error("No target chosen with these mod weights "+listModWeights);
            return new ItemStack(ModItems.itemKey);
        }
        // a mod has been chosen, now time to get some random block from within!
        //   oh but first we update the mod weights, so that they all increase by 1 EXCEPT the chosen one, that one
        //   reset to zero
        for (String key : modWeights.keySet()) {
            if(key.equalsIgnoreCase(target))
                modWeights.put(key, 0);
            else
                modWeights.put(key, modWeights.get(key)+1);
        }

        // okay, now we can truly finally pick a target
        // filteredCompleteList is already filtered to only have valid items so it's just a process of picking a
        // random index and returning the contents on that index
        final String finalTarget = target;
        List<ItemStack> targetModItems = filteredCompleteList.stream()
                .filter(informedItemStack -> informedItemStack.getMod().equalsIgnoreCase(finalTarget))
                .map(informedItemStack -> informedItemStack.item).collect(Collectors.toList());
        int targetIndex = random.nextInt(targetModItems.size());
        return targetModItems.get(targetIndex);
    }

    private static boolean isMod(String s) {
        return !s.contains(":") || s.trim().endsWith(":");
    }

    private static HashMap<String, Integer> getListModWeights(Iterable<InformedItemStack> filteredCompleteList) {
        HashMap<String, Integer> list = new HashMap<>();
        for (InformedItemStack item : filteredCompleteList) {
            String mod = item.getMod();
            if (!list.containsKey(mod)) {
                list.put(mod, 1);
            } else
                list.put(mod, list.get(mod) + 1);
        }
        return list;
    }

    public static void generateInternalList() {
        completeList = new HashSet<InformedItemStack>();

        // add blocks
        for (ResourceLocation loc :
                GameData.getBlockRegistry().getKeys()) {
            Block block = GameData.getBlockRegistry().getObject(loc);
            ItemStack itemStack = new ItemStack(block);
            if(itemStack.getItem() == null) continue;
            completeList.add(new InformedItemStack(itemStack));
        }
        // add items
        for (ResourceLocation loc :
                GameData.getItemRegistry().getKeys()) {
            Item item = GameData.getItemRegistry().getObject(loc);
            ItemStack itemStack = new ItemStack(item);
            if(itemStack.getItem() == null) continue;
            completeList.add(new InformedItemStack(itemStack));
        }
    }

    public static void addToInternalBlacklist(String[] candidates) {
        for (String s:
             candidates) {
            internalBlacklist.add(processName(s));
        }
    }

    private static ItemStack processName(String s) {
        ItemStack item;
        ResourceLocation loc = new ResourceLocation(s);
        if(GameData.getBlockRegistry().getKeys().contains(loc)){
            // block
            item = new ItemStack(GameData.getBlockRegistry().getObject(loc));
        } else if(GameData.getItemRegistry().getKeys().contains(loc)) {
            // item
            item = new ItemStack(GameData.getItemRegistry().getObject(loc));
        } else {
            ModLogger.warning("Weird location: " + loc);
            return null;
        }

        if(s.contains("@")) {
            int metadata;
            try {
                metadata = Integer.valueOf(s.substring(s.indexOf("@")));
            } catch (NumberFormatException e) {
                ModLogger.warning(String.format("Weird metadata %s in %s", s.substring(s.indexOf("@")), loc));
                return item;
            }
            item.setItemDamage(metadata);
        }
        return item;
    }
}
