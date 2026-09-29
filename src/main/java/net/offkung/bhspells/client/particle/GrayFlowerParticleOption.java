package net.offkung.bhspells.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.offkung.bhspells.registry.ParticleRegistry;
import org.joml.Vector3f;

import java.util.Locale;

public class GrayFlowerParticleOption implements ParticleOptions {
    public static final Deserializer<GrayFlowerParticleOption> DESERIALIZER =
            new Deserializer<>() {
                @Override
                public GrayFlowerParticleOption fromCommand(ParticleType<GrayFlowerParticleOption> type, StringReader reader) throws CommandSyntaxException {
                    reader.expect(' ');
                    float fr = reader.readFloat();
                    reader.expect(' ');
                    float fg = reader.readFloat();
                    reader.expect(' ');
                    float fb = reader.readFloat();
                    reader.expect(' ');
                    float scale = reader.readFloat();
                    return new GrayFlowerParticleOption(new Vector3f(fr, fg, fb), scale);
                }

                @Override
                public GrayFlowerParticleOption fromNetwork(ParticleType<GrayFlowerParticleOption> type, FriendlyByteBuf buf) {
                    return new GrayFlowerParticleOption(buf.readVector3f(), buf.readFloat());
                }
            };

    public static final Codec<GrayFlowerParticleOption> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ExtraCodecs.VECTOR3F.fieldOf("color").forGetter((option) -> {
            return option.color;
        }), Codec.FLOAT.fieldOf("scale").forGetter((option) -> {
            return option.scale;
        })).apply(instance, GrayFlowerParticleOption::new);
    });

    private final Vector3f color;
    private final float scale;

    public GrayFlowerParticleOption(Vector3f color, float scale) {
        this.color = color;
        this.scale = scale;
    }

    public Vector3f getColor() {
        return color;
    }

    public float getScale() {
        return scale;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeFloat(this.color.x());
        buffer.writeFloat(this.color.y());
        buffer.writeFloat(this.color.z());
        buffer.writeFloat(this.scale);
    }

    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f", BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()), this.color.x(), this.color.y(), this.color.z(), this.scale);
    }

    @Override
    public ParticleType<GrayFlowerParticleOption> getType() {
        return ParticleRegistry.GRAY_FLOWER_PARTICLE.get();
    }
}
