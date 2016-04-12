package desutine.kismet.server;

import com.google.common.collect.ImmutableList;
import desutine.kismet.Reference;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class WorldSavedDataTargets extends WorldSavedData {
    private static final String NAME = Reference.MOD_ID + "_TargetsData";

    private boolean valid = false;
    private List<StackWrapper> stacks = new ArrayList<>();

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
            final ItemStack stack = ItemStack.loadItemStackFromNBT(tagCompound.getCompoundTag("stack"));
            boolean obtainable = tagCompound.getBoolean("obtainable");
            stacks.add(new StackWrapper(stack, obtainable));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setBoolean("valid", valid);

        NBTTagList stacksNbt = new NBTTagList();
        for (StackWrapper wrapper : stacks) {
            NBTTagCompound tagCompound = new NBTTagCompound();
            tagCompound.setTag("stack", wrapper.getStack().serializeNBT());
            tagCompound.setBoolean("obtainable", wrapper.isObtainable());
            stacksNbt.appendTag(tagCompound);
        }
        nbt.setTag("stacks", stacksNbt);
    }

    public ImmutableList<StackWrapper> getStacks() {
        return ImmutableList.copyOf(stacks);
    }

    public void setStacks(List<StackWrapper> stacks) {
        this.stacks = stacks;
        valid = true;
        markDirty();
    }

    public boolean isValid() {
        return valid;
    }

    public void enrichStacks(List<StackWrapper> stacks) {
        this.stacks.addAll(stacks);
        // useless setStacks to force the valid/mark dirty
        setStacks(this.stacks);
    }
}
