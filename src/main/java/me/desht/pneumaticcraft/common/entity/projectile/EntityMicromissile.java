package me.desht.pneumaticcraft.common.entity.projectile;

import me.desht.pneumaticcraft.client.particle.AirParticleData;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModEntityTypes;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.item.ItemMicromissiles;
import me.desht.pneumaticcraft.common.item.ItemMicromissiles.FireMode;
import me.desht.pneumaticcraft.common.util.EntityFilter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.horse.HorseEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.Comparator;
import java.util.List;

public class EntityMicromissile extends ThrowableEntity {
    private static final double SEEK_RANGE = 24;

    private static final DataParameter<Integer> TARGET_ID = EntityDataManager.createKey(EntityMicromissile.class, DataSerializers.VARINT);
    private static final DataParameter<Float> MAX_VEL_SQ = EntityDataManager.createKey(EntityMicromissile.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> ACCEL = EntityDataManager.createKey(EntityMicromissile.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> TURN_SPEED = EntityDataManager.createKey(EntityMicromissile.class, DataSerializers.FLOAT);

    private Entity targetEntity = null;

    private float maxVelocitySq = 0.5f;
    private float accel = 1.05f; // straight line acceleration
    private float turnSpeed = 0.1f;
    private float explosionPower = 2f;
    private EntityFilter entityFilter;
    private boolean outOfFuel = false;
    private FireMode fireMode = FireMode.SMART;

    public static Entity create(EntityType<Entity> entityEntityType, World world) {
        return new EntityMicromissile(world);
    }

    public static Entity createClient(FMLPlayMessages.SpawnEntity spawnEntity, World world) {
        return new EntityMicromissile(world);
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public EntityMicromissile(World worldIn) {
        super(ModEntityTypes.MICROMISSILE, worldIn);
    }

    public EntityMicromissile(World worldIn, LivingEntity thrower, ItemStack iStack) {
        super(ModEntityTypes.MICROMISSILE, thrower, worldIn);

        if (iStack.hasTag()) {
            CompoundNBT tag = iStack.getTag();
            entityFilter = EntityFilter.fromString(tag.getString(ItemMicromissiles.NBT_FILTER));
            fireMode = FireMode.fromString(tag.getString(ItemMicromissiles.NBT_FIRE_MODE));
            switch (fireMode) {
                case SMART:
                    accel = Math.max(1.02f, 1.0f + tag.getFloat(ItemMicromissiles.NBT_TOP_SPEED) / 10f);
                    maxVelocitySq = (float) Math.pow(0.25 + tag.getFloat(ItemMicromissiles.NBT_TOP_SPEED) * 3.75f, 2);
                    turnSpeed = 0.4f * tag.getFloat(ItemMicromissiles.NBT_TURN_SPEED);
                    explosionPower = Math.max(1f, 5 * tag.getFloat(ItemMicromissiles.NBT_DAMAGE));
                    break;
                case DUMB:
                    accel = 1.02f;
                    maxVelocitySq = 2f;
                    turnSpeed = 0f;
                    explosionPower = 3f;
                    break;
            }
        }
    }

    public EntityMicromissile(World worldIn, double x, double y, double z) {
        this(worldIn);
        this.setPosition(x, y, z);
    }

    @Override
    protected void registerData() {
        dataManager.register(TARGET_ID, 0);
        dataManager.register(MAX_VEL_SQ, 0.5f);
        dataManager.register(ACCEL, 1.05f);
        dataManager.register(TURN_SPEED, 0.4f);
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        if (getEntityWorld().isRemote) {
            if (key.equals(MAX_VEL_SQ)) {
                maxVelocitySq = dataManager.get(MAX_VEL_SQ);
            } else if (key.equals(TARGET_ID)) {
                int id = dataManager.get(TARGET_ID);
                targetEntity = id > 0 ? getEntityWorld().getEntityByID(dataManager.get(TARGET_ID)) : null;
            } else if (key.equals(ACCEL)) {
                accel = dataManager.get(ACCEL);
            } else if (key.equals(TURN_SPEED)) {
                turnSpeed = dataManager.get(TURN_SPEED);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (ticksExisted == 1) {
            if (getEntityWorld().isRemote) {
                getEntityWorld().playSound(posX, posY, posZ, SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.PLAYERS, 1.0f, 0.8f, true);
            } else {
                dataManager.set(MAX_VEL_SQ, maxVelocitySq);
                dataManager.set(ACCEL, accel);
                dataManager.set(TURN_SPEED, turnSpeed);
            }
        }

        if (ticksExisted > PNCConfig.Common.Micromissiles.lifetime) {
            outOfFuel = true;
        }

        if (!outOfFuel) {
            // negate default slowdown of projectiles applied in superclass
            if (this.isInWater()) {
                setMotion(getMotion().scale(1.25));
            } else {
                setMotion(getMotion().scale(1 / 0.99));
            }

            if ((targetEntity == null || !targetEntity.isAlive()) && fireMode == FireMode.SMART && !getEntityWorld().isRemote && (ticksExisted & 0x3) == 0) {
                targetEntity = tryFindNewTarget();
            }

            if (targetEntity != null) {
                // turn toward the target
                Vec3d diff = targetEntity.getPositionVector().add(0, targetEntity.getEyeHeight(), 0).subtract(getPositionVector()).normalize().scale(turnSpeed);
                setMotion(getMotion().add(diff));
            }

            // accelerate up to max velocity but cap there
            double velSq = getMotion().lengthSquared();//motionX * motionX + motionY * motionY + motionZ * motionZ;
            double mul = velSq > maxVelocitySq ? maxVelocitySq / velSq : accel;
            setMotion(getMotion().scale(mul));

            if (getEntityWorld().isRemote) {
                Vec3d m = getMotion();
                world.addParticle(AirParticleData.DENSE, posX, posY, posZ, -m.x/2, -m.y/2, -m.z/2);
            }
        }
    }

    private Entity tryFindNewTarget() {
        AxisAlignedBB aabb = new AxisAlignedBB(posX, posY, posZ, posX, posY, posZ).grow(SEEK_RANGE);
        List<Entity> l = getEntityWorld().getEntitiesWithinAABB(LivingEntity.class, aabb, EntityPredicates.IS_ALIVE);
        l.sort(new TargetSorter());
        Entity tgt = null;
        // find the closest entity which matches this missile's entity filter
        for (Entity e : l) {
            if (isValidTarget(e) && e.getDistanceSq(this) < SEEK_RANGE * SEEK_RANGE) {
                RayTraceContext ctx = new RayTraceContext(getPositionVector(), e.getPositionVector().add(0, e.getEyeHeight(), 0), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, e);
                RayTraceResult res = getEntityWorld().rayTraceBlocks(ctx);
                if (res.getType() == RayTraceResult.Type.MISS || res.getType() == RayTraceResult.Type.ENTITY) {
                    tgt = e;
                    break;
                }
            }
        }
        dataManager.set(TARGET_ID, tgt == null ? 0 : tgt.getEntityId());
        return tgt;
    }

    public boolean isValidTarget(Entity e) {
        // never target the player who fired the missile or any of their pets/drones
        LivingEntity thrower = getThrower();
        if (thrower != null) {
            if (e.equals(thrower)
                    || e instanceof TameableEntity && thrower.equals(((TameableEntity) e).getOwner())
                    || e instanceof EntityDrone && thrower.getUniqueID().toString().equals(((EntityDrone) e).getOwnerUUID())
                    || e instanceof HorseEntity && thrower.getUniqueID().equals(((HorseEntity) e).getOwnerUniqueId())) {
                return false;
            }
        }

        if (entityFilter != null && !entityFilter.test(e)) {
            return false;
        }

        return e instanceof LivingEntity || e instanceof BoatEntity || e instanceof AbstractMinecartEntity;
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (ticksExisted > 5 && !getEntityWorld().isRemote && isAlive()) {
            explode();
        }
    }

    private void explode() {
        remove();
        Explosion.Mode mode = PNCConfig.Common.Micromissiles.damageTerrain ? Explosion.Mode.BREAK : Explosion.Mode.NONE;
        getEntityWorld().createExplosion(this, posX, posY, posZ, (float) PNCConfig.Common.Micromissiles.baseExplosionDamage * explosionPower, false, mode);
    }

    @Override
    public void shoot(Entity entityThrower, float pitch, float yaw, float pitchOffset, float velocity, float inaccuracy) {
        float x = -MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
        float y = -MathHelper.sin(pitch * 0.017453292F);
        float z = MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
        this.shoot((double)x, (double)y, (double)z, velocity, 0f);
        setMotion(getMotion().add(entityThrower.getMotion().x, 0, entityThrower.getMotion().z));
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        double f = Math.sqrt(x * x + y * y + z * z);
        x = x / f * velocity;
        y = y / f * velocity;
        z = z / f * velocity;
        setMotion(x, y, z);

        float f1 = MathHelper.sqrt(x * x + z * z);
        this.rotationYaw = (float)(MathHelper.atan2(x, z) * (180D / Math.PI));
        this.rotationPitch = (float)(MathHelper.atan2(y, (double)f1) * (180D / Math.PI));
        this.prevRotationYaw = this.rotationYaw;
        this.prevRotationPitch = this.rotationPitch;
    }

    @Override
    protected float getGravityVelocity() {
        return outOfFuel ? super.getGravityVelocity() : 0f;
    }

    @Override
    public boolean hasNoGravity() {
        return !outOfFuel;
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putFloat("turnSpeed", turnSpeed);
        compound.putFloat("explosionScaling", explosionPower);
        compound.putFloat("topSpeedSq", maxVelocitySq);
        compound.putString("filter", entityFilter.toString());
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        turnSpeed = compound.getFloat("turnSpeed");
        explosionPower = compound.getFloat("explosionScaling");
        maxVelocitySq = compound.getFloat("topSpeedSq");
        entityFilter = EntityFilter.fromString(compound.getString("filter"));
    }

    public void setTarget(Entity target) {
        targetEntity = target;
    }

    private class TargetSorter implements Comparator<Entity> {
        private final Vec3d vec;

        TargetSorter() {
            vec = new Vec3d(posX, posY, posZ);
        }

        @Override
        public int compare(Entity e1, Entity e2) {
            return Double.compare(vec.squareDistanceTo(e1.getPositionVector()), vec.squareDistanceTo(e2.getPositionVector()));
        }
    }
}
