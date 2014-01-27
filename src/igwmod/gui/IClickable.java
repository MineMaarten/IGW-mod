package igwmod.gui;

public interface IClickable extends IReservedSpace, IWidged{
    public boolean onMouseClick(GuiWiki gui, int x, int y);
}
