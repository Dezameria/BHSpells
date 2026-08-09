package net.offkung.bhspells.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class ColoredEndRodParticle extends SimpleAnimatedParticle {

    ColoredEndRodParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet spriteSet, ColoredEndRodParticleOption options) {
        super(level, x, y, z, spriteSet, 0.0125F);
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.quadSize *= 0.75F;
        this.lifetime = 60 + this.random.nextInt(12);
        this.setFadeColor(15916745);
        this.setSpriteFromAge(spriteSet);

        // Apply RGB color from particle options
        this.rCol = options.getRed();
        this.gCol = options.getGreen();
        this.bCol = options.getBlue();
    }

    @Override
    public void move(double x, double y, double z) {
        this.setBoundingBox(this.getBoundingBox().move(x, y, z));
        this.setLocationFromBoundingbox();
    }

    @Override
    public int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<ColoredEndRodParticleOption> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(@NotNull ColoredEndRodParticleOption options, @NotNull ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new ColoredEndRodParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites, options);
        }
    }
}
