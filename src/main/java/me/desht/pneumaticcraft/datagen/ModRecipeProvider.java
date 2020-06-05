package me.desht.pneumaticcraft.datagen;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.crafting.AmadronTradeResource;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.api.crafting.ingredient.StackedIngredient;
import me.desht.pneumaticcraft.api.crafting.recipe.AssemblyRecipe.AssemblyProgramType;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.PneumaticCraftTags;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModRecipes;
import me.desht.pneumaticcraft.datagen.recipe.*;
import net.minecraft.block.Blocks;
import net.minecraft.data.*;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.NBTIngredient;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
        shaped(ModItems.AIR_CANISTER.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                " T /IRI/IRI",
                'T', ModBlocks.PRESSURE_TUBE.get(),
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'R', Tags.Items.DUSTS_REDSTONE
        ).build(consumer);

        shaped(ModBlocks.ADVANCED_AIR_COMPRESSOR.get(), ModBlocks.ADVANCED_PRESSURE_TUBE.get(),
                "III/I T/ICI",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'T', ModBlocks.ADVANCED_PRESSURE_TUBE.get(),
                'C', ModBlocks.AIR_COMPRESSOR.get()
        ).build(consumer);

        shaped(ModBlocks.ADVANCED_LIQUID_COMPRESSOR.get(), ModBlocks.ADVANCED_PRESSURE_TUBE.get(),
                "III/ITT/ICI",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'T', ModBlocks.ADVANCED_PRESSURE_TUBE.get(),
                'C', ModBlocks.LIQUID_COMPRESSOR.get()
        ).build(consumer);

        shaped(ModItems.ADVANCED_PCB.get(), 4, ModItems.PRINTED_CIRCUIT_BOARD.get(),
                "RPR/PCP/RPR",
                'R', Tags.Items.DUSTS_REDSTONE,
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS,
                'C', ModItems.PRINTED_CIRCUIT_BOARD.get()
        ).build(consumer);

        shaped(ModBlocks.AERIAL_INTERFACE.get(), ModBlocks.ADVANCED_PRESSURE_TUBE.get(),
                "WHW/ESE/WTW",
                'W', ModBlocks.PRESSURE_CHAMBER_WALL.get(),
                'H', ModBlocks.OMNIDIRECTIONAL_HOPPER.get(),
                'S', Items.NETHER_STAR,
                'T', ModBlocks.ADVANCED_PRESSURE_TUBE.get(),
                'E', Items.ENDER_PEARL
        ).build(consumer);

        shaped(ModItems.AIR_CANISTER.get(), ModBlocks.PRESSURE_TUBE.get(),
                " T /IRI/IRI",
                'T', ModBlocks.PRESSURE_TUBE.get(),
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'R', Tags.Items.DUSTS_REDSTONE);

        shaped(ModBlocks.AIR_CANNON.get(), ModBlocks.PRESSURE_TUBE.get(),
                " B / ST/HHH",
                'B', ModItems.CANNON_BARREL.get(),
                'S', ModItems.STONE_BASE.get(),
                'T', ModBlocks.PRESSURE_TUBE.get(),
                'H', ModBlocks.REINFORCED_STONE_SLAB.get()
        ).build(consumer);

        shaped(ModBlocks.AIR_COMPRESSOR.get(), ModBlocks.PRESSURE_TUBE.get(),
                "III/I T/IFI",
                'I', PneumaticCraftTags.Items.REINFORCED_STONE_BRICKS,
                'T', ModBlocks.PRESSURE_TUBE.get(),
                'F', Blocks.FURNACE
        ).build(consumer);

        shaped(ModItems.AIR_GRATE_MODULE.get(), ModBlocks.PRESSURE_TUBE.get(),
                " B /BTB/ B ",
                'B', Blocks.IRON_BARS,
                'T', ModBlocks.PRESSURE_TUBE.get()
        ).build(consumer);

        shapedPressure(ModItems.AMADRON_TABLET.get(), ModItems.PLASTIC.get(),
                "PPP/PGP/PCP",
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS,
                'G', ModItems.GPS_TOOL.get(),
                'C', ModItems.AIR_CANISTER.get()
        ).build(consumer);

        shapeless(ModBlocks.APHORISM_TILE.get(), ModBlocks.APHORISM_TILE.get(),
                ModBlocks.APHORISM_TILE.get()
        ).build(consumer, RL("aphorism_tile_reset"));

        shaped(ModBlocks.ASSEMBLY_CONTROLLER.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                " B /TBB/III",
                'B', ModItems.PRINTED_CIRCUIT_BOARD.get(),
                'T', ModBlocks.PRESSURE_TUBE.get(),
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).build(consumer);

        shaped(ModBlocks.ASSEMBLY_DRILL.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                "DCC/  C/IBI",
                'D', Tags.Items.GEMS_DIAMOND,
                'C', ModItems.PNEUMATIC_CYLINDER.get(),
                'B', ModItems.PRINTED_CIRCUIT_BOARD.get(),
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).build(consumer);

        shaped(ModBlocks.ASSEMBLY_LASER.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                "DCC/  C/IBI",
                'D', Tags.Items.GLASS_RED,
                'C', ModItems.PNEUMATIC_CYLINDER.get(),
                'B', ModItems.PRINTED_CIRCUIT_BOARD.get(),
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).build(consumer);

        shaped(ModBlocks.ASSEMBLY_IO_UNIT_IMPORT.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                "HCC/  C/IBI",
                'H', Blocks.HOPPER,
                'C', ModItems.PNEUMATIC_CYLINDER.get(),
                'B', ModItems.PRINTED_CIRCUIT_BOARD.get(),
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).build(consumer);

        shaped(ModBlocks.ASSEMBLY_IO_UNIT_EXPORT.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                "CCH/C  /IBI",
                'H', Blocks.HOPPER,
                'C', ModItems.PNEUMATIC_CYLINDER.get(),
                'B', ModItems.PRINTED_CIRCUIT_BOARD.get(),
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).build(consumer);

        shapeless(ModBlocks.ASSEMBLY_IO_UNIT_EXPORT.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                ModBlocks.ASSEMBLY_IO_UNIT_IMPORT.get()
        ).build(consumer, RL("assembly_io_unit_export_from_import"));

        shapeless(ModBlocks.ASSEMBLY_IO_UNIT_IMPORT.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                ModBlocks.ASSEMBLY_IO_UNIT_EXPORT.get()
        ).build(consumer, RL("assembly_io_unit_import_from_export"));

        shaped(ModBlocks.ASSEMBLY_PLATFORM.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                "C C/PPP/IBI",
                'C', ModItems.PNEUMATIC_CYLINDER.get(),
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS,
                'B', ModItems.PRINTED_CIRCUIT_BOARD.get(),
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).build(consumer);

        shapeless(ModItems.ASSEMBLY_PROGRAM_DRILL_LASER.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                ModItems.ASSEMBLY_PROGRAM_LASER.get(),
                ModItems.ASSEMBLY_PROGRAM_DRILL.get()
        ).build(consumer);

        shaped(ModItems.CANNON_BARREL.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "I I/I I/IPI",
                'I', ModBlocks.REINFORCED_BRICK_WALL.get(),
                'P', ModBlocks.PRESSURE_TUBE.get()
        ).build(consumer);

        shaped(ModItems.CHARGING_MODULE.get(), ModBlocks.CHARGING_STATION.get(),
                " C /CPC/ C ",
                'C', ModBlocks.CHARGING_STATION.get(),
                'P', ModBlocks.PRESSURE_TUBE.get()
        ).build(consumer);

        shaped(ModBlocks.CHARGING_STATION.get(), ModBlocks.PRESSURE_TUBE.get(),
                "  T/PPP/SSS",
                'T', ModBlocks.PRESSURE_TUBE.get(),
                'P', Items.BRICK,
                'S', ModBlocks.REINFORCED_STONE_SLAB.get()
        ).build(consumer);

        shaped(ModItems.COLLECTOR_DRONE.get(), ModItems.TURBINE_ROTOR.get(),
                " R /RSR/ R ",
                'S', Items.HOPPER,
                'R', ModItems.TURBINE_ROTOR.get()
        ).build(consumer);

        shaped(ModBlocks.COMPRESSED_IRON_BLOCK.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "III/III/III",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).build(consumer, RL("compressed_iron_block_from_ingot"));

        shaped(ModItems.COMPRESSED_IRON_GEAR.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                " C /CIC/ C ",
                'C', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'I', Tags.Items.INGOTS_IRON
        ).build(consumer);

        shaped(ModItems.CROP_SUPPORT.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "I I/I I",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).build(consumer);

        shaped(ModBlocks.DISPLAY_TABLE.get(), ModBlocks.REINFORCED_STONE.get(),
                "SSS/I I",
                'S', ModBlocks.REINFORCED_STONE_SLAB.get(),
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).build(consumer);

        shaped(ModBlocks.DRILL_PIPE.get(), 3, ModBlocks.GAS_LIFT.get(),
                "T/T/T",
                'T', ModBlocks.PRESSURE_TUBE.get()
        ).build(consumer);

        shaped(ModItems.DRONE.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                " B /BPB/ B ",
                'B', ModItems.TURBINE_ROTOR.get(),
                'P', ModItems.PRINTED_CIRCUIT_BOARD.get()
        ).build(consumer);

        Item ccModem = ForgeRegistries.ITEMS.getValue(new ResourceLocation("computercraft:wireless_modem_normal"));
        ConditionalRecipe.builder()
                .addCondition(new ModLoadedCondition("computercraft"))
                .addRecipe(
                        shaped(ModBlocks.DRONE_INTERFACE.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                                " U /MP /III",
                                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                                'U', EnumUpgrade.RANGE.getItem(),
                                'P', ModItems.PRINTED_CIRCUIT_BOARD.get(),
                                'M', ccModem)
                                ::build)
                .build(consumer, RL("drone_interface"));

        shaped(ModBlocks.ELECTROSTATIC_COMPRESSOR.get(), ModItems.TURBINE_ROTOR.get(),
                "BPB/PRP/BCB",
                'B', Blocks.IRON_BARS,
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS,
                'R', ModItems.TURBINE_ROTOR.get(),
                'C', ModBlocks.AIR_COMPRESSOR.get()
        ).build(consumer);

        shaped(ModBlocks.ELEVATOR_BASE.get(), ModItems.PLASTIC.get(),
                "CP/PC",
                'C', ModItems.PNEUMATIC_CYLINDER.get(),
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS
        ).build(consumer, RL("elevator_base_1"));
        shaped(ModBlocks.ELEVATOR_BASE.get(), ModItems.PLASTIC.get(),
                "PC/CP",
                'C', ModItems.PNEUMATIC_CYLINDER.get(),
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS
        ).build(consumer, RL("elevator_base_2"));

        shaped(ModBlocks.ELEVATOR_CALLER.get(), ModItems.PLASTIC.get(),
                "BPB/PRP/BPB",
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS,
                'B', Blocks.STONE_BUTTON,
                'R', Tags.Blocks.STONE
        ).build(consumer);

        shaped(ModBlocks.ELEVATOR_FRAME.get(), 4, ModItems.COMPRESSED_IRON_INGOT.get(),
                "I I/I I/I I",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).build(consumer);

        shaped(ModBlocks.ETCHING_TANK.get(), ModItems.ETCHING_ACID_BUCKET.get(),
                "OGO/WTW/SSS",
                'O', Tags.Blocks.OBSIDIAN,
                'W', ModBlocks.REINFORCED_BRICK_WALL.get(),
                'T', ModBlocks.TANK_SMALL.get(),
                'S', ModBlocks.REINFORCED_BRICK_SLAB.get(),
                'G', Tags.Items.GLASS_PANES
        ).build(consumer);

        shaped(ModItems.FLOW_DETECTOR_MODULE.get(), ModItems.TURBINE_BLADE.get(),
                "B B/ T /B B",
                'B', ModItems.TURBINE_BLADE.get(),
                'T', ModBlocks.PRESSURE_TUBE.get()
        ).build(consumer);

        shaped(ModBlocks.FLUX_COMPRESSOR.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                "GCP/FRT/GQP",
                'G', Tags.Items.DUSTS_REDSTONE,
                'C', ModItems.COMPRESSED_IRON_GEAR.get(),
                'P', ModItems.PRINTED_CIRCUIT_BOARD.get(),
                'F', Tags.Items.STORAGE_BLOCKS_REDSTONE,
                'R', ModItems.TURBINE_ROTOR.get(),
                'T', ModBlocks.ADVANCED_PRESSURE_TUBE.get(),
                'Q', Blocks.BLAST_FURNACE
        ).build(consumer);

        shaped(ModBlocks.GAS_LIFT.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                " T /TGT/SSS",
                'T', ModBlocks.PRESSURE_TUBE.get(),
                'G', ModBlocks.TANK_SMALL.get(),
                'S', ModBlocks.REINFORCED_STONE_SLAB.get()
        ).build(consumer);

        shaped(ModItems.GPS_TOOL.get(), ModItems.PLASTIC.get(),
                " R /PGP/PDP",
                'R', Blocks.REDSTONE_TORCH,
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS,
                'G', Tags.Items.GLASS_PANES,
                'D', Tags.Items.GEMS_DIAMOND
        ).build(consumer);

        shapeless(ModItems.GPS_AREA_TOOL.get(), ModItems.PLASTIC.get(),
                ModItems.GPS_TOOL.get(), ModItems.GPS_TOOL.get()).build(consumer);

        shaped(ModItems.GUARD_DRONE.get(), ModItems.TURBINE_ROTOR.get(),
                " R /RSR/ R ",
                'S', Items.IRON_SWORD,
                'R', ModItems.TURBINE_ROTOR.get()
        ).build(consumer);

        shapeless(ModItems.GUN_AMMO.get(), ModItems.MINIGUN.get(),
                Tags.Items.GUNPOWDER, PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON, Tags.Items.INGOTS_GOLD
        ).build(consumer);
        miniGunAmmo(ModItems.GUN_AMMO_AP.get(), Tags.Items.GEMS_DIAMOND, Tags.Items.GEMS_DIAMOND).build(consumer);
        miniGunAmmo(ModItems.GUN_AMMO_EXPLOSIVE.get(), Blocks.TNT, Blocks.TNT).build(consumer);
        miniGunAmmo(ModItems.GUN_AMMO_FREEZING.get(), Blocks.ICE, Blocks.ICE).build(consumer);
        miniGunAmmo(ModItems.GUN_AMMO_INCENDIARY.get(), Tags.Items.RODS_BLAZE, Tags.Items.RODS_BLAZE).build(consumer);
        miniGunAmmo(ModItems.GUN_AMMO_WEIGHTED.get(), Tags.Items.STORAGE_BLOCKS_GOLD, Tags.Items.OBSIDIAN).build(consumer);

        shaped(ModItems.HARVESTING_DRONE.get(), ModItems.TURBINE_ROTOR.get(),
                " R /RSR/ R ",
                'S', Tags.Items.CROPS,
                'R', ModItems.TURBINE_ROTOR.get()
        ).build(consumer);

        shaped(ModItems.HEAT_FRAME.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "III/IFI/III",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'F', Blocks.FURNACE
        ).build(consumer);

        shaped(ModBlocks.HEAT_PIPE.get(), 6, ModItems.COMPRESSED_IRON_INGOT.get(),
                "WWW/BBB/WWW",
                'W', ModBlocks.THERMAL_LAGGING.get(),
                'B', PneumaticCraftTags.Items.STORAGE_BLOCKS_COMPRESSED_IRON
        ).build(consumer);

        shaped(ModBlocks.HEAT_SINK.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "BBB/IGI",
                'B', Blocks.IRON_BARS,
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'G', Tags.Items.INGOTS_GOLD
        ).build(consumer);

        shapeless(ModItems.COMPRESSED_IRON_INGOT.get(), 9, ModBlocks.COMPRESSED_IRON_BLOCK.get(),
                ModBlocks.COMPRESSED_IRON_BLOCK.get()
        ).build(consumer, RL("compressed_iron_ingot_from_block"));

        shaped(ModBlocks.KEROSENE_LAMP.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                " I /G G/IBI",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'G', Tags.Items.GLASS_PANES,
                'B', ModBlocks.TANK_SMALL.get()
        ).build(consumer);

        shaped(ModBlocks.LIQUID_COMPRESSOR.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "PBP/LCL",
                'P', ModBlocks.PRESSURE_TUBE.get(),
                'B', ModBlocks.TANK_SMALL.get(),
                'L', Tags.Items.LEATHER,
                'C', ModBlocks.AIR_COMPRESSOR.get()
        ).build(consumer);

        shaped(ModBlocks.LIQUID_HOPPER.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "T/H",
                'T', ModBlocks.TANK_SMALL.get(),
                'H', Blocks.HOPPER
        ).build(consumer);

        shaped(ModItems.LOGISTICS_CORE.get(), 2, ModItems.COMPRESSED_IRON_INGOT.get(),
                "BBB/BRB/BBB",
                'B', ModBlocks.REINFORCED_BRICK_TILE.get(),
                'R', Tags.Items.DUSTS_REDSTONE
        ).build(consumer);

        shaped(ModItems.LOGISTICS_DRONE.get(), ModItems.TURBINE_ROTOR.get(),
                " B /BCB/ B ",
                'B', ModItems.TURBINE_ROTOR.get(),
                'C', ModItems.LOGISTICS_CORE.get()
        ).build(consumer);

        shaped(ModItems.LOGISTICS_MODULE.get(), ModItems.LOGISTICS_CORE.get(),
                " R /RCR/TRT",
                'R', Tags.Items.DUSTS_REDSTONE,
                'C', ModItems.LOGISTICS_CORE.get(),
                'T', ModBlocks.PRESSURE_TUBE.get()
        ).build(consumer);

        shapedPressure(ModItems.MANOMETER.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "G/C",
                'G', ModItems.PRESSURE_GAUGE.get(),
                'C', ModItems.AIR_CANISTER.get()
        ).build(consumer);

        shaped(ModItems.MEMORY_STICK.get(), ModItems.PLASTIC.get(),
                "DED/PSP/G G",
                'D', Tags.Items.GEMS_DIAMOND,
                'E', Tags.Items.GEMS_EMERALD,
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS,
                'S', Blocks.SOUL_SAND,
                'G', Tags.Items.INGOTS_GOLD
        ).build(consumer);

        shaped(ModItems.MICROMISSILES.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                " T /WPW/WFW",
                'W', PneumaticCraftTags.Items.PLASTIC_SHEETS,
                'P', ModItems.PRINTED_CIRCUIT_BOARD.get(),
                'T', Blocks.TNT,
                'F', Items.FIRE_CHARGE
        ).build(consumer);

        shapedPressure(ModItems.MINIGUN.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "A  /CIB/GL ",
                'A', ModItems.AIR_CANISTER.get(),
                'C', Tags.Items.CHESTS_WOODEN,
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'B', ModItems.CANNON_BARREL.get(),
                'G', Tags.Items.INGOTS_GOLD,
                'L', Blocks.LEVER
        ).build(consumer);

        shaped(ModBlocks.OMNIDIRECTIONAL_HOPPER.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "I I/ICI/ I ",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'C', Tags.Items.CHESTS_WOODEN
        ).build(consumer);

        shapedPressure(ModItems.PNEUMATIC_BOOTS.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                "CPC/CAC",
                'C', ModItems.AIR_CANISTER.get(),
                'P', ModItems.PRINTED_CIRCUIT_BOARD.get(),
                'A', Items.LEATHER_BOOTS
        ).build(consumer);
        shapedPressure(ModItems.PNEUMATIC_CHESTPLATE.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                "CPC/CAC/CCC",
                'C', ModItems.AIR_CANISTER.get(),
                'P', ModItems.PRINTED_CIRCUIT_BOARD.get(),
                'A', Items.LEATHER_CHESTPLATE
        ).build(consumer);
        shapedPressure(ModItems.PNEUMATIC_HELMET.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                "CPC/CAC/CCC",
                'C', ModItems.AIR_CANISTER.get(),
                'P', ModItems.PRINTED_CIRCUIT_BOARD.get(),
                'A', Items.LEATHER_HELMET
        ).build(consumer);
        shapedPressure(ModItems.PNEUMATIC_LEGGINGS.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                "CPC/CAC/I I",
                'C', ModItems.AIR_CANISTER.get(),
                'P', ModItems.PRINTED_CIRCUIT_BOARD.get(),
                'A', Items.LEATHER_LEGGINGS,
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).build(consumer);

        shaped(ModItems.PNEUMATIC_CYLINDER.get(), 2, ModItems.PLASTIC.get(),
                "PIP/PIP/PBP",
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS,
                'B', ModItems.CANNON_BARREL.get(),
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).build(consumer);

        shaped(ModBlocks.PNEUMATIC_DOOR_BASE.get(), ModItems.PLASTIC.get(),
                " CI/IIT/III",
                'C', ModItems.PNEUMATIC_CYLINDER.get(),
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'T', ModBlocks.PRESSURE_TUBE.get()
        ).build(consumer);

        shaped(ModBlocks.PNEUMATIC_DOOR.get(), ModItems.PLASTIC.get(),
                "II/II/II",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).build(consumer);

        shaped(ModBlocks.PNEUMATIC_DYNAMO.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                " T /GIG/IPI",
                'T', ModBlocks.ADVANCED_PRESSURE_TUBE.get(),
                'G', ModItems.COMPRESSED_IRON_GEAR.get(),
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'P', ModItems.PRINTED_CIRCUIT_BOARD.get()
        ).build(consumer);

        shaped(ModBlocks.PRESSURE_CHAMBER_GLASS.get(), 16, ModItems.COMPRESSED_IRON_INGOT.get(),
                "III/IGI/III",
                'I', ModBlocks.REINFORCED_BRICKS.get(),
                'G', Tags.Items.GLASS
        ).build(consumer);
        shapeless(ModBlocks.PRESSURE_CHAMBER_GLASS.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                Tags.Items.GLASS, ModBlocks.PRESSURE_CHAMBER_WALL.get()
        ).build(consumer, RL("pressure_chamber_glass_x1"));
        shapeless(ModBlocks.PRESSURE_CHAMBER_GLASS.get(), 4, ModItems.COMPRESSED_IRON_INGOT.get(),
                Tags.Items.GLASS,
                ModBlocks.PRESSURE_CHAMBER_WALL.get(),
                ModBlocks.PRESSURE_CHAMBER_WALL.get(),
                ModBlocks.PRESSURE_CHAMBER_WALL.get(),
                ModBlocks.PRESSURE_CHAMBER_WALL.get()
        ).build(consumer, RL("pressure_chamber_glass_x4"));

        shapeless(ModBlocks.PRESSURE_CHAMBER_INTERFACE.get(), 2, ModItems.COMPRESSED_IRON_INGOT.get(),
                Blocks.HOPPER, ModBlocks.PRESSURE_CHAMBER_WALL.get(), ModBlocks.PRESSURE_CHAMBER_WALL.get()
        ).build(consumer);

        shaped(ModBlocks.PRESSURE_CHAMBER_VALVE.get(), 16, ModItems.COMPRESSED_IRON_INGOT.get(),
                "III/ITI/III",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'T', ModBlocks.PRESSURE_TUBE.get()
        ).build(consumer);
        shapeless(ModBlocks.PRESSURE_CHAMBER_VALVE.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                ModBlocks.PRESSURE_TUBE.get(), ModBlocks.PRESSURE_CHAMBER_WALL.get()
        ).build(consumer, RL("pressure_chamber_valve_x1"));
        shapeless(ModBlocks.PRESSURE_CHAMBER_VALVE.get(), 4, ModItems.COMPRESSED_IRON_INGOT.get(),
                ModBlocks.PRESSURE_TUBE.get(),
                ModBlocks.PRESSURE_CHAMBER_WALL.get(),
                ModBlocks.PRESSURE_CHAMBER_WALL.get(),
                ModBlocks.PRESSURE_CHAMBER_WALL.get(),
                ModBlocks.PRESSURE_CHAMBER_WALL.get()
        ).build(consumer, RL("pressure_chamber_valve_x4"));

        shaped(ModBlocks.PRESSURE_CHAMBER_WALL.get(), 16, ModItems.COMPRESSED_IRON_INGOT.get(),
                "III/I I/III",
                'I', ModBlocks.REINFORCED_BRICKS.get()
        ).build(consumer);

        shaped(ModItems.PRESSURE_GAUGE.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                " G /GIG/ G ",
                'G', Tags.Items.INGOTS_GOLD,
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).build(consumer);

        shaped(ModItems.PRESSURE_GAUGE_MODULE.get(), ModItems.PRESSURE_GAUGE.get(),
                " G /RTR",
                'G', ModItems.PRESSURE_GAUGE.get(),
                'R', Tags.Items.DUSTS_REDSTONE,
                'T', ModBlocks.PRESSURE_TUBE.get()
        ).build(consumer);

        shaped(ModBlocks.PRESSURE_TUBE.get(), 8, ModItems.COMPRESSED_IRON_INGOT.get(),
                "IGI",
                'G', Tags.Items.GLASS,
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).build(consumer);

        shaped(ModItems.PRINTED_CIRCUIT_BOARD.get(), ModItems.PLASTIC.get(),
                " T /CUC/ T ",
                'T', ModItems.TRANSISTOR.get(),
                'C', ModItems.CAPACITOR.get(),
                'U', ModItems.UNASSEMBLED_PCB.get()
        ).build(consumer);

        shaped(ModBlocks.PROGRAMMABLE_CONTROLLER.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                "IRI/CDP/INI",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'R', ModItems.REMOTE.get(),
                'C', ModItems.PRINTED_CIRCUIT_BOARD.get(),
                'P', ModBlocks.ADVANCED_PRESSURE_TUBE.get(),
                'D', ModItems.DRONE.get(),
                'N', ModItems.NETWORK_REGISTRY.get()
        ).build(consumer);

        shaped(ModBlocks.PROGRAMMER.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                "RGR/TBT/P P",
                'R', Tags.Items.DYES_RED,
                'G', Tags.Items.GLASS_PANES_BLACK,
                'T', ModItems.TURBINE_ROTOR.get(),
                'B', ModItems.PRINTED_CIRCUIT_BOARD.get(),
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS
        ).build(consumer);

        shaped(ModItems.PROGRAMMING_PUZZLE.get(),8, ModItems.PRINTED_CIRCUIT_BOARD.get(),
                "PPP/PCP/PPP",
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS,
                'C', ModItems.PRINTED_CIRCUIT_BOARD.get()
        ).build(consumer);

        shaped(ModItems.REDSTONE_MODULE.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                " R /TDT",
                'R', Tags.Items.DUSTS_REDSTONE,
                'T', ModBlocks.PRESSURE_TUBE.get(),
                'D', Blocks.REPEATER
        ).build(consumer);

        shaped(ModBlocks.REFINERY.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "SSS/RTR/SSS",
                'S', ModBlocks.REINFORCED_STONE_SLAB.get(),
                'T', ModBlocks.TANK_SMALL.get(),
                'R', Tags.Items.DUSTS_REDSTONE
        ).build(consumer);

        shaped(ModBlocks.REFINERY_OUTPUT.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "SSS/GDG/SSS",
                'S', ModBlocks.REINFORCED_STONE_SLAB.get(),
                'G', Tags.Items.GLASS,
                'D', Tags.Items.GEMS_DIAMOND
        ).build(consumer);

        shaped(ModItems.REGULATOR_TUBE_MODULE.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "STS",
                'S', ModItems.SAFETY_TUBE_MODULE.get(),
                'T', ModBlocks.PRESSURE_TUBE.get()
        ).build(consumer);

        shaped(ModItems.REINFORCED_AIR_CANISTER.get(), ModBlocks.ADVANCED_PRESSURE_TUBE.get(),
                " T /ICI/III",
                'T', ModBlocks.ADVANCED_PRESSURE_TUBE.get(),
                'C', ModItems.AIR_CANISTER.get(),
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).build(consumer);

        shaped(ModBlocks.REINFORCED_CHEST.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "IGI/WCW/IOI",
                'G', Tags.Items.NUGGETS_GOLD,
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'W', ModBlocks.REINFORCED_BRICK_WALL.get(),
                'O', Tags.Blocks.OBSIDIAN,
                'C', Tags.Items.CHESTS_WOODEN
        ).build(consumer);

        shaped(ModItems.REMOTE.get(), ModItems.TRANSISTOR.get(),
                " I /TGT/TDT",
                'I', ModItems.NETWORK_IO_PORT.get(),
                'D', ModItems.NETWORK_DATA_STORAGE.get(),
                'G', ModItems.GPS_TOOL.get(),
                'T', ModItems.TRANSISTOR.get()
        ).build(consumer);

        shaped(ModItems.SAFETY_TUBE_MODULE.get(), ModItems.PRESSURE_GAUGE.get(),
                " G /LTL",
                'G', ModItems.PRESSURE_GAUGE.get(),
                'L', Blocks.LEVER,
                'T', ModBlocks.PRESSURE_TUBE.get()
        ).build(consumer);

        shaped(ModBlocks.SECURITY_STATION.get(), ModItems.PRINTED_CIRCUIT_BOARD.get(),
                "DBD/TPT/G G",
                'D', Tags.Items.DYES_GRAY,
                'G', PneumaticCraftTags.Items.PLASTIC_SHEETS,
                'B', Tags.Items.GLASS_PANES_BLACK,
                'T', ModItems.TURBINE_ROTOR.get(),
                'P', ModItems.PRINTED_CIRCUIT_BOARD.get()
        ).build(consumer);

        shaped(ModItems.SEISMIC_SENSOR.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                " T /GRG/GCG",
                'T', Blocks.REDSTONE_TORCH,
                'G', Tags.Items.GLASS,
                'R', Blocks.REPEATER,
                'C', Blocks.NOTE_BLOCK
        ).build(consumer);

        shaped(ModBlocks.SENTRY_TURRET.get(), ModItems.PLASTIC.get(),
                " M /PIP/I I",
                'M', ModItems.MINIGUN.get(),
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS,
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).build(consumer);

        shaped(ModBlocks.SMART_CHEST.get(), ModBlocks.REINFORCED_CHEST.get(),
                "DPD/CHC",
                'D', Tags.Items.GEMS_DIAMOND,
                'H', ModBlocks.OMNIDIRECTIONAL_HOPPER.get(),
                'C', ModBlocks.REINFORCED_CHEST.get(),
                'P', ModItems.PRINTED_CIRCUIT_BOARD.get()
        ).build(consumer);

        shaped(ModItems.SPAWNER_AGITATOR.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "III/IGI/III",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'G', Items.GHAST_TEAR
        ).build(consumer);

        shaped(ModItems.STONE_BASE.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "S S/STS",
                'S', Tags.Items.STONE,
                'T', ModBlocks.PRESSURE_TUBE.get()
        ).build(consumer);

        shaped(ModBlocks.TAG_WORKBENCH.get(), ModBlocks.DISPLAY_TABLE.get(),
                "B/D",
                'B', Items.WRITABLE_BOOK,
                'D', ModBlocks.DISPLAY_TABLE.get()
        ).build(consumer);

        shaped(ModBlocks.TANK_SMALL.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "BIB/IGI/BIB",
                'B', Blocks.IRON_BARS,
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'G', Tags.Items.GLASS
        ).build(consumer);

        shaped(ModBlocks.TANK_MEDIUM.get(), ModItems.PLASTIC.get(),
                "PSP/ITI/PSP",
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS,
                'S', ModBlocks.TANK_SMALL.get(),
                'I', Tags.Items.INGOTS_GOLD,
                'T', ModBlocks.PRESSURE_TUBE.get()
        ).build(consumer);

        shaped(ModBlocks.TANK_LARGE.get(), ModBlocks.ADVANCED_PRESSURE_TUBE.get(),
                "PMP/DTD/PMP",
                'M', ModBlocks.TANK_MEDIUM.get(),
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS,
                'D', Tags.Items.GEMS_DIAMOND,
                'T', ModBlocks.ADVANCED_PRESSURE_TUBE.get()
        ).build(consumer);

        shaped(ModBlocks.THERMAL_COMPRESSOR.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "ITI/PAP/ITI",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'T', ModBlocks.PRESSURE_TUBE.get(),
                'A', ModBlocks.AIR_COMPRESSOR.get(),
                'P', Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE
        ).build(consumer);

        shaped(ModBlocks.THERMAL_LAGGING.get(), 6, ModItems.COMPRESSED_IRON_INGOT.get(),
                "WGW/GWG/WGW",
                'W', ItemTags.WOOL,
                'G', Tags.Items.GLASS
        ).build(consumer);

        shaped(ModBlocks.THERMOPNEUMATIC_PROCESSING_PLANT.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "SSS/TPT/SSS",
                'S', ModBlocks.REINFORCED_STONE_SLAB.get(),
                'T', ModBlocks.TANK_SMALL.get(),
                'P', ModBlocks.PRESSURE_TUBE.get()
        ).build(consumer);

        shapeless(ModItems.TRANSFER_GADGET.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                Blocks.HOPPER, PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).build(consumer);

        shaped(ModItems.TURBINE_ROTOR.get(), ModItems.TURBINE_BLADE.get(),
                " B / I /B B",
                'B', ModItems.TURBINE_BLADE.get(),
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).build(consumer);

        shaped(ModBlocks.UNIVERSAL_SENSOR.get(), ModItems.PLASTIC.get(),
                " S /PRP/PCP",
                'S', ModItems.SEISMIC_SENSOR.get(),
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS,
                'R', Blocks.REPEATER,
                'C', ModBlocks.PRESSURE_TUBE.get()
        ).build(consumer);

        shaped(ModBlocks.UV_LIGHT_BOX.get(), ModItems.PCB_BLUEPRINT.get(),
                "LLL/IBT/III",
                'L', Blocks.REDSTONE_LAMP,
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'B', ModItems.PCB_BLUEPRINT.get(),
                'T', ModBlocks.PRESSURE_TUBE.get()
        ).build(consumer);

        shaped(ModBlocks.VACUUM_PUMP.get(), ModItems.TURBINE_ROTOR.get(),
                "GRG/TRT/SSS",
                'G', ModItems.PRESSURE_GAUGE.get(),
                'R', ModItems.TURBINE_ROTOR.get(),
                'T', ModBlocks.PRESSURE_TUBE.get(),
                'S', ModBlocks.REINFORCED_STONE_SLAB.get()
        ).build(consumer);

        shaped(ModBlocks.VORTEX_TUBE.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "ITI/GTG/III",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'T', ModBlocks.PRESSURE_TUBE.get(),
                'G', Tags.Items.INGOTS_GOLD
        ).build(consumer);

        shapeless(Items.PAPER, Items.PAPER, ModItems.TAG_FILTER.get()).build(consumer, RL("paper_from_tag_filter"));

        // network components
        networkComponent(ModItems.DIAGNOSTIC_SUBROUTINE.get(), 1, PneumaticCraftTags.Items.PLASTIC_SHEETS, Tags.Items.DYES_RED).build(consumer);
        networkComponent(ModItems.NETWORK_API.get(), 1, PneumaticCraftTags.Items.PLASTIC_SHEETS, Tags.Items.DYES_BLUE).build(consumer);
        networkComponent(ModItems.NETWORK_DATA_STORAGE.get(), 1, PneumaticCraftTags.Items.PLASTIC_SHEETS, Tags.Items.DYES_GRAY).build(consumer);
        networkComponent(ModItems.NETWORK_IO_PORT.get(), 1, ModItems.CAPACITOR.get(), Tags.Items.DYES_CYAN).build(consumer);
        networkComponent(ModItems.NETWORK_REGISTRY.get(), 1, PneumaticCraftTags.Items.PLASTIC_SHEETS, Tags.Items.DYES_LIME).build(consumer);
        networkComponent(ModItems.NETWORK_NODE.get(), 16, ModItems.TRANSISTOR.get(), Tags.Items.DYES_PURPLE).build(consumer);

        // logistics frames
        logisticsFrame(ModItems.LOGISTICS_FRAME_ACTIVE_PROVIDER.get(), Tags.Items.DYES_PURPLE).build(consumer);
        logisticsFrame(ModItems.LOGISTICS_FRAME_PASSIVE_PROVIDER.get(), Tags.Items.DYES_RED).build(consumer);
        logisticsFrame(ModItems.LOGISTICS_FRAME_REQUESTER.get(), Tags.Items.DYES_BLUE).build(consumer);
        logisticsFrame(ModItems.LOGISTICS_FRAME_STORAGE.get(), Tags.Items.DYES_YELLOW).build(consumer);
        logisticsFrame(ModItems.LOGISTICS_FRAME_DEFAULT_STORAGE.get(), Tags.Items.DYES_GREEN).build(consumer);
        buildLogisticsFrameSelfCraft(ModItems.LOGISTICS_FRAME_ACTIVE_PROVIDER.get(), consumer);
        buildLogisticsFrameSelfCraft(ModItems.LOGISTICS_FRAME_PASSIVE_PROVIDER.get(), consumer);
        buildLogisticsFrameSelfCraft(ModItems.LOGISTICS_FRAME_REQUESTER.get(), consumer);
        buildLogisticsFrameSelfCraft(ModItems.LOGISTICS_FRAME_STORAGE.get(), consumer);
        buildLogisticsFrameSelfCraft(ModItems.LOGISTICS_FRAME_DEFAULT_STORAGE.get(), consumer);

        // pressurizable tools
        pneumaticTool(ModItems.CAMO_APPLICATOR.get(), Tags.Items.DYES_BLUE).build(consumer);
        pneumaticTool(ModItems.PNEUMATIC_WRENCH.get(), Tags.Items.DYES_ORANGE).build(consumer);
        pneumaticTool(ModItems.LOGISTICS_CONFIGURATOR.get(), Tags.Items.DYES_RED).build(consumer);
        pneumaticTool(ModItems.VORTEX_CANNON.get(), Tags.Items.DYES_YELLOW).build(consumer);

        // standard upgrade patterns (4 x lapis, 4 x edge item, 1 x center item)
        standardUpgrade(EnumUpgrade.ARMOR, PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON, Tags.Items.GEMS_DIAMOND).build(consumer);
        standardUpgrade(EnumUpgrade.BLOCK_TRACKER, Items.FERMENTED_SPIDER_EYE, ModBlocks.PRESSURE_CHAMBER_WALL.get()).build(consumer);
        standardUpgrade(EnumUpgrade.CHARGING, ModItems.CHARGING_MODULE.get(), ModBlocks.PRESSURE_TUBE.get()).build(consumer);
        standardUpgrade(EnumUpgrade.COORDINATE_TRACKER, ModItems.GPS_TOOL.get(), Tags.Items.DUSTS_REDSTONE).build(consumer);
        standardUpgrade(EnumUpgrade.DISPENSER, Blocks.DISPENSER, Tags.Items.GEMS_QUARTZ).build(consumer);
        standardUpgrade(EnumUpgrade.ENTITY_TRACKER, Items.FERMENTED_SPIDER_EYE, Tags.Items.BONES).build(consumer);
        standardUpgrade(EnumUpgrade.FLIPPERS, Items.BLACK_WOOL, PneumaticCraftTags.Items.PLASTIC_SHEETS).build(consumer);
        standardUpgrade(EnumUpgrade.INVENTORY, Tags.Items.CHESTS_WOODEN, ItemTags.PLANKS).build(consumer);
        standardUpgrade(EnumUpgrade.ITEM_LIFE, Items.CLOCK, Items.APPLE).build(consumer);
        standardUpgrade(EnumUpgrade.MAGNET, PneumaticCraftTags.Items.PLASTIC_SHEETS, PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON).build(consumer);
        standardUpgrade(EnumUpgrade.MINIGUN, ModItems.MINIGUN.get(), Tags.Items.GUNPOWDER).build(consumer);
        standardUpgrade(EnumUpgrade.RANGE, Items.BOW, Tags.Items.ARROWS).build(consumer);
        standardUpgrade(EnumUpgrade.SEARCH, Items.GOLDEN_CARROT, Items.ENDER_EYE).build(consumer);
        standardUpgrade(EnumUpgrade.SECURITY, ModItems.SAFETY_TUBE_MODULE.get(), Tags.Items.OBSIDIAN).build(consumer);
        standardUpgrade(EnumUpgrade.SPEED, FluidIngredient.of(1000, ModFluids.LUBRICANT.get()), Items.SUGAR).build(consumer);
        standardUpgrade(EnumUpgrade.STANDBY, ItemTags.BEDS, Items.REDSTONE_TORCH).build(consumer);
        standardUpgrade(EnumUpgrade.VOLUME, ModItems.AIR_CANISTER.get(), PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON).build(consumer);

        // non-standard upgrade patterns
        ItemStack nightVisionPotion = new ItemStack(Items.POTION);
        PotionUtils.addPotionToItemStack(nightVisionPotion, Potions.LONG_NIGHT_VISION);
        shaped(EnumUpgrade.NIGHT_VISION.getItem(), ModItems.PNEUMATIC_HELMET.get(),
                "LNL/GNG/LNL",
                'L', PneumaticCraftTags.Items.UPGRADE_COMPONENTS,
                'G', ModBlocks.PRESSURE_CHAMBER_GLASS.get(),
                'N', IngredientNBTWrapper.fromItemStack(nightVisionPotion)
        ).build(consumer);

        shaped(EnumUpgrade.SCUBA.getItem(), ModItems.PNEUMATIC_HELMET.get(),
                "LTL/PRP/LPL",
                'L', PneumaticCraftTags.Items.UPGRADE_COMPONENTS,
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS,
                'R', ModItems.REGULATOR_TUBE_MODULE.get(),
                'T', ModBlocks.ADVANCED_PRESSURE_TUBE.get()
        ).build(consumer);

        shaped(EnumUpgrade.JET_BOOTS.getItem(1), ModItems.PNEUMATIC_BOOTS.get(),
                "LTL/VCV/LTL",
                'L', PneumaticCraftTags.Items.UPGRADE_COMPONENTS,
                'V', ModItems.VORTEX_CANNON.get(),
                'C', ModBlocks.ADVANCED_AIR_COMPRESSOR.get(),
                'T', ModBlocks.ADVANCED_PRESSURE_TUBE.get()
        ).build(consumer);
        shaped(EnumUpgrade.JET_BOOTS.getItem(2), ModItems.PNEUMATIC_BOOTS.get(),
                "FFF/VUV/CFC",
                'F', Items.FEATHER,
                'V', ModItems.VORTEX_CANNON.get(),
                'C', ModItems.PNEUMATIC_CYLINDER.get(),
                'U', EnumUpgrade.JET_BOOTS.getItem(1)
        ).build(consumer);
        shaped(EnumUpgrade.JET_BOOTS.getItem(3), ModItems.PNEUMATIC_BOOTS.get(),
                "TBT/VUV/TBT",
                'T', Items.GHAST_TEAR,
                'B', Items.BLAZE_ROD,
                'V', ModItems.VORTEX_CANNON.get(),
                'U', EnumUpgrade.JET_BOOTS.getItem(2)
        ).build(consumer);
        ItemStack slowFallPotion = new ItemStack(Items.POTION);
        PotionUtils.addPotionToItemStack(slowFallPotion, Potions.LONG_SLOW_FALLING);
        shaped(EnumUpgrade.JET_BOOTS.getItem(4), ModItems.PNEUMATIC_BOOTS.get(),
                "MNM/VUV/P P",
                'N', Items.NETHER_STAR,
                'M', Items.PHANTOM_MEMBRANE,
                'V', ModItems.VORTEX_CANNON.get(),
                'P', IngredientNBTWrapper.fromItemStack(slowFallPotion),
                'U', EnumUpgrade.JET_BOOTS.getItem(3)
        ).build(consumer);
        shaped(EnumUpgrade.JET_BOOTS.getItem(5), ModItems.PNEUMATIC_BOOTS.get(),
                "RER/VUV/RDR",
                'R', Items.END_ROD,
                'E', Items.ELYTRA,
                'V', ModItems.VORTEX_CANNON.get(),
                'D', Items.DRAGON_BREATH,
                'U', EnumUpgrade.JET_BOOTS.getItem(4)
        ).build(consumer);

        shaped(EnumUpgrade.JUMPING.getItem(1), ModItems.PNEUMATIC_LEGGINGS.get(),
                "LCL/VTV/LPL",
                'L', PneumaticCraftTags.Items.UPGRADE_COMPONENTS,
                'P', Blocks.PISTON,
                'V', ModItems.VORTEX_CANNON.get(),
                'T', ModBlocks.PRESSURE_TUBE.get(),
                'C', ModItems.PNEUMATIC_CYLINDER.get()
        ).build(consumer);
        shaped(EnumUpgrade.JUMPING.getItem(2), ModItems.PNEUMATIC_LEGGINGS.get(),
                "PCP/SUS",
                'U', EnumUpgrade.JUMPING.getItem(1),
                'S', Blocks.SLIME_BLOCK,
                'P', Blocks.PISTON,
                'C', ModItems.PNEUMATIC_CYLINDER.get()
        ).build(consumer);
        ItemStack jumpBoostPotion1 = new ItemStack(Items.POTION);
        PotionUtils.addPotionToItemStack(jumpBoostPotion1, Potions.LEAPING);
        shaped(EnumUpgrade.JUMPING.getItem(3), ModItems.PNEUMATIC_LEGGINGS.get(),
                "PCP/JUJ/ J ",
                'U', EnumUpgrade.JUMPING.getItem(2),
                'J', IngredientNBTWrapper.fromItemStack(jumpBoostPotion1),
                'P', Blocks.PISTON,
                'C', ModItems.PNEUMATIC_CYLINDER.get()
        ).build(consumer);
        ItemStack jumpBoostPotion2 = new ItemStack(Items.POTION);
        PotionUtils.addPotionToItemStack(jumpBoostPotion2, Potions.STRONG_LEAPING);
        shaped(EnumUpgrade.JUMPING.getItem(4), ModItems.PNEUMATIC_LEGGINGS.get(),
                "PCP/JUJ/ J ",
                'U', EnumUpgrade.JUMPING.getItem(3),
                'J', IngredientNBTWrapper.fromItemStack(jumpBoostPotion2),
                'P', Blocks.PISTON,
                'C', ModItems.PNEUMATIC_CYLINDER.get()
        ).build(consumer);

        // bricks etc.
        shaped(ModBlocks.REINFORCED_STONE.get(), 8, ModItems.COMPRESSED_IRON_INGOT.get(),
                "BBB/BIB/BBB",
                'B', Blocks.STONE,
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON
        ).build(consumer);
        shaped(ModBlocks.REINFORCED_STONE.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "B/B",
                'B', ModBlocks.REINFORCED_STONE_SLAB.get()
        ).build(consumer, RL("reinforced_stone_from_slab"));
        shaped(ModBlocks.REINFORCED_BRICKS.get(), 4, ModItems.COMPRESSED_IRON_INGOT.get(),
                "SS/SS",
                'S', ModBlocks.REINFORCED_STONE.get()
        ).build(consumer);
        shapeless(ModBlocks.REINFORCED_BRICKS.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                ModBlocks.REINFORCED_BRICK_TILE.get()
        ).build(consumer, RL("reinforced_bricks_from_tile"));
        shaped(ModBlocks.REINFORCED_BRICKS.get(), ModItems.COMPRESSED_IRON_INGOT.get(),
                "B/B",
                'B', ModBlocks.REINFORCED_BRICK_SLAB.get()
        ).build(consumer, RL("reinforced_brick_from_slab"));
        shaped(ModBlocks.REINFORCED_BRICK_SLAB.get(), 6, ModItems.COMPRESSED_IRON_INGOT.get(),
                "BBB",
                'B', ModBlocks.REINFORCED_BRICKS.get()
        ).build(consumer);
        shaped(ModBlocks.REINFORCED_STONE_SLAB.get(), 6, ModItems.COMPRESSED_IRON_INGOT.get(),
                "BBB",
                'B', ModBlocks.REINFORCED_STONE.get()
        ).build(consumer);
        shaped(ModBlocks.REINFORCED_BRICK_STAIRS.get(), 4, ModItems.COMPRESSED_IRON_INGOT.get(),
                "B  /BB /BBB",
                'B', ModBlocks.REINFORCED_BRICKS.get()
        ).build(consumer);
        shaped(ModBlocks.REINFORCED_BRICK_PILLAR.get(), 3, ModItems.COMPRESSED_IRON_INGOT.get(),
                "B/B/B",
                'B', ModBlocks.REINFORCED_BRICKS.get()
        ).build(consumer);
        shaped(ModBlocks.REINFORCED_BRICK_TILE.get(), 4, ModItems.COMPRESSED_IRON_INGOT.get(),
                "BB/BB",
                'B', ModBlocks.REINFORCED_BRICKS.get()
        ).build(consumer);
        shaped(ModBlocks.REINFORCED_BRICK_WALL.get(), 6, ModItems.COMPRESSED_IRON_INGOT.get(),
                "BBB/BBB",
                'B', ModBlocks.REINFORCED_BRICKS.get()
        ).build(consumer);

        // plastic bricks
        for (DyeColor dye : DyeColor.values()) {
            plasticBrick(dye, dye.getTag()).build(consumer);
        }

        // specials
        specialRecipe(ModRecipes.DRONE_COLOR_CRAFTING.get()).build(consumer, getId("color_drone"));
        specialRecipe(ModRecipes.DRONE_UPGRADE_CRAFTING.get()).build(consumer, getId("drone_upgrade"));
        specialRecipe(ModRecipes.GUN_AMMO_POTION_CRAFTING.get()).build(consumer, getId("gun_ammo_potion_crafting"));
        specialRecipe(ModRecipes.ONE_PROBE_HELMET_CRAFTING.get()).build(consumer, getId("one_probe_crafting"));
        specialRecipe(ModRecipes.PATCHOULI_BOOK_CRAFTING.get()).build(consumer, getId("patchouli_book_crafting"));
        specialRecipe(ModRecipes.PRESSURE_CHAMBER_ENCHANTING.get()).build(consumer, getId("pressure_chamber/pressure_chamber_enchanting"));
        specialRecipe(ModRecipes.PRESSURE_CHAMBER_DISENCHANTING.get()).build(consumer, getId("pressure_chamber/pressure_chamber_disenchanting"));

        // smelting
        CookingRecipeBuilder.blastingRecipe(Ingredient.fromItems(ModItems.FAILED_PCB.get()), ModItems.EMPTY_PCB.get(),
                0.25f, 100)
                .addCriterion("has_empty_pcb", this.hasItem(ModItems.FAILED_PCB.get()))
                .build(consumer, RL("empty_pcb_from_failed_pcb"));
        CookingRecipeBuilder.smeltingRecipe(Ingredient.fromTag(PneumaticCraftTags.Items.PLASTIC_BRICKS), ModItems.PLASTIC.get(),
                0f, 100)
                .addCriterion("has_plastic", this.hasItem(ModItems.PLASTIC.get()))
                .build(consumer, RL("plastic_sheet_from_brick"));

        // pressure chamber
        pressureChamber(ImmutableList.of(StackedIngredient.fromItems(4, Items.SNOW_BLOCK)),
                1.5f, new ItemStack(Items.ICE))
                .build(consumer, RL("pressure_chamber/ice"));
        pressureChamber(ImmutableList.of(StackedIngredient.fromItems(4, Items.ICE)),
                2.0f, new ItemStack(Items.PACKED_ICE))
                .build(consumer, RL("pressure_chamber/packed_ice"));
        pressureChamber(ImmutableList.of(StackedIngredient.fromItems(4, Items.PACKED_ICE)),
                2.5f, new ItemStack(Items.BLUE_ICE))
                .build(consumer, RL("pressure_chamber/blue_ice"));
        pressureChamber(ImmutableList.of(Ingredient.fromTag(Tags.Items.INGOTS_IRON)),
                2.0f, new ItemStack(ModItems.COMPRESSED_IRON_INGOT.get()))
                .build(consumer, RL("pressure_chamber/compressed_iron_ingot"));
        pressureChamber(ImmutableList.of(Ingredient.fromTag(Tags.Items.STORAGE_BLOCKS_IRON)),
                2.0f, new ItemStack(ModBlocks.COMPRESSED_IRON_BLOCK.get()))
                .build(consumer, RL("pressure_chamber/compressed_iron_block"));
        pressureChamber(ImmutableList.of(
                StackedIngredient.fromItems(2, Items.REDSTONE_TORCH),
                StackedIngredient.fromTag(Tags.Items.NUGGETS_GOLD, 3),
                Ingredient.fromItems(ModItems.PLASTIC.get())),
                1.5f, new ItemStack(ModItems.EMPTY_PCB.get(), 3))
                .build(consumer, RL("pressure_chamber/empty_pcb"));
        pressureChamber(ImmutableList.of(
                StackedIngredient.fromTag(Tags.Items.NUGGETS_GOLD, 2),
                Ingredient.fromTag(Tags.Items.SLIMEBALLS),
                Ingredient.fromItems(ModItems.PLASTIC.get())),
                1f, new ItemStack(ModItems.CAPACITOR.get()))
                .build(consumer, RL("pressure_chamber/capacitor"));
        pressureChamber(ImmutableList.of(
                StackedIngredient.fromTag(Tags.Items.NUGGETS_GOLD, 3),
                Ingredient.fromTag(Tags.Items.DUSTS_REDSTONE),
                Ingredient.fromItems(ModItems.PLASTIC.get())),
                1f, new ItemStack(ModItems.TRANSISTOR.get()))
                .build(consumer, RL("pressure_chamber/transistor"));
        pressureChamber(ImmutableList.of(
                Ingredient.fromItems(ModItems.PLASTIC_BUCKET.get()),
                StackedIngredient.fromItems(2, Items.ROTTEN_FLESH),
                StackedIngredient.fromItems(2, Items.SPIDER_EYE),
                StackedIngredient.fromTag(Tags.Items.GUNPOWDER, 2)),
                1f, new ItemStack(ModItems.ETCHING_ACID_BUCKET.get()))
                .build(consumer, RL("pressure_chamber/etching_acid"));
        pressureChamber(ImmutableList.of(
                Ingredient.fromItems(Items.MILK_BUCKET),
                StackedIngredient.fromTag(Tags.Items.DYES_GREEN, 4)),
                1f, new ItemStack(Items.SLIME_BALL, 4), new ItemStack(Items.BUCKET))
                .build(consumer, RL("pressure_chamber/milk_to_slime_balls"));
        pressureChamber(ImmutableList.of(
                Ingredient.fromTag(Tags.Items.INGOTS_GOLD),
                StackedIngredient.fromTag(Tags.Items.DUSTS_REDSTONE, 2)),
                1f, new ItemStack(ModItems.TURBINE_BLADE.get()))
                .build(consumer, RL("pressure_chamber/turbine_blade"));
        pressureChamber(ImmutableList.of(StackedIngredient.fromTag(Tags.Items.STORAGE_BLOCKS_COAL, 8)),
                4f, new ItemStack(Items.DIAMOND))
                .build(consumer, RL("pressure_chamber/coal_to_diamond"));

        // explosion crafting
        explosionCrafting(Ingredient.fromTag(Tags.Items.INGOTS_IRON), 20, new ItemStack(ModItems.COMPRESSED_IRON_INGOT.get()))
                .build(consumer, RL("explosion_crafting/compressed_iron_ingot"));
        explosionCrafting(Ingredient.fromTag(Tags.Items.STORAGE_BLOCKS_IRON), 20, new ItemStack(ModBlocks.COMPRESSED_IRON_BLOCK.get()))
                .build(consumer, RL("explosion_crafting/compressed_iron_block"));

        // heat frame cooling
        heatFrameCooling(Ingredient.fromItems(Items.WATER_BUCKET), 273,
                new ItemStack(Blocks.ICE))
                .build(consumer, RL("heat_frame_cooling/ice"));
        heatFrameCooling(Ingredient.fromItems(Items.LAVA_BUCKET), 273,
                new ItemStack(Blocks.OBSIDIAN))
                .build(consumer, RL("heat_frame_cooling/obsidian"));
        heatFrameCooling(FluidIngredient.of(1000, PneumaticCraftTags.Fluids.PLASTIC), 273,
                new ItemStack(ModItems.PLASTIC.get()), 0.01f, 0.75f)
                .build(consumer, RL("heat_frame_cooling/plastic"));

        // refinery
        refinery(FluidIngredient.of(10, PneumaticCraftTags.Fluids.OIL),
                TemperatureRange.min(373),
                new FluidStack(ModFluids.DIESEL.get(), 4),
                new FluidStack(ModFluids.LPG.get(), 2)
        ).build(consumer, RL("refinery/oil_2"));
        refinery(FluidIngredient.of(10, PneumaticCraftTags.Fluids.OIL),
                TemperatureRange.min(373),
                new FluidStack(ModFluids.DIESEL.get(), 2),
                new FluidStack(ModFluids.KEROSENE.get(), 3),
                new FluidStack(ModFluids.LPG.get(), 2)
        ).build(consumer, RL("refinery/oil_3"));
        refinery(FluidIngredient.of(10, PneumaticCraftTags.Fluids.OIL),
                TemperatureRange.min(373),
                new FluidStack(ModFluids.DIESEL.get(), 2),
                new FluidStack(ModFluids.KEROSENE.get(), 3),
                new FluidStack(ModFluids.GASOLINE.get(), 3),
                new FluidStack(ModFluids.LPG.get(), 2)
        ).build(consumer, RL("refinery/oil_4"));

        // thermopneumatic processing plant
        thermoPlant(FluidIngredient.of(100, PneumaticCraftTags.Fluids.DIESEL), Ingredient.EMPTY,
                new FluidStack(ModFluids.KEROSENE.get(), 80), ItemStack.EMPTY,
                TemperatureRange.min(573), 2.0f, false
        ).build(consumer, RL("thermo_plant/kerosene"));
        thermoPlant(FluidIngredient.of(100, PneumaticCraftTags.Fluids.KEROSENE), Ingredient.EMPTY,
                new FluidStack(ModFluids.GASOLINE.get(), 80), ItemStack.EMPTY,
                TemperatureRange.min(573), 2.0f, false
        ).build(consumer, RL("thermo_plant/gasoline"));
        thermoPlant(FluidIngredient.of(100, PneumaticCraftTags.Fluids.GASOLINE), Ingredient.EMPTY,
                new FluidStack(ModFluids.LPG.get(), 80), ItemStack.EMPTY,
                TemperatureRange.min(573), 2.0f, false
        ).build(consumer, RL("thermo_plant/lpg"));
        thermoPlant(FluidIngredient.of(1000, PneumaticCraftTags.Fluids.DIESEL), Ingredient.fromTag(Tags.Items.DUSTS_REDSTONE),
                new FluidStack(ModFluids.LUBRICANT.get(), 1000), ItemStack.EMPTY,
                TemperatureRange.min(373), 0f, false
        ).build(consumer, RL("thermo_plant/lubricant"));
        thermoPlant(FluidIngredient.of(100, PneumaticCraftTags.Fluids.LPG), Ingredient.fromTag(ItemTags.COALS),
                new FluidStack(ModFluids.PLASTIC.get(), 1000), ItemStack.EMPTY,
                TemperatureRange.min(373), 0f, false
        ).build(consumer, RL("thermo_plant/plastic"));
        thermoPlant(FluidIngredient.of(1000, Fluids.WATER), Ingredient.fromItems(Items.LAPIS_LAZULI),
                FluidStack.EMPTY, new ItemStack(ModItems.UPGRADE_MATRIX.get(), 4),
                TemperatureRange.min(273), 2f, false
        ).build(consumer, RL("thermo_plant/upgrade_matrix"));

        // assembly system
        assembly(Ingredient.fromItems(ModItems.EMPTY_PCB.get()), new ItemStack(ModItems.UNASSEMBLED_PCB.get()),
                AssemblyProgramType.LASER)
                .build(consumer, RL("assembly/unassembled_pcb"));
        assembly(Ingredient.fromTag(Tags.Items.DUSTS_REDSTONE), new ItemStack(Items.RED_DYE, 2),
                AssemblyProgramType.DRILL)
                .build(consumer, RL("assembly/red_dye"));
        assembly(Ingredient.fromItems(ModBlocks.COMPRESSED_IRON_BLOCK.get()), new ItemStack(ModBlocks.PRESSURE_CHAMBER_VALVE.get(), 20),
                AssemblyProgramType.DRILL)
                .build(consumer, RL("assembly/pressure_chamber_valve"));
        assembly(StackedIngredient.fromItems(20, ModBlocks.PRESSURE_CHAMBER_VALVE.get()), new ItemStack(ModBlocks.ADVANCED_PRESSURE_TUBE.get(), 8),
                AssemblyProgramType.LASER)
                .build(consumer, RL("assembly/advanced_pressure_tube"));
        assembly(Ingredient.fromTag(Tags.Items.STORAGE_BLOCKS_QUARTZ), new ItemStack(ModBlocks.APHORISM_TILE.get(), 4),
                AssemblyProgramType.LASER)
                .build(consumer, RL("assembly/aphorism_tile"));

        // amadron (core static offers only)
        amadronStatic(
                AmadronTradeResource.of(new ItemStack(Items.EMERALD, 8)),
                AmadronTradeResource.of(new ItemStack(ModItems.ASSEMBLY_PROGRAM_DRILL.get()))
        ).build(consumer, RL("amadron/assembly_program_drill"));
        amadronStatic(
                AmadronTradeResource.of(new ItemStack(Items.EMERALD, 8)),
                AmadronTradeResource.of(new ItemStack(ModItems.ASSEMBLY_PROGRAM_LASER.get()))
        ).build(consumer, RL("amadron/assembly_program_laser"));
        amadronStatic(
                AmadronTradeResource.of(new ItemStack(Items.EMERALD, 14)),
                AmadronTradeResource.of(new ItemStack(ModItems.ASSEMBLY_PROGRAM_DRILL_LASER.get()))
        ).build(consumer, RL("amadron/assembly_program_drill_laser"));
        amadronStatic(
                AmadronTradeResource.of(new ItemStack(Items.EMERALD, 8)),
                AmadronTradeResource.of(new ItemStack(ModItems.PCB_BLUEPRINT.get()))
        ).build(consumer, RL("amadron/pcb_blueprint"));
        amadronStatic(
                AmadronTradeResource.of(new FluidStack(ModFluids.OIL.get(), 5000)),
                AmadronTradeResource.of(new ItemStack(Items.EMERALD))
        ).build(consumer, RL("amadron/oil_to_emerald"));
        amadronStatic(
                AmadronTradeResource.of(new FluidStack(ModFluids.DIESEL.get(), 4000)),
                AmadronTradeResource.of(new ItemStack(Items.EMERALD))
        ).build(consumer, RL("amadron/diesel_to_emerald"));
        amadronStatic(
                AmadronTradeResource.of(new FluidStack(ModFluids.KEROSENE.get(), 3000)),
                AmadronTradeResource.of(new ItemStack(Items.EMERALD))
        ).build(consumer, RL("amadron/kerosene_to_emerald"));
        amadronStatic(
                AmadronTradeResource.of(new FluidStack(ModFluids.GASOLINE.get(), 2000)),
                AmadronTradeResource.of(new ItemStack(Items.EMERALD))
        ).build(consumer, RL("amadron/gasoline_to_emerald"));
        amadronStatic(
                AmadronTradeResource.of(new FluidStack(ModFluids.LPG.get(), 1000)),
                AmadronTradeResource.of(new ItemStack(Items.EMERALD))
        ).build(consumer, RL("amadron/lpg_to_emerald"));
        amadronStatic(
                AmadronTradeResource.of(new FluidStack(ModFluids.LUBRICANT.get(), 2500)),
                AmadronTradeResource.of(new ItemStack(Items.EMERALD))
        ).build(consumer, RL("amadron/lubricant_to_emerald"));
        amadronStatic(
                AmadronTradeResource.of(new ItemStack(Items.EMERALD, 5)),
                AmadronTradeResource.of(new FluidStack(ModFluids.LUBRICANT.get(), 1000))
        ).build(consumer, RL("amadron/emerald_to_lubricant"));
        amadronStatic(
                AmadronTradeResource.of(new ItemStack(Items.EMERALD, 1)),
                AmadronTradeResource.of(new FluidStack(ModFluids.OIL.get(), 1000))
        ).build(consumer, RL("amadron/emerald_to_oil"));
    }

    private <T extends IItemProvider & IForgeRegistryEntry<?>> ShapelessRecipeBuilder shapeless(T result, T required, Object... ingredients) {
        return shapeless(result, 1, required, ingredients);
    }

    private <T extends IItemProvider & IForgeRegistryEntry<?>> ShapelessRecipeBuilder shapeless(T result, int count, T required, Object... ingredients) {
        ShapelessRecipeBuilder b = ShapelessRecipeBuilder.shapelessRecipe(result, count);
        for (Object v : ingredients) {
            if (v instanceof Tag<?>) {
                //noinspection unchecked
                b.addIngredient((Tag<Item>) v);
            } else if (v instanceof IItemProvider) {
                b.addIngredient((IItemProvider) v);
            } else if (v instanceof Ingredient) {
                b.addIngredient((Ingredient) v);
            } else {
                throw new IllegalArgumentException("bad type for recipe ingredient " + v);
            }
        }
        b.addCriterion("has_" + safeName(required), this.hasItem(required));
        return b;
    }

    private void buildLogisticsFrameSelfCraft(Item frame, Consumer<IFinishedRecipe> consumer) {
        shapeless(frame, frame, frame).build(consumer, frame.getRegistryName().toString() + "_self");
    }

    private ShapedRecipeBuilder logisticsFrame(Item result, Tag<Item> dye) {
        return shaped(result, 8, ModItems.LOGISTICS_CORE.get(),
                "PPP/PDP/PCP",
                'P', Items.STICK,
                'C', ModItems.LOGISTICS_CORE.get(),
                'D', dye);
    }

    private ShapedRecipeBuilder networkComponent(Item result, int count, Tag<Item> edge, Tag<Item> dyeCorner) {
        return shaped(result, count, ModItems.CAPACITOR.get(), "CEC/EXE/CEC", 'C', dyeCorner, 'E', edge, 'X', Tags.Items.CHESTS_WOODEN);
    }

    private ShapedRecipeBuilder networkComponent(Item result, int count, Item edge, Tag<Item> dyeCorner) {
        return shaped(result, count, ModItems.CAPACITOR.get(), "CEC/EXE/CEC", 'C', dyeCorner, 'E', edge, 'X', Tags.Items.CHESTS_WOODEN);
    }

    private <T extends IItemProvider & IForgeRegistryEntry<?>> ShapedPressurizableRecipeBuilder pneumaticTool(T result, Object dye) {
        return shapedPressure(result, ModItems.COMPRESSED_IRON_INGOT.get(),
                "IDI/C  /ILI",
                'I', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                'D', dye,
                'C', ModItems.AIR_CANISTER.get(),
                'L', Blocks.LEVER
        );
    }

    private <T extends IItemProvider & IForgeRegistryEntry<?>> ShapedPressurizableRecipeBuilder shapedPressure(T result, T required, String pattern, Object... keys) {
        ShapedPressurizableRecipeBuilder b = ShapedPressurizableRecipeBuilder.shapedRecipe(result);
        Arrays.stream(pattern.split("/")).forEach(b::patternLine);
        for (int i = 0; i < keys.length; i += 2) {
            Object v = keys[i + 1];
            if (v instanceof Tag<?>) {
                //noinspection unchecked
                b.key((Character) keys[i], (Tag<Item>) v);
            } else if (v instanceof IItemProvider) {
                b.key((Character) keys[i], (IItemProvider) v);
            } else if (v instanceof Ingredient) {
                b.key((Character) keys[i], (Ingredient) v);
            } else {
                throw new IllegalArgumentException("bad type for recipe ingredient " + v);
            }
        }
        b.addCriterion("has_" + safeName(required), this.hasItem(required));
        return b;
    }

    private <T extends IItemProvider & IForgeRegistryEntry<?>> ShapedRecipeBuilder shaped(T result, T required, String pattern, Object... keys) {
        return shaped(result, 1, required, pattern, keys);
    }

    private <T extends IItemProvider & IForgeRegistryEntry<?>> ShapedRecipeBuilder shaped(T result, int count, T required, String pattern, Object... keys) {
        ShapedRecipeBuilder b = ShapedRecipeBuilder.shapedRecipe(result, count);
        Arrays.stream(pattern.split("/")).forEach(b::patternLine);
        for (int i = 0; i < keys.length; i += 2) {
            Object v = keys[i + 1];
            if (v instanceof Tag<?>) {
                //noinspection unchecked
                b.key((Character) keys[i], (Tag<Item>) v);
            } else if (v instanceof IItemProvider) {
                b.key((Character) keys[i], (IItemProvider) v);
            } else if (v instanceof Ingredient) {
                b.key((Character) keys[i], (Ingredient) v);
            } else {
                throw new IllegalArgumentException("bad type for recipe ingredient " + v);
            }
        }
        b.addCriterion("has_" + safeName(required), this.hasItem(required));
        return b;
    }

    private ShapedRecipeBuilder plasticBrick(DyeColor color, Tag<Item> dyeIngredient) {
        Item brick = ModBlocks.plasticBrick(color).get().asItem();
        return shaped(brick, 8, ModItems.PLASTIC.get(),
                "PPP/PDP/PPP",
                'P', PneumaticCraftTags.Items.PLASTIC_SHEETS,
                'D', dyeIngredient);
    }

    private ShapedRecipeBuilder miniGunAmmo(Item result, Object item1, Object item2) {
        return shaped(result, ModItems.GUN_AMMO.get(),
                " A /C1C/C2C",
                'A', ModItems.GUN_AMMO.get(),
                'C', PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
                '1', item1,
                '2', item2);
    }

    private ShapedRecipeBuilder standardUpgrade(EnumUpgrade what, Object center, Object edge) {
        return shaped(what.getItem(), Items.LAPIS_LAZULI,
                "LXL/XCX/LXL",
                'L', PneumaticCraftTags.Items.UPGRADE_COMPONENTS,
                'X', edge,
                'C', center);
    }

//    private <T extends IItemProvider & IForgeRegistryEntry<?>> ShapedRecipeBuilder standardUpgrade(EnumUpgrade what, T center, T edge) {
//        return ShapedRecipeBuilder.shapedRecipe(what.getItem())
//                .patternLine("LXL")
//                .patternLine("XCX")
//                .patternLine("LXL")
//                .key('L', PneumaticCraftTags.Items.UPGRADE_COMPONENTS)
//                .key('X', edge)
//                .key('C', center)
//                .addCriterion("has_" + safeName(center), this.hasItem(center));
//    }

    private CustomRecipeBuilder specialRecipe(SpecialRecipeSerializer<?> recipe) {
        return CustomRecipeBuilder.customRecipe(recipe);
    }

    private PressureChamberRecipeBuilder pressureChamber(List<Ingredient> in, float pressure, ItemStack... out) {
        return new PressureChamberRecipeBuilder(in, pressure, out)
                .addCriterion(Criteria.has(ModBlocks.PRESSURE_CHAMBER_VALVE.get()));
    }

    private ExplosionCraftingRecipeBuilder explosionCrafting(Ingredient ingredient, int lossRate, ItemStack result) {
        return new ExplosionCraftingRecipeBuilder(ingredient, lossRate, result)
                .addCriterion(Criteria.has(ingredient));
    }

    private HeatFrameCoolingRecipeBuilder heatFrameCooling(Ingredient ingredient, int maxTemp, ItemStack result) {
        return heatFrameCooling(ingredient, maxTemp, result, 0f, 0f);
    }

    private HeatFrameCoolingRecipeBuilder heatFrameCooling(Ingredient ingredient, int maxTemp, ItemStack result, float bonusMult, float bonusLimit) {
        return new HeatFrameCoolingRecipeBuilder(ingredient, maxTemp, result, bonusMult, bonusLimit)
                .addCriterion(Criteria.has(ModItems.HEAT_FRAME.get()));
    }

    private RefineryRecipeBuilder refinery(FluidIngredient ingredient, TemperatureRange operatingTemp, FluidStack... outputs) {
        return new RefineryRecipeBuilder(ingredient, operatingTemp, outputs)
                .addCriterion(Criteria.has(ModBlocks.REFINERY.get()));
    }

    private ThermoPlantRecipeBuilder thermoPlant(FluidIngredient inputFluid, @Nullable Ingredient inputItem,
                                                 FluidStack outputFluid, ItemStack outputItem, TemperatureRange operatingTemperature, float requiredPressure,
                                                 boolean exothermic) {
        return new ThermoPlantRecipeBuilder(inputFluid, inputItem, outputFluid, outputItem, operatingTemperature, requiredPressure, exothermic)
                .addCriterion(Criteria.has(ModBlocks.THERMOPNEUMATIC_PROCESSING_PLANT.get()));
    }

    private AssemblyRecipeBuilder assembly(Ingredient input, ItemStack output, AssemblyProgramType programType) {
        return new AssemblyRecipeBuilder(input, output, programType)
                .addCriterion(Criteria.has(input));
    }

    private AmadronRecipeBuilder amadronStatic(AmadronTradeResource in, AmadronTradeResource out) {
        return new AmadronRecipeBuilder(in, out, true, 0)
                .addCriterion(Criteria.has(ModItems.AMADRON_TABLET.get()));
    }

    private AmadronRecipeBuilder amadronPeriodic(AmadronTradeResource in, AmadronTradeResource out, int tradeLevel) {
        return new AmadronRecipeBuilder(in, out, false, tradeLevel)
                .addCriterion(Criteria.has(ModItems.AMADRON_TABLET.get()));
    }

    private String getId(String s) {
        return RL(s).toString();
    }

    private ResourceLocation safeId(ResourceLocation id) {
        return new ResourceLocation(id.getNamespace(), safeName(id));
    }

    private String safeName(IForgeRegistryEntry<?>  i) {
        return safeName(i.getRegistryName());
    }

    private String safeName(ResourceLocation registryName) {
        return registryName.getPath().replace('/', '_');
    }

    // this wrapper is due to lack of any public constructor method for IngredientNBT
    private static class IngredientNBTWrapper extends NBTIngredient {
        IngredientNBTWrapper(ItemStack stack) {
            super(stack);
        }

        static IngredientNBTWrapper fromItemStack(ItemStack stack) {
            return new IngredientNBTWrapper(stack);
        }
    }

    @Override
    public String getName() {
        return "PneumaticCraft Recipes";
    }
}
