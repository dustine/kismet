package com.desutine.kismet;

import com.desutine.kismet.block.BlockDisplay;
import com.desutine.kismet.reference.Reference;
import com.desutine.kismet.tileentity.TileEntityDisplay;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.WorldAccessContainer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import java.awt.*;

public class ModPacketHandler {
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MODID);
    private static int displacer = 0;
    private static int getDisplacer() {
        return displacer++;
    }

    public static class DisplayUpdateMessage implements IMessage{
        private BlockPos pos;
        private int streak;
        private boolean fulfilled;

        // A default constructor is always required
        public DisplayUpdateMessage(){}

        public DisplayUpdateMessage(BlockPos pos, boolean fulfilled, int streak){
            this.pos = pos.getImmutable();
            this.fulfilled = fulfilled;
            this.streak = streak;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            int x,y,z;
            x = buf.readInt();
            y = buf.readInt();
            z = buf.readInt();
            pos = new BlockPos(x, y, z);
            fulfilled = buf.readBoolean();
            streak = buf.readInt();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(pos.getX());
            buf.writeInt(pos.getY());
            buf.writeInt(pos.getZ());
            buf.writeBoolean(fulfilled);
            buf.writeInt(streak);
        }
    }

    public static class DisplayUpdateMessageHandler implements IMessageHandler<DisplayUpdateMessage, IMessage> {
        @Override
        public IMessage onMessage(final DisplayUpdateMessage message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    IBlockState state = Minecraft.getMinecraft().theWorld.getBlockState(message.pos);
                    Chunk chunk = Minecraft.getMinecraft().theWorld.getChunkFromBlockCoords(message.pos);

                    TileEntityDisplay tt = (TileEntityDisplay) Minecraft.getMinecraft().theWorld.getTileEntity(message.pos);
                    tt.streak = message.streak;
                    tt.fulfilled = message.fulfilled;
                    Minecraft.getMinecraft().theWorld.markAndNotifyBlock(message.pos, chunk, state, tt.enrichState
                            (state), 2);
                }
            });
            return null;
        }
    }

    public void updateClientDisplay(int dimension, final BlockPos pos, boolean fulfilled, int streak){
        INSTANCE.registerMessage(DisplayUpdateMessageHandler.class, DisplayUpdateMessage.class, getDisplacer(), Side
                .CLIENT);
        INSTANCE.sendToDimension(new DisplayUpdateMessage(pos, fulfilled, streak), dimension);
    }
}
