package gs.down.loader;

import gs.app.lib.application.App;
import gs.app.lib.application.AppConfig;

public class Main {
    public static void main(String[] args) {
        /* Start the application */
        new App(new AppConfig("Downloader", 0, 0, 600, 400, false), new Loader());
    }
}
