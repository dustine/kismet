package desutine.kismet;

public final class Reference {
    public static final String MOD_ID = "kismet";
    public static final String VERSION = "@VERSION@";
    public static final String SERVER_PROXY_CLASS = "desutine.kismet.proxy.ServerProxy";
    public static final String CLIENT_PROXY_CLASS = "desutine.kismet.proxy.ClientProxy";
    public static final String GUI_FACTORY_CLASS = "desutine.kismet.client.gui.ModGuiFactory";

    public static class Colors {
        public static final int LIME = 0x00ff00;
        public static final int RED = 0xff0000;
        public static final int CYAN = 0x00ffff;
    }

    public static final class Names {
        public static final String MOD = "Kismet";

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
}
