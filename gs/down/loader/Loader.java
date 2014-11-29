package gs.down.loader;

import gs.app.lib.application.App;
import gs.app.lib.application.AppRun;
import gs.app.lib.gfx.Graphics;

import java.awt.*;

public class Loader extends AppRun {
    public void create(){
        App.SetTitle("SoniPlane downloader");
        new Thread(new Downloader()).start();
    }

    public void render(Graphics g) {
        g.setColor(Color.BLACK);

        if(Downloader.texts.size() > 0) {
            String[] t = Downloader.texts.toArray(new String[Downloader.texts.size()]);
            int y = (int) (2 + Graphics.GetTextHeight(t[0]));

            for (int i = t.length - 1;i >= 0;i --) {
                g.drawText(t[i], 2, App.GetBounds().h - y);
                y += Graphics.GetTextHeight(t[i]);
            }
        }
    }
}
