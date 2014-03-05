package igwmod.lib;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import cpw.mods.fml.client.FMLClientHandler;

public class Util{
    public static Entity getEntityForClass(Class<? extends Entity> entityClass){
        try {
            return entityClass.getConstructor(World.class).newInstance(FMLClientHandler.instance().getClient().theWorld);
        } catch(Exception e) {
            Log.error("[LocatedEntity.java] An entity class doesn't have a constructor with a single World parameter! Entity = " + entityClass.getName());
            e.printStackTrace();
            return null;
        }
    }
}
