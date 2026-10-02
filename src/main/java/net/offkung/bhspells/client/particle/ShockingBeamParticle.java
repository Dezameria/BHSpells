package net.offkung.bhspells.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Short-lived, high-energy magical electricity beam particle implementing
 * the "Emerald Arc Discharge / Unstable Magical Electricity" VFX concept.
 *
 * All segmented lightning geometry, branches, and shock rings are rendered client-side
 * deterministically via {@link ShockingLightningGeometry}.
 * Secondary sparks and plasma turbulence are emitted on discrete tick boundaries.
 */
public class ShockingBeamParticle extends TextureSheetParticle {
    private static final int FULL_BRIGHT = 15728880;
    private static final int DISCHARGE_LIFETIME = 10;

    private static final DustParticleOptions GREEN_ELECTRIC_DUST =
            new DustParticleOptions(new Vector3f(0.20F, 1.0F, 0.35F), 0.9F);
    private static final DustParticleOptions CYAN_ELECTRIC_DUST =
            new DustParticleOptions(new Vector3f(0.05F, 0.95F, 0.85F), 0.8F);

    private static final ParticleRenderType EMISSIVE_RENDER_TYPE = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            RenderSystem.depthMask(false);
            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
            RenderSystem.depthMask(true);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        }
    };

    private final Vec3 origin;
    private final Vec3 destination;
    private final float scaleMultiplier;
    private final long visualSeed;

    public ShockingBeamParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed,
            double zSpeed, ShockingBeamParticleOption options) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.origin = new Vec3(x, y, z);
        this.destination = options.getDestination();
        this.scaleMultiplier = options.getScale();
        this.visualSeed = options.getSeed() != 0L ? options.getSeed() : createVisualSeed(x, y, z, this.destination);
        this.setSize(1.0F, 1.0F);
        this.quadSize = 1.0F;
        this.lifetime = DISCHARGE_LIFETIME;
        this.hasPhysics = false;
        this.gravity = 0.0F;
    }

    @Override
    public boolean shouldCull() {
        return false;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return EMISSIVE_RENDER_TYPE;
    }

    @Override
    public int getLightColor(float partialTick) {
        return FULL_BRIGHT;
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        Vec3 cameraPos = camera.getPosition();
        double distSqr = cameraPos.distanceToSqr(this.origin);
        if (distSqr > 48.0D * 48.0D) {
            return; // Distance culling beyond 48 blocks
        }

        boolean reducedDetail = distSqr > 24.0D * 24.0D; // LOD reduction beyond 24 blocks

        Vector3f start = new Vector3f(
                (float) (this.origin.x() - cameraPos.x()),
                (float) (this.origin.y() - cameraPos.y()),
                (float) (this.origin.z() - cameraPos.z()));
        Vector3f end = new Vector3f(
                (float) (this.destination.x() - cameraPos.x()),
                (float) (this.destination.y() - cameraPos.y()),
                (float) (this.destination.z() - cameraPos.z()));

        ShockingLightningGeometry.renderDischarge(
                consumer,
                getU0(), getV0(), getU1(), getV1(),
                start, end,
                this.visualSeed, this.age, partialTick,
                this.scaleMultiplier, this.lifetime, reducedDetail);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed && this.level != null) {
            spawnChaosParticles();
        }
    }

    /**
     * Spawns electrical turbulence, sparks, and plasma dust on discrete ticks.
     * Keeps network cost zero while maintaining rich environmental accents.
     */
    private void spawnChaosParticles() {
        RandomSource rand = RandomSource.create(this.visualSeed ^ (0xABCD1234L * (this.age + 1)));
        Vec3 dir = this.destination.subtract(this.origin);
        double len = dir.length();
        if (len < 0.001) {
            return;
        }
        Vec3 normDir = dir.normalize();

        Vec3 ref = Math.abs(normDir.y) > 0.92D ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 right = normDir.cross(ref).normalize();
        Vec3 up = right.cross(normDir).normalize();

        // 1. Hand Origin Sparks & Energy Streaks
        if (this.age <= 2) {
            for (int i = 0; i < 2; i++) {
                double angle = rand.nextDouble() * Math.PI * 2.0;
                double speed = 0.04 + rand.nextDouble() * 0.08;
                Vec3 vel = right.scale(Math.cos(angle) * speed).add(up.scale(Math.sin(angle) * speed)).add(normDir.scale(0.04));
                this.level.addParticle(i % 2 == 0 ? GREEN_ELECTRIC_DUST : ParticleTypes.ELECTRIC_SPARK,
                        this.origin.x, this.origin.y, this.origin.z,
                        vel.x, vel.y, vel.z);
            }
        }

        // 2. Electrical Chaos along Main Arc (at pulse front)
        float surgeT = ShockingLightningGeometry.easeOutCubic(Mth.clamp((float) this.age / 4.2F, 0.0F, 1.0F));
        Vec3 pulsePos = this.origin.add(dir.scale(surgeT));
        for (int i = 0; i < 3; i++) {
            double angle = rand.nextDouble() * Math.PI * 2.0;
            double speed = (0.05 + rand.nextDouble() * 0.12) * this.scaleMultiplier;
            Vec3 vel = right.scale(Math.cos(angle) * speed).add(up.scale(Math.sin(angle) * speed));
            double jitterAlong = (rand.nextDouble() - 0.5) * 0.35;
            Vec3 pos = pulsePos.add(normDir.scale(jitterAlong));

            this.level.addParticle(rand.nextBoolean() ? ParticleTypes.ELECTRIC_SPARK : CYAN_ELECTRIC_DUST,
                    pos.x, pos.y, pos.z,
                    vel.x, vel.y, vel.z);
        }

        // 3. Origin Spark Burst (Burst of sparks at discharge point during initial ticks)
        if (this.age >= 1 && this.age <= 4) {
            for (int i = 0; i < 3; i++) {
                double theta = rand.nextDouble() * Math.PI * 2.0;
                double phi = (rand.nextDouble() - 0.5) * Math.PI;
                double speed = 0.08 + rand.nextDouble() * 0.14;
                Vec3 vel = new Vec3(
                        Math.cos(phi) * Math.cos(theta) * speed,
                        Math.sin(phi) * speed,
                        Math.cos(phi) * Math.sin(theta) * speed
                );
                // Bias forward along cast direction away from player
                if (vel.dot(normDir) < -0.02D) {
                    vel = vel.add(normDir.scale(-1.2D * vel.dot(normDir)));
                }
                this.level.addParticle(i % 2 == 0 ? ParticleTypes.ELECTRIC_SPARK : GREEN_ELECTRIC_DUST,
                        this.origin.x, this.origin.y, this.origin.z,
                        vel.x, vel.y, vel.z);
            }
        }
    }

    private static long createVisualSeed(double x, double y, double z, Vec3 destination) {
        long seed = Double.doubleToLongBits(x);
        seed = seed * 31L + Double.doubleToLongBits(y);
        seed = seed * 31L + Double.doubleToLongBits(z);
        seed = seed * 31L + Double.doubleToLongBits(destination.x());
        seed = seed * 31L + Double.doubleToLongBits(destination.y());
        return seed * 31L + Double.doubleToLongBits(destination.z());
    }

    public static class Provider implements ParticleProvider<ShockingBeamParticleOption> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(ShockingBeamParticleOption options, ClientLevel level, double x, double y,
                double z, double xSpeed, double ySpeed, double zSpeed) {
            ShockingBeamParticle particle = new ShockingBeamParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, options);
            particle.pickSprite(this.spriteSet);
            return particle;
        }
    }
}
