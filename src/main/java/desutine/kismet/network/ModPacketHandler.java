package desutine.kismet.network;

import desutine.kismet.reference.Reference;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class ModPacketHandler {
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MODID);
    private static int displacer = 0;

    //    public void updateClientDisplay(int dimension, final BlockPos pos, DisplayTileEntity tileEntity) {
//        INSTANCE.registerMessage(DisplayUpdateMessageHandler.class, DisplayUpdateMessage.class, getDisplacer(), Side
//                .CLIENT);
//        INSTANCE.sendToDimension(new DisplayUpdateMessage(pos, tileEntity.isFulfilled(), tileEntity.getStreak(), tileEntity
//                .getTarget()), dimension);
//    }
//
    private static int getDisplacer() {
        return displacer++;
    }

//
//    public static class DisplayUpdateMessage implements IMessage {
//        private BlockPos pos;
//        private int streak;
//        private boolean fulfilled;
//        private ItemStack target;
//
//        // A default constructor is always required
//        public DisplayUpdateMessage() {
//        }
//
//        public DisplayUpdateMessage(BlockPos pos, boolean fulfilled, int streak, ItemStack target) {
//            this.pos = pos.getImmutable();
//            this.fulfilled = fulfilled;
//            this.streak = streak;
//            this.target = target;
//        }
//
//        @Override
//        public void fromBytes(ByteBuf buf) {
//            int x, y, z;
//            x = buf.readInt();
//            y = buf.readInt();
//            z = buf.readInt();
//            pos = new BlockPos(x, y, z);
//            fulfilled = buf.readBoolean();
//            streak = buf.readInt();
//            target = ByteBufUtils.readItemStack(buf);
//        }
//
//        @Override
//        public void toBytes(ByteBuf buf) {
//            buf.writeInt(pos.getX());
//            buf.writeInt(pos.getY());
//            buf.writeInt(pos.getZ());
//            buf.writeBoolean(fulfilled);
//            buf.writeInt(streak);
//            ByteBufUtils.writeItemStack(buf, target);
//        }
//    }
//
//    public static class DisplayUpdateMessageHandler implements IMessageHandler<DisplayUpdateMessage, IMessage> {
//        @Override
//        public IMessage onMessage(final DisplayUpdateMessage message, MessageContext ctx) {
//            Minecraft.getMinecraft().addScheduledTask(new Runnable() {
//                @Override
//                public void run() {
//                    IExtendedBlockState state = getBlockState();
//                    Chunk chunk = Minecraft.getMinecraft().theWorld.getChunkFromBlockCoords(message.pos);
//
//                    DisplayTileEntity tt = (DisplayTileEntity) Minecraft.getMinecraft().theWorld.getTileEntity(message.pos);
//                    if (tt == null) return;
//                    tt.setStreak(message.streak);
//                    tt.setFulfilled(message.fulfilled);
//                    tt.setTarget(message.target);
////                    Minecraft.getMinecraft().theWorld.markAndNotifyBlock(message.pos, chunk, state, getBlockState(), 2);
//                    Minecraft.getMinecraft().theWorld.markBlockRangeForRenderUpdate(message.pos, message.pos);
//                }
//
//                IExtendedBlockState getBlockState() {
//                    return (IExtendedBlockState) Minecraft.getMinecraft().theWorld.getBlockState(message.pos);
//                }
//            });
//            return null;
//        }
//    }
}
