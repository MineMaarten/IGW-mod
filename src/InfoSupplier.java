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
            ResourceManager manager = FMLClientHandler.instance().getClient().getResourceManager();
            ResourceLocation location = infoMap.get(objectName);
            Resource resource = manager.getResource(location);
            InputStream stream = resource.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            List<String> textList = new ArrayList<String>();
            String line = br.readLine();
            while(line != null) {
                textList.add(line);
                line = br.readLine();
            }
            return textList;
        } catch(Exception e) {
            return Arrays.asList("No info available. Add " + objectName.replace("igwmod:", "/assets/igwmod/") + " to add info about this object.");
        }
    }
}
