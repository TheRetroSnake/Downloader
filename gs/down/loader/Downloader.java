package gs.down.loader;

import gs.app.lib.application.App;
import gs.app.lib.util.FileUtil;
import gs.app.lib.util.KeyUtil;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class Downloader implements Runnable {
    public static ArrayList<String> texts = new ArrayList<String>();
    private static final String version = "1.0.1";
    private static final String updateAdr = "http://discocentral.digibase.ca/SPP/update/";
    private static final String folder = System.getProperty("user.dir").replace("\\", "/");
    /* gets start of OS name. Win, Mac, Linus, SunOS or FreeBSD (some others exist, but fuck them, nobody uses anyway, right? RIGHT?) */
    private static final String OS = System.getProperty("os.name").split(" ")[0].replace("dows", "");

    @Override
    public void run() {
        title("Checking downloader version");
        App.repaint();
        String ver = GetVersion(updateAdr +"latest.txt");

        if (!ver.equals(version)) {
            latest("Outdated downloader!");
            display("Current version: "+ ver);
            display("Newest version: "+ version);
            title("Please download an update from release threads");
            App.repaint();

        } else {
            latest("Up to date!");
            title("Finding files to update");
            App.repaint();
            UpdateFiles(updateAdr + "downloads.txt");
        }

        title("Press Enter to exit");
        App.repaint();

        while (!KeyUtil.isHeld(KeyUtil.ENTER)) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        App.exit(0);
    }

    public static byte[] download(String address) throws IOException {
        URLConnection c = new URL(address).openConnection();
        InputStream in = new BufferedInputStream(c.getInputStream());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        int downloaded = 0;
        long len = GetFileSize(c);
        display("Downloaded 0 bytes out of "+ GetBytes(len));
        App.repaint();

        byte[] buf = new byte[1024];
        int n;

        while (-1 != (n = in.read(buf))){
            out.write(buf, 0, n);
            downloaded += n;
            latest("Downloaded "+ (int)((float)downloaded / len * 100) +"% ("+ GetBytes(downloaded) +"/"+ GetBytes(len) +")");
            App.repaint();
        }

        out.close();
        in.close();
        return out.toByteArray();
    }

    private static long GetFileSize(URLConnection conn) throws IOException {
        return conn.getContentLengthLong();
    }

    private void UpdateFiles(String adr) {
        try {
            String[] files = new String(download(adr)).split("\n");
            remove();
            int fileID = 1;
            int total = 0;
            for(String file : files){
                display("Downloading " + file.replace("%OS%", OS) +" file "+ fileID +"/"+ files.length);
                App.SetTitle(folder +"/"+ file.replace("%OS%", OS));

                byte[] down = download(updateAdr + file.replace("%OS%", OS));
                saveFile(folder + "/" + file.replace("%OS%", OS), down);

                remove();
                latest("file " + file.replace("%OS%", OS) + " size " + GetBytes(down.length) +" file "+ fileID +"/"+ files.length);

                total += down.length;
                fileID ++;
                App.repaint();
            }

            display("Update complete! Downloaded total of "+ files.length +" files with size of "+ GetBytes(total) +" ("+ total +" bytes)");
        } catch (IOException e) {
            e.printStackTrace();
            display("Failed to download!");
        }
    }

    private void saveFile(String file, byte[] data) {
        FileUtil.mkdirs(DiscludeFile(file));

        if(FileUtil.exists(file)){
            FileUtil.delete(file);
        }
        FileUtil.writeBytes(file, data, false);
    }

    private String DiscludeFile(String file) {
        return file.substring(0, file.lastIndexOf("/"));
    }

    private String GetVersion(String adr) {
        try {
            return new String(download(adr)).split("\n")[0].replace("DownloaderVersion: ", "");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void title(String text) {
        texts.add(texts.size(), text);
        App.SetTitle(text);
    }

    private static void display(String text) {
        texts.add(texts.size(), text);
    }

    private static void latest(String text) {
        texts.set(texts.size() -1, text);
    }

    private void remove() {
        texts.remove(texts.size() -1);
    }

    private static String GetBytes(long size) {
        if(size >= 1048576){
            return String.format("%.02f", ((float)size / 1048576)) +" MegaBytes";
        }  if(size >= 1024){
            return String.format("%.02f", ((float)size / 1024)) +" KiloBytes";
        }

        return size +" Bytes";
    }
}
