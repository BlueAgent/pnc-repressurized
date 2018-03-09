package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.Maps;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.common.block.BlockPneumaticCraft;
import me.desht.pneumaticcraft.common.inventory.SyncedField;
import me.desht.pneumaticcraft.common.item.ItemMachineUpgrade;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.network.*;
import me.desht.pneumaticcraft.common.thirdparty.computercraft.ILuaMethod;
import me.desht.pneumaticcraft.common.thirdparty.computercraft.LuaMethod;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.TileEntityCache;
import me.desht.pneumaticcraft.lib.ModIds;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;
import java.util.*;

@Optional.InterfaceList({@Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = ModIds.COMPUTERCRAFT)})
public class TileEntityBase extends TileEntity implements IGUIButtonSensitive, IDescSynced, IUpgradeAcceptor, IPeripheral {
    private final Set<Item> applicableUpgrades = new HashSet<>();
    private final Set<String> applicableCustomUpgrades = new HashSet<>();
    private final UpgradeCache upgradeCache = new UpgradeCache(this);
    @DescSynced
    protected UpgradeHandler upgradeHandler;
    protected boolean firstRun = true;  // True only the first time updateEntity invokes in a session
    protected int poweredRedstone; // The redstone strength currently applied to the block.
    protected List<ILuaMethod> luaMethods = new ArrayList<>();
    private boolean descriptionPacketScheduled;
    private List<SyncedField> descriptionFields;
    private TileEntityCache[] tileCache;
    private IBlockState cachedBlockState;

    public TileEntityBase() {
        this(0);
    }

    public TileEntityBase(int upgradeSize) {
        addLuaMethods();
        this.upgradeHandler = new UpgradeHandler(upgradeSize);
    }

    public static int getUpgrades(IItemHandler inv, IItemRegistry.EnumUpgrade upgrade) {
        int upgrades = 0;
        Item upgradeItem = Itemss.upgrades.get(upgrade);
        for (int i = 0; i < inv.getSlots(); i++) {
            if (inv.getStackInSlot(i).getItem() == upgradeItem) {
                upgrades += inv.getStackInSlot(i).getCount();
            }
        }
        return upgrades;
    }

    private static String makeUpgradeKey(ItemStack stack) {
        return stack.getItem().getRegistryName() + ":" + stack.getMetadata();
    }

    protected void addApplicableUpgrade(IItemRegistry.EnumUpgrade... upgrades) {
        for (IItemRegistry.EnumUpgrade upgrade : upgrades)
            addApplicableUpgrade(Itemss.upgrades.get(upgrade));
    }

    protected void addApplicableUpgrade(Item upgrade) {
        applicableUpgrades.add(upgrade);
    }

    protected void addApplicableCustomUpgrade(ItemStack... upgrades) {
        for (ItemStack upgrade : upgrades) {
            applicableCustomUpgrades.add(makeUpgradeKey(upgrade));
        }
    }

    // server side, chunk sending
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound compound = super.getUpdateTag();
        return new PacketDescription(this).writeNBT(compound);
    }

    // client side, chunk sending
    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        PacketDescription packet = new PacketDescription(tag);
        packet.handleClientSide(packet, PneumaticCraftRepressurized.proxy.getPlayer());
    }

    // server side, TE resync (notifyBlockUpdate)
    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, getBlockMetadata(), getUpdateTag());
    }

    // client side, TE resync
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        PacketDescription packet = new PacketDescription(pkt.getNbtCompound());
        packet.handleClientSide(packet, PneumaticCraftRepressurized.proxy.getPlayer());
    }

    @Override
    public BlockPos getPosition() {
        return getPos();
    }

    protected double getPacketDistance() {
        return 64;
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
        IBlockState state = world.getBlockState(getPos());
        world.notifyBlockUpdate(getPos(), state, state, 3);
    }

    /**
     * A way to safely mark a block for an update from another thread (like the CC Lua thread).
     */
    protected void scheduleDescriptionPacket() {
        descriptionPacketScheduled = true;
    }

    public void sendDescPacket(double maxPacketDistance) {
        NetworkHandler.sendToAllAround(new PacketDescription(this), new NetworkRegistry.TargetPoint(world.provider.getDimension(), getPos().getX(), getPos().getY(), getPos().getZ(), maxPacketDistance));
    }

    /*
     * Even though this class doesn't implement ITickable, we'll keep the base update() logic here; classes
     * which extend non-tickable subclasses might need it (e.g. TileEntityPressureChamberInterface)
     */
    public void updateImpl() {
        if (firstRun && !world.isRemote) {
            //firstRun = false;
            onFirstServerUpdate();
            onNeighborTileUpdate();
            onNeighborBlockUpdate();
        }
        firstRun = false;

        upgradeCache.validate();
        if (!world.isRemote) {

            if (this instanceof IHeatExchanger) {
                ((IHeatExchanger) this).getHeatExchangerLogic(null).update();
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

    protected void onFirstServerUpdate() {
        initializeIfHeatExchanger();
    }

    protected void updateNeighbours() {
        world.notifyNeighborsOfStateChange(getPos(), getBlockType(), true);
    }

    public void onBlockRotated() {
    }

    public void rerenderTileEntity() {
        world.markBlockRangeForRenderUpdate(getPos(), getPos());
    }

    protected boolean shouldRerenderChunkOnDescUpdate() {
        return false;
    }

    /**
     * Encoded into the description packet. Also is included in the world save.
     * Used as last resort, using @DescSynced is preferred.
     *
     * @param tag
     */
    @Override
    public void writeToPacket(NBTTagCompound tag) {
    }

    /**
     * Encoded into the description packet. Also is included in the world save.
     * Used as last resort, using @DescSynced is preferred.
     *
     * @param tag
     */
    @Override
    public void readFromPacket(NBTTagCompound tag) {
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        if (upgradeHandler != null) {
            tag.setTag("Upgrades", upgradeHandler.serializeNBT());
        }
        writeToPacket(tag);
        if (this instanceof IHeatExchanger) {
            ((IHeatExchanger) this).getHeatExchangerLogic(null).writeToNBT(tag);
        }
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (tag.hasKey("Upgrades") && upgradeHandler != null) {
            upgradeHandler = new UpgradeHandler(upgradeHandler.getSlots());
            upgradeHandler.deserializeNBT(tag.getCompoundTag("Upgrades"));
            upgradeCache.validate();
        }
        readFromPacket(tag);
        if (this instanceof IHeatExchanger) {
            ((IHeatExchanger) this).getHeatExchangerLogic(null).readFromNBT(tag);
        }
    }

    @Override
    public void validate() {
        super.validate();
        scheduleDescriptionPacket();
    }

    @Override
    public void onDescUpdate() {
        if (shouldRerenderChunkOnDescUpdate()) rerenderTileEntity();
    }

    /**
     * Called when a key is synced in the container.
     */
    public void onGuiUpdate() {
    }

    public EnumFacing getRotation() {
        if (cachedBlockState == null) {
            cachedBlockState = world.getBlockState(getPos());
        }
        return cachedBlockState.getValue(BlockPneumaticCraft.ROTATION);
    }

    @Override
    public void updateContainingBlockInfo() {
        cachedBlockState = null;
        super.updateContainingBlockInfo();
    }

    public int getUpgrades(Item upgrade) {
        int upgrades = 0;
        for (int i = 0; i < upgradeHandler.getSlots(); i++) {
            ItemStack stack = upgradeHandler.getStackInSlot(i);
            if (stack.getItem() == upgrade) {
                upgrades += stack.getCount();
            }
        }
        return upgrades;
    }

    public int getUpgrades(IItemRegistry.EnumUpgrade upgrade) {
        return upgradeCache.getUpgrades(upgrade);
    }

    public int getCustomUpgrades(ItemStack upgradeStack) {
        return upgradeCache.getUpgrades(upgradeStack);
    }

    public float getSpeedMultiplierFromUpgrades() {
        return (float) Math.pow(PneumaticValues.SPEED_UPGRADE_MULTIPLIER, Math.min(10, getUpgrades(IItemRegistry.EnumUpgrade.SPEED)));
    }

    public float getSpeedUsageMultiplierFromUpgrades() {
        return (float) Math.pow(PneumaticValues.SPEED_UPGRADE_USAGE_MULTIPLIER, Math.min(10, getUpgrades(IItemRegistry.EnumUpgrade.SPEED)));
    }

    @Override
    public void handleGUIButtonPress(int guiID, EntityPlayer player) {
    }

    public boolean isGuiUseableByPlayer(EntityPlayer player) {
        return getWorld().getTileEntity(getPos()) == this && player.getDistanceSq(getPos().getX() + 0.5D, getPos().getY() + 0.5D, getPos().getZ() + 0.5D) <= 64.0D;
    }

    public void onNeighborTileUpdate() {
        initializeIfHeatExchanger();
        for (TileEntityCache cache : getTileCache()) {
            cache.update();
        }
    }

    public TileEntityCache[] getTileCache() {
        if (tileCache == null) tileCache = TileEntityCache.getDefaultCache(getWorld(), getPos());
        return tileCache;
    }

    public void onNeighborBlockUpdate() {
        poweredRedstone = PneumaticCraftUtils.getRedstoneLevel(getWorld(), getPos());
        initializeIfHeatExchanger();
    }

    public boolean redstoneAllows() {
        if (getWorld().isRemote) onNeighborBlockUpdate();
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

    protected void initializeIfHeatExchanger() {
        if (this instanceof IHeatExchanger) {
            initializeHeatExchanger(((IHeatExchanger) this).getHeatExchangerLogic(null), getConnectedHeatExchangerSides());
        }
    }

    protected void initializeHeatExchanger(IHeatExchangerLogic heatExchanger, EnumFacing... connectedSides) {
        heatExchanger.initializeAsHull(getWorld(), getPos(), connectedSides);
    }

    /**
     * Gets the valid sides for heat exchanging to be allowed. returning an empty array will allow any side.
     *
     * @return
     */
    protected EnumFacing[] getConnectedHeatExchangerSides() {
        return new EnumFacing[0];
    }

    public void autoExportLiquid() {
        if (hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)) {
            IFluidHandler handler = getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
            FluidStack toDrain = handler.drain(1000, false);
            if (toDrain != null && toDrain.amount > 0) {
                for (EnumFacing d : EnumFacing.VALUES) {
                    TileEntity te = getTileCache()[d.ordinal()].getTileEntity();
                    if (te != null && te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, d.getOpposite())) {
                        IFluidHandler destHandler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, d.getOpposite());
                        FluidStack sent = FluidUtil.tryFluidTransfer(destHandler, handler, toDrain, true);
                        toDrain.amount -= sent == null ? 0 : sent.amount;
                        if (toDrain.amount <= 0) break;
                    }
                }
            }
        }
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
    protected void processFluidItem(int inputSlot, int outputSlot) {
        if (!hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)
                || !hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null))
            return;
        IItemHandler itemHandler = getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        ItemStack fluidContainer = itemHandler.getStackInSlot(inputSlot);
        IFluidHandlerItem fluidHandlerItem = FluidUtil.getFluidHandler(fluidContainer);
        if (fluidHandlerItem == null) {
            return;
        }
        if (fluidContainer.getCount() > 1) {
            FluidStack stack = fluidHandlerItem.drain(1, false);
            if (stack != null && stack.amount > 0) {
                // disallow multiple filled items (shouldn't normally happen anyway but let's be paranoid)
                return;
            } else {
                // multiple empty items OK, but be sure to only fill one of them...
                ItemStack itemToFill = fluidContainer.copy();
                itemToFill.setCount(1);
                fluidHandlerItem = FluidUtil.getFluidHandler(fluidContainer);
            }
        }

        IFluidHandler fluidHandler = getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);

        FluidStack itemContents = fluidHandlerItem.drain(1000, false);
        if (itemContents != null && itemContents.amount > 0) {
            // input item contains fluid: drain from input item into tank, move to output if empty
            FluidStack transferred = FluidUtil.tryFluidTransfer(fluidHandler, fluidHandlerItem, itemContents.amount, true);
            if (transferred != null && transferred.amount == itemContents.amount) {
                // all transferred; move empty container to output if possible
                ItemStack emptyContainerStack = fluidHandlerItem.getContainer();
                ItemStack excess = itemHandler.insertItem(outputSlot, emptyContainerStack, false);
                if (excess.isEmpty()) {
                    itemHandler.extractItem(inputSlot, 1, false);
                }
            }
        } else if (itemHandler.getStackInSlot(outputSlot).isEmpty()) {
            // input item(s) is/are empty: drain from tank to one input item, move to output
            FluidStack transferred = FluidUtil.tryFluidTransfer(fluidHandlerItem, fluidHandler, Integer.MAX_VALUE, true);
            if (transferred != null && transferred.amount > 0) {
                itemHandler.extractItem(inputSlot, 1, false);
                ItemStack filledContainerStack = fluidHandlerItem.getContainer();
                itemHandler.insertItem(outputSlot, filledContainerStack, false);
            }
        }
    }

    @Override
    public ITextComponent getDisplayName() {
        return getName() == null ? new TextComponentString("???") : new TextComponentTranslation(getName());
    }

    @Override
    public Set<Item> getApplicableUpgrades() {
        return applicableUpgrades;
    }

    @Override
    public String getName() {
        return null; //Is called directly from the block instead.
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }

    protected void addLuaMethods() {
        if (this instanceof IHeatExchanger) {
            final IHeatExchanger exchanger = (IHeatExchanger) this;
            luaMethods.add(new LuaMethod("getTemperature") {
                @Override
                public Object[] call(Object[] args) throws Exception {
                    if (args.length == 0) {
                        return new Object[]{exchanger.getHeatExchangerLogic(null).getTemperature()};
                    } else if (args.length == 1) {
                        IHeatExchangerLogic logic = exchanger.getHeatExchangerLogic(getDirForString((String) args[0]));
                        return new Object[]{logic != null ? logic.getTemperature() : 0};
                    } else {
                        throw new IllegalArgumentException("getTemperature method requires 0 or 1 argument (direction: up, down, east, west, north, south!");
                    }
                }
            });
        }
    }

    @Override
    public String getType() {
        return getBlockType().getUnlocalizedName().substring(5);
    }

    @Override
    public String[] getMethodNames() {
        String[] methodNames = new String[luaMethods.size()];
        for (int i = 0; i < methodNames.length; i++) {
            methodNames[i] = luaMethods.get(i).getMethodName();
        }
        return methodNames;
    }

    public List<ILuaMethod> getLuaMethods() {
        return luaMethods;
    }

    @Override
    @Optional.Method(modid = ModIds.COMPUTERCRAFT)
    public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException {
        try {
            return luaMethods.get(method).call(arguments);
        } catch (Exception e) {
            throw new LuaException(e.getMessage());
        }
    }

    @Override
    @Optional.Method(modid = ModIds.COMPUTERCRAFT)
    public void attach(IComputerAccess computer) {
    }

    @Override
    @Optional.Method(modid = ModIds.COMPUTERCRAFT)
    public void detach(IComputerAccess computer) {
    }

    @Override
    @Optional.Method(modid = ModIds.COMPUTERCRAFT)
    public boolean equals(IPeripheral other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        if (other instanceof TileEntity) {
            TileEntity tother = (TileEntity) other;
            return tother.getWorld().equals(getWorld()) && tother.getPos().equals(getPos());
        }

        return false;
    }

    public IItemHandlerModifiable getPrimaryInventory() {
        return null;
    }

    public UpgradeHandler getUpgradesInventory() {
        return upgradeHandler;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return getPrimaryInventory() != null;
        } else {
            return super.hasCapability(capability, facing);
        }
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && getPrimaryInventory() != null) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(getPrimaryInventory());
        }
        return super.getCapability(capability, facing);
    }

    /**
     * Collect all items which should be dropped when this TE is broken.  Override this if the subclassing TE has
     * extra inventories which need to be dropped.
     *
     * @param drops list in which to collect dropped items
     */
    public void getAllDrops(NonNullList<ItemStack> drops) {
        if (getPrimaryInventory() != null) {
            for (int i = 0; i < getPrimaryInventory().getSlots(); i++) {
                drops.add(getPrimaryInventory().getStackInSlot(i));
            }
        }
        // TODO consider preserving upgrades in the dropped block
        if (getUpgradesInventory() != null) {
            for (int i = 0; i < getUpgradesInventory().getSlots(); i++) {
                drops.add(getUpgradesInventory().getStackInSlot(i));
            }
        }

        if (this instanceof ICamouflageableTE) {
            IBlockState camoState = ((ICamouflageableTE) this).getCamouflage();
            if (camoState != null) {
                drops.add(ICamouflageableTE.getStackForState(camoState));
            }
        }
    }

    /**
     * Carry out any tasks which need a world object (the world is null in the TE constructor)
     */
    public void onTileEntityCreated() {
    }

    public String getRedstoneButtonText(int mode) {
        switch (mode) {
            case 0:
                return "gui.tab.redstoneBehaviour.button.anySignal";
            case 1:
                return "gui.tab.redstoneBehaviour.button.highSignal";
            case 2:
                return "gui.tab.redstoneBehaviour.button.lowSignal";
        }
        return "<ERROR>";
    }

    public String getRedstoneString() {
        return this instanceof IRedstoneControlled ? "gui.tab.redstoneBehaviour.enableOn" : "gui.tab.redstoneBehaviour.emitRedstoneWhen";
    }

    /**
     * Called when a machine's upgrades have changed in any way.  This is also called from readNBT() when saved upgrades
     * are deserialized, so it is not guaranteed that the world field is non-null - beware.
     */
    protected void onUpgradesChanged() {}

    public UpgradeCache getUpgradeCache() {
        return upgradeCache;
    }

    public class UpgradeHandler extends FilteredItemStackHandler {
        UpgradeHandler(int upgradeSize) {
            super(upgradeSize);
        }

        @Override
        public boolean test(Integer integer, ItemStack itemStack) {
            return itemStack.isEmpty()
                    || applicableUpgrades.contains(itemStack.getItem())
                    || applicableCustomUpgrades.contains(makeUpgradeKey(itemStack));
        }

        @Override
        protected void onContentsChanged(int slot) {
            upgradeCache.invalidate();
        }
    }

    public class UpgradeCache {
        private final int upgradeCount[] = new int[IItemRegistry.EnumUpgrade.values().length];
        private Map<String,Integer> customUpgradeCount;
        private final TileEntityBase te;
        private boolean isValid = false;

        UpgradeCache(TileEntityBase te) {
            this.te = te;
        }

        void validate() {
            if (isValid) return;

            Arrays.fill(upgradeCount, 0);
            customUpgradeCount = null;
            IItemHandler inv = te.getUpgradesInventory();
            for (int i = 0; i < inv.getSlots(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (stack.getItem() instanceof ItemMachineUpgrade) {
                    // native upgrade
                    int idx = ((ItemMachineUpgrade) stack.getItem()).getUpgradeType().ordinal();
                    upgradeCount[idx] += inv.getStackInSlot(i).getCount();
                } else if (!inv.getStackInSlot(i).isEmpty()) {
                    // custom upgrade from another mod
                    if (customUpgradeCount == null)
                        customUpgradeCount = Maps.newHashMap();
                    String key = makeUpgradeKey(stack);
                    customUpgradeCount.put(key, customUpgradeCount.getOrDefault(key, 0) + stack.getCount());
                }
            }
            te.onUpgradesChanged();
            isValid = true;
        }

        /**
         * Mark the upgrade cache as invalid.  It will be revalidated at the start of the next update tick for the TE.
         */
        public void invalidate() {
            isValid = false;
        }

        public int getUpgrades(IItemRegistry.EnumUpgrade type) {
            return upgradeCount[type.ordinal()];
        }

        public int getUpgrades(ItemStack stack) {
            return customUpgradeCount == null ? 0 : customUpgradeCount.getOrDefault(makeUpgradeKey(stack), 0);
        }
    }
}
