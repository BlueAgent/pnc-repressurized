package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.GuiHandler.EnumGuiId;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAdvancedLiquidCompressor;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;

public class BlockAdvancedLiquidCompressor extends BlockPneumaticCraftModeled {

    BlockAdvancedLiquidCompressor() {
        super(Material.IRON, "advanced_liquid_compressor");
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityAdvancedLiquidCompressor.class;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    public EnumGuiId getGuiID() {
        return EnumGuiId.ADVANCED_LIQUID_COMPRESSOR;
    }

}
