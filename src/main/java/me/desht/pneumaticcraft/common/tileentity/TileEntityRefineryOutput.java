package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableMap;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerRefinery;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class TileEntityRefineryOutput extends TileEntityTickableBase
        implements IHeatExchanger, IRedstoneControlled, IComparatorSupport, ISerializableTanks, ISmartFluidSync, INamedContainerProvider {

    private TileEntityRefineryController controllerTE = null;

    @DescSynced
    @LazySynced
    private final SmartSyncTank outputTank = new SmartSyncTank(this, PneumaticValues.NORMAL_TANK_CAPACITY);

    @SuppressWarnings("unused")
    @DescSynced
    private int outputAmountScaled;  // just present to cause a TE sync when fluid changes

    @GuiSynced
    private final IHeatExchangerLogic heatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().getHeatExchangerLogic();

    private final LazyOptional<IFluidHandler> fluidCap = LazyOptional.of(() -> outputTank);
    private final LazyOptional<IFluidHandler> fluidCapWrapped = LazyOptional.of(() -> new TankWrapper(outputTank));

    public TileEntityRefineryOutput() {
        super(ModTileEntities.REFINERY_OUTPUT);
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic(Direction side) {
        return heatExchanger;
    }

    @Override
    public int getComparatorValue() {
        TileEntityRefineryController controller = getRefineryController();
        return controller == null ? 0 : controller.getRedstoneMode();
    }

    @Override
    public int getRedstoneMode() {
        TileEntityRefineryController controller = getRefineryController();
        return controller == null ? 0 : controller.getRedstoneMode();
    }

    @Nonnull
    @Override
    public Map<String, FluidTank> getSerializableTanks() {
        return ImmutableMap.of("Tank", outputTank);
    }

    @Override
    public void updateScaledFluidAmount(int tankIndex, int amount) {
        outputAmountScaled = outputTank.getScaledFluidAmount();
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return null;
    }

    @Override
    public ITextComponent getDisplayName() {
        return getDisplayNameInternal();
    }

    @Nullable
    @Override
    public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
        TileEntityRefineryController controller = getRefineryController();
        return controller == null ? null : new ContainerRefinery(windowId, inventory, controller.getPos());
    }

    public TileEntityRefineryController getRefineryController() {
        if (controllerTE != null && controllerTE.isRemoved()) controllerTE = null;

        if (controllerTE == null) {
            BlockPos checkPos = this.pos;
            while (world.getBlockState(checkPos.down()).getBlock() == ModBlocks.REFINERY_OUTPUT) {
                checkPos = checkPos.down();
            }
            if (world.getBlockState(checkPos.down()).getBlock() == ModBlocks.REFINERY) {
                // refinery directly under the output stack
                controllerTE = (TileEntityRefineryController) world.getTileEntity(checkPos.down());
            } else {
                // is refinery horizontally adjacent to bottom of stack?
                for (Direction d : Direction.Plane.HORIZONTAL) {
                    if (world.getBlockState(checkPos.offset(d)).getBlock() == ModBlocks.REFINERY) {
                        controllerTE = (TileEntityRefineryController) world.getTileEntity(checkPos.offset(d));
                    }
                }
            }
        }
        return controllerTE;
    }

    public IFluidTank getOutputTank() {
        return outputTank;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return side == Direction.DOWN ? fluidCap.cast() : fluidCapWrapped.cast();
        } else {
            return super.getCapability(cap, side);
        }
    }

    @Override
    public void remove() {
        super.remove();

        fluidCap.invalidate();
    }

    private class TankWrapper implements IFluidHandler {
        private final SmartSyncTank wrapped;

        TankWrapper(SmartSyncTank wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public int getTanks() {
            return wrapped.getTanks();
        }

        @Nonnull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return wrapped.getFluidInTank(tank);
        }

        @Override
        public int getTankCapacity(int tank) {
            return wrapped.getTankCapacity(tank);
        }

        @Override
        public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
            return wrapped.isFluidValid(tank, stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;  // this tank is only for draining
        }

        @Nonnull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return wrapped.drain(resource, action);
        }

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return wrapped.drain(maxDrain, action);
        }
    }
}
