package desutine.kismet.common.config;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameData;

import javax.annotation.Nullable;
import java.util.*;

class BlockListHelper {
    // some items outright crash the game if tried to render so let's remove them!
    static Set<ResourceLocation> internalBlacklist = new HashSet<ResourceLocation>() {
    };
    static HashSet<InformedResourceLocation> completeList;

    public static ItemStack generateTarget(HashMap<String, Integer> modWeights, List<ItemStack> lastTargets) {
        // declared this set outside for performance reasons, less gc required
        final Set<String> configModList = new HashSet<>();
        final Set<ResourceLocation> configList = new HashSet<>();
        for (String s : ConfigKismet.getList()) {
            if (isMod(s))
                configModList.add(s);
            else
                configList.add(new ResourceLocation(s));
        }

        // filtered iterable
        Iterable<InformedResourceLocation> filteredCompleteList = Iterables.filter(completeList, new
                Predicate<InformedResourceLocation>() {
                    @Override
                    public boolean apply(@Nullable InformedResourceLocation input) {
                        // no nulls pls
                        if (input == null) return false;
                        // if strict, it's a no-go
                        if (ConfigKismet.isStrict() && !input.isObtainable()) return false;

                        // list compare
                        String mod = input.location.getResourceDomain();
                        switch (ConfigKismet.getListMode()) {
                            case "blacklist":
                                return !configModList.contains(mod) && !configList.contains(input.location);
                            case "whitelist":
                                return configModList.contains(mod) || configList.contains(input.location);
                        }
                        return false;
                    }
                });

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

        return null;
    }

    private static boolean isMod(String s) {
        return !s.contains(":") || s.trim().endsWith(":");
    }

    private static HashMap<String, Integer> getListModWeights(Iterable<InformedResourceLocation> filteredCompleteList) {
        HashMap<String, Integer> list = new HashMap<>();
        for (InformedResourceLocation item : filteredCompleteList) {
            String mod = item.location.getResourceDomain();
            if (!list.containsKey(mod)) {
                list.put(mod, 1);
            } else
                list.put(mod, list.get(mod) + 1);
        }
        return list;
    }

    static void generateInternalList() {
        completeList = new HashSet<InformedResourceLocation>();

        // add items and blocks
        Set<ResourceLocation> allItems = GameData.getBlockRegistry().getKeys();
        allItems.addAll(GameData.getItemRegistry().getKeys());
        for (ResourceLocation loc : allItems) {
            // don't add stuff that is on the internal blacklist
            if (!internalBlacklist.contains(loc))
                completeList.add(new InformedResourceLocation(loc));

            // set craftable flags
            // todo: set the "is craftable?" flag
        }
    }
}
