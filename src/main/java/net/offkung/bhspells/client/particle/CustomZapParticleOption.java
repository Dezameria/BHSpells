package net.offkung.bhspells.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.Util;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.registry.ParticleRegistry;
import org.joml.Vector3f;

import java.util.Locale;
import java.util.stream.IntStream;

public class CustomZapParticleOption implements ParticleOptions {
    public static final Codec<CustomZapParticleOption> CODEC = Codec.INT_STREAM.comapFlatMap((stream) -> {
        return Util.fixedSize(stream, 6).map((values) -> {
            Vec3 destination = new Vec3(values[0] / 10f, values[1] / 10f, values[2] / 10f);
            Vector3f color = new Vector3f(values[3] / 255f, values[4] / 255f, values[5] / 255f);
            return new CustomZapParticleOption(destination, color);
        });
    }, (option) -> {
        Vec3 dest = option.getDestination();
        Vector3f color = option.getColor();
        return IntStream.of(
                (int) (dest.x * 10f), (int) (dest.y * 10f), (int) (dest.z * 10f),
                (int) (color.x() * 255f), (int) (color.y() * 255f), (int) (color.z() * 255f)
        );
    });
    public static final ParticleOptions.Deserializer<CustomZapParticleOption> DESERIALIZER = new ParticleOptions.Deserializer<>() {
        public CustomZapParticleOption fromCommand(ParticleType<CustomZapParticleOption> type, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            float x = (float) reader.readDouble();
            reader.expect(' ');
            float y = (float) reader.readDouble();
            reader.expect(' ');
            float z = (float) reader.readDouble();

            Vector3f color = new Vector3f(1f, 0.85f, 0.2f);
            if (reader.canRead() && reader.peek() == ' ') {
                reader.expect(' ');
                float r = (float) reader.readDouble();
                reader.expect(' ');
                float g = (float) reader.readDouble();
                reader.expect(' ');
                float b = (float) reader.readDouble();
                color = new Vector3f(r, g, b);
            }

            return new CustomZapParticleOption(new Vec3(x, y, z), color);
        }

        public CustomZapParticleOption fromNetwork(ParticleType<CustomZapParticleOption> type, FriendlyByteBuf buf) {
            Vec3 destination = readDestination(buf);
            Vector3f color = readColor(buf);
            return new CustomZapParticleOption(destination, color);
        }
    };

    private final Vec3 destination;
    private final Vector3f color;

    public CustomZapParticleOption(Vec3 destination, Vector3f color) {
        this.destination = destination;
        this.color = color;
    }

    public CustomZapParticleOption(Vec3 destination) {
        this(destination, new Vector3f(1f, 0.85f, 0.2f));
    }

    public void writeToNetwork(FriendlyByteBuf pBuffer) {
        writeDestination(this.destination, pBuffer);
        writeColor(this.color, pBuffer);
    }

    public String writeToString() {
        Vec3 vec3 = this.destination;
        double d0 = vec3.x();
        double d1 = vec3.y();
        double d2 = vec3.z();
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f %.2f %.2f", BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()), d0, d1, d2, this.color.x(), this.color.y(), this.color.z());
    }

    public ParticleType<CustomZapParticleOption> getType() {
        return ParticleRegistry.CUSTOM_ZAP_PARTICLE.get();
    }

    public Vec3 getDestination() {
        return this.destination;
    }

    public Vector3f getColor() {
        return this.color;
    }

    private static Vec3 readDestination(FriendlyByteBuf buf) {
        return new Vec3(buf.readInt() / 10f, buf.readInt() / 10f, buf.readInt() / 10f);
    }

    private static void writeDestination(Vec3 vec, FriendlyByteBuf buf) {
        buf.writeInt((int) (vec.x * 10));
        buf.writeInt((int) (vec.y * 10));
        buf.writeInt((int) (vec.z * 10));
    }

    private static Vector3f readColor(FriendlyByteBuf buf) {
        return new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    private static void writeColor(Vector3f color, FriendlyByteBuf buf) {
        buf.writeFloat(color.x());
        buf.writeFloat(color.y());
        buf.writeFloat(color.z());
    }
}
