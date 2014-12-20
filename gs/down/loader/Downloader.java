package gs.down.loader;

import gs.app.lib.application.App;
import gs.app.lib.util.FileUtil;
import gs.app.lib.util.KeyUtil;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class Downloader implements Runnable {
    /* list of texts to display on screen */
    public static ArrayList<String> texts = new ArrayList<String>();
    /* version number */
    private static final String version = "1.0.4";
    /* web address to download files from */
    private static final String updateAdr = "http://discocentral.digibase.ca/SPP/update/";
    /* folder to save files to */
    private static final String folder = FileUtil.getJarFolder().replace("\\", "/");
    /* gets start of OS name. Win, Mac, Linus, SunOS or FreeBSD (some others exist, but fuck them, nobody uses anyway, right? RIGHT?) */
    private static final String OS = System.getProperty("os.name").split(" ")[0].replace("dows", "");

    @Override
    /* base method */
    public void run() {
        /* print out launch folder */
        display("launch folder: " + folder);
        /* Make sure we got the correct folder */
        if(!IsRightFolder()){
            /* error message */
            display("launch folder does not contain Downloader.jar!");
            display("Are you sure this is correct folder, and this application is not renamed?");

        } else {
        /* get program version */
            title("Checking downloader version");
            App.repaint();
            String ver = GetVersion(updateAdr + "latest.txt");

            if (!ver.equals(version)) {
            /* if program is outdated, display information */
                latest("Outdated downloader!");
                display("Current version: " + ver);
                display("Newest version: " + version);
                title("Please download an update from release threads");
                App.repaint();

            } else {
            /* update files */
                latest("Up to date!");
                title("Finding files to update");
                App.repaint();
                UpdateFiles(updateAdr + "downloads.txt");
            }
        }

        /* display exit information */
        title("Press Enter to exit");
        App.repaint();

        /* loop while ENTER key is not held */
        while (!KeyUtil.isHeld(KeyUtil.ENTER)) {
            /* make thread sleep to save processor time */
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /* DONE */
        App.exit(0);
    }

    private boolean IsRightFolder() {
        File[] files = new File(folder).listFiles();
        if(files != null) {

            for (File f : files) {
                if (f != null && f.isFile()) {
                    if (f.getName().equals("Downloader.jar")) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /* download specific file and spit out in byte array */
    public static byte[] download(String address) throws IOException {
        /* Create connection, get inputStream and create outputStream */
        URLConnection c = new URL(address).openConnection();
        InputStream in = new BufferedInputStream(c.getInputStream());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        /* declare variables and display info */
        int downloaded = 0;
        long len = GetFileSize(c);
        display("Downloaded 0 bytes out of "+ GetBytes(len));
        App.repaint();

        byte[] buf = new byte[1024];
        int n;

        /* loop while there is data to download */
        while (-1 != (n = in.read(buf))){
            /* write downloaded data to outputStream */
            out.write(buf, 0, n);
            /* set correct amount of data downloaded and display it */
            downloaded += n;
            latest("Downloaded "+ (int)((float)downloaded / len * 100) +"% ("+ GetBytes(downloaded) +"/"+ GetBytes(len) +")");
            App.repaint();
        }

        /* close streams properly and return byte array */
        out.close();
        in.close();
        return out.toByteArray();
    }

    /* gets size of a specific file */
    private static long GetFileSize(URLConnection conn) throws IOException {
        return conn.getContentLengthLong();
    }

    /* main loop for updating the files */
    private void UpdateFiles(String adr) {
        try {
            /* download descriptor file for files needing to download, and convert to array */
            String[] files = new String(download(adr)).split("\n");
            remove();
            /* initialize variables and start the loop */
            int fileID = 1;
            int total = 0;
            for(String file : files){
                /* display information */
                display("Downloading " + file.replace("%OS%", OS) +" file "+ fileID +"/"+ files.length);
                App.SetTitle(folder +"/"+ file.replace("%OS%", OS));

                /* download and save the file */
                byte[] down = download(updateAdr + file.replace("%OS%", OS));
                saveFile(folder + "/" + file.replace("%OS%", OS), down);

                /* update correct information */
                remove();
                latest("file " + file.replace("%OS%", OS) + " size " + GetBytes(down.length) +" file "+ fileID +"/"+ files.length);

                /* set variables */
                total += down.length;
                fileID ++;
                App.repaint();
            }

            /* tell download is complete */
            display("Update complete! Downloaded total of "+ files.length +" files with size of "+ GetBytes(total) +" ("+ total +" bytes)");
        } catch (IOException e) {
            /* catch errors */
            e.printStackTrace();
            display("Failed to download!");
        }
    }

    /* saves a file of the byte array */
    private void saveFile(String file, byte[] data) {
        /* make sure the directory exists */
        FileUtil.mkdirs(DiscludeFile(file));

        /* delete the file if it existed */
        if(FileUtil.exists(file)){
            FileUtil.delete(file);
        }

        /* save the new file */
        FileUtil.writeBytes(file, data, false);
    }

    /* removes filename from folder address */
    private String DiscludeFile(String file) {
        return file.substring(0, file.lastIndexOf("/"));
    }

    /* gets programs current version */
    private String GetVersion(String adr) {
        try {
            /* cheap and lazy way to obtain current version of downloader */
            return new String(download(adr)).split("\n")[0].replace("DownloaderVersion: ", "");
        } catch (IOException e) {
            e.printStackTrace();
        }
        /* error happened, return nothing */
        return "";
    }

    /* displays next text and sets program title */
    private void title(String text) {
        texts.add(texts.size(), text);
        App.SetTitle(text);
    }

    /* display text */
    private static void display(String text) {
        texts.add(texts.size(), text);
    }

    /* replace latest text */
    private static void latest(String text) {
        texts.set(texts.size() -1, text);
    }

    /* removes last text from the list */
    private void remove() {
        texts.remove(texts.size() -1);
    }

    /* translate size to Bytes/kiloBytes/megaBytes accordingly */
    private static String GetBytes(long size) {
        if(size >= 1048576){
            /* if more than 1 megaByte*/
            return String.format("%.02f", ((float)size / 1048576)) +" MegaBytes";
        }  if(size >= 1024){
            /* if more than 1 kiloByte */
            return String.format("%.02f", ((float)size / 1024)) +" KiloBytes";
        }

        /* less than 1 kilobyte */
        return size +" Bytes";
    }
}
