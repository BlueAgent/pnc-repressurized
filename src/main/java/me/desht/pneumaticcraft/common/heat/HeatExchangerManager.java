package me.desht.pneumaticcraft.common.heat;

import me.desht.pneumaticcraft.api.heat.HeatBehaviour;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.api.tileentity.IHeatRegistry;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.heat.behaviour.HeatBehaviourManager;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import java.util.*;

public class HeatExchangerManager implements IHeatRegistry {
    public static final double DEFAULT_FLUID_RESISTANCE = 500;

    // Used to add thermal properties to vanilla blocks or non-tile-entity modded blocks
    private final Map<Block, IHeatExchanger> specialBlockExchangers = new HashMap<>();

    private static final IHeatExchangerLogic AIR_EXCHANGER = new HeatExchangerLogicConstant(295, 100);

    private static final HeatExchangerManager INSTANCE = new HeatExchangerManager();

    public static HeatExchangerManager getInstance() {
        return INSTANCE;
    }

    public void init() {
        registerBlockExchanger(Blocks.ICE, 263, 500);
        registerBlockExchanger(Blocks.PACKED_ICE, 263, 500);
        registerBlockExchanger(Blocks.SNOW, 268, 1000);
        registerBlockExchanger(Blocks.TORCH, 1700, 100000);
        registerBlockExchanger(Blocks.FIRE, 1700, 1000);
        registerBlockExchanger(Blocks.MAGMA, 1700, 500);

        Map<String, Fluid> fluids = FluidRegistry.getRegisteredFluids();
        for (Fluid fluid : fluids.values()) {
            if (fluid.getBlock() != null) {
                registerBlockExchanger(fluid.getBlock(), fluid.getTemperature(), ConfigHandler.general.fluidThermalResistance);
            }
        }
        registerBlockExchanger(Blocks.FLOWING_WATER, FluidRegistry.WATER.getTemperature(), 500);
        registerBlockExchanger(Blocks.FLOWING_LAVA, FluidRegistry.LAVA.getTemperature(), 500);
    }

    public IHeatExchangerLogic getLogic(World world, BlockPos pos, EnumFacing side) {
        if (!world.isBlockLoaded(pos)) return null;
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof IHeatExchanger) {
            return ((IHeatExchanger) te).getHeatExchangerLogic(side);
        } else {
            if (world.isAirBlock(pos)) {
                return AIR_EXCHANGER;
            } else {
                Block block = world.getBlockState(pos).getBlock();
                if (block instanceof IHeatExchanger) {
                    return ((IHeatExchanger) block).getHeatExchangerLogic(side);
                } else {
                    IHeatExchanger exchanger = specialBlockExchangers.get(block);
                    return exchanger == null ? null : exchanger.getHeatExchangerLogic(side);
                }
            }
        }
    }

    public void registerBlockExchanger(Block block, IHeatExchanger heatExchanger) {
        if (block == null)
            throw new IllegalArgumentException("block is null when trying to register a heat exchanger!");
        if (block instanceof IHeatExchanger)
            Log.warning("The block " + block.getTranslationKey() + " is implementing IHeatExchanger. Therefore you don't need to register it as such");
        if (specialBlockExchangers.containsKey(block)) {
            Log.error("The block " + block.getTranslationKey() + " was registered as heat exchanger already! It won't be added!");
        } else {
            specialBlockExchangers.put(block, heatExchanger);
        }
    }

    public void registerBlockExchanger(Block block, IHeatExchangerLogic heatExchangerLogic) {
        registerBlockExchanger(block, new SimpleHeatExchanger(heatExchangerLogic));
    }

    @Override
    public void registerBlockExchanger(Block block, double temperature, double thermalResistance) {
        registerBlockExchanger(block, new HeatExchangerLogicConstant(temperature, thermalResistance));
    }

    @Override
    public void registerHeatBehaviour(Class<? extends HeatBehaviour> heatBehaviour) {
        HeatBehaviourManager.getInstance().registerBehaviour(heatBehaviour);
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic() {
        return new HeatExchangerLogic();
    }

    public static class TemperatureData {
        private final Double[] temp = new Double[7];

        private boolean isMultisided = true;

        public TemperatureData(IHeatExchanger heatExchanger) {
            Arrays.fill(temp, null);

            Set<IHeatExchangerLogic> heatExchangers = new HashSet<>();
            IHeatExchangerLogic logic = null;
            for (EnumFacing face : EnumFacing.VALUES) {
                logic = heatExchanger.getHeatExchangerLogic(face);
                if (logic != null) {
                    if (heatExchangers.contains(logic)) {
                        isMultisided = false;
                        break;
                    } else {
                        heatExchangers.add(logic);
                    }
                }
            }

            if (isMultisided) {
                for (EnumFacing face : EnumFacing.VALUES) {
                    logic = heatExchanger.getHeatExchangerLogic(face);
                    if (logic != null) {
                        temp[face.ordinal()] = logic.getTemperature();
                    }
                }
            } else if (logic != null) {
                temp[6] = logic.getTemperature();
            }
        }

        public boolean isMultisided() {
            return isMultisided;
        }

        public double getTemperature(EnumFacing face) {
            return face == null ? temp[6] : temp[face.ordinal()];
        }

        public boolean hasData(EnumFacing face) {
            return face == null ? temp[6] != null : temp[face.ordinal()] != null;
        }
    }
}
