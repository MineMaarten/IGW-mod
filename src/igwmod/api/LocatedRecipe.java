package igwmod.api;

import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class LocatedRecipe{
    private final IRecipe recipe;
    private final int x, y;

    public LocatedRecipe(ShapedRecipes recipe, int x, int y){
        this.recipe = recipe;
        this.x = x;
        this.y = y;
    }

    public LocatedRecipe(ShapelessRecipes recipe, int x, int y){
        this.recipe = recipe;
        this.x = x;
        this.y = y;
    }

    public LocatedRecipe(ShapedOreRecipe recipe, int x, int y){
        this.recipe = recipe;
        this.x = x;
        this.y = y;
    }

    public IRecipe getRecipe(){
        return recipe;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

}
