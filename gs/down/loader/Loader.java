package gs.down.loader;

import gs.app.lib.application.App;
import gs.app.lib.application.AppRun;
import gs.app.lib.gfx.Graphics;

import java.awt.*;

public class Loader extends AppRun {
    public void create(){
        /* start the downloading process */
        App.SetTitle("SoniPlane downloader");
        new Thread(new Downloader()).start();
    }

    public void render(Graphics g) {
        /* set color to black for text */
        g.setColor(Color.BLACK);

        if(Downloader.texts.size() > 0) {   // make sure the text length is greater than 0, so don't crash
            /* Get array of strings of what we want to render */
            String[] t = Downloader.texts.toArray(new String[Downloader.texts.size()]);
            /* set initial y-offset */
            int y = (int) (2 + Graphics.GetTextHeight(t[0]));

            /* main loop for drawing */
            for (int i = t.length - 1;i >= 0;i --) {
                /* draw next line to application height - y-offset */
                g.drawText(t[i], 2, App.GetBounds().h - y);
                /* increase y-offset by text height */
                y += Graphics.GetTextHeight(t[i]);
            }
        }
    }
}
