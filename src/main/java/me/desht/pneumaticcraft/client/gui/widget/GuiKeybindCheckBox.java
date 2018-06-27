package me.desht.pneumaticcraft.client.gui.widget;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.UpgradeRenderHandlerList;
import me.desht.pneumaticcraft.common.config.HelmetWidgetDefaults;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketToggleHelmetFeature;
import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.proxy.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiKeybindCheckBox extends GuiCheckBox {
    public static final String UPGRADE_PREFIX = "pneumaticHelmet.upgrade.";

    private boolean isAwaitingKey;
    private String oldCheckboxText;
    private KeyBinding keyBinding;
    private final String keyBindingName;
    private static final Map<String, GuiKeybindCheckBox> trackedCheckboxes = new HashMap<>();
    private static GuiKeybindCheckBox coreComponents;

    public GuiKeybindCheckBox(int id, int x, int y, int color, String text) {
        this(id, x, y, color, text, text);
    }

    public GuiKeybindCheckBox(int id, int x, int y, int color, String text, String keyBindingName) {
        super(id, x, y, color, text);
        this.keyBindingName = keyBindingName;
        keyBinding = setOrAddKeybind(keyBindingName, -1, KeyModifier.NONE); //get the saved value.
        if (!trackedCheckboxes.containsKey(keyBindingName)) {
            checked = HelmetWidgetDefaults.INSTANCE.getKey(keyBindingName);
            trackedCheckboxes.put(keyBindingName, this);
            MinecraftForge.EVENT_BUS.register(this);
            if (keyBindingName.equals(UPGRADE_PREFIX + "coreComponents")) {
                // stash this one since it's referenced a lot
                coreComponents = this;
            }
        } else {
            checked = trackedCheckboxes.get(keyBindingName).checked;
        }
    }

    public static GuiKeybindCheckBox getCoreComponents() {
        return coreComponents;
    }

    public static GuiKeybindCheckBox fromKeyBindingName(String name) {
        return trackedCheckboxes.get(name);
    }

    @Override
    public void onMouseClicked(int mouseX, int mouseY, int button) {
        if (button == 0) {
            super.onMouseClicked(mouseX, mouseY, button);
            GuiKeybindCheckBox trackedBox = trackedCheckboxes.get(keyBindingName);
            if (trackedBox != this) {
                trackedBox.onMouseClicked(mouseX, mouseY, button);
            } else {
                HelmetWidgetDefaults.INSTANCE.setKey(keyBindingName, checked);
                try {
                    HelmetWidgetDefaults.INSTANCE.writeToFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for (EntityEquipmentSlot slot : UpgradeRenderHandlerList.ARMOR_SLOTS) {
                    List<IUpgradeRenderHandler> renderHandlers = UpgradeRenderHandlerList.instance().getHandlersForSlot(slot);
                    for (int i = 0; i < renderHandlers.size(); i++) {
                        IUpgradeRenderHandler upgradeRenderHandler = renderHandlers.get(i);
                        if ((UPGRADE_PREFIX + upgradeRenderHandler.getUpgradeName()).equals(keyBindingName)) {
                            NetworkHandler.sendToServer(new PacketToggleHelmetFeature((byte) i, coreComponents.checked && checked, slot));
                        }
                    }
                    if (keyBindingName.equals(UPGRADE_PREFIX + "coreComponents")) {
                        for (int i = 0; i < renderHandlers.size(); i++) {
                            NetworkHandler.sendToServer(new PacketToggleHelmetFeature((byte) i, checked && GuiKeybindCheckBox.fromKeyBindingName(GuiKeybindCheckBox.UPGRADE_PREFIX + renderHandlers.get(i).getUpgradeName()).checked, slot));
                        }
                    }
                }
            }
        } else {
            isAwaitingKey = !isAwaitingKey;
            if (isAwaitingKey) {
                oldCheckboxText = text;
                text = "gui.setKeybind";
            } else {
                text = oldCheckboxText;
            }
        }
    }

    @Override
    public boolean onKey(char key, int keyCode) {
        if (isAwaitingKey) {
            if (KeyModifier.isKeyCodeModifier(keyCode)) {
                return true;
            } else {
                isAwaitingKey = false;
                keyBinding = setOrAddKeybind(keyBindingName, keyCode, KeyModifier.getActiveModifier());
                text = oldCheckboxText;
                if (trackedCheckboxes.containsKey(keyBindingName)) {
                    MinecraftForge.EVENT_BUS.unregister(trackedCheckboxes.get(keyBindingName));
                }
                MinecraftForge.EVENT_BUS.register(this);
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = FMLClientHandler.instance().getClient();
        if (mc.inGameHasFocus && keyBinding != null && keyBinding.isPressed()) {
            onMouseClicked(0, 0, 0);
            HUDHandler.instance().addMessage(I18n.format("pneumaticHelmet.message." + (checked ? "enable" : "disable") + "Setting", I18n.format(text)), new ArrayList<>(), 60, 0x7000AA00);
        }
    }

    /**
     * @param keybindName name of the key binding
     * @param keyCode     when < 0, this will function as a getter & may return null
     * @param modifier key modifier (may be NONE)
     * @return the key binding
     */
    private static KeyBinding setOrAddKeybind(String keybindName, int keyCode, KeyModifier modifier) {
        GameSettings gameSettings = FMLClientHandler.instance().getClient().gameSettings;
        for (KeyBinding keyBinding : gameSettings.keyBindings) {
            if (keyBinding != null && keyBinding.getKeyDescription().equals(keybindName)) {
                if (keyCode >= 0) {
                    keyBinding.setKeyModifierAndCode(modifier, keyCode);
                    KeyBinding.resetKeyBindingArrayAndHash();
                    gameSettings.saveOptions();
                }
                return keyBinding;
            }
        }
        // If the keybind wasn't registered yet, look for it in the Minecraft options.txt file (which
        // we scanned in ClientProxy#getAllKeybindsFromOptionsFile() during pre-init)
        if (keyCode < 0) {
            if (((ClientProxy) PneumaticCraftRepressurized.proxy).keybindToKeyCodes.containsKey(keybindName)) {
                Pair<Integer,KeyModifier> binding = ((ClientProxy) PneumaticCraftRepressurized.proxy).keybindToKeyCodes.get(keybindName);
                keyCode = binding.getLeft();
                modifier = binding.getRight();
            } else {
                return null;
            }
        }
        KeyBinding keyBinding = new KeyBinding(keybindName, KeyConflictContext.IN_GAME, modifier, keyCode, Names.PNEUMATIC_KEYBINDING_CATEGORY);
        ClientRegistry.registerKeyBinding(keyBinding);
        KeyBinding.resetKeyBindingArrayAndHash();
        gameSettings.saveOptions();
        return keyBinding;
    }

    @Override
    public void addTooltip(int mouseX, int mouseY, List<String> curTooltip, boolean shiftPressed) {
        if (keyBinding != null) {
            String s = keyBinding.getKeyModifier() != KeyModifier.NONE ? keyBinding.getKeyModifier() + " + " : "";
            curTooltip.add(I18n.format("gui.keybindBoundKey", s + Keyboard.getKeyName(keyBinding.getKeyCode())));
        } else if (!isAwaitingKey) {
            curTooltip.add("gui.keybindRightClickToSet");
        }
    }
}
