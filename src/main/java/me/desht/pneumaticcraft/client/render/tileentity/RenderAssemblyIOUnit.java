package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.client.model.block.ModelAssemblyIOUnit;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyIOUnit;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.ResourceLocation;

public class RenderAssemblyIOUnit extends AbstractModelRenderer<TileEntityAssemblyIOUnit> {
    private final ModelAssemblyIOUnit model;

    public RenderAssemblyIOUnit() {
        model = new ModelAssemblyIOUnit();
    }

    @Override
    ResourceLocation getTexture(TileEntityAssemblyIOUnit te) {
        return te != null && te.isImportUnit() ? Textures.MODEL_ASSEMBLY_IO_IMPORT : Textures.MODEL_ASSEMBLY_IO_EXPORT;
    }

    @Override
    void renderModel(TileEntityAssemblyIOUnit te, float partialTicks) {
        if (te != null) {
            float[] renderAngles = new float[5];
            for (int i = 0; i < 5; i++) {
                renderAngles[i] = te.oldAngles[i] + (te.angles[i] - te.oldAngles[i]) * partialTicks;
            }

            EntityItem ghostEntityItem = null;
            if (!te.getPrimaryInventory().getStackInSlot(0).isEmpty()) {
                ghostEntityItem = new EntityItem(te.getWorld());
                ghostEntityItem.hoverStart = 0.0F;
                ghostEntityItem.setItem(te.getPrimaryInventory().getStackInSlot(0));
            }
            RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
            boolean fancySetting = renderManager.options.fancyGraphics;
            renderManager.options.fancyGraphics = true;
            model.renderModel(0.0625F, renderAngles, te.oldClawProgress + (te.clawProgress - te.oldClawProgress) * partialTicks, ghostEntityItem);
            renderManager.options.fancyGraphics = fancySetting;
        } else {
            model.renderModel(0.0625F, new float[]{0, 0, 35, 55, 0}, 0, null);
        }
    }
}
