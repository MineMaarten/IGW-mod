package igwmod;

import igwmod.lib.Constants;
import igwmod.updater.PageDownloader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import net.minecraft.client.resources.Resource;
import net.minecraft.client.resources.ResourceManager;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.client.FMLClientHandler;

public class InfoSupplier{
    private static HashMap<String, ResourceLocation> infoMap = new HashMap<String, ResourceLocation>();

    /**
     * Returns a wikipage for an object name.
     * @param objectName
     * @return
     */
    public static List<String> getInfo(String objectName){
        objectName = objectName + ".txt";
        if(!infoMap.containsKey(objectName)) {
            infoMap.put(objectName, new ResourceLocation(objectName));
        }
        try {
            BufferedReader br;
            ResourceManager manager = FMLClientHandler.instance().getClient().getResourceManager();
            ResourceLocation location = infoMap.get(objectName);
            Resource resource = manager.getResource(location);
            InputStream stream = resource.getInputStream();
            br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            List<String> textList = new ArrayList<String>();
            String line = br.readLine();
            while(line != null) {
                textList.add(line);
                line = br.readLine();
            }
            br.close();
            return textList;
        } catch(Exception e) {
            if(ConfigHandler.shouldUseOfflineWikipages) {
                return Arrays.asList("No info available about this topic. The page was tried to be retrieved from the IGW-Mod assets folder, as \"" + Constants.USE_OFFLINE_WIKIPAGES_KEY + "\" was enabled in the configs.", "This config option is mainly used for debugging purposes, and if you are, add the wikipage for this topig in " + objectName.replace("igwmod:", "/assets/igwmod/") + " of the IGWMod zip.");
            } else if(PageDownloader.upToDate) {
                return Arrays.asList("No info available about this topic yet. When the page is added you'll automatically get updated.");
            } else {
                return Arrays.asList("No info available about this topic. This could be the cause of the following problems:", "-You don't have a proper internet connection.", "-An internal error occured while trying to download the wikipages.", "-The GitHub download is down.");
            }

        }
    }
}
