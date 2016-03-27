package desutine.kismet.network;

import desutine.kismet.network.packet.SyncTileEntityNBTMessage;
import desutine.kismet.network.packet.SyncTileEntityNBTMessageHandler;
import desutine.kismet.reference.Reference;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandlerKismet {
    private static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MODID);
    private static int displacer = 0;

    public void syncDisplayTargetToClient(int dimension, TileEntity tileEntity){
        INSTANCE.registerMessage(SyncTileEntityNBTMessageHandler.class, SyncTileEntityNBTMessage.class, getDisplacer(),
                Side.CLIENT);

        INSTANCE.sendToDimension(new SyncTileEntityNBTMessage(tileEntity), dimension);
    }

    private static int getDisplacer() {
        return displacer++;
    }

}
