package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.block.tubes.ModuleNetworkManager;
import me.desht.pneumaticcraft.common.block.tubes.ModuleRegistrator;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.config.Config;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ItemTubeModule;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAdvancedPressureTube;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.BBConstants;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class BlockPressureTube extends BlockPneumaticCraftCamo {

    private static final AxisAlignedBB BASE_BOUNDS = new AxisAlignedBB(
            BBConstants.PRESSURE_PIPE_MIN_POS, BBConstants.PRESSURE_PIPE_MIN_POS, BBConstants.PRESSURE_PIPE_MIN_POS,
            BBConstants.PRESSURE_PIPE_MAX_POS, BBConstants.PRESSURE_PIPE_MAX_POS, BBConstants.PRESSURE_PIPE_MAX_POS
    );

    private static final EnumProperty<ConnectionType> UP = EnumProperty.create("up", ConnectionType.class);
    private static final EnumProperty<ConnectionType> DOWN = EnumProperty.create("down", ConnectionType.class);
    private static final EnumProperty<ConnectionType> NORTH = EnumProperty.create("north", ConnectionType.class);
    private static final EnumProperty<ConnectionType> EAST = EnumProperty.create("east", ConnectionType.class);
    private static final EnumProperty<ConnectionType> SOUTH = EnumProperty.create("south", ConnectionType.class);
    private static final EnumProperty<ConnectionType> WEST = EnumProperty.create("west", ConnectionType.class);
    private static final EnumProperty<ConnectionType>[] CONNECTION_PROPERTIES_3 = new EnumProperty[]{DOWN, UP, NORTH, SOUTH, WEST, EAST};

    private final AxisAlignedBB[] boundingBoxes = new AxisAlignedBB[6];
    private final AxisAlignedBB[] closedBoundingBoxes = new AxisAlignedBB[6];
    private final Tier tier;

    public enum Tier {
        ONE(1, PneumaticValues.DANGER_PRESSURE_PRESSURE_TUBE, PneumaticValues.MAX_PRESSURE_PRESSURE_TUBE, PneumaticValues.VOLUME_PRESSURE_TUBE),
        TWO(2, PneumaticValues.DANGER_PRESSURE_ADVANCED_PRESSURE_TUBE, PneumaticValues.MAX_PRESSURE_ADVANCED_PRESSURE_TUBE, PneumaticValues.VOLUME_ADVANCED_PRESSURE_TUBE);

        private final int tier;
        final float dangerPressure;
        final float criticalPressure;
        final int volume;

        Tier(int tier, float dangerPressure, float criticalPressure, int volume) {
            this.tier = tier;
            this.dangerPressure = dangerPressure;
            this.criticalPressure = criticalPressure;
            this.volume = volume;
        }
    }

    public BlockPressureTube(String registryName, Tier tier) {
        super(Material.IRON, registryName);

        double width = (BBConstants.PRESSURE_PIPE_MAX_POS - BBConstants.PRESSURE_PIPE_MIN_POS) / 2;
        double height = BBConstants.PRESSURE_PIPE_MIN_POS;

        boundingBoxes[0] = new AxisAlignedBB(0.5 - width, BBConstants.PRESSURE_PIPE_MIN_POS - height, 0.5 - width, 0.5 + width, BBConstants.PRESSURE_PIPE_MIN_POS, 0.5 + width);
        boundingBoxes[1] = new AxisAlignedBB(0.5 - width, BBConstants.PRESSURE_PIPE_MAX_POS, 0.5 - width, 0.5 + width, BBConstants.PRESSURE_PIPE_MAX_POS + height, 0.5 + width);
        boundingBoxes[2] = new AxisAlignedBB(0.5 - width, 0.5 - width, BBConstants.PRESSURE_PIPE_MIN_POS - height, 0.5 + width, 0.5 + width, BBConstants.PRESSURE_PIPE_MIN_POS);
        boundingBoxes[3] = new AxisAlignedBB(0.5 - width, 0.5 - width, BBConstants.PRESSURE_PIPE_MAX_POS, 0.5 + width, 0.5 + width, BBConstants.PRESSURE_PIPE_MAX_POS + height);
        boundingBoxes[4] = new AxisAlignedBB(BBConstants.PRESSURE_PIPE_MIN_POS - height, 0.5 - width, 0.5 - width, BBConstants.PRESSURE_PIPE_MIN_POS, 0.5 + width, 0.5 + width);
        boundingBoxes[5] = new AxisAlignedBB(BBConstants.PRESSURE_PIPE_MAX_POS, 0.5 - width, 0.5 - width, BBConstants.PRESSURE_PIPE_MAX_POS + height, 0.5 + width, 0.5 + width);

        height = 2.5 / 16f;  // size of "plug"
        closedBoundingBoxes[0] = new AxisAlignedBB(0.5 - width, BBConstants.PRESSURE_PIPE_MIN_POS - height, 0.5 - width, 0.5 + width, BBConstants.PRESSURE_PIPE_MIN_POS, 0.5 + width);
        closedBoundingBoxes[1] = new AxisAlignedBB(0.5 - width, BBConstants.PRESSURE_PIPE_MAX_POS, 0.5 - width, 0.5 + width, BBConstants.PRESSURE_PIPE_MAX_POS + height, 0.5 + width);
        closedBoundingBoxes[2] = new AxisAlignedBB(0.5 - width, 0.5 - width, BBConstants.PRESSURE_PIPE_MIN_POS - height, 0.5 + width, 0.5 + width, BBConstants.PRESSURE_PIPE_MIN_POS);
        closedBoundingBoxes[3] = new AxisAlignedBB(0.5 - width, 0.5 - width, BBConstants.PRESSURE_PIPE_MAX_POS, 0.5 + width, 0.5 + width, BBConstants.PRESSURE_PIPE_MAX_POS + height);
        closedBoundingBoxes[4] = new AxisAlignedBB(BBConstants.PRESSURE_PIPE_MIN_POS - height, 0.5 - width, 0.5 - width, BBConstants.PRESSURE_PIPE_MIN_POS, 0.5 + width, 0.5 + width);
        closedBoundingBoxes[5] = new AxisAlignedBB(BBConstants.PRESSURE_PIPE_MAX_POS, 0.5 - width, 0.5 - width, BBConstants.PRESSURE_PIPE_MAX_POS + height, 0.5 + width, 0.5 + width);

        this.tier = tier;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityPressureTube.class;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        switch (tier) {
            case ONE: return new TileEntityPressureTube();
            case TWO: return new TileEntityAdvancedPressureTube();
        }
        return null;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        for (EnumProperty p : CONNECTION_PROPERTIES_3) {
            builder.add(p);
        }
    }

    @Override
    public BlockState getActualState(BlockState state, IBlockAccess worldIn, BlockPos pos) {
        state = super.getActualState(state, worldIn, pos);
        TileEntityPressureTube tube = getTE(worldIn, pos);
        if (tube != null) {
            for (int i = 0; i < 6; i++) {
                ConnectionType conn = tube.sidesClosed[i] ? ConnectionType.CLOSED : tube.sidesConnected[i] ? ConnectionType.CONNECTED : ConnectionType.OPEN;
                state = state.withProperty(CONNECTION_PROPERTIES_3[i], conn);
            }
            // bit of a hack, but pressure tube should appear to connect to underside of gas lift
            if (worldIn.getBlockState(pos.up()).getBlock() instanceof BlockGasLift) {
                state = state.withProperty(CONNECTION_PROPERTIES_3[Direction.UP.getIndex()], ConnectionType.CONNECTED);
            }
        }
        return state;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult brtr) {
        if (tryPlaceModule(player, world, pos, brtr.getFace(), hand, false)) {
            return true;
        }
        if (!player.isSneaking()) {
            TubeModule module = getFocusedModule(world, pos, player);
            if (module != null) {
                return module.onActivated(player, hand);
            }
        }
        return super.onBlockActivated(state, world, pos, player, hand, brtr);
    }

    public int getTier() {
        return tier.tier;
    }

    private static TileEntityPressureTube getTE(IBlockReader world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        return te instanceof TileEntityPressureTube ? (TileEntityPressureTube) te : null;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, entity, stack);

        ModuleNetworkManager.getInstance(world).invalidateCache();
        // force TE to calculate its connections immediately so network manager rescanning works
        TileEntityPressureTube te = getTE(world, pos);
        if (te != null) {
            te.onNeighborTileUpdate();
        }
    }

    public boolean tryPlaceModule(PlayerEntity player, World world, BlockPos pos, Direction side, Hand hand, boolean simulate) {
        TileEntityPressureTube tePT = getTE(world, pos);
        if (tePT == null) return false;

        ItemStack heldStack = player.getHeldItem(hand);
        if (heldStack.getItem() instanceof ItemTubeModule) {
            if (tePT.modules[side.ordinal()] == null && !tePT.sidesClosed[side.ordinal()]) {
                TubeModule module = ModuleRegistrator.getModule(((ItemTubeModule) heldStack.getItem()).moduleName);
                if (module == null) return false;
                if (simulate) module.markFake();
                tePT.setModule(module, side);
                if (!simulate) {
                    neighborChanged(world.getBlockState(pos), world, pos, this, pos.offset(side), false);
                    world.notifyNeighborsOfStateChange(pos, this);
                    if (!player.isCreative()) heldStack.shrink(1);
                    NetworkHandler.sendToAllAround(
                            new PacketPlaySound(SoundType.GLASS.getStepSound(), SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(),
                                    SoundType.GLASS.getVolume() * 5.0f, SoundType.GLASS.getPitch() * 0.9f, false),
                            world);
                    ModuleNetworkManager.getInstance(world).invalidateCache();
                }
                return true;
            }
        } else if (heldStack.getItem() == ModItems.ADVANCED_PCB && !simulate) {
            TubeModule module = BlockPressureTube.getFocusedModule(world, pos, player);
            if (module != null && !module.isUpgraded() && module.canUpgrade()) {
                if (!world.isRemote) {
                    module.upgrade();
                    tePT.sendDescriptionPacket();
                    if (!player.isCreative()) heldStack.shrink(1);
                }
                return true;
            }
        }
        return false;
    }

    public static TubeModule getFocusedModule(World world, BlockPos pos, PlayerEntity player) {
        Pair<Vec3d, Vec3d> vecs = PneumaticCraftUtils.getStartAndEndLookVec(player);
        BlockState state = world.getBlockState(pos);
        RayTraceResult rayTraceResult = state.collisionRayTrace(world, pos, vecs.getLeft(), vecs.getRight());
        TubeHitInfo tubeHitInfo = getHitInfo(rayTraceResult);
        if (tubeHitInfo.type == TubeHitInfo.PartType.MODULE) {
            TileEntityPressureTube tube = getTE(world, pos);
            return tube == null ? null : tube.modules[tubeHitInfo.dir.ordinal()];
        }
        return null;
    }

    /**
     * Get the part of the tube being looked at.
     *
     * @param world the world
     * @param pos the blockpos
     * @param player the player
     * @return (true, side) if it's the side of the tube core, or (false, side) if it's a tube arm
     */
    private static Pair<Boolean, Direction> getLookedTube(World world, BlockPos pos, PlayerEntity player) {
        Pair<Vec3d, Vec3d> vecs = PneumaticCraftUtils.getStartAndEndLookVec(player);
        BlockState state = world.getBlockState(pos);
        RayTraceResult rayTraceResult = state.collisionRayTrace(world, pos, vecs.getLeft(), vecs.getRight());
        TubeHitInfo tubeHitInfo = getHitInfo(rayTraceResult);
        if (tubeHitInfo.type == TubeHitInfo.PartType.TUBE) {
            // return either the tube arm (if connected), or the side of the centre face (if not)
            return tubeHitInfo.dir == null ? Pair.of(true, rayTraceResult.sideHit) : Pair.of(false, tubeHitInfo.dir);
        }
        return null;
    }

    @Override
    public RayTraceResult collisionRayTrace(BlockState state, World world, BlockPos pos, Vec3d origin, Vec3d direction) {
        RayTraceResult bestRTR = null;
        AxisAlignedBB bestAABB = null;

        setBlockBounds(BASE_BOUNDS);
        RayTraceResult rtr = super.collisionRayTrace(state, world, pos, origin, direction);
        if (rtr != null) {
            rtr.hitInfo = TubeHitInfo.CENTER;
            bestRTR = rtr;
            bestAABB = getBoundingBox(state, world, pos);
        }

        TileEntityPressureTube tube = getTE(world, pos);
        if (tube == null) return null;
        for (int i = 0; i < 6; i++) {
            if (tube.sidesConnected[i] || tube.sidesClosed[i]) {
                setBlockBounds(tube.sidesClosed[i] ? closedBoundingBoxes[i] : boundingBoxes[i]);
                rtr = super.collisionRayTrace(state, world, pos, origin, direction);
                if (isCloserMOP(origin, bestRTR, rtr)) {
                    rtr.hitInfo = new TubeHitInfo(Direction.byIndex(i), TubeHitInfo.PartType.TUBE);  // tube connection arm
                    bestRTR = rtr;
                    bestAABB = getBoundingBox(state, world, pos);
                }
            }
        }

        TubeModule[] modules = tube.modules;
        for (Direction dir : Direction.VALUES) {
            if (modules[dir.ordinal()] != null) {
                setBlockBounds(modules[dir.ordinal()].boundingBoxes[dir.ordinal()]);
                rtr = super.collisionRayTrace(state, world, pos, origin, direction);
                if (isCloserMOP(origin, bestRTR, rtr)) {
                    rtr.hitInfo = new TubeHitInfo(dir, TubeHitInfo.PartType.MODULE);  // tube module
                    bestRTR = rtr;
                    bestAABB = getBoundingBox(state, world, pos);
                }
            }
        }
        if (bestAABB != null) setBlockBounds(bestAABB);
        return bestRTR;
    }

    private boolean isCloserMOP(Vec3d origin, RayTraceResult originalMOP, RayTraceResult newMOP) {
        return newMOP != null &&
                (originalMOP == null || origin.squareDistanceTo(newMOP.hitVec) < origin.squareDistanceTo(originalMOP.hitVec));
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, World world, BlockPos pos, PlayerEntity player) {
        TubeHitInfo tubeHitInfo = getHitInfo(target);
        if (tubeHitInfo.type == TubeHitInfo.PartType.TUBE) {
            return super.getPickBlock(state, target, world, pos, player);
        } else if (tubeHitInfo.type == TubeHitInfo.PartType.MODULE) {
            TileEntityPressureTube tube = getTE(world, pos);
            if (tube != null) {
                TubeModule module = tube.modules[tubeHitInfo.dir.ordinal()];
                if (module != null) {
                    return new ItemStack(ModuleRegistrator.getModuleItem(module.getType()));
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean onWrenched(World world, PlayerEntity player, BlockPos pos, Direction side, Hand hand) {
        if (player == null) return false;
        TileEntityPressureTube tube = getTE(world, pos);
        if (tube == null) return false;
        TubeModule module = getFocusedModule(world, pos, player);
        if (player.isSneaking()) {
            if (module != null) {
                // detach and drop the module as an item
                if (!player.isCreative()) {
                    for (ItemStack drop : module.getDrops()) {
                        ItemEntity entity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                        entity.setItem(drop);
                        world.addEntity(entity);
                        entity.onCollideWithPlayer(player);
                    }
                }
                tube.setModule(null, module.getDirection());
                neighborChanged(world.getBlockState(pos), world, pos, this, pos.offset(side), false);
                world.notifyNeighborsOfStateChange(pos, this);
            } else {
                // drop the pressure tube as an item
                if (!player.isCreative()) spawnDrops(world.getBlockState(pos), world, pos); //dropBlockAsItem(world, pos, world.getBlockState(pos), 0);
                world.removeBlock(pos, false);
            }
        } else {
            if (module != null) {
                module.onActivated(player, hand);
            } else {
                // close (or reopen) this side of the pipe
                Pair<Boolean, Direction> lookData = getLookedTube(world, pos, player);
                if (lookData != null) {
                    Direction sideHit = lookData.getRight();
                    tube.sidesClosed[sideHit.ordinal()] = !tube.sidesClosed[sideHit.ordinal()];
                    neighborChanged(world.getBlockState(pos), world, pos, this, pos.offset(side), false);
                    world.notifyNeighborsOfStateChange(pos, this);
                }
            }
        }
        ModuleNetworkManager.getInstance(world).invalidateCache();
        return true;
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        for (ItemStack drop : getModuleDrops(getTE(world, pos))) {
            ItemEntity entity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            entity.setItem(drop);
            world.addEntity(entity);
        }
        ModuleNetworkManager.getInstance(world).invalidateCache();
        super.onReplaced(state, world, pos, newState, isMoving);
    }

    private static NonNullList<ItemStack> getModuleDrops(TileEntityPressureTube tube) {
        NonNullList<ItemStack> drops = NonNullList.create();
        if (tube != null) {
            for (TubeModule module : tube.modules) {
                if (module != null) {
                    drops.addAll(module.getDrops());
                }
            }
        }
        return drops;
    }

    @Override
    public void addCollisionBoxToList(BlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        if (getCamoState(worldIn, pos) != null) {
            super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
            return;
        }

        addCollisionBoxToList(pos, entityBox, collidingBoxes, BASE_BOUNDS);

        TileEntityPressureTube tePt = getTE(worldIn, pos);
        if (tePt != null) {
            for (int i = 0; i < 6; i++) {
                if (tePt.sidesConnected[i]) {
                    addCollisionBoxToList(pos, entityBox, collidingBoxes, boundingBoxes[i]);
                }
                if (tePt.modules[i] != null) {
                    addCollisionBoxToList(pos, entityBox, collidingBoxes, tePt.modules[i].boundingBoxes[i]);
                }
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, World par1World, BlockPos pos, Random rand) {
        if (!Config.Client.tubeModuleRedstoneParticles || PneumaticCraftRepressurized.proxy.particleLevel() == 2) return;

        TileEntityPressureTube tePt = getTE(par1World, pos);
        if (tePt != null) {
            int l = 0;
            Direction side = null;
            for (TubeModule module : tePt.modules) {
                if (module != null && module.getRedstoneLevel() > l) {
                    l = module.getRedstoneLevel();
                    side = module.getDirection();
                }
            }
            if (l > 0) {
                double x = pos.getX() + 0.5D + side.getXOffset() * 0.5D + (rand.nextFloat() - 0.5D) * 0.5D;
                double y = pos.getY() + 0.5D + side.getYOffset() * 0.5D + (rand.nextFloat() - 0.5D) * 0.5D;
                double z = pos.getZ() + 0.5D + side.getZOffset() * 0.5D + (rand.nextFloat() - 0.5D) * 0.5D;
                float f = l / 15.0F;
                float dx = f * 0.6F + 0.4F;
                float dy = Math.max(0f, f * f * 0.7F - 0.5F);
                float dz = Math.max(0f, f * f * 0.6F - 0.7F);
                par1World.addParticle(RedstoneParticleData.REDSTONE_DUST, x, y, z, dx, dy, dz);
            }
        }

    }

    @Override
    public int getWeakPower(BlockState state, IBlockAccess par1IBlockAccess, BlockPos pos, Direction side) {
        TileEntityPressureTube tePt = getTE(par1IBlockAccess, pos);
        if (tePt != null) {
            int redstoneLevel = 0;
            for (Direction face : Direction.VALUES) {
                if (tePt.modules[face.ordinal()] != null) {
                    if (side.getOpposite() == face || face != side && tePt.modules[face.ordinal()].isInline()) {//if we are on the same side, or when we have an 'in line' module that is not on the opposite side.
                        redstoneLevel = Math.max(redstoneLevel, tePt.modules[face.ordinal()].getRedstoneLevel());
                    }
                }
            }
            return redstoneLevel;
        }
        return 0;
    }

    @Override
    public boolean canProvidePower(BlockState state) {
        return true;
    }

    @Nonnull
    private static TubeHitInfo getHitInfo(RayTraceResult result) {
        return result != null && result.hitInfo instanceof TubeHitInfo ? (TubeHitInfo) result.hitInfo : TubeHitInfo.NO_HIT;
    }

    /**
     * Stores information about the subpart of a pressure tube that is being looked at or interacted with.
     */
    private static class TubeHitInfo {
        static final TubeHitInfo NO_HIT = new TubeHitInfo(null, null);
        static final TubeHitInfo CENTER = new TubeHitInfo(null, PartType.TUBE);

        enum PartType { TUBE, MODULE }
        final Direction dir;
        final PartType type;

        TubeHitInfo(Direction dir, PartType type) {
            this.dir = dir;
            this.type = type;
        }
    }

    /**
     * Tri-state representing the 3 possible states for a tube connection.
     */
    public enum ConnectionType implements IStringSerializable {
        OPEN("open"),
        CONNECTED("connected"),
        CLOSED("closed");

        private final String name;
        ConnectionType(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
