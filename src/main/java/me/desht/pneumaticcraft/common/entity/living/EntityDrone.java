package me.desht.pneumaticcraft.common.entity.living;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.api.block.IPneumaticWrenchable;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.drone.IPathNavigator;
import me.desht.pneumaticcraft.api.drone.IPathfindHandler;
import me.desht.pneumaticcraft.api.event.SemiblockEvent;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.tileentity.IManoMeasurable;
import me.desht.pneumaticcraft.client.render.RenderDroneHeldItem;
import me.desht.pneumaticcraft.client.render.RenderProgressingLine;
import me.desht.pneumaticcraft.common.DamageSourcePneumaticCraft.DamageSourceDroneOverload;
import me.desht.pneumaticcraft.common.DroneRegistry;
import me.desht.pneumaticcraft.common.ai.*;
import me.desht.pneumaticcraft.common.ai.DroneAIManager.EntityAITaskEntry;
import me.desht.pneumaticcraft.common.config.Config;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModEntityTypes;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.Sounds;
import me.desht.pneumaticcraft.common.inventory.handler.ChargeableItemHandler;
import me.desht.pneumaticcraft.common.item.ItemGPSTool;
import me.desht.pneumaticcraft.common.item.ItemGunAmmo;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.common.network.*;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetGoToLocation;
import me.desht.pneumaticcraft.common.recipes.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferCustom;
import me.desht.pneumaticcraft.common.tileentity.PneumaticEnergyStorage;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.fakeplayer.DroneFakePlayer;
import me.desht.pneumaticcraft.common.util.fakeplayer.DroneItemHandler;
import me.desht.pneumaticcraft.common.util.fakeplayer.FakeNetHandlerPlayerServer;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.NBTKeys;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.passive.IFlyingAnimal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class EntityDrone extends EntityDroneBase implements
        IManoMeasurable, IPneumaticWrenchable, IEntityAdditionalSpawnData,
        IHackableEntity, IDroneBase, IFlyingAnimal {

    private static final float LASER_EXTEND_SPEED = 0.05F;

    private static final HashMap<String, Integer> colorMap = new HashMap<>();

    static {
        colorMap.put("aureylian", 0xff69b4);
        colorMap.put("loneztar", 0x00a0a0);
        colorMap.put("jadedcat", 0xa020f0);
        colorMap.put("desht", 0xff6000);
    }

    private EntityDroneItemHandler inventory = new EntityDroneItemHandler(this);
    private final LazyOptional<IItemHandlerModifiable> inventoryCap = LazyOptional.of(() -> inventory);

    private final FluidTank tank = new FluidTank(Integer.MAX_VALUE);
    private final LazyOptional<IFluidHandler> fluidCap = LazyOptional.of(() -> tank);

    private final PneumaticEnergyStorage energy = new PneumaticEnergyStorage(100000);
    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);

    private final ItemStackHandler upgradeInventory = new ItemStackHandler(9) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            energy.setCapacity(100000 + 100000 * getUpgrades(EnumUpgrade.VOLUME));
        }
    };
    private final int[] emittingRedstoneValues = new int[6];
    private float propSpeed;

    float currentAir; //the current held energy of the Drone;
    private float volume;
    private RenderProgressingLine targetLine;
    private RenderProgressingLine oldTargetLine;
    public List<IProgWidget> progWidgets = new ArrayList<>();

    private DroneFakePlayer fakePlayer;
    public String playerName = "Drone";
    private String playerUUID;

    private DroneGoToChargingStation chargeAI;
    private DroneGoToOwner gotoOwnerAI;
    private final DroneAIManager aiManager = new DroneAIManager(this);

    private boolean firstTick = true;
    public boolean naturallySpawned = true; //determines if it should drop a drone when it dies.
    private double speed;
    private int lifeUpgrades;
    private int suffocationCounter = 40; //Drones are invincible for suffocation for this time.
    private boolean isSuffocating;
    private boolean disabledByHacking;
    private boolean standby; //If true, the drone's propellors stop, the drone will fall down, and won't use pressure.
    private Minigun minigun;

    private AmadronOffer handlingOffer;
    private int offerTimes;
    private ItemStack usedTablet;//Tablet used to place the order.
    private String buyingPlayer;
    private final DroneDebugList debugList = new DroneDebugList();
    private final Set<ServerPlayerEntity> syncedPlayers = new HashSet<>();

    private int securityUpgradeCount; // for liquid immunity: 1 = breathe in water, 2 = temporary air bubble, 3+ = permanent water removal
    private final Map<BlockPos, BlockState> displacedLiquids = new HashMap<>();  // liquid blocks displaced by security upgrade

    // Although this is only used by DroneAILogistics, it is here rather than there
    // so it can persist, for performance reasons; DroneAILogistics is a short-lived object
    private LogisticsManager logisticsManager;

    public static EntityDrone create(EntityType<Entity> entityEntityType, World world) {
        return new EntityDrone(ModEntityTypes.DRONE, world);
    }

    public static Entity createClient(FMLPlayMessages.SpawnEntity spawnEntity, World world) {
        return new EntityDrone(ModEntityTypes.DRONE, world);
    }

    public EntityDrone(EntityType<? extends EntityDrone> type, World world) {
        super(type, world);
        moveController = new DroneMovementController(this);
        goalSelector.addGoal(1, chargeAI = new DroneGoToChargingStation(this));
    }

    public EntityDrone(EntityType<? extends EntityDrone> type, World world, PlayerEntity player) {
        this(type, world);
        if (player != null) {
            playerUUID = player.getGameProfile().getId().toString();
            playerName = player.getName().getFormattedText();
        } else {
            playerUUID = getUniqueID().toString(); //Anonymous drone used for Amadron or spawned with a Dispenser
        }
    }

    @SubscribeEvent
    public void onSemiblockEvent(SemiblockEvent event) {
        if (!event.getWorld().isRemote && event.getWorld() == getEntityWorld()) {
            logisticsManager = null;
        }
    }

    @Override
    protected PathNavigator createNavigator(World worldIn) {
        EntityPathNavigateDrone nav = new EntityPathNavigateDrone(this, worldIn);
        nav.setCanOpenDoors(false);
        nav.setCanSwim(true);
        nav.setCanEnterDoors(true);
        return nav;
    }

    private void initializeFakePlayer() {
        fakePlayer = new DroneFakePlayer((ServerWorld) world, new GameProfile(UUID.fromString(getOwnerUUID()), playerName), this);
        fakePlayer.connection = new FakeNetHandlerPlayerServer(ServerLifecycleHooks.getCurrentServer(), fakePlayer);
//        fakePlayer.inventory = new InventoryFakePlayer(fakePlayer) {
//            @Override
//            public IItemHandlerModifiable getUnderlyingItemHandler() {
//                return EntityDrone.this.inventory;
//            }
//        };
    }

    private static final DataParameter<Boolean> ACCELERATING = EntityDataManager.createKey(EntityDrone.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Float> PRESSURE = EntityDataManager.createKey(EntityDrone.class, DataSerializers.FLOAT);
    private static final DataParameter<String> PROGRAM_KEY = EntityDataManager.createKey(EntityDrone.class, DataSerializers.STRING);
    private static final DataParameter<BlockPos> DUG_POS = EntityDataManager.createKey(EntityDrone.class, DataSerializers.BLOCK_POS);
    private static final DataParameter<Boolean> GOING_TO_OWNER = EntityDataManager.createKey(EntityDrone.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> DRONE_COLOR = EntityDataManager.createKey(EntityDrone.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> MINIGUN_ACTIVE = EntityDataManager.createKey(EntityDrone.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> HAS_MINIGUN = EntityDataManager.createKey(EntityDrone.class, DataSerializers.BOOLEAN);
    private static final DataParameter<ItemStack> AMMO = EntityDataManager.createKey(EntityDrone.class, DataSerializers.ITEMSTACK);
    private static final DataParameter<String> LABEL = EntityDataManager.createKey(EntityDrone.class, DataSerializers.STRING);
    private static final DataParameter<Integer> ACTIVE_WIDGET = EntityDataManager.createKey(EntityDrone.class, DataSerializers.VARINT);
    private static final DataParameter<BlockPos> TARGET_POS = EntityDataManager.createKey(EntityDrone.class, DataSerializers.BLOCK_POS);
    private static final DataParameter<ItemStack> HELD_ITEM = EntityDataManager.createKey(EntityDrone.class, DataSerializers.ITEMSTACK);
    private static final DataParameter<Integer> TARGET_ID = EntityDataManager.createKey(EntityDrone.class, DataSerializers.VARINT);

    @Override
    protected void registerData() {
        super.registerData();
        dataManager.register(PRESSURE, 0.0f);
        dataManager.register(ACCELERATING, false);
        dataManager.register(PROGRAM_KEY, "");
        dataManager.register(DUG_POS, BlockPos.ZERO);
        dataManager.register(GOING_TO_OWNER, false);
        dataManager.register(DRONE_COLOR, 0);
        dataManager.register(MINIGUN_ACTIVE, false);
        dataManager.register(HAS_MINIGUN, false);
        dataManager.register(AMMO, ItemStack.EMPTY);
        dataManager.register(LABEL, "");
        dataManager.register(ACTIVE_WIDGET, 0);
        dataManager.register(TARGET_POS, BlockPos.ZERO);
        dataManager.register(HELD_ITEM, ItemStack.EMPTY);
        dataManager.register(TARGET_ID, 0);
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
        getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(40F);
        getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(getRange());
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return inventoryCap.cast();
        } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return fluidCap.cast();
        } else if (capability == CapabilityEnergy.ENERGY) {
            return energyCap.cast();
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void writeSpawnData(PacketBuffer data) {
        PacketUtil.writeUTF8String(data, getFakePlayer().getName().getFormattedText());
    }

    @Override
    public void readSpawnData(PacketBuffer data) {
        playerName = PacketUtil.readUTF8String(data);
    }

    /**
     * Determines if an entity can be despawned, used on idle far away entities
     */
    @Override
    public boolean canDespawn(double dist) {
        return false;
    }

    @Override
    protected float getSoundVolume() {
        return 0.2F;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource p_184601_1_) {
        return Sounds.DRONE_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return Sounds.DRONE_DEATH;
    }

    @Override
    public void tick() {
        if (firstTick) {
            firstTick = false;
            volume = PneumaticValues.DRONE_VOLUME + getUpgrades(EnumUpgrade.VOLUME) * PneumaticValues.VOLUME_VOLUME_UPGRADE;
            securityUpgradeCount = getUpgrades(EnumUpgrade.SECURITY);
            if (securityUpgradeCount > 0) {
                ((EntityPathNavigateDrone) getPathNavigator()).pathThroughLiquid = true;
            }
            setPathPriority(PathNodeType.WATER, securityUpgradeCount > 0 ? 0.0f : -1.0f);
            speed = 0.15 + Math.min(10, getUpgrades(EnumUpgrade.SPEED)) * 0.015;
            lifeUpgrades = getUpgrades(EnumUpgrade.ITEM_LIFE);
            if (!world.isRemote) {
                setHasMinigun(getUpgrades(EnumUpgrade.ENTITY_TRACKER) > 0);
                MinecraftForge.EVENT_BUS.register(this);
                aiManager.setWidgets(progWidgets);
            }
            energy.setCapacity(100000 + 100000 * getUpgrades(EnumUpgrade.VOLUME));
        }
        boolean enabled = !disabledByHacking && getPressure(null) > 0.01F;
        if (!world.isRemote) {
            inventory.updateHeldItem();
            setAccelerating(!standby && enabled);
            if (isAccelerating()) {
                fallDistance = 0;
            }
            if (lifeUpgrades > 0) {
                int interval = 10 / lifeUpgrades;
                if (interval == 0 || ticksExisted % interval == 0) {
                    heal(1);
                }
            }
            if (!isSuffocating) {
                suffocationCounter = 40;
            }
            isSuffocating = false;
            Path path = getNavigator().getPath();
            if (path != null) {
                PathPoint target = path.getFinalPathPoint();
                if (target != null) {
                    setTargetedBlock(new BlockPos(target.x, target.y, target.z));
                } else {
                    setTargetedBlock(null);
                }
            } else {
                setTargetedBlock(null);
            }
            if (world.getGameTime() % 20 == 0) {
                updateSyncedPlayers();
            }
            FakePlayer fp = getFakePlayer();
            fp.posX = posX;
            fp.posY = posY;
            fp.posZ = posZ;
            fp.tick();
        } else {
            if (digLaser != null) digLaser.update();
            oldLaserExtension = laserExtension;
            if (getActiveProgramKey().equals("dig")) {
                laserExtension = Math.min(1, laserExtension + LASER_EXTEND_SPEED);
            } else {
                laserExtension = Math.max(0, laserExtension - LASER_EXTEND_SPEED);
            }

            if (isAccelerating() && rand.nextBoolean()) {
                int x = (int) Math.floor(posX);
                int y = (int) Math.floor(posY - 1);
                int z = (int) Math.floor(posZ);
                BlockPos pos = new BlockPos(x, y, z);
                BlockState state = null;
                for (int i = 0; i < 3; i++) {
                    state = world.getBlockState(pos);
                    if (state.getMaterial() != Material.AIR) break;
                    y--;
                }

                if (state.getMaterial() != Material.AIR) {
                    Vec3d vec = new Vec3d(posY - y, 0, 0);
                    vec = vec.rotateYaw((float) (rand.nextFloat() * Math.PI * 2));
                    IParticleData data = new BlockParticleData(ParticleTypes.BLOCK, state);
                    world.addParticle(data, posX + vec.x, y + 1, posZ + vec.z, vec.x, 0, vec.z);
                }
            }
        }
        if (securityUpgradeCount > 1 && getHealth() > 0F) {
            restoreLiquids(true);

            for (int x = (int) posX - 1; x <= (int) (posX + getWidth()); x++) {
                for (int y = (int) posY - 1; y <= (int) (posY + getHeight() + 1); y++) {
                    for (int z = (int) posZ - 2; z <= (int) (posZ + getWidth()); z++) {
                        if (PneumaticCraftUtils.isBlockLiquid(world.getBlockState(new BlockPos(x, y, z)).getBlock())) {
                            BlockPos pos = new BlockPos(x, y, z);
                            if (securityUpgradeCount == 2) displacedLiquids.put(pos, world.getBlockState(pos));
                            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
                        }
                    }
                }
            }
        }
        if (isAccelerating()) {
            setMotion(getMotion().scale(0.3));
            propSpeed = Math.min(1, propSpeed + 0.04F);
            addAir(null, -1);
        } else {
            propSpeed = Math.max(0, propSpeed - 0.04F);
        }
        oldPropRotation = propRotation;
        propRotation += propSpeed;

        if (!world.isRemote && isAlive()) {
            for (int i = 0; i < 4; i++) {
                getFakePlayer().interactionManager.tick();
            }
        }
        super.tick();
        if (hasMinigun()) getMinigun().setAttackTarget(getAttackTarget()).update(posX, posY, posZ);
        if (!world.isRemote && isAlive()) {
            if (enabled) aiManager.onUpdateTasks();
            for (Direction d : Direction.values()) {
                if (getEmittingRedstone(d) > 0) {
                    if (world.isAirBlock(new BlockPos((int) Math.floor(posX + getWidth() / 2), (int) Math.floor(posY), (int) Math.floor(posZ + getWidth() / 2)))) {
                        world.setBlockState(new BlockPos((int) Math.floor(posX + getWidth() / 2), (int) Math.floor(posY), (int) Math.floor(posZ + getWidth() / 2)), ModBlocks.DRONE_REDSTONE_EMITTER.getDefaultState());
                    }
                    break;
                }
            }
        }
    }

    @Override
    public boolean canBreatheUnderwater() {
        return securityUpgradeCount > 0;
    }

    public BlockPos getTargetedBlock() {
        BlockPos pos = dataManager.get(TARGET_POS);
        return pos.equals(BlockPos.ZERO) ? null : pos;
    }

    private void setTargetedBlock(BlockPos pos) {
        dataManager.set(TARGET_POS, pos == null ? BlockPos.ZERO : pos);
    }

    @Override
    public int getLaserColor() {
        if (colorMap.containsKey(getCustomName().getFormattedText().toLowerCase())) {
            return colorMap.get(getCustomName().getFormattedText().toLowerCase());
        } else if (colorMap.containsKey(playerName.toLowerCase())) {
            return colorMap.get(playerName.toLowerCase());
        }
        return super.getLaserColor();
    }

    @Override
    protected BlockPos getDugBlock() {
        BlockPos pos = dataManager.get(DUG_POS);
        return pos.equals(BlockPos.ZERO) ? null : pos;
    }

    @Override
    public ItemStack getDroneHeldItem() {
        return Config.Client.dronesRenderHeldItem ? dataManager.get(HELD_ITEM) : ItemStack.EMPTY;
    }

    @Override
    public void setDugBlock(BlockPos pos) {
        dataManager.set(DUG_POS, pos == null ? BlockPos.ZERO : pos);
    }

    public List<EntityAITaskEntry> getRunningTasks() {
        return aiManager.getRunningTasks();
    }

    public Goal getRunningTargetAI() {
        return aiManager.getTargetAI();
    }

    public void setVariable(String varName, BlockPos pos) {
        aiManager.setCoordinate(varName, pos);
    }

    public BlockPos getVariable(String varName) {
        return aiManager.getCoordinate(varName);
    }

    private String getActiveProgramKey() {
        return dataManager.get(PROGRAM_KEY);
    }

    /**
     * Can only be called when the drone is being debugged, so the client has a synced progWidgets array.
     *
     * @return
     */
    public IProgWidget getActiveWidget() {
        int index = getActiveWidgetIndex();
        if (index >= 0 && index < progWidgets.size()) {
            return progWidgets.get(index);
        } else {
            return null;
        }
    }

    private int getActiveWidgetIndex() {
        return dataManager.get(ACTIVE_WIDGET);
    }

    @Override
    public void setActiveProgram(IProgWidget widget) {
        dataManager.set(PROGRAM_KEY, widget.getWidgetString());
        dataManager.set(ACTIVE_WIDGET, progWidgets.indexOf(widget));
    }

    private void setAccelerating(boolean accelerating) {
        dataManager.set(ACCELERATING, accelerating);
    }

    @Override
    public boolean isAccelerating() {
        return dataManager.get(ACCELERATING);
    }

    private void setDroneColor(int color) {
        dataManager.set(DRONE_COLOR, color);
    }

    @Override
    public int getDroneColor() {
        return dataManager.get(DRONE_COLOR);
    }

    private void setMinigunActivated(boolean activated) {
        dataManager.set(MINIGUN_ACTIVE, activated);
    }

    private boolean isMinigunActivated() {
        return dataManager.get(MINIGUN_ACTIVE);
    }

    private void setHasMinigun(boolean hasMinigun) {
        dataManager.set(HAS_MINIGUN, hasMinigun);
    }

    public boolean hasMinigun() {
        return dataManager.get(HAS_MINIGUN);
    }

    public int getAmmoColor() {
        ItemStack ammo = dataManager.get(AMMO);
        if (ammo.getItem() instanceof ItemGunAmmo) {
            return ((ItemGunAmmo) ammo.getItem()).getAmmoColor(ammo);
        }
        return 0x808080;  // shouldn't happen
    }

    public void setAmmoColor(ItemStack color) {
        dataManager.set(AMMO, color);
    }

    /**
     * Decrements the entity's air supply when underwater
     */
    @Override
    protected int decreaseAirSupply(int par1) {
        return -20; // make drones insta drown
    }

    /**
     * Moves the entity based on the specified heading.  Args: strafe, forward
     */
    @Override
    public void travel(Vec3d travelVec) {
        if (world.isRemote) {
            LivingEntity targetEntity = getAttackTarget();
            if (targetEntity != null && !targetEntity.isAlive()) {
                setAttackTarget(null);
                targetEntity = null;
            }
            if (targetEntity != null) {
                if (targetLine == null) targetLine = new RenderProgressingLine(0, -getHeight() / 2, 0, 0, 0, 0);
                if (oldTargetLine == null) oldTargetLine = new RenderProgressingLine(0, -getHeight() / 2, 0, 0, 0, 0);

                targetLine.endX = targetEntity.posX - posX;
                targetLine.endY = targetEntity.posY + targetEntity.getHeight() / 2 - posY;
                targetLine.endZ = targetEntity.posZ - posZ;
                oldTargetLine.endX = targetEntity.prevPosX - prevPosX;
                oldTargetLine.endY = targetEntity.prevPosY + targetEntity.getHeight() / 2 - prevPosY;
                oldTargetLine.endZ = targetEntity.prevPosZ - prevPosZ;

                oldTargetLine.setProgress(targetLine.getProgress());
                targetLine.incProgressByDistance(0.3D);
                ignoreFrustumCheck = true; //don't stop rendering the drone when it goes out of the camera frustrum, as we need to render the target lines as well.
            } else {
                targetLine = oldTargetLine = null;
                ignoreFrustumCheck = false; //don't stop rendering the drone when it goes out of the camera frustrum, as we need to render the target lines as well.
            }
        }
        if (getRidingEntity() == null && isAccelerating()) {
            double d3 = getMotion().y;
            super.travel(travelVec);
            setMotion(getMotion().x, d3 * 0.6D, getMotion().z);
        } else {
            super.travel(travelVec);
        }
        onGround = true; //set onGround to true so AI pathfinding will keep updating.
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderExtras(double transX, double transY, double transZ, float partialTicks) {
        super.renderExtras(transX, transY, transZ, partialTicks);

        if (targetLine != null && oldTargetLine != null) {
            GlStateManager.pushMatrix();
            GlStateManager.scaled(1, -1, 1);
            GlStateManager.disableTexture();
            GlStateManager.color4f(1, 0, 0, 1);
            targetLine.renderInterpolated(oldTargetLine, partialTicks);
            GlStateManager.color4f(1, 1, 1, 1);
            GlStateManager.enableTexture();
            GlStateManager.popMatrix();
        }

        double x = lastTickPosX + (posX - lastTickPosX) * partialTicks;
        double y = lastTickPosY + (posY - lastTickPosY) * partialTicks;
        double z = lastTickPosZ + (posZ - lastTickPosZ) * partialTicks;
        getMinigun().render(x, y, z, 0.6);

        ItemStack held = getDroneHeldItem();
        if (!held.isEmpty() && !(held.getItem() instanceof ItemGunAmmo && hasMinigun())) {
            if (renderDroneHeldItem == null) {
                renderDroneHeldItem = new RenderDroneHeldItem(world);
            }
            renderDroneHeldItem.render(held);
        }
    }

    public double getRange() {
        return 75;
    }

    @Override
    public boolean processInteract(PlayerEntity player, Hand hand) {
        ItemStack equippedItem = player.getHeldItem(hand);
        if (!world.isRemote && !equippedItem.isEmpty()) {
            if (equippedItem.getItem() == ModItems.GPS_TOOL) {
                BlockPos gpsLoc = ItemGPSTool.getGPSLocation(equippedItem);
                if (gpsLoc != null) {
                    getNavigator().tryMoveToXYZ(gpsLoc.getX(), gpsLoc.getY(), gpsLoc.getZ(), 0.1D);
                }
            } else {
                DyeColor color = DyeColor.getColor(equippedItem);
                if (color != null) {
                    setDroneColor(color.getId());
                    if (Config.Common.General.useUpDyesWhenColoring && !player.isCreative()) {
                        equippedItem.shrink(1);
                        if (equippedItem.getCount() <= 0) {
                            player.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Called when a drone is hit by a Pneumatic Wrench.
     */
    @Override
    public boolean onWrenched(World world, PlayerEntity player, BlockPos pos, Direction side, Hand hand) {
        if (!naturallySpawned) {
            if (player.isCreative()) naturallySpawned = true;//don't drop the drone in creative.
            attackEntityFrom(new DamageSourceDroneOverload("wrenched"), 2000.0F);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Restore any liquids that may have been displaced by the drone (security upgrade)
     *
     * @param distCheck if true, only restore liquids in blocks > 1 block distance away from the drone
     */
    private void restoreLiquids(boolean distCheck) {
        Iterator<Map.Entry<BlockPos, BlockState>> iter = displacedLiquids.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<BlockPos, BlockState> entry = iter.next();
            BlockPos pos = entry.getKey();
            if (!distCheck || pos.distanceSq(posX, posY, posZ, true) > 1) {
                if (world.isAirBlock(pos) || PneumaticCraftUtils.isBlockLiquid(world.getBlockState(pos).getBlock())) {
                    world.setBlockState(pos, entry.getValue(), 2);
                }
                iter.remove();
            }
        }
    }

    @Nullable
    @Override
    public Entity changeDimension(DimensionType type) {
        Entity entity = super.changeDimension(type);
        if (entity != null) {
            restoreLiquids(false);
        }
        return entity;
    }

    @Override
    public void onDeath(DamageSource par1DamageSource) {
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) {
                entityDropItem(inventory.getStackInSlot(i), 0);
                inventory.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
        restoreLiquids(false);
        if (!naturallySpawned) {
            ItemStack drone = getDroppedStack();
            if (hasCustomName()) drone.setDisplayName(getCustomName());
            entityDropItem(drone, 0);

            if (!world.isRemote) {
                PlayerEntity owner = getOwner();
                if (owner != null) {
                    int x = (int) Math.floor(posX);
                    int y = (int) Math.floor(posY);
                    int z = (int) Math.floor(posZ);
                    ITextComponent msg = hasCustomName() ?
                            new TranslationTextComponent("death.drone.named", getCustomName().toString(), x, y, z) :
                            new TranslationTextComponent("death.drone", x, y, z);
                    msg = msg.appendSibling(new StringTextComponent(" - ")).appendSibling(par1DamageSource.getDeathMessage(this));
                    owner.sendStatusMessage(msg, false);
                }
            }
        }
        if (!world.isRemote) getFakePlayer().interactionManager.abortDestroyBlock();
        setCustomName(new StringTextComponent(""));  // keep other mods (like CoFH Core) quiet about death message broadcasts
        super.onDeath(par1DamageSource);
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    protected ItemStack getDroppedStack() {
        CompoundNBT tag = new CompoundNBT();
        writeAdditional(tag);
        ItemStack drone = new ItemStack(ModItems.DRONE);
        drone.setTag(tag);
        return drone;
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        if (world.isRemote && TARGET_ID.equals(key)) {
            int id = dataManager.get(TARGET_ID);
            if (id > 0) {
                Entity e = getEntityWorld().getEntityByID(id);
                if (e instanceof LivingEntity) {
                    setAttackTarget((LivingEntity) e);
                }
            }
            if (targetLine != null && oldTargetLine != null) {
                targetLine.setProgress(0);
                oldTargetLine.setProgress(0);
            }
        } else {
            super.notifyDataManagerChange(key);
        }
    }

    @Override
    public void setAttackTarget(LivingEntity entity) {
        super.setAttackTarget(entity);
        if (!world.isRemote) {
            dataManager.set(TARGET_ID, entity == null ? 0 : entity.getEntityId());
        }
    }

    @Override
    public float getPressure(ItemStack iStack) {
        return dataManager.get(PRESSURE);
    }

    @Override
    public void addAir(ItemStack iStack, int amount) {
        if (!world().isRemote) {
            currentAir += amount;
            dataManager.set(PRESSURE, currentAir / volume);
        }
    }

    @Override
    public float maxPressure(ItemStack iStack) {
        return PneumaticValues.DRONE_MAX_PRESSURE;
    }

    @Override
    public int getVolume(ItemStack iStack) {
        return (int) volume;
    }

    @Override
    public void printManometerMessage(PlayerEntity player, List<ITextComponent> curInfo) {
        if (hasCustomName()) curInfo.add(getCustomName().applyTextStyle(TextFormatting.AQUA));
        curInfo.add(xlate("entityTracker.info.tamed", getFakePlayer().getName()));
        curInfo.add(xlate("gui.tooltip.pressure",PneumaticCraftUtils.roundNumberTo(getPressure(null), 1) + " bar."));
    }

    @Override
    public void writeAdditional(CompoundNBT tag) {
        super.writeAdditional(tag);
        TileEntityProgrammer.setWidgetsToNBT(progWidgets, tag);
        tag.putBoolean("naturallySpawned", naturallySpawned);
        tag.putFloat("currentAir", currentAir);
        tag.putFloat("propSpeed", propSpeed);
        tag.putBoolean("disabledByHacking", disabledByHacking);
        tag.putBoolean("hackedByOwner", gotoOwnerAI != null);
        tag.putInt("color", getDroneColor());
        tag.putBoolean("standby", standby);
        tag.putFloat("volume", volume);

        CompoundNBT variableTag = new CompoundNBT();
        aiManager.writeToNBT(variableTag);
        tag.put("variables", variableTag);

        tag.put("Inventory", inventory.serializeNBT());
        tag.put(ChargeableItemHandler.NBT_UPGRADE_TAG, upgradeInventory.serializeNBT());

        tank.writeToNBT(tag);

        if (handlingOffer != null) {
            CompoundNBT subTag = new CompoundNBT();
            subTag.putBoolean("isCustom", handlingOffer instanceof AmadronOfferCustom);
            handlingOffer.writeToNBT(subTag);
            tag.put("amadronOffer", subTag);
            tag.putInt("offerTimes", offerTimes);
            if (!usedTablet.isEmpty()) usedTablet.write(subTag);
            tag.putString("buyingPlayer", buyingPlayer);
        }

        if (!displacedLiquids.isEmpty()) {
            ListNBT disp = new ListNBT();
            for (Map.Entry<BlockPos, BlockState> entry : displacedLiquids.entrySet()) {
                CompoundNBT p = net.minecraft.nbt.NBTUtil.writeBlockPos(entry.getKey());
                CompoundNBT s = net.minecraft.nbt.NBTUtil.writeBlockState(entry.getValue());
                ListNBT l = new ListNBT();
                l.add(0, p);
                l.add(1, s);
                disp.add(0, l);
            }
            tag.put("displacedLiquids", disp);
        }
    }

    @Override
    public void readAdditional(CompoundNBT tag) {
        super.readAdditional(tag);
        progWidgets = TileEntityProgrammer.getWidgetsFromNBT(tag);
        naturallySpawned = tag.getBoolean("naturallySpawned");
        currentAir = tag.getFloat("currentAir");
        volume = tag.getFloat("volume");
        dataManager.set(PRESSURE, currentAir / volume);
        propSpeed = tag.getFloat("propSpeed");
        disabledByHacking = tag.getBoolean("disabledByHacking");
        setGoingToOwner(tag.getBoolean("hackedByOwner"));
        setDroneColor(tag.getInt("color"));
        aiManager.readFromNBT(tag.getCompound("variables"));
        standby = tag.getBoolean("standby");

        upgradeInventory.deserializeNBT(tag.getCompound(ChargeableItemHandler.NBT_UPGRADE_TAG));

        // we can't just deserialize the saved inv directly into the inventory, since that
        // also affects its size, meaning any added dispenser upgrades wouldn't work
        // TODO verify it's ok to create the fake player at this stage (may need to lazy-create it later)
        inventory = new EntityDroneItemHandler(this);
        ItemStackHandler tmpInv = new ItemStackHandler();
        tmpInv.deserializeNBT(tag.getCompound("Inventory"));
        for (int i = 0; i < tmpInv.getSlots() && i < inventory.getSlots(); i++) {
            inventory.setStackInSlot(i, tmpInv.getStackInSlot(i).copy());
        }

        tank.setCapacity(PneumaticValues.DRONE_TANK_SIZE * (1 + getUpgrades(EnumUpgrade.DISPENSER)));
        tank.readFromNBT(tag);

        energy.setCapacity(100000 + 100000 * getUpgrades(EnumUpgrade.VOLUME));

        if (tag.contains("amadronOffer")) {
            CompoundNBT subTag = tag.getCompound("amadronOffer");
            handlingOffer = subTag.getBoolean("isCustom") ? AmadronOfferCustom.loadFromNBT(subTag) : AmadronOffer.loadFromNBT(subTag);
            usedTablet = subTag.contains("id") ? ItemStack.read(subTag) : ItemStack.EMPTY;
            buyingPlayer = subTag.getString("buyingPlayer");
        } else {
            handlingOffer = null;
            usedTablet = ItemStack.EMPTY;
            buyingPlayer = null;
        }
        offerTimes = tag.getInt("offerTimes");

        if (tag.contains("displacedLiquids")) {
            for (INBT inbt : tag.getList("displacedLiquids", Constants.NBT.TAG_LIST)) {
                ListNBT l = (ListNBT) inbt;
                CompoundNBT p = l.getCompound(0);
                CompoundNBT s = l.getCompound(1);
                BlockPos pos = net.minecraft.nbt.NBTUtil.readBlockPos(p);
                BlockState state = net.minecraft.nbt.NBTUtil.readBlockState(s);
                displacedLiquids.put(pos, state);
            }
        }
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getOwnerUUID(){
        if(playerUUID == null){
            Log.warning(String.format("Drone with owner '%s' has no UUID! Substituting the Drone's UUID (%s).", playerName, getUniqueID().toString()));
            playerUUID = getUniqueID().toString();
        }
        return playerUUID;
    }

    /**
     * This and read() are _not_ being transfered from/to the Drone item.
     */
    @Override
    public CompoundNBT writeWithoutTypeId(CompoundNBT tag) {
        super.writeWithoutTypeId(tag);
        // this can be called client-side, e.g. TheOneProbe
        // but this data isn't sync'd to the client
        if (!getEntityWorld().isRemote) {
            if (playerName != null) {
                tag.putString("owner", playerName);
                tag.putString("ownerUUID", getOwnerUUID());
            }
        }
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        // see writeWithoutTypeId() above
        if (!getEntityWorld().isRemote) {
            if (tag.contains("owner")) {
                playerName = tag.getString("owner");
                playerUUID = tag.contains("ownerUUID") ? tag.getString("ownerUUID") : null;
            }
        }
    }

    public int getUpgrades(EnumUpgrade upgrade) {
        return getUpgrades(upgrade.getItem());
    }

//    @Override
    public int getUpgrades(Item upgrade) {
        int upgrades = 0;
        for (int i = 0; i < upgradeInventory.getSlots(); i++) {
            if (upgradeInventory.getStackInSlot(i).getItem() == upgrade) {
                upgrades += upgradeInventory.getStackInSlot(i).getCount();
            }
        }
        return upgrades;
    }

    @Override
    public FakePlayer getFakePlayer() {
        if (fakePlayer == null && !world.isRemote) {
            initializeFakePlayer();
        }
        return fakePlayer;
    }

    public Minigun getMinigun() {
        if (minigun == null) {
            minigun = new MinigunDrone(this).setPlayer(getFakePlayer()).setWorld(world).setPressurizable(this, PneumaticValues.DRONE_USAGE_ATTACK);
        }
        return minigun;
    }

    @Override
    public boolean attackEntityAsMob(Entity entity) {
        getFakePlayer().attackTargetEntityWithCurrentItem(entity);
        addAir(null, -PneumaticValues.DRONE_USAGE_ATTACK);
        return true;
    }

    @Override
    public boolean attackEntityFrom(DamageSource damageSource, float damage) {
        if (damageSource == DamageSource.IN_WALL) {
            isSuffocating = true;
            if (suffocationCounter-- > 0 || !Config.Common.General.enableDroneSuffocation) {
                return false;
            }
        }
        return super.attackEntityFrom(damageSource, damage);
    }

    @Override
    public IItemHandlerModifiable getInv() {
        return inventory;
    }

    public double getSpeed() {
        return speed;
    }

    public int getEmittingRedstone(Direction side) {
        return emittingRedstoneValues[side.ordinal()];
    }

    @Override
    public void setEmittingRedstone(Direction side, int value) {
        if (emittingRedstoneValues[side.ordinal()] != value) {
            emittingRedstoneValues[side.ordinal()] = value;
            BlockPos pos = new BlockPos((int) Math.floor(posX + getWidth() / 2), (int) Math.floor(posY), (int) Math.floor(posZ + getWidth() / 2));
            BlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    @Override
    public boolean isBlockValidPathfindBlock(BlockPos pos) {
        if (world.isAirBlock(pos)) return true;
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (PneumaticCraftUtils.isBlockLiquid(block)) {
            return securityUpgradeCount > 0;
        }
        if (!state.getMaterial().blocksMovement() && block != Blocks.LADDER) return true;
        if (DroneRegistry.getInstance().pathfindableBlocks.containsKey(block)) {
            IPathfindHandler pathfindHandler = DroneRegistry.getInstance().pathfindableBlocks.get(block);
            return pathfindHandler == null || pathfindHandler.canPathfindThrough(world, pos);
        } else {
            return false;
        }
    }

    @Override
    public void sendWireframeToClient(BlockPos pos) {
        NetworkHandler.sendToAllAround(new PacketShowWireframe(this, pos), world);
    }

    /**
     * IHackableEntity
     */

    @Override
    public String getId() {
        return null;
    }

    @Override
    public boolean canHack(Entity entity, PlayerEntity player) {
        return isAccelerating();
    }

    @Override
    public void addInfo(Entity entity, List<String> curInfo, PlayerEntity player) {
        if (playerName.equals(player.getName())) {
            if (isGoingToOwner()) {
                curInfo.add("pneumaticHelmet.hacking.result.resumeTasks");
            } else {
                curInfo.add("pneumaticHelmet.hacking.result.callBack");
            }
        } else {
            curInfo.add("pneumaticHelmet.hacking.result.disable");
        }
    }

    @Override
    public void addPostHackInfo(Entity entity, List<String> curInfo, PlayerEntity player) {
        if (playerName.equals(player.getName())) {
            if (isGoingToOwner()) {
                curInfo.add("pneumaticHelmet.hacking.finished.calledBack");
            } else {
                curInfo.add("pneumaticHelmet.hacking.finished.resumedTasks");
            }
        } else {
            curInfo.add("pneumaticHelmet.hacking.finished.disabled");
        }
    }

    @Override
    public int getHackTime(Entity entity, PlayerEntity player) {
        return playerName.equals(player.getName()) ? 20 : 100;
    }

    @Override
    public void onHackFinished(Entity entity, PlayerEntity player) {
        if (!world.isRemote && player.getGameProfile().equals(getFakePlayer().getGameProfile())) {
            setGoingToOwner(gotoOwnerAI == null);//toggle the state
        } else {
            disabledByHacking = true;
        }
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return false;
    }

    private void setGoingToOwner(boolean state) {
        if (!world.isRemote) {
            if (state && gotoOwnerAI == null) {
                gotoOwnerAI = new DroneGoToOwner(this);
                goalSelector.addGoal(2, gotoOwnerAI);
                dataManager.set(GOING_TO_OWNER, true);
                setActiveProgram(new ProgWidgetGoToLocation());
            } else if (!state && gotoOwnerAI != null) {
                goalSelector.removeGoal(gotoOwnerAI);
                gotoOwnerAI = null;
                dataManager.set(GOING_TO_OWNER, false);
            }
        }
    }

    private boolean isGoingToOwner() {
        return dataManager.get(GOING_TO_OWNER);
    }

    @Override
    public IFluidTank getTank() {
        return tank;
    }

    /**
     * Returns the owning player. Returns null when the player is not online.
     *
     * @return the owning player
     */
    public PlayerEntity getOwner() {
        return ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUsername(playerName);
    }

    public void setStandby(boolean standby) {
        this.standby = standby;
    }

    @Override
    public World world() {
        return world;
    }

    @Override
    public Vec3d getDronePos() {
        return new Vec3d(posX, posY, posZ);
    }

    @Override
    public void dropItem(ItemStack stack) {
        entityDropItem(stack, 0);
    }

    @Override
    public List<IProgWidget> getProgWidgets() {
        return progWidgets;
    }

    @Override
    public GoalSelector getTargetAI() {
        return targetSelector;
    }

    @Override
    public boolean isProgramApplicable(IProgWidget widget) {
        return true;
    }

    @Override
    public void setName(ITextComponent string) {
        setCustomName(string);
    }

    @Override
    public void setCarryingEntity(Entity entity) {
        if (entity == null) {
            for (Entity e : getCarryingEntities()) {
                e.stopRiding();
                if (e instanceof AbstractMinecartEntity || e instanceof BoatEntity) {
                    // little kludge to prevent the dropped minecart/boat immediately picking up the drone
                    e.posY -= 2;
                    if (world.getBlockState(e.getPosition()).isNormalCube(world, e.getPosition())) {
                        e.posY++;
                    }
                    // minecarts have their own tick() which doesn't decrement rideCooldown
                    if (e instanceof AbstractMinecartEntity) e.rideCooldown = 0;
                }
            }
        } else {
            entity.startRiding(this);
        }
    }

    @Override
    public List<Entity> getCarryingEntities() {
        return getPassengers();
    }

    @Override
    public boolean isAIOverriden() {
        return chargeAI.isExecuting || gotoOwnerAI != null;
    }

    @Override
    public void onItemPickupEvent(ItemEntity curPickingUpEntity, int stackSize) {
        onItemPickup(curPickingUpEntity, stackSize);
    }

    @Override
    public IPathNavigator getPathNavigator() {
        return (IPathNavigator) getNavigator();
    }

    public void tryFireMinigun(LivingEntity target) {
        ItemStack ammo = getAmmo();
        if (getMinigun().setAmmoStack(ammo).tryFireMinigun(target)) {
            for (int i = 0; i < inventory.getSlots(); i++) {
                if (inventory.getStackInSlot(i) == ammo) {
                    inventory.setStackInSlot(i, ItemStack.EMPTY);
                }
            }
        }
    }

    public ItemStack getAmmo() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.getItem() instanceof ItemGunAmmo) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public void setHandlingOffer(AmadronOffer offer, int times, @Nonnull ItemStack usedTablet, String buyingPlayer) {
        handlingOffer = offer;
        offerTimes = times;
        this.usedTablet = usedTablet.copy();
        this.buyingPlayer = buyingPlayer;
    }

    public AmadronOffer getHandlingOffer() {
        return handlingOffer;
    }

    public int getOfferTimes() {
        return offerTimes;
    }

    public ItemStack getUsedTablet() {
        return usedTablet;
    }

    public String getBuyingPlayer() {
        return buyingPlayer;
    }

    @Override
    public void overload(String msgKey, Object... params) {
        attackEntityFrom(new DamageSourceDroneOverload(msgKey, params), 2000.0F);
    }

    @Override
    public DroneAIManager getAIManager() {
        return aiManager;
    }

    @Override
    public LogisticsManager getLogisticsManager() {
        return logisticsManager;
    }

    @Override
    public void setLogisticsManager(LogisticsManager logisticsManager) {
        this.logisticsManager = logisticsManager;
    }

    @Override
    public void updateLabel() {
        dataManager.set(LABEL, getAIManager() != null ? getAIManager().getLabel() : "Main");
    }

    public String getLabel() {
        return dataManager.get(LABEL);
    }

    public DebugEntry getCurrentDebugEntry() {
        return debugList.getCurrent();
    }

    public DebugEntry getDebugEntry(int widgetID) {
        return debugList.get(widgetID);
    }

    @Override
    public void addDebugEntry(String message) {
        addDebugEntry(message, null);
    }

    @Override
    public void addDebugEntry(String message, BlockPos pos) {
        DebugEntry entry = new DebugEntry(message, getActiveWidgetIndex(), pos);

        // add the entry server-side
        addDebugEntry(entry);

        // add the entry client-side
        PacketSendDroneDebugEntry packet = new PacketSendDroneDebugEntry(entry, this);
        for (ServerPlayerEntity player : syncedPlayers) {
            NetworkHandler.sendToPlayer(packet, player);
        }
    }

    public void addDebugEntry(DebugEntry entry) {
        debugList.addEntry(entry);
    }

    public void trackAsDebugged(ServerPlayerEntity player) {
        NetworkHandler.sendToPlayer(new PacketSyncDroneEntityProgWidgets(this), player);

        for (DebugEntry entry : debugList.getAll()) {
            NetworkHandler.sendToPlayer(new PacketSendDroneDebugEntry(entry, this), player);
        }

        syncedPlayers.add(player);
    }

    private void updateSyncedPlayers() {
        syncedPlayers.removeIf(player -> !player.isAlive()
                || player.getItemStackFromSlot(EquipmentSlotType.HEAD).isEmpty()
                || NBTUtil.getInteger(player.getItemStackFromSlot(EquipmentSlotType.HEAD), NBTKeys.PNEUMATIC_HELMET_DEBUGGING_DRONE) != getEntityId());
    }

    private class MinigunDrone extends Minigun {
        private final EntityDrone drone;

        MinigunDrone(EntityDrone drone) {
            super(true);
            this.drone = drone;
        }

        @Override
        public Object getSoundSource() {
            return drone;
        }

        @Override
        public boolean isMinigunActivated() {
            return EntityDrone.this.isMinigunActivated();
        }

        @Override
        public void setMinigunActivated(boolean activated) {
            EntityDrone.this.setMinigunActivated(activated);
        }

        @Override
        public void setAmmoColorStack(@Nonnull ItemStack ammo) {
            setAmmoColor(ammo);
        }

        @Override
        public int getAmmoColor() {
            return EntityDrone.this.getAmmoColor();
        }

        @Override
        public void playSound(SoundEvent soundName, float volume, float pitch) {
            NetworkHandler.sendToAllAround(new PacketPlaySound(soundName, SoundCategory.NEUTRAL, posX, posY, posZ, volume, pitch, true), world);
        }
    }

    private class EntityDroneItemHandler extends DroneItemHandler {
        EntityDroneItemHandler(IDrone holder) {
            super(holder);
        }

        @Override
        public void updateHeldItem() {
            if (heldItemChanged && Config.Client.dronesRenderHeldItem)
                dataManager.set(HELD_ITEM, getStackInSlot(0));

            super.updateHeldItem();
        }
    }

    private class DroneDebugList {
        private final Map<Integer, DebugEntry> debugEntries = new HashMap<>();

        private DroneDebugList() {
        }

        void addEntry(DebugEntry entry) {
            debugEntries.put(EntityDrone.this.getActiveWidgetIndex(), entry);
        }

        public Collection<DebugEntry> getAll() {
            return debugEntries.values();
        }

        public DebugEntry get(int widgetId) {
            return debugEntries.get(widgetId);
        }

        public DebugEntry getCurrent() {
            return debugEntries.get(EntityDrone.this.getActiveWidgetIndex());
        }
    }
}
