package igwmod;

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
            File serverFolder = new File(IGWMod.proxy.getSaveLocation() + "\\igwmodServer\\");
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
        return mcDataLocation.substring(0, mcDataLocation.length() - 2);
    }

    @Override
    public EntityPlayer getPlayer(){
        return null;
    }

}
