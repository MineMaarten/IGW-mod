package igwmod.gui;
import net.minecraft.util.ResourceLocation;

public class LocatedTexture{
    public ResourceLocation texture;
    public int x, y, width, heigth;
    public float size;

    public LocatedTexture(ResourceLocation texture, int x, int y, int width, int heigth, float size){
        this.texture = texture;
        this.x = x;
        this.y = y;
        this.width = width;
        this.heigth = heigth;
        this.size = size;
    }
}
