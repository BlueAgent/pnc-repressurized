package me.desht.pneumaticcraft.client.gui.semiblock;

import me.desht.pneumaticcraft.client.gui.programmer.GuiProgWidgetLiquidFilter;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetLiquidFilter;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.fluids.Fluid;

public class GuiLogisticsLiquidFilter extends GuiProgWidgetLiquidFilter {
    private final Screen parentScreen;

    public GuiLogisticsLiquidFilter(Screen parentScreen) {
        super(new ProgWidgetLiquidFilter(), null);
        this.parentScreen = parentScreen;
    }

    public Fluid getFilter() {
        return progWidget.getFluid();
    }

    public void setFilter(Fluid fluid) {
        progWidget.setFluid(fluid);
    }

    @Override
    public void onClose() {
        minecraft.displayGuiScreen(parentScreen);
    }
}
