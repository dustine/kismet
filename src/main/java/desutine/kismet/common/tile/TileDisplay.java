package desutine.kismet.common.tile;

import desutine.kismet.Kismet;
import desutine.kismet.common.config.ConfigKismet;
import desutine.kismet.common.block.BlockDisplay;
import desutine.kismet.reference.Blocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TileDisplay extends TileEntity implements ITickable {
    private static final int STREAK_MAX = 20;
    private int streak;
    private boolean fulfilled;
    private long deadline;
    private ItemStack target;
    private List<ItemStack> lastTargets;
    private HashMap<String, Integer> modWeights;
    private boolean stateChanged;
//    private String target;
//    private TargetType type;

    public TileDisplay() {
        // generate a fresh new ip!
//        target = Reference.MODID + ':' + Names.KEY;
//        type = TargetType.ITEM;
        super();
        modWeights = new HashMap<String, Integer>();
//        setTarget(new ItemStack(Items.itemKey));
        lastTargets = new ArrayList<ItemStack>();
    }

    @Override
    public void update() {
        // isDirty is set to true whenever the internal state of the block is changed
        // name coming from the parent's method markDirty()
        boolean isDirty = false;
        isDirty |= checkForDeadline();
        isDirty |= checkForNullTarget();
        if (isDirty) {
            markDirty();
        }
        if (this.worldObj.isRemote && this.stateChanged) {
            stateChanged = false;
            updateClientDisplay();
        }
    }

    private boolean checkForNullTarget() {
        if(getTarget() == null) {
            return getNewTarget();
        }
        return false;
    }

    private boolean checkForDeadline() {
        if (getDeadline() < worldObj.getTotalWorldTime()) {
            setDeadline(worldObj.getTotalWorldTime() + ConfigKismet.getTimeLimit());

            if (isFulfilled()) {
                setStreak(getStreak() + 1);
            } else {
                setStreak(0);
            }

            return getNewTarget();
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    private void updateClientDisplay() {
        IBlockState oldState = worldObj.getBlockState(pos);
        IBlockState newState = Blocks.kismetDisplayBlock.getActualState(oldState, worldObj, pos);
        // kinda of a hack but really only to force the block to update in tone ._.
        worldObj.markAndNotifyBlock(pos, worldObj.getChunkFromBlockCoords(pos), oldState, newState, 2);
    }

    public long getDeadline() {
        return deadline;
    }

    public boolean isFulfilled() {
        return fulfilled;
    }

    public int getStreak() {
        return streak;
    }

    /**
     * Tries to get a new target from config
     * @return true if a new target has been assigned, false if not or a null target has been assigned
     */
    public boolean getNewTarget() {
        // only server pls
        if(worldObj.isRemote) return false;

//        int nrBlocks = GameData.getBlockRegistry().getKeys().size();
//        int nrItems = GameData.getItemRegistry().getKeys().size();
//        Random random = new Random();
//        if(random.nextDouble() < ((double) nrBlocks) / (nrItems + nrBlocks)){
//            // blocks
//            target = new ItemStack(GameData.getBlockRegistry().getRandomObject(random));
//        } else {
//            // items
//            target = new ItemStack(GameData.getItemRegistry().getRandomObject(random));
//        }
        ItemStack target = ConfigKismet.generateTarget(modWeights, lastTargets);
        setTarget(target);
        if(target == null) return false;
        // new target, no fulfillment
        setFulfilled(false);
        // sync client with server as target picking only happens server-wise (for safety)
        Kismet.packetHandler.syncDisplayTargetToClient(worldObj.provider.getDimension(), this);
        return true;
    }

    public void setStreak(int streak) {
        int oldStreak = this.streak;
        this.streak = streak;
        // TO-DO: Unhardcode the streak limit
        if(this.streak > STREAK_MAX) {
            this.streak = STREAK_MAX;
            // don't cause a state update if the old streak was already over the max
            if(oldStreak > STREAK_MAX) return;
        }
        this.stateChanged = true;
    }

    public void setFulfilled(boolean fulfilled) {
        this.fulfilled = fulfilled;
        this.stateChanged = true;
    }
    public void setDeadline(long deadline) {
        this.deadline = deadline;
        // not really needed as it's not directly related to display
//        this.stateChanged = true;
    }
    public IBlockState enrichState(IBlockState state) {
        return state.withProperty(BlockDisplay.STREAK, getStreak())
                .withProperty(BlockDisplay.FULFILLED, isFulfilled());
    }@Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        setDeadline(compound.getLong("deadline"));
        setStreak(compound.getInteger("streak"));
        setFulfilled(compound.getBoolean("fulfilled"));
        if (compound.hasKey("target")) {
            getTarget().readFromNBT(compound.getCompoundTag("target"));
        }

        // 10 for COMPOUND, check NBTBase
        NBTTagList modWeightsNbt = compound.getTagList("modWeights", 10);
        for (int i = 0; i < modWeightsNbt.tagCount(); i++) {
            NBTTagCompound nbtTagCompound = modWeightsNbt.getCompoundTagAt(i);
            String modId = nbtTagCompound.getString("id");
            int modWeight = nbtTagCompound.getInteger("weight");
            this.modWeights.put(modId, modWeight);
        }

        lastTargets.clear();
        NBTTagList lastTargetsNbt = compound.getTagList("lastTargets", 10);
        for (int i = 0; i < lastTargetsNbt.tagCount(); i++) {
            NBTTagCompound nbtTagCompound = lastTargetsNbt.getCompoundTagAt(i);
            lastTargets.add(ItemStack.loadItemStackFromNBT(nbtTagCompound));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        compound.setLong("deadline", getDeadline());
        compound.setInteger("streak", getStreak());
        compound.setBoolean("fulfilled", isFulfilled());
        // target can be null :/
        if (getTarget() != null) {
            NBTTagCompound targetTag = new NBTTagCompound();
            getTarget().writeToNBT(targetTag);
            compound.setTag("target", targetTag);
        }

        NBTTagList modWeightsNbt = new NBTTagList();
        for (String key:modWeights.keySet()) {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            nbtTagCompound.setString("id", key);
            nbtTagCompound.setInteger("weight", modWeights.get(key));
            modWeightsNbt.appendTag(nbtTagCompound);
        }
        compound.setTag("modWeights", modWeightsNbt);

        NBTTagList lastTargetsNbt = new NBTTagList();
        for (int i = 0; i < lastTargets.size(); i++) {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            lastTargets.get(i).writeToNBT(nbtTagCompound);
            lastTargetsNbt.appendTag(nbtTagCompound);
        }
        compound.setTag("lastTargets", lastTargetsNbt);
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


    public ItemStack getTarget() {
        return target;
    }

    public void setTarget(ItemStack target) {
        this.target = target;
        // not really needed as it's used up by the TESR so that updates in real time-ish
//        this.stateChanged = true;
    }
}
