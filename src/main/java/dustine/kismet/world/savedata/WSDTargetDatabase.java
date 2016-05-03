package dustine.kismet.world.savedata;

import com.google.common.collect.ImmutableList;
import dustine.kismet.Log;
import dustine.kismet.Reference;
import dustine.kismet.target.Target;
import dustine.kismet.target.TargetHelper;
import dustine.kismet.target.TargetLibraryBuilder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class WSDTargetDatabase extends WorldSavedData {
    private static final String NAME = Reference.Names.TARGET_DATABASE;

    private boolean valid = false;
    private Map<String, Target> database = new HashMap<>();

    public WSDTargetDatabase() {
        super(NAME);
    }

    @SuppressWarnings("unused")
    public WSDTargetDatabase(String name) {
        super(name);
    }

    public static WSDTargetDatabase get(World world) {
        MapStorage storage = world.getMapStorage();
        WSDTargetDatabase instance =
                (WSDTargetDatabase) storage.loadData(WSDTargetDatabase.class, NAME);

        if (instance == null) {
            instance = new WSDTargetDatabase();
            storage.setData(NAME, instance);
        }
        return instance;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("valid")) this.valid = nbt.getBoolean("valid");

        this.database.clear();
        NBTTagList proceduralNbt = nbt.getTagList("database", 10);
        for (int i = 0; i < proceduralNbt.tagCount(); i++) {
            NBTTagCompound tagCompound = proceduralNbt.getCompoundTagAt(i);
            final Target target = new Target(tagCompound);
            this.database.put(target.toString(), target);
        }

        TargetLibraryBuilder.build(this.database.values());
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setBoolean("valid", this.valid);

        NBTTagList stacksNbt = new NBTTagList();
        for (Target target : this.database.values()) {
            if (target == null) {
                Log.error("Null target in savedata");
                continue;
            }
            NBTTagCompound tagCompound = target.serializeNBT();
            if (tagCompound != null) {
                stacksNbt.appendTag(tagCompound);
            }
        }
        nbt.setTag("database", stacksNbt);
    }

    public ImmutableList<Target> getDatabase() {
        return ImmutableList.copyOf(this.database.values());
    }

    public void setDatabase(Map<String, Target> database) {
        this.database = database;
        if (database.isEmpty())
            this.valid = false;
        markDirty();
    }

    @Override
    public void markDirty() {
        super.markDirty();
        this.valid = true;
    }

    public boolean isValid() {
        return this.valid;
    }

    /**
     * @param newStacks
     */
    public void enrichStacks(Collection<Target> newStacks) {
        newStacks.forEach(target -> {
            String key = TargetHelper.toUniqueKey(target);
            if (this.database.containsKey(key)) {
                Target originalTarget = this.database.get(key);
                this.database.put(key, originalTarget.joinWith(target));
            } else {
                this.database.put(key, target);
            }
        });

        markDirty();
    }
}
