package dustine.kismet.world.savedata;

import com.google.common.collect.ImmutableMap;
import dustine.kismet.Log;
import dustine.kismet.Reference;
import dustine.kismet.config.ConfigKismet;
import dustine.kismet.target.EnumOrigin;
import dustine.kismet.target.Target;
import dustine.kismet.target.TargetLibrary;
import dustine.kismet.target.TargetPatcher;
import dustine.kismet.util.StackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WSDTargetDatabase extends WorldSavedData {
    private static final String NAME = Reference.Names.TARGET_DATABASE;

    private boolean valid = false;
    private Map<String, Target> database = new HashMap<>();

    public WSDTargetDatabase() {
        super(NAME);
    }

    public WSDTargetDatabase(final String name) {
        super(name);
    }

    public static WSDTargetDatabase get(final World world) {
        final MapStorage storage = world.getMapStorage();
        WSDTargetDatabase instance =
                (WSDTargetDatabase) storage.loadData(WSDTargetDatabase.class, NAME);

        if (instance == null) {
            instance = new WSDTargetDatabase();
            storage.setData(NAME, instance);
        }
        return instance;
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        if (nbt.hasKey("valid")) this.valid = nbt.getBoolean("valid");

        this.database.clear();
        final NBTTagList proceduralNbt = nbt.getTagList("database", 10);
        for (int i = 0; i < proceduralNbt.tagCount(); i++) {
            final NBTTagCompound tagCompound = proceduralNbt.getCompoundTagAt(i);
            final Target target = new Target(tagCompound);
            this.database.put(target.toString(), target);
        }

        TargetLibrary.build(getDatabase());
    }

    @Override
    public void writeToNBT(final NBTTagCompound nbt) {
        nbt.setBoolean("valid", this.valid);

        final NBTTagList stacksNbt = new NBTTagList();
        for (final Target target : this.database.values()) {
            if (target == null) {
                Log.error("Null target in savedata");
                continue;
            }
            final NBTTagCompound tagCompound = target.serializeNBT();
            if (tagCompound != null) {
                stacksNbt.appendTag(tagCompound);
            }
        }
        nbt.setTag("database", stacksNbt);
    }

    /**
     * Returns the target database, where all the possible Targets are present. Includes overrides and forced targets
     * from the config. If you don't want these, use {@code getSavedata()}.
     * <p>
     * <b>Notice</b>: this list, as it has to load data from the config, is generated anew every time the function is
     * called. Cache it if you're using it externally. And if you're worried performance wise, it's not a processing
     * leak for the mod as this is only called when evoked by user command or when the library is refreshed, which
     * seldom happens.
     *
     * @return The target database
     */
    public Map<String, Target> getDatabase() {
        // todo cache the database; just the savedata isn't enough...
        return ImmutableMap.copyOf(addConfigStacks(this.database));
    }

    /**
     * Using targets as a initial blueprint, this function loads all the defined stacks in the configurations, such as
     * the hidden lists and the force-add stacks, and joins them into targets.
     *
     * @param savedata The entry savadata values
     * @return A map from joining targets with the config lists
     */
    private static Map<String, Target> addConfigStacks(final Map<String, Target> savedata) {
        final Map<String, Target> resultMap = new HashMap<>();

        // forced stacks, the ones that are added for sure to the filtered stacks
        addKeyedOriginsToTargetMap(ConfigKismet.getForceAdd(), EnumOrigin.FORCED, resultMap);
        // the overrides, both from file and runtime
        for (final EnumOrigin origin : EnumOrigin.values()) {
            addKeyedOriginsToTargetMap(TargetPatcher.getOverrides(origin), origin, resultMap);
        }

        // add all the targets now to this list
        savedata.values().forEach(target -> {
            final String key = target.toString();
            if (resultMap.containsKey(key)) {
                resultMap.put(key, resultMap.get(key).joinWith(target));
            } else {
                resultMap.put(key, target);
            }
        });
        return resultMap;
    }

    private static void addKeyedOriginsToTargetMap(Set<String> keys, EnumOrigin origin, Map<String, Target> targetMap) {
        for (final String item : keys) {
            if (item.startsWith("!") || isMod(item)) continue;

            final ItemStack stack = StackHelper.getItemStack(item);
            if (stack == null) continue;

            // add the entries as subtype-having wrappers
            final Target target = new Target(stack, origin);
            // force hasSubtypes to true if user specified a metadata value
            if (hasMetadata(item))
                target.setHasSubtypes(true);

            final String key = target.toString();
            if (targetMap.containsKey(key)) {
                targetMap.put(key, targetMap.get(key).joinWith(target));
            } else
                targetMap.put(key, target);
        }
    }

    private static boolean hasMetadata(final String entry) {
        final String[] split = entry.split(":");
        if (split.length < 3) return false;
        final Integer meta = StackHelper.tryParse(split[2]);
        return meta != null;
    }

    private static boolean isMod(final String s) {
        return !s.contains(":");
    }

    public Map<String, Target> getSavedata() {
        return ImmutableMap.copyOf(this.database);
    }

    public boolean isValid() {
        return this.valid;
    }

    /**
     * Joins the list of Targets into the current database (adds them if new, does joinWith if not)
     *
     * @param targets The targets to join with
     */
    public void joinDatabaseWith(final Collection<Target> targets) {
        targets.forEach(target -> {
            final String key = target.toString();
            if (this.database.containsKey(key)) {
                final Target originalTarget = this.database.get(key);
                this.database.put(key, originalTarget.joinWith(target));
            } else {
                this.database.put(key, target);
            }
        });

        this.valid = !this.database.isEmpty();
        markDirty();
    }

    /**
     * Clears the database from its entries.
     */
    public void clearDatabase() {
        this.database = new HashMap<>();
        this.valid = false;
        markDirty();
    }
}
