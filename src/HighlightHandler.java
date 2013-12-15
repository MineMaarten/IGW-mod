import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.event.ForgeSubscribe;

import org.lwjgl.opengl.GL11;

public class HighlightHandler{
    private float alpha;
    private static final float ALPHA_INCREASE = 0.005F;

    @ForgeSubscribe
    public void onBlockHighlight(DrawBlockHighlightEvent event){
        if(TickHandler.showTooltip()) {
            alpha += ALPHA_INCREASE;
            if(alpha > 0.2F) alpha = 0.2F;
            GL11.glPushMatrix();
            double intPolPlayerX = event.player.prevPosX + (event.player.posX - event.player.prevPosX) * event.partialTicks;
            double intPolPlayerY = event.player.prevPosY + (event.player.posY - event.player.prevPosY) * event.partialTicks;
            double intPolPlayerZ = event.player.prevPosZ + (event.player.posZ - event.player.prevPosZ) * event.partialTicks;
            GL11.glTranslated(-intPolPlayerX, -intPolPlayerY, -intPolPlayerZ);
            GL11.glTranslated(event.target.blockX, event.target.blockY, event.target.blockZ);
            GL11.glColor4d(0, 0, 1, alpha);
            GL11.glLineWidth(3);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            Tessellator tess = Tessellator.instance;
            tess.startDrawing(GL11.GL_LINES);
            tess.addVertex(0, 0, 0);
            tess.addVertex(0, 1, 0);
            tess.addVertex(1, 0, 0);
            tess.addVertex(1, 1, 0);
            tess.addVertex(1, 0, 1);
            tess.addVertex(1, 1, 1);
            tess.addVertex(0, 0, 1);
            tess.addVertex(0, 1, 1);
            tess.draw();

            tess.startDrawing(GL11.GL_LINE_LOOP);
            tess.addVertex(0, 0, 0);
            tess.addVertex(0, 0, 1);
            tess.addVertex(1, 0, 1);
            tess.addVertex(1, 0, 0);
            tess.draw();

            tess.startDrawing(GL11.GL_LINE_LOOP);
            tess.addVertex(0, 1, 0);
            tess.addVertex(0, 1, 1);
            tess.addVertex(1, 1, 1);
            tess.addVertex(1, 1, 0);
            tess.draw();

            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glPopMatrix();
        } else {
            alpha = 0;
        }
    }
}
