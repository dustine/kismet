package desutine.kismet.server;

import com.google.common.collect.ImmutableList;
import desutine.kismet.Reference;
import desutine.kismet.target.TargetLibraryFactory;
import desutine.kismet.util.StackHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class WorldSavedDataTargets extends WorldSavedData {
    private static final String NAME = Reference.MOD_ID + "_TargetsData";

    private boolean valid = false;
    private Map<String, InformedStack> stacks = new HashMap<>();

    public WorldSavedDataTargets() {
        super(NAME);
    }

    @SuppressWarnings("unused")
    public WorldSavedDataTargets(String name) {
        super(name);
    }

    public static WorldSavedDataTargets get(World world) {
        MapStorage storage = world.getMapStorage();
        WorldSavedDataTargets instance =
                (WorldSavedDataTargets) storage.loadData(WorldSavedDataTargets.class, NAME);

        if (instance == null) {
            instance = new WorldSavedDataTargets();
            storage.setData(NAME, instance);
        }
        return instance;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("valid")) valid = nbt.getBoolean("valid");

        stacks.clear();
        NBTTagList proceduralNbt = nbt.getTagList("stacks", 10);
        for (int i = 0; i < proceduralNbt.tagCount(); i++) {
            NBTTagCompound tagCompound = proceduralNbt.getCompoundTagAt(i);
            final InformedStack wrapper = new InformedStack(tagCompound);
            stacks.put(wrapper.toString(), wrapper);
        }

        TargetLibraryFactory.recreateLibrary(this.stacks.values());
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setBoolean("valid", valid);

        NBTTagList stacksNbt = new NBTTagList();
        for (InformedStack wrapper : stacks.values()) {
            NBTTagCompound tagCompound = wrapper.serializeNBT();
            if (tagCompound != null) {
                stacksNbt.appendTag(tagCompound);
            }
        }
        nbt.setTag("stacks", stacksNbt);
    }

    public ImmutableList<InformedStack> getStacks() {
        return ImmutableList.copyOf(stacks.values());
    }

    public void setStacks(Map<String, InformedStack> stacks) {
        this.stacks = stacks;
        if (stacks.isEmpty())
            valid = false;
        markDirty();
    }

    @Override
    public void markDirty() {
        super.markDirty();
        valid = true;
    }

    public boolean isValid() {
        return valid;
    }

    /**
     * @param newStacks
     * @return number of skipped (joined with already existing) stacks
     */
    public int enrichStacks(List<InformedStack> newStacks) {
        final int[] skipped = {0};
        newStacks.forEach(wrapper -> {
            String key = StackHelper.toUniqueKey(wrapper);
            if (stacks.containsKey(key)) {
                stacks.get(key).joinWith(wrapper);
                ++skipped[0];
            } else {
                stacks.put(key, wrapper);
            }
        });

        markDirty();
        return skipped[0];
    }
}
