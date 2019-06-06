package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.inventory.ContainerLogistics;
import me.desht.pneumaticcraft.common.semiblock.ItemSemiBlockBase;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockLogistics;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class ItemLogisticsFrame extends ItemSemiBlockBase {

    public ItemLogisticsFrame(String registryName) {
        super(registryName);
        setCreativeTab(PneumaticCraftRepressurized.tabPneumaticCraft);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand handIn) {
        ItemStack stack = player.getHeldItem(handIn);
        if (!world.isRemote) {
            player.openGui(PneumaticCraftRepressurized.instance, getSemiBlock(world, null, stack).getGuiID().ordinal(), world, 0, 0, 0);
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> curInfo, ITooltipFlag extraInfo) {
        super.addInformation(stack, worldIn, curInfo, extraInfo);
        addTooltip(stack, worldIn, curInfo, PneumaticCraftRepressurized.proxy.isSneakingInGui());
    }

    public static void addTooltip(ItemStack stack, World world, List<String> curInfo, boolean sneaking) {
        if (stack.getTagCompound() != null && stack.getItem() instanceof ItemSemiBlockBase) {
            SemiBlockLogistics logistics = ContainerLogistics.getLogistics(world, stack);
            if (logistics == null) return;
            if (logistics.isInvisible()) {
                curInfo.add("- " + PneumaticCraftUtils.xlate("gui.logistic_frame.invisible"));
            }
            String key = SemiBlockManager.getKeyForSemiBlock(SemiBlockManager.getSemiBlockForItem((ItemSemiBlockBase) stack.getItem()));
            if (sneaking) {
                if (logistics.isFuzzyMeta()) curInfo.add("\u2022 " + PneumaticCraftUtils.xlate("gui.logistic_frame.fuzzyMeta"));
                if (logistics.isFuzzyNBT()) curInfo.add("\u2022 " + PneumaticCraftUtils.xlate("gui.logistic_frame.fuzzyNBT"));
                ItemStack[] stacks = new ItemStack[logistics.getFilters().getSlots()];
                for (int i = 0; i < logistics.getFilters().getSlots(); i++) {
                    stacks[i] = logistics.getFilters().getStackInSlot(i);
                }
                curInfo.add(TextFormatting.WHITE + PneumaticCraftUtils.xlate("gui.logistic_frame." + (logistics.isWhitelist() ? "whitelist" : "blacklist")) + ":");
                int l = curInfo.size();
                PneumaticCraftUtils.sortCombineItemStacksAndToString(curInfo, stacks);
                if (curInfo.size() == l) curInfo.add(PneumaticCraftUtils.xlate("gui.misc.no_items"));
                l = curInfo.size();
                for (int i = 0; i < 9; i++) {
                    FluidStack fluid = logistics.getTankFilter(i).getFluid();
                    if (fluid != null) {
                        curInfo.add("\u2022 " + fluid.amount + "mB " + fluid.getLocalizedName());
                    }
                }
                if (curInfo.size() == l) curInfo.add(PneumaticCraftUtils.xlate("gui.misc.no_fluids"));
            } else {
                curInfo.add(PneumaticCraftUtils.xlate(String.format("gui.%s.hasFilters", key)));
            }
        }
    }

}
