package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.GuiCheckBox;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetBlockRightClick;
import net.minecraft.client.resources.I18n;

public class GuiProgWidgetBlockRightClick extends GuiProgWidgetPlace<ProgWidgetBlockRightClick> {
    private GuiCheckBox checkboxSneaking;

    public GuiProgWidgetBlockRightClick(ProgWidgetBlockRightClick widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();
        checkboxSneaking = new GuiCheckBox(guiLeft + 100, guiTop + 20, 0xFF404040,
                I18n.format("gui.progWidget.blockRightClick.sneaking"));
        checkboxSneaking.setChecked(progWidget.isSneaking());
        checkboxSneaking.setTooltip(I18n.format("gui.progWidget.blockRightClick.sneaking.tooltip"));
        addButton(checkboxSneaking);
    }

    @Override
    public void onClose() {
        super.onClose();
        progWidget.setSneaking(checkboxSneaking.checked);
    }
}
