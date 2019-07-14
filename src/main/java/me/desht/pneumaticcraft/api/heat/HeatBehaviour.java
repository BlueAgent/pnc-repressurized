package me.desht.pneumaticcraft.api.heat;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Extend this class, and register it via
 * {@link me.desht.pneumaticcraft.api.tileentity.IHeatRegistry#registerHeatBehaviour(Class heatBehaviour)}
 * <p>
 * This can be used to add heat dependent logic to non-TE's or blocks you don't have access to. For example,
 * PneumaticCraft uses this to power Furnaces with heat, and to turn Lava into Obsidian when heat is drained.
 * This only works for ticking heat logic, not for static heat sources like lava blocks.
 */
public abstract class HeatBehaviour<Tile extends TileEntity> implements INBTSerializable<CompoundNBT> {

    private String id;
    private IHeatExchangerLogic connectedHeatLogic;
    private World world;
    private BlockPos pos;
    private Tile cachedTE;
    private BlockState blockState;
    private Direction direction;  // direction of this behaviour from the tile entity's PoV

    /**
     * Called by the connected IHeatExchangerLogic.
     * @param id ID of this behaviour; can be used to
     * @param connectedHeatLogic
     * @param world
     * @param pos
     * @param direction direction of this behaviour from the tile entity's PoV
     */
    public void initialize(String id, IHeatExchangerLogic connectedHeatLogic, World world, BlockPos pos, Direction direction) {
        this.connectedHeatLogic = connectedHeatLogic;
        this.world = world;
        this.pos = pos;
        this.direction = direction;
        cachedTE = null;
        blockState = null;
    }

    public IHeatExchangerLogic getHeatExchanger() {
        return connectedHeatLogic;
    }

    public World getWorld() {
        return world;
    }

    public BlockPos getPos() {
        return pos;
    }

    public Direction getDirection() {
        return direction;
    }

    public Tile getTileEntity() {
        if (cachedTE == null || cachedTE.isRemoved()) cachedTE = (Tile) world.getTileEntity(pos);
        return cachedTE;
    }

    public BlockState getBlockState() {
        if (blockState == null) blockState = world.getBlockState(pos);
        return blockState;
    }

    /**
     * Unique id for this behaviour. Used in NBT saving. I recommend prefixing it with your modid.
     *
     * @return a unique ID
     */
    public abstract String getId();

    /**
     * Return true when this heat behaviour is applicable for this coordinate. World access methods can be used here
     * (getWorld(), getPos(), getBlock(), getTileEntity()).
     *
     * @return true if this behaviour is applicable here
     */
    public abstract boolean isApplicable();

    /**
     * Called every tick to update this behaviour.
     */
    public abstract void update();

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT tag = new CompoundNBT();
        NBTUtil.writeBlockPos(pos);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        pos = NBTUtil.readBlockPos(nbt);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof HeatBehaviour) {
            HeatBehaviour behaviour = (HeatBehaviour) o;
            return behaviour.getId().equals(getId()) && behaviour.getPos().equals(getPos());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int i = getId().hashCode();
        i = i * 31 + getPos().hashCode();
        return i;
    }
}
