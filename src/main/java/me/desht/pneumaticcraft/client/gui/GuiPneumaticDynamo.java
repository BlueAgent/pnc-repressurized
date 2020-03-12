package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetEnergy;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.common.inventory.ContainerPneumaticDynamo;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDynamo;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.energy.CapabilityEnergy;

import java.util.ArrayList;
import java.util.List;

public class GuiPneumaticDynamo extends GuiPneumaticContainerBase<ContainerPneumaticDynamo,TileEntityPneumaticDynamo> {
    private WidgetAnimatedStat inputStat;

    public GuiPneumaticDynamo(ContainerPneumaticDynamo container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();
        inputStat = addAnimatedStat("Output", Textures.GUI_BUILDCRAFT_ENERGY, 0xFF555555, false);

        te.getCapability(CapabilityEnergy.ENERGY).ifPresent(storage -> addButton(new WidgetEnergy(guiLeft + 20, guiTop + 20, storage)));
        addButton(new WidgetTemperature(guiLeft + 87, guiTop + 20, 273, 675,
                te.getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY), 325, 625));
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_4UPGRADE_SLOTS;
    }

    @Override
    public void tick() {
        super.tick();
        inputStat.setText(getOutputStat());
    }

    private List<String> getOutputStat() {
        List<String> textList = new ArrayList<>();
        textList.add(TextFormatting.GRAY + "Maximum RF production:");
        textList.add(TextFormatting.BLACK.toString() + te.getRFRate() + " RF/tick");
        textList.add(TextFormatting.GRAY + "Maximum output rate:");
        textList.add(TextFormatting.BLACK.toString() + te.getRFRate() * 2 + " RF/tick");
        textList.add(TextFormatting.GRAY + "Current stored RF:");
        textList.add(TextFormatting.BLACK.toString() + te.getInfoEnergyStored() + " RF");
        return textList;
    }

    @Override
    protected void addPressureStatInfo(List<String> pressureStatText) {
        super.addPressureStatInfo(pressureStatText);
        pressureStatText.add(TextFormatting.BLACK + I18n.format("gui.tooltip.maxUsage", te.getAirRate()));
    }

    @Override
    public void addProblems(List<String> curInfo) {
        super.addProblems(curInfo);
        if (te.getEfficiency() < 100) {
            curInfo.add(I18n.format("gui.tab.problems.advancedAirCompressor.efficiency", te.getEfficiency() + "%%"));
        }
    }
}
