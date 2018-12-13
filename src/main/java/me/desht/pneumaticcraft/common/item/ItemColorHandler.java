package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.block.Blockss;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.awt.*;

public class ItemColorHandler {
    public static void registerColorHandlers() {
        final IItemColor ammoColor = (stack, tintIndex) -> {
            switch (tintIndex) {
                case 1:
                    return getAmmoColor(stack);
                default:
                    return Color.WHITE.getRGB();
            }
        };
        final IItemColor plasticColor = (stack, tintIndex) -> {
            int plasticColour = ItemPlastic.getColour(stack);
            return plasticColour >= 0 ? plasticColour : 0xffffff;
        };
        final IItemColor aphorismTileColor = (stack, tintIndex) -> tintIndex == 0 ? EnumDyeColor.BLUE.getColorValue() : EnumDyeColor.WHITE.getColorValue();

        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(ammoColor, Itemss.GUN_AMMO);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(ammoColor, Itemss.GUN_AMMO_INCENDIARY);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(ammoColor, Itemss.GUN_AMMO_WEIGHTED);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(ammoColor, Itemss.GUN_AMMO_ARMOR_PIERCING);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(ammoColor, Itemss.GUN_AMMO_EXPLOSIVE);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(ammoColor, Itemss.GUN_AMMO_FREEZING);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(plasticColor, Itemss.PLASTIC);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(aphorismTileColor, Item.getItemFromBlock(Blockss.APHORISM_TILE));
    }

    public static int getAmmoColor(@Nonnull ItemStack stack) {
        if (stack.getItem() instanceof ItemGunAmmo) {
            return ((ItemGunAmmo) stack.getItem()).getAmmoColor(stack);
        } else {
            return 0x00FFFF00;
        }
    }
}
