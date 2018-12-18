package me.desht.pneumaticcraft.client.gui.pneumaticHelmet;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IOptionPage;
import me.desht.pneumaticcraft.client.KeyHandler;
import net.minecraft.client.gui.GuiButton;

public class GuiLauncherOptions implements IOptionPage {
    private KeybindingButton changeKeybindingButton;

    @Override
    public String getPageName() {
        return "Item Launcher";
    }

    @Override
    public void initGui(IGuiScreen gui) {
        changeKeybindingButton = new KeybindingButton(10, 30, 128, 150, 20, "Change Launch Key...", KeyHandler.getInstance().keybindLauncher);
        gui.getButtonList().add(changeKeybindingButton);
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 10) {
            changeKeybindingButton.toggleKeybindMode();
        }
    }

    @Override
    public void drawPreButtons(int x, int y, float partialTicks) {

    }

    @Override
    public void drawScreen(int x, int y, float partialTicks) {

    }

    @Override
    public void keyTyped(char ch, int key) {
        if (changeKeybindingButton != null) changeKeybindingButton.receiveKey(key);
    }

    @Override
    public void mouseClicked(int x, int y, int button) {

    }

    @Override
    public void handleMouseInput() {

    }

    @Override
    public boolean canBeTurnedOff() {
        return false;
    }

    @Override
    public boolean displaySettingsText() {
        return false;
    }
}
