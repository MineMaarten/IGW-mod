package igwmod.gui;

import igwmod.api.WikiRegistry;
import igwmod.lib.Textures;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

public class BlockAndItemWikiTab implements IWikiTab{
    private static RenderItem itemRenderer;
    private static ItemStack drawingStack;

    static {
        itemRenderer = new RenderItem();
        itemRenderer.setRenderManager(RenderManager.instance);
    }

    @Override
    public String getName(){
        return "igwmod.wikitab.blocksAndItems.name";
    }

    @Override
    public ItemStack renderTabIcon(GuiWiki gui){
        return new ItemStack(Block.grass);
    }

    @Override
    public List<IReservedSpace> getReservedSpaces(){
        List<IReservedSpace> reservedSpaces = new ArrayList<IReservedSpace>();
        reservedSpaces.add(new LocatedTexture(Textures.GUI_ITEMS_AND_BLOCKS, 40, 74, 36, 144));
        return reservedSpaces;
    }

    @Override
    public List<IPageLink> getPages(int[] indexes){
        List<ItemStack> itemStacks = WikiRegistry.getItemAndBlockPageEntries();
        List<IPageLink> pages = new ArrayList<IPageLink>();
        if(indexes == null) {
            for(int i = 0; i < itemStacks.size(); i++) {
                pages.add(new LocatedStack(itemStacks.get(i), 41 + i % 2 * 18, 75 + i / 2 * 18));
            }
        } else {
            for(int i = 0; i < indexes.length; i++) {
                pages.add(new LocatedStack(itemStacks.get(indexes[i]), 41 + i % 2 * 18, 75 + i / 2 * 18));
            }
        }
        return pages;
    }

    @Override
    public int pagesPerTab(){
        return 16;
    }

    @Override
    public int pagesPerScroll(){
        return 2;
    }

    @Override
    public int getSearchBarAndScrollStartY(){
        return 61;
    }

    @Override
    public void renderForeground(GuiWiki gui, int mouseX, int mouseY){
        if(drawingStack != null) {
            if(drawingStack.getItem() instanceof ItemBlock) {
                gui.renderRotatingBlockIntoGUI(gui, drawingStack, 55, 33, 2.8F);
            } else {
                GL11.glPushMatrix();
                GL11.glTranslated(49, 20, 0);
                GL11.glScaled(2.2, 2.2, 2.2);
                itemRenderer.renderItemAndEffectIntoGUI(gui.fontRenderer, gui.mc.getTextureManager(), drawingStack, 0, 0);
                GL11.glPopMatrix();
            }
        }
    }

    @Override
    public void renderBackground(GuiWiki gui, int mouseX, int mouseY){}

    @Override
    public void onMouseClick(GuiWiki gui, int mouseX, int mouseY, int mouseKey){}

    @Override
    public void onPageChange(GuiWiki gui, String pageName, Object... metadata){
        if(metadata.length > 0 && metadata[0] instanceof ItemStack) {
            drawingStack = (ItemStack)metadata[0];
        }
    }

}
