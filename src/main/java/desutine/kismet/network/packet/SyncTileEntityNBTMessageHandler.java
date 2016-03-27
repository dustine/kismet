package desutine.kismet.network.packet;

import desutine.kismet.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SyncTileEntityNBTMessageHandler implements IMessageHandler<SyncTileEntityNBTMessage,
        IMessage> {
  @Override
  public IMessage onMessage(final SyncTileEntityNBTMessage message, MessageContext ctx) {
    Minecraft.getMinecraft().addScheduledTask(new Runnable() {
      @Override
      public void run() {
        int x, y, z;
        x = message.nbtTagCompound.getInteger("x");
        y = message.nbtTagCompound.getInteger("y");
        z = message.nbtTagCompound.getInteger("z");
        BlockPos pos = new BlockPos(x, y, z);

        TileEntity tileEntity = Minecraft.getMinecraft().theWorld.getTileEntity(pos);
        if (tileEntity == null) return;

        try {
          tileEntity.readFromNBT(message.nbtTagCompound);
        } catch (Throwable te) {
          ModLogger.error(te);
        }
      }
    });
    return null;
  }
}
