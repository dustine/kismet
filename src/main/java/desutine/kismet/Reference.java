package desutine.kismet;

public final class Reference {
    public static final String MOD_ID = "kismet";
    public static final String VERSION = "1.9-0.0.2.0";
    public static final String SERVER_PROXY_CLASS = "desutine.kismet.proxy.ServerProxy";
    public static final String CLIENT_PROXY_CLASS = "desutine.kismet.proxy.ClientProxy";

    public static final String GUI_FACTORY_CLASS = "desutine.kismet.client.gui.ModGuiFactory";

    public static class Colors {
        public static final int LIME = 0x00ff00;
        public static final int RED = 0xff0000;
    }

    public static final class Names {
        public static final String MOD = "Kismet";

        public static final class Items {
            public static final String KEY = "key";
            public static final String NONE = "none";
        }

        public static final class Blocks {
            public static final String DISPLAY = "display";
        }

        public static final class Tiles {

            public static final String TE_DISPLAY = "te-display";
        }

        public static final class Config {

            public static final String CONFIG_GUI_TITLE = "gui.config.mainTitle";
            public static String CONFIG_CATEGORY_MAIN = "gui.config.category.main";
        }
    }
}
