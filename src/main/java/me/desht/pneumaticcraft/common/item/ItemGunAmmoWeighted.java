package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

public class ItemGunAmmoWeighted extends ItemGunAmmo {
    @Override
    public int getMaxDamage(ItemStack stack) {
        return PNCConfig.Common.Minigun.weightedAmmoCartridgeSize;
    }

    @Override
    public int getAmmoColor(ItemStack ammo) {
        return 0x00404040;
    }

    @Override
    public float getRangeMultiplier(ItemStack ammoStack) {
        return (float) PNCConfig.Common.Minigun.weightedAmmoRangeMultiplier;
    }

    @Override
    public float getAirUsageMultiplier(Minigun minigun, ItemStack ammoStack) {
        return (float) PNCConfig.Common.Minigun.weightedAmmoAirUsageMultiplier;
    }

    @Override
    public float getDamageMultiplier(Entity target, ItemStack ammoStack) {
        return (float) PNCConfig.Common.Minigun.weightedAmmoDamageMultiplier;
    }
}
