package igwmod.api;

import igwmod.gui.GuiWiki;
import igwmod.gui.IWikiTab;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

public class WikiRegistry{

    //Not using a map here, because ItemStacks aren't comparable.
    private static List<ItemStack> itemAndBlockPageEntryKeys = new ArrayList<ItemStack>();
    private static List<String> itemAndBlockPageEntryValues = new ArrayList<String>();

    public static void registerWikiTab(IWikiTab tab){
        GuiWiki.wikiTabs.add(tab);
    }

    public static void registerBlockAndItemPageEntry(ItemStack stack){
        registerBlockAndItemPageEntry(stack, stack.getUnlocalizedName().replace("tile.", "block/").replace("item.", "item/"));
    }

    public static void registerBlockAndItemPageEntry(ItemStack stack, String page){
        itemAndBlockPageEntryKeys.add(stack);
        itemAndBlockPageEntryValues.add(page);
    }

    public static String getPageForItemStack(ItemStack stack){
        for(int i = 0; i < itemAndBlockPageEntryKeys.size(); i++) {
            if(itemAndBlockPageEntryKeys.get(i).isItemEqual(stack)) return itemAndBlockPageEntryValues.get(i);
        }
        return null;
    }

    public static List<ItemStack> getItemAndBlockPageEntries(){
        return itemAndBlockPageEntryKeys;
    }
}
