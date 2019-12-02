package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.api.recipe.IRefineryRecipe;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityRefineryController;
import me.desht.pneumaticcraft.common.tileentity.TileEntityRefineryOutput;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.ItemHandlerHelper;

public class BlockRefineryOutput extends BlockPneumaticCraft {
    public BlockRefineryOutput() {
        super("refinery_output");
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityRefineryOutput.class;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult brtr) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityRefineryOutput) {
            // normally, activating any refinery block would open the controller TE's gui, but if we
            // activate with a fluid tank in hand (which can actually transfer fluid out),
            // then we should activate the actual refinery output that was clicked
            boolean canTransferFluid = FluidUtil.getFluidHandler(ItemHandlerHelper.copyStackWithSize(player.getHeldItem(hand), 1))
                    .map(heldHandler -> FluidUtil.getFluidHandler(world, pos, brtr.getFace())
                            .map(refineryHandler -> couldTransferFluidOut(heldHandler, refineryHandler))
                            .orElse(false))
                    .orElse(false);
            if (canTransferFluid) {
                return super.onBlockActivated(state, world, pos, player, hand, brtr);
            } else if (!world.isRemote) {
                TileEntityRefineryController master = ((TileEntityRefineryOutput) te).getRefineryController();
                if (master != null) {
                    NetworkHooks.openGui((ServerPlayerEntity) player, master, master.getPos());
                }
            }
            return true;
        }
        return false;
    }

    private boolean couldTransferFluidOut(IFluidHandler h1, IFluidHandler h2) {
        FluidStack f = FluidUtil.tryFluidTransfer(h1, h2, 1000, false);
        return !f.isEmpty();
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        int nOutputs = 0;
        int up = 1, down = 1;
        while (worldIn.getBlockState(pos.up(up++)).getBlock() instanceof BlockRefineryOutput) {
            nOutputs++;
        }
        while (worldIn.getBlockState(pos.down(down++)).getBlock() instanceof BlockRefineryOutput) {
            nOutputs++;
        }
        return nOutputs < IRefineryRecipe.MAX_OUTPUTS  && super.isValidPosition(state, worldIn, pos);
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (!worldIn.isRemote() && facingState.getBlock() == ModBlocks.REFINERY_OUTPUT) {
            recache(worldIn, currentPos);
        }
        return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    private void recache(IWorld world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityRefineryOutput) {
            TileEntityRefineryController teC = ((TileEntityRefineryOutput) te).getRefineryController();
            if (teC != null) teC.cacheRefineryOutputs();
        }
    }
}
