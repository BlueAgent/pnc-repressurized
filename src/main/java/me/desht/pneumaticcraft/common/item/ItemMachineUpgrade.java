package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Rarity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ItemMachineUpgrade extends ItemPneumatic {
    public static final String NBT_DIRECTION = "Facing";
    private final int index;

    public ItemMachineUpgrade(String registryName, int index) {
        super(DEFAULT_PROPS, registryName);
        this.index = index;
    }

    public EnumUpgrade getUpgradeType() {
        return EnumUpgrade.values()[index];
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, World world, List<ITextComponent> infoList, ITooltipFlag par4) {
        if (PneumaticCraftRepressurized.proxy.isSneakingInGui()) {
            infoList.add(xlate("gui.tooltip.item.upgrade.usedIn"));
            PneumaticRegistry.getInstance().getItemRegistry().addTooltip(this, infoList);
        } else {
            infoList.add(xlate("gui.tooltip.item.upgrade.shiftMessage"));
        }
        if (getUpgradeType() == EnumUpgrade.DISPENSER) {
            Direction dir = stack.hasTag() ? Direction.byName(NBTUtil.getString(stack, NBT_DIRECTION)) : null;
            infoList.add(xlate("message.dispenser.direction", dir == null ? "*" : dir.getName()));
            infoList.add(xlate("message.dispenser.clickToSet"));
        }
        super.addInformation(stack, world, infoList, par4);
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        if (getUpgradeType() == EnumUpgrade.DISPENSER) {
            if (!context.getWorld().isRemote) {
                setDirection(context.getPlayer(), context.getHand(), context.getFace());
            }
            return ActionResultType.SUCCESS;
        }
        return super.onItemUseFirst(stack, context);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if (getUpgradeType() == EnumUpgrade.DISPENSER) {
            if (!worldIn.isRemote) {
                setDirection(playerIn, handIn, null);
            }
            return ActionResult.newResult(ActionResultType.SUCCESS, playerIn.getHeldItem(handIn));
        }
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    private void setDirection(PlayerEntity player, Hand hand, Direction facing) {
        ItemStack stack = player.getHeldItem(hand);
        if (facing == null) {
            stack.setTag(null);
            player.sendStatusMessage(new TranslationTextComponent("message.dispenser.direction", "*"), true);
        } else {
            NBTUtil.setString(stack, NBT_DIRECTION, facing.getName());
            player.sendStatusMessage(new TranslationTextComponent("message.dispenser.direction", facing.getName()), true);
        }
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return getUpgradeType() == EnumUpgrade.CREATIVE ? Rarity.EPIC : Rarity.COMMON;
    }
}
