package me.desht.pneumaticcraft.api.client.pneumatic_helmet;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import java.util.List;

/**
 * Implement this class and register it with {@link IPneumaticHelmetRegistry#registerBlockTrackEntry(IBlockTrackEntry)}.
 * Your implementation must provide a no-parameter constructor. For every entity that's applicable for this definition,
 * an instance is created.
 */
public interface IBlockTrackEntry {
    /**
     * This method should return true if the coordinate checked is one that
     * should be tracked. Most entries will just return true when the blockID is
     * the one that they track.
     *
     * @param world The world that is examined.
     * @param pos   The position of the block examined.
     * @param state The block of the current coordinate. This will save you a
     *              call to World.getBlockState().
     * @param te    The TileEntity at this x,y,z.
     * @return true if the coordinate should be tracked by this BlockTrackEntry.
     */
    boolean shouldTrackWithThisEntry(IBlockReader world, BlockPos pos, BlockState state, TileEntity te);

    /**
     * This method controls whether the block should be updated by the server (at 5
     * second intervals). This is specifically aimed at Tile Entities, as the server will
     * send an NBT packet. Return an empty list if no updates are needed, otherwise a
     * list of the block positions for which updates should be sent (in most cases,
     * only the te's own block pos, but potentially others for multiblocks like the
     * vanilla double chest)
     *
     * @param te The TileEntity at the currently checked location.
     * @return true a list of the block positions that should be updated
     */
    List<BlockPos> getServerUpdatePositions(TileEntity te);

    /**
     * The return of this method defines at how many tracked blocks of this type
     * the HUD should stop displaying text at the tracked blocks of this type.
     *
     * @return amount of blocks the HUD should stop displaying the block info.
     */
    int spamThreshold();

    /**
     * This method is called each render tick to retrieve the blocks additional
     * information. The method behaves the same as the addInformation method in
     * the Item class. This method only will be called if
     * shouldTrackWithThisEntry() returned true and the player hovers over the
     * coordinate.
     *
     * @param world    The world the block is in.
     * @param pos      The position the block is at.
     * @param te       The TileEntity at the x,y,z.
     * @param face     The blockface the player is looking at (null if player is not looking directly at the block)
     * @param infoList The list of lines to display.
     */
    void addInformation(World world, BlockPos pos, TileEntity te, Direction face, List<ITextComponent> infoList);
    /**
     * This method is called when displaying the currently tracked blocks.
     * Will be tried to be mapped to the localization file first.
     *
     * @return the name of the group of this entry.
     */
    ResourceLocation getEntryID();

    /**
     * Convenience method: check if the given capability provider provides the given capability.
     *
     * @param provider the capability provider
     * @param cap the capability
     * @return true the provider provides the capability on any face, including the null "face"
     */
    static boolean hasCapabilityOnAnyFace(ICapabilityProvider provider, Capability<?> cap) {
        for (Direction face : Direction.VALUES) {
            if (provider.getCapability(cap, face).isPresent()) return true;
        }
        return provider.getCapability(cap).isPresent();
    }
}
