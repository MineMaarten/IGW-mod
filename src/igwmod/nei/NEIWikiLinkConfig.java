package igwmod.nei;

import igwmod.lib.Constants;
import codechicken.nei.api.IConfigureNEI;
import codechicken.nei.forge.GuiContainerManager;

public class NEIWikiLinkConfig implements IConfigureNEI{
    @Override
    public void loadConfig(){
        // API.addKeyBind("igwWiki", Keyboard.KEY_I);
        GuiContainerManager.addInputHandler(new WikiLinkInputHandler());
    }

    @Override
    public String getName(){
        return "IGW-Mod addon";
    }

    @Override
    public String getVersion(){
        return Constants.MOD_VERSION;
    }
}
