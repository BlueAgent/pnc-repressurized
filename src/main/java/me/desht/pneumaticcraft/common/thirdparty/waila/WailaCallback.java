package me.desht.pneumaticcraft.common.thirdparty.waila;

import mcp.mobius.waila.api.IWailaRegistrar;
import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.api.tileentity.IPneumaticMachine;
import me.desht.pneumaticcraft.common.tileentity.IRedstoneControl;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import net.minecraft.block.Block;

public class WailaCallback {
    public static void callback(IWailaRegistrar registrar) {
        registrar.registerBodyProvider(new WailaPneumaticHandler(), IPneumaticMachine.class);
        registrar.registerBodyProvider(new WailaHeatHandler(), IHeatExchanger.class);
        registrar.registerBodyProvider(new WailaSemiBlockHandler(), Block.class);
        registrar.registerBodyProvider(new WailaRedstoneControl(), IRedstoneControl.class);
        registrar.registerBodyProvider(new WailaTubeModuleHandler(), TileEntityPressureTube.class);
        registrar.registerBodyProvider(new WailaEntityHandler(), IPressurizable.class);

        registrar.registerNBTProvider(new WailaPneumaticHandler(), IPneumaticMachine.class);
        registrar.registerNBTProvider(new WailaHeatHandler(), IHeatExchanger.class);
        registrar.registerNBTProvider(new WailaSemiBlockHandler(), Block.class);
        registrar.registerNBTProvider(new WailaRedstoneControl(), IRedstoneControl.class);
        registrar.registerNBTProvider(new WailaTubeModuleHandler(), TileEntityPressureTube.class);
        registrar.registerNBTProvider(new WailaEntityHandler(), IPressurizable.class);
    }

}
