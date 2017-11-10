package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AssemblyRecipe {

    private final ItemStack input;
    private final ItemStack output;

    public AssemblyRecipe(ItemStack input, ItemStack output) {
        this.input = input;
        this.output = output;
    }

    public ItemStack getInput() {
        return input;
    }

    public ItemStack getOutput() {
        return output;
    }

    public static void addDrillRecipe(Object input, Object output) {
        PneumaticRecipeRegistry.getInstance().addAssemblyDrillRecipe(input, output);
    }

    public static void addLaserRecipe(Object input, Object output) {
        PneumaticRecipeRegistry.getInstance().addAssemblyLaserRecipe(input, output);
    }
}
