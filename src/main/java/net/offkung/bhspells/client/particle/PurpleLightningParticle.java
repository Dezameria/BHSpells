package net.offkung.bhspells.client.particle;

import com.github.alexthe666.citadel.client.render.LightningBoltData;
import com.github.alexthe666.citadel.client.render.LightningRender;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector4f;

public class PurpleLightningParticle extends Particle {
    private final LightningRender lightningRender = new LightningRender();

    public PurpleLightningParticle(ClientLevel world, double x, double y, double z, double xd, double yd, double zd) {
        super(world, x, y, z);
        this.setSize(0.5F, 0.5F);
        this.x = x;
        this.y = y;
        this.z = z;
        this.xd = 0.0F;
        this.yd = 0.0F;
        this.zd = 0.0F;
        Vec3 lightningTo = this.findLightningToPos(world, x, y, z, 32);
        Vec3 to = lightningTo.subtract(x, y, z);
        this.lifetime = (int)Math.ceil(to.length());
        int sections = 4 * this.lifetime;
        boolean purple = this.random.nextBoolean();
        Vector4f color = purple ? new Vector4f(0.25F, 0F, 0.34F, 0.3F) : new Vector4f(0.05F, 0.05F, 0.05F, 0.1F);
        LightningBoltData.BoltRenderInfo boltData = new LightningBoltData.BoltRenderInfo(0.2F, 0.1F, 0.2F, 0.6F, color, 0.5F);
        LightningBoltData bolt = (new LightningBoltData(boltData, Vec3.ZERO, to, sections)).size(0.3F + this.random.nextFloat() * 0.2F).lifespan(this.lifetime + 1).spawn(LightningBoltData.SpawnFunction.CONSECUTIVE);
        this.lightningRender.update(this, bolt, 1.0F);
    }

    public boolean shouldCull() {
        return false;
    }

    private Vec3 findLightningToPos(ClientLevel world, double x, double y, double z, int range) {
        Vec3 vec3 = new Vec3(x, y, z);

        for(int i = 0; i < 10; ++i) {
            Vec3 vec31 = vec3.add((this.random.nextFloat() * (float)range - (float)range / 2.0F), (this.random.nextFloat() * (float)range - (float)range / 2.0F), this.random.nextFloat() * (float)range - (float)range / 2.0F);
            if (this.canSeeBlock(vec3, vec31)) {
                return vec31;
            }
        }

        return vec3;
    }

    private boolean canSeeBlock(Vec3 from, Vec3 to) {
        BlockHitResult result = this.level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
        return Vec3.atCenterOf(result.getBlockPos()).distanceTo(to) < (double)3.0F;
    }

    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.xd = 0.0F;
        this.yd = 0.0F;
        this.zd = 0.0F;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.move(this.xd, this.yd, this.zd);
            this.yd -= this.gravity;
        }

    }

    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
        Vec3 cameraPos = camera.getPosition();
        float x = (float) Mth.lerp(partialTick, this.xo, this.x);
        float y = (float)Mth.lerp(partialTick, this.yo, this.y);
        float z = (float)Mth.lerp(partialTick, this.zo, this.z);
        PoseStack posestack = new PoseStack();
        posestack.pushPose();
        posestack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        posestack.translate(x, y, z);
        this.lightningRender.render(partialTick, posestack, multibuffersource$buffersource);
        multibuffersource$buffersource.endBatch();
        posestack.popPose();
    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new PurpleLightningParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }
}
