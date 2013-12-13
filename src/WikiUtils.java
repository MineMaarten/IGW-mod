import java.util.HashMap;

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
            for(Item item : Item.itemsList) {
                if(item != null) {
                    String itemName = item.getUnlocalizedName().replace("tile.", "block/").replace("item.", "item/");
                    unlocMap.put(itemName, new ItemStack(item));
                }
            }
        }
        return unlocMap.get(name);
    }
}
