import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;

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
    public static void addShapedRecipe(String code, List<LocatedStack> locatedStacks, List<LocatedTexture> locatedTextures, int x, int y){
        locatedTextures.add(new LocatedTexture(TextureSupplier.getTexture(Paths.WIKI_PATH + "texture/GuiCrafting.png"), x + GuiWiki.TEXT_START_X, y + GuiWiki.TEXT_START_Y, 256, 256, 1));

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
        if(resultStack != null) locatedStacks.add(new LocatedStack(resultStack, x + RESULT_STACK_X_OFFSET + GuiWiki.TEXT_START_X, y + RESULT_STACK_Y_OFFSET + GuiWiki.TEXT_START_Y));
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
            locatedStacks.add(new LocatedStack(resultStack, x + STACKS_X_OFFSET + 61 + GuiWiki.TEXT_START_X, y + STACKS_Y_OFFSET + 19 + GuiWiki.TEXT_START_Y));
        }
    }
}
