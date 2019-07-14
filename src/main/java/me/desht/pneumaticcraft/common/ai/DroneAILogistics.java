package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.ai.LogisticsManager.LogisticsTask;
import me.desht.pneumaticcraft.common.progwidgets.ILiquidFiltered;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetAreaItemBase;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetInventoryBase;
import me.desht.pneumaticcraft.common.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockLogistics;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import me.desht.pneumaticcraft.common.util.StreamUtils;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Stream;

public class DroneAILogistics extends Goal {
    private Goal curAI;
    private final IDroneBase drone;
    private final ProgWidgetAreaItemBase widget;
    private LogisticsTask curTask;

    public DroneAILogistics(IDroneBase drone, ProgWidgetAreaItemBase widget) {
        this.drone = drone;
        this.widget = widget;
    }

    private LogisticsManager getLogisticsManager() {
        if (drone.getLogisticsManager() == null) {
            Set<BlockPos> area = widget.getCachedAreaSet();
            if (!area.isEmpty()) {
                AxisAlignedBB aabb = ProgWidgetAreaItemBase.getExtents(area);
                Stream<ISemiBlock> semiBlocksInArea = SemiBlockManager.getInstance(drone.world()).getSemiBlocksInArea(drone.world(), aabb);
                Stream<SemiBlockLogistics> logisticFrames = StreamUtils.ofType(SemiBlockLogistics.class, semiBlocksInArea);

                LogisticsManager manager = new LogisticsManager();
                logisticFrames.filter(frame -> area.contains(frame.getPos())).forEach(manager::addLogisticFrame);
                drone.setLogisticsManager(manager);
            }
        }
        return drone.getLogisticsManager();
    }

    @Override
    public boolean shouldExecute() {
        if (getLogisticsManager() == null) return false;
        curTask = null;
        return doLogistics();
    }

    private boolean doLogistics() {
        ItemStack item = drone.getInv().getStackInSlot(0);
        FluidStack fluid = drone.getTank().getFluid();
        PriorityQueue<LogisticsTask> tasks = getLogisticsManager().getTasks(item.isEmpty() ? fluid : item);
        if (tasks.size() > 0) {
            curTask = tasks.poll();
            return execute(curTask);
        }
        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (curTask == null) return false;
        if (!curAI.shouldContinueExecuting()) {
            if (curAI instanceof DroneEntityAIInventoryImport) {
                curTask.requester.clearIncomingStack(curTask.transportingItem);
                return clearAIAndProvideAgain();
            } else if (curAI instanceof DroneAILiquidImport) {
                curTask.requester.clearIncomingStack(curTask.transportingFluid);
                return clearAIAndProvideAgain();
            } else {
                curAI = null;
                return false;
            }
        } else {
            curTask.informRequester();
            return true;
        }
    }

    private boolean clearAIAndProvideAgain() {
        curAI = null;
        if (curTask.isStillValid(drone.getInv().getStackInSlot(0).isEmpty() ? drone.getTank().getFluid() : drone.getInv().getStackInSlot(0)) && execute(curTask)) {
            return true;
        } else {
            curTask = null;
            return doLogistics();
        }
    }

    public boolean execute(LogisticsTask task) {
        if (!drone.getInv().getStackInSlot(0).isEmpty()) {
            if (hasNoPathTo(task.requester.getPos())) return false;
            curAI = new DroneEntityAIInventoryExport(drone,
                    new FakeWidgetLogistics(task.requester.getPos(), task.requester.getSide(), task.transportingItem));
        } else if (drone.getTank().getFluidAmount() > 0) {
            if (hasNoPathTo(task.requester.getPos())) return false;
            curAI = new DroneAILiquidExport(drone,
                    new FakeWidgetLogistics(task.requester.getPos(), task.requester.getSide(), task.transportingFluid.stack));
        } else if (!task.transportingItem.isEmpty()) {
            if (hasNoPathTo(task.provider.getPos())) return false;
            curAI = new DroneEntityAIInventoryImport(drone,
                    new FakeWidgetLogistics(task.provider.getPos(), task.provider.getSide(), task.transportingItem));
        } else {
            if (hasNoPathTo(task.provider.getPos())) return false;
            curAI = new DroneAILiquidImport(drone,
                    new FakeWidgetLogistics(task.provider.getPos(),  task.provider.getSide(), task.transportingFluid.stack));
        }
        if (curAI.shouldExecute()) {
            task.informRequester();
            return true;
        } else {
            return false;
        }
    }

    private boolean hasNoPathTo(BlockPos pos) {
        for (Direction d : Direction.VALUES) {
            if (drone.isBlockValidPathfindBlock(pos.offset(d))) return false;
        }
        drone.addDebugEntry("gui.progWidget.general.debug.cantNavigate", pos);
        return true;
    }

    private static class FakeWidgetLogistics extends ProgWidgetInventoryBase implements ILiquidFiltered {
        private final ItemStack stack;
        private final FluidStack fluid;
        private final Set<BlockPos> area;
        private final boolean[] sides = new boolean[6];

        FakeWidgetLogistics(BlockPos pos, Direction side, @Nonnull ItemStack stack) {
            this.stack = stack;
            this.fluid = null;
            area = new HashSet<>();
            area.add(pos);
            sides[side.getIndex()] = true;
        }

        FakeWidgetLogistics(BlockPos pos, Direction side, FluidStack fluid) {
            this.stack = ItemStack.EMPTY;
            this.fluid = fluid;
            area = new HashSet<>();
            area.add(pos);
            sides[side.getIndex()] = true;
        }

        @Override
        public String getWidgetString() {
            return null;
        }

        @Override
        public DyeColor getColor() {
            return DyeColor.WHITE;  // arbitrary
        }

        @Override
        public void getArea(Set<BlockPos> area) {
            area.addAll(this.area);
        }

        @Override
        public void setSides(boolean[] sides) {
        }

        @Override
        public boolean[] getSides() {
            return sides;
        }

        @Override
        public boolean isItemValidForFilters(@Nonnull ItemStack item) {
            return !item.isEmpty() && item.isItemEqual(stack);
        }

        @Override
        public ResourceLocation getTexture() {
            return null;
        }

        @Override
        public boolean useCount() {
            return true;
        }

        @Override
        public void setUseCount(boolean useCount) {
        }

        @Override
        public int getCount() {
            return !stack.isEmpty() ? stack.getCount() : fluid.amount;
        }

        @Override
        public void setCount(int count) {
        }

        @Override
        public boolean isFluidValid(Fluid fluid) {
            return fluid == this.fluid.getFluid();
        }

    }

}
