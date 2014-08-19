package igwmod.gui.tabs;

import igwmod.gui.GuiWiki;
import igwmod.gui.IPageLink;
import igwmod.gui.IReservedSpace;
import igwmod.gui.LocatedString;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseWikiTab implements IWikiTab{
    protected List<String> pageEntries = new ArrayList<String>();

    @Override
    public List<IReservedSpace> getReservedSpaces(){
        return null;
    }

    @Override
    public List<IPageLink> getPages(int[] pageIndexes){
        List<IPageLink> pages = new ArrayList<IPageLink>();
        if(pageIndexes == null) {
            for(int i = 0; i < pageEntries.size(); i++) {
                pages.add(new LocatedString(getPageName(pageEntries.get(i)), 80, 64 + 11 * i, false, getPageLocation(pageEntries.get(i))));
            }
        } else {
            for(int i = 0; i < pageIndexes.length; i++) {
                pages.add(new LocatedString(getPageName(pageEntries.get(pageIndexes[i])), 80, 64 + 11 * i, false, getPageLocation(pageEntries.get(pageIndexes[i]))).capTextWidth(pagesPerTab() > pageIndexes.length ? 100 : 77));
            }
        }
        return pages;
    }

    @Override
    public int pagesPerTab(){
        return 36;
    }

    @Override
    public int pagesPerScroll(){
        return 1;
    }

    @Override
    public int getSearchBarAndScrollStartY(){
        return 18;
    }

    @Override
    public void renderForeground(GuiWiki gui, int mouseX, int mouseY){}

    @Override
    public void renderBackground(GuiWiki gui, int mouseX, int mouseY){}

    @Override
    public void onMouseClick(GuiWiki gui, int mouseX, int mouseY, int mouseKey){}

    @Override
    public void onPageChange(GuiWiki gui, String pageName, Object... metadata){}

    protected abstract String getPageName(String pageEntry);

    protected abstract String getPageLocation(String pageEntry);

}
