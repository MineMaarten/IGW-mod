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
    public static void addCraftingRecipe(String code, List<LocatedStack> locatedStacks, List<LocatedTexture> locatedTextures, int x, int y){
        locatedTextures.add(new LocatedTexture(TextureSupplier.getTexture(Paths.WIKI_PATH + "texture/GuiCrafting.png"), x + GuiWiki.TEXT_START_X, y + GuiWiki.TEXT_START_Y, 256, 256, 1));
        if(code.startsWith("{key=")) {
            addAutomaticCraftingRecipe(code, locatedStacks, locatedTextures, x, y);
        } else {
            addManualCraftingRecipe(code, locatedStacks, locatedTextures, x, y);
        }
    }

    /**
     * Check RecipeRetrievalEvent to see what this method does.
     */
    private static void addAutomaticCraftingRecipe(String code, List<LocatedStack> locatedStacks, List<LocatedTexture> locatedTextures, int x, int y){
        String key = code.substring(5, code.length() - 1);
        RecipeRetrievalEvent recipeEvent = new RecipeRetrievalEvent(key);
        MinecraftForge.EVENT_BUS.post(recipeEvent);
        if(recipeEvent.recipe instanceof ShapedRecipes) {
            ShapedRecipes recipe = (ShapedRecipes)recipeEvent.recipe;
            for(int i = 0; i < recipe.recipeHeight; i++) {
                for(int j = 0; j < recipe.recipeWidth; j++) {
                    ItemStack ingredientStack = recipe.recipeItems[i * 3 + j];
                    if(ingredientStack != null) {
                        locatedStacks.add(new LocatedStack(ingredientStack, x + STACKS_X_OFFSET + GuiWiki.TEXT_START_X + j * 18, y + STACKS_Y_OFFSET + GuiWiki.TEXT_START_Y + i * 18));
                    }
                }
            }
            locatedStacks.add(new LocatedStack(recipe.getRecipeOutput(), x + RESULT_STACK_X_OFFSET + GuiWiki.TEXT_START_X, y + RESULT_STACK_Y_OFFSET + GuiWiki.TEXT_START_Y));
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
                        locatedStacks.add(new LocatedStack(ingredientStack, x + STACKS_X_OFFSET + GuiWiki.TEXT_START_X + j * 18, y + STACKS_Y_OFFSET + GuiWiki.TEXT_START_Y + i * 18));
                    }
                }
            }
            locatedStacks.add(new LocatedStack(recipe.getRecipeOutput(), x + RESULT_STACK_X_OFFSET + GuiWiki.TEXT_START_X, y + RESULT_STACK_Y_OFFSET + GuiWiki.TEXT_START_Y));
        } else if(recipeEvent.recipe instanceof ShapelessRecipes) {
            ShapelessRecipes recipe = (ShapelessRecipes)recipeEvent.recipe;
            for(int i = 0; i < 3; i++) {
                for(int j = 0; j < 3; j++) {
                    if(i * 3 + j < recipe.recipeItems.size()) {
                        ItemStack ingredientStack = (ItemStack)recipe.recipeItems.get(i * 3 + j);
                        if(ingredientStack != null) {
                            locatedStacks.add(new LocatedStack(ingredientStack, x + STACKS_X_OFFSET + GuiWiki.TEXT_START_X + j * 18, y + STACKS_Y_OFFSET + GuiWiki.TEXT_START_Y + i * 18));
                        }
                    }
                }
            }
            locatedStacks.add(new LocatedStack(recipe.getRecipeOutput(), x + RESULT_STACK_X_OFFSET + GuiWiki.TEXT_START_X, y + RESULT_STACK_Y_OFFSET + GuiWiki.TEXT_START_Y));
        } else if(recipeEvent.recipe == null) {
            throw new IllegalArgumentException("RecipeRetrievalEvent: For the given key, no subscriber returned a recipe! key = " + key);
        } else {
            throw new IllegalArgumentException("RecipeRetrievalEvent: Don't pass anything other than ShapedRecipes or ShapelessRecipes! key = " + key);
        }
    }

    private static void addManualCraftingRecipe(String code, List<LocatedStack> locatedStacks, List<LocatedTexture> locatedTextures, int x, int y){
        String[] recipeParts = code.substring(1).split("\\}");
        String ingredientCode = recipeParts[0];
        String result = recipeParts[1];
        String[] ingredients = ingredientCode.split(",");
        Map<String, ItemStack> ingredientMap = new HashMap<String, ItemStack>();
        for(int i = 3; i < ingredients.length; i++) {
            String[] ingredient = ingredients[i].split("=");
            ingredientMap.put(ingredient[0], WikiUtils.getStackFromName(ingredient[1]));
        }
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                ItemStack ingredientStack = ingredientMap.get(ingredients[i].substring(j, j + 1));
                if(ingredientStack != null) {
                    locatedStacks.add(new LocatedStack(ingredientStack, x + STACKS_X_OFFSET + GuiWiki.TEXT_START_X + j * 18, y + STACKS_Y_OFFSET + GuiWiki.TEXT_START_Y + i * 18));
                }
            }
        }

        ItemStack resultStack = WikiUtils.getStackFromName(result);
        if(resultStack != null) {
            locatedStacks.add(new LocatedStack(resultStack, x + RESULT_STACK_X_OFFSET + GuiWiki.TEXT_START_X, y + RESULT_STACK_Y_OFFSET + GuiWiki.TEXT_START_Y));
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
    public static void addFurnaceRecipe(String code, List<LocatedStack> locatedStacks, List<LocatedTexture> locatedTextures, int x, int y){
        locatedTextures.add(new LocatedTexture(TextureSupplier.getTexture(Paths.WIKI_PATH + "texture/GuiFurnace.png"), x + GuiWiki.TEXT_START_X, y + GuiWiki.TEXT_START_Y, 256, 256, 1));
        String[] recipe = code.substring(1).split("}");
        ItemStack inputStack = WikiUtils.getStackFromName(recipe[0]);
        if(inputStack != null) {
            locatedStacks.add(new LocatedStack(inputStack, x + STACKS_X_OFFSET + GuiWiki.TEXT_START_X, y + STACKS_Y_OFFSET + GuiWiki.TEXT_START_Y));
        }
        ItemStack resultStack = WikiUtils.getStackFromName(recipe[1]);
        if(resultStack != null) {
            locatedStacks.add(new LocatedStack(resultStack, x + STACKS_X_OFFSET + 60 + GuiWiki.TEXT_START_X, y + STACKS_Y_OFFSET + 18 + GuiWiki.TEXT_START_Y));
        }
    }
}
