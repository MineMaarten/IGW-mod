package igwmod.gui;

import java.awt.Rectangle;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

public class LocatedTexture implements IReservedSpace, IWidged{
    public ResourceLocation texture;
    public int x, y, width, heigth;

    public LocatedTexture(ResourceLocation texture, int x, int y, int width, int heigth){
        this.texture = texture;
        this.x = x;
        this.y = y;
        this.width = width;
        this.heigth = heigth;
    }

    @Override
    public Rectangle getReservedSpace(){
        return new Rectangle(x, y, width, heigth);
    }

    @Override
    public void renderBackground(GuiWiki gui, int mouseX, int mouseY){
        gui.mc.getTextureManager().bindTexture(texture);
        drawTexture(x, y, width, heigth);
    }

    @Override
    public void renderForeground(GuiWiki gui, int mouseX, int mouseY){}

    public static void drawTexture(int x, int y, int width, int heigth){
        int minYCap = Math.max(0, GuiWiki.MIN_TEXT_Y - y);
        int maxYCap = Math.min(heigth, GuiWiki.MAX_TEXT_Y - y);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + maxYCap, 0, 0.0, (float)maxYCap / heigth);//TODO render at right Z level
        tessellator.addVertexWithUV(x + width, y + maxYCap, 0, 1.0, (float)maxYCap / heigth);
        tessellator.addVertexWithUV(x + width, y + minYCap, 0, 1, (float)minYCap / heigth);
        tessellator.addVertexWithUV(x, y + minYCap, 0, 0, (float)minYCap / heigth);
        tessellator.draw();
        // this.drawTexturedModalRect(x, y, 0, 0, 16, 16);
    }

    @Override
    public void setX(int x){
        this.x = x;
    }

    @Override
    public void setY(int y){
        this.y = y;
    }

    @Override
    public int getX(){
        return x;
    }

    @Override
    public int getY(){
        return y;
    }
}
