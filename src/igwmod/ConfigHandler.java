package igwmod;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class ConfigHandler{

    public static boolean shouldShowTooltip;
    public static boolean debugMode;
    public static boolean shouldAutoScale;
    private static Configuration conf;

    public static String popupPage;
    public static boolean popupPageEveryTime;

    public static void init(File configFile){
        conf = new Configuration(configFile);
        conf.load();
        shouldShowTooltip = conf.get(Configuration.CATEGORY_GENERAL, "Should show tooltip", true).getBoolean(true);
        debugMode = conf.get(Configuration.CATEGORY_GENERAL, "Debug mode", false).getBoolean(false);
        shouldAutoScale = conf.get(Configuration.CATEGORY_GENERAL, "Automatically scale to GUI scale AUTO in IGW GUI", true).getBoolean(true);
        popupPage = conf.get(Configuration.CATEGORY_GENERAL, "Pop-up page", "", "When putting in a path, this page will be shown right when a player logs into the world").getString();
        popupPageEveryTime = conf.get(Configuration.CATEGORY_GENERAL, "Pop-up every time", false, "When true, will make the 'Pop-up page' show up everytime when a player logs in. When false, it will only ever happen once").getBoolean(false);
        conf.save();
    }

    public static void disableTooltip(){
        conf.load();
        conf.get(Configuration.CATEGORY_GENERAL, "Should show tooltip", true).set(false);
        shouldShowTooltip = false;
        conf.save();
    }

    public static void disablePopUpPage(){
        conf.load();
        conf.get(Configuration.CATEGORY_GENERAL, "Pop-up page", "").set("");
        popupPage = "";
        conf.save();
    }
}
