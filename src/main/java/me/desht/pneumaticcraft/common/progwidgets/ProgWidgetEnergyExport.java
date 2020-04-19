package me.desht.pneumaticcraft.common.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.ai.DroneAIEnergyExport;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class ProgWidgetEnergyExport extends ProgWidgetInventoryBase {

    public ProgWidgetEnergyExport() {
        super(ModProgWidgets.RF_EXPORT.get());
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgets.AREA.get());
    }

    @Override
    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return new DroneAIEnergyExport(drone, (ProgWidgetEnergyExport) widget);
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_RF_EXPORT;
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.ORANGE;
    }
}
