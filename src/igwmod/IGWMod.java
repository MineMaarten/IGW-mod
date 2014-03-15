package igwmod;

import igwmod.api.WikiRegistry;
import igwmod.gui.BlockAndItemWikiTab;
import igwmod.gui.EntityWikiTab;
import igwmod.gui.IGWWikiTab;
import igwmod.lib.Constants;
import igwmod.lib.Log;
import igwmod.lib.Paths;
import igwmod.render.TooltipOverlayHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = Constants.MOD_ID, name = "In-Game Wiki Mod", version = "1.0.1")
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

        WikiRegistry.registerWikiTab(new IGWWikiTab());
        WikiRegistry.registerWikiTab(new BlockAndItemWikiTab());
        WikiRegistry.registerWikiTab(new EntityWikiTab());

    }

    @EventHandler
    public void init(FMLInitializationEvent event){

    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event){
        addDefaultKeys();
    }

    private void addDefaultKeys(){
        //Register all basic items that have (default) pages to the item and blocks page.
        List<ItemStack> allCreativeStacks = new ArrayList<ItemStack>();
        for(Item item : Item.itemsList) {
            if(item != null) {
                try {
                    item.getSubItems(item.itemID, item.getCreativeTab(), allCreativeStacks);
                } catch(Exception e) {
                    //ForgeMultipart throws a NPE when Item#getSubItems() gets called.
                }
            }
        }
        for(ItemStack stack : allCreativeStacks) {
            List<String> info = InfoSupplier.getInfo(Paths.WIKI_PATH + WikiUtils.getNameFromStack(stack));
            if(info != null) WikiRegistry.registerBlockAndItemPageEntry(stack);
        }

        //Register all entities that have (default) pages to the entity page. (Y u no use generics Mojang?!)
        for(Map.Entry<String, Class<? extends Entity>> entry : (Set<Map.Entry<String, Class<? extends Entity>>>)EntityList.stringToClassMapping.entrySet()) {
            if(InfoSupplier.getInfo(Paths.WIKI_PATH + "entity/" + entry.getKey()) != null) WikiRegistry.registerEntityPageEntry(entry.getValue());
        }

        //Add automatically generated crafting recipe key mappings.
        for(IRecipe recipe : (List<IRecipe>)CraftingManager.getInstance().getRecipeList()) {
            if(recipe.getRecipeOutput() != null) {
                String blockCode = WikiUtils.getNameFromStack(recipe.getRecipeOutput());
                if(!WikiCommandRecipeIntegration.autoMappedRecipes.containsKey(blockCode)) WikiCommandRecipeIntegration.autoMappedRecipes.put(blockCode, recipe);
            }
        }

        //Add automatically generated furnace recipe key mappings.
        for(Map.Entry<Integer, ItemStack> entry : (Set<Map.Entry<Integer, ItemStack>>)FurnaceRecipes.smelting().getSmeltingList().entrySet()) {
            String blockCode = WikiUtils.getNameFromStack(entry.getValue());
            if(!WikiCommandRecipeIntegration.autoMappedFurnaceRecipes.containsKey(blockCode)) WikiCommandRecipeIntegration.autoMappedFurnaceRecipes.put(blockCode, new ItemStack(entry.getKey(), 1, 0));
        }
        for(Map.Entry<List<Integer>, ItemStack> entry : FurnaceRecipes.smelting().getMetaSmeltingList().entrySet()) {
            String blockCode = WikiUtils.getNameFromStack(entry.getValue());
            if(!WikiCommandRecipeIntegration.autoMappedFurnaceRecipes.containsKey(blockCode)) WikiCommandRecipeIntegration.autoMappedFurnaceRecipes.put(blockCode, new ItemStack(entry.getKey().get(0), 1, entry.getKey().get(1)));
        }

        Log.info("Registered " + WikiRegistry.getItemAndBlockPageEntries().size() + " Block & Item page entries.");
        Log.info("Registered " + WikiRegistry.getEntityPageEntries().size() + " Entity page entries.");
    }

    @EventHandler
    public void processIMCRequests(FMLInterModComms.IMCEvent event){
        List<FMLInterModComms.IMCMessage> messages = event.getMessages();
        for(FMLInterModComms.IMCMessage message : messages) {
            try {
                Class clazz = Class.forName(message.key);
                try {
                    Method method = clazz.getMethod(message.getStringValue());
                    try {
                        method.invoke(null);
                        Log.info("Successfully gave " + message.getSender() + " a nudge! Happy to be doing business!");
                    } catch(IllegalAccessException e) {
                        Log.error(message.getSender() + " tried to register to IGW. Failed because the method can NOT be accessed: " + message.getStringValue());
                    } catch(IllegalArgumentException e) {
                        Log.error(message.getSender() + " tried to register to IGW. Failed because the method has arguments or it isn't static: " + message.getStringValue());
                    } catch(InvocationTargetException e) {
                        Log.error(message.getSender() + " tried to register to IGW. Failed because the method threw an exception: " + message.getStringValue());
                        e.printStackTrace();
                    }
                } catch(NoSuchMethodException e) {
                    Log.error(message.getSender() + " tried to register to IGW. Failed because the method can NOT be found: " + message.getStringValue());
                } catch(SecurityException e) {
                    Log.error(message.getSender() + " tried to register to IGW. Failed because the method can NOT be accessed: " + message.getStringValue());
                }
            } catch(ClassNotFoundException e) {
                Log.error(message.getSender() + " tried to register to IGW. Failed because the class can NOT be found: " + message.key);
            }

        }
    }
}
