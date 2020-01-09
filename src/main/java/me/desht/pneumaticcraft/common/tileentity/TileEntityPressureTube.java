package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.api.tileentity.IAirListener;
import me.desht.pneumaticcraft.api.tileentity.IManoMeasurable;
import me.desht.pneumaticcraft.common.block.BlockPressureTube;
import me.desht.pneumaticcraft.common.block.tubes.IInfluenceDispersing;
import me.desht.pneumaticcraft.common.block.tubes.ModuleRegistrator;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;
import java.util.List;

public class TileEntityPressureTube extends TileEntityPneumaticBase implements IAirListener, IManoMeasurable, ICamouflageableTE {
    @DescSynced
    public final boolean[] sidesClosed = new boolean[6];
    public TubeModule[] modules = new TubeModule[6];
    @DescSynced
    private ItemStack camoStack = ItemStack.EMPTY;
    private BlockState camoState;
    private AxisAlignedBB renderBoundingBox = null;
    private Direction inLineModuleDir = null;  // only one inline module allowed

    public TileEntityPressureTube() {
        this(ModTileEntities.PRESSURE_TUBE, PneumaticValues.DANGER_PRESSURE_PRESSURE_TUBE, PneumaticValues.MAX_PRESSURE_PRESSURE_TUBE, PneumaticValues.VOLUME_PRESSURE_TUBE, 0);
    }

    TileEntityPressureTube(TileEntityType type, float dangerPressurePressureTube, float maxPressurePressureTube, int volumePressureTube, int upgradeSlots) {
        super(type, dangerPressurePressureTube, maxPressurePressureTube, volumePressureTube, upgradeSlots);
    }

    @Override
    public void read(CompoundNBT nbt) {
        super.read(nbt);

        byte closed = nbt.getByte("sidesClosed");
        for (int i = 0; i < 6; i++) {
            sidesClosed[i] = ((closed & 1 << i) != 0);
        }
        camoStack = ICamouflageableTE.readCamoStackFromNBT(nbt);
        camoState = ICamouflageableTE.getStateForStack(camoStack);
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        super.write(nbt);

        byte connected = 0, closed = 0;
        for (int i = 0; i < 6; i++) {
            if (sidesClosed[i]) closed |= 1 << i;
        }
        nbt.putByte("sidesConnected", connected);
        nbt.putByte("sidesClosed", closed);
        ICamouflageableTE.writeCamoStackToNBT(camoStack, nbt);
        return nbt;
    }

    @Override
    public void writeToPacket(CompoundNBT tag) {
        super.writeToPacket(tag);
        writeModulesToNBT(tag);
    }

    public void writeModulesToNBT(CompoundNBT tag) {
        ListNBT moduleList = new ListNBT();
        for (int i = 0; i < modules.length; i++) {
            if (modules[i] != null) {
                CompoundNBT moduleTag = new CompoundNBT();
                moduleTag.putString("type", modules[i].getType().toString());
                modules[i].writeToNBT(moduleTag);
                moduleTag.putInt("side", i);
                moduleList.add(moduleTag);
            }
        }
        tag.put("modules", moduleList);
    }

    @Override
    public void readFromPacket(CompoundNBT tag) {
        super.readFromPacket(tag);
        modules = new TubeModule[6];
        ListNBT moduleList = tag.getList("modules", 10);
        for (int i = 0; i < moduleList.size(); i++) {
            CompoundNBT moduleTag = moduleList.getCompound(i);
            TubeModule module = ModuleRegistrator.createModule(new ResourceLocation(moduleTag.getString("type")));
            if (module != null) {
                module.readFromNBT(moduleTag);
                setModule(module, Direction.byIndex(moduleTag.getInt("side")));
            }
        }
        updateRenderBoundingBox();
        if (hasWorld() && getWorld().isRemote) {
            rerenderTileEntity();
        }
    }

    private void updateRenderBoundingBox() {
        renderBoundingBox = new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX() + 1, getPos().getY() + 1, getPos().getZ() + 1);

        for (int i = 0; i < 6; i++) {
            if (modules[i] != null && modules[i].getRenderBoundingBox() != null) {
                renderBoundingBox = renderBoundingBox.union(modules[i].getRenderBoundingBox());
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        boolean hasModules = false;
        for (TubeModule module : modules) {
            if (module != null) {
                hasModules = true;
                module.shouldDrop = true;
                module.update();
            }
        }

        if (!getWorld().isRemote && !hasModules) {
            List<IAirHandlerMachine.Connection> l = airHandler.getConnectedAirHandlers(this);
            if (l.size() == 1) {
                IAirHandlerMachine.Connection c = l.get(0);
                Direction d = c.getDirection();
                if (d != null && modules[d.getOpposite().ordinal()] == null && canConnectPneumatic(d.getOpposite())) {
                    airHandler.airLeak(this, d.getOpposite());
                }
            }
        }
    }

    @Override
    public void onAirDispersion(IAirHandlerMachine handler, @Nullable Direction side, int airDispersed) {
        if (side != null) {
            int intSide = side.ordinal();
            if (modules[intSide] instanceof IInfluenceDispersing) {
                ((IInfluenceDispersing) modules[intSide]).onAirDispersion(airDispersed);
            }
        }
    }

    @Override
    public int getMaxDispersion(IAirHandlerMachine handler, @Nullable Direction side) {
        if (side != null) {
            int intSide = side.ordinal();
            if (modules[intSide] instanceof IInfluenceDispersing) {
                return ((IInfluenceDispersing) modules[intSide]).getMaxDispersion();
            }
        }
        return Integer.MAX_VALUE;
    }

    public TubeModule getModule(Direction side) {
        return modules[side.getIndex()];
    }

    public boolean mayPlaceModule(Direction side) {
        return inLineModuleDir == null && modules[side.ordinal()] == null && !sidesClosed[side.ordinal()];
    }

    public void setModule(TubeModule module, Direction side) {
        if (module != null) {
            module.setDirection(side);
            module.setTube(this);
            if (module.isInline()) {
                inLineModuleDir = side;
            }
        } else {
            if (inLineModuleDir == side) {
                inLineModuleDir = null;
            }
        }
        modules[side.getIndex()] = module;
        if (getWorld() != null && !getWorld().isRemote) {
            world.setBlockState(getPos(), BlockPressureTube.recalculateState(world, pos, getBlockState()), Constants.BlockFlags.DEFAULT);
            sendDescriptionPacket();
            markDirty();
        }
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return (inLineModuleDir == null || inLineModuleDir.getAxis() == side.getAxis())
                && !sidesClosed[side.ordinal()]
                && (modules[side.ordinal()] == null || modules[side.ordinal()].isInline());
    }

    @Override
    public void onNeighborTileUpdate() {
        super.onNeighborTileUpdate();
        for (TubeModule module : modules) {
            if (module != null) module.onNeighborTileUpdate();
        }
    }

    @Override
    public void onNeighborBlockUpdate() {
        super.onNeighborBlockUpdate();
        for (TubeModule module : modules) {
            if (module != null) module.onNeighborBlockUpdate();
        }
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return null;
    }

    public TileEntity getConnectedNeighbor(Direction dir) {
        int idx = dir.getIndex();
        if (!sidesClosed[idx] && (modules[idx] == null || modules[idx].isInline() && dir.getAxis() == modules[idx].getDirection().getAxis())) {
            TileEntity te = getCachedNeighbor(dir);
            if (te != null && te.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, dir.getOpposite()).isPresent()) {
                return te;
            }
        }
        return null;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return renderBoundingBox != null ? renderBoundingBox : new AxisAlignedBB(getPos());
    }

    @Override
    public void printManometerMessage(PlayerEntity player, List<ITextComponent> text) {
        RayTraceResult mop = PneumaticCraftUtils.getEntityLookedObject(player);
        if (mop != null && mop.hitInfo instanceof Direction) {
            Direction dir = (Direction) mop.hitInfo;
            if (modules[dir.ordinal()] != null) {
                modules[dir.ordinal()].addInfo(text);
            }
        }
    }

    @Override
    public BlockState getCamouflage() {
        return camoState;
    }

    @Override
    public void setCamouflage(BlockState state) {
        camoState = state;
        camoStack = ICamouflageableTE.getStackForState(state);
        sendDescriptionPacket();
        markDirty();
    }

    @Override
    public void onDescUpdate() {
        camoState = ICamouflageableTE.getStateForStack(camoStack);

        super.onDescUpdate();
    }

    public static TileEntityPressureTube getTube(TileEntity te) {
        return te instanceof TileEntityPressureTube ? (TileEntityPressureTube) te : null;
    }
}
