package igwmod.api;

import igwmod.gui.GuiWiki;
import igwmod.gui.IWikiTab;
import igwmod.lib.Paths;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class WikiRegistry{

    private static List<Map.Entry<String, ItemStack>> itemAndBlockPageEntries = new ArrayList<Map.Entry<String, ItemStack>>();
    private static Map<Class<? extends Entity>, String> entityPageEntries = new HashMap<Class<? extends Entity>, String>();

    public static void registerWikiTab(IWikiTab tab){
        GuiWiki.wikiTabs.add(tab);
    }

    public static void registerBlockAndItemPageEntry(ItemStack stack){
        registerBlockAndItemPageEntry(stack, Paths.WIKI_PATH + stack.getUnlocalizedName().replace("tile.", "block/").replace("item.", "item/"));
    }

    public static void registerBlockAndItemPageEntry(ItemStack stack, String page){
        itemAndBlockPageEntries.add(new AbstractMap.SimpleEntry(page, stack));
    }

    public static void registerEntityPageEntry(Class<? extends Entity> entityClass){
        registerEntityPageEntry(entityClass, Paths.WIKI_PATH + "entity/" + EntityList.classToStringMapping.get(entityClass));
    }

    public static void registerEntityPageEntry(Class<? extends Entity> entityClass, String page){
        entityPageEntries.put(entityClass, page);
    }

    public static String getPageForItemStack(ItemStack stack){
        for(Map.Entry<String, ItemStack> entry : itemAndBlockPageEntries) {
            if(entry.getValue().isItemEqual(stack) && areTagsEqual(entry.getValue(), stack)) return entry.getKey();
        }
        for(Map.Entry<String, ItemStack> entry : itemAndBlockPageEntries) {
            if(entry.getValue().isItemEqual(stack)) return entry.getKey();
        }
        return null;
    }

    private static boolean areTagsEqual(ItemStack stack1, ItemStack stack2){
        NBTTagCompound tag1 = stack1.getTagCompound();
        NBTTagCompound tag2 = stack2.getTagCompound();
        if(tag1 == null && tag2 == null) return true;
        if(tag1 != null && tag2 == null || tag1 == null && tag2 != null) return false;
        return tag1.equals(tag2);
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
