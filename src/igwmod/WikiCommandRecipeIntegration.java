package igwmod;

import igwmod.api.RecipeRetrievalEvent;
import igwmod.gui.GuiWiki;
import igwmod.gui.LocatedStack;
import igwmod.gui.LocatedTexture;
import igwmod.lib.Log;
import igwmod.lib.Paths;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.ShapedOreRecipe;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class WikiCommandRecipeIntegration{
    private static final int STACKS_X_OFFSET = 1;
    private static final int STACKS_Y_OFFSET = 1;
    private static final int RESULT_STACK_X_OFFSET = 95;
    private static final int RESULT_STACK_Y_OFFSET = STACKS_Y_OFFSET + 18;

    /**
     * 
     * @param code example: {www,cic,crc,w=block/wood,c=block/stonebrick,i=item/ingotIron,r=block/redstoneDust}block/pistonBase
     * @param locatedStacks
     * @param locatedTextures
     */
    public static void addCraftingRecipe(String code, List<LocatedStack> locatedStacks, List<LocatedTexture> locatedTextures) throws IllegalArgumentException{
        if(!code.startsWith("crafting{")) throw new IllegalArgumentException("Code needs to starts with 'crafting{'! Full code: " + code);
        String[] codeParts = code.substring(9).split(",");
        int x;
        try {
            x = Integer.parseInt(codeParts[0]);
        } catch(NumberFormatException e) {
            throw new IllegalArgumentException("The first parameter (the x coordinate) contains an invalid number. Check for spaces or invalid characters! Full Code: " + code);
        }
        int y;
        try {
            y = Integer.parseInt(codeParts[1]);
        } catch(NumberFormatException e) {
            throw new IllegalArgumentException("The second parameter (the y coordinate) contains an invalid number. Check for spaces or invalid characters! Full Code: " + code);
        }
        locatedTextures.add(new LocatedTexture(TextureSupplier.getTexture(Paths.WIKI_PATH + "texture/GuiCrafting.png"), x, y, (int)(116 / GuiWiki.TEXT_SCALE), (int)(54 / GuiWiki.TEXT_SCALE)));

        if(code.contains("key=")) {
            if(codeParts.length != 3) throw new IllegalArgumentException("An RecipeRetrievalEvent crafting code can only have 3 parameters: x, y and the key! Full Code: " + code);
            addAutomaticCraftingRecipe(codeParts[2], locatedStacks, locatedTextures, (int)(x * GuiWiki.TEXT_SCALE), (int)(y * GuiWiki.TEXT_SCALE));
        } else {
            addManualCraftingRecipe(codeParts, locatedStacks, locatedTextures, (int)(x * GuiWiki.TEXT_SCALE), (int)(y * GuiWiki.TEXT_SCALE));
        }
    }

    /**
     * Check RecipeRetrievalEvent to see what this method does.
     * @param y 
     * @param x 
     */
    private static void addAutomaticCraftingRecipe(String code, List<LocatedStack> locatedStacks, List<LocatedTexture> locatedTextures, int x, int y) throws IllegalArgumentException{
        String key = code.substring(4, code.length() - 1);
        RecipeRetrievalEvent recipeEvent = new RecipeRetrievalEvent(key);
        MinecraftForge.EVENT_BUS.post(recipeEvent);
        if(recipeEvent.recipe instanceof ShapedRecipes) {
            ShapedRecipes recipe = (ShapedRecipes)recipeEvent.recipe;
            for(int i = 0; i < recipe.recipeHeight; i++) {
                for(int j = 0; j < recipe.recipeWidth; j++) {
                    ItemStack ingredientStack = recipe.recipeItems[i * 3 + j];
                    if(ingredientStack != null) {
                        locatedStacks.add(new LocatedStack(ingredientStack, x + STACKS_X_OFFSET + j * 18, y + STACKS_Y_OFFSET + i * 18));
                    }
                }
            }
            locatedStacks.add(new LocatedStack(recipe.getRecipeOutput(), x + RESULT_STACK_X_OFFSET, y + RESULT_STACK_Y_OFFSET));
        } else if(recipeEvent.recipe instanceof ShapedOreRecipe) {
            ShapedOreRecipe recipe = (ShapedOreRecipe)recipeEvent.recipe;
            int recipeHeight = 0;
            int recipeWidth = 0;
            try {
                recipeHeight = ReflectionHelper.findField(ShapedOreRecipe.class, "height").getInt(recipe);
                recipeWidth = ReflectionHelper.findField(ShapedOreRecipe.class, "width").getInt(recipe);
            } catch(Exception e) {
                Log.error("Something went wrong while trying to get the width and height fields from ShapedOreRecipe!");
                e.printStackTrace();
            }
            for(int i = 0; i < recipeHeight; i++) {
                for(int j = 0; j < recipeWidth; j++) {
                    Object ingredient = recipe.getInput()[i * 3 + j];
                    ItemStack ingredientStack = ingredient instanceof ItemStack ? (ItemStack)ingredient : ((List<ItemStack>)ingredient).get(0);
                    if(ingredientStack != null) {
                        locatedStacks.add(new LocatedStack(ingredientStack, x + STACKS_X_OFFSET + j * 18, y + STACKS_Y_OFFSET + i * 18));
                    }
                }
            }
            locatedStacks.add(new LocatedStack(recipe.getRecipeOutput(), x + RESULT_STACK_X_OFFSET, y + RESULT_STACK_Y_OFFSET));
        } else if(recipeEvent.recipe instanceof ShapelessRecipes) {
            ShapelessRecipes recipe = (ShapelessRecipes)recipeEvent.recipe;
            for(int i = 0; i < 3; i++) {
                for(int j = 0; j < 3; j++) {
                    if(i * 3 + j < recipe.recipeItems.size()) {
                        ItemStack ingredientStack = (ItemStack)recipe.recipeItems.get(i * 3 + j);
                        if(ingredientStack != null) {
                            locatedStacks.add(new LocatedStack(ingredientStack, x + STACKS_X_OFFSET + j * 18, y + STACKS_Y_OFFSET + i * 18));
                        }
                    }
                }
            }
            locatedStacks.add(new LocatedStack(recipe.getRecipeOutput(), x + RESULT_STACK_X_OFFSET, y + RESULT_STACK_Y_OFFSET));
        } else if(recipeEvent.recipe == null) {
            throw new IllegalArgumentException("RecipeRetrievalEvent: For the given key, no subscriber returned a recipe! key = " + key);
        } else {
            throw new IllegalArgumentException("RecipeRetrievalEvent: Don't pass anything other than ShapedRecipes, ShapedOreRecipes or ShapelessRecipes! key = " + key);
        }
    }

    private static void addManualCraftingRecipe(String[] codeParts, List<LocatedStack> locatedStacks, List<LocatedTexture> locatedTextures, int x, int y) throws IllegalArgumentException{
        String[] lastTwoArguments = codeParts[codeParts.length - 1].split("\\}");
        if(lastTwoArguments.length != 2) throw new IllegalArgumentException("There's something wrong with the last two arguments of the code. Check if it contains a '}'! Last part: " + codeParts[codeParts.length - 1]);
        String[] ingredients = new String[codeParts.length - 1];
        for(int i = 2; i < codeParts.length; i++)
            ingredients[i - 2] = codeParts[i];
        ingredients[codeParts.length - 2] = lastTwoArguments[0];
        String result = lastTwoArguments[1];
        Map<String, ItemStack> ingredientMap = new HashMap<String, ItemStack>();
        for(int i = 3; i < ingredients.length; i++) {
            String[] ingredient = ingredients[i].split("=");
            ingredientMap.put(ingredient[0], WikiUtils.getStackFromName(ingredient[1]));
        }
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                ItemStack ingredientStack = ingredientMap.get(ingredients[i].substring(j, j + 1));
                if(ingredientStack != null) {
                    locatedStacks.add(new LocatedStack(ingredientStack, x + STACKS_X_OFFSET + j * 18, y + STACKS_Y_OFFSET + i * 18));
                }
            }
        }
        ItemStack resultStack = WikiUtils.getStackFromName(result);
        if(resultStack != null) {
            locatedStacks.add(new LocatedStack(resultStack, x + RESULT_STACK_X_OFFSET, y + RESULT_STACK_Y_OFFSET));
        }
    }

    /**
     * 
     * @param code example: {block/sand}block/glass
     * @param locatedStacks
     * @param locatedTextures
     * @param x
     * @param y
     */
    public static void addFurnaceRecipe(String code, List<LocatedStack> locatedStacks, List<LocatedTexture> locatedTextures) throws IllegalArgumentException{
        String[] codeParts = code.substring(8).split(",");
        if(codeParts.length != 3) throw new IllegalArgumentException("Code needs to contain 3 parts: x, y, and the recipe. It now contains " + codeParts.length + ". Full code: " + code);
        String[] recipe = codeParts[code.length() - 1].split("}");
        if(recipe.length != 2) throw new IllegalArgumentException("Recipe needs to contain 2 parts: ',input}output]'. It now contains " + recipe.length + ". Full code: " + code);
        int x;
        try {
            x = Integer.parseInt(codeParts[0]);
        } catch(NumberFormatException e) {
            throw new IllegalArgumentException("The first parameter (the x coordinate) contains an invalid number. Check for spaces or invalid characters! Full Code: " + code);
        }
        int y;
        try {
            y = Integer.parseInt(codeParts[1]);
        } catch(NumberFormatException e) {
            throw new IllegalArgumentException("The second parameter (the y coordinate) contains an invalid number. Check for spaces or invalid characters! Full Code: " + code);
        }
        locatedTextures.add(new LocatedTexture(TextureSupplier.getTexture(Paths.WIKI_PATH + "wiki/texture/GuiFurnace.png"), x, y, (int)(82 / GuiWiki.TEXT_SCALE), (int)(54 / GuiWiki.TEXT_SCALE)));
        x = (int)(x * GuiWiki.TEXT_SCALE);
        y = (int)(y * GuiWiki.TEXT_SCALE);
        ItemStack inputStack = WikiUtils.getStackFromName(recipe[0]);
        if(inputStack != null) {
            locatedStacks.add(new LocatedStack(inputStack, x + STACKS_X_OFFSET, y + STACKS_Y_OFFSET));
        }
        ItemStack resultStack = WikiUtils.getStackFromName(recipe[1]);
        if(resultStack != null) {
            locatedStacks.add(new LocatedStack(resultStack, x + STACKS_X_OFFSET + 60, y + STACKS_Y_OFFSET + 18));
        }
    }
}
