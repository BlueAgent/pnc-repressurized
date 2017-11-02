package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityCompressedIronBlock;
import me.desht.pneumaticcraft.common.tileentity.TileEntityLiquidHopper;
import me.desht.pneumaticcraft.common.tileentity.TileEntityUVLightBox;
import me.desht.pneumaticcraft.common.tileentity.TileEntityVortexTube;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlockColorHandler {
    public static void registerColorHandlers() {
        final IBlockColor heatColor = (state, blockAccess, pos, tintIndex) -> {
            if (blockAccess != null && pos != null) {
                TileEntity te = blockAccess.getTileEntity(pos);
                int heatLevel = 10;
                if (te instanceof TileEntityCompressedIronBlock) {
                    heatLevel = ((TileEntityCompressedIronBlock) te).getHeatLevel();
                } else if (te instanceof TileEntityVortexTube) {
                    switch (tintIndex) {
                        case 0: heatLevel = ((TileEntityVortexTube) te).getHotHeatLevel(); break;
                        case 1: heatLevel = ((TileEntityVortexTube) te).getColdHeatLevel(); break;
                    }
                }
                double[] color = TileEntityCompressedIronBlock.getColorForHeatLevel(heatLevel);
                return 0xFF000000 + ((int) (color[0] * 255) << 16) + ((int) (color[1] * 255) << 8) + (int) (color[2] * 255);
            }
            return 0xFFFFFFFF;
        };
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(heatColor,
                Blockss.COMPRESSED_IRON, Blockss.HEAT_SINK, Blockss.VORTEX_TUBE);

        final IBlockColor uvLightBoxLampColor = (state, blockAccess, pos, tintIndex) -> {
            if (blockAccess != null && pos != null) {
                TileEntity te = blockAccess.getTileEntity(pos);
                if (te instanceof TileEntityUVLightBox) {
                    return ((TileEntityUVLightBox) te).areLightsOn ? 0xFF4000FF : 0xFFAFAFE4;
                }
            }
            return 0xFFAFAFE4;
        };
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(uvLightBoxLampColor, Blockss.UV_LIGHT_BOX);

        final IBlockColor liquidHopperColor = (state, blockAccess, pos, tintIndex) -> {
            if (blockAccess != null && pos != null) {
                TileEntity te = blockAccess.getTileEntity(pos);
                if (te instanceof TileEntityLiquidHopper) {
                    FluidStack fluidStack = ((TileEntityLiquidHopper) te).getTank().getFluid();
                    if (fluidStack != null && fluidStack.amount > 0) {
                        return fluidStack.getFluid().getColor();
                    }
                }
            }
            return 0xFFFFFFFF;
        };
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(liquidHopperColor, Blockss.LIQUID_HOPPER);
    }

}
