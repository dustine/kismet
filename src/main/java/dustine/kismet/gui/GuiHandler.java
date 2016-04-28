package dustine.kismet.gui;

import dustine.kismet.Kismet;
import net.minecraft.entity.player.EntityPlayer;
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
                return null;
        }
        throw new IllegalArgumentException("Unknown GUI ID");
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch (EnumGuiID.values()[ID]) {
            case DISPLAY:
                return null;
        }
        throw new IllegalArgumentException("Unknown GUI ID");
    }

    public enum EnumGuiID {
        DISPLAY
    }
}
