package igwmod;

import igwmod.api.RecipeRetrievalEvent;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.event.ForgeSubscribe;

public class RecipeEventTest{
    @ForgeSubscribe
    public void onButtonRecipeSearch(RecipeRetrievalEvent event){
        if(event.key.equals("stoneButton")) {
            event.recipe = searchForRecipe(Block.stoneButton.blockID);
        } else if(event.key.equals("piston")) {
            event.recipe = searchForRecipe(Block.pistonBase.blockID);
        }
    }

    private IRecipe searchForRecipe(int itemID){
        for(IRecipe recipe : (List<IRecipe>)CraftingManager.getInstance().getRecipeList()) {
            if(recipe.getRecipeOutput() != null && recipe.getRecipeOutput().itemID == itemID) {
                return recipe;
            }
        }
        return null;
    }
}
