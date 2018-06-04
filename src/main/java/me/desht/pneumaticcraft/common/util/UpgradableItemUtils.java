package me.desht.pneumaticcraft.common.util;

import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.NBTUtil;
import me.desht.pneumaticcraft.common.inventory.ChargeableItemHandler;
import me.desht.pneumaticcraft.common.item.Itemss;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;

/**
 * Some helper methods to manage items which can store upgrades (Pneumatic Helmets, Drones...)
 */
public class UpgradableItemUtils {
    /**
     * Add a standardized tooltip listing the installed upgrades in the given item.
     *
     * @param iStack the item
     * @param world the world
     * @param textList list of text to append tooltip too
     * @param flag tooltip flag
     * @return number of (unique) upgrades in the item
     */
    public static int addUpgradeInformation(ItemStack iStack, World world, List<String> textList, ITooltipFlag flag) {
        ItemStack[] inventoryStacks = getUpgradeStacks(iStack);
        boolean isItemEmpty = true;
        for (ItemStack stack : inventoryStacks) {
            if (!stack.isEmpty()) {
                isItemEmpty = false;
                break;
            }
        }
        if (isItemEmpty) {
            textList.add(I18n.format("gui.tooltip.upgrades.empty"));
        } else {
            textList.add(I18n.format("gui.tooltip.upgrades.not_empty"));
            PneumaticCraftUtils.sortCombineItemStacksAndToString(textList, inventoryStacks);
        }
        return inventoryStacks.length;
    }

    /**
     * Retrieves the upgrades currently installed on the given itemstack.
     */
    public static ItemStack[] getUpgradeStacks(ItemStack iStack) {
        NBTTagCompound tag = NBTUtil.getCompoundTag(iStack, ChargeableItemHandler.NBT_UPGRADE_TAG);
        ItemStack[] inventoryStacks = new ItemStack[9];
        Arrays.fill(inventoryStacks, ItemStack.EMPTY);
        NBTTagList itemList = tag.getTagList("Items", 10);
        for (int i = 0; i < itemList.tagCount(); i++) {
            NBTTagCompound slotEntry = itemList.getCompoundTagAt(i);
            int j = slotEntry.getByte("Slot");
            if (j >= 0 && j < 9) {
                inventoryStacks[j] = new ItemStack(slotEntry);
            }
        }
        return inventoryStacks;
    }

    public static int getUpgrades(EnumUpgrade upgrade, ItemStack stack) {
        return getUpgrades(Itemss.upgrades.get(upgrade), stack);
    }

    public static int getUpgrades(Item upgrade, ItemStack iStack) {
        int upgrades = 0;
        ItemStack[] stacks = getUpgradeStacks(iStack);
        for (ItemStack stack : stacks) {
            if (stack.getItem() == upgrade) {
                upgrades += stack.getCount();
            }
        }
        return upgrades;
    }
}
