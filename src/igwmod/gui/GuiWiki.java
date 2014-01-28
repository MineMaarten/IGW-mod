package igwmod.gui;

import igwmod.InfoSupplier;
import igwmod.TickHandler;
import igwmod.api.BlockWikiEvent;
import igwmod.api.EntityWikiEvent;
import igwmod.api.ItemWikiEvent;
import igwmod.lib.Paths;
import igwmod.lib.Textures;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;

/**
 * Derived from Vanilla's GuiContainerCreative
 */

public class GuiWiki extends GuiContainer{
    private static String currentFile = ""; //path (ResourceLocation) of the current wikipage
    private static List<String> fileInfo = new ArrayList<String>(); //The raw info directly retrieved from the .txt file.

    public static List<IWikiTab> wikiTabs = new ArrayList<IWikiTab>();//A list of all the tabs registered.
    private static IWikiTab currentTab;
    private static int currentTabPage = 0;

    private static int currentWikiPagePage = 0;
    private static List<IPageLink> visibleWikiPages = new ArrayList<IPageLink>();
    private static int matchingWikiPages;

    private static final List<LocatedStack> locatedStacks = new ArrayList<LocatedStack>();
    private static final List<LocatedString> locatedStrings = new ArrayList<LocatedString>();
    private static final List<LocatedTexture> locatedTextures = new ArrayList<LocatedTexture>();

    private static final ResourceLocation scrollbarTexture = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");

    private static float currentPageLinkScroll;
    private static float currentPageScroll;
    private static int currentPageTranslation;

    /** True if the scrollbar is being dragged */
    private boolean isScrollingPageLink;
    private boolean isScrollingPage;
    private boolean wasClicking;
    private int lastMouseX;

    private static GuiTextField searchField;

    private static final int PAGE_LINK_SCROLL_X = 80;
    private static final int PAGE_LINK_SCROLL_HEIGHT = 214;
    private static final int PAGE_LINK_SCROLL_Y = 14;

    private static final int PAGE_SCROLL_X = 240;
    private static final int PAGE_SCROLL_HEIGHT = 230;
    private static final int PAGE_SCROLL_Y = 4;

    public static final double TEXT_SCALE = 0.5D;

    public static final int MAX_TEXT_Y = 453;
    public static final int MIN_TEXT_Y = 10;

    public GuiWiki(){
        super(new ContainerBlockWiki());
        allowUserInput = true;
        ySize = 238;
        xSize = 256;
        if(currentTab == null) currentTab = wikiTabs.get(0);
    }

    @Override
    public void initGui(){
        super.initGui();
        buttonList.clear();
        Keyboard.enableRepeatEvents(true);

        String lastSearch = "";
        if(searchField != null) lastSearch = searchField.getText();
        searchField = new GuiTextField(fontRenderer, guiLeft + 40, guiTop + currentTab.getSearchBarAndScrollStartY(), 53, fontRenderer.FONT_HEIGHT);
        searchField.setMaxStringLength(15);
        searchField.setEnableBackgroundDrawing(true);
        searchField.setVisible(true);
        searchField.setFocused(false);
        searchField.setCanLoseFocus(true);
        searchField.setText(lastSearch);
        updateSearch();
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    @Override
    public void onGuiClosed(){
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void handleMouseClick(Slot slot, int x, int y, int mouse){
        if(slot != null && slot.getHasStack()) {
            setCurrentFile(slot.getStack());
        }
    }

    @Override
    protected void mouseClicked(int x, int y, int button){
        super.mouseClicked(x, y, button);
        searchField.mouseClicked(x, y, button);

        List<IWikiTab> visibleTabs = getVisibleTabs();
        for(int i = 0; i < visibleTabs.size(); i++) {
            if(x <= 33 + guiLeft && x >= 1 + guiLeft && y >= 8 + guiTop + i * 35 && y <= 43 + guiTop + i * 35) {
                currentTab = visibleTabs.get(i);
                updateSearch();
                initGui();//update the textfield location.
                break;
            }
        }

        for(IPageLink link : visibleWikiPages) {
            if(link.onMouseClick(this, -guiLeft + x, -guiTop + y)) return;
        }
        for(LocatedString link : locatedStrings) {
            if(link.onMouseClick(this, -guiLeft + x, -guiTop + y)) return;
        }

        if(hasMultipleTabPages() && x < 33 + guiLeft && x >= 1 + guiLeft && y >= 214 + guiTop && y <= 236 + guiTop) {
            if(button == 0) {
                if(++currentTabPage >= getTotalTabPages()) currentTabPage = 0;
            } else if(button == 1) {
                if(--currentTabPage < 0) currentTabPage = getTotalTabPages() - 1;
            }
        }
    }

    public void setCurrentFile(World world, int x, int y, int z){
        BlockWikiEvent wikiEvent = new BlockWikiEvent(world, x, y, z);
        wikiEvent.pageOpened = Paths.WIKI_PATH + wikiEvent.drawnStack.getUnlocalizedName().replace("tile.", "block/").replace("item.", "item/");
        MinecraftForge.EVENT_BUS.post(wikiEvent);
        setCurrentFile(wikiEvent.pageOpened, wikiEvent.drawnStack);
    }

    public void setCurrentFile(Entity entity){
        EntityWikiEvent wikiEvent = new EntityWikiEvent(entity);
        wikiEvent.pageOpened = Paths.WIKI_PATH + "entity/" + EntityList.getEntityString(entity);
        MinecraftForge.EVENT_BUS.post(wikiEvent);
        setCurrentFile(wikiEvent.pageOpened, entity);
    }

    public void setCurrentFile(ItemStack stack){
        ItemWikiEvent wikiEvent = new ItemWikiEvent(stack, Paths.WIKI_PATH + stack.getUnlocalizedName().replace("tile.", "block/").replace("item.", "item/"));
        MinecraftForge.EVENT_BUS.post(wikiEvent);
        setCurrentFile(wikiEvent.pageOpened, stack);
    }

    public void setCurrentFile(String file, Object... metadata){
        currentFile = file;
        fileInfo = InfoSupplier.getInfo(currentFile);
        if(fileInfo == null) fileInfo = Arrays.asList("No info available about this topic. IGW-Mod is currently looking for " + currentFile.replace("igwmod:", "igwmod/assets/") + ".txt.");
        IWikiTab tab = getTabForPage(currentFile);
        if(tab != null) currentTab = tab;
        currentTabPage = getPageNumberForTab(currentTab);
        currentTab.onPageChange(this, file, metadata);
        updateWikiPage();
    }

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    @Override
    protected void keyTyped(char par1, int par2){
        if(searchField.textboxKeyTyped(par1, par2)) {
            currentPageLinkScroll = 0;
            updateSearch();
        } else {
            super.keyTyped(par1, par2);
        }
    }

    private void updateSearch(){
        List<IPageLink> pages = currentTab.getPages(null);//request all pages.

        if(pages != null) {
            List<Integer> matchingIndexes = new ArrayList<Integer>();
            for(int i = 0; i < pages.size(); i++) {
                if(pages.get(i).getName().toLowerCase().contains(searchField.getText().toLowerCase())) {
                    matchingIndexes.add(i);
                }
            }
            matchingWikiPages = matchingIndexes.size();
            int firstListedPageIndex = (int)(getScrollStates() * currentPageLinkScroll + 0.5F) * currentTab.pagesPerScroll();
            int[] indexes = new int[Math.min(Math.min(matchingIndexes.size() - firstListedPageIndex, matchingIndexes.size()), currentTab.pagesPerTab())];
            for(int i = 0; i < indexes.length; i++) {
                indexes[i] = matchingIndexes.get(firstListedPageIndex + i);
            }
            visibleWikiPages = currentTab.getPages(indexes);
        } else {
            visibleWikiPages = new ArrayList<IPageLink>();
            matchingWikiPages = 0;
        }
        ((ContainerBlockWiki)inventorySlots).updateStacks(locatedStacks, visibleWikiPages);
    }

    private void updatePageScrolling(){
        int translation = -(int)(currentPageScroll * getMaxPageTranslation() / 2 + 0.5F) * 2 - currentPageTranslation;
        currentPageTranslation += translation;
        for(LocatedStack stack : locatedStacks) {
            stack.setY(stack.getY() + translation / 2);
        }
        for(LocatedString string : locatedStrings) {
            string.setY(string.getY() + translation);
        }
        for(LocatedTexture image : locatedTextures) {
            image.setY(image.getY() + translation);
        }
        ((ContainerBlockWiki)inventorySlots).updateStacks(locatedStacks, visibleWikiPages);
    }

    private int getMaxPageTranslation(){
        int maxTranslation = -100000;
        for(LocatedTexture texture : locatedTextures) {
            maxTranslation = Math.max(maxTranslation, texture.y + texture.heigth);
        }
        for(LocatedString string : locatedStrings) {
            maxTranslation = Math.max(maxTranslation, string.getY() + fontRenderer.FONT_HEIGHT);
        }
        return Math.max(maxTranslation - currentPageTranslation - MAX_TEXT_Y, 0);
    }

    private boolean needsPageLinkScrollBars(){
        return matchingWikiPages > currentTab.pagesPerTab();
    }

    private boolean needsPageScrollBars(){
        return getMaxPageTranslation() > 0;
    }

    @Override
    public void handleMouseInput(){
        super.handleMouseInput();
        int i = Mouse.getEventDWheel();

        if(i != 0) {
            if(i > 0) {
                i = 1;
            }

            if(i < 0) {
                i = -1;
            }

            if(lastMouseX < PAGE_LINK_SCROLL_X + guiLeft + 14) {
                if(needsPageLinkScrollBars()) {
                    int j = getScrollStates();

                    currentPageLinkScroll = (float)(currentPageLinkScroll - (double)i / (double)j);

                    if(currentPageLinkScroll < 0.0F) {
                        currentPageLinkScroll = 0.0F;
                    }

                    if(currentPageLinkScroll > 1.0F) {
                        currentPageLinkScroll = 1.0F;
                    }
                    updateSearch();
                }
            } else {
                if(needsPageScrollBars()) {
                    int maxTranslation = getMaxPageTranslation();
                    currentPageScroll -= (float)i / maxTranslation * 40;
                    if(currentPageScroll > 1F) currentPageScroll = 1F;
                    else if(currentPageScroll < 0F) currentPageScroll = 0F;
                    updatePageScrolling();
                }
            }
        }
    }

    private int getScrollStates(){
        return (1 + matchingWikiPages - currentTab.pagesPerTab()) / currentTab.pagesPerScroll();
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        lastMouseX = mouseX;
        boolean leftClicking = Mouse.isButtonDown(0);
        int pageLinkScrollX1 = guiLeft + PAGE_LINK_SCROLL_X;
        int pageLinkScrollY1 = guiTop + PAGE_LINK_SCROLL_Y + currentTab.getSearchBarAndScrollStartY();
        int pageLinkScrollX2 = pageLinkScrollX1 + 14;
        int pageLinkScrollY2 = pageLinkScrollY1 + PAGE_LINK_SCROLL_HEIGHT - currentTab.getSearchBarAndScrollStartY();

        int pageScrollX1 = guiLeft + PAGE_SCROLL_X;
        int pageScrollY1 = guiTop + PAGE_SCROLL_Y;
        int pageScrollX2 = pageScrollX1 + 14;
        int pageScrollY2 = pageScrollY1 + PAGE_SCROLL_HEIGHT;

        if(!wasClicking && leftClicking) {
            if(mouseX >= pageLinkScrollX1 && mouseY >= pageLinkScrollY1 && mouseX < pageLinkScrollX2 && mouseY < pageLinkScrollY2) {
                isScrollingPageLink = needsPageLinkScrollBars();
            } else if(mouseX >= pageScrollX1 && mouseY >= pageScrollY1 && mouseX < pageScrollX2 && mouseY < pageScrollY2) {
                isScrollingPage = needsPageScrollBars();
            }
        }

        if(!leftClicking) {
            isScrollingPageLink = false;
            isScrollingPage = false;
        }

        wasClicking = leftClicking;

        if(isScrollingPageLink) {
            currentPageLinkScroll = (mouseY - pageLinkScrollY1 - 7.5F) / (pageLinkScrollY2 - pageLinkScrollY1 - 15.0F);

            if(currentPageLinkScroll < 0.0F) {
                currentPageLinkScroll = 0.0F;
            }

            if(currentPageLinkScroll > 1.0F) {
                currentPageLinkScroll = 1.0F;
            }
            updateSearch();
        } else if(isScrollingPage) {
            currentPageScroll = (mouseY - pageScrollY1 - 7.5F) / (pageScrollY2 - pageScrollY1 - 15.0F);

            if(currentPageScroll < 0.0F) {
                currentPageScroll = 0.0F;
            }

            if(currentPageScroll > 1.0F) {
                currentPageScroll = 1.0F;
            }
            updatePageScrolling();
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_LIGHTING);
    }

    /**
     * Draw the background layer for the GuiContainer (everything behind the items)
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int mouseX, int mouseY){
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        // RenderHelper.enableGUIStandardItemLighting();

        //Draw the wiki tabs.
        mc.getTextureManager().bindTexture(Textures.GUI_WIKI);
        drawTexturedModalRect(guiLeft + 33, guiTop, 33, 0, xSize - 33, ySize);
        List<IWikiTab> visibleTabs = getVisibleTabs();
        for(int i = 0; i < visibleTabs.size(); i++) {
            drawTexturedModalRect(guiLeft, guiTop + 4 + i * 35, 0, currentTab == visibleTabs.get(i) ? 0 : 35, 33, 35);
        }

        //Draw the change tabpage tab.
        if(hasMultipleTabPages()) {
            drawTexturedModalRect(guiLeft, guiTop + 214, 0, 70, 33, 22);
        }

        //draw the pagelink scrollbar
        drawTexturedModalRect(guiLeft + PAGE_LINK_SCROLL_X - 1, guiTop + PAGE_LINK_SCROLL_Y + currentTab.getSearchBarAndScrollStartY() - 1, PAGE_SCROLL_X - 1, PAGE_SCROLL_Y - 1, 14, PAGE_LINK_SCROLL_HEIGHT - currentTab.getSearchBarAndScrollStartY() - 1);
        drawTexturedModalRect(guiLeft + PAGE_LINK_SCROLL_X - 1, guiTop + PAGE_LINK_SCROLL_Y + PAGE_LINK_SCROLL_HEIGHT - 2, PAGE_SCROLL_X - 1, PAGE_SCROLL_Y + PAGE_SCROLL_HEIGHT - 2, 14, 1);

        //Draw the text field.
        searchField.drawTextBox();

        drawWikiPage(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY){
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        // Draw the wiki page images.
        GL11.glColor4d(1, 1, 1, 1);
        GL11.glPushMatrix();
        GL11.glScaled(TEXT_SCALE, TEXT_SCALE, 1);
        for(LocatedTexture texture : locatedTextures) {
            texture.render(this, mouseX, mouseY);
        }

        GL11.glPopMatrix();

        //Draw wiki tab images.
        List<IReservedSpace> reservedSpaces = currentTab.getReservedSpaces();
        if(reservedSpaces != null) {
            for(IReservedSpace space : reservedSpaces) {
                if(space instanceof LocatedTexture) {
                    ((LocatedTexture)space).render(this, mouseX, mouseY);
                }
            }
        }

        //render the wiki links
        for(IPageLink link : visibleWikiPages) {
            link.render(this, mouseX, mouseY);
        }

        GL11.glColor4d(1, 1, 1, 1);
        //Draw the scroll bar widgets.
        mc.getTextureManager().bindTexture(scrollbarTexture);
        drawTexturedModalRect(PAGE_LINK_SCROLL_X, PAGE_LINK_SCROLL_Y + currentTab.getSearchBarAndScrollStartY() + (int)((PAGE_LINK_SCROLL_HEIGHT - currentTab.getSearchBarAndScrollStartY() - 17) * currentPageLinkScroll), 232 + (needsPageLinkScrollBars() ? 0 : 12), 0, 12, 15);
        drawTexturedModalRect(PAGE_SCROLL_X, PAGE_SCROLL_Y + (int)((PAGE_SCROLL_Y + PAGE_SCROLL_HEIGHT - PAGE_SCROLL_Y - 17) * currentPageScroll), 232 + (needsPageScrollBars() ? 0 : 12), 0, 12, 15);

        GL11.glEnable(GL11.GL_LIGHTING);

        GL11.glPushMatrix();
        GL11.glTranslated(0, 4, 0);
        List<IWikiTab> visibleTabs = getVisibleTabs();
        for(IWikiTab tab : visibleTabs) {
            ItemStack drawingStack = tab.renderTabIcon(this);
            if(drawingStack != null) {
                renderRotatingBlockIntoGUI(this, drawingStack, 11, 10, 1.5F);
            }
            GL11.glTranslated(0, 35, 0);
        }
        GL11.glPopMatrix();

        //draw the tab page browse text if necessary
        if(hasMultipleTabPages()) {
            fontRenderer.drawString(currentTabPage + 1 + "/" + getTotalTabPages(), 10, 221, 0xFF000000);
        }

        //Draw the wiki page stacks.
        for(LocatedStack locatedStack : locatedStacks) {
            locatedStack.render(this, mouseX, mouseY);
        }

        //draw the wikipage
        currentTab.renderForeground(this, mouseX, mouseY);

        drawTooltips(mouseX, mouseY);

        //GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        //  RenderHelper.disableStandardItemLighting();
        //   GL11.glDisable(GL11.GL_LIGHTING);
        //GL11.glDisable(GL11.GL_DEPTH_TEST);
    }

    private void drawTooltips(int x, int y){
        List<IWikiTab> visibleTabs = getVisibleTabs();
        for(int i = 0; i < visibleTabs.size(); i++) {
            if(x <= 33 + guiLeft && x >= 1 + guiLeft && y >= 4 + guiTop + i * 35 && y <= 39 + guiTop + i * 35) {
                drawCreativeTabHoveringText(I18n.getString(visibleTabs.get(i).getName()), x - guiLeft, y - guiTop);
            }
        }
        if(hasMultipleTabPages() && x < 33 + guiLeft && x >= 1 + guiLeft && y >= 214 + guiTop && y <= 236 + guiTop) {
            func_102021_a(Arrays.asList(new String[]{I18n.getString("igwmod.tooltip.tabPageBrowse.next"), I18n.getString("igwmod.tooltip.tabPageBrowse.previous")}), x - guiLeft, y - guiTop);
        }
        /*
        if(curSection == EnumWikiSection.ENTITIES) {
            for(int i = 0; i < shownEntityList.size(); i++) {
                if(x >= guiLeft + 41 && x <= guiLeft + 76 && y >= guiTop + 75 + i * 36 && y <= guiTop + 110 + i * 36) {
                    drawCreativeTabHoveringText(shownEntityList.get(i).getEntityName(), x - guiLeft, y - guiTop);
                }
            }
        }*/
    }

    private void updateWikiPage(){
        List<IReservedSpace> reservedSpaces = currentTab.getReservedSpaces();
        if(reservedSpaces == null) reservedSpaces = new ArrayList<IReservedSpace>();
        reservedSpaces.add(new ReservedSpace(new Rectangle(0, 0, 200, Integer.MAX_VALUE)));
        InfoSupplier.analyseInfo(fontRenderer, fileInfo, reservedSpaces, locatedStrings, locatedStacks, locatedTextures);
        ((ContainerBlockWiki)inventorySlots).updateStacks(locatedStacks, visibleWikiPages);
        currentPageTranslation = 0;
        currentPageScroll = 0;
    }

    private void drawWikiPage(int mouseX, int mouseY){

        currentTab.renderBackground(this, mouseX, mouseY);
        GL11.glPushMatrix();
        GL11.glTranslated(guiLeft, guiTop, 0);
        for(LocatedString locatedString : locatedStrings) {
            if(locatedString.getY() > MIN_TEXT_Y && locatedString.getReservedSpace().height + locatedString.getY() <= MAX_TEXT_Y) {
                locatedString.render(this, mouseX, mouseY);
            }
        }
        GL11.glPopMatrix();

    }

    private List<IWikiTab> getVisibleTabs(){
        List<IWikiTab> tabs = new ArrayList<IWikiTab>();
        for(int i = currentTabPage * 6; i < currentTabPage * 6 + 6 && i < wikiTabs.size(); i++) {
            tabs.add(wikiTabs.get(i));
        }
        return tabs;
    }

    private IWikiTab getTabForPage(String page){
        for(IWikiTab tab : wikiTabs) {
            List<IPageLink> links = tab.getPages(null);
            if(links != null) {
                for(IPageLink link : links) {
                    if(page.equals(link.getLinkAddress())) return tab;
                }
            }
        }
        return null;
    }

    private int getPageNumberForTab(IWikiTab tab){
        int index = wikiTabs.indexOf(tab);
        if(index == -1) {
            return 0;
        } else {
            return index / 6;
        }
    }

    private boolean hasMultipleTabPages(){
        return wikiTabs.size() > 6;
    }

    private int getTotalTabPages(){
        return wikiTabs.size() / 6 + 1;
    }

    /**
     * This method was copied from Equivalent Exchange 3's RenderUtils.java class, https://github.com/pahimar/Equivalent-Exchange-3/blob/master/src/main/java/com/pahimar/ee3/client/renderer/RenderUtils.java
     * @param fontRenderer
     * @param stack
     * @param x
     * @param y
     * @param zLevel
     * @param scale
     */
    public void renderRotatingBlockIntoGUI(GuiWiki gui, ItemStack stack, int x, int y, float scale){

        RenderBlocks renderBlocks = new RenderBlocks();

        Block block = Block.blocksList[stack.itemID];
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        GL11.glPushMatrix();
        GL11.glTranslatef(x - 2, y + 3, -3.0F + gui.zLevel);
        GL11.glScalef(10.0F, 10.0F, 10.0F);
        GL11.glTranslatef(1.0F, 0.5F, 1.0F);
        GL11.glScalef(1.0F * scale, 1.0F * scale, -1.0F);
        GL11.glRotatef(210.0F, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(-TickHandler.ticksExisted, 0.0F, 1.0F, 0.0F);

        int var10 = Item.itemsList[stack.itemID].getColorFromItemStack(stack, 0);
        float var16 = (var10 >> 16 & 255) / 255.0F;
        float var12 = (var10 >> 8 & 255) / 255.0F;
        float var13 = (var10 & 255) / 255.0F;

        GL11.glColor4f(var16, var12, var13, 1.0F);

        GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
        renderBlocks.useInventoryTint = true;
        renderBlocks.renderBlockAsItem(block, stack.getItemDamage(), 1.0F);
        renderBlocks.useInventoryTint = true;
        GL11.glPopMatrix();
    }

}
