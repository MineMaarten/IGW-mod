package igwmod.nei;

import igwmod.WikiUtils;

import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import codechicken.nei.forge.IContainerTooltipHandler;

public class TooltipHandler implements IContainerTooltipHandler{

    @Override
    public List<String> handleTooltipFirst(GuiContainer gui, int mousex, int mousey, List<String> currenttip){
        return currenttip;
    }

    @Override
    public List<String> handleItemTooltip(GuiContainer gui, ItemStack itemstack, List<String> currenttip){
        if(igwmod.ConfigHandler.debugMode) currenttip.add(String.format(EnumChatFormatting.AQUA + "[IGW-Mod_DEBUG] IGW name: " + WikiUtils.getNameFromStack(itemstack)));

        return currenttip;
    }

    /** @return tooltipString */
    public String getTooltipString(){
        return null;
    }
}
