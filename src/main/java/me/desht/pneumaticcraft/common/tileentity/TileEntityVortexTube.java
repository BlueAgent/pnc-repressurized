package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.network.DescSynced;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;

public class TileEntityVortexTube extends TileEntityPneumaticBase implements IHeatExchanger, IHeatTinted {
    private final IHeatExchangerLogic coldHeatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().getHeatExchangerLogic();
    private final IHeatExchangerLogic hotHeatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().getHeatExchangerLogic();
    private final IHeatExchangerLogic connectingExchanger = PneumaticRegistry.getInstance().getHeatRegistry().getHeatExchangerLogic();
    private int visualizationTimer = 30;

    @DescSynced
    public final boolean[] sidesConnected = new boolean[6];
    @DescSynced
    private boolean visualize;
    @DescSynced
    private int coldHeatLevel = 10, hotHeatLevel = 10;

    public TileEntityVortexTube() {
        super(20, 25, 2000, 0);
        coldHeatExchanger.setThermalResistance(0.01);
        hotHeatExchanger.setThermalResistance(0.01);
        connectingExchanger.setThermalResistance(100);
        connectingExchanger.addConnectedExchanger(coldHeatExchanger);
        connectingExchanger.addConnectedExchanger(hotHeatExchanger);
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic(EnumFacing side) {
        if (side == null || side == getRotation().getOpposite()) {
            return hotHeatExchanger;
        } else if (side == getRotation()) {
            return coldHeatExchanger;
        } else {
            return null;
        }
    }

    @Override
    protected EnumFacing[] getConnectedHeatExchangerSides() {
        return new EnumFacing[]{getRotation().getOpposite()};
    }

    @Override
    protected void initializeIfHeatExchanger() {
        super.initializeIfHeatExchanger();
        initializeHeatExchanger(coldHeatExchanger, getRotation());
    }

    @Override
    public boolean isConnectedTo(EnumFacing side) {
        return side != getRotation() && side != getRotation().getOpposite();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        NBTTagCompound coldHeatTag = new NBTTagCompound();
        coldHeatExchanger.writeToNBT(coldHeatTag);
        tag.setTag("coldHeat", coldHeatTag);
        for (int i = 0; i < 6; i++) {
            tag.setBoolean("sideConnected" + i, sidesConnected[i]);
        }
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        coldHeatExchanger.readFromNBT(tag.getCompoundTag("coldHeat"));
        for (int i = 0; i < 6; i++) {
            sidesConnected[i] = tag.getBoolean("sideConnected" + i);
        }
    }

    public int getColdHeatLevel() {
        return visualize ? 0 : coldHeatLevel;
    }

    public int getHotHeatLevel() {
        return visualize ? 20 : hotHeatLevel;
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            // Only update the cold and connecting side, the hot side is handled in TileEntityBase.
            connectingExchanger.update();
            coldHeatExchanger.update();
            int usedAir = (int) (getPressure() * 10);
            if (usedAir > 0) {
                addAir(-usedAir);
                double generatedHeat = usedAir / 10D;
                coldHeatExchanger.addHeat(-generatedHeat);
                hotHeatExchanger.addHeat(generatedHeat);
            }
            visualize = visualizationTimer > 0;
            if (visualize) visualizationTimer--;
            coldHeatLevel = HeatUtil.getHeatLevelForTemperature(coldHeatExchanger.getTemperature());
            hotHeatLevel = HeatUtil.getHeatLevelForTemperature(hotHeatExchanger.getTemperature());
        }
    }

    @Override
    public void onBlockRotated() {
        visualizationTimer = 60;
    }

    @Override
    public void onNeighborBlockUpdate() {
        super.onNeighborBlockUpdate();
        updateConnections();
    }

    private void updateConnections() {
        List<Pair<EnumFacing, IAirHandler>> connections = getAirHandler(null).getConnectedPneumatics();
        Arrays.fill(sidesConnected, false);
        for (Pair<EnumFacing, IAirHandler> entry : connections) {
            sidesConnected[entry.getKey().ordinal()] = true;
        }
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    @Override
    public int getHeatLevelForTintIndex(int tintIndex) {
        switch (tintIndex) {
            case 0: return visualize ? 20 : hotHeatLevel;
            case 1: return visualize ? 0 : coldHeatLevel;
            default: return 0xFFFFFFFF;
        }
    }
}
