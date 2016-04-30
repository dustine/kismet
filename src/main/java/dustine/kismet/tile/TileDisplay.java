package dustine.kismet.tile;

import dustine.kismet.ConfigKismet;
import dustine.kismet.Kismet;
import dustine.kismet.Log;
import dustine.kismet.block.BlockDisplay;
import dustine.kismet.block.BlockTimedDisplay;
import dustine.kismet.target.InformedStack;
import dustine.kismet.target.TargetGenerationResult;
import dustine.kismet.target.TargetLibrary;
import dustine.kismet.util.StackHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class TileDisplay extends TileEntity implements ITickable, ICapabilityProvider {
    @CapabilityInject(IItemHandler.class)
    private static Capability<IItemHandler> ITEM_HANDLER_CAPABILITY = null;
    private final TextFormatting[] colors = new TextFormatting[] {
            TextFormatting.WHITE,
            TextFormatting.GREEN,
            TextFormatting.BLUE,
            TextFormatting.DARK_PURPLE,
            TextFormatting.GOLD
    };
    private IItemHandler targetSlot = null;
    private int skipped;
    private int score;
    private int highScore;
    private long deadline;
    private InformedStack target;
    private List<InformedStack> history;
    private HashMap<String, Integer> weights;
    private boolean stateChanged;
    private long newTargetTimeout;

    public TileDisplay() {
        super();
        if (ITEM_HANDLER_CAPABILITY != null)
            this.targetSlot = new TileDisplaySlotHandler(this);
        this.weights = new HashMap<>();
        this.history = new ArrayList<>();
    }

    @CapabilityInject(IItemHandler.class)
    private static void capRegistered(Capability<IItemHandler> cap) {
//        Log.info(cap);
    }

    public String getStylizedDeadline(boolean color) {
        // format the time remaining as hh:mm:ss
        // less error-prone way to get the seconds already rounded up
        final long l = getDeadline() - this.worldObj.getTotalWorldTime();
        long remainingTime = l / 20 + (l % 20 == 0 ? 0 : 1);

        // yellow -> red -> bold red
        String styleCode;
        if (color && remainingTime <= 15 * 60) {
            if (remainingTime > 5 * 60) {
                styleCode = TextFormatting.YELLOW.toString();
            } else {
                styleCode = TextFormatting.RED.toString();
            }
        } else styleCode = "";

        String remainingTimeString = DurationFormatUtils.formatDurationHMS(remainingTime * 1000);
        remainingTimeString = remainingTimeString.substring(0, remainingTimeString.indexOf("."));

        String resetStyleCode = TextFormatting.RESET.toString();
        return styleCode + remainingTimeString + resetStyleCode;
    }

    private long getDeadline() {
        return this.deadline;
    }

    private void setDeadline(long deadline) {
        this.deadline = deadline;
        // no sync required as this action is done on both sides at once
    }

    public String getStylizedScore() {
        int deliminator = getScore() / 10;
        String styleCode;
        if (deliminator >= this.colors.length) {
            // use the last colour
            styleCode = this.colors[this.colors.length - 1].toString();
        } else {
            styleCode = this.colors[deliminator].toString();
        }

        String resetStyleCode = TextFormatting.RESET.toString();
        return styleCode + this.score + resetStyleCode;
    }

    public int getScore() {
        return this.score;
    }

    public void setScore(int score) {
        this.score = score;
        // update the high score too
        setHighScore(Math.max(getHighScore(), getScore()));
    }

    @Override
    public void update() {
        // isDirty is set to true whenever the internal state of the tile is changed
        // name coming from the parent's method markDirty()
        boolean isDirty = false;
        if (getBlockType() instanceof BlockTimedDisplay) {
            isDirty = checkForDeadline();
        }
        isDirty |= checkForNullTarget();

        if (isDirty) {
            markDirty();
        }

        if (this.stateChanged) {
            this.stateChanged = false;
            final IBlockState oldState = this.worldObj.getBlockState(this.pos);
            final IBlockState actualState = oldState.getBlock().getActualState(oldState, this.worldObj, this.pos);
            final Chunk chunk = this.worldObj.getChunkFromBlockCoords(this.pos);
            this.worldObj.markAndNotifyBlock(this.pos, chunk, oldState, actualState, 3);
        }
    }

    private boolean checkForNullTarget() {
        return (getTarget() == null || !getTarget().hasItem()) && getNewTarget();
    }

    private boolean checkForDeadline() {
        if (getDeadline() < this.worldObj.getTotalWorldTime()) {
            resetDeadline();

            if (!isFulfilled()) {
                // reset the variables and save the high score
                this.history.clear();
                setScore(0);
            }
            setFulfilled(false);

            return getNewTarget();
        }
        return false;
    }

    private void resetDeadline() {
        setDeadline(this.worldObj.getTotalWorldTime() + ConfigKismet.getTimedLimit() * 20 * 60);
    }

    public boolean rollForKey() {
        final Random random = Kismet.RANDOM;
        return random.nextDouble() < getKeyChance();
    }

    private double getKeyChance() {
        return 1.0 / (this.skipped + 1);
    }

    /**
     * Tries to get a new target from config
     *
     * @return true if a new target has been assigned (even if the new target is invalid/null)
     */
    public boolean getNewTarget() {
        // a timeout for server issues
        if (this.newTargetTimeout > this.worldObj.getTotalWorldTime()) return false;

        // remove the fulfillment
        final IBlockState state = this.worldObj.getBlockState(this.pos);
        this.worldObj.setBlockState(this.pos, state.withProperty(BlockDisplay.FULFILLED, false));

        // only server pls from now on
        if (this.worldObj.isRemote) {
            return false;
        }

        final InformedStack oldTarget = this.target;

        TargetGenerationResult targetResult = TargetLibrary.generateTarget(this.weights, this.history);
        if (targetResult.hasFlag()) {
            this.newTargetTimeout = this.worldObj.getTotalWorldTime() + 5 * 20;
            Log.warning("Failed to get target, " + targetResult.getFlag());
        }
        setTarget(targetResult.getValue());

        // sync client with server as target picking only happens server-wise (for safety)
        if (oldTarget != this.target) {
            resetDeadline();
            return true;
        }

        return false;
    }

    public boolean isFulfilled() {
        return this.worldObj.getBlockState(this.pos).getValue(BlockDisplay.FULFILLED);
    }

    private void setFulfilled(boolean fulfilled) {
        IBlockState state = this.worldObj.getBlockState(this.pos);
        boolean oldFulfilled = state.getValue(BlockDisplay.FULFILLED);
        // optimization trick, less state packets
        if (oldFulfilled == fulfilled) return;
        state = state.withProperty(BlockDisplay.FULFILLED, fulfilled);
        this.worldObj.setBlockState(this.pos, state);
    }

    public boolean isReady() {
        return this.target != null && this.target.hasItem();
    }

    public String getStylizedKeyChance() {
        double chance = getKeyChance();
        if (chance < 0.001) {
            return "< 0.001%";
        } else {
            return String.format("%.3f%%", chance * 100);
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == ITEM_HANDLER_CAPABILITY) {
            if (facing != null && facing != this.worldObj.getBlockState(this.pos).getValue(BlockDisplay.FACING)
                    .getOpposite())
                return super.getCapability(capability, facing);
            //noinspection unchecked
            return (T) this.targetSlot;
        }
        return super.getCapability(capability, facing);
    }


    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == ITEM_HANDLER_CAPABILITY) {
            if (facing != null && facing != this.worldObj.getBlockState(this.pos).getValue(BlockDisplay.FACING)
                    .getOpposite())
                return super.hasCapability(capability, facing);
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        setSkipped(nbt.getInteger("skipped"));
        setDeadline(nbt.getLong("deadline"));
        setScore(nbt.getInteger("score"));
        setHighScore(nbt.getInteger("highScore"));

        if (nbt.hasKey("target")) {
            this.target = new InformedStack(nbt.getCompoundTag("target"));
        } else {
            this.target = null;
        }

        // 10 for COMPOUND, check NBTBase
        NBTTagList modWeightsNbt = nbt.getTagList("weights", 10);
        for (int i = 0; i < modWeightsNbt.tagCount(); i++) {
            NBTTagCompound nbtTagCompound = modWeightsNbt.getCompoundTagAt(i);
            String modId = nbtTagCompound.getString("id");
            int modWeight = nbtTagCompound.getInteger("weight");
            this.weights.put(modId, modWeight);
        }

        this.history.clear();
        NBTTagList lastTargetsNbt = nbt.getTagList("history", 10);
        for (int i = 0; i < lastTargetsNbt.tagCount(); i++) {
            NBTTagCompound compound = lastTargetsNbt.getCompoundTagAt(i);
            this.history.add(new InformedStack(compound));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        compound.setInteger("skipped", getSkipped());
        compound.setLong("deadline", getDeadline());
        compound.setInteger("score", getScore());
        compound.setInteger("highScore", getHighScore());

        // target can be null :/
        if (getTarget() != null) {
            NBTTagCompound targetTag = getTarget().writeToNBT();
            compound.setTag("target", targetTag);
        }

        NBTTagList modWeightsNbt = new NBTTagList();
        for (String key : this.weights.keySet()) {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            nbtTagCompound.setString("id", key);
            nbtTagCompound.setInteger("weight", this.weights.get(key));
            modWeightsNbt.appendTag(nbtTagCompound);
        }
        compound.setTag("weights", modWeightsNbt);

        NBTTagList lastTargetsNbt = new NBTTagList();
        for (InformedStack lastTarget : this.history) {
            NBTTagCompound targetTag = lastTarget.writeToNBT();
            lastTargetsNbt.appendTag(targetTag);
        }
        compound.setTag("history", lastTargetsNbt);
    }

    @Override
    public Packet<INetHandlerPlayClient> getDescriptionPacket() {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        writeToNBT(nbtTagCompound);
        return new SPacketUpdateTileEntity(this.pos, getBlockMetadata(), nbtTagCompound);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }

    public InformedStack getTarget() {
        return this.target;
    }

    public void setTarget(InformedStack target) {
        InformedStack oldTarget = this.target;

        if (!StackHelper.isEquivalent(this.target, target)) {
            this.target = target;
        }

        // check if we need to force a block update regarding the ready
        if (oldTarget != target)
            this.stateChanged = true;
    }

    public void setSkipped(int skipped) {
        if (skipped < 0) return;
        this.skipped = skipped;
        this.stateChanged = true;
    }

    public int getSkipped() {
        return this.skipped;
    }

    public void setHighScore(int highScore) {
        this.highScore = highScore;
    }

    public int getHighScore() {
        return highScore;
    }
}
