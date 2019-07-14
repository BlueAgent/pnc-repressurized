package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.GuiButtonSpecial;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.common.util.JsonToNBTConverter;
import me.desht.pneumaticcraft.common.util.NBTToJsonConverter;
import me.desht.pneumaticcraft.common.util.PastebinHandler;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

public class GuiPastebin extends GuiPneumaticScreenBase {

    private WidgetTextField usernameBox, passwordBox;
    private WidgetTextField pastebinBox;
    private final String pastingString;
    CompoundNBT outputTag;
    private final Screen parentScreen;
    private String statusMessage;
    private EnumState state = EnumState.NONE;

    private enum EnumState {
        NONE, GETTING, PUTTING, LOGIN, LOGOUT
    }

    private GuiPastebin(Screen parentScreen, String pastingString) {
        super(new StringTextComponent("Pastebin"));
        xSize = 183;
        ySize = 202;
        this.pastingString = pastingString;
        this.parentScreen = parentScreen;
        minecraft.keyboardListener.enableRepeatEvents(true);
    }

    GuiPastebin(Screen parentScreen, CompoundNBT tag) {
        this(parentScreen, new NBTToJsonConverter(tag).convert(true));
    }

    @Override
    public void init() {
        super.init();
        if (!PastebinHandler.isLoggedIn()) {
            usernameBox = new WidgetTextField(font, guiLeft + 10, guiTop + 30, 80, 10);
            addButton(usernameBox);

            passwordBox = new WidgetTextField(font, guiLeft + 10, guiTop + 56, 80, 10).setAsPasswordBox();
            addButton(passwordBox);

            GuiButtonSpecial loginButton = new GuiButtonSpecial(guiLeft + 100, guiTop + 30, 60, 20, I18n.format("gui.pastebin.button.login"), b -> login());
            loginButton.setTooltipText("Pastebin login is optional");
            addButton(loginButton);

            addLabel(I18n.format("gui.pastebin.username"), guiLeft + 10, guiTop + 20);
            addLabel(I18n.format("gui.pastebin.password"), guiLeft + 10, guiTop + 46);
        } else {
            GuiButtonSpecial logoutButton = new GuiButtonSpecial(guiLeft + 60, guiTop + 30, 60, 20, I18n.format("gui.pastebin.button.logout"), b -> logout());
            addButton(logoutButton);
        }

        pastebinBox = new WidgetTextField(font, guiLeft + 10, guiTop + 130, 160, 10) {
            @Override
            protected void onFocusedChanged(boolean focused) {
                if (focused) {
                    setCursorPositionEnd();
                    setSelectionPos(0);
                }
                super.onFocusedChanged(focused);
            }
        };
        addButton(pastebinBox);

        GuiButtonSpecial pasteButton = new GuiButtonSpecial(guiLeft + 31, guiTop + 78, 120, 20, I18n.format("gui.pastebin.button.upload"), b -> sendToPastebin());
        addButton(pasteButton);
        GuiButtonSpecial getButton = new GuiButtonSpecial(guiLeft + 31, guiTop + 167, 120, 20, I18n.format("gui.pastebin.button.get"), b -> getFromPastebin());
        addButton(getButton);

        GuiButtonSpecial putInClipBoard = new GuiButtonSpecial(guiLeft + 8, guiTop + 78, 20, 20, "", b -> putToClipboard());
        putInClipBoard.setRenderedIcon(Textures.GUI_COPY_ICON_LOCATION);
        putInClipBoard.setTooltipText(I18n.format("gui.pastebin.button.copyToClipboard"));
        addButton(putInClipBoard);
        GuiButtonSpecial retrieveFromClipboard = new GuiButtonSpecial(guiLeft + 8, guiTop + 167, 20, 20, "", b -> getFromClipboard());
        retrieveFromClipboard.setRenderedIcon(Textures.GUI_PASTE_ICON_LOCATION);
        retrieveFromClipboard.setTooltipText(I18n.format("gui.pastebin.button.loadFromClipboard"));
        addButton(retrieveFromClipboard);

        addLabel(I18n.format("gui.pastebin.pastebinLink"), guiLeft + 10, guiTop + 120);
    }

    private void login() {
        PastebinHandler.login(usernameBox.getText(), passwordBox.getText());
        state = EnumState.LOGIN;
        statusMessage = I18n.format("gui.pastebin.loggingIn");
    }

    private void logout() {
        PastebinHandler.logout();
        state = EnumState.LOGOUT;
    }

    private void sendToPastebin() {
        PastebinHandler.put(pastingString);
        state = EnumState.PUTTING;
        statusMessage = I18n.format("gui.pastebin.uploadingToPastebin");
    }

    private void getFromPastebin() {
        PastebinHandler.get(pastebinBox.getText());
        state = EnumState.GETTING;
        statusMessage = I18n.format("gui.pastebin.retrievingFromPastebin");
    }

    private void putToClipboard() {
        minecraft.keyboardListener.setClipboardString(pastingString);
        statusMessage = I18n.format("gui.pastebin.clipboardSetToContents");
    }

    private void getFromClipboard() {
        readFromString(minecraft.keyboardListener.getClipboardString());
        statusMessage = I18n.format("gui.pastebin.retrievedFromClipboard");
    }

    @Override
    public void tick() {
        super.tick();
        if (state == EnumState.LOGOUT) {
            state = EnumState.NONE;
            init();
        }
        if (state != EnumState.NONE && PastebinHandler.isDone()) {
            statusMessage = "";
            String pastebinText;
            switch (state) {
                case GETTING:
                    pastebinText = PastebinHandler.getHandler().contents;
                    if (pastebinText != null) {
                        readFromString(pastebinText);
                    } else {
                        statusMessage = I18n.format("gui.pastebin.invalidPastebin");
                    }
                    break;
                case PUTTING:
                    if (PastebinHandler.getException() != null) {
                        statusMessage = PastebinHandler.getException().getMessage();
                    } else {
                        pastebinText = PastebinHandler.getHandler().getLink;
                        if (pastebinText == null) pastebinText = "<ERROR>";
                        if (pastebinText.contains("pastebin.com")) {
                            pastebinBox.setText(pastebinText);
                        } else {
                            statusMessage = pastebinText;
                        }
                    }
                    break;
                case LOGIN:
                    if (!PastebinHandler.isLoggedIn()) {
                        statusMessage = I18n.format("gui.pastebin.invalidLogin");
                    }
                    init();
            }
            state = EnumState.NONE;
        }
    }

    private void readFromString(String string) {
        try {
            outputTag = new JsonToNBTConverter(string).convert();
        } catch (Exception e) {
            e.printStackTrace();
            statusMessage = I18n.format("gui.pastebin.invalidFormattedPastebin");
        }
    }

    @Override
    public void render(int x, int y, float partialTicks) {
        renderBackground();
        super.render(x, y, partialTicks);
        if (statusMessage != null && !statusMessage.isEmpty()) font.drawString(statusMessage, guiLeft + 5, guiTop + 5, 0xFFFF0000);
    }

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            minecraft.keyboardListener.enableRepeatEvents(false);
            minecraft.displayGuiScreen(parentScreen);
            onClose();
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.GUI_PASTEBIN;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
