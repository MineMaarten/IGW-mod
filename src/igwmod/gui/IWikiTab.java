package igwmod.gui;

import java.util.List;

import net.minecraft.item.ItemStack;

public interface IWikiTab{
    /**
     * The returned string will be displayed on the tooltip when you hover over the tab.
     * @return
     */
    public String getName();

    /**
     * Will be called by the GUI to render the tab. The render matrix will already be translated dependant on where this tab is.
     * @return When you return an ItemStack, this stack will be drawn rotating. Returning null is valid, nothing will be drawn.
     */
    public ItemStack renderTabIcon(GuiWiki gui);

    /**
     * With this you can specify which spaces in the wikipage are prohibited for text to occur. This is for example being used by the Item & Blocks
     * tab to prevent text from going through the item list. This list is also used to add standard widgets, like images that need to exist on every
     * wikipage of this tab. Just add a {@link LocatedTexture} to this list and it will be rendered.
     * @return
     */
    public List<IReservedSpace> getReservedSpaces();

    /**
     * In here you should return the full list of pages. This will also define how people will be able to navigate through pages on this tab.
     * The most simplest way is to use {@link LinkedLocatedString}.
     * @param pageIndexes : This array will be null when every existing page is requested (used for search queries). When specific pages are
     * requested (as a result of a search query), this array will contain the indexes it wants of the list returner earlier. Return a list
     * with only the elements of the indexes given. This way, you can decide where you want to put pagelinks (spacings, only vertical or in pairs
     * of two) however you want.
     * @return
     */
    public List<IPageLink> getPages(int[] pageIndexes);

    /**
     * Return the amount of page links that fit on one page (it will create a multi-page-link button if there are more pages than that).
     * @return
     */
    public int pagesPerTab();

    public int pagesPerScroll();

    public void renderForeground(GuiWiki gui, int mouseX, int mouseY);

    public void renderBackground(GuiWiki gui, int mouseX, int mouseY);

    public void onMouseClick(GuiWiki gui, int mouseX, int mouseY, int mouseKey);

    public void onPageChange(GuiWiki gui, String pageName, Object... metadata);

}
