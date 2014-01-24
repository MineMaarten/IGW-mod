package igwmod.gui;

import igwmod.InfoSupplier;
import igwmod.TickHandler;
import igwmod.api.BlockWikiEvent;
import igwmod.api.EntityWikiEvent;
import igwmod.api.ItemWikiEvent;
import igwmod.lib.Paths;
import igwmod.lib.Textures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
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

public class GuiWiki extends InventoryEffectRenderer{
    private static String currentFile = "";
    private static List<String> fileInfo = new ArrayList<String>();

    public static List<IWikiTab> wikiTabs = new ArrayList<IWikiTab>();
    private static IWikiTab currentTab;
    private static int currentTabPage = 0;

    private static int currentWikiPagePage = 0;
    private static List<IPageLink> visibleWikiPages = new ArrayList<IPageLink>();

    private final List<LocatedStack> locatedStacks = new ArrayList<LocatedStack>();
    private final List<LocatedString> locatedStrings = new ArrayList<LocatedString>();
    private final List<LocatedTexture> locatedTextures = new ArrayList<LocatedTexture>();

    private static final ResourceLocation scrollbarTexture = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");

    /** Amount scrolled in Creative mode inventory (0 = top, 1 = bottom) */
    private static float currentScroll;

    /** True if the scrollbar is being dragged */
    private boolean isScrolling;
    private boolean wasClicking;

    private static GuiTextField searchField;

    private static final int SCROLL_X = 80;
    private static final int SCROLL_HEIGHT = 162;
    private static final int SCROLL_Y = 66;

    public static final double TEXT_SCALE = 0.5D;

    public GuiWiki(){
        super(new ContainerBlockWiki());
        allowUserInput = true;
        ySize = 238;
        xSize = 256;
        if(currentTab == null) currentTab = wikiTabs.get(0);
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
        for(LocatedStack locatedStack : locatedStacks) {
            if(locatedStack.isMouseWithinRegion(x - guiLeft, y)) {
                setCurrentFile(locatedStack.stack);
                break;
            }
        }
        List<IWikiTab> visibleTabs = getVisibleTabs();
        for(int i = 0; i < visibleTabs.size(); i++) {
            if(x <= 33 + guiLeft && x >= 1 + guiLeft && y >= 8 + guiTop + i * 35 && y <= 43 + guiTop + i * 35) {
                currentTab = visibleTabs.get(i);
                break;
            }
        }

        for(IPageLink link : visibleWikiPages) {
            link.onMouseClick(this, x, y);
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
        if(fileInfo == null) fileInfo = Arrays.asList("No info available about this topic. IGW-Mod is currently looking for " + currentFile + ".txt.");
        IWikiTab tab = getTabForPage(currentFile);
        if(tab != null) currentTab = tab;
        currentTabPage = getPageNumberForTab(currentTab);
        currentTab.onPageChange(this, file, metadata);
        updateWikiPage();
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void initGui(){
        super.initGui();
        buttonList.clear();
        Keyboard.enableRepeatEvents(true);
        searchField = new GuiTextField(fontRenderer, guiLeft + 40, guiTop + 52, 53, fontRenderer.FONT_HEIGHT);
        searchField.setMaxStringLength(15);
        searchField.setEnableBackgroundDrawing(true);
        searchField.setVisible(true);
        searchField.setFocused(true);
        searchField.setCanLoseFocus(false);
        // searchField.setTextColor(16777215);
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    @Override
    public void onGuiClosed(){
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    @Override
    protected void keyTyped(char par1, int par2){
        if(searchField.textboxKeyTyped(par1, par2)) //{
        updateSearch();
        //    } else {
        super.keyTyped(par1, par2);
        //  }
    }

    private void updateSearch(){
        List<IPageLink> pages = currentTab.getPages(null);//request all pages.
        if(pages != null) {
            List<Integer> matchingIndexes = new ArrayList<Integer>();
            for(int i = 0; i < pages.size(); i++) {
                if(pages.get(i).getName().contains(searchField.getText())) matchingIndexes.add(i);
            }
            int[] indexes = new int[matchingIndexes.size()];
            for(int i = 0; i < indexes.length; i++) {
                indexes[i] = matchingIndexes.get(i);
            }
            visibleWikiPages = currentTab.getPages(indexes);
        }
        ((ContainerBlockWiki)inventorySlots).updateStacks(locatedStacks, visibleWikiPages);
    }

    //split from above for custom search tabs
    private void updateFilteredItems(ContainerBlockWiki containercreative){
        /*  Iterator iterator = containercreative.itemList.iterator();
          String s = searchField.getText().toLowerCase();

          while(iterator.hasNext()) {
              ItemStack itemstack = (ItemStack)iterator.next();
              boolean flag = false;
              Iterator iterator1 = itemstack.getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips).iterator();

              while(true) {
                  if(iterator1.hasNext()) {
                      String s1 = (String)iterator1.next();

                      if(!s1.toLowerCase().contains(s)) {
                          continue;
                      }

                      flag = true;
                  }

                  if(!flag) {
                      iterator.remove();
                  }

                  break;
              }
          }

          currentScroll = 0.0F;
          containercreative.scrollTo(0.0F);*/
    }

    /**
     * returns (if you are not on the inventoryTab) and (the flag isn't set) and( you have more than 1 page of items)
     */
    private boolean needsScrollBars(){
        // return curSection == EnumWikiSection.BLOCK_AND_ITEM ? ((ContainerBlockWiki)inventorySlots).hasMoreThan1PageOfItemsInList() : filteredEntityList.size() > 4;
        return false;
    }

    /**
     * Handles mouse input.
     */
    @Override
    public void handleMouseInput(){
        super.handleMouseInput();
        /* int i = Mouse.getEventDWheel();

         if(i != 0 && needsScrollBars()) {
             int j = curSection == EnumWikiSection.BLOCK_AND_ITEM ? ((ContainerBlockWiki)inventorySlots).itemList.size() / 8 - 9 + 1 : filteredEntityList.size() - 4 + 1;

             if(i > 0) {
                 i = 1;
             }

             if(i < 0) {
                 i = -1;
             }

             currentScroll = (float)(currentScroll - (double)i / (double)j);

             if(currentScroll < 0.0F) {
                 currentScroll = 0.0F;
             }

             if(currentScroll > 1.0F) {
                 currentScroll = 1.0F;
             }

             if(curSection == EnumWikiSection.BLOCK_AND_ITEM) {
                 ((ContainerBlockWiki)inventorySlots).scrollTo(currentScroll);
             } else {
                 scrollEntityListTo(currentScroll);
             }
         }*/
    }

    private void scrollEntityListTo(float scroll){
        /*        int scrollPossibilities = filteredEntityList.size() - 4 + 1;
                int currentIndex = (int)(scroll * scrollPossibilities + 0.5D);

                if(currentIndex < 0) {
                    currentIndex = 0;
                }

                shownEntityList.clear();
                for(int i = currentIndex; i < currentIndex + 4 && i < filteredEntityList.size(); i++) {
                    shownEntityList.add(filteredEntityList.get(i));
                }*/
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void drawScreen(int par1, int par2, float partialTicks){

        boolean flag = Mouse.isButtonDown(0);
        int k = guiLeft;
        int l = guiTop;
        int i1 = k + SCROLL_X;
        int j1 = l + SCROLL_Y;
        int k1 = i1 + 14;
        int l1 = j1 + SCROLL_HEIGHT;

        if(!wasClicking && flag && par1 >= i1 && par2 >= j1 && par1 < k1 && par2 < l1) {
            isScrolling = needsScrollBars();
        }

        if(!flag) {
            isScrolling = false;
        }

        wasClicking = flag;

        if(isScrolling) {
            currentScroll = (par2 - j1 - 7.5F) / (l1 - j1 - 15.0F);

            if(currentScroll < 0.0F) {
                currentScroll = 0.0F;
            }

            if(currentScroll > 1.0F) {
                currentScroll = 1.0F;
            }

            //    ((ContainerBlockWiki)inventorySlots).scrollTo(currentScroll);
            scrollEntityListTo(currentScroll);
        }

        super.drawScreen(par1, par2, partialTicks);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_LIGHTING);
    }

    @Override
    protected void drawItemStackTooltip(ItemStack par1ItemStack, int par2, int par3){
        List list = par1ItemStack.getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips);
        CreativeTabs creativetabs = par1ItemStack.getItem().getCreativeTab();

        if(creativetabs == null && par1ItemStack.itemID == Item.enchantedBook.itemID) {
            Map map = EnchantmentHelper.getEnchantments(par1ItemStack);

            if(map.size() == 1) {
                Enchantment enchantment = Enchantment.enchantmentsList[((Integer)map.keySet().iterator().next()).intValue()];
                CreativeTabs[] acreativetabs = CreativeTabs.creativeTabArray;
                int k = acreativetabs.length;

                for(int l = 0; l < k; ++l) {
                    CreativeTabs creativetabs1 = acreativetabs[l];

                    if(creativetabs1.func_111226_a(enchantment.type)) {
                        creativetabs = creativetabs1;
                        break;
                    }
                }
            }
        }

        if(creativetabs != null) {
            list.add(1, "" + EnumChatFormatting.BOLD + EnumChatFormatting.BLUE + I18n.getString(creativetabs.getTranslatedTabLabel()));
        }

        for(int i1 = 0; i1 < list.size(); ++i1) {
            if(i1 == 0) {
                list.set(i1, "\u00a7" + Integer.toHexString(par1ItemStack.getRarity().rarityColor) + (String)list.get(i1));
            } else {
                list.set(i1, EnumChatFormatting.GRAY + (String)list.get(i1));
            }
        }

        func_102021_a(list, par2, par3);
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
            texture.render(null, mouseX, mouseY);
        }
        GL11.glPopMatrix();

        //Draw wiki tab images.
        List<IReservedSpace> reservedSpaces = currentTab.getReservedSpaces();
        if(reservedSpaces != null) {
            for(IReservedSpace space : reservedSpaces) {
                if(space instanceof LocatedTexture) {
                    ((LocatedTexture)space).render(null, mouseX, mouseY);
                }
            }
        }

        //render the wiki links
        for(IPageLink link : visibleWikiPages) {
            link.render(this, mouseX, mouseY);
        }

        //Draw the scroll bar.
        int i1 = SCROLL_X;
        int k = SCROLL_Y;
        int l = k + SCROLL_HEIGHT;
        mc.getTextureManager().bindTexture(scrollbarTexture);
        drawTexturedModalRect(i1, k + (int)((l - k - 17) * currentScroll), 232 + (needsScrollBars() ? 0 : 12), 0, 12, 15);

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
        InfoSupplier.analyseInfo(fontRenderer, fileInfo, reservedSpaces, locatedStrings, locatedStacks, locatedTextures);
        ((ContainerBlockWiki)inventorySlots).updateStacks(locatedStacks, visibleWikiPages);
    }

    private void drawWikiPage(int mouseX, int mouseY){

        currentTab.renderBackground(this, mouseX, mouseY);
        GL11.glPushMatrix();
        GL11.glTranslated(guiLeft, guiTop, 0);
        GL11.glScaled(TEXT_SCALE, TEXT_SCALE, 1);
        for(LocatedString locatedString : locatedStrings) {
            locatedString.render(this, mouseX, mouseY);
        }
        GL11.glPopMatrix();

    }

    /*
        private void drawSelectedStack(){
            if(drawingStack != null) {
                if(itemRenderer == null) {
                    itemRenderer = new RenderItem(){
                        @Override
                        public boolean shouldBob(){
                            return false;
                        }
                    };
                    itemRenderer.setRenderManager(RenderManager.instance);
                }
                if(renderedEntityItem == null) renderedEntityItem = new EntityItem(FMLClientHandler.instance().getClient().theWorld);
                if(renderedEntityItem.getEntityItem() != drawingStack) renderedEntityItem.setEntityItemStack(drawingStack);
                GL11.glPushMatrix();
                GL11.glTranslated(0, 0, -1);
                itemRenderer.doRenderItem(renderedEntityItem, 0, 0, 0, -TickHandler.ticksExisted, 0);
                GL11.glPopMatrix();
            }
        }*/

    /**
     * Renders the creative inventory hovering text if mouse is over it. Returns true if did render or false otherwise.
     * Params: current creative tab to be checked, current mouse x position, current mouse y position.
     */
    protected boolean renderCreativeInventoryHoveringText(CreativeTabs par1CreativeTabs, int par2, int par3){
        int k = par1CreativeTabs.getTabColumn();
        int l = 28 * k;
        byte b0 = 0;

        if(k == 5) {
            l = xSize - 28 + 2;
        } else if(k > 0) {
            l += k;
        }

        int i1;

        if(par1CreativeTabs.isTabInFirstRow()) {
            i1 = b0 - 32;
        } else {
            i1 = b0 + ySize;
        }

        if(isPointInRegion(l + 3, i1 + 3, 23, 27, par2, par3)) {
            drawCreativeTabHoveringText(I18n.getString(par1CreativeTabs.getTranslatedTabLabel()), par2, par3);
            return true;
        } else {
            return false;
        }
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
                    if(link.getLinkAddress().equals(page)) return tab;
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
