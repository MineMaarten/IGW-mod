package igwmod.nei;

import igwmod.ClientProxy;
import igwmod.gui.GuiWiki;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.guihook.IContainerInputHandler;
import cpw.mods.fml.common.FMLCommonHandler;

public class IGWInputHandler implements IContainerInputHandler{

    @Override
    public boolean keyTyped(GuiContainer gui, char keyChar, int keyCode){
        return false;
    }

    @Override
    public void onKeyTyped(GuiContainer gui, char keyChar, int keyID){}

    @Override
    public boolean lastKeyTyped(GuiContainer gui, char keyChar, int keyCode){
        if(ClientProxy.openInterfaceKey.getKeyCode() == keyCode) {
            GuiContainerManager.getManager(gui);
            ItemStack hoveredStack = GuiContainerManager.getStackMouseOver(gui);
            if(hoveredStack != null) {
                hoveredStack = hoveredStack.copy();
                hoveredStack.stackSize = 1;
                GuiWiki guiWiki = new GuiWiki();
                FMLCommonHandler.instance().showGuiScreen(guiWiki);
                guiWiki.setCurrentFile(hoveredStack);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(GuiContainer gui, int mousex, int mousey, int button){
        return false;
    }

    @Override
    public void onMouseClicked(GuiContainer gui, int mousex, int mousey, int button){}

    @Override
    public void onMouseUp(GuiContainer gui, int mousex, int mousey, int button){}

    @Override
    public boolean mouseScrolled(GuiContainer gui, int mousex, int mousey, int scrolled){
        return false;
    }

    @Override
    public void onMouseScrolled(GuiContainer gui, int mousex, int mousey, int scrolled){}

    @Override
    public void onMouseDragged(GuiContainer gui, int mousex, int mousey, int button, long heldTime){}
}
