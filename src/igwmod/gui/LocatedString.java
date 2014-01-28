package igwmod.gui;

import java.awt.Rectangle;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;

public class LocatedString implements IPageLink{
    protected static FontRenderer fontRenderer = FMLClientHandler.instance().getClient().fontRenderer;
    private final String string;
    private String cappedText;
    private int x;
    private int y;
    private int color;
    private final boolean shadow;
    private String linkAddress;

    /**
     * A constructor for linked located strings. Color doesn't matter as these will always be the same for linked strings.
     * @param string
     * @param x
     * @param y
     * @param shadow
     * @param linkAddress
     */
    public LocatedString(String string, int x, int y, boolean shadow, String linkAddress){
        this.string = string;
        cappedText = string;
        this.x = x;
        this.y = y;
        this.shadow = shadow;
        this.linkAddress = linkAddress;
    }

    /**
     * A constructor for unlinked located strings. You can specify a color.
     * @param string
     * @param x
     * @param y
     * @param color
     * @param shadow
     */
    public LocatedString(String string, int x, int y, int color, boolean shadow){
        this.string = string;
        cappedText = string;
        this.x = x;
        this.y = y;
        this.color = color;
        this.shadow = shadow;
    }

    public LocatedString capTextWidth(int maxWidth){
        cappedText = string;
        if(fontRenderer.getStringWidth(cappedText) <= maxWidth) return this;
        while(fontRenderer.getStringWidth(cappedText + "...") > maxWidth) {
            cappedText = cappedText.substring(0, cappedText.length() - 1);
        }
        cappedText += "...";
        return this;
    }

    @Override
    public boolean onMouseClick(GuiWiki gui, int x, int y){
        if(linkAddress != null) {
            if(getMouseSpace().contains(x, y)) {
                gui.setCurrentFile(linkAddress);
                return true;
            }
        }
        return false;
    }

    private Rectangle getMouseSpace(){
        return new Rectangle((int)(x * GuiWiki.TEXT_SCALE), (int)(y * GuiWiki.TEXT_SCALE), (int)(fontRenderer.getStringWidth(cappedText) * GuiWiki.TEXT_SCALE), (int)(fontRenderer.FONT_HEIGHT * GuiWiki.TEXT_SCALE));
    }

    @Override
    public void render(GuiWiki gui, int mouseX, int mouseY){
        GL11.glPushMatrix();
        GL11.glScaled(GuiWiki.TEXT_SCALE, GuiWiki.TEXT_SCALE, 1);
        if(getLinkAddress() != null) {
            Rectangle mouseSpace = getMouseSpace();
            mouseSpace.x += gui.guiLeft;
            mouseSpace.y += gui.guiTop;
            fontRenderer.drawString(EnumChatFormatting.UNDERLINE + cappedText, x, y, mouseSpace.contains(mouseX, mouseY) ? 0xFFFFFF00 : 0xFF3333FF, shadow);
        } else {
            fontRenderer.drawString(cappedText, x, y, color, shadow);
        }
        GL11.glPopMatrix();
    }

    @Override
    public Rectangle getReservedSpace(){
        return new Rectangle(x, y, fontRenderer.getStringWidth(cappedText), fontRenderer.FONT_HEIGHT);
    }

    @Override
    public String getName(){
        return string;
    }

    @Override
    public String getLinkAddress(){
        return linkAddress;
    }

    @Override
    public String toString(){
        return x + ", " + y + ", string: " + string;
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
