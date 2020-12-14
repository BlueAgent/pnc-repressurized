package me.desht.pneumaticcraft.common.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class ProgWidgetDroneConditionPressure extends ProgWidgetDroneCondition {

    public ProgWidgetDroneConditionPressure() {
        super(ModProgWidgets.DRONE_CONDITION_PRESSURE.get());
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgets.TEXT.get());
    }

    @Override
    protected int getCount(IDroneBase drone, IProgWidget widget) {
        float pressure = drone.getCapability(PNCCapabilities.AIR_HANDLER_CAPABILITY)
                .orElseThrow(RuntimeException::new).getPressure();
        maybeRecordMeasuredVal(drone, (int)(pressure * 1000));
        return (int) pressure;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CONDITION_DRONE_PRESSURE;
    }

}
