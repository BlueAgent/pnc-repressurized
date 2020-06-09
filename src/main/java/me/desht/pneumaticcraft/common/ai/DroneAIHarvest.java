package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.api.harvesting.HarvestHandler;
import me.desht.pneumaticcraft.api.harvesting.HoeHandler;
import me.desht.pneumaticcraft.common.core.ModRegistries;
import me.desht.pneumaticcraft.common.progwidgets.IToolUser;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetAreaItemBase;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.function.Consumer;

public class DroneAIHarvest<W extends ProgWidgetAreaItemBase & IToolUser> extends DroneAIBlockInteraction<W> {
    public DroneAIHarvest(IDroneBase drone, W widget) {
        super(drone, widget);
    }

    @Override
    public boolean shouldExecute() {
        if (abortIfRequiredHoeIsMissing()) return false;
        return super.shouldExecute();
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        if (abortIfRequiredHoeIsMissing()) return false;
        return getApplicableHandler(pos) != null;
    }

    private boolean abortIfRequiredHoeIsMissing() {
        if (progWidget.requiresTool() && getDamageableHoe() == null) {
            abort();
            drone.addDebugEntry("pneumaticcraft.gui.progWidget.harvest.debug.missingHoe");
            return true;
        } else {
            return false;
        }
    }

    private HarvestHandler getApplicableHandler(BlockPos pos) {
        BlockState state = worldCache.getBlockState(pos);
        return ModRegistries.HARVEST_HANDLERS.getValues()
                .stream()
                .filter(handler -> handler.canHarvest(drone.world(), worldCache, pos, state, drone) &&
                        hasApplicableItemFilters(handler, pos, state))
                .findFirst()
                .orElse(null);
    }

    private boolean hasApplicableItemFilters(HarvestHandler harvestHandler, BlockPos pos, BlockState blockState) {
        List<ItemStack> droppedStacks = harvestHandler.addFilterItems(drone.world(), worldCache, pos, blockState, drone);
        return droppedStacks.stream()
                .anyMatch(droppedStack -> progWidget.isItemValidForFilters(droppedStack, blockState));
    }

    @Override
    protected boolean respectClaims() {
        return true;
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double distToBlock) {
        HarvestHandler applicableHandler = getApplicableHandler(pos);
        if (applicableHandler != null) {
            BlockState state = worldCache.getBlockState(pos);
            if (applicableHandler.canHarvest(drone.world(), worldCache, pos, state, drone)) {
                Consumer<PlayerEntity> damageableHoe = getDamageableHoe();
                if (damageableHoe != null) {
                    if (applicableHandler.harvestAndReplant(drone.world(), worldCache, pos, state, drone)) {
                        damageableHoe.accept(drone.getFakePlayer());
                    }
                } else {
                    applicableHandler.harvest(drone.world(), worldCache, pos, state, drone);
                }

            }
        }
        return false;
    }

    private Consumer<PlayerEntity> getDamageableHoe() {
        for (int i = 0; i < drone.getInv().getSlots(); i++) {
            ItemStack stack = drone.getInv().getStackInSlot(i);
            HoeHandler handler = ModRegistries.HOE_HANDLERS.getValues().stream()
                    .filter(hoeHandler -> hoeHandler.test(stack))
                    .findFirst().orElse(null);
            if (handler != null) return handler.getConsumer(stack);
        }
        return null;
    }
}
