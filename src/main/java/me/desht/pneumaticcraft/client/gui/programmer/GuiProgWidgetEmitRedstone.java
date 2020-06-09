package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetEmitRedstone;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.Direction;

public class GuiProgWidgetEmitRedstone extends GuiProgWidgetOptionBase<ProgWidgetEmitRedstone> {

    public GuiProgWidgetEmitRedstone(ProgWidgetEmitRedstone widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        for (Direction dir : Direction.VALUES) {
            String sideName = ClientUtils.translateDirection(dir);
            WidgetCheckBox checkBox = new WidgetCheckBox(guiLeft + 8, guiTop + 30 + dir.getIndex() * 12, 0xFF404040, sideName,
                    b -> progWidget.getSides()[dir.getIndex()] = b.checked);
            checkBox.checked = progWidget.getSides()[dir.getIndex()];
            addButton(checkBox);
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        font.drawString(I18n.format("pneumaticcraft.gui.progWidget.general.affectingSides"), guiLeft + 8, guiTop + 20, 0xFF604040);
    }
}
