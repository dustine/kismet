package dustine.kismet.config;

import dustine.kismet.block.BlockChillDisplay;
import dustine.kismet.block.BlockKismet;
import dustine.kismet.block.BlockTimedDisplay;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public class ConfigCopy implements INBTSerializable<NBTTagCompound> {
    private boolean chillEnabled;
    private boolean timedEnabled;
    private int timedLimit;

    public ConfigCopy() {
        this.chillEnabled = ConfigKismet.isChillEnabled();
        this.timedEnabled = ConfigKismet.isTimedEnabled();
        this.timedLimit = ConfigKismet.getTimedLimit();
    }

    public int getTimedLimit() {
        return this.timedLimit;
    }

    @Override public NBTTagCompound serializeNBT() {
        final NBTTagCompound compound = new NBTTagCompound();

        compound.setBoolean("chillEnabled", this.chillEnabled);
        compound.setBoolean("timedEnabled", this.timedEnabled);
        compound.setInteger("timedLimit", this.timedLimit);

        return compound;
    }

    @Override public void deserializeNBT(NBTTagCompound compound) {
        this.chillEnabled = compound.getBoolean("chillEnabled");
        this.timedEnabled = compound.getBoolean("timedEnabled");
        this.timedLimit = compound.getInteger("timedLimit");
    }

    public boolean isBlockEnabled(BlockKismet blockType) {
        if (blockType instanceof BlockTimedDisplay) {
            return isTimedEnabled();
        } else
            return blockType instanceof BlockChillDisplay && isChillEnabled();
    }

    public boolean isTimedEnabled() {
        return this.timedEnabled;
    }

    public boolean isChillEnabled() {
        return this.chillEnabled;
    }
}
