package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityElectrostaticCompressor;
import net.minecraft.tileentity.TileEntity;

public class BlockElectrostaticCompressor extends BlockPneumaticCraftModeled {
    public BlockElectrostaticCompressor() {
        super("electrostatic_compressor");
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityElectrostaticCompressor.class;
    }
}
