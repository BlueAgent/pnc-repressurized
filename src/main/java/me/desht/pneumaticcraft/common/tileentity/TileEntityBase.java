package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.common.block.BlockPneumaticCraft;
import me.desht.pneumaticcraft.common.block.BlockPneumaticCraftCamo;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.network.*;
import me.desht.pneumaticcraft.common.thirdparty.IHeatDisperser;
import me.desht.pneumaticcraft.common.thirdparty.computercraft.LuaMethod;
import me.desht.pneumaticcraft.common.thirdparty.computercraft.LuaMethodRegistry;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.TileEntityCache;
import me.desht.pneumaticcraft.common.util.upgrade.ApplicableUpgradesDB;
import me.desht.pneumaticcraft.common.util.upgrade.IUpgradeHolder;
import me.desht.pneumaticcraft.common.util.upgrade.UpgradeCache;
import me.desht.pneumaticcraft.lib.NBTKeys;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorld;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

//@Optional.InterfaceList({
//        @Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = ModIds.COMPUTERCRAFT)
//})
public abstract class TileEntityBase extends TileEntity implements IGUIButtonSensitive, IDescSynced, IUpgradeAcceptor, IUpgradeHolder /*, IPeripheral*/ {
    private static final List<String> REDSTONE_LABELS = ImmutableList.of(
            "gui.tab.redstoneBehaviour.button.anySignal",
            "gui.tab.redstoneBehaviour.button.highSignal",
            "gui.tab.redstoneBehaviour.button.lowSignal"
    );

    private static final List<IHeatDisperser> moddedDispersers = new ArrayList<>();

    private final UpgradeCache upgradeCache = new UpgradeCache(this);

    @GuiSynced
    private final UpgradeHandler upgradeHandler;
    @GuiSynced
    int poweredRedstone; // The redstone strength currently applied to the block.

    boolean firstRun = true;  // True only the first time updateEntity invokes in a session
    private boolean descriptionPacketScheduled;
    private List<SyncedField> descriptionFields;
    private TileEntityCache[] tileCache;
    private boolean preserveStateOnBreak = false; // set to true if shift-wrenched to keep upgrades in the block
    private float actualSpeedMult = PneumaticValues.DEF_SPEED_UPGRADE_MULTIPLIER;
    private float actualUsageMult = PneumaticValues.DEF_SPEED_UPGRADE_USAGE_MULTIPLIER;
//    private LuaMethodRegistry luaMethodRegistry = null;

    public TileEntityBase(TileEntityType type) {
        this(type, 0);
    }

    public TileEntityBase(TileEntityType type, int upgradeSize) {
        super(type);

        this.upgradeHandler = new UpgradeHandler(upgradeSize);
    }

    @Override
    public String getUpgradeAcceptorTranslationKey() {
        return getBlockTranslationKey();
    }

    public String getBlockTranslationKey() {
        return "block.pneumaticcraft." + getType().getRegistryName().getPath();
    }

    /**
     * Call this from {@link INamedContainerProvider#getDisplayName()}
     * @return display name for this TE's GUI
     */
    ITextComponent getDisplayNameInternal() {
        return new TranslationTextComponent(getBlockTranslationKey());
    }

    // server side, chunk sending
    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT compound = super.getUpdateTag();
        return new PacketDescription(this).writeNBT(compound);
    }

    // client side, chunk sending
    @Override
    public void handleUpdateTag(CompoundNBT tag) {
        new PacketDescription(tag).process();
    }

    /***********
       We don't override getUpdatePacket() or onDataPacket() because TE sync'ing is all handled
       by our custom PacketDescription and the @DescSynced system
     ***********/

    @Override
    public BlockPos getPosition() {
        return getPos();
    }

    @Override
    public List<SyncedField> getDescriptionFields() {
        if (descriptionFields == null) {
            descriptionFields = NetworkUtils.getSyncedFields(this, DescSynced.class);
            for (SyncedField field : descriptionFields) {
                field.update();
            }
        }
        return descriptionFields;
    }

    public void sendDescriptionPacket() {
        sendDescriptionPacket(256);
    }

    void sendDescriptionPacket(double maxPacketDistance) {
        NetworkHandler.sendToAllAround(new PacketDescription(this), world, maxPacketDistance);
    }

    /**
     * A way to safely mark a block for an update from another thread (like the CC Lua thread).
     */
    void scheduleDescriptionPacket() {
        descriptionPacketScheduled = true;
    }

    /**
     * A way of dispersing heat to other mods which have their own heat API.
     *
     * @param disperser a heat disperser adapter object
     */
    public static void registerHeatDisperser(IHeatDisperser disperser) {
        moddedDispersers.add(disperser);
    }

    /*
     * Even though this class doesn't implement ITickableTileEntity, we'll keep the base update() logic here; classes
     * which extend non-tickable subclasses might need it (e.g. TileEntityPressureChamberInterface)
     */
    void tickImpl() {
        if (firstRun && !world.isRemote) {
            onFirstServerTick();
            onNeighborTileUpdate();
            onNeighborBlockUpdate();
        }
        firstRun = false;

        upgradeCache.validate();

        if (!world.isRemote) {
            getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY).ifPresent(logic -> {
                logic.tick();
                for (IHeatDisperser disperser : moddedDispersers) {
                    disperser.disperseHeat(this, tileCache);
                }
            });

            if (this instanceof IAutoFluidEjecting && getUpgrades(EnumUpgrade.DISPENSER) > 0) {
                ((IAutoFluidEjecting) this).autoExportFluid(this);
            }

            if (descriptionFields == null) descriptionPacketScheduled = true;
            for (SyncedField field : getDescriptionFields()) {
                if (field.update()) {
                    descriptionPacketScheduled = true;
                }
            }

            if (descriptionPacketScheduled) {
                descriptionPacketScheduled = false;
                sendDescriptionPacket();
            }
        }
    }

    @Override
    public void remove() {
        super.remove();

        if (getInventoryCap().isPresent()) getInventoryCap().invalidate();
        if (getHeatCap(null).isPresent()) getHeatCap(null).invalidate();
    }

    protected void onFirstServerTick() {
        initializeHullHeatExchangers();
    }

    protected void updateNeighbours() {
        world.notifyNeighborsOfStateChange(getPos(), getBlockState().getBlock());
    }

    public void onBlockRotated() {
        if (this instanceof ISideConfigurable) {
            for (SideConfigurator sc : ((ISideConfigurable) this).getSideConfigurators()) {
                sc.setupFacingMatrix();
            }
        }
    }

    void rerenderTileEntity() {
        world.notifyBlockUpdate(getPos(), getBlockState(), getBlockState(), 0);
    }

    protected boolean shouldRerenderChunkOnDescUpdate() {
        return this instanceof ICamouflageableTE;
    }

    /**
     * Encoded into the description packet. Also included in saved data written by {@link TileEntityBase#write(CompoundNBT)}
     *
     * Prefer to use @DescSynced where possible - use this either for complex fields not handled by @DescSynced,
     * or for non-ticking tile entities.
     *
     * @param tag NBT tag
     */
    @Override
    public void writeToPacket(CompoundNBT tag) {
        if (this instanceof ISideConfigurable) {
            tag.put(NBTKeys.NBT_SIDE_CONFIG, SideConfigurator.writeToNBT((ISideConfigurable) this));
        }
    }

    /**
     * Encoded into the description packet. Also included in saved data read by {@link TileEntityBase#read(CompoundNBT)}.
     *
     * Prefer to use @DescSynced where possible - use this either for complex fields not handled by @DescSynced,
     * or for non-ticking tile entities.
     *
     * @param tag NBT tag
     */
    @Override
    public void readFromPacket(CompoundNBT tag) {
        if (this instanceof ISideConfigurable) {
            SideConfigurator.readFromNBT(tag.getCompound(NBTKeys.NBT_SIDE_CONFIG), (ISideConfigurable) this);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);

        if (getUpgradeHandler().getSlots() > 0) {
            tag.put(NBTKeys.NBT_UPGRADE_INVENTORY, getUpgradeHandler().serializeNBT());
        }

        getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY)
                .ifPresent(logic -> tag.put(NBTKeys.NBT_HEAT_EXCHANGER, logic.serializeNBT()));

        if (this instanceof ISerializableTanks) {
            tag.put(NBTKeys.NBT_SAVED_TANKS, ((ISerializableTanks) this).serializeTanks());
        }
        writeToPacket(tag);

        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);

        if (tag.contains(NBTKeys.NBT_UPGRADE_INVENTORY) && getUpgradeHandler() != null) {
            getUpgradeHandler().deserializeNBT(tag.getCompound(NBTKeys.NBT_UPGRADE_INVENTORY));
        }

        getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY)
                .ifPresent(logic -> logic.deserializeNBT(tag.getCompound(NBTKeys.NBT_HEAT_EXCHANGER)));

        if (this instanceof ISerializableTanks) {
            ((ISerializableTanks) this).deserializeTanks(tag.getCompound(NBTKeys.NBT_SAVED_TANKS));
        }
        readFromPacket(tag);
    }

    @Override
    public void validate() {
        super.validate();
        scheduleDescriptionPacket();
    }

    @Override
    public void onDescUpdate() {
        if (shouldRerenderChunkOnDescUpdate()) {
            rerenderTileEntity();
            if (this instanceof ICamouflageableTE) requestModelDataUpdate();
        }
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        if (this instanceof ICamouflageableTE) {
            return new ModelDataMap.Builder()
                    .withInitial(BlockPneumaticCraftCamo.BLOCK_ACCESS, world)
                    .withInitial(BlockPneumaticCraftCamo.BLOCK_POS, pos)
                    .withInitial(BlockPneumaticCraftCamo.CAMO_STATE, ((ICamouflageableTE) this).getCamouflage())
                    .build();
        } else {
            return super.getModelData();
        }
    }

    /**
     * Called when a key is synced in the container.
     */
    public void onGuiUpdate() {
    }

    public Direction getRotation() {
        BlockState state = getBlockState();
        return state.getBlock() instanceof BlockPneumaticCraft ? ((BlockPneumaticCraft) state.getBlock()).getRotation(state) : Direction.NORTH;
    }

    @Override
    public void updateContainingBlockInfo() {
        super.updateContainingBlockInfo();
    }

    public int getUpgrades(Item upgrade) {
        int upgrades = 0;
        for (int i = 0; i < getUpgradeHandler().getSlots(); i++) {
            ItemStack stack = getUpgradeHandler().getStackInSlot(i);
            if (stack.getItem() == upgrade) {
                upgrades += stack.getCount();
            }
        }
        return upgrades;
    }

    public int getUpgrades(EnumUpgrade upgrade) {
        return upgradeCache.getUpgrades(upgrade);
    }

    public float getSpeedMultiplierFromUpgrades() {
        return actualSpeedMult;
    }

    public float getSpeedUsageMultiplierFromUpgrades() {
        return actualUsageMult;
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, PlayerEntity player) {
    }

    public boolean isGuiUseableByPlayer(PlayerEntity player) {
        return getWorld().getTileEntity(getPos()) == this && player.getDistanceSq(getPos().getX() + 0.5D, getPos().getY() + 0.5D, getPos().getZ() + 0.5D) <= 64.0D;
    }

    public void onNeighborTileUpdate() {
        for (TileEntityCache cache : getTileCache()) {
            cache.update();
        }
    }

    public TileEntityCache[] getTileCache() {
        if (tileCache == null) tileCache = TileEntityCache.getDefaultCache(getWorld(), getPos());
        return tileCache;
    }

    public TileEntity getCachedNeighbor(Direction dir) {
        return getTileCache()[dir.getIndex()].getTileEntity();
    }

    public void onNeighborBlockUpdate() {
        poweredRedstone = PneumaticCraftUtils.getRedstoneLevel(getWorld(), getPos());
        if (getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY).isPresent()) {
            initializeHullHeatExchangers();
        }
        for (TileEntityCache cache : getTileCache()) {
            cache.update();
        }
    }

    public boolean redstoneAllows() {
        switch (((IRedstoneControl) this).getRedstoneMode()) {
            case 0:
                return true;
            case 1:
                return poweredRedstone > 0;
            case 2:
                return poweredRedstone == 0;
        }
        return false;
    }

    private void initializeHullHeatExchangers() {
        Map<IHeatExchangerLogic, List<Direction>> map = new IdentityHashMap<>();
        for (Direction side : Direction.VALUES) {
            getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY, side)
                    .ifPresent(logic -> map.computeIfAbsent(logic, k -> new ArrayList<>()).add(side));
        }
        map.forEach((logic, sides) ->
                logic.initializeAsHull(getWorld(), getPos(), heatExchangerBlockFilter(), sides.toArray(new Direction[0])));
    }

    /**
     * Should this (heat-using) machine lose heat to the surrounding air blocks? Most blocks do.
     * @return true if heat will be lost to the air on exposed faces, false otherwise
     */
    protected BiPredicate<IWorld,BlockPos> heatExchangerBlockFilter() {
        return IHeatExchangerLogic.ALL_BLOCKS;
    }

    @Override
    public Type getSyncType() {
        return Type.TILE_ENTITY;
    }

    /**
     * Take a fluid-containing from the input slot, use it to fill the primary input tank of the tile entity,
     * and place the resulting emptied container in the output slot.
     *
     * @param inputSlot input slot
     * @param outputSlot output slot
     */
    void processFluidItem(int inputSlot, int outputSlot) {
        getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(itemHandler -> {
            ItemStack inputStack = itemHandler.getStackInSlot(inputSlot);
            if (inputStack.getCount() > 1) return;

            FluidUtil.getFluidHandler(inputStack).ifPresent(fluidHandlerItem -> {
                FluidStack itemContents = fluidHandlerItem.drain(1000, IFluidHandler.FluidAction.SIMULATE);

                getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).ifPresent(fluidHandler -> {
                    if (!itemContents.isEmpty()) {
                        // input item contains fluid: drain from input item into tank, move to output if empty
                        FluidStack transferred = FluidUtil.tryFluidTransfer(fluidHandler, fluidHandlerItem, itemContents.getAmount(), true);
                        if (transferred.getAmount() == itemContents.getAmount()) {
                            // all transferred; move empty container to output if possible
                            ItemStack emptyContainerStack = fluidHandlerItem.getContainer();
                            ItemStack excess = itemHandler.insertItem(outputSlot, emptyContainerStack, true);
                            if (excess.isEmpty()) {
                                itemHandler.extractItem(inputSlot, 1, false);
                                itemHandler.insertItem(outputSlot, emptyContainerStack, false);
                            }
                        }
                    } else if (itemHandler.getStackInSlot(outputSlot).isEmpty()) {
                        // input item(s) is/are empty: drain from tank to one input item, move to output
                        FluidStack transferred = FluidUtil.tryFluidTransfer(fluidHandlerItem, fluidHandler, Integer.MAX_VALUE, true);
                        if (!transferred.isEmpty()) {
                            itemHandler.extractItem(inputSlot, 1, false);
                            ItemStack filledContainerStack = fluidHandlerItem.getContainer();
                            itemHandler.insertItem(outputSlot, filledContainerStack, false);
                        }
                    }
                });
            });
        });
    }

    @Override
    public Map<EnumUpgrade, Integer> getApplicableUpgrades() {
        return ApplicableUpgradesDB.getInstance().getApplicableUpgrades(this);
    }

    protected void addLuaMethods(LuaMethodRegistry registry) {
        if (getHeatCap(null).isPresent()) {
            registry.registerLuaMethod(new LuaMethod("getTemperature") {
                @Override
                public Object[] call(Object[] args) {
                    requireArgs(args, 0, 1, "face? (down/up/north/south/west/east)");
                    Direction dir = args.length == 0 ? null : getDirForString((String) args[0]);
                    return new Object[]{
                            getHeatCap(dir).map(IHeatExchangerLogic::getTemperature).orElseThrow(RuntimeException::new)
                    };
                }
            });
        }
    }

    // todo 1.14 computercraft
//    private LuaMethodRegistry getLuaMethodRegistry() {
//        if (luaMethodRegistry == null) {
//            luaMethodRegistry = new LuaMethodRegistry();
//            addLuaMethods(luaMethodRegistry);
//        }
//        return luaMethodRegistry;
//    }
//
//    @Override
//    public String getType() {
//        return getBlockType().getTranslationKey().substring(5);
//    }
//
//    @Override
//    public String[] getMethodNames() {
//        return getLuaMethodRegistry().getMethodNames();
//    }
//
//    public Object[] callLuaMethod(String methodName, Object... args) throws Exception {
//        return getLuaMethodRegistry().getMethod(methodName).call(args);
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.COMPUTERCRAFT)
//    public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException {
//        try {
//            return getLuaMethodRegistry().getMethod(method).call(arguments);
//        } catch (Exception e) {
//            throw new LuaException(e.getMessage());
//        }
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.COMPUTERCRAFT)
//    public void attach(IComputerAccess computer) {
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.COMPUTERCRAFT)
//    public void detach(IComputerAccess computer) {
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.COMPUTERCRAFT)
//    public boolean equals(IPeripheral other) {
//        if (other == null) {
//            return false;
//        }
//        if (this == other) {
//            return true;
//        }
//        if (other instanceof TileEntity) {
//            TileEntity otherTE = (TileEntity) other;
//            return otherTE.getWorld().equals(getWorld()) && otherTE.getPos().equals(getPos());
//        }
//
//        return false;
//    }

    public abstract IItemHandler getPrimaryInventory();

    @Override
    public UpgradeHandler getUpgradeHandler() {
        return upgradeHandler;
    }

    @Nonnull
    protected LazyOptional<IItemHandler> getInventoryCap() {
        // for internal use only!
        return LazyOptional.empty();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, getInventoryCap());
        } else if (cap == PNCCapabilities.HEAT_EXCHANGER_CAPABILITY) {
            return PNCCapabilities.HEAT_EXCHANGER_CAPABILITY.orEmpty(cap, getHeatCap(side));
//        } else if (cap == Mekanism.CAPABILITY_HEAT_TRANSFER && this instanceof IHeatExchanger) {
//            return Mekanism.getHeatAdapter(this, side).cast();
        }
        return super.getCapability(cap, side);
    }

    public LazyOptional<IHeatExchangerLogic> getHeatCap(Direction side) {
        return LazyOptional.empty();
    }

    /**
     * Collect all items which should be dropped when this TE is broken.  Override and extend this in subclassing
     * TE's which have extra inventories to be dropped.
     *
     * @param drops list in which to collect dropped items
     */
    public void getContentsToDrop(NonNullList<ItemStack> drops) {
        PneumaticCraftUtils.collectNonEmptyItems(getPrimaryInventory(), drops);

        if (!shouldPreserveStateOnBreak()) {
            UpgradeHandler uh = getUpgradeHandler();
            for (int i = 0; i < uh.getSlots(); i++) {
                if (!uh.getStackInSlot(i).isEmpty()) {
                    drops.add(uh.getStackInSlot(i));
                }
            }
        }

        if (this instanceof ICamouflageableTE) {
            BlockState camoState = ((ICamouflageableTE) this).getCamouflage();
            if (camoState != null) {
                drops.add(ICamouflageableTE.getStackForState(camoState));
            }
        }
    }

    public final String getRedstoneButtonText(int mode) {
        try {
            return getRedstoneButtonLabels().get(mode);
        } catch (ArrayIndexOutOfBoundsException e) {
            return "<ERROR>";
        }
    }

    protected List<String> getRedstoneButtonLabels() {
        return REDSTONE_LABELS;
    }

    public int getRedstoneModeCount() {
        return getRedstoneButtonLabels().size();
    }

    public String getRedstoneTabTitle() {
        return this instanceof IRedstoneControlled ? "gui.tab.redstoneBehaviour.enableOn" : "gui.tab.redstoneBehaviour.emitRedstoneWhen";
    }

    /**
     * Should this tile entity preserve its state (currently: upgrades and stored air) when broken?
     * By default this is true when sneak-wrenched, and false when broken by pick.
     *
     * @return true if state should be preserved, false otherwise
     */
    public boolean shouldPreserveStateOnBreak() {
        return preserveStateOnBreak;
    }

    public void setPreserveStateOnBreak(boolean preserveStateOnBreak) {
        this.preserveStateOnBreak = preserveStateOnBreak;
    }

    /**
     * Called when a machine's upgrades have changed in any way.  This is also called from readNBT() when saved upgrades
     * are deserialized, so it is not guaranteed that the world field is non-null - beware.  If you override this,
     * remember to call the super method!
     */
    @Override
    public void onUpgradesChanged() {
        actualSpeedMult = (float) Math.pow(PNCConfig.Common.Machines.speedUpgradeSpeedMultiplier, Math.min(10, getUpgrades(EnumUpgrade.SPEED)));
        actualUsageMult = (float) Math.pow(PNCConfig.Common.Machines.speedUpgradeUsageMultiplier, Math.min(10, getUpgrades(EnumUpgrade.SPEED)));
    }

    public UpgradeCache getUpgradeCache() {
        return upgradeCache;
    }

    /**
     * Get any extra data to be serialized onto a dropped item stack. The supplied tag is the "BlockEntityTag" subtag of
     * the item's NBT data, so will be automatically deserialized into the TE when the itemblock is next placed.
     *
     * @param blockEntityTag the existing "BlockEntityTag" subtag to add data to
     */
    public void serializeExtraItemData(CompoundNBT blockEntityTag) {
    }

    public class UpgradeHandler extends BaseItemStackHandler {
        UpgradeHandler(int upgradeSize) {
            super(TileEntityBase.this, upgradeSize);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
            return itemStack.isEmpty() || isApplicable(itemStack) && isUnique(slot, itemStack);
        }

        @Override
        protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
            EnumUpgrade upgrade = EnumUpgrade.from(stack);
            if (upgrade == null) return 0;
            return ApplicableUpgradesDB.getInstance().getMaxUpgrades(te, upgrade);
        }

        private boolean isUnique(int slot, ItemStack stack) {
            for (int i = 0; i < getSlots(); i++) {
                if (i != slot && EnumUpgrade.from(stack) == EnumUpgrade.from(getStackInSlot(i))) return false;
            }
            return true;
        }

        private boolean isApplicable(ItemStack stack) {
            EnumUpgrade upgrade = EnumUpgrade.from(stack);
            return ApplicableUpgradesDB.getInstance().getMaxUpgrades(TileEntityBase.this, upgrade) > 0;
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);

            upgradeCache.invalidate();
        }
    }
}
