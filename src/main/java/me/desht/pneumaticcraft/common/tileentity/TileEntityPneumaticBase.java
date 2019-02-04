package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.common.block.tubes.IPneumaticPosProvider;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.thirdparty.computercraft.LuaConstant;
import me.desht.pneumaticcraft.common.thirdparty.computercraft.LuaMethod;
import me.desht.pneumaticcraft.common.thirdparty.computercraft.LuaMethodRegistry;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityPneumaticBase extends TileEntityTickableBase implements IPneumaticPosProvider {
    @GuiSynced
    final IAirHandler airHandler;
    public final float dangerPressure, criticalPressure;
    private final int defaultVolume;

    public TileEntityPneumaticBase(float dangerPressure, float criticalPressure, int volume, int upgradeSlots) {
        super(upgradeSlots);
        airHandler = PneumaticRegistry.getInstance().getAirHandlerSupplier().createAirHandler(dangerPressure, criticalPressure, volume);
        for (Item upgrade : airHandler.getApplicableUpgrades()) {
            addApplicableUpgrade(upgrade);
        }

        this.dangerPressure = dangerPressure;
        this.criticalPressure = criticalPressure;
        defaultVolume = volume;
//        addLuaMethods(luaMethodRegistry);
    }

    @Override
    public void update() {
        super.update();
        airHandler.update();
    }

    @Override
    public void validate() {
        super.validate();
        airHandler.validate(this);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        airHandler.writeToNBT(tag);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        airHandler.readFromNBT(tag);
    }

    @Override
    public void onNeighborTileUpdate() {
        super.onNeighborTileUpdate();
        airHandler.onNeighborChange();
    }

    @Override
    public void onNeighborBlockUpdate() {
        super.onNeighborBlockUpdate();
        airHandler.onNeighborChange();
    }

    @Override
    public void writeToPacket(NBTTagCompound tag) {
        super.writeToPacket(tag);
        airHandler.writeToNBT(tag);
    }

    @Override
    public void readFromPacket(NBTTagCompound tag) {
        super.readFromPacket(tag);
        airHandler.readFromNBT(tag);
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
                    IAirHandler handler = getAirHandler(getDirForString((String) args[0]));
                    return new Object[]{handler != null ? handler.getPressure() : 0};
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

    @Override
    public World world() {
        return getWorld();
    }

    @Override
    public BlockPos pos() {
        return getPos();
    }

    @Override
    public IAirHandler getAirHandler(EnumFacing side) {
        return side == null || isConnectedTo(side) ? airHandler : null;
    }

    public float getPressure() {
        return getAirHandler(null).getPressure();
    }

    public void addAir(int air) {
        getAirHandler(null).addAir(air);
    }

    /**
     * Checks if the given side of this TE can be pneumatically connected to.
     *
     * @param side the side to check
     * @return true if connected, false otherwise
     */
    public boolean isConnectedTo(EnumFacing side) {
        return true;
    }

    public int getDefaultVolume() {
        return defaultVolume;
    }
}
