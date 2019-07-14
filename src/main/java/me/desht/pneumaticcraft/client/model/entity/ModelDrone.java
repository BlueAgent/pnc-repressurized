package me.desht.pneumaticcraft.client.model.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.entity.EntityProgrammableController;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.entity.living.EntityDroneBase;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.util.math.AxisAlignedBB;

public class ModelDrone extends EntityModel<EntityDroneBase> {
    //fields
    private final RendererModel Base;
    private final RendererModel Base2;
    private final RendererModel Base3;
    private final RendererModel Base4;
    private final RendererModel Base5;
    private final RendererModel Prop1Part1;
    private final RendererModel Prop1Part2;
    private final RendererModel Prop1Part3;
    private final RendererModel Prop2Part1;
    private final RendererModel Prop2Part2;
    private final RendererModel Prop2Part3;
    private final RendererModel Prop3Part1;
    private final RendererModel Prop3Part2;
    private final RendererModel Prop3Part3;
    private final RendererModel Prop4Part1;
    private final RendererModel Prop4Part2;
    private final RendererModel Prop4Part3;
    private final RendererModel Frame1;
    private final RendererModel Frame2;
    private final RendererModel LandingStand1;
    private final RendererModel LandingStand2;
    private final RendererModel LandingStand3;
    private final RendererModel LandingStand4;
    private final RendererModel LaserArm;
    private final RendererModel LaserSource;
    private final ModelDroneMinigun minigun = new ModelDroneMinigun();
    private boolean renderFrame = false;
    private int frameColor = 0;

    public ModelDrone(int frameColor){
        this();
        renderFrame = true;
        this.frameColor = frameColor;
    }
    
    public ModelDrone() {
        textureWidth = 64;
        textureHeight = 32;

        Base = new RendererModel(this, 0, 0);
        Base.addBox(0F, 0F, 0F, 6, 6, 6);
        Base.setRotationPoint(-3F, 14F, -3F);
        Base.setTextureSize(64, 32);
        Base.mirror = true;
        setRotation(Base, 0F, 0F, 0F);
        Base2 = new RendererModel(this, 0, 12);
        Base2.addBox(0F, 0F, 0F, 4, 4, 1);
        Base2.setRotationPoint(-2F, 15F, -4F);
        Base2.setTextureSize(64, 32);
        Base2.mirror = true;
        setRotation(Base2, 0F, 0F, 0F);
        Base3 = new RendererModel(this, 0, 12);
        Base3.addBox(0F, 0F, 0F, 4, 4, 1);
        Base3.setRotationPoint(-2F, 15F, 3F);
        Base3.setTextureSize(64, 32);
        Base3.mirror = true;
        setRotation(Base3, 0F, 0F, 0F);
        Base4 = new RendererModel(this, 10, 12);
        Base4.addBox(0F, 0F, 0F, 1, 4, 4);
        Base4.setRotationPoint(3F, 15F, -2F);
        Base4.setTextureSize(64, 32);
        Base4.mirror = true;
        setRotation(Base4, 0F, 0F, 0F);
        Base5 = new RendererModel(this, 10, 12);
        Base5.addBox(0F, 0F, 0F, 1, 4, 4);
        Base5.setRotationPoint(-4F, 15F, -2F);
        Base5.setTextureSize(64, 32);
        Base5.mirror = true;
        setRotation(Base5, 0F, 0F, 0F);
        Prop1Part1 = new RendererModel(this, 0, 17);
        Prop1Part1.addBox(0.5F, 0F, -0.8F, 3, 1, 2);
        Prop1Part1.setRotationPoint(11.5F, 14F, 0F);
        Prop1Part1.setTextureSize(64, 32);
        Prop1Part1.mirror = true;
        setRotation(Prop1Part1, -0.3490659F, 0F, 0F);
        Prop1Part2 = new RendererModel(this, 0, 17);
        Prop1Part2.addBox(-3.5F, 0F, -1.2F, 3, 1, 2);
        Prop1Part2.setRotationPoint(11.5F, 14F, 0F);
        Prop1Part2.setTextureSize(64, 32);
        Prop1Part2.mirror = true;
        setRotation(Prop1Part2, 0.3490659F, 0F, 0F);
        Prop1Part3 = new RendererModel(this, 0, 20);
        Prop1Part3.addBox(-0.5F, 0F, -0.5F, 1, 2, 1);
        Prop1Part3.setRotationPoint(11.5F, 14F, 0F);
        Prop1Part3.setTextureSize(64, 32);
        Prop1Part3.mirror = true;
        setRotation(Prop1Part3, 0F, 0F, 0F);
        Prop2Part1 = new RendererModel(this, 0, 17);
        Prop2Part1.addBox(0.5F, 0F, -0.8F, 3, 1, 2);
        Prop2Part1.setRotationPoint(-11.5F, 14F, 0F);
        Prop2Part1.setTextureSize(64, 32);
        Prop2Part1.mirror = true;
        setRotation(Prop2Part1, -0.3490659F, 0F, 0F);
        Prop2Part2 = new RendererModel(this, 0, 17);
        Prop2Part2.addBox(-3.5F, 0F, -1.2F, 3, 1, 2);
        Prop2Part2.setRotationPoint(-11.5F, 14F, 0F);
        Prop2Part2.setTextureSize(64, 32);
        Prop2Part2.mirror = true;
        setRotation(Prop2Part2, 0.3490659F, 0F, 0F);
        Prop2Part3 = new RendererModel(this, 0, 20);
        Prop2Part3.addBox(-0.5F, 0F, -0.5F, 1, 2, 1);
        Prop2Part3.setRotationPoint(-11.5F, 14F, 0F);
        Prop2Part3.setTextureSize(64, 32);
        Prop2Part3.mirror = true;
        setRotation(Prop2Part3, 0F, 0F, 0F);
        Prop3Part1 = new RendererModel(this, 0, 17);
        Prop3Part1.addBox(-3.5F, 0F, -0.8F, 3, 1, 2);
        Prop3Part1.setRotationPoint(0F, 13.7F, -11.5F);
        Prop3Part1.setTextureSize(64, 32);
        Prop3Part1.mirror = true;
        setRotation(Prop3Part1, -0.3490659F, 0F, 0F);
        Prop3Part2 = new RendererModel(this, 0, 17);
        Prop3Part2.addBox(0.5F, 0F, -1.1F, 3, 1, 2);
        Prop3Part2.setRotationPoint(0F, 14F, -11.5F);
        Prop3Part2.setTextureSize(64, 32);
        Prop3Part2.mirror = true;
        setRotation(Prop3Part2, 0.3490659F, 0F, 0F);
        Prop3Part3 = new RendererModel(this, 0, 20);
        Prop3Part3.addBox(-0.5F, 0F, -0.5F, 1, 2, 1);
        Prop3Part3.setRotationPoint(0F, 14F, -11.5F);
        Prop3Part3.setTextureSize(64, 32);
        Prop3Part3.mirror = true;
        setRotation(Prop3Part3, 0F, 0F, 0F);
        Prop4Part1 = new RendererModel(this, 0, 17);
        Prop4Part1.addBox(-3.5F, 0F, -0.8F, 3, 1, 2);
        Prop4Part1.setRotationPoint(0F, 14F, 11.5F);
        Prop4Part1.setTextureSize(64, 32);
        Prop4Part1.mirror = true;
        setRotation(Prop4Part1, -0.3490659F, 0F, 0F);
        Prop4Part2 = new RendererModel(this, 0, 17);
        Prop4Part2.addBox(0.5F, 0F, -1.1F, 3, 1, 2);
        Prop4Part2.setRotationPoint(0F, 14F, 11.5F);
        Prop4Part2.setTextureSize(64, 32);
        Prop4Part2.mirror = true;
        setRotation(Prop4Part2, 0.3490659F, 0F, 0F);
        Prop4Part3 = new RendererModel(this, 0, 20);
        Prop4Part3.addBox(-0.5F, 0F, -0.5F, 1, 2, 1);
        Prop4Part3.setRotationPoint(0F, 14F, 11.5F);
        Prop4Part3.setTextureSize(64, 32);
        Prop4Part3.mirror = true;
        setRotation(Prop4Part3, 0F, 0F, 0F);
        Frame1 = new RendererModel(this, 0, 26);
        Frame1.addBox(0F, 0F, 0F, 26, 2, 2);
        Frame1.setRotationPoint(-13F, 16F, -1F);
        Frame1.setTextureSize(64, 32);
        Frame1.mirror = true;
        setRotation(Frame1, 0F, 0F, 0F);
        Frame2 = new RendererModel(this, 0, 0);
        Frame2.addBox(0F, 0F, 0F, 2, 2, 26);
        Frame2.setRotationPoint(-1F, 16F, -13F);
        Frame2.setTextureSize(64, 32);
        Frame2.mirror = true;
        setRotation(Frame2, 0F, 0F, 0F);
        LandingStand1 = new RendererModel(this, 30, 0);
        LandingStand1.addBox(-1F, 0F, -0.5F, 1, 6, 1);
        LandingStand1.setRotationPoint(-8F, 18F, 0F);
        LandingStand1.setTextureSize(64, 32);
        LandingStand1.mirror = true;
        setRotation(LandingStand1, 0F, 0F, 0F);
        LandingStand2 = new RendererModel(this, 30, 0);
        LandingStand2.addBox(0F, 0F, -0.5F, 1, 6, 1);
        LandingStand2.setRotationPoint(8F, 18F, 0F);
        LandingStand2.setTextureSize(64, 32);
        LandingStand2.mirror = true;
        setRotation(LandingStand2, 0F, 0F, 0F);
        LandingStand3 = new RendererModel(this, 30, 0);
        LandingStand3.addBox(-0.5F, 0F, -1F, 1, 6, 1);
        LandingStand3.setRotationPoint(0F, 18F, -8F);
        LandingStand3.setTextureSize(64, 32);
        LandingStand3.mirror = true;
        setRotation(LandingStand3, 0F, 0F, 0F);
        LandingStand4 = new RendererModel(this, 30, 0);
        LandingStand4.addBox(-0.5F, 0F, 0F, 1, 6, 1);
        LandingStand4.setRotationPoint(0F, 18F, 8F);
        LandingStand4.setTextureSize(64, 32);
        LandingStand4.mirror = true;
        setRotation(LandingStand4, 0F, 0F, 0F);
        LaserArm = new RendererModel(this, 56, 0);
        LaserArm.addBox(0F, 0F, 0F, 1, 2, 1);
        LaserArm.setRotationPoint(-0.5F, 20F, -0.5F);
        LaserArm.setTextureSize(64, 32);
        LaserArm.mirror = true;
        setRotation(LaserArm, 0F, 0F, 0F);
        LaserSource = new RendererModel(this, 56, 3);
        LaserSource.addBox(0F, 0F, 0F, 2, 2, 2);
        LaserSource.setRotationPoint(-1F, 22F, -1F);
        LaserSource.setTextureSize(64, 32);
        LaserSource.mirror = true;
        setRotation(LaserSource, 0F, 0F, 0F);

        LaserArm.offsetY = LaserSource.offsetY = -4.5F / 16;
    }

    @Override
    public void render(EntityDroneBase drone, float f, float f1, float f2, float f3, float f4, float f5) {
        if (drone instanceof EntityProgrammableController) f5 /= 2F;
        super.render(drone, f, f1, f2, f3, f4, f5);
        setRotationAngles(drone, f, f1, f2, f3, f4, f5);
        if (drone != null) RenderUtils.glColorHex(0xFF000000 + drone.getDroneColor());
        Base2.render(f5);
        Base3.render(f5);
        Base4.render(f5);
        Base5.render(f5);
        GlStateManager.color4f(1, 1, 1, 1);
        Base.render(f5);
        Prop1Part1.render(f5);
        Prop1Part2.render(f5);
        Prop1Part3.render(f5);
        Prop2Part1.render(f5);
        Prop2Part2.render(f5);
        Prop2Part3.render(f5);
        Prop3Part1.render(f5);
        Prop3Part2.render(f5);
        Prop3Part3.render(f5);
        Prop4Part1.render(f5);
        Prop4Part2.render(f5);
        Prop4Part3.render(f5);
        Frame1.render(f5);
        Frame2.render(f5);
        LandingStand1.render(f5);
        LandingStand2.render(f5);
        LandingStand3.render(f5);
        LandingStand4.render(f5);
        LaserArm.render(f5);
        LaserSource.render(f5);
        if (drone instanceof EntityDrone && ((EntityDrone) drone).hasMinigun())
            minigun.render(drone, f, f1, f2, f3, f4, f5);
        if (renderFrame) {
            GlStateManager.disableTexture();
            RenderUtils.glColorHex(frameColor);
            double s = 3 / 16D;
            double y = 17 / 16D;
            RenderUtils.renderFrame(new AxisAlignedBB(-s, y - s, -s, s, y + s, s), 1 / 32D);
            GlStateManager.enableTexture();
        }
    }

    @Override
    public void setLivingAnimations(EntityDroneBase drone, float par2, float par3, float partialTicks) {
        float propRotation = drone.oldPropRotation + (drone.propRotation - drone.oldPropRotation) * partialTicks;
        Prop1Part1.rotateAngleY = propRotation;
        Prop1Part2.rotateAngleY = propRotation;
        Prop1Part3.rotateAngleY = propRotation;
        Prop2Part1.rotateAngleY = propRotation;
        Prop2Part2.rotateAngleY = propRotation;
        Prop2Part3.rotateAngleY = propRotation;
        Prop3Part1.rotateAngleY = -propRotation;
        Prop3Part2.rotateAngleY = -propRotation;
        Prop3Part3.rotateAngleY = -propRotation;
        Prop4Part1.rotateAngleY = -propRotation;
        Prop4Part2.rotateAngleY = -propRotation;
        Prop4Part3.rotateAngleY = -propRotation;

        float laserExtension = drone.oldLaserExtension + (drone.laserExtension - drone.oldLaserExtension) * partialTicks;
        laserExtension = (1F - laserExtension) * -4.5F / 16F;
        LaserArm.offsetY = LaserSource.offsetY = laserExtension;
    }

    private void setRotation(RendererModel model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

}
