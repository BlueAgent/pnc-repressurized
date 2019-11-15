package me.desht.pneumaticcraft.client.model.block;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.render.tileentity.AbstractTileModelRenderer;
import net.minecraft.client.renderer.entity.model.RendererModel;

public class ModelAssemblyDrill extends AbstractTileModelRenderer.BaseModel {
    private final RendererModel baseTurn;
    private final RendererModel baseTurn2;
    private final RendererModel armBase1;
    private final RendererModel armBase2;
    private final RendererModel supportMiddle;
    private final RendererModel armMiddle1;
    private final RendererModel armMiddle2;
    private final RendererModel drillBase;
    private final RendererModel drill;

    public ModelAssemblyDrill() {
        textureWidth = 64;
        textureHeight = 64;

        baseTurn = new RendererModel(this, 0, 17);
        baseTurn.addBox(0F, 0F, 0F, 7, 1, 7);
        baseTurn.setRotationPoint(-3.5F, 22F, -3.5F);
        baseTurn.setTextureSize(64, 32);
        baseTurn.mirror = true;
        setRotation(baseTurn, 0F, 0F, 0F);
        baseTurn2 = new RendererModel(this, 28, 17);
        baseTurn2.addBox(0F, 0F, 0F, 4, 5, 4);
        baseTurn2.setRotationPoint(-2F, 17F, -2F);
        baseTurn2.setTextureSize(64, 32);
        baseTurn2.mirror = true;
        setRotation(baseTurn2, 0F, 0F, 0F);
        armBase1 = new RendererModel(this, 0, 25);
        armBase1.addBox(0F, 0F, 0F, 1, 2, 8);
        armBase1.setRotationPoint(2F, 17F, -1F);
        armBase1.setTextureSize(64, 32);
        armBase1.mirror = true;
        setRotation(armBase1, 0F, 0F, 0F);
        armBase2 = new RendererModel(this, 0, 25);
        armBase2.addBox(0F, 0F, 0F, 1, 2, 8);
        armBase2.setRotationPoint(-3F, 17F, -1F);
        armBase2.setTextureSize(64, 32);
        armBase2.mirror = true;
        setRotation(armBase2, 0F, 0F, 0F);
        supportMiddle = new RendererModel(this, 0, 57);
        supportMiddle.addBox(0F, 0F, 0F, 2, 1, 1);
        supportMiddle.setRotationPoint(-1F, 17.5F, 5.5F);
        supportMiddle.setTextureSize(64, 32);
        supportMiddle.mirror = true;
        setRotation(supportMiddle, 0F, 0F, 0F);
        armMiddle1 = new RendererModel(this, 0, 35);
        armMiddle1.addBox(0F, 0F, 0F, 1, 17, 2);
        armMiddle1.setRotationPoint(-2F, 2F, 5F);
        armMiddle1.setTextureSize(64, 32);
        armMiddle1.mirror = true;
        setRotation(armMiddle1, 0F, 0F, 0F);
        armMiddle2 = new RendererModel(this, 0, 35);
        armMiddle2.addBox(0F, 0F, 0F, 1, 17, 2);
        armMiddle2.setRotationPoint(1F, 2F, 5F);
        armMiddle2.setTextureSize(64, 32);
        armMiddle2.mirror = true;
        setRotation(armMiddle2, 0F, 0F, 0F);
        drillBase = new RendererModel(this, 8, 38);
        drillBase.addBox(0F, 0F, 0F, 2, 2, 3);
        drillBase.setRotationPoint(-1F, 2F, 4.5F);
        drillBase.setTextureSize(64, 32);
        drillBase.mirror = true;
        setRotation(drillBase, 0F, 0F, 0F);
        drill = new RendererModel(this, 23, 54);
        drill.addBox(0F, 0F, 0F, 1, 1, 4);
        drill.setRotationPoint(-0.5F, 2.5F, 1F);
        drill.setTextureSize(64, 32);
        drill.mirror = true;
        setRotation(drill, 0F, 0F, 0F);
    }

    public void renderModel(float size, float[] angles) {
        GlStateManager.pushMatrix();
        GlStateManager.rotated(angles[0], 0, 1, 0);
        baseTurn.render(size);
        baseTurn2.render(size);
        GlStateManager.translated(0, 18 / 16F, 0);
        GlStateManager.rotated(angles[1], 1, 0, 0);
        GlStateManager.translated(0, -18 / 16F, 0);
        armBase1.render(size);
        armBase2.render(size);
        supportMiddle.render(size);
        GlStateManager.translated(0, 18 / 16F, 6 / 16F);
        GlStateManager.rotated(angles[2], 1, 0, 0);
        GlStateManager.translated(0, -18 / 16F, -6 / 16F);
        armMiddle1.render(size);
        armMiddle2.render(size);
        GlStateManager.translated(0, 3 / 16F, 6 / 16F);
        GlStateManager.rotated(angles[3], 1, 0, 0);
        GlStateManager.translated(0, -3 / 16F, -6 / 16F);
        drillBase.render(size);
        GlStateManager.translated(0, 3 / 16F, 0);
        GlStateManager.rotated(angles[4], 0, 0, 1);
        GlStateManager.translated(0, -3 / 16F, 0);
        drill.render(size);
        GlStateManager.popMatrix();
    }
}
