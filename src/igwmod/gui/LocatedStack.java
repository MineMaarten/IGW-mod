package igwmod.gui;

import igwmod.api.WikiRegistry;

import java.awt.Rectangle;

import net.minecraft.item.ItemStack;

public class LocatedStack implements IReservedSpace, IPageLink{
    public ItemStack stack;
    public int x, y;

    public LocatedStack(ItemStack stack, int x, int y){
        this.stack = stack;
        this.x = x;
        this.y = y;
    }

    public boolean isMouseWithinRegion(int mouseX, int mouseY){
        return getReservedSpace().contains(mouseX, mouseY);
    }

    @Override
    public Rectangle getReservedSpace(){
        return new Rectangle(x, y, (int)(16 / GuiWiki.TEXT_SCALE), (int)(16 / GuiWiki.TEXT_SCALE));
    }

    @Override
    public void onMouseClick(GuiWiki gui, int x, int y){
        if(new Rectangle(this.x, this.y, 16, 16).contains(x, y)) {
            gui.setCurrentFile(stack);
        }
    }

    @Override
    public void render(GuiWiki gui, int mouseX, int mouseY){
        /*gui.itemRenderer.renderItemAndEffectIntoGUI(gui.fontRenderer, gui.mc.getTextureManager(), stack, x, y);
        gui.itemRenderer.renderItemOverlayIntoGUI(gui.fontRenderer, gui.mc.getTextureManager(), stack, x, y, null);

        for(int j1 = 0; j1 < gui.inventorySlots.inventorySlots.size(); ++j1) {
            Slot slot = (Slot)inventorySlots.inventorySlots.get(j1);
            this.drawSlotInventory(slot);

            if(this.isMouseOverSlot(slot, par1, par2) && slot.func_111238_b() && !objectundermouse) {
                theSlot = slot;
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                int k1 = slot.xDisplayPosition;
                i1 = slot.yDisplayPosition;
                this.drawGradientRect(k1, i1, k1 + 16, i1 + 16, -2130706433, -2130706433);
                GL11.glEnable(GL11.GL_LIGHTING);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
            }
        }*/
    }

    @Override
    public String getName(){
        return stack.getDisplayName();
    }

    @Override
    public String getLinkAddress(){
        return WikiRegistry.getPageForItemStack(stack);
    }
}
