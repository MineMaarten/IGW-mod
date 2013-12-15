import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = "IGWMod", name = "In-Game Wiki Mod", version = "0.1.0")
public class IGWMod{
    @Instance("IGWMod")
    public IGWMod instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event){
        TickRegistry.registerTickHandler(new TickHandler(), Side.CLIENT);

        TickRegistry.registerTickHandler(new TooltipOverlayHandler(), Side.CLIENT);

        //Not being used, as it doesn't really add anything...
        // MinecraftForge.EVENT_BUS.register(new HighlightHandler());

        //We don't need a proxy here, because this is a client-only mod.
        KeyBindingRegistry.registerKeyBinding(new KeybindingHandler());

        ConfigHandler.init(event.getSuggestedConfigurationFile());
    }

    @EventHandler
    public void init(FMLInitializationEvent event){

    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event){

    }
}
