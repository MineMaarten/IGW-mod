package igwmod.gui;

import java.awt.Rectangle;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.EnumChatFormatting;
import cpw.mods.fml.client.FMLClientHandler;

public class LocatedString implements IPageLink{
    protected static FontRenderer fontRenderer = FMLClientHandler.instance().getClient().fontRenderer;
    private final String string;
    private final int x, y;
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
        this.x = x;
        this.y = y;
        this.color = color;
        this.shadow = shadow;
    }

    @Override
    public void onMouseClick(GuiWiki gui, int x, int y){
        if(getReservedSpace().contains(x, y)) {
            gui.setCurrentFile(linkAddress);
        }
    }

    @Override
    public void render(GuiWiki gui, int mouseX, int mouseY){
        if(getLinkAddress() != null) {
            fontRenderer.drawString(EnumChatFormatting.UNDERLINE + string, x, y, getReservedSpace().contains(mouseX, mouseY) ? 0xFFFFFF00 : 0xFF3333FF, shadow);
        } else {
            fontRenderer.drawString(string, x, y, color, shadow);
        }
    }

    @Override
    public Rectangle getReservedSpace(){
        return new Rectangle(x, y, fontRenderer.getStringWidth(string), fontRenderer.FONT_HEIGHT);
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
}
