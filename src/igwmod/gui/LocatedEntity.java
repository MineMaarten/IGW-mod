package igwmod.gui;

import igwmod.lib.Paths;
import igwmod.lib.Util;

import java.awt.Rectangle;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;

public class LocatedEntity implements IReservedSpace, IPageLink{
    private final Entity entity;
    private int x, y;

    public LocatedEntity(Class<? extends Entity> clazz, int x, int y){
        entity = Util.getEntityForClass(clazz);
        this.x = x;
        this.y = y;
    }

    @Override
    public void render(GuiWiki gui, int mouseX, int mouseY){
        EntityWikiTab.drawEntity(entity, x + 16, y + 27, 0.5F, 0);
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

    @Override
    public boolean onMouseClick(GuiWiki gui, int x, int y){
        if(getReservedSpace().contains(x, y)) {
            gui.setCurrentFile(entity);
            return true;
        }
        return false;
    }

    @Override
    public Rectangle getReservedSpace(){
        return new Rectangle(x, y, 32, 32);
    }

    @Override
    public String getName(){
        return entity.getEntityName();
    }

    @Override
    public String getLinkAddress(){
        return Paths.WIKI_PATH + "entity/" + EntityList.getEntityString(entity);
    }

}
