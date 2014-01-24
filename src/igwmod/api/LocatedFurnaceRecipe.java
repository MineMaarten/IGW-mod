package igwmod.api;

import net.minecraft.item.ItemStack;

public class LocatedFurnaceRecipe{
    private final ItemStack input, output;
    private final int x, y;

    public LocatedFurnaceRecipe(ItemStack input, ItemStack output, int x, int y){
        this.input = input;
        this.output = output;
        this.x = x;
        this.y = y;
    }

    public ItemStack getInput(){
        return input;
    }

    public ItemStack getOutput(){
        return output;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }
}
