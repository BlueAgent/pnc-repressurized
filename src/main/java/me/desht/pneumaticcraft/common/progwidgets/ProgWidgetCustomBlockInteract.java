package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.api.drone.ICustomBlockInteract;
import me.desht.pneumaticcraft.common.ai.DroneAICustomBlockInteract;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;

public class ProgWidgetCustomBlockInteract extends ProgWidgetInventoryBase {

    private ICustomBlockInteract interactor;

    public ProgWidgetCustomBlockInteract setInteractor(ICustomBlockInteract interactor) {
        this.interactor = interactor;
        return this;
    }

    @Override
    public IProgWidget copy() {
        ProgWidgetCustomBlockInteract widget = (ProgWidgetCustomBlockInteract) super.copy();
        widget.setInteractor(interactor);
        return widget;
    }

    @Override
    public String getWidgetString() {
        return interactor.getName();
    }

    @Override
    public ResourceLocation getTexture() {
        return interactor.getTexture();
    }

    @Override
    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return new DroneAICustomBlockInteract(drone, (ProgWidgetInventoryBase) widget, interactor);
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters() {
        return new Class[]{ProgWidgetArea.class};
    }

    @Override
    public DyeColor getColor() {
        return interactor.getColor();
    }

}
