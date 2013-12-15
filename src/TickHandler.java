import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class TickHandler implements ITickHandler{
    private static int ticksHovered;
    private static Entity lastEntityHovered;
    private static int xHovered;
    private static int yHovered;
    private static int zHovered;
    public static int ticksExisted;

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData){}

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData){
        EntityPlayer player = (EntityPlayer)tickData[0];
        if(player == FMLClientHandler.instance().getClient().thePlayer) {
            ticksExisted++;
            MovingObjectPosition lookedObject = WikiUtils.getPlayerLookedObject();
            if(lookedObject != null) {
                if(lookedObject.typeOfHit == EnumMovingObjectType.ENTITY) {
                    if(lastEntityHovered == lookedObject.entityHit) {
                        ticksHovered++;
                        if(ticksHovered == 20) {
                            System.out.println("entity: " + lookedObject.entityHit.getEntityName());
                        }
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
                        if(ticksHovered == 60) player.addChatMessage("press 'i' to see info about this object.");
                    } else {
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

    public static boolean showTooltip(){
        return ticksHovered > 60;
    }

    @Override
    public EnumSet<TickType> ticks(){
        return EnumSet.of(TickType.PLAYER);
    }

    @Override
    public String getLabel(){
        return "In-Game Wiki Mod tickhandler";
    }

    public static void openWikiGui(){
        if(showTooltip()) {
            World world = FMLClientHandler.instance().getClient().theWorld;
            if(world != null) {
                Block block = Block.blocksList[world.getBlockId(xHovered, yHovered, zHovered)];
                if(block != null) {
                    GuiBlockWiki gui = new GuiBlockWiki();
                    FMLCommonHandler.instance().showGuiScreen(gui);
                    gui.setCurrentFile(Paths.WIKI_PATH + block.getUnlocalizedName().replace("tile.", "block/"), new ItemStack(block.blockID, 1, world.getBlockMetadata(xHovered, yHovered, zHovered)));

                }
            }
        } else {
            FMLCommonHandler.instance().showGuiScreen(new GuiBlockWiki());
        }
    }

}
