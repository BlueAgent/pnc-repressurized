package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.recipe.IPneumaticRecipeRegistry;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.block.tubes.ModuleRegistrator;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.fluid.Fluids;
import me.desht.pneumaticcraft.common.item.ItemNetworkComponents;
import me.desht.pneumaticcraft.common.item.ItemPlastic;
import me.desht.pneumaticcraft.common.item.ItemProgrammingPuzzle;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockHeatFrame;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

// TODO convert to JSON recipes
public class CraftingRegistrator {
    @GameRegistry.ObjectHolder("theoneprobe:probe")
    static final Item ONE_PROBE = null;

    public static void init() {
        ItemStack lapis = new ItemStack(Items.DYE, 1, 4);
        ItemStack cobbleSlab = new ItemStack(Blocks.STONE_SLAB, 1, 3);
        
        // Elevators
        addRecipe(new ItemStack(Blockss.ELEVATOR_FRAME, 4, 0), "i i", "i i", "i i", 'i', Names.INGOT_IRON_COMPRESSED);
        addRecipe(new ItemStack(Itemss.PNEUMATIC_CYLINDER), "pip", "pip", "pbp", 'p', new ItemStack(Itemss.PLASTIC, 1, ItemPlastic.BLUE), 'i', Names.INGOT_IRON_COMPRESSED, 'b', Itemss.CANNON_BARREL);
        addRecipe(new ItemStack(Blockss.ELEVATOR_BASE, 4, 0), "cp", "pc", 'p', new ItemStack(Itemss.PLASTIC, 1, ItemPlastic.GREY), 'c', Itemss.PNEUMATIC_CYLINDER);
        addRecipe(new ItemStack(Blockss.ELEVATOR_BASE, 4, 0), "pc", "cp", 'p', new ItemStack(Itemss.PLASTIC, 1, ItemPlastic.GREY), 'c', Itemss.PNEUMATIC_CYLINDER);
        addRecipe(new ItemStack(Blockss.ELEVATOR_CALLER, 1, 0), "cpc", "prp", "cpc", 'p', new ItemStack(Itemss.PLASTIC, 1, ItemPlastic.BROWN), 'c', new ItemStack(Itemss.PLASTIC, 1, ItemPlastic.WHITE), 'r', Items.REDSTONE);
        addRecipe(new ItemStack(Blockss.ELEVATOR_CALLER, 1, 0), "cpc", "prp", "cpc", 'c', new ItemStack(Itemss.PLASTIC, 1, ItemPlastic.BROWN), 'p', new ItemStack(Itemss.PLASTIC, 1, ItemPlastic.WHITE), 'r', Items.REDSTONE);

        //Security Station
        addRecipe(new ItemStack(Blockss.SECURITY_STATION), "gbg", "tpt", "ggg", 'g', new ItemStack(Itemss.PLASTIC, 1, ItemPlastic.GREY), 'b', new ItemStack(Itemss.PLASTIC, 1, ItemPlastic.BLACK), 't', Itemss.TURBINE_ROTOR, 'p', Itemss.PRINTED_CIRCUIT_BOARD);
        addRecipe(new ItemStack(Itemss.NETWORK_COMPONENT, 16, ItemNetworkComponents.NETWORK_NODE), "ttt", "tct", "ttt", 't', Itemss.TRANSISTOR, 'c', Blocks.CHEST);
        addRecipe(new ItemStack(Itemss.NETWORK_COMPONENT, 1, ItemNetworkComponents.NETWORK_IO_PORT), "ttt", "tct", "ttt", 't', Itemss.CAPACITOR, 'c', Blocks.CHEST);
        addRecipe(new ItemStack(Itemss.NETWORK_COMPONENT, 1, ItemNetworkComponents.NETWORK_REGISTRY), "ttt", "tct", "ttt", 't', new ItemStack(Itemss.PLASTIC, 1, ItemPlastic.LIME), 'c', Blocks.CHEST);
        addRecipe(new ItemStack(Itemss.NETWORK_COMPONENT, 1, ItemNetworkComponents.DIAGNOSTIC_SUBROUTINE), "ttt", "tct", "ttt", 't', new ItemStack(Itemss.PLASTIC, 1, ItemPlastic.RED), 'c', Blocks.CHEST);
        addRecipe(new ItemStack(Itemss.NETWORK_COMPONENT, 1, ItemNetworkComponents.NETWORK_API), "ttt", "tct", "ttt", 't', new ItemStack(Itemss.PLASTIC, 1, ItemPlastic.BLUE), 'c', Blocks.CHEST);
        addRecipe(new ItemStack(Itemss.NETWORK_COMPONENT, 1, ItemNetworkComponents.NETWORK_DATA_STORAGE), "ttt", "tct", "ttt", 't', new ItemStack(Itemss.PLASTIC, 1, ItemPlastic.GREY), 'c', Blocks.CHEST);

        // Machine Upgrades
        addRecipe(getUpgrade(EnumUpgrade.VOLUME), "lil", "ici", "lil", 'l', lapis, 'i', Names.INGOT_IRON_COMPRESSED, 'c', new ItemStack(Itemss.AIR_CANISTER, 1, OreDictionary.WILDCARD_VALUE));
        addRecipe(getUpgrade(EnumUpgrade.DISPENSER), "lil", "idi", "lil", 'l', lapis, 'i', Items.QUARTZ, 'd', Blocks.DISPENSER);
        addRecipe(getUpgrade(EnumUpgrade.ITEM_LIFE), "lal", "aca", "lal", 'l', lapis, 'a', Items.APPLE, 'c', Items.CLOCK);
        addRecipe(getUpgrade(EnumUpgrade.ENTITY_TRACKER), "lbl", "bsb", "lbl", 'l', lapis, 'b', Items.BONE, 's', Items.FERMENTED_SPIDER_EYE);
        addRecipe(getUpgrade(EnumUpgrade.BLOCK_TRACKER), "lwl", "wsw", "lwl", 'l', lapis, 'w', Blockss.PRESSURE_CHAMBER_WALL, 's', Items.FERMENTED_SPIDER_EYE);
        addRecipe(getUpgrade(EnumUpgrade.SPEED), "lsl", "scs", "lsl", 'l', lapis, 's', Items.SUGAR, 'c', Fluids.getBucketStack(Fluids.LUBRICANT));
        addRecipe(getUpgrade(EnumUpgrade.SEARCH), "lel", "ege", "lel", 'l', lapis, 'e', Items.ENDER_EYE, 'g', Items.GOLDEN_CARROT);
        addRecipe(getUpgrade(EnumUpgrade.COORDINATE_TRACKER), "lrl", "rgr", "lrl", 'l', lapis, 'r', Items.REDSTONE, 'g', Itemss.GPS_TOOL);
        addRecipe(getUpgrade(EnumUpgrade.RANGE), "lal", "aba", "lal", 'l', lapis, 'a', Items.ARROW, 'b', Items.BOW);
        addRecipe(getUpgrade(EnumUpgrade.SECURITY), "lol", "obo", "lol", 'l', lapis, 'o', Blocks.OBSIDIAN, 'b', ModuleRegistrator.getModuleItem(Names.MODULE_SAFETY_VALVE));

        // Misc crafting components
        addRecipe(new ItemStack(Itemss.AIR_CANISTER, 1, Itemss.AIR_CANISTER.getMaxDamage()), " t ", "iri", "iri", 'i', Names.INGOT_IRON_COMPRESSED, 'r', Items.REDSTONE, 't', new ItemStack(Blockss.PRESSURE_TUBE, 1, 0));
        addRecipe(new ItemStack(Itemss.TURBINE_ROTOR), " b ", " i ", "b b", 'i', Names.INGOT_IRON_COMPRESSED, 'b', Itemss.TURBINE_BLADE);
        addRecipe(new ItemStack(Blockss.VACUUM_PUMP), "grg", "trt", "sss", 'g', Itemss.PRESSURE_GAUGE, 'r', Itemss.TURBINE_ROTOR, 's', cobbleSlab, 't', new ItemStack(Blockss.PRESSURE_TUBE, 1, 0));

        // Pneumatic Items
        addRecipe(new ItemStack(Itemss.VORTEX_CANNON, 1, Itemss.VORTEX_CANNON.getMaxDamage()), "idi", "c  ", "ili", 'd', "dyeYellow", 'i', Itemss.INGOT_IRON_COMPRESSED, 'l', Blocks.LEVER, 'c', new ItemStack(Itemss.AIR_CANISTER, 1, Itemss.AIR_CANISTER.getMaxDamage()));
        addRecipe(new ItemStack(Itemss.PNEUMATIC_WRENCH, 1, Itemss.PNEUMATIC_WRENCH.getMaxDamage()), "idi", "c  ", "ili", 'd', "dyeOrange", 'i', Itemss.INGOT_IRON_COMPRESSED, 'l', Blocks.LEVER, 'c', new ItemStack(Itemss.AIR_CANISTER, 1, Itemss.AIR_CANISTER.getMaxDamage()));
        addRecipe(new ItemStack(Itemss.LOGISTICS_CONFIGURATOR, 1, Itemss.LOGISTICS_CONFIGURATOR.getMaxDamage()), "idi", "c  ", "ili", 'd', "dyeRed", 'i', Itemss.INGOT_IRON_COMPRESSED, 'l', Blocks.LEVER, 'c', new ItemStack(Itemss.AIR_CANISTER, 1, Itemss.AIR_CANISTER.getMaxDamage()));
        addRecipe(new ItemStack(Itemss.CAMO_APPLICATOR, 1, Itemss.LOGISTICS_CONFIGURATOR.getMaxDamage()), "idi", "c  ", "ili", 'd', "dyeBlue", 'i', Itemss.INGOT_IRON_COMPRESSED, 'l', Blocks.LEVER, 'c', new ItemStack(Itemss.AIR_CANISTER, 1, Itemss.AIR_CANISTER.getMaxDamage()));
        addRecipe(new ItemStack(Itemss.AMADRON_TABLET, 1, Itemss.AMADRON_TABLET.getMaxDamage()), "ppp", "pgp", "pcp", 'p', new ItemStack(Itemss.PLASTIC, 1, ItemPlastic.GREY), 'g', Itemss.GPS_TOOL, 'c', new ItemStack(Itemss.AIR_CANISTER, 1, Itemss.AIR_CANISTER.getMaxDamage()));
        addRecipe(new ItemStack(Itemss.PNEUMATIC_HELMET, 1), "cec", "c c", 'e', Itemss.PRINTED_CIRCUIT_BOARD, 'c', new ItemStack(Itemss.AIR_CANISTER, 1, Itemss.AIR_CANISTER.getMaxDamage()));
        addShapelessRecipe(new ItemStack(Itemss.MANOMETER, 1, Itemss.MANOMETER.getMaxDamage()), new ItemStack(Itemss.AIR_CANISTER, 1, Itemss.AIR_CANISTER.getMaxDamage()), Itemss.PRESSURE_GAUGE);

        ForgeRegistries.RECIPES.register(new RecipeGun("dyeYellow", Itemss.VORTEX_CANNON));
        ForgeRegistries.RECIPES.register(new RecipeGun("dyeOrange", Itemss.PNEUMATIC_WRENCH));
        ForgeRegistries.RECIPES.register(new RecipeGun("dyeRed", Itemss.LOGISTICS_CONFIGURATOR));
        ForgeRegistries.RECIPES.register(new RecipePneumaticHelmet());
        ForgeRegistries.RECIPES.register(new RecipeManometer());
        ForgeRegistries.RECIPES.register(new RecipeColorDrone());
        ForgeRegistries.RECIPES.register(new RecipeLogisticToDrone());
        ForgeRegistries.RECIPES.register(new RecipeGunAmmo());
        ForgeRegistries.RECIPES.register(new RecipeAmadronTablet());
        if (ONE_PROBE != null) ForgeRegistries.RECIPES.register(new RecipeOneProbe());

        //Heat related
        addRecipe(new ItemStack(Blockss.HEAT_SINK), "bbb", "igi", 'i', Names.INGOT_IRON_COMPRESSED, 'b', Blocks.IRON_BARS, 'g', "ingotGold");
        addRecipe(new ItemStack(Blockss.VORTEX_TUBE), "iti", "gtg", "iii", 'i', Names.INGOT_IRON_COMPRESSED, 'g', "ingotGold", 't', Blockss.PRESSURE_TUBE);
        addRecipe(new ItemStack(SemiBlockManager.getItemForSemiBlock(SemiBlockHeatFrame.class)), "iii", "ifi", "iii", 'i', Names.INGOT_IRON_COMPRESSED, 'f', Blocks.FURNACE);

        // misc
        addRecipe(new ItemStack(Blockss.COMPRESSED_IRON), "iii", "iii", "iii", 'i', Names.INGOT_IRON_COMPRESSED);
        addShapelessRecipe(new ItemStack(Itemss.INGOT_IRON_COMPRESSED, 9, 0), Names.BLOCK_IRON_COMPRESSED);

        addShapelessRecipe(new ItemStack(Itemss.PRINTED_CIRCUIT_BOARD), Itemss.UNASSEMBLED_PCB, Itemss.TRANSISTOR, Itemss.TRANSISTOR, Itemss.TRANSISTOR, Itemss.CAPACITOR, Itemss.CAPACITOR, Itemss.CAPACITOR);
        addRecipe(new ItemStack(Itemss.ADVANCED_PCB), "rpr", "pcp", "rpr", 'c', Itemss.PRINTED_CIRCUIT_BOARD, 'r', Items.REDSTONE, 'p', new ItemStack(Itemss.PLASTIC, 1, ItemPlastic.YELLOW));
        addRecipe(new ItemStack(Itemss.ADVANCED_PCB), "prp", "rcr", "prp", 'c', Itemss.PRINTED_CIRCUIT_BOARD, 'r', Items.REDSTONE, 'p', new ItemStack(Itemss.PLASTIC, 1, ItemPlastic.YELLOW));
        addRecipe(new ItemStack(Blockss.UV_LIGHT_BOX), "lll", "ibt", "iii", 'l', Blocks.REDSTONE_LAMP, 'b', Itemss.PCB_BLUEPRINT, 'i', Names.INGOT_IRON_COMPRESSED, 't', new ItemStack(Blockss.PRESSURE_TUBE, 1, 0));
        addShapelessRecipe(new ItemStack(Itemss.GUN_AMMO), new ItemStack(Items.GUNPOWDER), Names.INGOT_IRON_COMPRESSED, "ingotGold");
        addRecipe(new ItemStack(Blockss.SENTRY_TURRET), " m ", "pip", "i i", 'm', Itemss.MINIGUN, 'p', new ItemStack(Itemss.PLASTIC, 1, ItemPlastic.BLACK), 'i', Names.INGOT_IRON_COMPRESSED);
        addRecipe(new ItemStack(Itemss.MINIGUN), "cib", "g  ", 'c', Names.BLOCK_IRON_COMPRESSED, 'i', Names.INGOT_IRON_COMPRESSED, 'b', Itemss.CANNON_BARREL, 'g', "ingotGold");

        GameRegistry.addSmelting(Itemss.FAILED_PCB, new ItemStack(Itemss.EMPTY_PCB, 1, Itemss.EMPTY_PCB.getMaxDamage()), 0);

        // Assembly Machines
        addRecipe(new ItemStack(Blockss.ASSEMBLY_DRILL), true, "dcc", "  c", "ipi", 'd', Items.DIAMOND, 'c', Itemss.PNEUMATIC_CYLINDER, 'i', Names.INGOT_IRON_COMPRESSED, 'p', Itemss.PRINTED_CIRCUIT_BOARD);
        addRecipe(new ItemStack(Blockss.ASSEMBLY_LASER), true, "dcc", "  c", "ipi", 'd', new ItemStack(Items.DYE, 1, 1), 'c', Itemss.PNEUMATIC_CYLINDER, 'i', Names.INGOT_IRON_COMPRESSED, 'p', Itemss.PRINTED_CIRCUIT_BOARD);
        addRecipe(new ItemStack(Blockss.ASSEMBLY_IO_UNIT), true, "hcc", "  c", "ipi", 'h', Blocks.HOPPER, 'c', Itemss.PNEUMATIC_CYLINDER, 'i', Names.INGOT_IRON_COMPRESSED, 'p', Itemss.PRINTED_CIRCUIT_BOARD);
        addRecipe(new ItemStack(Blockss.ASSEMBLY_PLATFORM), true, "a a", "ppp", "ici", 'a', Itemss.PNEUMATIC_CYLINDER, 'p', new ItemStack(Itemss.PLASTIC, 1, ItemPlastic.ORANGE), 'i', Names.INGOT_IRON_COMPRESSED, 'c', Itemss.PRINTED_CIRCUIT_BOARD);
        addRecipe(new ItemStack(Blockss.ASSEMBLY_CONTROLLER), true, " c ", "tcc", "iii", 'i', Names.INGOT_IRON_COMPRESSED, 'c', Itemss.PRINTED_CIRCUIT_BOARD, 't', new ItemStack(Blockss.PRESSURE_TUBE, 1, 0));

        addRecipe(new ItemStack(Blockss.PNEUMATIC_DOOR), "cc", "cc", "cc", 'c', Names.INGOT_IRON_COMPRESSED);
        addRecipe(new ItemStack(Blockss.PNEUMATIC_DOOR_BASE), true, " #c", "cct", "ccc", '#', Itemss.PNEUMATIC_CYLINDER, 'c', Names.INGOT_IRON_COMPRESSED, 't', new ItemStack(Blockss.PRESSURE_TUBE, 1, 0));

        addRecipe(new ItemStack(Blockss.UNIVERSAL_SENSOR), "plp", "lpl", "pcp", 'p', new ItemStack(Itemss.PLASTIC, 1, ItemPlastic.PURPLE), 'l', new ItemStack(Itemss.PLASTIC, 1, ItemPlastic.LIGHT_BLUE), 'c', "dustRedstone");
        addRecipe(new ItemStack(Blockss.AERIAL_INTERFACE), "whw", "ese", "wtw", 'w', Blockss.PRESSURE_CHAMBER_WALL, 'h', Blocks.HOPPER, 'e', Items.ENDER_PEARL, 's', new ItemStack(Items.SKULL, 1, 1), 't', new ItemStack(Blockss.ADVANCED_PRESSURE_TUBE, 1, 0));
        addRecipe(new ItemStack(Blockss.OMNIDIRECTIONAL_HOPPER), "i i", "ici", " i ", 'i', Names.INGOT_IRON_COMPRESSED, 'c', Blocks.CHEST);
        addRecipe(new ItemStack(Blockss.LIQUID_HOPPER), "i i", "ici", " i ", 'i', "blockGlass", 'c', Blocks.HOPPER);

        addRecipe(new ItemStack(Blockss.PLASTIC_MIXER), "igi", "g g", "iii", 'i', Names.INGOT_IRON_COMPRESSED, 'g', "blockGlass");
        addRecipe(new ItemStack(Blockss.KEROSENE_LAMP), " i ", "g g", "ibi", 'i', Names.INGOT_IRON_COMPRESSED, 'g', "blockGlass", 'b', Items.BUCKET);

        addRecipe(new ItemStack(Itemss.DRONE), " b ", "bcb", " b ", 'b', Itemss.TURBINE_ROTOR, 'c', Itemss.PRINTED_CIRCUIT_BOARD);
        addRecipe(new ItemStack(Blockss.PROGRAMMABLE_CONTROLLER), "iri", "cdp", "ini", 'i', Names.INGOT_IRON_COMPRESSED, 'c', Itemss.PRINTED_CIRCUIT_BOARD, 'r', Itemss.REMOTE, 'd', Itemss.DRONE, 'p', Blockss.ADVANCED_PRESSURE_TUBE, 'n', new ItemStack(Itemss.NETWORK_COMPONENT, 1, ItemNetworkComponents.NETWORK_REGISTRY));

        addRecipe(new ItemStack(Blockss.PROGRAMMER), "gbg", "tpt", "ggg", 'g', new ItemStack(Itemss.PLASTIC, 1, ItemPlastic.RED), 'b', new ItemStack(Itemss.PLASTIC, 1, ItemPlastic.BLACK), 't', Itemss.TURBINE_ROTOR, 'p', Itemss.PRINTED_CIRCUIT_BOARD);

        addRecipe(new ItemStack(Itemss.LOGISTICS_DRONE), " b ", "bcb", " b ", 'b', Itemss.TURBINE_ROTOR, 'c', "dustRedstone");
        addRecipe(new ItemStack(Itemss.LOGISTICS_FRAME_PASSIVE_PROVIDER), "ppp", "p p", "ppp", 'p', new ItemStack(Itemss.PLASTIC, 1, ItemPlastic.RED));
        addRecipe(new ItemStack(Itemss.LOGISTICS_FRAME_ACTIVE_PROVIDER), "ppp", "p p", "ppp", 'p', new ItemStack(Itemss.PLASTIC, 1, ItemPlastic.PURPLE));
        addRecipe(new ItemStack(Itemss.LOGISTICS_FRAME_REQUESTER), "ppp", "p p", "ppp", 'p', new ItemStack(Itemss.PLASTIC, 1, ItemPlastic.BLUE));
        addRecipe(new ItemStack(Itemss.LOGISTICS_FRAME_STORAGE), "ppp", "p p", "ppp", 'p', new ItemStack(Itemss.PLASTIC, 1, ItemPlastic.YELLOW));
        addRecipe(new ItemStack(Itemss.LOGISTICS_FRAME_DEFAULT_STORAGE), "ppp", "p p", "ppp", 'p', new ItemStack(Itemss.PLASTIC, 1, ItemPlastic.LIME));

        addShapelessRecipe(new ItemStack(Itemss.ASSEMBLY_PROGRAM, 1, 2), new ItemStack(Itemss.ASSEMBLY_PROGRAM, 1, 0), new ItemStack(Itemss.ASSEMBLY_PROGRAM, 1, 1));

        addPressureChamberRecipes();
        addAssemblyRecipes();
        addThermopneumaticProcessingPlantRecipes();
        registerAmadronOffers();
        addCoolingRecipes();
    }

    private static void addOneProbeRecipe() {

    }

    public static ItemStack getUpgrade(EnumUpgrade upgrade) {
        return new ItemStack(Itemss.upgrades.get(upgrade), 1);
    }

    public static void addProgrammingPuzzleRecipes() {
        NonNullList<ItemStack> widgets = NonNullList.create();
        ItemProgrammingPuzzle.addItems(widgets);
        for (ItemStack output : widgets) {
            output.setCount(4);
            addRecipe(output, "ppp", "pcp", "ppp", 'p', new ItemStack(Itemss.PLASTIC, 1, output.getItemDamage()), 'c', Itemss.PRINTED_CIRCUIT_BOARD);
        }
    }

    private static void addPressureChamberRecipes() {
        IPneumaticRecipeRegistry registry = PneumaticRegistry.getInstance().getRecipeRegistry();

        // diamond
        if (ConfigHandler.recipes.enableCoalToDiamondsRecipe) {
            registry.registerPressureChamberRecipe(new ItemStack[]{new ItemStack(Blocks.COAL_BLOCK, 8, 0)},
                    4.0F,
                    new ItemStack[]{new ItemStack(Items.DIAMOND, 1, 0)});
        }

        // compressed iron
        registry.registerPressureChamberRecipe(new Object[]{new ImmutablePair<>("ingotIron", 1)}, 2F,
                new ItemStack[]{new ItemStack(Itemss.INGOT_IRON_COMPRESSED, 1, 0)});
        registry.registerPressureChamberRecipe(new Object[]{new ImmutablePair<>("blockIron", 1)}, 2F,
                new ItemStack[]{new ItemStack(Blockss.COMPRESSED_IRON, 1, 0)});

        // turbine blade
        registry.registerPressureChamberRecipe(new Object[]{new ImmutablePair<>("dustRedstone", 2), new ImmutablePair<>("ingotGold", 1)},
                1F,
                new ItemStack[]{new ItemStack(Itemss.TURBINE_BLADE, 1, 0)});
        // Empty PCB
        registry.registerPressureChamberRecipe(new Object[]{new ItemStack(Itemss.PLASTIC, 1, ItemPlastic.GREEN), new ImmutablePair<>(Names.INGOT_IRON_COMPRESSED, 1)},
                1.5F,
                new ItemStack[]{new ItemStack(Itemss.EMPTY_PCB, 1, Itemss.EMPTY_PCB.getMaxDamage())});
        // Etching Acid Bucket
        registry.registerPressureChamberRecipe(new ItemStack[]{new ItemStack(Itemss.PLASTIC, 2, ItemPlastic.GREEN), new ItemStack(Items.ROTTEN_FLESH, 2, 0), new ItemStack(Items.GUNPOWDER, 2, 0), new ItemStack(Items.SPIDER_EYE, 2, 0), new ItemStack(Items.WATER_BUCKET)},
                1.0F,
                new ItemStack[]{Fluids.getBucketStack(Fluids.ETCHING_ACID)});
        // Transistor
        registry.registerPressureChamberRecipe(new Object[]{new ItemStack(Itemss.PLASTIC, 1, ItemPlastic.BLACK), new ImmutablePair<>("ingotIronCompressed", 1), new ImmutablePair<>("dustRedstone", 1)},
                1.0F,
                new ItemStack[]{new ItemStack(Itemss.TRANSISTOR)});
        // Capacitor
        registry.registerPressureChamberRecipe(new Object[]{new ItemStack(Itemss.PLASTIC, 1, ItemPlastic.CYAN), new ImmutablePair<>("ingotIronCompressed", 1), new ImmutablePair<>("dustRedstone", 1)},
                1.0F,
                new ItemStack[]{new ItemStack(Itemss.CAPACITOR)});

        // Vacuum dis-enchanting
        registry.registerPressureChamberRecipe(new PressureChamberVacuumEnchantHandler());
    }

    private static void addAssemblyRecipes() {
        AssemblyRecipe.addLaserRecipe(new ItemStack(Itemss.EMPTY_PCB, 1, Itemss.EMPTY_PCB.getMaxDamage()), Itemss.UNASSEMBLED_PCB);
        AssemblyRecipe.addLaserRecipe(new ItemStack(Blockss.PRESSURE_CHAMBER_VALVE, 20, 0), new ItemStack(Blockss.ADVANCED_PRESSURE_TUBE, 8, 0));
        AssemblyRecipe.addLaserRecipe(Blocks.QUARTZ_BLOCK, new ItemStack(Blockss.APHORISM_TILE, 4, 0));

        AssemblyRecipe.addDrillRecipe(new ItemStack(Blockss.COMPRESSED_IRON, 1, 0), new ItemStack(Blockss.PRESSURE_CHAMBER_VALVE, 20, 0));
        AssemblyRecipe.addDrillRecipe(new ItemStack(Items.REDSTONE, 1, 0), new ItemStack(Items.DYE, 5, 1));
    }

    public static void addAssemblyCombinedRecipes() {
        calculateAssemblyChain(AssemblyRecipe.drillRecipes, AssemblyRecipe.laserRecipes, AssemblyRecipe.drillLaserRecipes);
    }

    private static void calculateAssemblyChain(List<AssemblyRecipe> firstRecipeList, List<AssemblyRecipe> secondRecipeList, List<AssemblyRecipe> totalRecipeList) {
        for (AssemblyRecipe firstRecipe : firstRecipeList) {
            for (AssemblyRecipe secondRecipe : secondRecipeList) {
                if (firstRecipe.getOutput().isItemEqual(secondRecipe.getInput()) && firstRecipe.getOutput().getCount() % secondRecipe.getInput().getCount() == 0 && secondRecipe.getOutput().getMaxStackSize() >= secondRecipe.getOutput().getCount() * (firstRecipe.getOutput().getCount() / secondRecipe.getInput().getCount())) {
                    ItemStack output = secondRecipe.getOutput().copy();
                    output.setCount(output.getCount() * (firstRecipe.getOutput().getCount() / secondRecipe.getInput().getCount()));
                    totalRecipeList.add(new AssemblyRecipe(firstRecipe.getInput(), output));
                }
            }
        }
    }

    /**
     * Adds recipes like 9 gold ingot --> 1 gold block, and 1 gold block --> 9 gold ingots.
     */
    public static void addPressureChamberStorageBlockRecipes() {
        // search for a 3x3 recipe where all 9 ingredients are the same
        for (IRecipe recipe : CraftingManager.REGISTRY) {
            if (recipe instanceof ShapedRecipes) {
                ShapedRecipes shaped = (ShapedRecipes) recipe;
                NonNullList<Ingredient> ingredients = recipe.getIngredients();
                ItemStack ref = ingredients.get(0).getMatchingStacks()[0];
                if (ref.isEmpty() || ingredients.size() < 9) continue;
                boolean valid = true;
                for (int i = 0; i < 9; i++) {
                    ItemStack stack = ingredients.get(i).getMatchingStacks()[0];
                    if (!stack.isItemEqual(ref)) {
                        valid = false;
                        break;
                    }
                }
                if (valid) {
                    ItemStack inputStack = ref.copy();
                    inputStack.setCount(9);
                    PressureChamberRecipe.chamberRecipes.add(new PressureChamberRecipe(new ItemStack[]{inputStack}, 1.0F, new ItemStack[]{shaped.getRecipeOutput()}, false));

                    ItemStack inputStack2 = shaped.getRecipeOutput().copy();
                    inputStack2.setCount(1);
                    PressureChamberRecipe.chamberRecipes.add(new PressureChamberRecipe(new ItemStack[]{inputStack2}, -0.5F, new ItemStack[]{inputStack}, false));

                }
            }
        }
    }

    private static void addThermopneumaticProcessingPlantRecipes() {
        PneumaticRecipeRegistry registry = PneumaticRecipeRegistry.getInstance();
        registry.registerThermopneumaticProcessingPlantRecipe(new FluidStack(Fluids.LPG, 100), new ItemStack(Items.COAL), new FluidStack(Fluids.PLASTIC, 1000), 373, 0);
        registry.registerThermopneumaticProcessingPlantRecipe(new FluidStack(Fluids.DIESEL, 1000), new ItemStack(Items.REDSTONE), new FluidStack(Fluids.LUBRICANT, 1000), 373, 0);
        registry.registerThermopneumaticProcessingPlantRecipe(new FluidStack(Fluids.DIESEL, 100), ItemStack.EMPTY, new FluidStack(Fluids.KEROSENE, 80), 573, 2);
        registry.registerThermopneumaticProcessingPlantRecipe(new FluidStack(Fluids.KEROSENE, 100), ItemStack.EMPTY, new FluidStack(Fluids.GASOLINE, 80), 573, 2);
        registry.registerThermopneumaticProcessingPlantRecipe(new FluidStack(Fluids.GASOLINE, 100), ItemStack.EMPTY, new FluidStack(Fluids.LPG, 80), 573, 2);
    }

    private static void registerAmadronOffers() {
        PneumaticRecipeRegistry registry = PneumaticRecipeRegistry.getInstance();
        registry.registerDefaultStaticAmadronOffer(new ItemStack(Items.EMERALD, 8), new ItemStack(Itemss.PCB_BLUEPRINT));
        registry.registerDefaultStaticAmadronOffer(new ItemStack(Items.EMERALD, 8), new ItemStack(Itemss.ASSEMBLY_PROGRAM, 1, 0));
        registry.registerDefaultStaticAmadronOffer(new ItemStack(Items.EMERALD, 8), new ItemStack(Itemss.ASSEMBLY_PROGRAM, 1, 1));
        registry.registerDefaultStaticAmadronOffer(new ItemStack(Items.EMERALD, 14), new ItemStack(Itemss.ASSEMBLY_PROGRAM, 1, 2));
        registry.registerDefaultStaticAmadronOffer(new FluidStack(Fluids.OIL, 5000), new ItemStack(Items.EMERALD, 1));
        registry.registerDefaultStaticAmadronOffer(new FluidStack(Fluids.DIESEL, 4000), new ItemStack(Items.EMERALD, 1));
        registry.registerDefaultStaticAmadronOffer(new FluidStack(Fluids.LUBRICANT, 2500), new ItemStack(Items.EMERALD, 1));
        registry.registerDefaultStaticAmadronOffer(new FluidStack(Fluids.KEROSENE, 3000), new ItemStack(Items.EMERALD, 1));
        registry.registerDefaultStaticAmadronOffer(new FluidStack(Fluids.GASOLINE, 2000), new ItemStack(Items.EMERALD, 1));
        registry.registerDefaultStaticAmadronOffer(new FluidStack(Fluids.LPG, 1000), new ItemStack(Items.EMERALD, 1));
        registry.registerDefaultStaticAmadronOffer(new ItemStack(Items.EMERALD), new FluidStack(Fluids.OIL, 1000));
        registry.registerDefaultStaticAmadronOffer(new ItemStack(Items.EMERALD, 5), new FluidStack(Fluids.LUBRICANT, 1000));

        for (int i = 0; i < 256; i++) {
            try {
                for (int j = 0; j < 10; j++) {
                    EntityVillager villager = new EntityVillager(null, i);
                    MerchantRecipeList list = villager.getRecipes(null);
                    for (MerchantRecipe recipe : list) {
                        if (recipe.getSecondItemToBuy().isEmpty() && !recipe.getItemToBuy().isEmpty() && !recipe.getItemToSell().isEmpty()) {
                            registry.registerDefaultPeriodicAmadronOffer(recipe.getItemToBuy(), recipe.getItemToSell());
                        }
                    }
                }
            } catch (Throwable e) {
            }
        }
    }

    private static void addCoolingRecipes() {
        PneumaticRecipeRegistry registry = PneumaticRecipeRegistry.getInstance();
        registry.registerHeatFrameCoolRecipe(new ItemStack(Items.WATER_BUCKET), new ItemStack(Blocks.ICE));
        registry.registerHeatFrameCoolRecipe(new ItemStack(Items.LAVA_BUCKET), new ItemStack(Blocks.OBSIDIAN));
    }
    
    private static void addRefineryRecipes() {
    	PneumaticRecipeRegistry registry = PneumaticRecipeRegistry.getInstance();
    	registry.registerRefineryRecipe(new FluidStack(Fluids.OIL, 10), new FluidStack(Fluids.DIESEL, 4), new FluidStack(Fluids.LPG, 2));
    	registry.registerRefineryRecipe(new FluidStack(Fluids.OIL, 10), new FluidStack(Fluids.DIESEL, 2), new FluidStack(Fluids.KEROSENE, 3), new FluidStack(Fluids.LPG, 2));
    	registry.registerRefineryRecipe(new FluidStack(Fluids.OIL, 10), new FluidStack(Fluids.DIESEL, 2), new FluidStack(Fluids.KEROSENE, 3), new FluidStack(Fluids.GASOLINE, 3), new FluidStack(Fluids.LPG, 2));
    }

    private static int recipeIndex = 0;

    public static void addRecipe(ItemStack result, Object... recipe) {
        String recipeName = "recipe_" + recipeIndex++;
        ShapedOreRecipe newRecipe = new ShapedOreRecipe(RL(recipeName), result, recipe);
        ForgeRegistries.RECIPES.register(newRecipe.setRegistryName(RL(recipeName)));
        scanForFluids(newRecipe);
    }

    public static void addShapelessRecipe(ItemStack result, Object... recipe) {
        String recipeName = "recipe_" + recipeIndex++;
        ForgeRegistries.RECIPES.register(new ShapelessOreRecipe(RL(recipeName), result, recipe).setRegistryName(RL(recipeName)));
    }

    private static void scanForFluids(ShapedOreRecipe recipe) {
        for (int i = 0; i < recipe.getIngredients().size(); i++) {
            Ingredient ingredient = recipe.getIngredients().get(i);
            for (ItemStack stack : ingredient.getMatchingStacks()) {
                FluidStack fluid = FluidUtil.getFluidContained(stack);
                if (fluid != null) {
                    ForgeRegistries.RECIPES.register(new RecipeFluid(recipe, i));
                }
            }
        }
    }
}
