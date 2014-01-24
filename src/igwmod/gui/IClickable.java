package igwmod.gui;

public interface IClickable extends IReservedSpace, IWidged{
    public void onMouseClick(GuiWiki gui, int x, int y);
}
