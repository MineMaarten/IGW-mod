package igwmod;

import igwmod.lib.IGWLog;
import igwmod.network.MessageSendServerTab;
import igwmod.network.NetworkHandler;

import java.io.File;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import cpw.mods.fml.common.event.FMLInterModComms.IMCEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.server.FMLServerHandler;

public class ServerProxy implements IProxy{

    @Override
    public void preInit(FMLPreInitializationEvent event){
        MinecraftForge.EVENT_BUS.register(this);

    }

    @SubscribeEvent
    public void onJoinWorld(EntityJoinWorldEvent event){
        if(!event.world.isRemote && event.entity instanceof EntityPlayer) {
            File serverFolder = new File(IGWMod.proxy.getSaveLocation() + File.separator + "igwmod");
            if(!serverFolder.exists()) {
                serverFolder = new File(IGWMod.proxy.getSaveLocation() + File.separator + "igwmodServer");//TODO legacy remove
                if(serverFolder.exists()) {
                    IGWLog.warning("Found IGW Mod server page in the 'igwmodServer' folder. This is deprecated! Rename the folder to 'igwmod' instead.");
                }
            }
            if(serverFolder.exists()) {
                NetworkHandler.sendTo(new MessageSendServerTab(serverFolder), (EntityPlayerMP)event.entity);
            }
        }
    }

    @Override
    public void postInit(){

    }

    @Override
    public void processIMC(IMCEvent event){}

    @Override
    public String getSaveLocation(){
        String mcDataLocation = FMLServerHandler.instance().getSavesDirectory().getAbsolutePath();
        return mcDataLocation;
    }

    @Override
    public EntityPlayer getPlayer(){
        return null;
    }

}
