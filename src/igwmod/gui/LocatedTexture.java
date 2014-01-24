package igwmod.gui;

import java.awt.Rectangle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.client.FMLClientHandler;

public class LocatedTexture implements IReservedSpace, IWidged{
    private static Minecraft mc = FMLClientHandler.instance().getClient();
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
    public void render(GuiWiki gui, int mouseX, int mouseY){
        mc.getTextureManager().bindTexture(texture);
        drawTexture(x, y, width, heigth);
    }

    public static void drawTexture(int x, int y, int width, int height){
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + height, 0, 0.0, 1.0);
        tessellator.addVertexWithUV(x + width, y + height, 0, 1.0, 1.0);
        tessellator.addVertexWithUV(x + width, y, 0, 1.0, 0.0);
        tessellator.addVertexWithUV(x, y, 0, 0.0, 0.0);
        tessellator.draw();
        // this.drawTexturedModalRect(x, y, 0, 0, 16, 16);
    }
}
