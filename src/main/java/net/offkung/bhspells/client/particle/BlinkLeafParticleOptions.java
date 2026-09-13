package net.offkung.bhspells.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.offkung.bhspells.registry.ParticleRegistry;

import java.util.Locale;

public class BlinkLeafParticleOptions implements ParticleOptions {
    public static final ParticleOptions.Deserializer<BlinkLeafParticleOptions> DESERIALIZER =
            new ParticleOptions.Deserializer<>() {
                @Override
                public BlinkLeafParticleOptions fromCommand(ParticleType<BlinkLeafParticleOptions> type, StringReader reader) throws CommandSyntaxException {
                    reader.expect(' ');
                    float r = reader.readFloat();
                    reader.expect(' ');
                    float g = reader.readFloat();
                    reader.expect(' ');
                    float b = reader.readFloat();
                    reader.expect(' ');
                    int lifetime = reader.readInt();
                    reader.expect(' ');
                    int blinkInterval = reader.readInt();
                    return new BlinkLeafParticleOptions(r, g, b, lifetime, blinkInterval);
                }

                @Override
                public BlinkLeafParticleOptions fromNetwork(ParticleType<BlinkLeafParticleOptions> type, FriendlyByteBuf buf) {
                    return new BlinkLeafParticleOptions(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readVarInt(), buf.readVarInt());
                }
            };

    public static final Codec<BlinkLeafParticleOptions> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(
                Codec.FLOAT.fieldOf("r").forGetter((option) -> option.r),
                Codec.FLOAT.fieldOf("g").forGetter((option) -> option.g),
                Codec.FLOAT.fieldOf("b").forGetter((option) -> option.b),
                Codec.INT.fieldOf("lifetime").forGetter((option) -> option.lifetime),
                Codec.INT.fieldOf("blink_interval").forGetter((option) -> option.blinkInterval)
        ).apply(instance, BlinkLeafParticleOptions::new);
    });

    private final float r, g, b;
    private final int lifetime;
    private final int blinkInterval;

    public BlinkLeafParticleOptions(float r, float g, float b, int lifetime, int blinkInterval) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.lifetime = lifetime;
        this.blinkInterval = blinkInterval;
    }

    public float getR() {
        return r;
    }

    public float getG() {
        return g;
    }

    public float getB() {
        return b;
    }

    public int getLifetime() {
        return lifetime;
    }

    public int getBlinkInterval() {
        return blinkInterval;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeFloat(this.r);
        buffer.writeFloat(this.g);
        buffer.writeFloat(this.b);
        buffer.writeVarInt(this.lifetime);
        buffer.writeVarInt(this.blinkInterval);
    }

    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %d %d", BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()), this.r, this.g, this.b, this.lifetime, this.blinkInterval);
    }

    @Override
    public ParticleType<BlinkLeafParticleOptions> getType() {
        return ParticleRegistry.BLINK_LEAF.get();
    }
}
