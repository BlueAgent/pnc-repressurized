package me.desht.pneumaticcraft.client.render.entity.drone;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.model.ModelMinigun;
import me.desht.pneumaticcraft.client.model.entity.drone.ModelDrone;
import me.desht.pneumaticcraft.client.render.RenderMinigunTracers;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.entity.living.EntityDroneBase;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.math.MathHelper;

public class DroneMinigunLayer extends LayerRenderer<EntityDroneBase, ModelDrone> {
    private final ModelMinigun modelDroneMinigun = new ModelMinigun();

    DroneMinigunLayer(RenderDrone renderer) {
        super(renderer);
    }

    @Override
    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, EntityDroneBase entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entityIn instanceof EntityDrone) {
            EntityDrone drone = (EntityDrone) entityIn;
            if (drone.hasMinigun()) {
                modelDroneMinigun.renderMinigun(matrixStackIn, bufferIn, packedLightIn, LivingRenderer.getPackedOverlay(entityIn, 0.0F), drone.getMinigun(), partialTicks, true);
                renderMinigunTracers(drone, matrixStackIn, bufferIn, partialTicks);
            }
        }
    }

    private void renderMinigunTracers(EntityDrone drone, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, float partialTicks) {
        if (RenderMinigunTracers.shouldRender(drone.getMinigun())) {
            double x = MathHelper.lerp(partialTicks, drone.lastTickPosX, drone.getPosX());
            double y = MathHelper.lerp(partialTicks, drone.lastTickPosY, drone.getPosY());
            double z = MathHelper.lerp(partialTicks, drone.lastTickPosZ, drone.getPosZ());
            matrixStackIn.push();
            matrixStackIn.rotate(Vector3f.XP.rotationDegrees(180));
            matrixStackIn.translate(0, -1.5, 0);
            matrixStackIn.scale(2f, 2f, 2f);
            RenderMinigunTracers.render(drone.getMinigun(), matrixStackIn, bufferIn, x, y, z, 0.6);
            matrixStackIn.pop();
        }
    }
}
