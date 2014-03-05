package igwmod.gui;

import igwmod.ConfigHandler;
import igwmod.lib.Paths;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

public class IGWWikiTab extends BaseWikiTab{

    public IGWWikiTab(){
        pageEntries.add("intro");
        if(ConfigHandler.debugMode) {
            pageEntries.add("devIntro");
            pageEntries.add("devItemAndBlock");
            pageEntries.add("devPageCommands");
            pageEntries.add("devForModders");
        }
    }

    @Override
    public String getName(){
        return "igwmod.wikitab.igwmod.name";
    }

    @Override
    public ItemStack renderTabIcon(GuiWiki gui){
        return new ItemStack(Block.wood);
    }

    @Override
    protected String getPageName(String pageEntry){
        return I18n.getString("igwtab.entry." + pageEntry);
    }

    @Override
    protected String getPageLocation(String pageEntry){
        return Paths.WIKI_PATH + "igwtab/" + pageEntry;
    }
}
