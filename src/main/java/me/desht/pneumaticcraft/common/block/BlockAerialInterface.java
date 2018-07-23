package me.desht.pneumaticcraft.common.block;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import me.desht.pneumaticcraft.common.GuiHandler.EnumGuiId;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAerialInterface;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

public class BlockAerialInterface extends BlockPneumaticCraft {
    BlockAerialInterface() {
        super(Material.IRON, "aerial_interface");
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityAerialInterface.class;
    }

    @Override
    public EnumGuiId getGuiID() {
        return EnumGuiId.AERIAL_INTERFACE;
    }

    @Override
    public void onBlockPlacedBy(World par1World, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack par6ItemStack) {
        TileEntity te = par1World.getTileEntity(pos);
        if (te instanceof TileEntityAerialInterface && entity instanceof EntityPlayer) {
            ((TileEntityAerialInterface) te).setPlayer(((EntityPlayer) entity));
        }
    }

    @Override
    public boolean canProvidePower(IBlockState state) {
        return true;
    }

    @Override
    public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        TileEntity te = blockAccess.getTileEntity(pos);
        if (te instanceof TileEntityAerialInterface) {
            return ((TileEntityAerialInterface)te ).shouldEmitRedstone() ? 15 : 0;
        }
        return 0;
    }

    /**
     * Produce an peripheral implementation from a block location.
     *
     * @return a peripheral, or null if there is not a peripheral here you'd like to handle.
     * @see dan200.computercraft.api.ComputerCraftAPI#registerPeripheralProvider(IPeripheralProvider)
     */
    @Override
    @Optional.Method(modid = ModIds.COMPUTERCRAFT)
    public IPeripheral getPeripheral(World world, BlockPos pos, EnumFacing side) {
        return side.getAxis() == EnumFacing.Axis.Y ? super.getPeripheral(world, pos, side) : null;
    }
}
