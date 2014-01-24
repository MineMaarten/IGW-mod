package igwmod;

import igwmod.lib.Constants;

import java.util.EnumSet;

import net.minecraft.client.settings.KeyBinding;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.TickType;

public class KeybindingHandler extends KeyBindingRegistry.KeyHandler{
    private static final KeybindingHandler INSTANCE = new KeybindingHandler();

    public KeybindingHandler(){
        super(new KeyBinding[]{new KeyBinding("igwmod.keys.wiki", Constants.DEFAULT_KEYBIND_OPEN_GUI)}, new boolean[]{false});
    }

    public static KeybindingHandler instance(){
        return INSTANCE;
    }

    @Override
    public String getLabel(){
        return "In-Game Wiki Keybind Handler";
    }

    @Override
    public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat){
        if(tickEnd && FMLClientHandler.instance().getClient().inGameHasFocus) {
            TickHandler.openWikiGui();
        }
    }

    public String getKeyName(int keyCode){
        for(KeyBinding keyBinding : keyBindings) {
            if(keyBinding.keyCode == keyCode) return keyBinding.keyDescription;
        }
        return null;
    }

    @Override
    public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd){}

    @Override
    public EnumSet<TickType> ticks(){
        return EnumSet.of(TickType.CLIENT);
    }

}
