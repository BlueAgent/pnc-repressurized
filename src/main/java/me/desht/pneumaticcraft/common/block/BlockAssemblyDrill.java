package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyDrill;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

public class BlockAssemblyDrill extends BlockPneumaticCraft {
    private static final VoxelShape BASE_SHAPE = Block.makeCuboidShape(2, 0, 2, 14, 14, 14);
    private static final VoxelShape COLLISION_SHAPE = Block.makeCuboidShape(2, 2, 2, 14, 14, 14);

    public BlockAssemblyDrill() {
        super(Material.IRON, "assembly_drill");
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext) {
        return BASE_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext) {
        return COLLISION_SHAPE;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityAssemblyDrill.class;
    }
}
