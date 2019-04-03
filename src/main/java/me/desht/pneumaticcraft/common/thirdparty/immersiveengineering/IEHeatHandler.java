package me.desht.pneumaticcraft.common.thirdparty.immersiveengineering;

import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import net.minecraft.tileentity.TileEntity;

import static me.desht.pneumaticcraft.common.config.ConfigHandler.integration;

class IEHeatHandler {
    static void registerHeatHandler() {
        ExternalHeaterHandler.registerHeatableAdapter(TileEntityBase.class, new ExternalHeaterHandler.HeatableAdapter() {
            @Override
            public int doHeatTick(TileEntity tileEntity, int energyAvailable, boolean canHeat) {
                if (tileEntity instanceof IHeatExchanger && integration.ieExternalHeaterHeatPerRF > 0) {
                    IHeatExchangerLogic heatExchanger = ((IHeatExchanger) tileEntity).getHeatExchangerLogic(null);
                    if (heatExchanger != null && energyAvailable >= integration.ieExternalHeaterRFperTick) {
                        heatExchanger.addHeat(integration.ieExternalHeaterRFperTick * integration.ieExternalHeaterHeatPerRF);
                        return integration.ieExternalHeaterRFperTick;
                    }
                }
                return 0;
            }
        });
    }
}
