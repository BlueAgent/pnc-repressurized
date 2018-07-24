package me.desht.pneumaticcraft.common.config;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.heat.HeatExchangerManager;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.io.IOException;

@Config(modid = Names.MOD_ID)
public class ConfigHandler {
    private static ISubConfig[] subConfigs = new ISubConfig[] {
            new AmadronOfferSettings(),
            AmadronOfferStaticConfig.INSTANCE,
            AmadronOfferPeriodicConfig.INSTANCE,
            new ProgWidgetConfig(),
            HelmetWidgetDefaults.INSTANCE,
            ThirdPartyConfig.INSTANCE
    };

    @Config.Name("general")
    @Config.LangKey("gui.config.category.general")
    @Config.Comment("General stuff that doesn't fit anywhere else")
    public static General general = new General();

    @Config.Name("machine_properties")
    @Config.LangKey("gui.config.category.machine")
    @Config.Comment("Settings affecting the operation of machines")
    public static MachineProperties machineProperties = new MachineProperties();

    @Config.Name("advanced")
    @Config.LangKey("gui.config.category.advanced")
    @Config.Comment("Advanced settings (shouldn't need to touch under normal circumstances)")
    public static Advanced advanced = new Advanced();

    @Config.Name("recipes")
    @Config.LangKey("gui.config.category.recipes")
    @Config.Comment("Enable/disable certain recipes (note: better done with CraftTweaker if you have it)")
    public static Recipes recipes = new Recipes();

    @Config.Name("client")
    @Config.LangKey("gui.config.category.client")
    @Config.Comment("Settings affecting only the client")
    public static ClientOptions client = new ClientOptions();

    @Config.Name("helmet")
    @Config.LangKey("gui.config.category.helmet")
    @Config.Comment("Pneumatic Helmet HUD settings (note: you can also set this up via helmet GUI)")
    public static HelmetOptions helmetOptions = new HelmetOptions();

    public static void sync() {
        ConfigManager.sync(Names.MOD_ID, Config.Type.INSTANCE);
    }

    public static void onPreInit(File configFile) {
        PneumaticCraftRepressurized.proxy.initConfig();

        for(ISubConfig subConfig : subConfigs) {
            File subFolder = new File(configFile.getAbsolutePath().substring(0, configFile.getAbsolutePath().length() - 4) + File.separator);
            if (subFolder.exists() || subFolder.mkdirs()) {
                File subFile = new File(subFolder, subConfig.getConfigFilename() + ".cfg");
                try {
                    subConfig.preInit(subFile);
                } catch(IOException e) {
                    PneumaticCraftRepressurized.logger.error("Config file " + subConfig.getConfigFilename() + " failed to create! Unexpected things can happen!");
                    e.printStackTrace();
                } catch (ClassCastException e) {
                    PneumaticCraftRepressurized.logger.error("Config file " + subConfig.getConfigFilename() + " appears to be invalid JSON! Unexpected things can happen!");
                    e.printStackTrace();
                }
            }
        }
    }

    public static void onPostInit() {
        for(ISubConfig subConfig : subConfigs) {
            try {
                subConfig.postInit();
            } catch(IOException e) {
                PneumaticCraftRepressurized.logger.error("Config file " + subConfig.getConfigFilename() + " failed to create! Unexpected things can happen!");
                e.printStackTrace();
            } catch (ClassCastException e) {
                PneumaticCraftRepressurized.logger.error("Config file " + subConfig.getConfigFilename() + " appears to be invalid JSON! Unexpected things can happen!");
                e.printStackTrace();
            }
        }
    }

    public static class General {
        @Config.Comment("Chance per chunk in percentage to generate an Oil Lake. Set to 0 for no spawns")
        @Config.RangeDouble(min = 0d, max = 100d)
        public double oilGenerationChance = 15d;
        @Config.Comment("Loss percentage (on average) of Compressed Iron ingots/blocks when exposed to an explosion")
        @Config.RangeInt(min = 0, max = 100)
        public int configCompressedIngotLossRate = 20;
        @Config.Comment("Enables the dungeon loot generation of the Nuke Virus and Stop! Worm (not useful in single-player worlds)")
        public boolean enableDungeonLoot = true;
        @Config.Comment("Damage of the Miniguns. This applies to the Sentry Gun, Handheld Minigun, and Drone-based minigun")
        public float configMinigunDamage = 4f;
        @Config.Comment("Enable Drone Suffocation Damage")
        public boolean enableDroneSuffocationDamage = true;
        @Config.Comment("Efficiency of fuel buckets as furnace fuel (default 0.05 means 1 bucket of LPG smelts 450 items in a vanilla furnace")
        public float fuelBucketEfficiencyMultiplier = 0.05f;
        @Config.Comment("Maximum number of blocks in the area defined in an Area Programming Puzzle Piece")
        public int maxProgrammingArea = 250000;
        @Config.Comment("Enable/disable explosion crafting (iron->compressed iron).  If you disable this, you'll need another way to get compressed iron initially")
        public boolean explosionCrafting = true;
        @Config.Comment("Oil worldgen blacklist: add dimension IDs to this list if you don't want oil worldgen to happen there.")
        @Config.RequiresMcRestart
        public int[] oilWorldGenBlacklist = new int[] { 1, -1 };
        @Config.Comment("Thermal resistance of non-vanilla fluids, which is how fast heat moves between them and adjacent heat-handling blocks like the refinery.  Lower values mean faster heat movement.")
        @Config.RequiresWorldRestart
        public double fluidThermalResistance = HeatExchangerManager.DEFAULT_FLUID_RESISTANCE;
        @Config.Comment("Chance per shot (1 in X) of potion-tipped ammo proc'ing the potion effect")
        public int minigunPotionProcChance = 15;
    }

    public static class MachineProperties {
        @Config.Comment("Changing this value will alter the pressurized air usage of the Pneumatic Generator. The output, EU, will stay the same.")
        public int pneumaticGeneratorEfficiency = 40;
        @Config.Comment("Changing this value will alter the pressurized air production of the Electric Compressor. The input, EU, will stay the same")
        public int electricCompressorEfficiency = 40;
//        @Config.Comment("Changing this value will alter the pressurized air usage of the Pneumatic Engine. The output, MJ, will stay the same")
//        public int pneumaticEngineEfficiency = 40;
//        @Config.Comment("Changing this value will alter the pressurized air production of the Kinetic Compressor. The input, MJ, will stay the same")
//        public int kineticCompressorEfficiency = 40;
//        @Config.Comment("Changing this value will alter the hydraulic bar production of the Pneumatic Pump. The input, air, will stay the same")
//        public int pneumaticPumpEfficiency = 40;
        @Config.Comment("Changing this value will alter the pressurized air production of the Flux Compressor. The input, RF, will stay the same")
        public int fluxCompressorEfficiency = 40;
        @Config.Comment("Changing this value will alter the pressurized air usage of the Pneumatic Dynamo. The output, RF, will stay the same")
        public int pneumaticDynamoEfficiency = 40;
        @Config.Comment("The max height of an elevator per stacked Elevator Base.")
        @Config.RangeInt(min = 1, max = 256)
        public int elevatorBaseBlocksPerBase = 4;
        @Config.Comment("Can the Kerosene Lamp burn any kind of fuel?  If false, only Kerosene can be burnt")
        public boolean keroseneLampCanUseAnyFuel = true;
        @Config.Comment("Kerosene Lamp fuel efficiency: higher values mean fuel will last longer in the lamp")
        public float keroseneLampFuelEfficiency = 1.0f;
    }

    public static class Advanced {
        @Config.Comment("When set to true, Drones will not execute any program. This is useful to set to true when due to a bug Drones are lagging your server or crashing it. Please report the bug if you encounter it.")
        public boolean stopDroneAI = false;
//        @Config.Comment("ONLY SET TO TRUE WHEN YOU KNOW WHAT YOU'RE DOING. When set to true, this will convert any Pressure Tube in the world that was a FMP to its block variant. Handy when you're about to remove FMP from the instance. This will remove any other parts from the block like covers. Exception are tube modules.")
//        public boolean convertMultipartsTBlock = false;
        @Config.Comment("When set to true, the Kerosene Lamp's fake air blocks won't be registered and therefore removed from the world. Useful if this causes trouble (it shouldn't though)")
        public boolean disableKeroseneLampFakeAirBlock = false;
        @Config.Comment("The amount by which any liquid tank's contents must change, as a proportion of the tank's total capacity, to trigger an update to clients. Larger values mean fewer updates but less granularity in client-side fluid rendering.")
        @Config.RangeDouble(min = 0.0001, max = 1)
        @Config.RequiresWorldRestart
        public double liquidTankUpdateThreshold = 0.01;
    }

    public static class Recipes {
        @Config.Comment("Electric Compressor (currently ignored, defined in JSON)")
        public boolean enableElectricCompressorRecipe = true;
        @Config.Comment("Pneumatic Generator (currently ignored, defined in JSON)")
        public boolean enablePneumaticGeneratorRecipe = true;
//        @Config.Comment("Pneumatic Pump")
//        public boolean enablePneumaticPumpRecipe = true;
        @Config.Comment("8 Block of Coal --> 1 Diamond (Pressure Chamber)")
        @Config.RequiresMcRestart
        public boolean enableCoalToDiamondsRecipe = true;
    }

    public static class ClientOptions {
        @Config.Comment("Enable Aphorism Tile Drama!")
        public boolean aphorismDrama = true;
        @Config.Comment("When true, the Pneumatic Helmet will be a model. Warning: this model looks far too good to be in MC (currently ignored)")
        public boolean useHelmetModel = false;
        @Config.Comment("Defines the default difficulty of the Programmer shown to the user. 0 = easy, 1 = medium, 2 = advanced")
        @Config.RangeInt(min = 0, max = 2)
        public int programmerDifficulty = 0;
        @Config.Comment("Show tank fluids with the The One Probe.  Note that TOP also has support for showing tanks, which may or may not be enabled.")
        public boolean topShowsFluids = true;
        @Config.Comment("Tint Logistics configuration GUI backgrounds according to the colour of the logistics frame you are configuring")
        public boolean logisticsGUITint = true;
        @Config.Comment("Drones render their held item (the item in slot 0 of their inventory) ?")
        public boolean dronesRenderHeldItem = true;
    }

    public static class HelmetOptions {
        @Config.Comment("Pneumatic helmet power readout X")
        public int powerX = -1;
        @Config.Comment("Pneumatic helmet power readout Y")
        public int powerY = 2;
        @Config.Comment("Pneumatic helmet power readout on left?")
        public boolean powerLeft = false;
        @Config.Comment("Pneumatic helmet message readout X")
        public int messageX = 2;
        @Config.Comment("Pneumatic helmet message readout Y")
        public int messageY = 2;
        @Config.Comment("Pneumatic helmet message readout on left?")
        public boolean messageLeft = false;
        @Config.Comment("Pneumatic helmet block tracker X")
        public int blockTrackerX = -1;
        @Config.Comment("Pneumatic helmet block tracker Y")
        public int blockTrackerY = 46;
        @Config.Comment("Pneumatic helmet block tracker on left?")
        public boolean blockTrackerLeft = true;
        @Config.Comment("Pneumatic helmet entity tracker X")
        public int entityTrackerX = -1;
        @Config.Comment("Pneumatic helmet entity tracker Y")
        public int entityTrackerY = 90;
        @Config.Comment("Pneumatic helmet entity tracker on left?")
        public boolean entityTrackerLeft = true;
        @Config.Comment("Pneumatic helmet item search X")
        public int itemSearchX = -1;
        @Config.Comment("Pneumatic helmet item search Y")
        public int itemSearchY = 17;
        @Config.Comment("Pneumatic helmet item search on left?")
        public boolean itemSearchLeft = true;
        @Config.Comment("Pneumatic helmet Path Enabled")
        public boolean pathEnabled = true;
        @Config.Comment("Pneumatic helmet Wire Path")
        public boolean wirePath = true;
        @Config.Comment("Pneumatic helmet X-Ray")
        public boolean xRayEnabled = false;
        @Config.Comment("Pneumatic helmet Path Update Rate")
        public int pathUpdateSetting = 1;
    }

    public static void setProgrammerDifficulty(int difficulty) {
        client.programmerDifficulty = difficulty;
        sync();
    }

    public static int getProgrammerDifficulty() {
        return client.programmerDifficulty;
    }

    @Mod.EventBusSubscriber
    public static class ConfigSyncHandler
    {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
        {
            if (event.getModID().equals(Names.MOD_ID)) {
                ConfigManager.sync(Names.MOD_ID, Config.Type.INSTANCE);
                PneumaticCraftRepressurized.logger.info("Configuration has been saved.");
            }
        }
    }
}
