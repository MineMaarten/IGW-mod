package igwmod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class WikiUtils{
    private static HashMap<String, ItemStack> unlocMap;

    public static ItemStack getStackFromName(String name){
        if(unlocMap == null) {
            unlocMap = new HashMap<String, ItemStack>();
            List<ItemStack> stackList = new ArrayList<ItemStack>();
            for(Item item : Item.itemsList) {
                if(item != null) {
                    try {
                        item.getSubItems(item.itemID, item.getCreativeTab(), stackList);
                    } catch(Exception e) {
                        //ForgeMultipart throws a NPE when Item#getSubItems() gets called.
                    }
                }
            }
            for(ItemStack stack : stackList) {
                String itemName = stack.getUnlocalizedName().replace("tile.", "block/").replace("item.", "item/");
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
