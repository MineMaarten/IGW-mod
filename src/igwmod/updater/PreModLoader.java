package igwmod.updater;

import igwmod.lib.Log;

import java.io.File;
import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

/**
 * For this to be called before the mods get loaded I needed to implement IFMLLoadingPlugin as well as adding a .jar manifest. 
 * This is a MANIFEST.MF file located in the .jar's META-INF file containing the location of this class.
 */
@Deprecated
public class PreModLoader implements IFMLLoadingPlugin{

    @Override
    public String[] getLibraryRequestClass(){
        return null;
    }

    @Override
    public String[] getASMTransformerClass(){
        return null;
    }

    @Override
    public String getModContainerClass(){
        return null;
    }

    @Override
    public String getSetupClass(){
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data){
        Log.info("In-Game wiki mod early stage loading...");
        File mcDir = (File)data.get("mcLocation");
        // PageDownloader.init(mcDir);
    }

}
