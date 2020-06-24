package me.desht.pneumaticcraft.common.tileentity;

import com.mojang.datafixers.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraftforge.common.util.Constants;

/**
 * Implement this interface in tile entities which should store a camouflaged state.  The corresponding block should
 * be a subclass of {@link me.desht.pneumaticcraft.common.block.BlockPneumaticCraftCamo}
 */
public interface ICamouflageableTE {
    /**
     * Get the camouflage state; the blockstate which will be used to render this tile entity's block.
     *
     * @return the camouflage state, or null if the block should not be camouflaged
     */
    BlockState getCamouflage();

    /**
     * Set the camouflage for the tile entity.  The tile entity should sync this state to the client, and do
     * any necessary re-rendering of the block when the synced state changes.
     *
     * @param state the camo block state
     */
    void setCamouflage(BlockState state);

    /**
     * Convenience method: get the itemstack for the given block state.
     *
     * @param state the block state
     * @return an item for that block state
     */
    static ItemStack getStackForState(BlockState state) {
        return state == null ? ItemStack.EMPTY : new ItemStack(state.getBlock().asItem());
    }

    /**
     * Convenience method: sync camo state to client
     *
     * @param te the tile entity
     */
    static void syncToClient(TileEntityBase te) {
        if (te.getWorld() != null && !te.getWorld().isRemote) {
            te.sendDescriptionPacket();
            te.markDirty();
        }
    }

    static BlockState readCamo(CompoundNBT tag) {
        if (tag.contains("camoState", Constants.NBT.TAG_COMPOUND)) {
            return BlockState.deserialize(new Dynamic<>(NBTDynamicOps.INSTANCE, tag.getCompound("camoState")));
        } else if (tag.contains("camoStack", Constants.NBT.TAG_COMPOUND)) {
            // TODO remove this in 1.16: migrating from old-style itemstack storage
            ItemStack stack = ItemStack.read(tag.getCompound("camoStack"));
            if (stack.getItem() instanceof BlockItem) {
                return (((BlockItem) stack.getItem()).getBlock()).getDefaultState();
            }
        }
        return null;
    }

    static void writeCamo(CompoundNBT tag, BlockState state) {
        if (state != null) {
            Dynamic<INBT> ops = BlockState.serialize(NBTDynamicOps.INSTANCE, state);
            tag.put("camoState", ops.getValue());
        }
    }
}
