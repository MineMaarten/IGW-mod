package igwmod;

import igwmod.gui.GuiWiki;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class TickHandler{
    private static int ticksHovered;
    private static Entity lastEntityHovered;
    private static int xHovered;
    private static int yHovered;
    private static int zHovered;
    public static int ticksExisted;
    private static final int MIN_TICKS_HOVER = 50;

    @SubscribeEvent
    public void tick(TickEvent.PlayerTickEvent event){
        if(event.phase == TickEvent.Phase.END) {
            EntityPlayer player = event.player;
            if(player == FMLClientHandler.instance().getClient().thePlayer) {
                if(!ConfigHandler.popupPage.equals("")) {
                    GuiWiki gui = new GuiWiki();
                    FMLCommonHandler.instance().showGuiScreen(gui);
                    gui.setCurrentFile(ConfigHandler.popupPage);
                    if(!ConfigHandler.popupPageEveryTime) {
                        ConfigHandler.disablePopUpPage();
                    } else {
                        ConfigHandler.popupPage = "";
                    }
                }
                ticksExisted++;
                MovingObjectPosition lookedObject = FMLClientHandler.instance().getClient().objectMouseOver;
                if(lookedObject != null) {
                    if(lookedObject.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                        if(lastEntityHovered == lookedObject.entityHit) {
                            ticksHovered++;
                            xHovered = 0;
                            yHovered = 0;
                            zHovered = 0;
                        } else {
                            lastEntityHovered = lookedObject.entityHit;
                            ticksHovered = 0;
                            xHovered = 0;
                            yHovered = 0;
                            zHovered = 0;
                        }
                    } else {
                        if(lookedObject.blockX == xHovered && lookedObject.blockY == yHovered && lookedObject.blockZ == zHovered) {
                            ticksHovered++;
                            lastEntityHovered = null;
                        } else {
                            if(!event.player.worldObj.isAirBlock(lookedObject.blockX, lookedObject.blockY, lookedObject.blockZ)) {
                                ticksHovered = 0;
                                lastEntityHovered = null;
                                xHovered = lookedObject.blockX;
                                yHovered = lookedObject.blockY;
                                zHovered = lookedObject.blockZ;
                            }
                        }
                    }
                }
            }
        }
    }

    public static boolean showTooltip(){
        return ticksHovered > MIN_TICKS_HOVER;
    }

    public static void openWikiGui(){
        // if(showTooltip()) {
        ConfigHandler.disableTooltip();
        if(lastEntityHovered != null) {
            GuiWiki gui = new GuiWiki();
            FMLCommonHandler.instance().showGuiScreen(gui);
            gui.setCurrentFile(lastEntityHovered);
        } else if(xHovered != 0 || yHovered != 0 || zHovered != 0) {
            World world = FMLClientHandler.instance().getClient().theWorld;
            if(world != null) {
                if(!world.isAirBlock(xHovered, yHovered, zHovered)) {
                    GuiWiki gui = new GuiWiki();
                    FMLCommonHandler.instance().showGuiScreen(gui);
                    gui.setCurrentFile(world, xHovered, yHovered, zHovered);
                }
            }
        } else {
            FMLCommonHandler.instance().showGuiScreen(new GuiWiki());
        }
    }

    public static String getCurrentObjectName(){
        if(lastEntityHovered != null) {
            return lastEntityHovered.getCommandSenderName();
        } else {
            try {
                World world = FMLClientHandler.instance().getClient().theWorld;
                Block block = world.getBlock(xHovered, yHovered, zHovered);
                if(block != null) {
                    ItemStack idPicked = block.getPickBlock(FMLClientHandler.instance().getClient().objectMouseOver, world, xHovered, yHovered, zHovered);
                    return (idPicked != null ? idPicked : new ItemStack(block, 1, world.getBlockMetadata(xHovered, yHovered, zHovered))).getDisplayName();
                }
            } catch(Throwable e) {}
            return EnumChatFormatting.RED + "<ERROR>";
        }

    }

}
