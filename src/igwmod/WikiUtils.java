package igwmod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import cpw.mods.fml.client.FMLClientHandler;

public class WikiUtils{
    private static HashMap<String, ItemStack> unlocMap;

    public static MovingObjectPosition getPlayerLookedObject(){
        Minecraft mc = FMLClientHandler.instance().getClient();
        double d0 = mc.playerController.getBlockReachDistance();
        return mc.renderViewEntity.rayTrace(d0, 0);
    }

    public static ItemStack getStackFromName(String name){
        if(unlocMap == null) {
            unlocMap = new HashMap<String, ItemStack>();
            List<ItemStack> stackList = new ArrayList<ItemStack>();
            for(Item item : Item.itemsList) {
                if(item != null) {
                    item.getSubItems(item.itemID, item.getCreativeTab(), stackList);
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
}
