package igwmod.json;

import igwmod.InfoSupplier;
import igwmod.WikiUtils;
import igwmod.api.WikiRegistry;
import igwmod.lib.IGWLog;
import igwmod.lib.Paths;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

public class JsonRegistrator{
    /* private static JsonRegistrator INSTANCE = new JsonRegistrator();

     public static JsonRegistrator getInstance(){
         return INSTANCE;
     }*/

    public static void init(){
        for(ModContainer mod : Loader.instance().getActiveModList()) {
            try {
                InputStream jsonStream = InfoSupplier.getStreamForResource(new ResourceLocation(mod.getModId().toLowerCase() + Paths.WIKI_PATH + "pageMappings.json"));
                List<String> lines = IOUtils.readLines(jsonStream);
                String jsonString = StringUtils.join(lines, "");
                JsonObject obj = new JsonParser().parse(jsonString).getAsJsonObject();
                for(Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                    String forwardedPage = entry.getKey();
                    for(JsonElement forwardingPage : entry.getValue().getAsJsonArray()) {
                        ItemStack stack = WikiUtils.getStackFromName(forwardingPage.getAsString());
                        if(stack != null) {
                            WikiRegistry.registerBlockAndItemPageEntry(stack, forwardedPage);
                        } else {
                            IGWLog.warning("Couldn't forward the page " + forwardingPage.getAsString() + "! Not a valid item name");
                        }
                    }
                }
                IGWLog.info("Successfully loaded IGW json file for mod " + mod.getName());
            } catch(IOException e) {} catch(Exception e) {
                IGWLog.warning("IGW json failed to load for mod " + mod.getName() + "! Details:");
                e.printStackTrace();
            }
        }
    }
}
