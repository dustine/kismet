package dustine.kismet.gui;

import dustine.kismet.Kismet;
import dustine.kismet.client.gui.GuiDisplay;
import dustine.kismet.inventory.ContainerDisplay;
import dustine.kismet.tile.TileDisplay;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class GuiHandler implements IGuiHandler {
    private final static IGuiHandler INSTANCE = new GuiHandler();

    private GuiHandler() {
    }

    public static void register() {
        NetworkRegistry.INSTANCE.registerGuiHandler(Kismet.instance, GuiHandler.INSTANCE);
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch (EnumGuiID.values()[ID]) {
            case DISPLAY:
                return new ContainerDisplay(player.inventory, (TileDisplay) world.getTileEntity(new BlockPos(x, y, z)));
        }
        throw new IllegalArgumentException("Unknown GUI ID");
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch (EnumGuiID.values()[ID]) {
            case DISPLAY:
                return new GuiDisplay(player.inventory, (TileDisplay) world.getTileEntity(new BlockPos(x, y, z)));
        }
        throw new IllegalArgumentException("Unknown GUI ID");
    }

    public enum EnumGuiID {
        DISPLAY
    }
}
