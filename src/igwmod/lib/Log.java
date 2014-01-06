package igwmod.lib;

import java.util.logging.Level;
import java.util.logging.Logger;

import cpw.mods.fml.common.FMLLog;

public class Log{
    private static Logger logger = Logger.getLogger(Constants.MOD_ID);

    static {
        logger.setParent(FMLLog.getLogger());
    }

    public static void info(String message){
        logger.log(Level.INFO, message);
    }

    public static void error(String message){
        logger.log(Level.SEVERE, message);
    }

    public static void warning(String message){
        logger.log(Level.WARNING, message);
    }
}
