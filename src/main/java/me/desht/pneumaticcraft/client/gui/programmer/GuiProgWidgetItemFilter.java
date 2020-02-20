package me.desht.pneumaticcraft.client.gui.programmer;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.gui.GuiInventorySearcher;
import me.desht.pneumaticcraft.client.gui.GuiItemSearcher;
import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget.WidgetDifficulty;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetItemFilter;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;

public class GuiProgWidgetItemFilter extends GuiProgWidgetOptionBase<ProgWidgetItemFilter> {
    private GuiItemSearcher searchGui;
    private GuiInventorySearcher invSearchGui;
    private WidgetCheckBox checkBoxUseDamage;
    private WidgetCheckBox checkBoxUseNBT;
    private WidgetCheckBox checkBoxUseOreDict;
    private WidgetCheckBox checkBoxUseModSimilarity;
    private WidgetCheckBox checkBoxMatchBlock;
    private WidgetComboBox variableField;

    public GuiProgWidgetItemFilter(ProgWidgetItemFilter widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        addButton(new WidgetButtonExtended(guiLeft + 4, guiTop + 24, 70, 20, "Search item...", b -> openSearcher()));
        addButton(new WidgetButtonExtended(guiLeft + 78, guiTop + 24, 100, 20, "Search inventory...", b -> openInventorySearcher()));

        checkBoxUseDamage = new WidgetCheckBox(guiLeft + 8, guiTop + 96, 0xFF404040,
                "Match Item durability", b -> progWidget.useItemDurability = b.checked);
        checkBoxUseDamage.setTooltip(Arrays.asList("Check to handle differently damaged", "tools as different."));
        checkBoxUseDamage.checked = progWidget.useItemDurability;
        addButton(checkBoxUseDamage);

        checkBoxUseNBT = new WidgetCheckBox(guiLeft + 8, guiTop + 108, 0xFF404040, "Match Item NBT", b -> {
            progWidget.useNBT = b.checked;
            checkBoxMatchBlock.active = !b.checked;
        });
        checkBoxUseNBT.setTooltip(Arrays.asList("Check to handle items like Enchanted Books", "or Fireworks as different."));
        checkBoxUseNBT.checked = progWidget.useNBT;
        addButton(checkBoxUseNBT);

        checkBoxUseOreDict = new WidgetCheckBox(guiLeft + 8, guiTop + 120, 0xFF404040, "Match Item Tags", b -> {
            progWidget.useItemTags = b.checked;
            checkBoxUseDamage.active = !b.checked;
            checkBoxUseNBT.active = !b.checked;
            checkBoxUseModSimilarity.active = !b.checked;
            checkBoxMatchBlock.active = !b.checked;
        });
        checkBoxUseOreDict.setTooltip(Arrays.asList("Check to handle items with", "common Item Tags as the same."));
        checkBoxUseOreDict.checked = progWidget.useItemTags;
        addButton(checkBoxUseOreDict);

        checkBoxUseModSimilarity = new WidgetCheckBox(guiLeft + 8, guiTop + 132, 0xFF404040, "Match by Mod", b -> {
            progWidget.useModSimilarity = b.checked;
            checkBoxUseDamage.active = !b.checked;
            checkBoxUseNBT.active = !b.checked;
            checkBoxUseOreDict.active = !b.checked;
            checkBoxMatchBlock.active = !b.checked;
        });
        checkBoxUseModSimilarity.setTooltip(Arrays.asList("Check to handle items from the", "same mod as the same."));
        checkBoxUseModSimilarity.checked = progWidget.useModSimilarity;
        addButton(checkBoxUseModSimilarity);

        checkBoxMatchBlock = new WidgetCheckBox(guiLeft + 8, guiTop + 144, 0xFF404040, "Match by Block", b -> {
            progWidget.matchBlock = b.checked;
            checkBoxUseModSimilarity.active = !b.checked;
            checkBoxUseNBT.active = !b.checked;
            checkBoxUseOreDict.active = !b.checked;
        });
        checkBoxMatchBlock.setTooltip(Arrays.asList("Check to match by block instead of", "dropped item. Useful for blocks", "which don't drop an item.", TextFormatting.GRAY.toString() + TextFormatting.ITALIC + "Only used by the 'Dig' programming piece."));
        checkBoxMatchBlock.checked = progWidget.matchBlock;
        addButton(checkBoxMatchBlock);

        variableField = new WidgetComboBox(font, guiLeft + 90, guiTop + 60, 80, font.FONT_HEIGHT + 1);
        variableField.setElements(guiProgrammer.te.getAllVariables());
        variableField.setMaxStringLength(GlobalVariableManager.MAX_VARIABLE_LEN);
        variableField.setText(progWidget.getVariable());

        if (PNCConfig.Client.programmerDifficulty == WidgetDifficulty.ADVANCED) {
            addButton(variableField);
        }

        checkBoxUseDamage.active = !checkBoxUseOreDict.checked && !checkBoxUseModSimilarity.checked;
        checkBoxUseNBT.active = !checkBoxUseOreDict.checked && !checkBoxUseModSimilarity.checked && !checkBoxMatchBlock.checked;
        checkBoxUseOreDict.active = !checkBoxUseModSimilarity.checked && !checkBoxMatchBlock.checked;
        checkBoxUseModSimilarity.active = !checkBoxUseOreDict.checked && !checkBoxMatchBlock.checked;
        checkBoxMatchBlock.active = !checkBoxUseNBT.checked && !checkBoxUseModSimilarity.checked && !checkBoxUseOreDict.checked;

        if (searchGui != null) progWidget.setFilter(searchGui.getSearchStack());
        if (invSearchGui != null) progWidget.setFilter(invSearchGui.getSearchStack());
        searchGui = null;
        invSearchGui = null;

        checkBoxUseDamage.active = progWidget.getFilter().getMaxDamage() > 0;
    }

    private void openSearcher() {
        ClientUtils.openContainerGui(ModContainers.ITEM_SEARCHER.get(), new StringTextComponent("Search"));
        if (minecraft.currentScreen instanceof GuiItemSearcher) {
            searchGui = (GuiItemSearcher) minecraft.currentScreen;
            searchGui.setSearchStack(progWidget.getFilter());
        }
    }

    private void openInventorySearcher() {
        ClientUtils.openContainerGui(ModContainers.INVENTORY_SEARCHER.get(), new StringTextComponent("Search"));
        if (minecraft.currentScreen instanceof GuiInventorySearcher) {
            invSearchGui = (GuiInventorySearcher) minecraft.currentScreen;
            invSearchGui.setSearchStack(progWidget.getFilter());
        }
    }

    public void setFilterStack(ItemStack stack) {
        progWidget.setFilter(stack);
    }

    @Override
    public void onClose() {
        progWidget.setVariable(variableField.getText());

        super.onClose();
    }
    
    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        minecraft.getTextureManager().bindTexture(getTexture());
        GlStateManager.enableTexture();
        GlStateManager.color4f(1, 1, 1, 1);
        blit(guiLeft + 49, guiTop + 51, 186, 0, 18, 18);
        if (PNCConfig.Client.programmerDifficulty == WidgetDifficulty.ADVANCED) {
            font.drawString(I18n.format("gui.progWidget.itemFilter.variableLabel"), guiLeft + 90, guiTop + 49, 0xFF404040);
        }
        String f = I18n.format("gui.progWidget.itemFilter.filterLabel");
        font.drawString(f, guiLeft + 48 - font.getStringWidth(f), guiTop + 56, 0xFF404040);
        if (!progWidget.getRawFilter().isEmpty())
            GuiUtils.drawItemStack(progWidget.getRawFilter(), guiLeft + 50, guiTop + 52);
    }
}
