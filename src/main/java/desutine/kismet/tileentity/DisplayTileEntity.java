package desutine.kismet.tileentity;

import desutine.kismet.Kismet;
import desutine.kismet.ModConfig;
import desutine.kismet.block.DisplayBlock;
import desutine.kismet.reference.Blocks;
import desutine.kismet.reference.Items;
import mezz.jei.RecipeRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class DisplayTileEntity extends TileEntity implements ITickable {
    private static final int STREAK_MAX = 20;
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
    }

    private boolean checkForDeadline(boolean isDirty) {
        if (getDeadline() < worldObj.getTotalWorldTime()) {
            setDeadline(worldObj.getTotalWorldTime() + ModConfig.getTimeLimit());

            if (isFulfilled()) {
                setStreak(getStreak() + 1);
            } else {
                setStreak(0);
            }

            getNewTarget();
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

    public void getNewTarget() {
        // only server pls
        if(worldObj.isRemote) return;

        int nrBlocks = GameData.getBlockRegistry().getKeys().size();
        int nrItems = GameData.getItemRegistry().getKeys().size();
        Random random = new Random();
        if(random.nextDouble() < ((double) nrBlocks) / (nrItems + nrBlocks)){
            // blocks
            target = new ItemStack(GameData.getBlockRegistry().getRandomObject(random));
        } else {
            // items
            target = new ItemStack(GameData.getItemRegistry().getRandomObject(random));
        }

        // new target, no fulfillment
        setFulfilled(false);
        // sync client with server as this won't be true anymore after this function
        Kismet.packetHandler.syncDisplayTargetToClient(worldObj.provider.getDimension(), this);
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
    }@Override
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
