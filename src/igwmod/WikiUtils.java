package igwmod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class WikiUtils{
    private static HashMap<String, ItemStack> unlocMap;

    public static ItemStack getStackFromName(String name){
        if(unlocMap == null) {
            unlocMap = new HashMap<String, ItemStack>();
            List<ItemStack> stackList = new ArrayList<ItemStack>();

            Iterator iterator = Item.itemRegistry.iterator();
            while(iterator.hasNext()) {
                Item item = (Item)iterator.next();

                if(item != null && item.getCreativeTab() != null) {
                    item.getSubItems(item, (CreativeTabs)null, stackList);
                }
            }

            for(ItemStack stack : stackList) {
                String itemName = stack.getUnlocalizedName().replace("tile.", "block/").replace("item.", "item/");//TODO improve
                unlocMap.put(itemName, stack);
            }
        }
        String[] splitName = name.contains("#") ? name.split("#") : new String[]{name};
        ItemStack stack = unlocMap.get(splitName[0]);
        if(stack != null) {
            stack = stack.copy();
            if(splitName.length > 1) stack.stackSize = Integer.parseInt(splitName[1]);
            return stack;
        } else {
            return null;
        }
    }

    public static String getNameFromStack(ItemStack stack){
        return stack.getUnlocalizedName().replace("tile.", "block/").replace("item.", "item/");
    }
}
