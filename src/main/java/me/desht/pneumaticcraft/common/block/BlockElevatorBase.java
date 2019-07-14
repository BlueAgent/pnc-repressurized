package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorBase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public class BlockElevatorBase extends BlockPneumaticCraftCamo {

    public BlockElevatorBase() {
        super(Material.IRON, "elevator_base");
    }

    @Override
    public void onBlockAdded(BlockState newState, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onBlockAdded(newState, world, pos, oldState, isMoving);
        TileEntityElevatorBase elevatorBase = getCoreTileEntity(world, pos);
        if (elevatorBase != null) {
            elevatorBase.updateMaxElevatorHeight();
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(BlockPneumaticCraft.NORTH, BlockPneumaticCraft.SOUTH, BlockPneumaticCraft.WEST, BlockPneumaticCraft.EAST);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityElevatorBase.class;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult brtr) {
        return super.onBlockActivated(state, world, getCoreElevatorPos(world, pos), player, hand, brtr);
    }

    private static BlockPos getCoreElevatorPos(World world, BlockPos pos) {
        if (world.getBlockState(pos.offset(Direction.UP)).getBlock() == ModBlocks.ELEVATOR_BASE) {
            return getCoreElevatorPos(world, pos.offset(Direction.UP));
        } else {
            return pos;
        }
    }

    public static TileEntityElevatorBase getCoreTileEntity(World world, BlockPos pos) {
        return (TileEntityElevatorBase) world.getTileEntity(getCoreElevatorPos(world, pos));
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (world.getBlockState(pos.offset(Direction.DOWN)).getBlock() == ModBlocks.ELEVATOR_BASE) {
                TileEntity te = world.getTileEntity(pos.offset(Direction.DOWN));
                ((TileEntityElevatorBase) te).moveUpgradesFromAbove();
            }
            TileEntityElevatorBase elevatorBase = getCoreTileEntity(world, pos);
            if (elevatorBase != null) {
                elevatorBase.updateMaxElevatorHeight();
            }
        }
        super.onReplaced(state, world, pos, newState, isMoving);
    }
}
