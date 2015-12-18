package igwmod;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class TessWrapper{
    public static void startDrawingQuads(){
        Tessellator.getInstance().getWorldRenderer().func_181668_a(7, DefaultVertexFormats.field_181707_g);
    }

    public static void addVertex(double x, double y, double z){
        Tessellator.getInstance().getWorldRenderer().func_181662_b(x, y, z).func_181675_d();
    }

    public static void addVertexWithUV(double x, double y, double z, double u, double v){
        Tessellator.getInstance().getWorldRenderer().func_181662_b(x, y, z).func_181673_a(u, v).func_181675_d();
    }

    public static void draw(){
        Tessellator.getInstance().draw();
    }
}
