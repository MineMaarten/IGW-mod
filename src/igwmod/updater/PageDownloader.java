package igwmod.updater;

import igwmod.lib.Constants;
import igwmod.lib.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;

@Deprecated
public class PageDownloader{
    public static boolean upToDate = false;
    public static String onlinePageDir;

    public static void init(File mcDir){
        Log.info("Retrieving wikipages from " + Constants.WIKI_PAGE_LOCATION);
        String minecraftDir = mcDir.getAbsolutePath();
        minecraftDir = minecraftDir.substring(0, minecraftDir.length() - 1);
        onlinePageDir = minecraftDir + "mods/" + Constants.ZIP_NAME;
        try {
            File wikiFile = File.createTempFile(onlinePageDir + ".zip", null);
            Long startTime = System.nanoTime();
            FileUtils.copyURLToFile(new URL(Constants.WIKI_PAGE_LOCATION), wikiFile, Constants.CONNECTION_TIMEOUT, Constants.READ_TIMEOUT);
            Log.info("Succesfully retrieved the wikipages in a whopping " + (System.nanoTime() - startTime) / 1000000 + " ms! #swag");
            unZip(wikiFile, onlinePageDir);
            // addFilesToExistingZip(new File(minecraftDir + "mods/igw.jar"), new File[]{new File(onlinePageDir + ".jar")});
            upToDate = true;
        } catch(MalformedURLException e) {
            Log.error("The URL used to retrieve the wikipages seems malformed!");
            e.printStackTrace();
        } catch(IOException e) {
            Log.error("An error has occured when trying to retrieve the wikipages!");
            e.printStackTrace();
        }
    }

    //Based on http://stackoverflow.com/questions/938958/how-should-i-extract-compressed-folders-in-java
    private static void unZip(File zipFile, String location){
        Log.info("Unzipping files at " + location);
        try {
            String destinationname = location + ".jar";
            byte[] buf = new byte[1024];
            int n;
            ZipInputStream zipinputstream = new ZipInputStream(new FileInputStream(zipFile));
            File tempFile = File.createTempFile(destinationname, null);
            ZipOutputStream fileoutputstream = new ZipOutputStream(new FileOutputStream(tempFile));

            //Begin with the existent mod files.
            ZipInputStream modZip = new ZipInputStream(new FileInputStream(destinationname));
            ZipEntry modFile = modZip.getNextEntry();
            while(modFile != null) {
                Log.info("Copying over mod file: " + modFile.getName());
                fileoutputstream.putNextEntry(modFile);
                while((n = modZip.read(buf, 0, 1024)) > -1) {
                    fileoutputstream.write(buf, 0, n);
                }
                modZip.closeEntry();
                modFile = modZip.getNextEntry();
            }
            modZip.close();
            new File(destinationname).delete();

            //And add the downloaded wikipages to it.
            ZipEntry zipentry = zipinputstream.getNextEntry();
            while(zipentry != null) {
                String entryName = destinationname + File.separator + zipentry.getName();
                entryName = entryName.replace('/', File.separatorChar);
                entryName = entryName.replace('\\', File.separatorChar);
                if(entryName.contains("IGW-mod-master\\resources\\assets\\igwmod\\wiki")) {
                    Log.info("unzipping: " + entryName);
                    String target = entryName.replace("IGW-mod-master\\resources\\", "");
                    File newFile = new File(target);
                    if(zipentry.isDirectory()) {
                        if(!newFile.mkdirs()) {
                            Log.warning("not good!");
                            //break;
                        }
                        zipentry = zipinputstream.getNextEntry();
                        continue;
                    }

                    String zipNameOld = zipentry.getName();
                    String zipName = zipNameOld.replace('/', File.separatorChar).replace('\\', File.separatorChar).replace("IGW-mod-master\\resources\\", "");
                    fileoutputstream.putNextEntry(new ZipEntry(zipName));
                    while((n = zipinputstream.read(buf, 0, 1024)) > -1) {
                        fileoutputstream.write(buf, 0, n);
                    }
                }
                zipinputstream.closeEntry();
                zipentry = zipinputstream.getNextEntry();
            }

            fileoutputstream.close();
            zipinputstream.close();

            tempFile.renameTo(new File(destinationname));

            Log.info("Done unzipping!");

        } catch(IOException ex) {
            Log.error("An error has occured while trying to unzip");
            ex.printStackTrace();
        }
    }

}
