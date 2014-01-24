package igwmod;

import igwmod.api.WikiRegistry;
import igwmod.gui.BlockAndItemWikiTab;
import igwmod.gui.EntityWikiTab;
import igwmod.lib.Constants;
import igwmod.lib.Paths;
import igwmod.render.TooltipOverlayHandler;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = Constants.MOD_ID, name = "In-Game Wiki Mod", version = "0.1.0")
public class IGWMod{
    @Instance(Constants.MOD_ID)
    public IGWMod instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event){

        TickRegistry.registerTickHandler(new TickHandler(), Side.CLIENT);

        TickRegistry.registerTickHandler(new TooltipOverlayHandler(), Side.CLIENT);

        //Not being used, as it doesn't really add anything...
        // MinecraftForge.EVENT_BUS.register(new HighlightHandler());

        //We don't need a proxy here, because this is a client-only mod.
        KeyBindingRegistry.registerKeyBinding(KeybindingHandler.instance());

        ConfigHandler.init(event.getSuggestedConfigurationFile());

        MinecraftForge.EVENT_BUS.register(new RecipeEventTest());

        for(int i = 0; i < 5; i++)
            WikiRegistry.registerWikiTab(new BlockAndItemWikiTab());
        for(int i = 0; i < 2; i++)
            WikiRegistry.registerWikiTab(new EntityWikiTab());

    }

    @EventHandler
    public void init(FMLInitializationEvent event){

    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event){
        lookForItemPages();
    }

    private void lookForItemPages(){
        List<ItemStack> allCreativeStacks = new ArrayList<ItemStack>();
        for(Item item : Item.itemsList) {
            if(item != null) item.getSubItems(item.itemID, item.getCreativeTab(), allCreativeStacks);
        }
        for(ItemStack stack : allCreativeStacks) {
            List<String> info = InfoSupplier.getInfo(Paths.WIKI_PATH + stack.getUnlocalizedName().replace("tile.", "block/").replace("item.", "item/"));
            if(info != null) WikiRegistry.registerBlockAndItemPageEntry(stack);
        }
    }
}
