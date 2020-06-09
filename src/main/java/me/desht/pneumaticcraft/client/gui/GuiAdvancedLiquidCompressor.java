package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.common.inventory.ContainerLiquidCompressor;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class GuiAdvancedLiquidCompressor extends GuiLiquidCompressor {

    public GuiAdvancedLiquidCompressor(ContainerLiquidCompressor container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();
        addButton(new WidgetTemperature(guiLeft + 92, guiTop + 20, 273, 675,
                te.getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY), 325, 625));
    }

    @Override
    protected int getFluidOffset() {
        return 72;
    }

    @Override
    public void addWarnings(List<String> curInfo) {
        super.addWarnings(curInfo);

        if (te.getEfficiency() < 100) {
            curInfo.add(I18n.format("pneumaticcraft.gui.tab.problems.advancedAirCompressor.efficiency", te.getEfficiency() + "%%"));
        }
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_ADVANCED_LIQUID_COMPRESSOR;
    }

}
