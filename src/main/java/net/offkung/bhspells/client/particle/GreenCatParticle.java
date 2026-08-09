package net.offkung.bhspells.client.particle;

import dev.kosmx.playerAnim.core.util.Vector3;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

public class GreenCatParticle extends TextureSheetParticle {
    private final Vector3f fromColor;
    private final Vector3f toColor;

    private final float startQuadSize;
    private final float endQuadSize;

    protected GreenCatParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, Vector3f fromColor, Vector3f toColor, int lifetime) {
        super(level, x, y, z, xd, yd, zd);

        this.fromColor = fromColor;
        this.toColor = toColor;
        this.lifetime = lifetime;

        this.xd = xd;
        this.yd = yd;
        this.zd = zd;

        this.hasPhysics = false;
        this.gravity = 0.0f;

        this.startQuadSize = 0.3f;
        this.endQuadSize = 1.4f;
        this.quadSize = this.startQuadSize;

        setColor(fromColor.x(), fromColor.y(), fromColor.z());
        this.alpha = 1.0f;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        float progress = (float) this.age / (float) this.lifetime; // 0 -> 1 over lifetime

        float r = Mth.lerp(progress, fromColor.x(), toColor.x());
        float g = Mth.lerp(progress, fromColor.y(), toColor.y());
        float b = Mth.lerp(progress, fromColor.z(), toColor.z());
        setColor(r, g, b);

        this.quadSize = Mth.lerp(progress, startQuadSize, endQuadSize);

        float fadeStart = 0.4f;
        if (progress > fadeStart) {
            float fadeProgress = (progress - fadeStart) / (1.0f - fadeStart);
            this.alpha = Mth.clamp(1.0f - fadeProgress, 0.0f, 1.0f);
        }

        this.move(this.xd, this.yd, this.zd);

        if (this.onGround) {
            this.xd *= 0.98D;
            this.zd *= 0.98D;
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public int getLightColor(float partialTicks) {
        return LightTexture.FULL_BRIGHT;
    }

    public static class Provider implements ParticleProvider<GreenCatParticleOption> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(GreenCatParticleOption options, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            GreenCatParticle particle = new GreenCatParticle(level, x, y, z, xd, yd, zd, options.getFromColor(), options.getToColor(), options.getLifetime());
            particle.pickSprite(this.sprites);
            return particle;
        }
    }
}
