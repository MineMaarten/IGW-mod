package igwmod;

import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public interface IProxy{
    public void preInit(FMLPreInitializationEvent event);

    public void postInit();

    public void processIMC(FMLInterModComms.IMCEvent event);

    public String getSaveLocation();

    public EntityPlayer getPlayer();
}
