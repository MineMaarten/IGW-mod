package igwmod;
import java.util.EnumSet;

import net.minecraft.client.settings.KeyBinding;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.TickType;

public class KeybindingHandler extends KeyBindingRegistry.KeyHandler{

    /**
     * For now (ModJam) a quick and dirty hard-coded keybinding (set to 'i').
     */
    public static final int KEYBIND_OPEN_GUI = 23;

    public KeybindingHandler(){
        super(new KeyBinding[]{new KeyBinding("Open In-Game Wiki GUI", KEYBIND_OPEN_GUI)}, new boolean[]{false});
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

    @Override
    public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd){}

    @Override
    public EnumSet<TickType> ticks(){
        return EnumSet.of(TickType.CLIENT);
    }

}
