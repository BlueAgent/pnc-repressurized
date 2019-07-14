package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableMap;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.config.Config;
import me.desht.pneumaticcraft.common.inventory.ContainerLiquidHopper;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import me.desht.pneumaticcraft.common.util.FluidUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.Block;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class TileEntityLiquidHopper extends TileEntityOmnidirectionalHopper implements ISerializableTanks, ISmartFluidSync {
    private int comparatorValue = -1;

    @LazySynced
    @DescSynced
    @GuiSynced
    private final HopperTank tank = new HopperTank(this, PneumaticValues.NORMAL_TANK_CAPACITY);
    private final LazyOptional<IFluidHandler> tankCap = LazyOptional.of(() -> tank);
    private final WrappedFluidTank inputWrapper = new WrappedFluidTank(tank, true);
    private final LazyOptional<IFluidHandler> inputCap = LazyOptional.of(() -> inputWrapper);
    private final WrappedFluidTank outputWrapper = new WrappedFluidTank(tank, false);
    private final LazyOptional<IFluidHandler> outputCap = LazyOptional.of(() -> outputWrapper);

    @SuppressWarnings("unused")
    @DescSynced
    private int fluidAmountScaled;

    public TileEntityLiquidHopper() {
        super();

        if (Config.Common.Machines.liquidHopperDispenser) {
            addApplicableUpgrade(EnumUpgrade.DISPENSER);
        }
    }

    @Override
    protected int getInvSize() {
        return 0;
    }

    @Override
    protected int getComparatorValueInternal() {
        if (comparatorValue < 0) {
            if (tank.getFluidAmount() == 0) return 0;
            FluidStack fluidStack = tank.getFluid();
            comparatorValue = (int) (1 + ((float) fluidStack.amount / tank.getCapacity() * 14f));
        }
        return comparatorValue;
    }

    @Override
    protected boolean doExport(int maxItems) {
        Direction dir = getRotation();

        if (tank.getFluid() != null) {
            TileEntity neighbor = getCachedNeighbor(dir);
            LazyOptional<IFluidHandler> cap = neighbor.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir.getOpposite());
            if (cap.isPresent()) {
                return cap.map(fluidHandler -> {
                    int amount = Math.min(maxItems * 100, tank.getFluid().amount - leaveMaterialCount * 1000);
                    FluidStack transferred = FluidUtil.tryFluidTransfer(fluidHandler, tank, amount, true);
                    return transferred != null && transferred.amount > 0;
                }).orElse(false);
            }
        }

        if (getWorld().isAirBlock(getPos().offset(dir))) {
            for (ItemEntity entity : getNeighborItems(this, dir)) {
                NonNullList<ItemStack> returnedItems = NonNullList.create();
                if (FluidUtils.tryFluidExtraction(tank, entity.getItem(), returnedItems)) {
                    if (entity.getItem().getCount() <= 0) entity.remove();
                    for (ItemStack stack : returnedItems) {
                        ItemEntity item = new ItemEntity(getWorld(), entity.posX, entity.posY, entity.posZ, stack);
                        item.setMotion(entity.getMotion());
                        getWorld().addEntity(item);
                    }
                    return true;
                }
            }
        }

        if (Config.Common.Machines.liquidHopperDispenser && getUpgrades(EnumUpgrade.DISPENSER) > 0) {
            if (getWorld().isAirBlock(getPos().offset(dir))) {
                FluidStack extractedFluid = tank.drain(1000, false);
                if (extractedFluid != null && extractedFluid.amount == 1000) {
                    Block fluidBlock = extractedFluid.getFluid().getBlock();
                    if (fluidBlock != null) {
                        tank.drain(1000, true);
                        getWorld().setBlockState(getPos().offset(dir), fluidBlock.getDefaultState());
                    }
                }
            }
        }

        return false;
    }

    @Override
    protected boolean doImport(int maxItems) {
        TileEntity inputInv = getCachedNeighbor(inputDir);

        if (inputInv != null) {
            LazyOptional<IFluidHandler> cap = inputInv.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, inputDir.getOpposite());
            if (cap.isPresent()) {
                return cap.map(fluidHandler -> {
                    FluidStack fluid = fluidHandler.drain(maxItems * 100, false);
                    if (fluid != null) {
                        int filledFluid = tank.fill(fluid, true);
                        if (filledFluid > 0) {
                            fluidHandler.drain(filledFluid, true);
                            return true;
                        }
                    }
                    return false;
                }).orElse(false);
            }
        }

        if (getWorld().isAirBlock(getPos().offset(inputDir))) {
            for (ItemEntity entity : getNeighborItems(this, inputDir)) {
                NonNullList<ItemStack> returnedItems = NonNullList.create();
                if (FluidUtils.tryFluidInsertion(tank, entity.getItem(), returnedItems)) {
                    if (entity.getItem().isEmpty()) entity.remove();
                    for (ItemStack stack : returnedItems) {
                        ItemEntity item = new ItemEntity(getWorld(), entity.posX, entity.posY, entity.posZ, stack);
                        item.setMotion(entity.getMotion());
                        getWorld().addEntity(item);
                    }
                    return true;
                }
            }
        }

        if (Config.Common.Machines.liquidHopperDispenser && getUpgrades(EnumUpgrade.DISPENSER) > 0) {
            BlockPos neighborPos = getPos().offset(inputDir);
            FluidStack fluidStack = FluidUtils.getFluidAt(getWorld(), neighborPos, false);
            if (fluidStack != null && fluidStack.amount == Fluid.BUCKET_VOLUME) {
                if (tank.fill(fluidStack, false) == Fluid.BUCKET_VOLUME) {
                    tank.fill(fluidStack, true);
                    FluidUtils.getFluidAt(getWorld(), neighborPos, true);
                    return true;
                }
            }
        }

        return false;
    }

    public FluidTank getTank() {
        return tank;
    }

    public Direction getInputDirection() {
        return inputDir;
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);

        fluidAmountScaled = tank.getScaledFluidAmount();
        comparatorValue = -1;
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return null;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            if (facing == inputDir) {
                return inputCap.cast();
            } else if (facing == getRotation()) {
                return outputCap.cast();
            } else {
                return tankCap.cast();
            }
        } else {
            return super.getCapability(capability, facing);
        }
    }

    @Override
    public LazyOptional<IItemHandlerModifiable> getInventoryCap() {
        return LazyOptional.empty();
    }

    @Nonnull
    @Override
    public Map<String, FluidTank> getSerializableTanks() {
        return ImmutableMap.of("Tank", tank);
    }

    @Override
    public void updateScaledFluidAmount(int tankIndex, int amount) {
        fluidAmountScaled = amount;
    }

    @Override
    protected void onUpgradesChanged() {
        super.onUpgradesChanged();

        if (world != null && !world.isRemote && getUpgrades(EnumUpgrade.CREATIVE) > 0) {
            FluidStack fluidStack = tank.getFluid();
            if (fluidStack != null && fluidStack.amount > 0) {
                tank.setFluid(new FluidStack(fluidStack.getFluid(), PneumaticValues.NORMAL_TANK_CAPACITY));
            }
        }
    }

    @Override
    public ITextComponent getDisplayName() {
        return getDisplayNameInternal();
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerLiquidHopper(i, playerInventory, getPos());
    }

    class HopperTank extends SmartSyncTank {
        HopperTank(ISmartFluidSync holder, int capacity) {
            super(holder, capacity);
        }

        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            comparatorValue = -1;
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            int filled = super.fill(resource, doFill);
            if (isCreative && getFluidAmount() > 0 && getFluid().getFluid() == resource.getFluid()) {
                return resource.amount;   // acts like an infinite fluid sink
            } else {
                return filled;
            }
        }

        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            return super.drain(resource, !isCreative && doDrain);
        }

        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return super.drain(maxDrain, !isCreative && doDrain);
        }
    }

    class WrappedFluidTank implements IFluidTank, IFluidHandler {
        private final FluidTank wrappedTank;
        private final boolean inbound;

        WrappedFluidTank(FluidTank wrappedTank, boolean inbound) {
            // inbound == true: fill *only*, inbound == false: drain *only*
            this.wrappedTank = wrappedTank;
            this.inbound = inbound;
        }

        @Nullable
        @Override
        public FluidStack getFluid() {
            return wrappedTank.getFluid();
        }

        @Override
        public int getFluidAmount() {
            return wrappedTank.getFluidAmount();
        }

        @Override
        public int getCapacity() {
            return wrappedTank.getCapacity();
        }

        @Override
        public FluidTankInfo getInfo() {
            return wrappedTank.getInfo();
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            return wrappedTank.getTankProperties();
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return inbound ? wrappedTank.fill(resource, doFill) : 0;
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            return inbound ? null : tank.drain(resource, doDrain);
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return inbound ? null : wrappedTank.drain(maxDrain, doDrain);
        }
    }
}
