package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.thirdparty.computercraft.LuaConstant;
import me.desht.pneumaticcraft.common.thirdparty.computercraft.LuaMethod;
import me.desht.pneumaticcraft.common.thirdparty.computercraft.LuaMethodRegistry;
import me.desht.pneumaticcraft.lib.NBTKeys;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class TileEntityPneumaticBase extends TileEntityTickableBase {
    @GuiSynced
    final IAirHandlerMachine airHandler;
    private final LazyOptional<IAirHandlerMachine> airHandlerCap;
    public final float dangerPressure;
    public final float criticalPressure;
    private final int defaultVolume;

    public TileEntityPneumaticBase(TileEntityType type, float dangerPressure, float criticalPressure, int volume, int upgradeSlots) {
        super(type, upgradeSlots);

        this.airHandler = PneumaticRegistry.getInstance().getAirHandlerMachineFactory()
                .createAirHandler(dangerPressure, criticalPressure, volume);
        this.airHandlerCap = LazyOptional.of(() -> airHandler);
        this.dangerPressure = dangerPressure;
        this.criticalPressure = criticalPressure;
        this.defaultVolume = volume;
    }

    @Override
    public void tick() {
        super.tick();
        airHandler.tick(this);
    }

    @Override
    public void remove() {
        super.remove();
        airHandlerCap.invalidate();
    }

    @Override
    public void onUpgradesChanged() {
        super.onUpgradesChanged();

        airHandler.setVolumeUpgrades(getUpgrades(EnumUpgrade.VOLUME));
        airHandler.setHasSecurityUpgrade(getUpgrades(EnumUpgrade.SECURITY) > 0);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY) {
            return side == null || canConnectPneumatic(side) ?
                    PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY.orEmpty(cap, airHandlerCap) :
                    LazyOptional.empty();
        } else {
            return super.getCapability(cap, side);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.put(NBTKeys.NBT_AIR_HANDLER, airHandler.serializeNBT());
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);

        airHandler.deserializeNBT(tag.getCompound(NBTKeys.NBT_AIR_HANDLER));
        if (tag.contains(NBTKeys.NBT_AIR_AMOUNT)) {
            // when restoring from item NBT
            airHandler.addAir(tag.getInt(NBTKeys.NBT_AIR_AMOUNT));
        }
    }

    @Override
    public void onBlockRotated() {
        super.onBlockRotated();
        airHandler.invalidateNeighbours();
    }

    @Override
    public void onNeighborBlockUpdate() {
        super.onNeighborBlockUpdate();
        airHandler.invalidateNeighbours();
    }

    @Override
    public void writeToPacket(CompoundNBT tag) {
        super.writeToPacket(tag);
        tag.put(NBTKeys.NBT_AIR_HANDLER, airHandler.serializeNBT());
    }

    @Override
    public void readFromPacket(CompoundNBT tag) {
        super.readFromPacket(tag);
        airHandler.deserializeNBT(tag.getCompound(NBTKeys.NBT_AIR_HANDLER));
    }

    @Override
    protected void addLuaMethods(LuaMethodRegistry registry) {
        super.addLuaMethods(registry);
        registry.registerLuaMethod(new LuaMethod("getPressure") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 0, 1, "face (down/up/north/south/west/east)");
                if (args.length == 0) {
                    return new Object[]{airHandler.getPressure()};
                } else {
                    LazyOptional<IAirHandlerMachine> cap = getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, getDirForString((String) args[0]));
                    return new Object[]{ cap.map(IAirHandler::getPressure).orElse(0f) };
                }
            }
        });

        if (this instanceof IMinWorkingPressure) {
            final IMinWorkingPressure mwp = (IMinWorkingPressure) this;
            registry.registerLuaMethod(new LuaMethod("getMinWorkingPressure") {
                @Override
                public Object[] call(Object[] args) {
                    requireNoArgs(args);
                    return new Object[] { mwp.getMinWorkingPressure() };
                }
            });
        }

        registry.registerLuaMethod(new LuaConstant("getDangerPressure", dangerPressure));
        registry.registerLuaMethod(new LuaConstant("getCriticalPressure", criticalPressure));
        registry.registerLuaMethod(new LuaConstant("getDefaultVolume", defaultVolume));
    }

    /*
     * End ComputerCraft API 
     */

    public float getPressure() {
        return airHandler.getPressure();
    }

    public void addAir(int air) {
        airHandler.addAir(air);
    }

    /**
     * Checks if the given side of this TE can be pneumatically connected to.
     *
     * @param side the side to check
     * @return true if connected, false otherwise
     */
    public boolean canConnectPneumatic(Direction side) {
        return true;
    }

    public int getDefaultVolume() {
        return defaultVolume;
    }

    public void forceLeak(Direction dir) {
        airHandler.airLeak(this, dir);
    }

    public boolean isLeaking() {
        return airHandler.getConnectedAirHandlers(this).isEmpty();
    }
}
