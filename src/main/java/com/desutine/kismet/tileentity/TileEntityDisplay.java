package com.desutine.kismet.tileentity;

import com.desutine.kismet.Kismet;
import com.desutine.kismet.ModLogger;
import com.desutine.kismet.ModPacketHandler;
import com.desutine.kismet.block.BlockDisplay;
import com.desutine.kismet.reference.Names;
import com.desutine.kismet.reference.Reference;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityDisplay extends TileEntity implements ITickable {
    private long deadline;
    public int streak;
    public boolean fulfilled;
    private String target;
    private TargetType type;

    public TileEntityDisplay() {
        // generate a fresh new ip!
        target = Reference.MODID + ':' + Names.KEY_NAME;
        type = TargetType.ITEM;
    }

    @Override
    public void update() {
        if(!this.worldObj.isRemote){
            boolean isDirty = false;
            // if server
            if(deadline < worldObj.getTotalWorldTime()){
                // todo - unhardcode the ammount, also i'm assuming 24000 ticks = one day, hah
                deadline = worldObj.getTotalWorldTime() + 200;

                fulfilled = !fulfilled;
                streak = (streak + 1) % 15;
                isDirty = true;
            }

            if(isDirty){
                markDirty();
                // update via web towards the client
                Kismet.packetHandler.updateClientDisplay(worldObj.provider.getDimension(), pos, fulfilled, streak);
            }
        }
    }

    public IBlockState enrichState(IBlockState state) {
        return state.withProperty(BlockDisplay.STREAK, streak)
                .withProperty(BlockDisplay.FULFILLED, fulfilled);
    }

    private enum TargetType {
        ITEM, BLOCK
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        compound.setLong("deadline", deadline);
        compound.setInteger("streak", streak);
        compound.setBoolean("fulfilled", fulfilled);
        compound.setString("target", target);
        compound.setString("type", type.name());
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        deadline = compound.getLong("deadline");
        streak = compound.getInteger("streak");
        fulfilled = compound.getBoolean("fulfilled");
        target = compound.getString("target");
        type = TargetType.valueOf(compound.getString("type"));
    }
}
