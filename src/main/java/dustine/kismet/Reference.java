package dustine.kismet;

import net.minecraft.util.ResourceLocation;

public final class Reference {
    public static final String MOD_ID = "kismet";
    public static final String VERSION = "@VERSION@";
    public static final String SERVER_PROXY_CLASS = "dustine.kismet.proxy.ServerProxy";
    public static final String CLIENT_PROXY_CLASS = "dustine.kismet.proxy.ClientProxy";
    public static final String GUI_FACTORY_CLASS = "dustine.kismet.client.gui.ModGuiFactory";

    public static final class Colors {
        public static final int TEXT_GREY = 0x404040;
    }

    public static final class Names {
        public static final String MOD = "Kismet";
        public static final String MOD_PRETTY = "§eKismet";
        public static final String TARGET_DATABASE = MOD_ID + "TargetDatabase";

        public static final class Items {
            public static final String KEY = "key";
        }

        public static final class Blocks {
            public static final String TIMED_DISPLAY = "timedDisplay";
            public static final String CHILL_DISPLAY = "chillDisplay";
        }

        public static final class Tiles {
            public static final String TILE_DISPLAY = "tile_display";
        }
    }

    public static class GUI {
        public static final ResourceLocation DISPLAY = new ResourceLocation("kismet:textures/gui/display.png");
    }
}
