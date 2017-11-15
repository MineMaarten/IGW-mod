package igwmod;

import igwmod.api.IWikiHooks;
import igwmod.gui.GuiWiki;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.FMLClientHandler;

public class WikiHooks implements IWikiHooks{

    @Override
    public void showWikiGui(String pageLocation){
        GuiScreen gui = FMLClientHandler.instance().getClient().currentScreen;

        GuiWiki guiWiki = new GuiWiki();
        FMLClientHandler.instance().showGuiScreen(guiWiki);

        guiWiki.setCurrentFile(pageLocation);
        guiWiki.setPreviousScreen(gui);
    }
}
