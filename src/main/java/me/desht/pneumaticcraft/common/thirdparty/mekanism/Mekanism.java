package me.desht.pneumaticcraft.common.thirdparty.mekanism;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.lib.ModIds;
import me.desht.pneumaticcraft.lib.Names;
import mekanism.api.heat.IHeatHandler;
import net.minecraft.fluid.Fluid;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

@Mod.EventBusSubscriber
public class Mekanism implements IThirdParty {
    @CapabilityInject(IHeatHandler.class)
    public static final Capability<IHeatHandler> CAPABILITY_HEAT_HANDLER = null;

    @ObjectHolder("mekanism:ethene")
    private static Fluid ETHENE = null;
    @ObjectHolder("mekanism:hydrogen")
    private static Fluid HYDROGEN = null;

    public static boolean available = false;

    @Override
    public void init() {
        available = true;

        if (ETHENE != null) {
            // takes some effort to make, so it's as good as LPG
            PneumaticRegistry.getInstance().getFuelRegistry().registerFuel(ETHENE, 1800000, 1.25f);
        }
        if (HYDROGEN != null) {
            // low fuel value, but burns fast
            PneumaticRegistry.getInstance().getFuelRegistry().registerFuel(HYDROGEN, 300000, 2f);
        }
    }

    @SubscribeEvent
    public static void attachHeatAdapters(AttachCapabilitiesEvent<TileEntity> event) {
        if (PNCConfig.Common.Integration.mekThermalEfficiencyFactor != 0 && CAPABILITY_HEAT_HANDLER != null) {
            if (event.getObject().getType().getRegistryName().getNamespace().equals(Names.MOD_ID)) {
                event.addCapability(RL("pnc2mek_heat_adapter"), new PNC2MekHeatProvider(event.getObject()));
            }
            if (event.getObject().getType().getRegistryName().getNamespace().equals(ModIds.MEKANISM)) {
                event.addCapability(RL("mek2pnc_heat_adapter"), new Mek2PNCHeatProvider(event.getObject()));
            }
        }
    }
}
