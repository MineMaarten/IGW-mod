package igwmod.nei;

import igwmod.WikiUtils;

import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import codechicken.nei.guihook.IContainerTooltipHandler;

public class TooltipHandler implements IContainerTooltipHandler{

    @Override
    public List<String> handleTooltip(GuiContainer gui, int mousex, int mousey, List<String> currenttip){
        return currenttip;
    }

    @Override
    public List<String> handleItemDisplayName(GuiContainer gui, ItemStack itemstack, List<String> currenttip){
        return currenttip;
    }

    @Override
    public List<String> handleItemTooltip(GuiContainer gui, ItemStack itemstack, int mousex, int mousey, List<String> currenttip){
        if(igwmod.ConfigHandler.debugMode && itemstack != null) currenttip.add(String.format(EnumChatFormatting.AQUA + "IGW name: " + WikiUtils.getOwningModId(itemstack) + ":" + WikiUtils.getNameFromStack(itemstack)));
        return currenttip;
    }
}
