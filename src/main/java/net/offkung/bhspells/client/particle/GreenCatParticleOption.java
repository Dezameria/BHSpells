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

public class GreenCatParticleOption implements ParticleOptions {
    public static final ParticleOptions.Deserializer<GreenCatParticleOption> DESERIALIZER =
            new ParticleOptions.Deserializer<>() {
                @Override
                public GreenCatParticleOption fromCommand(ParticleType<GreenCatParticleOption> type, StringReader reader) throws CommandSyntaxException {
                    reader.expect(' ');
                    float fr = reader.readFloat();
                    reader.expect(' ');
                    float fg = reader.readFloat();
                    reader.expect(' ');
                    float fb = reader.readFloat();
                    reader.expect(' ');
                    float tr = reader.readFloat();
                    reader.expect(' ');
                    float tg = reader.readFloat();
                    reader.expect(' ');
                    float tb = reader.readFloat();
                    reader.expect(' ');
                    int lifetime = reader.readInt();
                    return new GreenCatParticleOption(new Vector3f(fr, fg, fb), new Vector3f(tr, tg, tb), lifetime);
                }

                @Override
                public GreenCatParticleOption fromNetwork(ParticleType<GreenCatParticleOption> type, FriendlyByteBuf buffer) {
                    Vector3f from = new Vector3f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
                    Vector3f to = new Vector3f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
                    int lifetime = buffer.readVarInt();
                    return new GreenCatParticleOption(from, to, lifetime);
                }
            };

    public static final Codec<GreenCatParticleOption> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ExtraCodecs.VECTOR3F.fieldOf("fromColor").forGetter(o -> o.fromColor),
                    ExtraCodecs.VECTOR3F.fieldOf("toColor").forGetter(o -> o.toColor),
                    Codec.INT.fieldOf("lifetime").forGetter(o -> o.lifetime)
            ).apply(instance, GreenCatParticleOption::new)
    );

    private final Vector3f fromColor;
    private final Vector3f toColor;
    private final int lifetime;

    public GreenCatParticleOption(Vector3f fromColor, Vector3f toColor, int lifetime) {
        this.fromColor = fromColor;
        this.toColor = toColor;
        this.lifetime = lifetime;
    }

    public Vector3f getFromColor() {
        return fromColor;
    }

    public Vector3f getToColor() {
        return toColor;
    }

    public int getLifetime() {
        return lifetime;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeFloat(fromColor.x());
        buffer.writeFloat(fromColor.y());
        buffer.writeFloat(fromColor.z());
        buffer.writeFloat(toColor.x());
        buffer.writeFloat(toColor.y());
        buffer.writeFloat(toColor.z());
        buffer.writeVarInt(lifetime);
    }

    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f %.2f %.2f %d",
                BuiltInRegistries.PARTICLE_TYPE.getKey(getType()),
                fromColor.x(), fromColor.y(), fromColor.z(),
                toColor.x(), toColor.y(), toColor.z(),
                lifetime);
    }

    @Override
    public ParticleType<GreenCatParticleOption> getType() {
        return ParticleRegistry.GREEN_CAT.get();
    }
}
