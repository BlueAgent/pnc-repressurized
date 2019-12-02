package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.api.recipe.IPressureChamberRecipe;
import me.desht.pneumaticcraft.common.util.ItemStackHandlerIterable;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class PressureChamberPressureEnchantHandler implements IPressureChamberRecipe {

    private static final ResourceLocation ID = RL("pressure_chamber_enchanting");

    @Override
    public float getCraftingPressure() {
        return 2F;
    }

    @Override
    public boolean isValidRecipe(ItemStackHandler chamberHandler) {
        return getRecipeIngredients(chamberHandler) != null;
    }

    private ItemStack[] getRecipeIngredients(ItemStackHandler inputStacks) {
        List<ItemStack> enchantedBooks = new ItemStackHandlerIterable(inputStacks)
                                                    .stream()
                                                    .filter(book -> book.getItem() == Items.ENCHANTED_BOOK)
                                                    .collect(Collectors.toList());

        if (enchantedBooks.isEmpty()) return null;

        for (ItemStack inputStack : new ItemStackHandlerIterable(inputStacks)) {
            if ((inputStack.isEnchantable() || inputStack.isEnchanted()) && inputStack.getItem() != Items.ENCHANTED_BOOK) {
                for (ItemStack enchantedBook : enchantedBooks) {
                    Map<Enchantment, Integer> bookMap = EnchantmentHelper.getEnchantments(enchantedBook);
                    for (Map.Entry<Enchantment, Integer> entry : bookMap.entrySet()) {
                        if (entry.getKey().canApply(inputStack)) {
                            return new ItemStack[]{ inputStack, enchantedBook};
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public NonNullList<ItemStack> craftRecipe(ItemStackHandler chamberHandler) {
        ItemStack[] recipeIngredients = getRecipeIngredients(chamberHandler);
        if (recipeIngredients == null) return IPressureChamberRecipe.EMPTY_LIST;

        ItemStack enchantedTool = recipeIngredients[0];
        ItemStack enchantedBook = recipeIngredients[1];
        
        Map<Enchantment, Integer> bookMap = EnchantmentHelper.getEnchantments(enchantedBook);
        bookMap.forEach(enchantedTool::addEnchantment);
        
        enchantedBook.shrink(1);
        return NonNullList.from(ItemStack.EMPTY, new ItemStack(Items.BOOK));
    }

    @Override
    public List<List<ItemStack>> getInputsForDisplay() {
        List<List<ItemStack>> res = new ArrayList<>();

        ItemStack pick = new ItemStack(Items.DIAMOND_PICKAXE);
        res.add(NonNullList.from(ItemStack.EMPTY, pick));

        ItemStack enchBook = new ItemStack(Items.ENCHANTED_BOOK);
        enchBook.addEnchantment(Enchantments.FORTUNE, 1);
        IPressureChamberRecipe.setTooltipKey(enchBook, "gui.nei.tooltip.pressureEnchantBook");
        res.add(NonNullList.from(ItemStack.EMPTY, enchBook));

        return res;
    }

    @Override
    public NonNullList<ItemStack> getResultForDisplay() {
        ItemStack pick = new ItemStack(Items.DIAMOND_PICKAXE);
        pick.addEnchantment(Enchantments.FORTUNE, 1);
        IPressureChamberRecipe.setTooltipKey(pick, "gui.nei.tooltip.pressureEnchantItemOut");
        ItemStack book = new ItemStack(Items.BOOK);
        IPressureChamberRecipe.setTooltipKey(book, "gui.nei.tooltip.pressureEnchantBookOut");
        return NonNullList.from(ItemStack.EMPTY, pick, book);
    }

    @Override
    public boolean isOutputItem(ItemStack stack) {
        return false;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeResourceLocation(ID);
        buf.writeFloat(getCraftingPressure());
        buf.writeVarInt(2);
        getInputsForDisplay().get(0).forEach(s -> Ingredient.fromStacks(s).write(buf));
        buf.writeVarInt(2);
        getResultForDisplay().forEach(buf::writeItemStack);
    }
}
