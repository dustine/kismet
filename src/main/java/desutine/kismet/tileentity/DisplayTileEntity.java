package desutine.kismet.tileentity;

import desutine.kismet.block.DisplayBlock;
import desutine.kismet.reference.Blocks;
import desutine.kismet.reference.Items;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DisplayTileEntity extends TileEntity implements ITickable {
    private int streak;
    private boolean fulfilled;
    private long deadline;
    private ItemStack target;
    private boolean stateChanged;
//    private String target;
//    private TargetType type;

    public DisplayTileEntity() {
        // generate a fresh new ip!
//        target = Reference.MODID + ':' + Names.KEY;
//        type = TargetType.ITEM;
        super();
        setTarget(new ItemStack(Items.itemKey));
    }

    @Override
    public void update() {
        // if server
//        if (!this.worldObj.isRemote) {
        // isDirty is set to true whenever the internal state of the block is changed
        // name coming from the parent's method markDirty()
            boolean isDirty = false;

        isDirty |= checkForDeadline(isDirty);

            if (isDirty) {
                markDirty();
            }
        if (this.worldObj.isRemote && this.stateChanged) {
            stateChanged = false;
            updateClientDisplay();
        }
//        }
    }

    private boolean checkForDeadline(boolean isDirty) {
        if (getDeadline() < worldObj.getTotalWorldTime()) {
            // todo - unhardcode the ammount, also i'm assuming 24000 ticks = one day, hah
            setDeadline(worldObj.getTotalWorldTime() + 100);

            if (isFulfilled()) {
                setStreak((getStreak() + 1) % 15);
            } else {
                setStreak(0);
            }
            setFulfilled(false);
            isDirty = true;
        }
        return isDirty;
    }

    @SideOnly(Side.CLIENT)
    private void updateClientDisplay() {
        IBlockState oldState = worldObj.getBlockState(pos);
        IBlockState newState = Blocks.kismetDisplayBlock.getActualState(oldState, worldObj, pos);
        // kinda of a hack but really only to force the block to update in tone ._.
        worldObj.markAndNotifyBlock(pos, worldObj.getChunkFromBlockCoords(pos), oldState, newState, 2);
//        Kismet.packetHandler.updateClientDisplay(worldObj.provider.getDimension(), pos, this);
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

    public void setStreak(int streak) {
        this.streak = streak;
        this.stateChanged = true;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        setDeadline(compound.getLong("deadline"));
        setStreak(compound.getInteger("streak"));
        setFulfilled(compound.getBoolean("fulfilled"));
        if (compound.hasKey("target")) {
            getTarget().readFromNBT(compound.getCompoundTag("target"));
        }
//        target = compound.getString("target");
//        type = TargetType.valueOf(compound.getString("type"));

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
        return state.withProperty(DisplayBlock.STREAK, getStreak())
                .withProperty(DisplayBlock.FULFILLED, isFulfilled());
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
//        compound.setString("target", target);
//        compound.setString("type", type.name());
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
