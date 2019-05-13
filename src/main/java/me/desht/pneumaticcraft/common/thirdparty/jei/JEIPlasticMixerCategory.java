package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.recipes.PlasticMixerRegistry;
import me.desht.pneumaticcraft.common.thirdparty.jei.JEIPlasticMixerCategory.PlasticMixerRecipeWrapper;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.IJeiHelpers;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

public class JEIPlasticMixerCategory extends PneumaticCraftCategory<PlasticMixerRecipeWrapper> {

    public JEIPlasticMixerCategory(IJeiHelpers jeiHelpers) {
        super(jeiHelpers);
    }

    @Override
    public String getUid() {
        return ModCategoryUid.PLASTIC_MIXER;
    }

    @Override
    public String getTitle() {
        return I18n.format(Blockss.PLASTIC_MIXER.getTranslationKey() + ".name");
    }

    @Override
    public ResourceDrawable getGuiTexture() {
        return new ResourceDrawable(Textures.GUI_PLASTIC_MIXER, 0, 0, 6, 3, 166, 79);
    }

    static class PlasticMixerRecipeWrapper extends PneumaticCraftCategory.MultipleInputOutputRecipeWrapper {

        private PlasticMixerRecipeWrapper(ItemStack input, FluidStack output, int temperature) {
            addOutputLiquid(output, 146, 11);
            addIngredient(new PositionedStack(input, 92, 23));
            setUsedTemperature(76, 22, temperature);
        }

        private PlasticMixerRecipeWrapper(FluidStack input, ItemStack output) {
            addInputLiquid(input, 146, 11);
            addIngredient(new PositionedStack(getDye("dyeRed", 1), 122, 17));
            addIngredient(new PositionedStack(getDye("dyeGreen", 2), 122, 35));
            addIngredient(new PositionedStack(getDye("dyeBlue", 4), 122, 53));
            addOutput(new PositionedStack(output, 92, 55));
        }

        private ItemStack getDye(String oreDictName, int fallbackMeta) {
            NonNullList<ItemStack> entries = OreDictionary.getOres(oreDictName);
            return entries.isEmpty() ? new ItemStack(Items.DYE, 1, fallbackMeta) : entries.get(0);
        }
    }

    List<MultipleInputOutputRecipeWrapper> getAllRecipes() {
        List<MultipleInputOutputRecipeWrapper> recipes = new ArrayList<>();

        for (PlasticMixerRegistry.PlasticMixerRecipe recipe : PlasticMixerRegistry.INSTANCE.allRecipes()) {
            if (recipe.getFluidStack().amount > 0 && !recipe.getItemStack().isEmpty()) {
                if (recipe.allowSolidifying()) {
                    for (int i = 0; i < 16; i++) {
                        recipes.add(new PlasticMixerRecipeWrapper(recipe.getFluidStack(), new ItemStack(recipe.getItemStack().getItem(), 1, i)));
                    }
                }
                if (recipe.allowMelting()) {
                    int n = recipe.getItemStack().getItem().getHasSubtypes() ? 16 : 1;
                    for (int i = 0; i < n; i++) {
                        recipes.add(new PlasticMixerRecipeWrapper(new ItemStack(recipe.getItemStack().getItem(), 1, i), recipe.getFluidStack(), recipe.getTemperature()));
                    }
                }
            }
        }

        return recipes;
    }
}
