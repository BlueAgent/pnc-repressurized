package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.client.IFOVModifierItem;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderCoordWireframe;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.UpgradeRenderHandlerList;
import me.desht.pneumaticcraft.common.config.Config;
import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.recipes.CraftingRegistrator;
import me.desht.pneumaticcraft.common.recipes.factories.OneProbeRecipeFactory;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;

//@Optional.InterfaceList({
//        @Optional.Interface(iface = "thaumcraft.api.items.IGoggles", modid = ModIds.THAUMCRAFT),
//        @Optional.Interface(iface = "thaumcraft.api.items.IVisDiscountGear", modid = ModIds.THAUMCRAFT),
//        @Optional.Interface(iface = "thaumcraft.api.items.IRevealer", modid = ModIds.THAUMCRAFT)
//})
public class ItemPneumaticArmor extends ArmorItem
        implements IPressurizable, IChargeableContainerProvider, IUpgradeAcceptor,
        /*IVisDiscountGear, IGoggles, IRevealer,*/ IFOVModifierItem
{

    private static final IArmorMaterial COMPRESSED_IRON_MATERIAL = new CompressedArmorMaterial();

    private static final int[] ARMOR_VOLUMES = new int[] {
            PneumaticValues.PNEUMATIC_BOOTS_VOLUME,
            PneumaticValues.PNEUMATIC_LEGGINGS_VOLUME,
            PneumaticValues.PNEUMATIC_CHESTPLATE_VOLUME,
            PneumaticValues.PNEUMATIC_HELMET_VOLUME
    };
//    private static final int[] VIS_DISCOUNTS = new int[] { 1, 2, 2, 5 };
    private static final List<Set<Item>> applicableUpgrades = new ArrayList<>();

    public static final String NBT_SEARCH_ITEM = "SearchStack";
    public static final String NBT_COORD_TRACKER = "CoordTracker";
    public static final String NBT_ENTITY_FILTER = "entityFilter";
    public static final String NBT_JUMP_BOOST = "jumpBoost";
    public static final String NBT_SPEED_BOOST = "speedBoost";
    public static final String NBT_BUILDER_MODE = "JetBootsBuilderMode";

    public ItemPneumaticArmor(String name, EquipmentSlotType equipmentSlotIn) {
        super(COMPRESSED_IRON_MATERIAL, equipmentSlotIn, ItemPneumatic.DEFAULT_PROPS);

        setRegistryName(name);
    }

    /**
     * Check if the player is wearing any pneumatic armor piece.
     *
     * @param player the player
     * @return true if the player is wearing pneumatic armor
     */
    public static boolean isPlayerWearingAnyPneumaticArmor(PlayerEntity player) {
        for (EquipmentSlotType slot : UpgradeRenderHandlerList.ARMOR_SLOTS) {
            if (isPneumaticArmorPiece(player, slot)) return true;
        }
        return false;
    }

    public static boolean isPneumaticArmorPiece(PlayerEntity player, EquipmentSlotType slot) {
        return player.getItemStackFromSlot(slot).getItem() instanceof ItemPneumaticArmor;
    }

    /**
     * Get the base item volume before any volume upgrades are added.
     *
     * @return the base volume
     */
    public int getBaseVolume() {
        return ARMOR_VOLUMES[slot.getIndex()];
    }

    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type) {
        return slot == EquipmentSlotType.LEGS ? Textures.ARMOR_PNEUMATIC + "_2.png" : Textures.ARMOR_PNEUMATIC + "_1.png";
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        ItemPneumatic.addStandardTooltip(stack, worldIn, tooltip, flagIn);

        if (slot == EquipmentSlotType.HEAD) {
            addHelmetInformation(stack, worldIn, tooltip, flagIn);
        }
    }

    private void addHelmetInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if (stack.hasTag() && stack.getTag().getInt(OneProbeRecipeFactory.ONE_PROBE_TAG) == 1) {
            tooltip.add(new StringTextComponent("The One Probe installed").applyTextStyle(TextFormatting.BLUE));
        }

        // supplementary search & tracker information
        Item searchedItem = getSearchedItem(stack);
        if (searchedItem != null) {
            for (int i = 0; i < tooltip.size(); i++) {
                if (tooltip.get(i).getFormattedText().contains("Item Search")) {
                    ItemStack searchedStack = new ItemStack(searchedItem);
                    tooltip.set(i, tooltip.get(i).appendText(" (searching " + searchedStack.getDisplayName().getFormattedText() + ")"));
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

    public static void initApplicableUpgrades() {
        for (int i = 0; i < 4; i++) {
            applicableUpgrades.add(new HashSet<>());
        }

        for (EquipmentSlotType slot : UpgradeRenderHandlerList.ARMOR_SLOTS) {
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
        addApplicableUpgrade(EquipmentSlotType.HEAD, EnumUpgrade.RANGE);
        addApplicableUpgrade(EquipmentSlotType.CHEST, EnumUpgrade.SECURITY);
    }

    private static void addApplicableUpgrade(EquipmentSlotType slot, EnumUpgrade what) {
        if (what.isDepLoaded()) {
            applicableUpgrades.get(slot.getIndex()).add(CraftingRegistrator.getUpgrade(what).getItem());
        }
    }

    @Override
    public Set<Item> getApplicableUpgrades() {
        return applicableUpgrades.get(slot.getIndex());
    }

    @Override
    public String getUpgradeAcceptorTranslationKey() {
        return getTranslationKey();
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

    // todo 1.14 ISpecialArmor ?
//    @Override
//    public ArmorProperties getProperties(LivingEntity player, @Nonnull ItemStack armor, DamageSource source, double damage, int slot) {
//        int maxAbsorb = armor.getMaxDamage() + 1 - armor.getItemDamage();
//        float ratio;
//        if (source.isExplosion()) {
//            ratio = 0.18F;  // absorb a lot of explosion damage
//        } else {
//            ratio = ((ArmorItem) armor.getItem()).damageReduceAmount / 30.0F;
//        }
//        ArmorProperties ap = new ArmorProperties(1, ratio, maxAbsorb);
//        int armorUpgrades = Math.min(6, UpgradableItemUtils.getUpgrades(EnumUpgrade.ARMOR, armor));
//        ap.Armor = armorUpgrades * (slot == 2 ? 1.0F : 0.5F);  // slot 2 = chestplate
//        ap.Toughness = Math.min(2, armorUpgrades * 0.5F);
//        return ap;
//    }
//
//    @Override
//    public int getArmorDisplay(PlayerEntity player, @Nonnull ItemStack armor, int slot) {
//        int armorUpgrades = Math.min(6, UpgradableItemUtils.getUpgrades(EnumUpgrade.ARMOR, armor));
//        return Math.min(armorUpgrades, 2);
//    }
//
//    @Override
//    public void damageArmor(LivingEntity entity, @Nonnull ItemStack stack, DamageSource source, int damage, int slot) {
//        if (source.isExplosion()) {
//            // compressed iron is very explosion-resistant
//            return;
//        }
//        ItemStack copy = stack.copy();
//        stack.damageItem(damage, entity);
//        if (stack.isEmpty() && entity instanceof PlayerEntity) {
//            // armor has been destroyed; return the upgrades to the player, at least
//            ItemStack[] upgrades = UpgradableItemUtils.getUpgradeStacks(copy);
//            for (ItemStack upgrade : upgrades) {
//                ItemHandlerHelper.giveItemToPlayer((PlayerEntity) entity, upgrade);
//            }
//        }
//    }

    /* ----------- Pneumatic Helmet helpers ---------- */

    public static int getIntData(ItemStack stack, String key, int def) {
        if (stack.getItem() instanceof ItemPneumaticArmor && stack.hasTag() && stack.getTag().contains(key, Constants.NBT.TAG_INT)) {
            return stack.getTag().getInt(key);
        } else {
            return def;
        }
    }

    public static boolean getBooleanData(ItemStack stack, String key, boolean def) {
        if (stack.getItem() instanceof ItemPneumaticArmor && stack.hasTag() && stack.getTag().contains(key, Constants.NBT.TAG_BYTE)) {
            return stack.getTag().getByte(key) == 1;
        } else {
            return def;
        }
    }

    public static Item getSearchedItem(ItemStack helmetStack) {
        if (helmetStack.isEmpty() || !NBTUtil.hasTag(helmetStack, NBT_SEARCH_ITEM)) return null;
        String itemName = NBTUtil.getString(helmetStack, NBT_SEARCH_ITEM);
        return itemName == null || itemName.isEmpty() ? null : ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
    }

    public static void setSearchedItem(ItemStack helmetStack, Item searchedItem) {
        NBTUtil.setString(helmetStack, NBT_SEARCH_ITEM, searchedItem.getRegistryName().toString());
    }

    public static BlockPos getCoordTrackerPos(ItemStack helmetStack, World world) {
        if (helmetStack.isEmpty() || !NBTUtil.hasTag(helmetStack, NBT_COORD_TRACKER)) return null;
        CompoundNBT tag = NBTUtil.getCompoundTag(helmetStack, NBT_COORD_TRACKER);
        GlobalPos gPos = PneumaticCraftUtils.deserializeGlobalPos(tag);
        if (gPos.getPos().getY() < 0 || !world.getDimension().getType().equals(gPos.getDimension())) {
            return null;
        }
        return gPos.getPos();
    }

    public static void setCoordTrackerPos(ItemStack helmetStack, GlobalPos gPos) {
        NBTUtil.setCompoundTag(helmetStack, ItemPneumaticArmor.NBT_COORD_TRACKER, PneumaticCraftUtils.serializeGlobalPos(gPos));
    }

    public static String getEntityFilter(ItemStack helmetStack) {
        if (helmetStack.isEmpty() || !NBTUtil.hasTag(helmetStack, NBT_ENTITY_FILTER)) return "";
        return NBTUtil.getString(helmetStack, NBT_ENTITY_FILTER);
    }

    @Override
    public float getFOVModifier(ItemStack stack, PlayerEntity player, EquipmentSlotType slot) {
        if (slot == EquipmentSlotType.LEGS && Config.Client.leggingsFOVFactor > 0) {
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer();
            double boost = handler.getSpeedBoostFromLegs();
            if (boost > 0) {
                return 1.0f + (float) (boost * 2.0 * Config.Client.leggingsFOVFactor);
            }
        }
        return 1.0f;
    }

    @Override
    public INamedContainerProvider getContainerProvider(TileEntityChargingStation te) {
        return new IChargeableContainerProvider.Provider(te, ModContainerTypes.CHARGING_ARMOR);
    }

    /*------- Thaumcraft -------- */

    private boolean hasThaumcraftUpgradeAndPressure(ItemStack stack) {
        return getPressure(stack) > 0F && UpgradableItemUtils.getUpgrades(EnumUpgrade.THAUMCRAFT, stack) > 0;
    }

//    @Override
//    @Optional.Method(modid = ModIds.THAUMCRAFT)
//    public int getVisDiscount(ItemStack stack, PlayerEntity player) {
//        return hasThaumcraftUpgradeAndPressure(stack) ? VIS_DISCOUNTS[armorType.getIndex()] : 0;
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.THAUMCRAFT)
//    public boolean showIngamePopups(ItemStack itemstack, LivingEntity player) {
//        return armorType == EquipmentSlotType.HEAD && hasThaumcraftUpgradeAndPressure(itemstack);
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.THAUMCRAFT)
//    public boolean showNodes(ItemStack itemstack, LivingEntity player) {
//        return armorType == EquipmentSlotType.HEAD && hasThaumcraftUpgradeAndPressure(itemstack);
//    }

    private static class CompressedArmorMaterial implements IArmorMaterial {
        static final int[] DMG_REDUCTION = new int[]{2, 5, 6, 2};

        @Override
        public int getDurability(EquipmentSlotType equipmentSlotType) {
            return PneumaticValues.PNEUMATIC_ARMOR_DURABILITY_BASE;
        }

        @Override
        public int getDamageReductionAmount(EquipmentSlotType equipmentSlotType) {
            return DMG_REDUCTION[equipmentSlotType.getIndex()];
        }

        @Override
        public int getEnchantability() {
            return 9;
        }

        @Override
        public SoundEvent getSoundEvent() {
            return SoundEvents.ITEM_ARMOR_EQUIP_IRON;
        }

        @Override
        public Ingredient getRepairMaterial() {
            return Ingredient.EMPTY;
        }

        @Override
        public String getName() {
            return "compressed_iron";
        }

        @Override
        public float getToughness() {
            return 1.0f;
        }
    }
}
