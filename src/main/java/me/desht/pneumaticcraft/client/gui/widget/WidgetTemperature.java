package me.desht.pneumaticcraft.client.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class WidgetTemperature extends Widget implements ITooltipSupplier {

    private int[] scales;
    protected final IHeatExchangerLogic logic;
    private final int minTemp, maxTemp;

    public WidgetTemperature(int x, int y, int minTemp, int maxTemp, IHeatExchangerLogic logic, int... scales) {
        super(x, y, 13, 50, "");
        this.scales = scales;
        this.logic = logic;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp - 273;
    }

    public void setScales(int... scales) {
        this.scales = scales;
    }

    public int[] getScales() {
        return scales;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTick) {
        if (this.visible) {
            GlStateManager.disableLighting();
            Minecraft.getInstance().getTextureManager().bindTexture(Textures.WIDGET_TEMPERATURE);
            GlStateManager.color4f(1, 1, 1, 1);
            AbstractGui.blit(x + 6, y, 6, 0, 7, 50, 18, 50);

            int barLength = (logic.getTemperatureAsInt() - minTemp) * 48 / maxTemp;
            barLength = MathHelper.clamp(barLength, 0, 48);
            AbstractGui.blit(x + 7, y + 1 + 48 - barLength, 13, 48 - barLength, 5, barLength, 18, 50);

            for (int scale : scales) {
                int scaleY = 48 - (scale - minTemp) * 48 / maxTemp;
                AbstractGui.blit(x, y - 1 + scaleY, 0, 0, 6, 5, 18, 50);
            }
        }
    }

    @Override
    public void addTooltip(int mouseX, int mouseY, List<String> curTip, boolean shift) {
        curTip.add("Temperature: " + (logic.getTemperatureAsInt() - 273) + "\u00b0C");
    }
}
