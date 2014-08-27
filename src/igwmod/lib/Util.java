package igwmod.lib;

import java.net.URI;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import cpw.mods.fml.client.FMLClientHandler;

public class Util{
    public static Entity getEntityForClass(Class<? extends Entity> entityClass){
        try {
            return entityClass.getConstructor(World.class).newInstance(FMLClientHandler.instance().getClient().theWorld);
        } catch(Exception e) {
            IGWLog.error("[LocatedEntity.java] An entity class doesn't have a constructor with a single World parameter! Entity = " + entityClass.getName());
            e.printStackTrace();
            return null;
        }
    }

    public static void openBrowser(String url){
        try {
            Class oclass = Class.forName("java.awt.Desktop");
            Object object = oclass.getMethod("getDesktop", new Class[0]).invoke((Object)null, new Object[0]);
            oclass.getMethod("browse", new Class[]{URI.class}).invoke(object, new Object[]{new URI(url)});
        } catch(Throwable throwable) {
            IGWLog.error("Couldn\'t open link");
            throwable.printStackTrace();
        }
    }
}
