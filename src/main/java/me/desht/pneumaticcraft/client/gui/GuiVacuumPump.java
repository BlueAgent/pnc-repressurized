package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.inventory.ContainerVacuumPump;
import me.desht.pneumaticcraft.common.tileentity.TileEntityVacuumPump;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.GuiConstants;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class GuiVacuumPump extends GuiPneumaticContainerBase<ContainerVacuumPump,TileEntityVacuumPump> {

    public GuiVacuumPump(ContainerVacuumPump container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_VACUUM_PUMP_LOCATION;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);

        font.drawString("+", 32, 47, 0xFF00AA00);
        font.drawString("-", 138, 47, 0xFFFF0000);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y) {
        super.drawGuiContainerBackgroundLayer(opacity, x, y);

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        float pressure = te.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, te.getInputSide())
                .map(IAirHandlerMachine::getPressure)
                .orElseThrow(RuntimeException::new);
        GuiUtils.drawPressureGauge(font, -1, PneumaticValues.MAX_PRESSURE_VACUUM_PUMP, PneumaticValues.DANGER_PRESSURE_VACUUM_PUMP, PneumaticValues.MIN_PRESSURE_VACUUM_PUMP, pressure, xStart + xSize / 5, yStart + ySize / 5 + 4);

        float vacPressure = te.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, te.getVacuumSide())
                .map(IAirHandlerMachine::getPressure)
                .orElseThrow(RuntimeException::new);
        GuiUtils.drawPressureGauge(font, -1, PneumaticValues.MAX_PRESSURE_VACUUM_PUMP, PneumaticValues.DANGER_PRESSURE_VACUUM_PUMP, -1, vacPressure, xStart + xSize * 4 / 5, yStart + ySize / 5 + 4);
    }

    @Override
    protected PointXY getGaugeLocation() {
        return null;
    }

    @Override
    protected void addPressureStatInfo(List<String> pressureStatText) {
        IAirHandlerMachine inputAirHandler = te.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, te.getInputSide())
                .orElseThrow(RuntimeException::new);
        IAirHandlerMachine vacuumHandler = te.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, te.getVacuumSide())
                .orElseThrow(RuntimeException::new);

        String col = TextFormatting.BLACK.toString();

        pressureStatText.add(col + I18n.format("gui.tab.status.vacuumPump.inputPressure",
                PneumaticCraftUtils.roundNumberTo(inputAirHandler.getPressure(), 2)));
        pressureStatText.add(col + I18n.format("gui.tab.status.vacuumPump.vacuumPressure",
                PneumaticCraftUtils.roundNumberTo(vacuumHandler.getPressure(), 2)));
        pressureStatText.add(col + I18n.format("gui.tab.status.vacuumPump.inputAir",
                String.format("%,d", inputAirHandler.getAir())));
        pressureStatText.add(col + I18n.format("gui.tab.status.vacuumPump.vacuumAir",
                String.format("%,d", vacuumHandler.getAir())));

        int volume = inputAirHandler.getVolume();
        int upgrades = te.getUpgrades(EnumUpgrade.VOLUME);
        pressureStatText.add(col + I18n.format("gui.tooltip.baseVolume",
                String.format("%,d", PneumaticValues.VOLUME_VACUUM_PUMP)));
        pressureStatText.add(col + I18n.format("gui.tooltip.effectiveVolume",
                String.format("%,d", volume)));
        if (volume > inputAirHandler.getBaseVolume()) {
            pressureStatText.add(col + GuiConstants.TRIANGLE_RIGHT + " "
                    + upgrades + " x " + EnumUpgrade.VOLUME.getItemStack().getDisplayName().getFormattedText());
            pressureStatText.add(col + I18n.format("gui.tooltip.effectiveVolume", String.format("%,d",volume)));
        }

        if (te.turning) {
            int suction = Math.round(PneumaticValues.PRODUCTION_VACUUM_PUMP * te.getSpeedMultiplierFromUpgrades());
            pressureStatText.add(col + I18n.format("gui.tooltip.suction", String.format("%,d", suction)));
        }
    }

    @Override
    protected void addProblems(List<String> textList) {
        super.addProblems(textList);
        float pressure = te.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, te.getInputSide())
                .map(IAirHandlerMachine::getPressure).orElseThrow(RuntimeException::new);
        if (pressure < PneumaticValues.MIN_PRESSURE_VACUUM_PUMP) {
            textList.add("gui.tab.problems.notEnoughPressure");
            textList.add(I18n.format("gui.tab.problems.applyPressure", PneumaticValues.MIN_PRESSURE_VACUUM_PUMP));
        }
    }

}
