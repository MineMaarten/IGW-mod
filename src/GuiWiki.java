import java.awt.Rectangle;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Derived from Vanilla's GuiContainerCreative
 */

@SideOnly(Side.CLIENT)
public class GuiWiki extends InventoryEffectRenderer{
    private static String currentFile = "";
    private static List<String> fileInfo = new ArrayList<String>();
    private static ItemStack drawingStack;
    private static EnumWikiSection curSection = EnumWikiSection.BLOCK_AND_ITEM;

    private static Entity curEntity;
    private static List<Entity> filteredEntityList = new ArrayList<Entity>();
    private static List<Entity> shownEntityList = new ArrayList<Entity>();
    private static List<Entity> totalEntityList;
    private static Entity tabEntity;

    private enum EnumWikiSection{
        BLOCK_AND_ITEM("Blocks & Items"), ENTITIES("Entities");

        public final String tabName;

        EnumWikiSection(String tabName){
            this.tabName = tabName;
        }
    }

    private final List<LocatedStack> locatedStacks = new ArrayList<LocatedStack>();
    private final List<LocatedString> locatedStrings = new ArrayList<LocatedString>();
    private final List<LocatedTexture> locatedTextures = new ArrayList<LocatedTexture>();

    private final RenderItem customItemRenderer;

    private static InventoryBasic inventory = new InventoryBasic("tmp", true, 36);
    private static final ResourceLocation field_110424_t = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");

    /** Amount scrolled in Creative mode inventory (0 = top, 1 = bottom) */
    private float currentScroll;

    /** True if the scrollbar is being dragged */
    private boolean isScrolling;

    /**
     * True if the left mouse button was held down last time drawScreen was called.
     */
    private boolean wasClicking;
    private GuiTextField searchField;

    private static final int SCROLL_X = 80;
    private static final int SCROLL_HEIGHT = 162;
    private static final int SCROLL_Y = 66;
    private static final int WRAP_LENGTH = 55;
    private static final double TEXT_SCALE = 0.5;
    public static final int TEXT_START_X = 100;
    public static final int TEXT_START_Y = 10;
    private static final int CHAR_WIDTH = 5;
    private boolean wasInBlockSection = true;

    private int currentTextureWidth = 16;
    private int currentTextureHeight = 16;
    private float currentTextureSize = 1;

    public GuiWiki(){
        super(new ContainerBlockWiki());
        EntityPlayer player = FMLClientHandler.instance().getClient().thePlayer;
        player.openContainer = inventorySlots;
        allowUserInput = true;
        ySize = 238;
        xSize = 256;

        customItemRenderer = new RenderItem();
        customItemRenderer.setRenderManager(RenderManager.instance);
        tabEntity = new EntityCreeper(player.worldObj);
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
            if(locatedStack.isMouseWithinRegion(x - 84, y)) {
                setCurrentFile(locatedStack.stack);
                break;
            }
        }
        for(int i = 0; i < EnumWikiSection.values().length; i++) {
            if(x <= 33 + guiLeft && x >= 1 + guiLeft && y >= 8 + guiTop + i * 35 && y <= 43 + guiTop + i * 35) {
                curSection = EnumWikiSection.values()[i];
                if(curSection == EnumWikiSection.BLOCK_AND_ITEM) {
                    if(drawingStack != null) setCurrentFile(drawingStack);
                    updateCreativeSearch();
                } else {
                    if(curEntity != null) setCurrentFile(curEntity);
                }
                updateWikiPage();
                break;
            }
        }
        if(curSection == EnumWikiSection.ENTITIES) {
            for(int i = 0; i < shownEntityList.size(); i++) {
                if(x >= guiLeft + 41 && x <= guiLeft + 76 && y >= guiTop + 75 + i * 36 && y <= guiTop + 110 + i * 36) {
                    setCurrentFile(shownEntityList.get(i));
                }
            }
        }

    }

    public void setCurrentFile(ItemStack stack){
        currentFile = Paths.WIKI_PATH + stack.getUnlocalizedName().replace("tile.", "block/").replace("item.", "item/");
        fileInfo = InfoSupplier.getInfo(currentFile);
        drawingStack = stack;
        curSection = EnumWikiSection.BLOCK_AND_ITEM;
        updateWikiPage();
    }

    public void setCurrentFile(Entity entity){
        currentFile = Paths.WIKI_PATH + "entity/" + EntityList.getEntityString(entity);
        fileInfo = InfoSupplier.getInfo(currentFile);
        curEntity = entity;
        curSection = EnumWikiSection.ENTITIES;
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
        updateCreativeSearch();
        updateEntitySearch();
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
        if(searchField.textboxKeyTyped(par1, par2)) {
            updateCreativeSearch();
            updateEntitySearch();
        } else {
            super.keyTyped(par1, par2);
        }
    }

    private void updateEntitySearch(){
        filteredEntityList.clear();
        Set<String> set = EntityList.stringToClassMapping.keySet();
        String textFieldText = searchField.getText().toLowerCase();
        if(totalEntityList == null) {
            totalEntityList = new ArrayList<Entity>();
            for(String key : set) {
                Class entityClass = (Class)EntityList.stringToClassMapping.get(key);
                if(EntityLivingBase.class.isAssignableFrom(entityClass) && !Modifier.isAbstract(entityClass.getModifiers())) {
                    totalEntityList.add(EntityList.createEntityByName(key, FMLClientHandler.instance().getClient().theWorld));
                }
            }
        }
        for(Entity entity : totalEntityList) {
            if(entity.getEntityName().toLowerCase().contains(textFieldText)) {
                filteredEntityList.add(entity);
            }
        }
        currentScroll = 0.0F;
        scrollEntityListTo(currentScroll);
    }

    private void updateCreativeSearch(){
        ContainerBlockWiki containercreative = (ContainerBlockWiki)inventorySlots;
        containercreative.itemList.clear();

        if(curSection == EnumWikiSection.BLOCK_AND_ITEM) {
            Item[] aitem = Item.itemsList;
            int i = aitem.length;
            int j;

            for(j = 0; j < i; ++j) {
                Item item = aitem[j];

                if(item != null && item.getCreativeTab() != null) {
                    item.getSubItems(item.itemID, (CreativeTabs)null, containercreative.itemList);
                }
            }

            Enchantment[] aenchantment = Enchantment.enchantmentsList;
            i = aenchantment.length;

            for(j = 0; j < i; ++j) {
                Enchantment enchantment = aenchantment[j];

                if(enchantment != null && enchantment.type != null) {
                    Item.enchantedBook.func_92113_a(enchantment, containercreative.itemList);
                }
            }

            updateFilteredItems(containercreative);
        }
    }

    //split from above for custom search tabs
    private void updateFilteredItems(ContainerBlockWiki containercreative){
        Iterator iterator = containercreative.itemList.iterator();
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
        containercreative.scrollTo(0.0F);
    }

    /**
     * returns (if you are not on the inventoryTab) and (the flag isn't set) and( you have more than 1 page of items)
     */
    private boolean needsScrollBars(){
        return curSection == EnumWikiSection.BLOCK_AND_ITEM ? ((ContainerBlockWiki)inventorySlots).hasMoreThan1PageOfItemsInList() : filteredEntityList.size() > 4;
    }

    /**
     * Handles mouse input.
     */
    @Override
    public void handleMouseInput(){
        super.handleMouseInput();
        int i = Mouse.getEventDWheel();

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
        }
    }

    private void scrollEntityListTo(float scroll){
        int scrollPossibilities = filteredEntityList.size() - 4 + 1;
        int currentIndex = (int)(scroll * scrollPossibilities + 0.5D);

        if(currentIndex < 0) {
            currentIndex = 0;
        }

        shownEntityList.clear();
        for(int i = currentIndex; i < currentIndex + 4 && i < filteredEntityList.size(); i++) {
            shownEntityList.add(filteredEntityList.get(i));
        }
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void drawScreen(int par1, int par2, float partialTicks){
        if(wasInBlockSection != (curSection == EnumWikiSection.BLOCK_AND_ITEM)) {
            wasInBlockSection = !wasInBlockSection;
            if(wasInBlockSection) {
                for(Slot slot : (List<Slot>)inventorySlots.inventorySlots) {
                    slot.xDisplayPosition += 500;
                }
            } else {
                for(Slot slot : (List<Slot>)inventorySlots.inventorySlots) {
                    slot.xDisplayPosition -= 500;
                }
            }
        }

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

            ((ContainerBlockWiki)inventorySlots).scrollTo(currentScroll);
            scrollEntityListTo(currentScroll);
        }
        super.drawScreen(par1, par2, partialTicks);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        if(curSection == EnumWikiSection.ENTITIES) {
            drawEntity(curEntity, guiLeft + 65, guiTop + 40, 0.7F, partialTicks);
            for(int i = 0; i < shownEntityList.size(); i++) {
                drawEntity(shownEntityList.get(i), guiLeft + 58, guiTop + 103 + i * 36, 0.5F, partialTicks);
            }
        }
        drawEntity(tabEntity, guiLeft + 18, guiTop + 71, 0.6F, partialTicks);

        GL11.glDisable(GL11.GL_LIGHTING);
    }

    private void drawEntity(Entity entity, int x, int y, float size, float partialTicks){
        if(entity != null) {
            GL11.glPushMatrix();
            GL11.glTranslated(x, y, 10);
            float maxHitboxComponent = Math.max(1, Math.max(entity.width, entity.height));
            GL11.glScaled(40 * size / maxHitboxComponent, -40 * size / maxHitboxComponent, -40 * size / maxHitboxComponent);
            //GL11.glRotated(20, 1, 0, 1);
            GL11.glRotatef(-30.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotated(TickHandler.ticksExisted + partialTicks, 0, 1, 0);
            RenderManager.instance.renderEntityWithPosYaw(entity, 0D, 0D, 0.0D, 0, partialTicks);
            GL11.glPopMatrix();
        }
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
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3){
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        // RenderHelper.enableGUIStandardItemLighting();

        mc.getTextureManager().bindTexture(curSection == EnumWikiSection.BLOCK_AND_ITEM ? Textures.GUI_BLOCK_WIKI : Textures.GUI_ENTITY_WIKI);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        searchField.drawTextBox();
        int i1 = guiLeft + SCROLL_X;
        int k = guiTop + SCROLL_Y;
        int l = k + SCROLL_HEIGHT;
        mc.getTextureManager().bindTexture(field_110424_t);
        drawTexturedModalRect(i1, k + (int)((l - k - 17) * currentScroll), 232 + (needsScrollBars() ? 0 : 12), 0, 12, 15);
        drawWikiPage();

    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2){
        super.drawGuiContainerForegroundLayer(par1, par2);
        GL11.glColor4d(1, 1, 1, 1);
        for(LocatedTexture texture : locatedTextures) {
            mc.getTextureManager().bindTexture(texture.texture);
            drawTexture(texture.x, texture.y, texture.width, texture.heigth);
        }
        GL11.glEnable(GL11.GL_LIGHTING);

        if(curSection == EnumWikiSection.BLOCK_AND_ITEM) drawSelectedStack();
        renderRotatingBlockIntoGUI(new ItemStack(Block.grass), 11, 17, 1.5F);
        for(LocatedStack locatedStack : locatedStacks) {
            itemRenderer.renderItemAndEffectIntoGUI(fontRenderer, mc.getTextureManager(), locatedStack.stack, locatedStack.x, locatedStack.y);
        }
        drawTooltips(par1, par2);
    }

    private void drawTooltips(int x, int y){
        for(LocatedStack locatedStack : locatedStacks) {
            if(locatedStack.isMouseWithinRegion(x - guiLeft, y - guiTop)) drawItemStackTooltip(locatedStack.stack, x - guiLeft, y - guiTop);
        }
        for(int i = 0; i < EnumWikiSection.values().length; i++) {
            if(x <= 33 + guiLeft && x >= 1 + guiLeft && y >= 8 + guiTop + i * 35 && y <= 43 + guiTop + i * 35) {
                drawCreativeTabHoveringText(EnumWikiSection.values()[i].tabName, x - guiLeft, y - guiTop);
            }
        }
        if(curSection == EnumWikiSection.ENTITIES) {
            for(int i = 0; i < shownEntityList.size(); i++) {
                if(x >= guiLeft + 41 && x <= guiLeft + 76 && y >= guiTop + 75 + i * 36 && y <= guiTop + 110 + i * 36) {
                    drawCreativeTabHoveringText(shownEntityList.get(i).getEntityName(), x - guiLeft, y - guiTop);
                }
            }
        }
    }

    private void drawWikiPage(){

        GL11.glPushMatrix();
        GL11.glTranslated(guiLeft + TEXT_START_X, TEXT_START_Y + guiTop, 0);
        GL11.glScaled(TEXT_SCALE, TEXT_SCALE, 1);
        for(LocatedString locatedString : locatedStrings) {
            fontRenderer.drawString(locatedString.string, locatedString.x, locatedString.y, locatedString.color, locatedString.shadow);
        }
        GL11.glPopMatrix();

    }

    public static void drawTexture(int x, int y, int width, int height){
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + height, 0, 0.0, 1.0);
        tessellator.addVertexWithUV(x + width, y + height, 0, 1.0, 1.0);
        tessellator.addVertexWithUV(x + width, y, 0, 1.0, 0.0);
        tessellator.addVertexWithUV(x, y, 0, 0.0, 0.0);
        tessellator.draw();
        // this.drawTexturedModalRect(x, y, 0, 0, 16, 16);
    }

    private void drawSelectedStack(){
        if(drawingStack != null) {
            if(drawingStack.getItem() instanceof ItemBlock) {
                renderRotatingBlockIntoGUI(drawingStack, 60, 20, 2.9F);
            } else {
                GL11.glPushMatrix();
                GL11.glTranslated(60 - 8, 20 - 8, 0);
                GL11.glScaled(2.2, 2.2, 2.2);
                itemRenderer.renderItemAndEffectIntoGUI(fontRenderer, mc.getTextureManager(), drawingStack, 0, 0);
                GL11.glPopMatrix();
            }
        }
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
    public void renderRotatingBlockIntoGUI(ItemStack stack, int x, int y, float scale){

        RenderBlocks renderBlocks = new RenderBlocks();

        Block block = Block.blocksList[stack.itemID];
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        GL11.glPushMatrix();
        GL11.glTranslatef(x - 2, y + 3, -3.0F + zLevel);
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

    private void updateWikiPage(){
        List<String> wrappedInfo = new ArrayList<String>();
        for(String line : fileInfo) {
            while(line.length() > 0) {
                int wrapLength = WRAP_LENGTH;
                for(int i = 0; i < line.length(); i++) {
                    if(line.charAt(i) == '[') {
                        for(int j = i; j < line.length(); j++) {
                            if(line.charAt(j) == ']') {
                                wrapLength += j - i + 1;
                                wrapLength -= decomposeCode(line.substring(i + 1, j), 0, 0).width / CHAR_WIDTH;
                                break;
                            }
                        }
                    }
                }
                String[] seperatedStrings = WordUtils.wrap(line, wrapLength).split(System.getProperty("line.separator"));
                wrappedInfo.add(seperatedStrings[0]);
                line = "";
                for(int i = 1; i < seperatedStrings.length; i++) {
                    line = line + (i > 1 ? " " : "") + seperatedStrings[i];
                }
            }
        }
        locatedStacks.clear();
        locatedStrings.clear();
        locatedTextures.clear();
        int yOffset = 0;
        for(int i = 0; i < wrappedInfo.size(); i++) {
            int posY = i * fontRenderer.FONT_HEIGHT + 1;
            int firstIndex = locatedStrings.size();
            int off = updateWikiString(wrappedInfo.get(i), 0, posY + yOffset, 0xFF000000, false);
            for(int j = firstIndex; j < locatedStrings.size(); j++) {
                LocatedString locatedString = locatedStrings.get(j);
                locatedString.y += off / 2;
            }
            yOffset += off;
        }
    }

    private int updateWikiString(String line, int posX, int posY, int color, boolean shadow){
        String[] splitLine = line.split("\\[", 2);
        locatedStrings.add(new LocatedString(splitLine[0], posX, posY, color, shadow));
        if(splitLine.length > 1) {
            String[] remainder = splitLine[1].split("\\]", 2);
            int imageX = posX + fontRenderer.getStringWidth(splitLine[0]) + 2;
            Rectangle rect = decomposeCode(remainder[0], imageX, posY);
            if(remainder.length > 1) {
                return Math.max(rect.height - fontRenderer.FONT_HEIGHT, updateWikiString(remainder[1], imageX + rect.width + 2, posY, color, shadow));
            } else {
                updateWikiString("ERROR: No closing ']' found!", imageX + rect.width + 2, posY, 0xFF0000FF, false);
            }
        }
        return 0;
    }

    private Rectangle decomposeCode(String code, int x, int y){
        if(code.startsWith("block/")) {
            ItemStack stack = WikiUtils.getStackFromName(code);
            if(stack != null) {
                locatedStacks.add(new LocatedStack(stack, TEXT_START_X + x / 2, TEXT_START_Y + y / 2));
            }
            return new Rectangle(36, 36);
        } else if(code.startsWith("texture/")) {
            locatedTextures.add(new LocatedTexture(TextureSupplier.getTexture(Paths.WIKI_PATH + code), TEXT_START_X + x / 2, TEXT_START_Y + y / 2, currentTextureWidth, currentTextureHeight, currentTextureSize));
            return new Rectangle((int)(currentTextureWidth * 2 * currentTextureSize), (int)(currentTextureHeight * 2 * currentTextureSize));
        } else if(code.startsWith("tsize=")) {
            try {
                currentTextureSize = Float.parseFloat(code.substring(6));
            } catch(Exception e) {}
        } else if(code.startsWith("theight=")) {
            try {
                currentTextureHeight = Integer.parseInt(code.substring(8));
            } catch(Exception e) {}
        } else if(code.startsWith("twidth=")) {
            try {
                currentTextureWidth = Integer.parseInt(code.substring(7));
            } catch(Exception e) {}
        } else if(code.startsWith("shaped")) {
            WikiCommandRecipeIntegration.addShapedRecipe(code.substring(6), locatedStacks, locatedTextures, x, y);
            return new Rectangle(100, 60);
        } else if(code.startsWith("furnace")) {
            WikiCommandRecipeIntegration.addFurnaceRecipe(code.substring(7), locatedStacks, locatedTextures, x, y);
            return new Rectangle(82, 54);
        }
        return new Rectangle(-4, 0);
    }

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

    /**
     * Returns the creative inventory
     */
    static InventoryBasic getInventory(){
        return inventory;
    }
}
