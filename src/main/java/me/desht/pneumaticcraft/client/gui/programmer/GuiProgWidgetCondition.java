package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.GuiCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.GuiRadioButton;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextFieldNumber;
import me.desht.pneumaticcraft.common.progwidgets.ICondition;
import me.desht.pneumaticcraft.common.progwidgets.ISidedWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetCondition;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetEntityCondition;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.util.Direction;

import java.util.ArrayList;
import java.util.List;

public class GuiProgWidgetCondition<T extends ProgWidgetCondition> extends GuiProgWidgetAreaShow<T> {

    private WidgetTextFieldNumber textField;

    public GuiProgWidgetCondition(T widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        if (isSidedWidget()) {
            for (Direction dir : Direction.VALUES) {
                String sideName = PneumaticCraftUtils.getOrientationName(dir);
                GuiCheckBox checkBox = new GuiCheckBox(guiLeft + 4, guiTop + 30 + dir.getIndex() * 12, 0xFF404040, sideName,
                        b -> ((ISidedWidget) progWidget).getSides()[dir.getIndex()] = b.checked);
                checkBox.checked = ((ISidedWidget) progWidget).getSides()[dir.getIndex()];
                addButton(checkBox);
            }
        }

        int baseX = isSidedWidget() ? 90 : 4;
        int baseY = isUsingAndOr() ? 60 : 30;

        List<GuiRadioButton> radioButtons;
        GuiRadioButton radioButton;
        if (isUsingAndOr()) {
            radioButtons = new ArrayList<>();
            radioButton = new GuiRadioButton(guiLeft + baseX, guiTop + 30, 0xFF404040, "Any block",
                    b -> progWidget.setAndFunction(false));
            radioButton.checked = !progWidget.isAndFunction();
            addButton(radioButton);
            radioButtons.add(radioButton);
            radioButton.otherChoices = radioButtons;

            radioButton = new GuiRadioButton(guiLeft + baseX, guiTop + 42, 0xFF404040, "All blocks",
                    b -> progWidget.setAndFunction(true));
            radioButton.checked = progWidget.isAndFunction();
            addButton(radioButton);
            radioButtons.add(radioButton);
            radioButton.otherChoices = radioButtons;
        }

        if (requiresNumber()) {
            radioButtons = new ArrayList<>();
            for (ICondition.Operator op : ICondition.Operator.values()) {
                radioButton = new GuiRadioButton(guiLeft + baseX, guiTop + baseY + op.ordinal() * 12, 0xFF404040,
                        op.toString(), b -> progWidget.setOperator(op));
                radioButton.checked = progWidget.getOperator() == op;
                addButton(radioButton);
                radioButtons.add(radioButton);
                radioButton.otherChoices = radioButtons;
            }

            textField = new WidgetTextFieldNumber(font, guiLeft + baseX, guiTop + baseY + 40, 50, 11);
            textField.setText(progWidget.getRequiredCount() + "");
            textField.setFocused2(true);
            textField.setResponder(s -> progWidget.setRequiredCount(textField.getValue()));
            addButton(textField);
        }
    }

    protected boolean isSidedWidget() {
        return progWidget instanceof ISidedWidget;
    }

    protected boolean isUsingAndOr() {
        return true;
    }

    protected boolean requiresNumber() {
        return true;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (isSidedWidget()) {
            font.drawString("Accessing sides:", guiLeft + 4, guiTop + 20, 0xFF404060);
        }
        String s = progWidget.getExtraStringInfo();
        font.drawString(s, guiLeft + xSize / 2f - font.getStringWidth(s) / 2f, guiTop + 120, 0xFF404060);
    }

    public static class Entity extends GuiProgWidgetCondition<ProgWidgetEntityCondition> {
        public Entity(ProgWidgetEntityCondition widget, GuiProgrammer guiProgrammer) {
            super(widget, guiProgrammer);
        }

        @Override
        protected boolean isSidedWidget() {
            return false;
        }

        @Override
        protected boolean isUsingAndOr() {
            return false;
        }
    }
}
