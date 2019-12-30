package me.desht.pneumaticcraft.common.capabilities;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.api.tileentity.IAirListener;
import me.desht.pneumaticcraft.api.tileentity.IManoMeasurable;
import me.desht.pneumaticcraft.client.particle.AirParticleData;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.network.PacketSpawnParticle;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A ticking air handler owned by a tile entity, which disperses air to those neighbouring air handlers
 * which have lower pressure than it does.
 */
public class MachineAirHandler extends BasicAirHandler implements IAirHandlerMachine, IManoMeasurable {
    private final float dangerPressure;
    private final float criticalPressure;
    private float maxPressure;
    private int volumeUpgrades = 0;
    private boolean hasSecurityUpgrade = false;
    private int soundCounter;

    private final List<LazyOptional<IAirHandlerMachine>> neighbourAirHandlers = new ArrayList<>();

    public MachineAirHandler(float dangerPressure, float criticalPressure, int volume) {
        super(volume);
        this.dangerPressure = dangerPressure;
        this.criticalPressure = criticalPressure;
        this.maxPressure = dangerPressure + (criticalPressure - dangerPressure) * (float) Math.random();

        for (Direction ignored : Direction.VALUES) {
            this.neighbourAirHandlers.add(LazyOptional.empty());
        }
    }

    @Override
    public int getVolume() {
        return getBaseVolume() + volumeUpgrades * PneumaticValues.VOLUME_VOLUME_UPGRADE;
    }

    @Override
    public float getDangerPressure() {
        return dangerPressure;
    }

    @Override
    public float getCriticalPressure() {
        return criticalPressure;
    }

    public float getMaxPressure() {
        return maxPressure;
    }

    @Override
    public void setPressure(float pressure) {
        addAir(((int) (pressure * getVolume())) - getAir());
    }

    @Override
    public void setVolumeUpgrades(int newVolumeUpgrades) {
        int newVolume = getBaseVolume() + newVolumeUpgrades * PneumaticValues.VOLUME_VOLUME_UPGRADE;
        if (newVolume < getVolume()) {
            // a decrease in volume causes a proportionate decrease in air amount to keep the pressure constant
            int newAir = (int) (getAir() * (float) newVolume / getVolume());
            addAir(newAir - getAir());
        }
        this.volumeUpgrades = newVolumeUpgrades;
    }

    @Override
    public void setHasSecurityUpgrade(boolean hasSecurityUpgrade) {
        this.hasSecurityUpgrade = hasSecurityUpgrade;
    }

    @Override
    public void invalidateNeighbours() {
        // TODO need to get the side passed in here too
        for (int i = 0; i < neighbourAirHandlers.size(); i++) {
            neighbourAirHandlers.set(i, LazyOptional.empty());
        }
//        neighbourAirHandlers.set(side.getIndex(), null);
    }

    @Override
    public void tick(TileEntity ownerTE) {
        World world = ownerTE.getWorld();
        if (!world.isRemote) {
            BlockPos pos = ownerTE.getPos();
            if (hasSecurityUpgrade) {
                doSecurityAirChecks(ownerTE);
            }

            float p = getPressure();
            if (p > maxPressure) {
                world.createExplosion(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 1.0F, Explosion.Mode.BREAK);
                world.destroyBlock(pos, false);
                return;
            } else if (p > dangerPressure) {
                float r = criticalPressure - dangerPressure;
                float d = p - dangerPressure;
                if (world.rand.nextFloat() * r < d / 50.0f) {
                    world.playSound(null, ownerTE.getPos(), ModSounds.CREAK, SoundCategory.BLOCKS, 0.7f, 0.6f + world.rand.nextFloat() * 0.8f);
                }
            }
            disperseAir(ownerTE);
        }
        if (soundCounter > 0) soundCounter--;
    }

    private void doSecurityAirChecks(TileEntity ownerTE) {
        if (getPressure() >= dangerPressure - 0.1) {
            airLeak(ownerTE, Direction.UP);
        }

        // Remove any remaining air
        int excessAir = getAir() - (int) (getVolume() * (dangerPressure - 0.1));
        if (excessAir > 0) {
            addAir(-excessAir);
            onAirDispersion(ownerTE,null, -excessAir);
        }
    }

    @Override
    public void airLeak(TileEntity ownerTE, Direction dir) {
        World world = ownerTE.getWorld();
        BlockPos pos = ownerTE.getPos();

        if (world.isRemote || Math.abs(getPressure()) < 0.01F) return;
        double motionX = dir.getXOffset();
        double motionY = dir.getYOffset();
        double motionZ = dir.getZOffset();
        if (soundCounter <= 0) {
            float pitch = MathHelper.clamp(1.0f + ((getPressure() - 3) / 10), 0.8f, 1.2f);
            soundCounter = (int) (20 / pitch);
            NetworkHandler.sendToAllAround(new PacketPlaySound(ModSounds.LEAKING_GAS, SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), 0.1F, pitch, true), world);
        }

        if (getPressure() < 0) {
            double speed = getPressure() * 0.1F - 0.1F;
            NetworkHandler.sendToAllAround(new PacketSpawnParticle(AirParticleData.DENSE, pos.getX() + 0.5D + motionX / 2D, pos.getY() + 0.5D + motionY / 2D, pos.getZ() + 0.5D + motionZ / 2D, motionX * speed, motionY * speed, motionZ * speed), world);
            int dispersedAmount = -(int) (getPressure() * PneumaticValues.AIR_LEAK_FACTOR) + 20;
            if (getAir() > dispersedAmount) dispersedAmount = -getAir();
            onAirDispersion(ownerTE, dir, dispersedAmount);
            addAir(dispersedAmount);
        } else {
            double speed = getPressure() * 0.1F + 0.1F;
            NetworkHandler.sendToAllAround(new PacketSpawnParticle(AirParticleData.DENSE, pos.getX() + 0.5D + motionX / 2D, pos.getY() + 0.5D + motionY / 2D, pos.getZ() + 0.5D + motionZ / 2D, motionX * speed, motionY * speed, motionZ * speed), world);
            int dispersedAmount = (int) (getPressure() * PneumaticValues.AIR_LEAK_FACTOR) + 20;
            if (dispersedAmount > getAir()) dispersedAmount = getAir();
            onAirDispersion(ownerTE, dir, -dispersedAmount);
            addAir(-dispersedAmount);
        }
    }

    private LazyOptional<IAirHandlerMachine> getNeighbourAirHandler(TileEntity ownerTE, Direction dir) {
        final int idx = dir.getIndex();
        if (!neighbourAirHandlers.get(idx).isPresent()) {
            TileEntity te1 = ownerTE.getWorld().getTileEntity(ownerTE.getPos().offset(dir));
            if (te1 != null) {
                neighbourAirHandlers.set(idx, te1.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, dir.getOpposite()));
                neighbourAirHandlers.get(idx).addListener(l -> neighbourAirHandlers.set(idx, LazyOptional.empty()));
            } else {
                neighbourAirHandlers.set(idx, LazyOptional.empty());
            }
        }
        return neighbourAirHandlers.get(idx);
    }

    private void disperseAir(TileEntity ownerTE) {
        // 1. build a list of all neighbouring and otherwise connected air handlers with a lower pressure than us
        List<IAirHandlerMachine.Connection> neighbours = getConnectedAirHandlers(ownerTE, true);

        // 2. get the total volume and air amount in this and all connected handlers
        int totalVolume = this.getVolume();
        int totalAir = this.getAir();
        for (IAirHandlerMachine.Connection neighbour : neighbours) {
            totalVolume += neighbour.getAirHandler().getVolume();
            totalAir += neighbour.getAirHandler().getAir();
        }

        // 3. figure out how much air will be dispersed to each neighbour
        for (IAirHandlerMachine.Connection neighbour: neighbours) {
            int totalMachineAir = (int) ((long) totalAir * neighbour.getAirHandler().getVolume() / totalVolume);
            neighbour.setMaxDispersion(getMaxDispersion(ownerTE, neighbour.getDirection()));
            neighbour.setAirToDisperse(totalMachineAir - neighbour.getAirHandler().getAir());
        }

        // 4. finally, actually disperse the air
        for (IAirHandlerMachine.Connection neighbour : neighbours) {
            int air = Math.min(neighbour.getMaxDispersion(), neighbour.getDispersedAir());
            onAirDispersion(ownerTE, neighbour.getDirection(), air);
            neighbour.getAirHandler().addAir(air);
            addAir(-air);
        }
    }

    private List<Connection> getConnectedAirHandlers(TileEntity ownerTE, boolean onlyLowerPressure) {
        List<IAirHandlerMachine.Connection> neighbours = new ArrayList<>();
        for (Direction d : Direction.VALUES) {
            getNeighbourAirHandler(ownerTE, d).ifPresent(h -> {
                if (ownerTE.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, d).isPresent()
                        && (!onlyLowerPressure || h.getPressure() < getPressure())) {
                    neighbours.add(new ConnectedAirHandler(d, h));
                }
            });
        }
        neighbours.addAll(addExtraConnectedHandlers(ownerTE).stream()
                .filter(h -> !onlyLowerPressure || h.getPressure() < getPressure())
                .map(ConnectedAirHandler::new)
                .collect(Collectors.toList()));
        return neighbours;
    }

    @Override
    public List<IAirHandlerMachine.Connection> getConnectedAirHandlers(TileEntity ownerTE) {
        return getConnectedAirHandlers(ownerTE, false);
    }

    private List<IAirHandlerMachine> addExtraConnectedHandlers(TileEntity ownerTE) {
        if (ownerTE instanceof IAirListener) {
            return ((IAirListener) ownerTE).addConnectedPneumatics(new ArrayList<>());
        }
        return Collections.emptyList();
    }

    private void onAirDispersion(TileEntity ownerTE, Direction dir, int airDispersed) {
        if (ownerTE instanceof IAirListener) {
            ((IAirListener) ownerTE).onAirDispersion(this, dir, airDispersed);
        }
    }

    private int getMaxDispersion(TileEntity ownerTE, Direction dir) {
        if (ownerTE instanceof IAirListener) {
            return ((IAirListener) ownerTE).getMaxDispersion(this, dir);
        } else {
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public void printManometerMessage(PlayerEntity player, List<ITextComponent> curInfo) {
        curInfo.add(new TranslationTextComponent("gui.tooltip.pressure",
                PneumaticCraftUtils.roundNumberTo(getPressure(), 1)));
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT tag = super.serializeNBT();
        tag.putFloat("maxPressure", maxPressure);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        maxPressure = nbt.getFloat("maxPressure");
        super.deserializeNBT(nbt);
    }

    private static class ConnectedAirHandler implements IAirHandlerMachine.Connection {
        final Direction direction; // may be null
        final IAirHandlerMachine airHandler;
        int maxDispersion;
        int toDisperse;

        ConnectedAirHandler(Direction direction, IAirHandlerMachine airHandler) {
            this.direction = direction;
            this.airHandler = airHandler;
        }

        ConnectedAirHandler(IAirHandlerMachine airHandler) {
            this(null, airHandler);
        }

        @Override
        public Direction getDirection() {
            return direction;
        }

        @Override
        public int getMaxDispersion() {
            return maxDispersion;
        }

        public void setMaxDispersion(int maxDispersion) {
            this.maxDispersion = maxDispersion;
        }

        @Override
        public int getDispersedAir() {
            return toDisperse;
        }

        public void setAirToDisperse(int toDisperse) {
            this.toDisperse = toDisperse;
        }

        @Override
        public IAirHandlerMachine getAirHandler() {
            return airHandler;
        }
    }
}
