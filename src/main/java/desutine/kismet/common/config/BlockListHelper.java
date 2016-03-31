package desutine.kismet.common.config;

import desutine.kismet.ModLogger;
import desutine.kismet.common.init.ModItems;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameData;

import java.util.*;
import java.util.stream.Collectors;

public class BlockListHelper {
    private static HashSet<InformedItemStack> completeList;

    public static ItemStack generateTarget(HashMap<String, Integer> weights, List<ItemStack> lastTargets) {
        // todo only do this (filtering stuff out of the complete list) on config change
        // declared this set outside for performance reasons, less gc required
        final Set<String> configModList = new HashSet<>();
        final Set<ItemStack> configList = new HashSet<>();
        for (String s : ConfigKismet.getList()) {
            if (isMod(s)) {
                configModList.add(s);
            } else {
                configList.add(processName(s));
            }
        }

        // filtered iterable
        List<InformedItemStack> configFilteredItems = completeList.stream().filter(input -> {
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

        // saving the mod weights before we removed items according to previousTargets
//        HashMap<String, Integer> statelessMetrics = getModItemCount(configFilteredItems);
        HashMap<String, Integer> statelessCount = getModItemCount(configFilteredItems);

        // add missing keys to weights
        // using statelessCount because it's the one with less filtering, and hence more possible mod keys
        //  this is important in case of an edge case, seen bellow
        //  also, using weight 1 as this unknown mod could have been added since last time the array was updated
        // sidenote: ty IDEA for the lambdas, omgosh do I love them
        statelessCount.keySet().stream()
                .filter(key -> !weights.containsKey(key))
                .forEach(key -> weights.put(key, 1));

        // remove the previous targets too while you're at it~
        List<InformedItemStack> targets = configFilteredItems.stream()
                .filter(informedItemStack -> !lastTargets.contains(informedItemStack.item))
                .collect(Collectors.toList());

        // get the weight of each mod's items, after filtering
        HashMap<String, Integer> metrics = new HashMap<>();
        // and save a copy of only the item count, just in case
        // todo check if a shallow copy is enough here
        HashMap<String, Integer> count = getModItemCount(targets);

        // do nrItems * weight on any metrics (explained bellow)
        for (String key : weights.keySet()) {
            // first of all, there never should be negative weights, so let's shift them to zero
            if (weights.get(key) < 0)
                weights.put(key, 0);
            if (count.containsKey(key)) {
                metrics.put(key, count.get(key) * weights.get(key));
            } else {
                metrics.put(key, 0);
            }
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
            lead to inevitable item repetition, even with the weight counterbalance.
         */
        Random random = new Random();

        // get max weights
        int max = 0;
        for (String key : metrics.keySet()) {
            max += metrics.get(key);
        }

        if (max <= 0) {
            // no weights! that's bad! means we're infringing on an edge case here: or we ran out of unrepeated items,
            //  or we ran out of unrepeated mods, or we outright ran out of items/mods (too strict config filtering)
            // now, we're assuming here that from a user perspective, a repeated mod is much worse than a repeated
            //  item, so let's repeat the calculations with all weights added one, so no mod is "excluded"

            HashMap<String, Integer> statelessMetrics = new HashMap<>();
            for (String key : weights.keySet()) {
                int modifiedWeight = weights.get(key);
                // why increase positive weights? the issue's on the zero ones
                // contingency: while negative weights should never happen, let's fix them up too (again)
                //  no extra cycles wasted anyway, and if this is properly optimized by the JVM, it takes the same
                //  number of instructions too
                if (modifiedWeight <= 0) modifiedWeight = 1;
                // we change both now because later, if this process failed, the next test(s) would require to increase
                //  statelessMetrics manually anyway
                statelessMetrics.put(key, statelessCount.get(key) * modifiedWeight);
                metrics.put(key, count.get(key) * modifiedWeight);
            }

            max = 0;
            for (String key : metrics.keySet()) {
                max += metrics.get(key);
            }
            if (max <= 0) {
                // max is still 0
                // so we have ruled out outrun mods (weight's fault). now, the blame shifts into the previousTargets
                //  (modCount's fault). thankfully, we saved up the unfiltered list above, didn't we? .u.
                // now, logic would say we removed the oldest targets and moved forward, but there's a problem that
                //  appears from this. the new sequence of targets would closely match the old one, seeing that we
                //  already ruled out mod burnout previously. so best to ignore lastTargets completely, and if that
                //  solves it, nuke it completely and start over. there's a chance some blocks are repeated but hey,
                //  what's the fun of randomness if stuff isn't repeated?
                metrics = statelessMetrics;

                max = 0;
                for (String key : metrics.keySet()) {
                    max += metrics.get(key);
                }

                if (max <= 1) {
                    // ...
                    // oh boy. if we can't discard any of the entry variables, it means the process itself is borked.
                    // log it, return a "poison" item to disable the block and call it a day.
                    // note: the 1 weight is to solve an edge case of an edge case: the resolution of the previous
                    //  contingency includes re-adding the newest previously chosen target, so you don't get the same
                    //  item twice in a row. now, if the sum of all weights, ignore previous targets, is equal to
                    //  one... we're basically ending up with 0 items to choose from again. and that's stupid.
                    ModLogger.error("Edge case contingency on block picking failed, using poison item");
                    // todo: create said poison item
                    // todo: rename the key field to just "key"
                    return new ItemStack(ModItems.itemKey);
                } else {
                    // well this contingency worked, so let's reset the variables to a workable state
                    // so any of the following logic and/or cycles doesn't require any extra logic in it

                    // nuke lastTargets... except for the newest target, to avoid having the same item twice in a row
                    ItemStack previousTarget = lastTargets.get(lastTargets.size() - 1);
                    lastTargets.clear();
                    lastTargets.add(previousTarget);

                    // and to properly remove it, we have to take it out from configFilteredItems
                    // we can use this time to put them into targets too
                    targets = configFilteredItems.stream()
                            .filter(informedItemStack -> informedItemStack.item.equals(previousTarget))
                            .collect(Collectors.toList());

                    // reset weights too, seeing that to get here you needed to already consider it broken
                    //  as extra incentive, it allows for added randomization, seeing that all the old targets will
                    //  be back!
                    weights.keySet().stream()
                            .filter(key -> statelessCount.get(key) > 0)
                            .forEach(key -> weights.put(key, 1));
                    weights.keySet().stream()
                            .filter(key -> statelessCount.get(key) <= 0)
                            .forEach(key -> weights.put(key, 0));

                    // and with all it done with, info log
                    ModLogger.info("Edge case occurred in block choosing, was resolved with discarding previous " +
                            "targets");
                }

            } else {
                // well this contingency worked, so let's reset the variables to a workable state
                // so any of the following logic and/or cycles doesn't require any extra logic in it

                // starting with adding one to each modWeight with any modCount
                weights.keySet().stream()
                        .filter(key -> weights.get(key) <= 0 && count.get(key) > 0)
                        .forEach(key -> weights.put(key, 1));

                // metrics was already altered above so that one's ready to go
                // let's just log it
                ModLogger.info("Edge case occurred in block choosing, was resolved with non-positive weights increase");
            }
        }

        // no edge case so let's move on right along on schedule
        // get a random number  on [0, max)
        int r = random.nextInt(max);
        // now iterate over the cumulative sum of the weights until you get a sum bigger than r
        int sum = 0;
        String targetMod = null;
        for (String mod : metrics.keySet()) {
            sum += metrics.get(mod);
            if(sum > r){
                targetMod = mod;
                break;
            }
        }
        // target should always be assigned a value, but just in case...
        if (targetMod == null) {
            ModLogger.error(String.format("Failed to get a targeted mod, from %s and %s", weights, lastTargets));
            return new ItemStack(ModItems.itemKey);
        }
        // a mod has been chosen, now time to get some random block from within!
        //   oh but first we update the mod weights, so that they all increase by 1 EXCEPT the chosen one, that one
        //   reset to zero
        for (String key : weights.keySet()) {
            if (key.equalsIgnoreCase(targetMod))
                weights.put(key, 0);
            else {
                // only increase by 1 if the mod still has items to offer
                if (count.containsKey(key) && count.get(key) > 0)
                    weights.put(key, weights.get(key) + 1);
            }
        }

        // okay, now we can truly finally pick a target
        // configFilteredItems is already filtered to only have valid items so it's just a process of picking a
        // random index and returning the contents on that index
        final String finalTarget = targetMod;
        List<ItemStack> decapsulatedTargets = targets.stream()
                .filter(informedItemStack -> informedItemStack.getMod().equalsIgnoreCase(finalTarget))
                .map(informedItemStack -> informedItemStack.item).collect(Collectors.toList());
        int targetIndex = random.nextInt(decapsulatedTargets.size());
        ItemStack newTarget = decapsulatedTargets.get(targetIndex);

        // finally, we have a target.
        // but ah, before finishing, we have to add the newly generated target to the lastTarget list!
        lastTargets.add(newTarget);
        return newTarget;
    }

    private static boolean isMod(String s) {
        return !s.contains(":") || s.trim().endsWith(":");
    }

    private static ItemStack processName(String s) {
        ItemStack item;
        ResourceLocation loc = new ResourceLocation(s);
        if (GameData.getBlockRegistry().getKeys().contains(loc)) {
            // block
            item = new ItemStack(GameData.getBlockRegistry().getObject(loc));
        } else if (GameData.getItemRegistry().getKeys().contains(loc)) {
            // item
            item = new ItemStack(GameData.getItemRegistry().getObject(loc));
        } else {
            ModLogger.warning("Weird location: " + loc);
            return null;
        }

        if (s.contains("@")) {
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

    private static HashMap<String, Integer> getModItemCount(Iterable<InformedItemStack> filteredCompleteList) {
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
        completeList = new HashSet<>();

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
}
