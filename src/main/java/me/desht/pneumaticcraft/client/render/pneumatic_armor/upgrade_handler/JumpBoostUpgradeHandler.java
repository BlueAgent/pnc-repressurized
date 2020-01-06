package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiJumpBoostOptions;
import net.minecraft.inventory.EquipmentSlotType;

public class JumpBoostUpgradeHandler extends IUpgradeRenderHandler.SimpleToggleableRenderHandler {
    @Override
    public String getUpgradeID() {
        return "jumpBoost";
    }

    @Override
    public EnumUpgrade[] getRequiredUpgrades() {
        return new EnumUpgrade[] { EnumUpgrade.JUMPING };
    }

    @Override
    public IOptionPage getGuiOptionsPage(IGuiScreen screen) {
        return new GuiJumpBoostOptions(screen,this);
    }

    @Override
    public EquipmentSlotType getEquipmentSlot() {
        return EquipmentSlotType.LEGS;
    }
}
