package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.recipe.IPneumaticRecipeRegistry;
import me.desht.pneumaticcraft.api.recipe.ItemIngredient;
import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.fluid.Fluids;
import me.desht.pneumaticcraft.common.item.ItemPlastic;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.lib.PneumaticValues;
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
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.items.ItemHandlerHelper;

public class CraftingRegistrator {
    @GameRegistry.ObjectHolder("theoneprobe:probe")
    public static final Item ONE_PROBE = null;

    private static ItemStack emptyPCB;

    public static void init() {
        // All crafting recipes are now defined in JSON
        emptyPCB = new ItemStack(Itemss.EMPTY_PCB);
        emptyPCB.setItemDamage(emptyPCB.getMaxDamage());

        GameRegistry.addSmelting(Itemss.FAILED_PCB, emptyPCB, 0);

        addPressureChamberRecipes();
        addAssemblyRecipes();
        addThermopneumaticProcessingPlantRecipes();
        registerAmadronOffers();
        addCoolingRecipes();
        addExplosionCraftingRecipes();
        addPlasticMixerRecipes();
    }

    public static ItemStack getUpgrade(EnumUpgrade upgrade) {
        return new ItemStack(Itemss.upgrades.get(upgrade), 1);
    }

    private static void addExplosionCraftingRecipes() {
        IPneumaticRecipeRegistry registry = PneumaticRegistry.getInstance().getRecipeRegistry();

        registry.registerExplosionCraftingRecipe("ingotIron", new ItemStack(Itemss.INGOT_IRON_COMPRESSED), ConfigHandler.general.configCompressedIngotLossRate);
        registry.registerExplosionCraftingRecipe("blockIron", new ItemStack(Blockss.COMPRESSED_IRON), ConfigHandler.general.configCompressedIngotLossRate);
    }

    private static void addPressureChamberRecipes() {
        IPneumaticRecipeRegistry registry = PneumaticRegistry.getInstance().getRecipeRegistry();

        // diamond
        if (ConfigHandler.recipes.enableCoalToDiamondsRecipe) {
            registry.registerPressureChamberRecipe(
                    new ItemIngredient[]{new ItemIngredient("blockCoal", 8)},
                    4.0F,
                    new ItemStack[]{new ItemStack(Items.DIAMOND, 1, 0)});
        }

        // compressed iron
        registry.registerPressureChamberRecipe(
                new ItemIngredient[]{new ItemIngredient("ingotIron", 1)},
                2F,
                new ItemStack[]{new ItemStack(Itemss.INGOT_IRON_COMPRESSED, 1, 0)});
        registry.registerPressureChamberRecipe(
                new ItemIngredient[]{new ItemIngredient("blockIron", 1)},
                2F,
                new ItemStack[]{new ItemStack(Blockss.COMPRESSED_IRON, 1, 0)});

        // turbine blade
        registry.registerPressureChamberRecipe(
                new ItemIngredient[]{
                        new ItemIngredient(new ItemStack(Items.REDSTONE, 2)),
                        new ItemIngredient("ingotGold", 1)},
                1F,
                new ItemStack[]{new ItemStack(Itemss.TURBINE_BLADE, 1, 0)});

        // Empty PCB
        registry.registerPressureChamberRecipe(
                new ItemIngredient[]{
                        new ItemIngredient(Itemss.PLASTIC, 1, ItemPlastic.GREEN),
                        new ItemIngredient(Names.INGOT_IRON_COMPRESSED, 1)},
                1.5F,
                new ItemStack[]{emptyPCB});

        // Etching Acid Bucket
        registry.registerPressureChamberRecipe(
                new ItemIngredient[]{
                        new ItemIngredient(Itemss.PLASTIC, 2, ItemPlastic.GREEN),
                        new ItemIngredient(Items.ROTTEN_FLESH, 2, 0),
                        new ItemIngredient(Items.GUNPOWDER, 2, 0),
                        new ItemIngredient(Items.SPIDER_EYE, 2, 0),
                        new ItemIngredient(Items.WATER_BUCKET, 1, 0)},
                1.0F,
                new ItemStack[]{Fluids.getBucketStack(Fluids.ETCHING_ACID)});

        // Transistor
        registry.registerPressureChamberRecipe(
                new ItemIngredient[]{
                new ItemIngredient(Itemss.PLASTIC, 1, ItemPlastic.BLACK),
                        new ItemIngredient("ingotIronCompressed", 1),
                        new ItemIngredient("dustRedstone", 1)},
                1.0F,
                new ItemStack[]{new ItemStack(Itemss.TRANSISTOR)});

        // Capacitor
        registry.registerPressureChamberRecipe(
                new ItemIngredient[]{
                        new ItemIngredient(Itemss.PLASTIC, 1, ItemPlastic.CYAN),
                        new ItemIngredient("ingotIronCompressed", 1),
                        new ItemIngredient("dustRedstone", 1)},
                1.0F,
                new ItemStack[]{new ItemStack(Itemss.CAPACITOR)});

        // Pressure enchanting
        registry.registerPressureChamberRecipe(new PressureChamberPressureEnchantHandler());
        
        // Vacuum dis-enchanting
        registry.registerPressureChamberRecipe(new PressureChamberVacuumEnchantHandler());
    }

    private static void addAssemblyRecipes() {
        AssemblyRecipe.addLaserRecipe(emptyPCB, Itemss.UNASSEMBLED_PCB);
        AssemblyRecipe.addLaserRecipe(new ItemStack(Blockss.PRESSURE_CHAMBER_VALVE, 20, 0), new ItemStack(Blockss.ADVANCED_PRESSURE_TUBE, 8, 0));
        AssemblyRecipe.addLaserRecipe(Blocks.QUARTZ_BLOCK, new ItemStack(Blockss.APHORISM_TILE, 4, 0));

        AssemblyRecipe.addDrillRecipe(new ItemStack(Blockss.COMPRESSED_IRON, 1, 0), new ItemStack(Blockss.PRESSURE_CHAMBER_VALVE, 20, 0));
        AssemblyRecipe.addDrillRecipe(new ItemStack(Items.REDSTONE, 1, 0), new ItemStack(Items.DYE, 5, 1));
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
                    IPneumaticRecipeRegistry registry = PneumaticRegistry.getInstance().getRecipeRegistry();
                    ItemStack inputStack = ItemHandlerHelper.copyStackWithSize(ref, 9);
                    registry.registerPressureChamberRecipe(
                            new ItemIngredient[]{new ItemIngredient(inputStack)},
                            1.0F, new ItemStack[]{shaped.getRecipeOutput()});
                    ItemStack inputStack2 = ItemHandlerHelper.copyStackWithSize(shaped.getRecipeOutput(), 1);
                    registry.registerPressureChamberRecipe(
                            new ItemIngredient[]{new ItemIngredient(inputStack2)},
                            -0.5F, new ItemStack[]{inputStack});

//                    ItemStack inputStack = ref.copy();
//                    inputStack.setCount(9);
//                    PressureChamberRecipe.chamberRecipes.add(new PressureChamberRecipe(new ItemStack[]{inputStack}, 1.0F, new ItemStack[]{shaped.getRecipeOutput()}, false));

//                    ItemStack inputStack2 = shaped.getRecipeOutput().copy();
//                    inputStack2.setCount(1);
//                    PressureChamberRecipe.chamberRecipes.add(new PressureChamberRecipe(new ItemStack[]{inputStack2}, -0.5F, new ItemStack[]{inputStack}, false));

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
            } catch (Throwable ignored) {
            }
        }
    }

    private static void addCoolingRecipes() {
        PneumaticRecipeRegistry registry = PneumaticRecipeRegistry.getInstance();
        registry.registerHeatFrameCoolRecipe(new ItemIngredient(new ItemStack(Items.WATER_BUCKET)), new ItemStack(Blocks.ICE));
        registry.registerHeatFrameCoolRecipe(new ItemIngredient(new ItemStack(Items.LAVA_BUCKET)), new ItemStack(Blocks.OBSIDIAN));
    }

    private static void addPlasticMixerRecipes() {
        PneumaticCraftAPIHandler.getInstance().getRecipeRegistry().registerPlasticMixerRecipe(
                new FluidStack(Fluids.PLASTIC, ConfigHandler.machineProperties.plasticMixerPlasticRatio),
                new ItemStack(Itemss.PLASTIC),
                PneumaticValues.PLASTIC_MIXER_MELTING_TEMP, true, true
        );
    }
}
