import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;
import cpw.mods.fml.client.FMLClientHandler;

public class WikiUtils{
    public static MovingObjectPosition getPlayerLookedObject(){
        Minecraft mc = FMLClientHandler.instance().getClient();
        double d0 = mc.playerController.getBlockReachDistance();
        return mc.renderViewEntity.rayTrace(d0, 0);
    }
}
