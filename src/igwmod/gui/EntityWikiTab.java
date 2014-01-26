package igwmod.gui;

import igwmod.TickHandler;
import igwmod.lib.Textures;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

public class EntityWikiTab implements IWikiTab{
    private static Entity curEntity;
    private static List<Entity> filteredEntityList = new ArrayList<Entity>();
    private static List<Entity> shownEntityList = new ArrayList<Entity>();
    private static List<Entity> totalEntityList;
    private static Entity tabEntity;

    public EntityWikiTab(){

    }

    @Override
    public String getName(){
        return "igwmod.wikitab.entities.name";
    }

    @Override
    public ItemStack renderTabIcon(GuiWiki gui){
        if(tabEntity == null) {
            EntityPlayer player = gui.mc.thePlayer;
            tabEntity = new EntityCreeper(player.worldObj);
        }
        drawEntity(tabEntity, 18, 28, 0.6F, 0);
        return null;
    }

    @Override
    public List<IReservedSpace> getReservedSpaces(){
        List<IReservedSpace> reservedSpaces = new ArrayList<IReservedSpace>();
        reservedSpaces.add(new ReservedSpace(new Rectangle(0, 0, 200, Integer.MAX_VALUE)));
        reservedSpaces.add(new LocatedTexture(Textures.GUI_ENTITIES, 40, 65, 53, 162));
        return reservedSpaces;
    }

    @Override
    public List<IPageLink> getPages(int[] indexes){
        return null;
    }

    @Override
    public int pagesPerTab(){
        return 4;
    }

    @Override
    public int pagesPerScroll(){
        return 1;
    }

    @Override
    public void renderForeground(GuiWiki gui, int mouseX, int mouseY){
        // TODO Auto-generated method stub

    }

    @Override
    public void renderBackground(GuiWiki gui, int mouseX, int mouseY){
        //  GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        // RenderHelper.enableStandardItemLighting();
        drawEntity(curEntity, gui.guiLeft + 65, gui.guiTop + 40, 0.7F, 0);
        for(int i = 0; i < shownEntityList.size(); i++) {
            drawEntity(shownEntityList.get(i), gui.guiLeft + 58, gui.guiTop + 103 + i * 36, 0.5F, 0);
        }
        //  GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void onMouseClick(GuiWiki gui, int mouseX, int mouseY, int mouseKey){
        for(int i = 0; i < shownEntityList.size(); i++) {
            if(mouseX >= gui.guiLeft + 41 && mouseX <= gui.guiLeft + 76 && mouseY >= gui.guiTop + 75 + i * 36 && mouseY <= gui.guiTop + 110 + i * 36) {
                gui.setCurrentFile(shownEntityList.get(i));
            }
        }
    }

    private void drawEntity(Entity entity, int x, int y, float size, float partialTicks){
        if(entity != null) {
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            short short1 = 240;
            short short2 = 240;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, short1 / 1.0F, short2 / 1.0F);

            GL11.glPushMatrix();
            GL11.glTranslated(x, y, 10);
            float maxHitboxComponent = Math.max(1, Math.max(entity.width, entity.height));
            GL11.glScaled(40 * size / maxHitboxComponent, -40 * size / maxHitboxComponent, -40 * size / maxHitboxComponent);
            //GL11.glRotated(20, 1, 0, 1);
            GL11.glRotatef(-30.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotated(TickHandler.ticksExisted + partialTicks, 0, 1, 0);
            RenderManager.instance.renderEntityWithPosYaw(entity, 0D, 0D, 0.0D, 0, partialTicks);
            GL11.glPopMatrix();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
        }
    }

    @Override
    public void onPageChange(GuiWiki gui, String pageName, Object... metadata){
        if(metadata[0] instanceof Entity) {
            curEntity = (Entity)metadata[0];
        }
    }

}
