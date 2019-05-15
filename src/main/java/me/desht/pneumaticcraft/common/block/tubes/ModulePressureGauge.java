package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.client.model.module.ModelGauge;
import me.desht.pneumaticcraft.client.model.module.ModelModuleBase;
import me.desht.pneumaticcraft.common.GuiHandler.EnumGuiId;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdatePressureBlock;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticBase;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.entity.player.EntityPlayer;

public class ModulePressureGauge extends TubeModuleRedstoneEmitting {
    public ModulePressureGauge() {
        lowerBound = 0;
        higherBound = 7.5F;
    }

    @Override
    public void update() {
        super.update();
        if (!pressureTube.world().isRemote) {
            if (pressureTube.world().getTotalWorldTime() % 20 == 0)
                NetworkHandler.sendToAllAround(new PacketUpdatePressureBlock((TileEntityPneumaticBase) getTube()), getTube().world());
            setRedstone(getRedstone(pressureTube.getAirHandler(null).getPressure()));
        }
    }

    private int getRedstone(float pressure) {
        return (int) ((pressure - lowerBound) / (higherBound - lowerBound) * 15);
    }

    @Override
    public String getType() {
        return Names.MODULE_GAUGE;
    }

    @Override
    public double getWidth() {
        return 0.5;
    }

    @Override
    protected double getHeight() {
        return 0.25;
    }

    @Override
    public boolean onActivated(EntityPlayer player) {
        return super.onActivated(player);
    }

    @Override
    protected EnumGuiId getGuiId() {
        return EnumGuiId.PRESSURE_MODULE;
    }

    @Override
    public Class<? extends ModelModuleBase> getModelClass() {
        return ModelGauge.class;
    }
}
