package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.client.IFOVModifierItem;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderCoordWireframe;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.UpgradeRenderHandlerList;
import me.desht.pneumaticcraft.common.GuiHandler.EnumGuiId;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.recipes.CraftingRegistrator;
import me.desht.pneumaticcraft.common.recipes.factories.OneProbeRecipeFactory;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.lib.ModIds;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.items.ItemHandlerHelper;
import thaumcraft.api.items.IGoggles;
import thaumcraft.api.items.IRevealer;
import thaumcraft.api.items.IVisDiscountGear;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@Optional.InterfaceList({
        @Optional.Interface(iface = "thaumcraft.api.items.IGoggles", modid = ModIds.THAUMCRAFT),
        @Optional.Interface(iface = "thaumcraft.api.items.IVisDiscountGear", modid = ModIds.THAUMCRAFT),
        @Optional.Interface(iface = "thaumcraft.api.items.IRevealer", modid = ModIds.THAUMCRAFT)
})
public class ItemPneumaticArmor extends ItemArmor
        implements IPressurizable, IChargingStationGUIHolderItem, IUpgradeAcceptor, ISpecialArmor,
        IVisDiscountGear, IGoggles, IRevealer, IFOVModifierItem
{
    private static final ArmorMaterial COMPRESSED_IRON_MATERIAL = EnumHelper.addArmorMaterial(
            "compressedIron", "compressedIron",
            PneumaticValues.PNEUMATIC_ARMOR_DURABILITY_BASE,
            new int[]{2, 5, 6, 2}, 9,
            SoundEvents.ITEM_ARMOR_EQUIP_IRON, 1.0F
    );

    private static final int[] ARMOR_VOLUMES = new int[] {
            PneumaticValues.PNEUMATIC_BOOTS_VOLUME,
            PneumaticValues.PNEUMATIC_LEGGINGS_VOLUME,
            PneumaticValues.PNEUMATIC_CHESTPLATE_VOLUME,
            PneumaticValues.PNEUMATIC_HELMET_VOLUME
    };
    private static final int[] VIS_DISCOUNTS = new int[] { 1, 2, 2, 5 };
    private static final List<Set<Item>> applicableUpgrades = new ArrayList<>();

    public static final String NBT_SEARCH_STACK = "SearchStack";
    public static final String NBT_COORD_TRACKER = "CoordTracker";
    public static final String NBT_ENTITY_FILTER = "entityFilter";
    public static final String NBT_JUMP_BOOST = "jumpBoost";
    public static final String NBT_SPEED_BOOST = "speedBoost";
    public static final String NBT_BUILDER_MODE = "JetBootsBuilderMode";

    public ItemPneumaticArmor(String name, EntityEquipmentSlot equipmentSlotIn) {
        super(COMPRESSED_IRON_MATERIAL, PneumaticCraftRepressurized.proxy.getArmorRenderID(Textures.ARMOR_PNEUMATIC), equipmentSlotIn);

        setRegistryName(name);
        setTranslationKey(name);
        setCreativeTab(PneumaticCraftRepressurized.tabPneumaticCraft);
    }

    /**
     * Check if the player is wearing any pneumatic armor piece.
     *
     * @param player the player
     * @return true if the player is wearing pneumatic armor
     */
    public static boolean isPlayerWearingAnyPneumaticArmor(EntityPlayer player) {
        for (EntityEquipmentSlot slot : UpgradeRenderHandlerList.ARMOR_SLOTS) {
            if (isPneumaticArmorPiece(player, slot)) return true;
        }
        return false;
    }

    public static boolean isPneumaticArmorPiece(EntityPlayer player, EntityEquipmentSlot slot) {
        return player.getItemStackFromSlot(slot).getItem() instanceof ItemPneumaticArmor;
    }

    /**
     * Get the base item volume before any volume upgrades are added.
     *
     * @return the base volume
     */
    public int getBaseVolume() {
        return ARMOR_VOLUMES[armorType.getIndex()];
    }

    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return slot == EntityEquipmentSlot.LEGS ? Textures.ARMOR_PNEUMATIC + "_2.png" : Textures.ARMOR_PNEUMATIC + "_1.png";
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        ItemPneumatic.addStandardTooltip(stack, worldIn, tooltip, flagIn);

        if (armorType == EntityEquipmentSlot.HEAD) {
            addHelmetInformation(stack, worldIn, tooltip, flagIn);
        }
    }

    private void addHelmetInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (stack.hasTagCompound() && stack.getTagCompound().getInteger(OneProbeRecipeFactory.ONE_PROBE_TAG) == 1) {
            tooltip.add(TextFormatting.BLUE + "The One Probe installed");
        }

        // supplementary search & tracker information
        ItemStack searchedStack = getSearchedStack(stack);
        if (!searchedStack.isEmpty()) {
            for (int i = 0; i < tooltip.size(); i++) {
                if (tooltip.get(i).contains("Item Search")) {
                    tooltip.set(i, tooltip.get(i) + " (searching " + searchedStack.getDisplayName() + ")");
                    break;
                }
            }
        }

        BlockPos pos = getCoordTrackerPos(stack, worldIn);
        if (pos != null) RenderCoordWireframe.addInfo(tooltip, worldIn, pos);
    }

    @Override
    public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack) {
        return false;
    }

    static void initApplicableUpgrades() {
        for (int i = 0; i < 4; i++) {
            applicableUpgrades.add(new HashSet<>());
        }

        for (EntityEquipmentSlot slot : UpgradeRenderHandlerList.ARMOR_SLOTS) {
            Set<Item> upgrades = applicableUpgrades.get(slot.getIndex());
            // upgrades automatically added due to an upgrade handler being registered
            UpgradeRenderHandlerList.instance().getHandlersForSlot(slot).forEach(
                    handler -> Arrays.stream(handler.getRequiredUpgrades())
                            .filter(Objects::nonNull)
                            .forEach(upgrades::add)
            );
            // upgrades common to all armor pieces without a specific handler
            addApplicableUpgrade(slot, EnumUpgrade.SPEED);
            addApplicableUpgrade(slot, EnumUpgrade.VOLUME);
            addApplicableUpgrade(slot, EnumUpgrade.ITEM_LIFE);
            addApplicableUpgrade(slot, EnumUpgrade.ARMOR);
            addApplicableUpgrade(slot, EnumUpgrade.THAUMCRAFT);
        }
        // piece-specific upgrades which don't have a specific handler
        addApplicableUpgrade(EntityEquipmentSlot.HEAD, EnumUpgrade.RANGE);
        addApplicableUpgrade(EntityEquipmentSlot.HEAD, EnumUpgrade.SECURITY);
        addApplicableUpgrade(EntityEquipmentSlot.CHEST, EnumUpgrade.SECURITY);
    }

    private static void addApplicableUpgrade(EntityEquipmentSlot slot, EnumUpgrade what) {
        if (what.isDepLoaded()) {
            applicableUpgrades.get(slot.getIndex()).add(CraftingRegistrator.getUpgrade(what).getItem());
        }
    }

    @Override
    public Set<Item> getApplicableUpgrades() {
        return applicableUpgrades.get(armorType.getIndex());
    }

    @Override
    public float getPressure(ItemStack iStack) {
        int volume = UpgradableItemUtils.getUpgrades(EnumUpgrade.VOLUME, iStack) * PneumaticValues.VOLUME_VOLUME_UPGRADE + getBaseVolume();
        int oldVolume = NBTUtil.getInteger(iStack, "volume");
        int currentAir = NBTUtil.getInteger(iStack, "air");
        if (volume < oldVolume) {
            currentAir = currentAir * volume / oldVolume;
            NBTUtil.setInteger(iStack, "air", currentAir);
        }
        if (volume != oldVolume) {
            NBTUtil.setInteger(iStack, "volume", volume);
        }
        return (float) currentAir / volume;
    }

    @Override
    public float maxPressure(ItemStack iStack) {
        return 10F;
    }

    @Override
    public int getVolume(ItemStack iStack) {
        return UpgradableItemUtils.getUpgrades(EnumUpgrade.VOLUME, iStack) * PneumaticValues.VOLUME_VOLUME_UPGRADE + getBaseVolume();
    }

    @Override
    public void addAir(ItemStack iStack, int amount) {
        int maxAir = (int)(maxPressure(iStack) * getVolume(iStack));
        int oldAir = NBTUtil.getInteger(iStack, "air");
        NBTUtil.setInteger(iStack, "air", Math.min(maxAir, Math.max(oldAir + amount, 0)));
    }

    @Override
    public EnumGuiId getGuiID() {
        return EnumGuiId.PNEUMATIC_ARMOR;
    }

    @Override
    public String getName() {
        return getTranslationKey() + ".name";
    }

    @Override
    public ArmorProperties getProperties(EntityLivingBase player, @Nonnull ItemStack armor, DamageSource source, double damage, int slot) {
        int maxAbsorb = armor.getMaxDamage() + 1 - armor.getItemDamage();
        float ratio;
        if (source.isExplosion()) {
            ratio = 0.18F;  // absorb a lot of explosion damage
        } else {
            ratio = ((ItemArmor) armor.getItem()).damageReduceAmount / 30.0F;
        }
        ArmorProperties ap = new ArmorProperties(1, ratio, maxAbsorb);
        int armorUpgrades = Math.min(6, UpgradableItemUtils.getUpgrades(EnumUpgrade.ARMOR, armor));
        ap.Armor = armorUpgrades * (slot == 2 ? 1.0F : 0.5F);  // slot 2 = chestplate
        ap.Toughness = Math.min(2, armorUpgrades * 0.5F);
        return ap;
    }

    @Override
    public int getArmorDisplay(EntityPlayer player, @Nonnull ItemStack armor, int slot) {
        int armorUpgrades = Math.min(6, UpgradableItemUtils.getUpgrades(EnumUpgrade.ARMOR, armor));
        return Math.min(armorUpgrades, 2);
    }

    @Override
    public void damageArmor(EntityLivingBase entity, @Nonnull ItemStack stack, DamageSource source, int damage, int slot) {
        if (source.isExplosion()) {
            // compressed iron is very explosion-resistant
            return;
        }
        ItemStack copy = stack.copy();
        stack.damageItem(damage, entity);
        if (stack.isEmpty() && entity instanceof EntityPlayer) {
            // armor has been destroyed; return the upgrades to the player, at least
            ItemStack[] upgrades = UpgradableItemUtils.getUpgradeStacks(copy);
            for (ItemStack upgrade : upgrades) {
                ItemHandlerHelper.giveItemToPlayer((EntityPlayer) entity, upgrade);
            }
        }
    }

    /* ----------- Pneumatic Helmet helpers ---------- */

    public static int getIntData(ItemStack stack, String key, int def) {
        if (stack.getItem() instanceof ItemPneumaticArmor && stack.hasTagCompound() && stack.getTagCompound().hasKey(key, Constants.NBT.TAG_INT)) {
            return stack.getTagCompound().getInteger(key);
        } else {
            return def;
        }
    }

    public static boolean getBooleanData(ItemStack stack, String key, boolean def) {
        if (stack.getItem() instanceof ItemPneumaticArmor && stack.hasTagCompound() && stack.getTagCompound().hasKey(key, Constants.NBT.TAG_BYTE)) {
            return stack.getTagCompound().getByte(key) == 1;
        } else {
            return def;
        }
    }

    @Nonnull
    public static ItemStack getSearchedStack(ItemStack helmetStack) {
        if (helmetStack.isEmpty() || !NBTUtil.hasTag(helmetStack, NBT_SEARCH_STACK)) return ItemStack.EMPTY;
        NBTTagCompound tag = NBTUtil.getCompoundTag(helmetStack, NBT_SEARCH_STACK);
        if (tag.getInteger("itemID") == -1) return ItemStack.EMPTY;
        return new ItemStack(Item.getItemById(tag.getInteger("itemID")), 1, tag.getInteger("itemDamage"));
    }

    public static BlockPos getCoordTrackerPos(ItemStack helmetStack, World world) {
        if (helmetStack.isEmpty() || !NBTUtil.hasTag(helmetStack, NBT_COORD_TRACKER)) return null;
        NBTTagCompound tag = NBTUtil.getCompoundTag(helmetStack, NBT_COORD_TRACKER);
        if (tag.getInteger("y") == -1 || world.provider.getDimension() != tag.getInteger("dimID")) {
            return null;
        }
        return NBTUtil.getPos(tag);
    }

    public static void setCoordTrackerPos(ItemStack helmetStack, int dimId, BlockPos pos) {
        NBTTagCompound tag = NBTUtil.getCompoundTag(helmetStack, ItemPneumaticArmor.NBT_COORD_TRACKER);
        tag.setInteger("dimID", dimId);
        NBTUtil.setPos(tag, pos);
    }

    public static String getEntityFilter(ItemStack helmetStack) {
        if (helmetStack.isEmpty() || !NBTUtil.hasTag(helmetStack, NBT_ENTITY_FILTER)) return "";
        return NBTUtil.getString(helmetStack, NBT_ENTITY_FILTER);
    }

    @Override
    public float getFOVModifier(ItemStack stack, EntityPlayer player, EntityEquipmentSlot slot) {
        if (slot == EntityEquipmentSlot.LEGS && ConfigHandler.client.leggingsFOVfactor > 0) {
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer();
            double boost = handler.getSpeedBoostFromLegs();
            if (boost > 0) {
                return 1.0f + (float) (boost * 2.0 * ConfigHandler.client.leggingsFOVfactor);
            }
        }
        return 1.0f;
    }

    /*------- Thaumcraft -------- */

    private boolean hasThaumcraftUpgradeAndPressure(ItemStack stack) {
        return getPressure(stack) > 0F && UpgradableItemUtils.getUpgrades(EnumUpgrade.THAUMCRAFT, stack) > 0;
    }

    @Override
    @Optional.Method(modid = ModIds.THAUMCRAFT)
    public int getVisDiscount(ItemStack stack, EntityPlayer player) {
        return hasThaumcraftUpgradeAndPressure(stack) ? VIS_DISCOUNTS[armorType.getIndex()] : 0;
    }

    @Override
    @Optional.Method(modid = ModIds.THAUMCRAFT)
    public boolean showIngamePopups(ItemStack itemstack, EntityLivingBase player) {
        return armorType == EntityEquipmentSlot.HEAD && hasThaumcraftUpgradeAndPressure(itemstack);
    }

    @Override
    @Optional.Method(modid = ModIds.THAUMCRAFT)
    public boolean showNodes(ItemStack itemstack, EntityLivingBase player) {
        return armorType == EntityEquipmentSlot.HEAD && hasThaumcraftUpgradeAndPressure(itemstack);
    }
}
