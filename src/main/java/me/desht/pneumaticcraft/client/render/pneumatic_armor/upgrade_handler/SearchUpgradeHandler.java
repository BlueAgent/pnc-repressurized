package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiSearchUpgradeOptions;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderSearchItemBlock;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.config.subconfig.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchUpgradeHandler implements IUpgradeRenderHandler {
    private int totalSearchedItemCount;
    private int itemSearchCount;
    private int ticksExisted;
    private final Map<ItemEntity, Integer> searchedItems = new HashMap<>();
    private final Map<BlockPos, RenderSearchItemBlock> trackedInventories = new HashMap<>();
    @OnlyIn(Dist.CLIENT)
    private WidgetAnimatedStat searchInfo;
    private ItemStack searchedStack = ItemStack.EMPTY;

    @Override
    @OnlyIn(Dist.CLIENT)
    public String getUpgradeID() {
        return "itemSearcher";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void tick(PlayerEntity player, int rangeUpgrades) {
        ticksExisted++;

        if ((ticksExisted & 0xf) == 0) {
            // count up all items in tracked inventories, and cull any inventories with no matching items
            int blockSearchCount = trackInventoryCounts(rangeUpgrades);

            searchedItems.entrySet().removeIf(e -> !e.getKey().isAlive());

            totalSearchedItemCount = itemSearchCount + blockSearchCount;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render3D(MatrixStack matrixStack, IRenderTypeBuffer buffer, float partialTicks) {
        IVertexBuilder builder = buffer.getBuffer(ModRenderTypes.getTextureRenderColored(Textures.GLOW_RESOURCE, true));

        searchedItems.forEach((item, value) -> {
            float height = MathHelper.sin((item.getAge() + partialTicks) / 10.0F + item.hoverStart) * 0.1F + 0.2F;
            RenderSearchItemBlock.renderSearch(matrixStack, builder,
                    item.lastTickPosX + (item.getPosX() - item.lastTickPosX) * partialTicks,
                    item.lastTickPosY + (item.getPosY() - item.lastTickPosY) * partialTicks + height,
                    item.lastTickPosZ + (item.getPosZ() - item.lastTickPosZ) * partialTicks, value,
                    totalSearchedItemCount, partialTicks
            );
        });

        trackedInventories.values().forEach(entry -> entry.renderSearchBlock(matrixStack, builder, totalSearchedItemCount, partialTicks));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render2D(MatrixStack matrixStack, float partialTicks, boolean helmetEnabled) {
        Item item = ItemPneumaticArmor.getSearchedItem(ClientUtils.getWornArmor(EquipmentSlotType.HEAD));
        List<String> textList = new ArrayList<>();
        if (item == null) {
            textList.add("press '" + I18n.format(KeyHandler.getInstance().keybindOpenOptions.getTranslationKey()) + "' to configure");
        } else {
            if (searchedStack.getItem() != item) searchedStack = new ItemStack(item);
            textList.add(searchedStack.getDisplayName().getString() + " (" + totalSearchedItemCount + " found)");
        }
        searchInfo.setText(textList);
    }

    @Override
    public EnumUpgrade[] getRequiredUpgrades() {
        return new EnumUpgrade[]{ EnumUpgrade.SEARCH };
    }

    private int trackInventoryCounts(int rangeUpgrades) {
        int blockSearchCount = 0;
        int blockTrackRange = BlockTrackUpgradeHandler.BLOCK_TRACKING_RANGE
                + Math.min(rangeUpgrades, 5) * PneumaticValues.RANGE_UPGRADE_HELMET_RANGE_INCREASE;
        int blockTrackRangeSq = blockTrackRange * blockTrackRange;

        PlayerEntity player = ClientUtils.getClientPlayer();
        List<BlockPos> toRemove = new ArrayList<>();
        for (Map.Entry<BlockPos,RenderSearchItemBlock> entry : trackedInventories.entrySet()) {
            int nItems = entry.getKey().distanceSq(player.getPosition()) < blockTrackRangeSq ?
                    entry.getValue().getSearchedItemCount() : 0;

            if (nItems == 0) {
                toRemove.add(entry.getKey());
            }
            blockSearchCount += nItems;
        }
        toRemove.forEach(trackedInventories::remove);

        return blockSearchCount;
    }

    /**
     * Called by the EntityTrackerUpgradeHandler every 16 ticks to find items in item entities on the ground.
     * @param player the player
     * @param rangeUpgrades number of range upgrades installed in the helmet
     * @param handlerEnabled true if the search handler is actually enabled, false otherwise
     */
    void trackItemEntities(PlayerEntity player, int rangeUpgrades, boolean handlerEnabled) {
        searchedItems.clear();
        itemSearchCount = 0;

        if (!handlerEnabled) return;

        Item searchedItem = ItemPneumaticArmor.getSearchedItem(ClientUtils.getWornArmor(EquipmentSlotType.HEAD));
        List<ItemEntity> items = player.world.getEntitiesWithinAABB(ItemEntity.class, EntityTrackUpgradeHandler.getAABBFromRange(player, rangeUpgrades));

        for (ItemEntity itemEntity : items) {
            if (!itemEntity.getItem().isEmpty() && searchedItem != null) {
                if (itemEntity.getItem().getItem() == searchedItem) {
                    searchedItems.put(itemEntity, itemEntity.getItem().getCount());
                    itemSearchCount += itemEntity.getItem().getCount();
                } else {
                    List<ItemStack> inventoryItems = PneumaticCraftUtils.getStacksInItem(itemEntity.getItem());
                    int itemCount = 0;
                    for (ItemStack inventoryItem : inventoryItems) {
                        if (inventoryItem.getItem() == searchedItem) {
                            itemCount += inventoryItem.getCount();
                        }
                    }
                    if (itemCount > 0) {
                        searchedItems.put(itemEntity, itemCount);
                        itemSearchCount += itemCount;
                    }
                }
            }
        }
    }

    /**
     * Called by the BlockTrackUpgradeHandler when it finds inventories while scanning blocks.  If
     * the inventory contains any of the searched item, its position is added to a track list.
     *
     * @param te TileEntity the tile entity, which is already known to support the item handler capability
     * @param handlerEnabled true if the search handler is actually enabled, false otherwise
     */
    void checkInventoryForItems(TileEntity te, Direction face, boolean handlerEnabled) {
        if (!handlerEnabled) {
            trackedInventories.clear();
        } else {
            Item searchedItem = ItemPneumaticArmor.getSearchedItem(ClientUtils.getWornArmor(EquipmentSlotType.HEAD));
            if (searchedItem != null) {
                te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face).ifPresent(handler -> {
                    if (checkForItems(handler, searchedItem)) {
                        trackedInventories.put(te.getPos(), new RenderSearchItemBlock(te.getWorld(), te.getPos()));
                    }
                });
            }
        }
    }

    private boolean checkForItems(IItemHandler handler, Item item) {
        for (int l = 0; l < handler.getSlots(); l++) {
            if (!handler.getStackInSlot(l).isEmpty()) {
                int items = RenderSearchItemBlock.getSearchedItemCount(handler.getStackInSlot(l), item);
                if (items > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void reset() {
        trackedInventories.clear();
        searchedItems.clear();
        ticksExisted = 0;
        searchInfo = null;
    }

    @Override
    public float getEnergyUsage(int rangeUpgrades, PlayerEntity player) {
        return PneumaticValues.USAGE_ITEM_SEARCHER;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public IOptionPage getGuiOptionsPage(IGuiScreen screen) {
        return new GuiSearchUpgradeOptions(screen,this);
    }

    @Override
    public EquipmentSlotType getEquipmentSlot() {
        return EquipmentSlotType.HEAD;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public WidgetAnimatedStat getAnimatedStat() {
        if (searchInfo == null) {
            WidgetAnimatedStat.StatIcon icon = WidgetAnimatedStat.StatIcon.of(EnumUpgrade.SEARCH.getItemStack());
            searchInfo = new WidgetAnimatedStat(null, new StringTextComponent("Currently searching for:"), icon,
                    0x3000AA00, null, ArmorHUDLayout.INSTANCE.itemSearchStat);
            searchInfo.setMinDimensionsAndReset(0, 0);
        }
        return searchInfo;
    }

    @Override
    public void onResolutionChanged() {
        searchInfo = null;
    }
}
