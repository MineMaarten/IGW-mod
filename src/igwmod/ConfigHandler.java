package igwmod;
import java.io.File;

import net.minecraftforge.common.Configuration;

public class ConfigHandler{

    public static boolean shouldShowTooltip;
    private static Configuration conf;

    public static void init(File configFile){
        Configuration config = new Configuration(configFile);
        conf = config;
        config.load();
        shouldShowTooltip = config.get(Configuration.CATEGORY_GENERAL, "Should show tooltip", true).getBoolean(true);
        config.save();
    }

    public static void disableTooltip(){
        conf.load();
        conf.get(Configuration.CATEGORY_GENERAL, "Should show tooltip", true).set(false);
        shouldShowTooltip = false;
        conf.save();
    }
}
