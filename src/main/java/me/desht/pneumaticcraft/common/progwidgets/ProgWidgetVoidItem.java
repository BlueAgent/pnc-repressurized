package me.desht.pneumaticcraft.common.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.ai.DroneAIVoidItem;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.List;

public class ProgWidgetVoidItem extends ProgWidget implements IItemFiltering {
    public ProgWidgetVoidItem() {
        super(ModProgWidgets.VOID_ITEM.get());
    }

    @Override
    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return new DroneAIVoidItem(drone, (IItemFiltering) widget);
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_VOID_ITEM;
    }

    @Override
    public boolean hasStepInput() {
        return true;
    }

    @Override
    public ProgWidgetType<?> returnType() {
        return null;
    }

    @Nonnull
    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgets.ITEM_FILTER.get());
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.RED;
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.EASY;
    }

    @Override
    public boolean isItemValidForFilters(ItemStack item) {
        return ProgWidgetItemFilter.isItemValidForFilters(item,
                ProgWidget.getConnectedWidgetList(this, 0, ModProgWidgets.ITEM_FILTER.get()),
                ProgWidget.getConnectedWidgetList(this, getParameters().size(), ModProgWidgets.ITEM_FILTER.get()),
                null
        );
    }
}
