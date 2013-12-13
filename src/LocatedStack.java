import net.minecraft.item.ItemStack;

public class LocatedStack{
    public ItemStack stack;
    public int x, y;

    public LocatedStack(ItemStack stack, int x, int y){
        this.stack = stack;
        this.x = x;
        this.y = y;
    }

    public boolean isMouseWithinRegion(int mouseX, int mouseY){
        System.out.println("mouseX: " + mouseX + ", mouseY: " + mouseY + ", x: " + x + ", y: " + y);
        return mouseX >= x && mouseX <= x + 16 && mouseY >= y && mouseY <= y + 16;
    }
}
