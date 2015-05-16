package igwmod;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLInterModComms.IMCEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;
import igwmod.api.WikiRegistry;
import igwmod.gui.tabs.BlockAndItemWikiTab;
import igwmod.gui.tabs.EntityWikiTab;
import igwmod.gui.tabs.IGWWikiTab;
import igwmod.lib.Constants;
import igwmod.lib.IGWLog;
import igwmod.lib.Paths;
import igwmod.lib.Util;
import igwmod.recipeintegration.IntegratorCraftingRecipe;
import igwmod.recipeintegration.IntegratorFurnace;
import igwmod.recipeintegration.IntegratorImage;
import igwmod.recipeintegration.IntegratorStack;
import igwmod.render.TooltipOverlayHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class ClientProxy implements IProxy{
    public static KeyBinding openInterfaceKey;

    @Override
    public void preInit(FMLPreInitializationEvent event){
        FMLCommonHandler.instance().bus().register(new TickHandler());

        FMLCommonHandler.instance().bus().register(new TooltipOverlayHandler());

        //Not being used, as it doesn't really add anything...
        // MinecraftForge.EVENT_BUS.register(new HighlightHandler());

        //We don't need a proxy here, since this is a client-only mod.

        openInterfaceKey = new KeyBinding("igwmod.keys.wiki", Constants.DEFAULT_KEYBIND_OPEN_GUI, "igwmod.keys.category");//TODO blend keybinding category in normal
        ClientRegistry.registerKeyBinding(openInterfaceKey);
        FMLCommonHandler.instance().bus().register(this);//subscribe to key events.

        ConfigHandler.init(event.getSuggestedConfigurationFile());

        WikiRegistry.registerWikiTab(new IGWWikiTab());
        WikiRegistry.registerWikiTab(new BlockAndItemWikiTab());
        WikiRegistry.registerWikiTab(new EntityWikiTab());

        WikiRegistry.registerRecipeIntegrator(new IntegratorImage());
        WikiRegistry.registerRecipeIntegrator(new IntegratorCraftingRecipe());
        WikiRegistry.registerRecipeIntegrator(new IntegratorFurnace());
        WikiRegistry.registerRecipeIntegrator(new IntegratorStack());
    }

    @SubscribeEvent
    public void onKeyBind(KeyInputEvent event){
        if(openInterfaceKey.isPressed() && FMLClientHandler.instance().getClient().inGameHasFocus) {
            TickHandler.openWikiGui();
        }
    }

    @Override
    public void postInit(){
        addDefaultKeys();
    }

    private void addDefaultKeys(){
        //Register all basic items that have (default) pages to the item and blocks page.
        List<ItemStack> allCreativeStacks = new ArrayList<ItemStack>();

        Iterator iterator = Item.itemRegistry.iterator();
        while(iterator.hasNext()) {
            Item item = (Item)iterator.next();

            if(item != null && item.getCreativeTab() != null) {
                try {
                    item.getSubItems(item, (CreativeTabs)null, allCreativeStacks);
                } catch(Throwable e) {
                    e.printStackTrace();
                }
            }
        }

        for(ItemStack stack : allCreativeStacks) {
            String modid = Paths.MOD_ID.toLowerCase();
            if(GameData.getItemRegistry().getNameForObject(stack.getItem()) != null) {
                UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor(stack.getItem());
                if(id != null && id.modId != null) modid = id.modId.toLowerCase();
                List<String> info = InfoSupplier.getInfo(modid, WikiUtils.getNameFromStack(stack), true);
                if(info != null) WikiRegistry.registerBlockAndItemPageEntry(stack);
            }
        }

        //Register all entities that have (default) pages to the entity page.
        for(Map.Entry<String, Class<? extends Entity>> entry : (Set<Map.Entry<String, Class<? extends Entity>>>)EntityList.stringToClassMapping.entrySet()) {
            String modid = Util.getModIdForEntity(entry.getValue());
            if(InfoSupplier.getInfo(modid, "entity/" + entry.getKey(), true) != null) WikiRegistry.registerEntityPageEntry(entry.getValue());
        }

        //Add automatically generated crafting recipe key mappings.
        for(IRecipe recipe : (List<IRecipe>)CraftingManager.getInstance().getRecipeList()) {
            if(recipe.getRecipeOutput() != null && recipe.getRecipeOutput().getItem() != null) {
                if(recipe.getRecipeOutput().getUnlocalizedName() == null) {
                    IGWLog.error("Item has no unlocalized name: " + recipe.getRecipeOutput().getItem());
                } else {
                    String blockCode = WikiUtils.getNameFromStack(recipe.getRecipeOutput());
                    if(!IntegratorCraftingRecipe.autoMappedRecipes.containsKey(blockCode)) IntegratorCraftingRecipe.autoMappedRecipes.put(blockCode, recipe);
                }
            }
        }

        //Add automatically generated furnace recipe key mappings.
        for(Map.Entry<ItemStack, ItemStack> entry : (Set<Map.Entry<ItemStack, ItemStack>>)FurnaceRecipes.smelting().getSmeltingList().entrySet()) {
            if(entry.getValue() != null && entry.getValue().getItem() != null) {
                String blockCode = WikiUtils.getNameFromStack(entry.getValue());
                if(!IntegratorFurnace.autoMappedFurnaceRecipes.containsKey(blockCode)) IntegratorFurnace.autoMappedFurnaceRecipes.put(blockCode, entry.getKey());
            }
        }

        IGWLog.info("Registered " + WikiRegistry.getItemAndBlockPageEntries().size() + " Block & Item page entries.");
        IGWLog.info("Registered " + WikiRegistry.getEntityPageEntries().size() + " Entity page entries.");
    }

    @Override
    public void processIMC(IMCEvent event){
        List<FMLInterModComms.IMCMessage> messages = event.getMessages();
        for(FMLInterModComms.IMCMessage message : messages) {
            try {
                Class clazz = Class.forName(message.key);
                try {
                    Method method = clazz.getMethod(message.getStringValue());
                    try {
                        method.invoke(null);
                        IGWLog.info("Successfully gave " + message.getSender() + " a nudge! Happy to be doing business!");
                    } catch(IllegalAccessException e) {
                        IGWLog.error(message.getSender() + " tried to register to IGW. Failed because the method can NOT be accessed: " + message.getStringValue());
                    } catch(IllegalArgumentException e) {
                        IGWLog.error(message.getSender() + " tried to register to IGW. Failed because the method has arguments or it isn't static: " + message.getStringValue());
                    } catch(InvocationTargetException e) {
                        IGWLog.error(message.getSender() + " tried to register to IGW. Failed because the method threw an exception: " + message.getStringValue());
                        e.printStackTrace();
                    }
                } catch(NoSuchMethodException e) {
                    IGWLog.error(message.getSender() + " tried to register to IGW. Failed because the method can NOT be found: " + message.getStringValue());
                } catch(SecurityException e) {
                    IGWLog.error(message.getSender() + " tried to register to IGW. Failed because the method can NOT be accessed: " + message.getStringValue());
                }
            } catch(ClassNotFoundException e) {
                IGWLog.error(message.getSender() + " tried to register to IGW. Failed because the class can NOT be found: " + message.key);
            }

        }
    }

    @Override
    public String getSaveLocation(){
        String mcDataLocation = Minecraft.getMinecraft().mcDataDir.getAbsolutePath();
        return mcDataLocation.substring(0, mcDataLocation.length() - 2);
    }

    @Override
    public EntityPlayer getPlayer(){
        return Minecraft.getMinecraft().thePlayer;
    }
}
