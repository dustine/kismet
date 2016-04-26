package dustine.kismet.world.savedata;

import com.google.common.collect.ImmutableList;
import dustine.kismet.Reference;
import dustine.kismet.target.InformedStack;
import dustine.kismet.target.TargetLibraryBuilder;
import dustine.kismet.util.StackHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class WSDTargetDatabase extends WorldSavedData {
    private static final String NAME = Reference.MOD_ID + "_TargetsData";

    private boolean valid = false;
    private Map<String, InformedStack> stacks = new HashMap<>();

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
        if (nbt.hasKey("valid")) valid = nbt.getBoolean("valid");

        stacks.clear();
        NBTTagList proceduralNbt = nbt.getTagList("stacks", 10);
        for (int i = 0; i < proceduralNbt.tagCount(); i++) {
            NBTTagCompound tagCompound = proceduralNbt.getCompoundTagAt(i);
            final InformedStack wrapper = new InformedStack(tagCompound);
            stacks.put(wrapper.toString(), wrapper);
        }

        TargetLibraryBuilder.recreateLibrary(this.stacks.values());
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
     */
    public void enrichStacks(List<InformedStack> newStacks) {
        newStacks.forEach(wrapper -> {
            String key = StackHelper.toUniqueKey(wrapper);
            if (stacks.containsKey(key)) {
                InformedStack originalStack = stacks.get(key);
                stacks.put(key, originalStack.joinWith(wrapper));
            } else {
                stacks.put(key, wrapper);
            }
        });

        markDirty();
    }
}
