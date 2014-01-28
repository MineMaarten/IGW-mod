package igwmod;

import igwmod.gui.IReservedSpace;
import igwmod.gui.LocatedStack;
import igwmod.gui.LocatedString;
import igwmod.gui.LocatedTexture;
import igwmod.lib.Log;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.Resource;
import net.minecraft.client.resources.ResourceManager;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.client.FMLClientHandler;

public class InfoSupplier{
    private static HashMap<String, ResourceLocation> infoMap = new HashMap<String, ResourceLocation>();
    private static final int MAX_TEXT_X = 475;
    private static int currentTextColor;
    private static String curPrefix = "";
    private static String curLink = "";

    /**
     * Returns a wikipage for an object name.
     * @param objectName
     * @return
     */
    public static List<String> getInfo(String objectName){
        objectName = objectName + ".txt";
        if(!infoMap.containsKey(objectName)) {
            infoMap.put(objectName, new ResourceLocation(objectName));
        }
        try {
            BufferedReader br;
            ResourceManager manager = FMLClientHandler.instance().getClient().getResourceManager();
            ResourceLocation location = infoMap.get(objectName);
            Resource resource = manager.getResource(location);
            InputStream stream = resource.getInputStream();
            br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            List<String> textList = new ArrayList<String>();
            String line = br.readLine();
            while(line != null) {
                textList.add(line);
                line = br.readLine();
            }
            br.close();
            return textList;
        } catch(Exception e) {
            return null;
        }
    }

    public static void analyseInfo(FontRenderer fontRenderer, List<String> fileInfo, List<IReservedSpace> reservedSpaces, List<LocatedString> locatedStrings, List<LocatedStack> locatedStacks, List<LocatedTexture> locatedTextures){
        currentTextColor = 0xFF000000;
        curPrefix = "";
        curLink = "";
        locatedStacks.clear();
        locatedStrings.clear();
        locatedTextures.clear();
        for(int k = 0; k < fileInfo.size(); k++) {
            String line = fileInfo.get(k);
            for(int i = 0; i < line.length(); i++) {
                if(line.charAt(i) == '[') {
                    for(int j = i; j < line.length(); j++) {
                        if(line.charAt(j) == ']') {
                            try {
                                if(decomposeTemplate(line.substring(i + 1, j), locatedStacks, locatedTextures)) {
                                    String cutString = line.substring(0, i) + line.substring(j + 1, line.length());
                                    if(cutString.equals("")) fileInfo.remove(k--);
                                    else fileInfo.set(k, cutString);
                                }
                            } catch(IllegalArgumentException e) {
                                fileInfo.add(EnumChatFormatting.RED + e.getMessage());
                                Log.warning(e.getMessage());
                            }
                            break;
                        }
                    }
                }
            }
        }

        int currentY = 20;
        for(int k = 0; k < fileInfo.size(); k++) {
            String line = " " + fileInfo.get(k);
            String[] sentenceWords = line.split(" ");
            int currentWord = 0;
            int currentX = 0;
            String textPart = "";
            int newX = 0;
            while(currentWord < sentenceWords.length || sentenceWords.length == 0) {
                int curTextColor = currentTextColor;
                String prefix = curPrefix;
                String link = curLink;
                boolean newLine = false;
                while(true) {
                    String potentialString = currentWord >= sentenceWords.length ? "" : textPart + (textPart.equals("") ? "" : " ") + sentenceWords[currentWord];
                    if(currentWord >= sentenceWords.length || fontRenderer.getStringWidth(prefix + potentialString) + currentX > MAX_TEXT_X && fontRenderer.getStringWidth(prefix + sentenceWords[currentWord]) <= MAX_TEXT_X - 200) {
                        newLine = true;
                        newX = 0;
                        break;
                    }
                    newX = getNewXFromIntersection(new Rectangle(currentX, currentY, fontRenderer.getStringWidth(prefix + potentialString), fontRenderer.FONT_HEIGHT), reservedSpaces, locatedStacks, locatedTextures);
                    if(textPart.equals("") && fontRenderer.getStringWidth(prefix + potentialString) + newX <= MAX_TEXT_X) {
                        currentX = newX;
                    } else if(newX != currentX) {
                        break;
                    }
                    currentWord++;
                    textPart = potentialString;
                    boolean foundCode = false;
                    if(currentWord < sentenceWords.length) {
                        String potentialCode = sentenceWords[currentWord];
                        int i = potentialCode.indexOf('[');
                        int j = potentialCode.indexOf(']');
                        while(i != -1 && j != -1) {
                            try {
                                sentenceWords[currentWord] = potentialCode.substring(0, i) + potentialCode.substring(j + 1, potentialCode.length());
                                decomposeInLineTemplate(potentialCode.substring(i + 1, j));
                                newX += fontRenderer.getStringWidth(textPart + " ");
                                foundCode = true;
                            } catch(IllegalArgumentException e) {
                                fileInfo.add(EnumChatFormatting.RED + e.getMessage());
                                Log.warning(e.getMessage());
                            }
                            potentialCode = sentenceWords[currentWord];
                            i = potentialCode.indexOf('[');
                            j = potentialCode.indexOf(']');
                        }
                        if(foundCode) break;
                    }
                }
                locatedStrings.add(link.equals("") ? new LocatedString(prefix + textPart, currentX, currentY, curTextColor, false) : new LocatedString(prefix + textPart, currentX, currentY, false, link));
                if(newLine) currentY += fontRenderer.FONT_HEIGHT + 1;
                currentX = newX;
                textPart = "";
                if(sentenceWords.length == 0) break;
            }
            // currentY += fontRenderer.FONT_HEIGHT + 1;
        }
    }

    private static int getNewXFromIntersection(Rectangle rect, List<IReservedSpace> reservedSpaces, List<LocatedStack> locatedStacks, List<LocatedTexture> locatedTextures){
        int oldX = rect.x;
        boolean modified = false;
        for(IReservedSpace reservedSpace : reservedSpaces) {
            Rectangle space = reservedSpace.getReservedSpace();
            if(space.x + space.width > rect.x && space.intersects(rect)) {
                rect = new Rectangle(space.x + space.width, rect.y, rect.width, rect.height);
                modified = true;
            }
        }
        for(LocatedStack locatedStack : locatedStacks) {
            Rectangle space = locatedStack.getReservedSpace();
            if(space.x + space.width > rect.x && space.intersects(rect)) {
                //rect = new Rectangle(rect.x, rect.y, space.x + space.width - rect.x, rect.height);
                rect = new Rectangle(space.x + space.width, rect.y, rect.width, rect.height);
                modified = true;
            }
        }
        for(LocatedTexture locatedTexture : locatedTextures) {
            Rectangle space = locatedTexture.getReservedSpace();
            if(space.x + space.width > rect.x && space.intersects(rect)) {
                // rect = new Rectangle(rect.x, rect.y, space.x + space.width - rect.x, rect.height);
                rect = new Rectangle(space.x + space.width, rect.y, rect.width, rect.height);
                modified = true;
            }
        }
        return modified ? rect.x : oldX;
    }

    private static boolean decomposeTemplate(String code, List<LocatedStack> locatedStacks, List<LocatedTexture> locatedTextures) throws IllegalArgumentException{
        if(code.startsWith("image")) {
            locatedTextures.add(getImageFromCode(code));
            return true;
        }
        if(code.startsWith("crafting")) {
            WikiCommandRecipeIntegration.addCraftingRecipe(code, locatedStacks, locatedTextures);
            return true;
        }
        if(code.startsWith("furnace")) {
            WikiCommandRecipeIntegration.addFurnaceRecipe(code, locatedStacks, locatedTextures);
            return true;
        }
        return false;
    }

    private static void decomposeInLineTemplate(String code) throws IllegalArgumentException{
        if(!code.endsWith("}")) throw new IllegalArgumentException("Code misses a '}' at the end! Full code: " + code);
        if(code.startsWith("color{")) {
            colorCommand(code);
        } else if(code.startsWith("prefix{")) {
            prefixCommand(code);
        } else if(code.startsWith("link{")) {
            curLink = code.substring(5, code.length() - 1);
            if(curLink.startsWith("block/") || curLink.startsWith("item/") || curLink.startsWith("entity/")) {
                curLink = "igwmod:wiki/" + curLink;
            }
        }
    }

    private static void colorCommand(String code) throws IllegalArgumentException{

        String colorCode = code.substring(6, code.length() - 1);
        if(colorCode.startsWith("0x")) colorCode = colorCode.substring(2);
        try {
            currentTextColor = 0xFF000000 | Integer.parseInt(colorCode, 16);
        } catch(NumberFormatException e) {
            throw new IllegalArgumentException("Using an invalid color parameter. Only use hexadecimal (0123456789ABCDEF) numbers! Also only use 6 digits (no alpha digits). Full code: " + code + ", color code: " + colorCode);
        }
    }

    private static void prefixCommand(String code) throws IllegalArgumentException{
        String prefixCode = code.substring(7, code.length() - 1);
        curPrefix = "";
        for(int i = 0; i < prefixCode.length(); i++) {
            if(prefixCode.charAt(i) != 'r') curPrefix += "\u00a7" + prefixCode.charAt(i);
        }
    }

    private static LocatedTexture getImageFromCode(String code) throws IllegalArgumentException{
        if(!code.startsWith("image{")) throw new IllegalArgumentException("The code needs to start with 'image{'! Full code: " + code);
        String[] codeArguments = code.substring(6).split(",");
        if(codeArguments.length != 5) throw new IllegalArgumentException("The code needs to contain 5 parameters: x, y, width, height, texture location. It now contains " + codeArguments.length + ". Full code: " + code);
        int[] coords = new int[4];
        try {
            for(int i = 0; i < 4; i++)
                coords[i] = Integer.parseInt(codeArguments[i]);
        } catch(NumberFormatException e) {
            throw new IllegalArgumentException("The code contains an invalid number! Check for spaces or invalid characters. Full code: " + code);
        }
        return new LocatedTexture(TextureSupplier.getTexture(codeArguments[4].substring(0, codeArguments[4].length() - 1)), coords[0], coords[1], coords[2], coords[3]);
    }
}
