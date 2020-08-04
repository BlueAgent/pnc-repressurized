package me.desht.pneumaticcraft.common.thirdparty.jei;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.crafting.recipe.PressureChamberRecipe;
import me.desht.pneumaticcraft.client.render.pressure_gauge.PressureGaugeRenderer2D;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class JEIPressureChamberRecipeCategory implements IRecipeCategory<PressureChamberRecipe> {
    private final String localizedName;
    private final IDrawable background;
    private final IDrawable icon;
    private final ITickTimer tickTimer;

    JEIPressureChamberRecipeCategory() {
//        super(jeiHelpers);
        background = JEIPlugin.jeiHelpers.getGuiHelper().createDrawable(Textures.GUI_JEI_PRESSURE_CHAMBER, 5, 11, 166, 116);
        icon = JEIPlugin.jeiHelpers.getGuiHelper().createDrawableIngredient(new ItemStack(ModBlocks.PRESSURE_CHAMBER_WALL.get()));
        localizedName = I18n.format("pneumaticcraft.gui.pressureChamber");
        tickTimer = JEIPlugin.jeiHelpers.getGuiHelper().createTickTimer(60, 60, false);
    }

    @Override
    public ResourceLocation getUid() {
        return ModCategoryUid.PRESSURE_CHAMBER;
    }

    @Override
    public String getTitle() {
        return localizedName;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setIngredients(PressureChamberRecipe recipe, IIngredients ingredients) {
        ingredients.setInputIngredients(recipe.getInputsForDisplay());
        ingredients.setOutputs(VanillaTypes.ITEM, recipe.getResultsForDisplay());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, PressureChamberRecipe recipe, IIngredients ingredients) {
        List<Ingredient> inputs = recipe.getInputsForDisplay();
        for (int i = 0; i < inputs.size(); i++) {
            int posX = 18 + i % 3 * 17;
            int posY = 78 - i / 3 * 17;
            recipeLayout.getItemStacks().init(i, true, posX, posY);
            recipeLayout.getItemStacks().set(i, Arrays.asList(inputs.get(i).getMatchingStacks()));
        }
        for (int i = 0; i < recipe.getResultsForDisplay().size(); i++) {
            ItemStack stack = recipe.getResultsForDisplay().get(i);
            recipeLayout.getItemStacks().init(inputs.size() + i, false, 100 + i % 3 * 18, 58 + i / 3 * 18);
            recipeLayout.getItemStacks().set(inputs.size() + i, stack);
        }
        recipeLayout.getItemStacks().addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
            String tooltipKey = recipe.getTooltipKey(input, slotIndex);
            if (!tooltipKey.isEmpty()) {
                tooltip.addAll(PneumaticCraftUtils.splitStringComponent(I18n.format(tooltipKey)));
            }
        });
    }

    @Override
    public void draw(PressureChamberRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
        float pressure = recipe.getCraftingPressure() * ((float) tickTimer.getValue() / tickTimer.getMaxValue());
        PressureGaugeRenderer2D.drawPressureGauge(matrixStack, Minecraft.getInstance().fontRenderer, -1, PneumaticValues.MAX_PRESSURE_PRESSURE_CHAMBER, PneumaticValues.DANGER_PRESSURE_PRESSURE_CHAMBER, recipe.getCraftingPressure(), pressure, 130, 27);
    }

    @Override
    public Class<? extends PressureChamberRecipe> getRecipeClass() {
        return PressureChamberRecipe.class;
    }

    @Override
    public List<ITextComponent> getTooltipStrings(PressureChamberRecipe recipe, double mouseX, double mouseY) {
        if (mouseX >= 100 && mouseY >= 7 && mouseX <= 140 && mouseY <= 47) {
            return ImmutableList.of(xlate("pneumaticcraft.gui.tooltip.pressure", recipe.getCraftingPressure()));
        }
        return Collections.emptyList();
    }
}
