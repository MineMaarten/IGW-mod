package igwmod;
import java.util.EnumSet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class TooltipOverlayHandler implements ITickHandler{

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData){}

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData){//tickData[0] = partialTicks
        if(TickHandler.showTooltip() && ConfigHandler.shouldShowTooltip && FMLClientHandler.instance().getClient().inGameHasFocus) {
            Minecraft mc = FMLClientHandler.instance().getClient();
            ScaledResolution sr = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
            FontRenderer fontRenderer = FMLClientHandler.instance().getClient().fontRenderer;
            String objectName = TickHandler.getCurrentObjectName();
            String moreInfo = "'i' for more info";
            fontRenderer.drawString(objectName, sr.getScaledWidth() / 2 - fontRenderer.getStringWidth(objectName) / 2, sr.getScaledHeight() / 2 - 20, 0xFFFFFFFF);
            fontRenderer.drawString(moreInfo, sr.getScaledWidth() / 2 - fontRenderer.getStringWidth(moreInfo) / 2, sr.getScaledHeight() / 2 - 10, 0xFFFFFFFF);
        }
    }

    @Override
    public EnumSet<TickType> ticks(){
        return EnumSet.of(TickType.RENDER);
    }

    @Override
    public String getLabel(){
        return "In-Game Wiki mod tooltip render handler";
    }

}
