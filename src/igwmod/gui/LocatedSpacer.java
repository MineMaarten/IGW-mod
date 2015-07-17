package igwmod.gui;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

import java.awt.*;

public class LocatedSpacer extends Gui implements IPageLink {
    protected static FontRenderer fontRenderer = FMLClientHandler.instance().getClient().fontRenderer;

    private int x;
    private int y;

    public LocatedSpacer(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String getName() {
        return "-";
    }

    @Override
    public String getLinkAddress() {
        return null;
    }

    @Override
    public boolean onMouseClick(GuiWiki gui, int x, int y) {
        return false;
    }

    @Override
    public Rectangle getReservedSpace() {
        return new Rectangle(x, y, 10, fontRenderer.FONT_HEIGHT);
    }

    @Override
    public void renderBackground(GuiWiki gui, int mouseX, int mouseY) {
        //Honestly, the spacer doesn't really require a renderer, but it's here.

    }

    @Override
    public void renderForeground(GuiWiki gui, int mouseX, int mouseY) {

    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getHeight() {
        return fontRenderer.FONT_HEIGHT;
    }
}
