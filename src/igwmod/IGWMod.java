package igwmod;

import igwmod.api.WikiRegistry;
import igwmod.gui.BlockAndItemWikiTab;
import igwmod.gui.EntityWikiTab;
import igwmod.lib.Constants;
import igwmod.lib.Paths;
import igwmod.render.TooltipOverlayHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        //Register all basic items that have (default) pages to the item and blocks page.
        List<ItemStack> allCreativeStacks = new ArrayList<ItemStack>();
        for(Item item : Item.itemsList) {
            if(item != null) item.getSubItems(item.itemID, item.getCreativeTab(), allCreativeStacks);
        }
        for(ItemStack stack : allCreativeStacks) {
            List<String> info = InfoSupplier.getInfo(Paths.WIKI_PATH + stack.getUnlocalizedName().replace("tile.", "block/").replace("item.", "item/"));
            if(info != null) WikiRegistry.registerBlockAndItemPageEntry(stack);
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
    }
}
