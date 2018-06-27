package me.desht.pneumaticcraft.client.render.pneumaticArmor;

import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IOptionPage;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.client.gui.pneumaticHelmet.SimpleToggleableOptions;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;

public class StepAssistUpgradeHandler implements IUpgradeRenderHandler {
    @Override
    public String getUpgradeName() {
        return "stepAssist";
    }

    @Override
    public void initConfig() {

    }

    @Override
    public void saveToConfig() {

    }

    @Override
    public void update(EntityPlayer player, int rangeUpgrades) {

    }

    @Override
    public void render3D(float partialTicks) {

    }

    @Override
    public void render2D(float partialTicks, boolean helmetEnabled) {

    }

    @Override
    public IGuiAnimatedStat getAnimatedStat() {
        return null;
    }

    @Override
    public Item[] getRequiredUpgrades() {
        return new Item[0];
    }

    @Override
    public float getEnergyUsage(int rangeUpgrades, EntityPlayer player) {
        return 0;
    }

    @Override
    public void reset() {

    }

    @Override
    public IOptionPage getGuiOptionsPage() {
        return new SimpleToggleableOptions(this);
    }

    @Override
    public EntityEquipmentSlot getEquipmentSlot() {
        return EntityEquipmentSlot.FEET;
    }
}
