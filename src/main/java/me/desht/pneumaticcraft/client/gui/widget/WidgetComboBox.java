package me.desht.pneumaticcraft.client.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.lib.GuiConstants;
import net.minecraft.client.gui.FontRenderer;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class WidgetComboBox extends WidgetTextField {

    private final ArrayList<String> elements = new ArrayList<>();
    private final FontRenderer fontRenderer;
    private boolean enabled = true;
    private boolean fixedOptions;
    private boolean shouldSort = true;
    private int selectedIndex = -1;
    private final int baseHeight; // unexpanded height
    private final Consumer<WidgetComboBox> pressable;
    private List<String> applicable = null;

    public WidgetComboBox(FontRenderer fontRenderer, int x, int y, int width, int height) {
        this(fontRenderer, x, y, width, height, b -> {});
    }

    public WidgetComboBox(FontRenderer fontRenderer, int x, int y, int width, int height, Consumer<WidgetComboBox> pressable) {
        super(fontRenderer, x, y, width, height);
        this.fontRenderer = fontRenderer;
        this.baseHeight = height;
        this.pressable = pressable;
    }

    public WidgetComboBox setElements(Collection<String> elements) {
        this.elements.clear();
        this.elements.addAll(elements);
        if (shouldSort) Collections.sort(this.elements);
        return this;
    }

    public WidgetComboBox setElements(String[] elements) {
        this.elements.clear();
        this.elements.ensureCapacity(elements.length);
        this.elements.addAll(Arrays.asList(elements));
        if (shouldSort) Collections.sort(this.elements);
        return this;
    }

    public WidgetComboBox setShouldSort(boolean shouldSort) {
        this.shouldSort = shouldSort;
        return this;
    }

    private List<String> getApplicableElements() {
//        if (applicable == null) {
            applicable = elements.stream()
                    .filter(element -> fixedOptions || element.toLowerCase().contains(getText().toLowerCase()))
                    .collect(Collectors.toList());
//        }
        return applicable;
    }

    @Override
    public void writeText(String textToWrite) {
        super.writeText(textToWrite);

        applicable = null; // force recalc
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTick) {
        super.renderButton(mouseX, mouseY, partialTick);

        if (enabled && isFocused()) {
            List<String> applicableElements = getApplicableElements();
            GlStateManager.translated(0, 0, 300);
            fill(x - 1, y + height + 1, x + width + 1, y + height + 3 + applicableElements.size() * fontRenderer.FONT_HEIGHT, 0xFFA0A0A0);
            fill(x,     y + height + 1, x + width,     y + height + 2 + applicableElements.size() * fontRenderer.FONT_HEIGHT, 0xFF000000);
            for (int i = 0; i < applicableElements.size(); i++) {
                String element = applicableElements.get(i);
                fontRenderer.drawStringWithShadow(fontRenderer.trimStringToWidth(element, getWidth()), x + 4, y + height + 2 + i * fontRenderer.FONT_HEIGHT, 0xE0E0E0);
            }
            fontRenderer.drawString(GuiConstants.TRIANGLE_UP, x + width - 6, y + 1, 0xc0c0c0);
            GlStateManager.translated(0, 0, -300);
        } else {
            fontRenderer.drawString(GuiConstants.TRIANGLE_DOWN, x + width - 6, y + 1, 0xc0c0c0);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (getVisible()) {
            int h = baseHeight + (isFocused() ? getApplicableElements().size() * fontRenderer.FONT_HEIGHT : 0);
            boolean flag = mouseX >= (double)this.x && mouseX < (double)(this.x + this.width)
                    && mouseY >= (double)this.y && mouseY < (double)(this.y + h);
            if (flag) {
                if (mouseY < y + height) {
                    // in the textfield itself
                    setFocused(!isFocused());
                } else {
                    // in the drop-down area
                    setFocused(false);
                    int i = ((int) mouseY - y) / fontRenderer.FONT_HEIGHT - 1;
                    if (i < getApplicableElements().size()) {
                        setText(getApplicableElements().get(i));
                        selectedIndex = i;
                        pressable.accept(this);
                    }
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (fixedOptions) return false;

        if (enabled && isFocused() && keyCode == GLFW.GLFW_KEY_TAB) { // Tab completion
            List<String> applicableElements = getApplicableElements();
            if (applicableElements.size() > 0) {
                setText(applicableElements.get(0));
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char key, int keyCode) {
        return !fixedOptions && super.charTyped(key, keyCode);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.enabled = enabled;
    }

    public WidgetComboBox setFixedOptions() {
        fixedOptions = true;
        applicable = null; // force recalc
        return this;
    }

    public int getSelectedElementIndex() {
        return selectedIndex;
    }

    public void selectElement(int index) {
        if (index >= 0 && index < elements.size()) {
            selectedIndex = index;
            setText(elements.get(index));
        }
    }
}
