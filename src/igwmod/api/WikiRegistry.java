package igwmod.api;

import igwmod.gui.GuiWiki;
import igwmod.gui.tabs.IWikiTab;
import igwmod.gui.tabs.ServerWikiTab;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.ItemStack;

public class WikiRegistry{

    private static List<Map.Entry<String, ItemStack>> itemAndBlockPageEntries = new ArrayList<Map.Entry<String, ItemStack>>();
    private static Map<Class<? extends Entity>, String> entityPageEntries = new HashMap<Class<? extends Entity>, String>();
    public static List<IRecipeIntegrator> recipeIntegrators = new ArrayList<IRecipeIntegrator>();

    public static void registerWikiTab(IWikiTab tab){
        if(tab instanceof ServerWikiTab) {
            for(IWikiTab t : GuiWiki.wikiTabs) {
                if(t instanceof ServerWikiTab) {
                    GuiWiki.wikiTabs.remove(t);
                    break;
                }
            }
            GuiWiki.wikiTabs.add(0, tab);
        } else {
            GuiWiki.wikiTabs.add(tab);
        }
    }

    public static void registerBlockAndItemPageEntry(ItemStack stack){
        if(stack == null || stack.getItem() == null) throw new IllegalArgumentException("Can't register null items");
        registerBlockAndItemPageEntry(stack, stack.getUnlocalizedName().replace("tile.", "block/").replace("item.", "item/"));
    }

    public static void registerBlockAndItemPageEntry(ItemStack stack, String page){
        itemAndBlockPageEntries.add(new AbstractMap.SimpleEntry(page, stack));
    }

    public static void registerEntityPageEntry(Class<? extends Entity> entityClass){
        registerEntityPageEntry(entityClass, "entity/" + EntityList.classToStringMapping.get(entityClass));
    }

    public static void registerEntityPageEntry(Class<? extends Entity> entityClass, String page){
        entityPageEntries.put(entityClass, page);
    }

    public static void registerRecipeIntegrator(IRecipeIntegrator recipeIntegrator){
        recipeIntegrators.add(recipeIntegrator);
    }

    public static String getPageForItemStack(ItemStack stack){
        for(Map.Entry<String, ItemStack> entry : itemAndBlockPageEntries) {
            if(entry.getValue().isItemEqual(stack) && ItemStack.areItemStackTagsEqual(entry.getValue(), stack)) return entry.getKey();
        }
        for(Map.Entry<String, ItemStack> entry : itemAndBlockPageEntries) {
            if(entry.getValue().isItemEqual(stack)) return entry.getKey();
        }
        return null;
    }

    public static String getPageForEntityClass(Class<? extends Entity> entityClass){
        return entityPageEntries.get(entityClass);
    }

    public static List<ItemStack> getItemAndBlockPageEntries(){
        List<ItemStack> entries = new ArrayList<ItemStack>();
        for(Map.Entry<String, ItemStack> entry : itemAndBlockPageEntries) {
            entries.add(entry.getValue());
        }
        return entries;
    }

    public static List<Class<? extends Entity>> getEntityPageEntries(){
        List<Class<? extends Entity>> entries = new ArrayList<Class<? extends Entity>>();
        for(Class<? extends Entity> entityClass : entityPageEntries.keySet()) {
            entries.add(entityClass);
        }
        return entries;

    }
}
