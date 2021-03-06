package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.thirdparty.ModdedWrenchUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityDisplayTable;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockDisplayTable extends BlockPneumaticCraft {
    private static final VoxelShape[] SHAPE_CACHE = new VoxelShape[16];

    private static final BooleanProperty NE = BooleanProperty.create("ne");
    private static final BooleanProperty SE = BooleanProperty.create("se");
    private static final BooleanProperty SW = BooleanProperty.create("sw");
    private static final BooleanProperty NW = BooleanProperty.create("nw");

    private static final VoxelShape TOP = makeCuboidShape(0, 14, 0, 16, 16, 16);
    private static final VoxelShape LEG1 = makeCuboidShape(1, 0, 1, 3, 14, 3);
    private static final VoxelShape LEG2 = makeCuboidShape(1, 0, 13, 3, 14, 15);
    private static final VoxelShape LEG3 = makeCuboidShape(13, 0, 1, 15, 14, 3);
    private static final VoxelShape LEG4 = makeCuboidShape(13, 0, 13, 15, 14, 15);

    public BlockDisplayTable() {
        super(ModBlocks.defaultProps());
        setDefaultState(getStateContainer().getBaseState()
                .with(NE, false)
                .with(NW, false)
                .with(SE, false)
                .with(NW, false)
        );
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);

        builder.add(NE, SW, SE, NW);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext ctx) {
        BlockState state = super.getStateForPlacement(ctx);
        boolean[] connected = getConnections(ctx.getWorld(), ctx.getPos(), state);
        return state.with(NE, connected[0]).with(SE, connected[1]).with(SW, connected[2]).with(NW, connected[3]);
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        boolean[] connected = getConnections(worldIn, currentPos, stateIn);
        return stateIn.with(NE, connected[0]).with(SE, connected[1]).with(SW, connected[2]).with(NW, connected[3]);
    }

    private VoxelShape getCachedShape(BlockState state) {
        int shapeIdx = (state.get(NE) ? 1 : 0) | (state.get(SE) ? 2 : 0) | (state.get(SW) ? 4 : 0) | (state.get(NW) ? 8 : 0);
        if (SHAPE_CACHE[shapeIdx] == null) {
            VoxelShape shape = TOP;
            for (Leg corner : Leg.values()) {
                if (!state.get(corner.prop)) {
                    shape = VoxelShapes.or(shape, corner.shape);
                }
            }
            SHAPE_CACHE[shapeIdx] = shape.simplify();
        }
        return SHAPE_CACHE[shapeIdx];
    }

    private boolean[] getConnections(IWorld world, BlockPos pos, BlockState state) {
        boolean[] res = new boolean[4];

        boolean connE = isMatch(world, pos, state, Direction.EAST);
        boolean connW = isMatch(world, pos, state, Direction.WEST);
        boolean connS = isMatch(world, pos, state, Direction.SOUTH);
        boolean connN = isMatch(world, pos, state, Direction.NORTH);

        res[Leg.SE.ordinal()] = connE || connS;
        res[Leg.NE.ordinal()] = connE || connN;
        res[Leg.SW.ordinal()] = connW || connS;
        res[Leg.NW.ordinal()] = connW || connN;

        return res;
    }

    private boolean isMatch(IWorld world, BlockPos pos, BlockState state, Direction dir) {
        BlockState state2 = world.getBlockState(pos.offset(dir));
        return state2.getBlock() == this && getRotation(state) == getRotation(state2);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityDisplayTable.class;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return getCachedShape(state);
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    protected boolean reversePlacementRotation() {
        return true;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult brtr) {
        TileEntity te = world.getTileEntity(pos);
        ItemStack heldStack = player.getHeldItem(hand);
        if (player.isSneaking() || te instanceof INamedContainerProvider || ModdedWrenchUtils.getInstance().isWrench(heldStack)) {
            return super.onBlockActivated(state, world, pos, player, hand, brtr);
        } else if (te instanceof TileEntityDisplayTable) {
            if (!world.isRemote) {
                TileEntityDisplayTable teDT = (TileEntityDisplayTable) te;
                if (teDT.getPrimaryInventory().getStackInSlot(0).isEmpty()) {
                    // try to put the player's held item onto the table
                    ItemStack excess = teDT.getPrimaryInventory().insertItem(0, player.getHeldItem(hand), false);
                    player.setHeldItem(hand, excess);
                } else {
                    // try to remove whatever is on the table
                    ItemStack stack = teDT.getPrimaryInventory().extractItem(0, 64, false);
                    PneumaticCraftUtils.dropItemOnGroundPrecisely(stack, world, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5);
                }
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    private enum Leg {
        NE( 1,-1, BlockDisplayTable.NE, Block.makeCuboidShape(13f, 0,  1f, 15f, 16,  3f)),
        SE( 1, 1, BlockDisplayTable.SE, Block.makeCuboidShape(13f, 0, 13f, 15f, 16, 15f)),
        SW(-1, 1, BlockDisplayTable.SW, Block.makeCuboidShape( 1f, 0, 13f,  3f, 16, 15f)),
        NW(-1,-1, BlockDisplayTable.NW, Block.makeCuboidShape( 1f, 0,  1f,  3f, 16,  3f));

        final int x;
        final int z;
        final BooleanProperty prop;
        final VoxelShape shape;

        Leg(int x, int z, BooleanProperty prop, VoxelShape shape) {
            this.x = x; this.z = z;
            this.prop = prop;
            this.shape = shape;
        }
    }
}
