package desutine.kismet.server;

import com.google.common.collect.ImmutableList;
import desutine.kismet.Reference;
import desutine.kismet.util.StackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class TargetsWorldSavedData extends WorldSavedData {
    private static final String NAME = Reference.MOD_ID + "_TargetsData";

    private List<ItemStack> forcedStacks = new ArrayList<>();
    private List<WrapperTarget> stacks = new ArrayList<>();

    public TargetsWorldSavedData() {
        super(NAME);
    }

    public TargetsWorldSavedData(String name) {
        super(name);
    }

    public static TargetsWorldSavedData get(World world) {
        MapStorage storage = world.getMapStorage();
        TargetsWorldSavedData instance = (TargetsWorldSavedData) storage.loadData(TargetsWorldSavedData.class, NAME);

        if (instance == null) {
            instance = new TargetsWorldSavedData();
            storage.setData(NAME, instance);
        }
        return instance;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        forcedStacks.clear();
        NBTTagList forcedNbt = nbt.getTagList("forcedStacks", 10);
        for (int i = 0; i < forcedNbt.tagCount(); i++) {
            NBTTagCompound tagCompound = forcedNbt.getCompoundTagAt(i);
            forcedStacks.add(ItemStack.loadItemStackFromNBT(tagCompound));
        }

        stacks.clear();
        NBTTagList proceduralNbt = nbt.getTagList("stacks", 10);
        for (int i = 0; i < proceduralNbt.tagCount(); i++) {
            NBTTagCompound tagCompound = proceduralNbt.getCompoundTagAt(i);
            final ItemStack stack = ItemStack.loadItemStackFromNBT(tagCompound.getCompoundTag("stack"));
            boolean obtainable = tagCompound.getBoolean("obtainable");
            stacks.add(new WrapperTarget(stack, obtainable));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        NBTTagList forcedStacksNbt = new NBTTagList();
        for (ItemStack stack : forcedStacks) {
            NBTTagCompound tagCompound = new NBTTagCompound();
            stack.writeToNBT(tagCompound);
            forcedStacksNbt.appendTag(tagCompound);
        }
        nbt.setTag("forcedStacks", forcedStacksNbt);

        NBTTagList stacksNbt = new NBTTagList();
        for (WrapperTarget wrapper : stacks) {
            NBTTagCompound tagCompound = new NBTTagCompound();
            tagCompound.setTag("stack", wrapper.stack.serializeNBT());
            tagCompound.setBoolean("obtainable", wrapper.obtainable);
            stacksNbt.appendTag(tagCompound);
        }
        nbt.setTag("stacks", stacksNbt);
    }

    public List<ItemStack> getForcedStacks() {
        // fixme immutability is missing
        return ImmutableList.copyOf(forcedStacks);
    }

    public void setForcedStacks(List<ItemStack> forcedStacks) {
        this.forcedStacks = forcedStacks;
        markDirty();
    }

    public ImmutableList<WrapperTarget> getStacks() {
        return ImmutableList.copyOf(stacks);
    }

    public void setStacks(List<WrapperTarget> stacks) {
        this.stacks = stacks;
        markDirty();
    }

    public final static class WrapperTarget {
        private final ItemStack stack;
        private boolean obtainable;

        public WrapperTarget(ItemStack stack, boolean obtainable) {
            this.stack = stack;
            this.obtainable = obtainable;
        }

        public ItemStack getStack() {
            return stack;
        }

        public boolean isObtainable() {
            return obtainable;
        }

        public void setObtainable(boolean obtainable) {
            this.obtainable = obtainable;
        }

        @Override
        public String toString() {
            return StackHelper.stackToString(stack) + "=" + obtainable;
        }
    }
}
