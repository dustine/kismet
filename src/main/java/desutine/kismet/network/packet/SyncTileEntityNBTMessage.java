package desutine.kismet.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class SyncTileEntityNBTMessage implements IMessage {

  public NBTTagCompound nbtTagCompound;

  public SyncTileEntityNBTMessage() {
  }

  public SyncTileEntityNBTMessage(final TileEntity tileEntity) {
    this.nbtTagCompound = new NBTTagCompound();
    tileEntity.writeToNBT(nbtTagCompound);
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    nbtTagCompound = ByteBufUtils.readTag(buf);
  }

  @Override
  public void toBytes(ByteBuf buf) {
    ByteBufUtils.writeTag(buf, nbtTagCompound);
  }
}
