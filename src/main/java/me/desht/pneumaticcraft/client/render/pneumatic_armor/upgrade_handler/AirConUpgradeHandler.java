package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import com.google.common.base.Strings;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IOptionPage;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiAirConditionerOptions;
import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.item.Itemss;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AirConUpgradeHandler extends IUpgradeRenderHandler.SimpleToggleableRenderHandler {
    private static final int MAX_AC = 20;

    public static int deltaTemp;  // set by packet from server
    private static int currentAC = 0; // cosmetic

    @SideOnly(Side.CLIENT)
    private GuiAnimatedStat acStat;
    private int statX;
    private int statY;
    private boolean statLeftSided;

    @Override
    public String getUpgradeName() {
        return "airConditioning";
    }

    @Override
    public Item[] getRequiredUpgrades() {
        return new Item[] { Itemss.upgrades.get(EnumUpgrade.AIR_CONDITIONING) };
    }

    @Override
    public EntityEquipmentSlot getEquipmentSlot() {
        return EntityEquipmentSlot.CHEST;
    }

    @Override
    public IOptionPage getGuiOptionsPage() {
        return new GuiAirConditionerOptions(this);
    }

    @Override
    public void update(EntityPlayer player, int rangeUpgrades) {
        super.update(player, rangeUpgrades);

        if ((player.world.getTotalWorldTime() & 0x3) == 0) {
            if (currentAC < deltaTemp)
                currentAC++;
            else if (currentAC > deltaTemp)
                currentAC--;
        }

        IGuiAnimatedStat stat = getAnimatedStat();
        if (stat.isClicked()) {
            int ac = MathHelper.clamp(currentAC, -MAX_AC, MAX_AC);
            String bar = (ac < 0 ? TextFormatting.BLUE : TextFormatting.GOLD)
                    + Strings.repeat("|", Math.abs(ac))
                    + TextFormatting.DARK_GRAY
                    + Strings.repeat("|", MAX_AC - Math.abs(ac));
            stat.setTitle(TextFormatting.YELLOW + "A/C: " + bar);
            stat.setBackGroundColor(ac < 0 ? 0x300080FF : (ac == 0 ? 0x3000AA00 : 0x30FFD000));
        }
    }

    @Override
    public void initConfig() {
        statX = ConfigHandler.helmetOptions.acStatX;
        statY = ConfigHandler.helmetOptions.acStatY;
        statLeftSided = ConfigHandler.helmetOptions.acStatLeft;
    }

    @Override
    public void saveToConfig() {
        ConfigHandler.helmetOptions.acStatX = statX = acStat.getBaseX();
        ConfigHandler.helmetOptions.acStatY = statY = acStat.getBaseY();
        ConfigHandler.helmetOptions.acStatLeft = statLeftSided = acStat.isLeftSided();
        ConfigHandler.sync();
    }

    @Override
    public IGuiAnimatedStat getAnimatedStat() {
        if (acStat == null) {
            Minecraft minecraft = Minecraft.getMinecraft();
            ScaledResolution sr = new ScaledResolution(minecraft);
            acStat = new GuiAnimatedStat(null, "", "",
                    statX != -1 ? statX : sr.getScaledWidth() - 2, statY, 0x3000AA00, null, statLeftSided);
            acStat.setMinDimensionsAndReset(0, 0);
        }
        return acStat;
    }
}
