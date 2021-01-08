package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockSecurityStation extends BlockPneumaticCraft {
    private static final VoxelShape BODY = Block.makeCuboidShape(1, 8, 1, 15, 11, 15);
    private static final VoxelShape LEG1 = Block.makeCuboidShape(1, 0, 1, 3, 8, 3);
    private static final VoxelShape LEG2 = Block.makeCuboidShape(13, 0, 13, 15, 8, 15);
    private static final VoxelShape LEG3 = Block.makeCuboidShape(1, 0, 13, 3, 8, 15);
    private static final VoxelShape LEG4 = Block.makeCuboidShape(13, 0, 1, 15, 8, 3);
    private static final VoxelShape SHAPE = VoxelShapes.or(BODY, LEG1, LEG2, LEG3, LEG4);

    public BlockSecurityStation() {
        super(ModBlocks.defaultProps());
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntitySecurityStation.class;
    }

    /**
     * Called when the block is placed in the world.
     */
    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity entityLiving, ItemStack iStack) {
        if (entityLiving instanceof PlayerEntity) {
            PneumaticCraftUtils.getTileEntityAt(world, pos, TileEntitySecurityStation.class)
                    .ifPresent(te -> te.sharedUsers.add(((PlayerEntity) entityLiving).getGameProfile()));
        }

        super.onBlockPlacedBy(world, pos, state, entityLiving, iStack);
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult brtr) {
        if (player.isSneaking()) {
            return ActionResultType.PASS;
        } else {
            if (!world.isRemote) {
                TileEntity te = world.getTileEntity(pos);
                if (te instanceof TileEntitySecurityStation) {
                    TileEntitySecurityStation teSS = (TileEntitySecurityStation) te;
                    if (teSS.isPlayerOnWhiteList(player)) {
                        return super.onBlockActivated(state, world, pos, player, hand, brtr);
                    } else if (!teSS.hasValidNetwork()) {
                        player.sendStatusMessage(PneumaticCraftUtils.xlate("pneumaticcraft.message.securityStation.outOfOrder").mergeStyle(TextFormatting.RED), false);
                    } else if (teSS.hasPlayerHacked(player)) {
                        player.sendStatusMessage(PneumaticCraftUtils.xlate("pneumaticcraft.message.securityStation.alreadyHacked").mergeStyle(TextFormatting.GOLD), false);
                    } else if (getPlayerHackLevel(player) < teSS.getSecurityLevel()) {
                        player.sendStatusMessage(PneumaticCraftUtils.xlate("pneumaticcraft.message.securityStation.cantHack").mergeStyle(TextFormatting.GOLD), false);
                        player.attackEntityFrom(DamageSource.OUT_OF_WORLD, 1f);
                    } else {
                        teSS.initiateHacking(player);
                    }
                }
            }
            return ActionResultType.SUCCESS;
        }
    }

    private int getPlayerHackLevel(PlayerEntity player) {
        CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
        return handler.isArmorReady(EquipmentSlotType.HEAD) && handler.getArmorPressure(EquipmentSlotType.HEAD) > 0f ?
                handler.getUpgradeCount(EquipmentSlotType.HEAD, EnumUpgrade.SECURITY) : 0;
    }

    @Override
    public boolean canProvidePower(BlockState state) {
        return true;
    }

    @Override
    public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return PneumaticCraftUtils.getTileEntityAt(blockAccess, pos, TileEntitySecurityStation.class)
                .map(teSS -> teSS.getRedstoneController().shouldEmit() ? 15 : 0).orElse(0);
    }
}
