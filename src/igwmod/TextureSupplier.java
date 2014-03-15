package igwmod;

import igwmod.lib.Log;

import java.util.HashMap;

import net.minecraft.util.ResourceLocation;

public class TextureSupplier{
    private static HashMap<String, ResourceLocation> textureMap = new HashMap<String, ResourceLocation>();

    public static ResourceLocation getTexture(String objectName){
        if(!textureMap.containsKey(objectName)) {
            textureMap.put(objectName, new ResourceLocation(objectName));
            if(!objectName.startsWith("igwmod:")) {
                Log.warning("Although this works in a dev environment, testing showed it doesn't when obfuscated. Move the images over to assets/igwmod... and use 'igwmod:...' instead of: " + objectName + ".");
            }
        }
        return textureMap.get(objectName);
    }

}
