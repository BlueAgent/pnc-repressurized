package me.desht.pneumaticcraft.common.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.ai.DroneAIBlockCondition;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class ProgWidgetEntityCondition extends ProgWidgetCondition {

    public ProgWidgetEntityCondition() {
        super(ModProgWidgets.CONDITION_ENTITY.get());
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgets.AREA.get(), ModProgWidgets.TEXT.get(), ModProgWidgets.TEXT.get());
    }

    @Override
    protected DroneAIBlockCondition getEvaluator(IDroneBase drone, IProgWidget widget) {
        return null;
    }

    @Override
    public IProgWidget getOutputWidget(IDroneBase drone, List<IProgWidget> allWidgets) {
        List<Entity> entities = getValidEntities(drone.world());
        boolean result = getOperator() == Operator.EQ ? entities.size() == getRequiredCount() : entities.size() >= getRequiredCount();
        if (result) {
            drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.condition.evaluatedTrue");
        } else {
            drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.condition.evaluatedFalse");
        }
        maybeRecordMeasuredVal(drone, entities.size());
        return ProgWidgetJump.jumpToLabel(drone, allWidgets, this, result);
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CONDITION_ENTITY;
    }
}
