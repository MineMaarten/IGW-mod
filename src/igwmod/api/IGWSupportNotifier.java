package igwmod.api;

import java.io.File;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * This class is meant to be copied to your own mod which implements IGW-Mod. When properly implemented by instantiating a new instance somewhere in your mod
 * loading stage, this will notify the player when it doesn't have IGW in the instance. It also needs to have the config option enabled to
 * notify the player. This config option will be generated in its own config file.
 * @author MineMaarten https://github.com/MineMaarten/IGW-mod
 */
public class IGWSupportNotifier{
    private String supportingMod;
    private static final String LATEST_DL_URL = "http://minecraft.curseforge.com/mc-mods/223815-in-game-wiki-mod/files/latest";
    private static final String DL_URL_1_7_10 = "http://minecraft.curseforge.com/mc-mods/223815-in-game-wiki-mod/files/2248719/download";

    /**
     * Needs to be instantiated somewhere in your mod's loading stage.
     */
    public IGWSupportNotifier(){
        if(FMLCommonHandler.instance().getSide() == Side.CLIENT && !Loader.isModLoaded("IGWMod")) {
            File dir = new File(".", "config");
            Configuration config = new Configuration(new File(dir, "IGWMod.cfg"));
            config.load();

            if(config.get(Configuration.CATEGORY_GENERAL, "enable_missing_notification", true, "When enabled, this will notify players when IGW-Mod is not installed even though mods add support.").getBoolean()) {
                ModContainer mc = Loader.instance().activeModContainer();
                String modid = mc.getModId();
                List<ModContainer> loadedMods = Loader.instance().getActiveModList();
                for(ModContainer container : loadedMods) {
                    if(container.getModId().equals(modid)) {
                        supportingMod = container.getName();
                        MinecraftForge.EVENT_BUS.register(this);
//                        ClientCommandHandler.instance.registerCommand(new CommandDownloadIGW());
                        break;
                    }
                }
            }
            config.save();
        }
    }

    @SubscribeEvent
    public void onPlayerJoin(TickEvent.PlayerTickEvent event){
        if(event.player.world.isRemote && event.player == FMLClientHandler.instance().getClientPlayerEntity()) {
//            event.player.(ITextComponent.Serializer.jsonToComponent("[\"" + TextFormatting.GOLD + "The mod " + supportingMod + " is supporting In-Game Wiki mod. " + TextFormatting.GOLD + "However, In-Game Wiki isn't installed! " + "[\"," + "{\"text\":\"Download Latest\",\"color\":\"green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/igwmod_download\"}}," + "\"]\"]"));
        	GuiNewChat chat = Minecraft.getMinecraft().ingameGUI.getChatGUI();
        	chat.printChatMessage(ITextComponent.Serializer.jsonToComponent("[\"" + TextFormatting.GOLD + "The mod " + supportingMod + " is supporting In-Game Wiki mod. " + TextFormatting.GOLD + "However, In-Game Wiki isn't installed! " + "[\"," + "{\"text\":\"Download Latest\",\"color\":\"green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/igwmod_download\"}}," + "\"]\"]"));
        	FMLCommonHandler.instance().bus().unregister(this);
        }
    }

//    Removes so the original author can fix these commands/downloads
    
//    private class CommandDownloadIGW extends CommandBase{
//
//        @Override
//        public int getRequiredPermissionLevel(){
//            return -100;
//        }
//
//        @Override
//        public String getCommandName(){
//            return "igwmod_download";
//        }
//
//        @Override
//        public String getCommandUsage(ICommandSender p_71518_1_){
//            return getCommandName();
//        }
//
//		@Override
//		public void execute(MinecraftServer server, ICommandSender sender,
//				String[] args) throws CommandException {
//			new ThreadDownloadIGW();
//		}
//
//		@Override
//		public String getName() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public String getUsage(ICommandSender sender) {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//    }

//    private class ThreadDownloadIGW extends Thread{
//
//        public ThreadDownloadIGW(){
//            setName("IGW-Mod Download Thread");
//            start();
//        }
//
//        @Override
//        public void run(){
//
//            try {
//                if(Minecraft.getMinecraft().player != null) Minecraft.getMinecraft().thePlayer.addChatMessage(new TextComponentString("Downloading IGW-Mod..."));
//
//                URL url = new URL(Loader.MC_VERSION.equals("1.7.10") ? DL_URL_1_7_10 : LATEST_DL_URL);
//                URLConnection connection = url.openConnection();
//                connection.connect();
//                File dir = new File(".", "mods");
//                File tempFile = File.createTempFile("IGW-Mod.jar", "");
//                FileUtils.copyURLToFile(url, tempFile);
//
//                ZipFile jar = new ZipFile(tempFile.getAbsolutePath());
//                Enumeration<? extends ZipEntry> entries = jar.entries();
//                InputStream mcmodInfo = null;
//                while(entries.hasMoreElements()) {
//                    ZipEntry entry = entries.nextElement();
//                    if(entry.getName().equals("mcmod.info")) {
//                        mcmodInfo = jar.getInputStream(entry);
//                        break;
//                    }
//                }
//                JsonParser parser = new JsonParser();
//                JsonObject obj = (JsonObject)((JsonArray)parser.parse(IOUtils.toString(mcmodInfo))).get(0);
//                jar.close();
//                String version = obj.get("version").getAsString();
//                String mcVersion = obj.get("mcversion").getAsString();
//                File renamedFile = new File(String.format("." + File.separator + "mods" + File.separator + "IGW-Mod-%s-%s-universal.jar", mcVersion, version));
//                FileUtils.copyFile(tempFile, renamedFile);
//                if(Minecraft.getMinecraft().thePlayer != null) Minecraft.getMinecraft().player.addChatMessage(new TextComponentString(TextFormatting.GREEN + "Successfully downloaded. Restart Minecraft to apply."));
//                Desktop.getDesktop().open(dir);
//                if(!Loader.MC_VERSION.equals(mcVersion)) {
//                    if(Minecraft.getMinecraft().thePlayer != null) Minecraft.getMinecraft().player.addChatMessage(new TextComponentString(TextFormatting.RED + "The version of Minecraft you are running doesn't seem to match the version of IGW-Mod that has been downloaded. The mod may not work."));
//                }
//
//                finalize();
//            } catch(Throwable e) {
//                e.printStackTrace();
//                if(Minecraft.getMinecraft().thePlayer != null) Minecraft.getMinecraft().player.addChatComponentMessage(new TextComponentString(TextFormatting.RED + "Failed to download"));
//                try {
//                    finalize();
//                } catch(Throwable e1) {
//                    e1.printStackTrace();
//                }
//            }
//        }
//
//    }
}
