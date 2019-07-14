package me.desht.pneumaticcraft.common.config;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import net.minecraftforge.fml.config.ModConfig;

import java.util.List;

public class Config {
    public static void setGuiRemoteGridSnap(boolean snap) {
        ConfigHandler.clientConfig.getConfigData().set("General.gui_remote_grid_snap", snap);
    }

    private static void setValueAndSave(final ModConfig modConfig, final String path, final Object newValue) {
        modConfig.getConfigData().set(path, newValue);
        modConfig.save();
    }

    public static class Client {
        public static boolean aphorismDrama;
        public static boolean fancyArmorModels;
        public static GuiProgrammer.Difficulty programmerDifficulty;
        public static boolean topShowsFluids;
        public static boolean logisticsGuiTint;
        public static boolean dronesRenderHeldItem;
        public static boolean semiBlockLighting;
        public static boolean guiBevel;
        public static boolean alwaysShowPressureDurabilityBar;
        public static boolean tubeModuleRedstoneParticles;
        public static int blockTrackerMaxTimePerTick;
        public static boolean guiRemoteGridSnap;
        public static double leggingsFOVFactor;
    }

    public static class Common {
        public static class General {
            public static double oilGenerationChance;
            public static int compressedIngotLossRate;
            public static boolean enableDungeonLoot;
            public static boolean enableDroneSuffocation;
            public static double fuelBucketEfficiency;
            public static int maxProgrammingArea;
            public static boolean explosionCrafting;
            public static List<Integer> oilWorldGenBlacklist;
            public static int minFluidFuelTemperature;
            public static boolean useUpDyesWhenColoring;
        }

        public static class Machines {
            public static boolean aerialInterfaceArmorCompat;
            public static double cropSticksGrowthBoostChance;
            public static int electricCompressorEfficiency;
            public static int electrostaticLightningChance;
            public static int elevatorBaseBlocksPerBase;
            public static int fluxCompressorEfficiency;
            public static boolean keroseneLampCanUseAnyFuel;
            public static double keroseneLampFuelEfficiency;
            public static int kineticCompressorEfficiency;
            public static boolean liquidHopperDispenser;
            public static boolean omniHopperDispenser;
            public static int plasticMixerPlasticRatio;
            public static int pneumaticDynamoEfficiency;
            public static int pneumaticEngineEfficiency;
            public static int pneumaticGeneratorEfficiency;
            public static int pneumaticPumpEfficiency;
            public static double speedUpgradeSpeedMultiplier;
            public static double speedUpgradeUsageMultiplier;
            public static double thermalCompressorThermalResistance;
        }

        public static class Armor {
            public static int jetBootsAirUsage;
            public static int armorStartupTime;
        }

        public static class Integration {
            public static double ieExternalHeaterHeatPerRF;
            public static int ieExternalHeaterRFperTick;
            public static double mekHeatEfficiency;
            public static double mekThermalResistanceMult;
            public static double tanAirConAirUsageMultiplier;
            public static double tanHeatDivider;
            public static int tanRefreshInterval;
        }

        public static class Advanced {
            public static boolean disableKeroseneLampFakeAirBlock;
            public static double liquidTankUpdateThreshold;
            public static boolean stopDroneAI;
        }

        public static class Micromissiles {
            public static double baseExplosionDamage;
            public static boolean damageTerrain;
            public static int launchCooldown;
            public static int lifetime;
            public static int missilePodSize;
        }

        public static class Minigun {
            public static double apAmmoDamageMultiplier;
            public static int apAmmoIgnoreArmorChance;
            public static int armorPiercingAmmoCartridgeSize;
            public static double baseDamage;
            public static int baseRange;
            public static int explosiveAmmoCartridgeSize;
            public static double explosiveAmmoDamageMultiplier;
            public static int explosiveAmmoExplosionChance;
            public static double explosiveAmmoExplosionPower;
            public static boolean explosiveAmmoTerrainDamage;
            public static int freezingAmmoBlockIceChance;
            public static int freezingAmmoCartridgeSize;
            public static int freezingAmmoEntityIceChance;
            public static double freezingAmmoFakeIceDamage;
            public static int incendiaryAmmoBlockIgniteChance;
            public static int incendiaryAmmoCartridgeSize;
            public static int incendiaryAmmoEntityIgniteChance;
            public static int incendiaryAmmoFireDuration;
            public static int potionProcChance;
            public static int standardAmmoCartridgeSize;
            public static double weightedAmmoAirUsageMultiplier;
            public static int weightedAmmoCartridgeSize;
            public static double weightedAmmoDamageMultiplier;
            public static double weightedAmmoRangeMultiplier;
        }
    }
}
