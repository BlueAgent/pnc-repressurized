package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.semiblock.*;
import me.desht.pneumaticcraft.common.semiblock.IProvidingInventoryListener.TileEntityAndFace;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockLogistics.FluidStackWrapper;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.IntStream;

public class LogisticsManager {

    @SuppressWarnings("unchecked")
    private final List<SemiBlockLogistics>[] logistics = new List[4];  // 4 priority levels

    public LogisticsManager() {
        for (int i = 0; i < logistics.length; i++) {
            logistics[i] = new ArrayList<>();
        }
    }

    void clearLogistics() {
        for (List<SemiBlockLogistics> list : logistics) {
            list.clear();
        }
    }

    public void addLogisticFrame(SemiBlockLogistics frame) {
        logistics[frame.getPriority()].add(frame);
    }

    public PriorityQueue<LogisticsTask> getTasks(Object holdingStack) {
        ItemStack item = holdingStack instanceof ItemStack ? (ItemStack) holdingStack : null;
        FluidStack fluid = holdingStack instanceof FluidStack ? (FluidStack) holdingStack : null;
        PriorityQueue<LogisticsTask> tasks = new PriorityQueue<>();
        for (int priority = logistics.length - 1; priority >= 0; priority--) {
            for (SemiBlockLogistics requester : logistics[priority]) {
                for (int i = 0; i < priority; i++) {
                    for (SemiBlockLogistics provider : logistics[i]) {
                        if (provider.shouldProvideTo(priority)) {
                            if (item != null) {
                                int requestedAmount = getRequestedAmount(requester, item);
                                if (requestedAmount > 0) {
                                    ItemStack stack = item.copy();
                                    stack.setCount(requestedAmount);
                                    tasks.add(new LogisticsTask(provider, requester, stack));
                                    return tasks;
                                }
                            } else if (fluid != null) {
                                int requestedAmount = getRequestedAmount(requester, fluid);
                                if (requestedAmount > 0) {
                                    fluid = fluid.copy();
                                    fluid.setAmount(requestedAmount);
                                    tasks.add(new LogisticsTask(provider, requester, new FluidStackWrapper(fluid)));
                                    return tasks;
                                }
                            } else {
                                tryProvide(provider, requester, tasks);
                            }
                        }
                    }
                }
            }
        }
        return tasks;
    }

    private void tryProvide(SemiBlockLogistics provider, SemiBlockLogistics requester, PriorityQueue<LogisticsTask> tasks) {
        if (provider.getTileEntity() == null) return;

        IOHelper.getInventoryForTE(provider.getTileEntity(), provider.getSide()).ifPresent(providingInventory -> {
            if (requester instanceof IProvidingInventoryListener)
                ((IProvidingInventoryListener) requester).notify(new TileEntityAndFace(provider.getTileEntity(), provider.getSide()));
            for (int i = 0; i < providingInventory.getSlots(); i++) {
                ItemStack providingStack = providingInventory.getStackInSlot(i);
                if (!providingStack.isEmpty() && (!(provider instanceof ISpecificProvider) || ((ISpecificProvider) provider).canProvide(providingStack))) {
                    int requestedAmount = getRequestedAmount(requester, providingStack);
                    if (requestedAmount > 0) {
                        ItemStack stack = providingStack.copy();
                        stack.setCount(requestedAmount);
                        tasks.add(new LogisticsTask(provider, requester, stack));
                    }
                }
            }
        });

        provider.getTileEntity().getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, provider.getSide()).ifPresent(fluidHandler -> {
            FluidStack providingStack = fluidHandler.drain(16000, IFluidHandler.FluidAction.SIMULATE);
            if (!providingStack.isEmpty()) {
                boolean canDrain = IntStream.range(0, fluidHandler.getTanks()).anyMatch(i -> fluidHandler.isFluidValid(i, providingStack));
                if (canDrain &&
                        (!(provider instanceof ISpecificProvider) || ((ISpecificProvider) provider).canProvide(providingStack))) {
                    int requestedAmount = getRequestedAmount(requester, providingStack);
                    if (requestedAmount > 0) {
                        FluidStack stack = providingStack.copy();
                        stack.setAmount(requestedAmount);
                        tasks.add(new LogisticsTask(provider, requester, new FluidStackWrapper(stack)));
                    }
                }
            }
        });
    }

    private static int getRequestedAmount(SemiBlockLogistics requester, ItemStack providingStack) {
        TileEntity te = requester.getTileEntity();
        if (te == null) return 0;

        int requestedAmount = requester instanceof ISpecificRequester ? ((ISpecificRequester) requester).amountRequested(providingStack) : providingStack.getMaxStackSize();
        int minOrderSize = requester instanceof SemiBlockRequester ? ((SemiBlockRequester) requester).getMinItemOrderSize() : 1;
        if (requestedAmount < minOrderSize) return 0;
        providingStack = providingStack.copy();
        providingStack.setCount(requestedAmount);
        ItemStack remainder = providingStack.copy();
        remainder.grow(requester.getIncomingItems(providingStack));
        remainder = IOHelper.insert(te, remainder, requester.getSide(), true);
        providingStack.shrink(remainder.getCount());
        if (providingStack.getCount() <= 0) return 0;
        return providingStack.getCount();
    }

    private static int getRequestedAmount(SemiBlockLogistics requester, FluidStack providingStack) {
        int requestedAmount = requester instanceof ISpecificRequester ? ((ISpecificRequester) requester).amountRequested(providingStack) : providingStack.getAmount();
        int minOrderSize = requester instanceof SemiBlockRequester ? ((SemiBlockRequester) requester).getMinFluidOrderSize() : 1;
        if (requestedAmount < minOrderSize) return 0;
        providingStack = providingStack.copy();
        providingStack.setAmount(requestedAmount);
        FluidStack remainder = providingStack.copy();
        remainder.grow(requester.getIncomingFluid(remainder.getFluid()));
        if (requester.getTileEntity() == null) return 0;
        requester.getTileEntity().getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, requester.getSide()).ifPresent(fluidHandler -> {
            int fluidFilled = fluidHandler.fill(remainder, IFluidHandler.FluidAction.SIMULATE);
            if (fluidFilled > 0) {
                remainder.shrink(fluidFilled);
            }
        });
        providingStack.shrink(remainder.getAmount());
        return providingStack.getAmount();
    }

    public static class LogisticsTask implements Comparable<LogisticsTask> {

        public final SemiBlockLogistics provider, requester;
        @Nonnull
        public final ItemStack transportingItem;
        public final FluidStackWrapper transportingFluid;

        LogisticsTask(SemiBlockLogistics provider, SemiBlockLogistics requester, @Nonnull ItemStack transportingItem) {
            this.provider = provider;
            this.requester = requester;
            this.transportingItem = transportingItem;
            transportingFluid = null;
        }

        LogisticsTask(SemiBlockLogistics provider, SemiBlockLogistics requester,
                      FluidStackWrapper transportingFluid) {
            this.provider = provider;
            this.requester = requester;
            this.transportingFluid = transportingFluid;
            transportingItem = ItemStack.EMPTY;
        }

        void informRequester() {
            if (!transportingItem.isEmpty()) {
                requester.informIncomingStack(transportingItem);
            } else {
                requester.informIncomingStack(transportingFluid);
            }
        }

        public boolean isStillValid(Object stack) {
            if (!transportingItem.isEmpty() && stack instanceof ItemStack) {
                int requestedAmount = getRequestedAmount(requester, (ItemStack) stack);
                return requestedAmount == ((ItemStack) stack).getCount();
            } else if (transportingFluid != null && stack instanceof FluidStack) {
                int requestedAmount = getRequestedAmount(requester, (FluidStack) stack);
                return requestedAmount == ((FluidStack) stack).getAmount();
            } else {
                return false;
            }
        }

        @Override
        public int compareTo(LogisticsTask task) {
            int value = !transportingItem.isEmpty() ? transportingItem.getCount() * 100 : transportingFluid.stack.getAmount();
            int otherValue = !task.transportingItem.isEmpty() ? task.transportingItem.getCount() * 100 : task.transportingFluid.stack.getAmount();
            return otherValue - value;
        }

    }
}
