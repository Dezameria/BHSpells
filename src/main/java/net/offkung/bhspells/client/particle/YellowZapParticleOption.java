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

import java.util.Locale;
import java.util.stream.IntStream;

public class YellowZapParticleOption implements ParticleOptions {
    public static final Codec<YellowZapParticleOption> CODEC = Codec.INT_STREAM.comapFlatMap((stream) -> {
        return Util.fixedSize(stream, 3).map((vec3) -> {
            return new YellowZapParticleOption(new Vec3(vec3[0] / 10f, vec3[1] / 10f, vec3[2] / 10f));
        });
    }, (option) -> {
        return IntStream.of((int) (option.getDestination().x * 10f), (int) (option.getDestination().y * 10), (int) (option.getDestination().z * 10f));
    });
    public static final ParticleOptions.Deserializer<YellowZapParticleOption> DESERIALIZER = new ParticleOptions.Deserializer<YellowZapParticleOption>() {
        public YellowZapParticleOption fromCommand(ParticleType<YellowZapParticleOption> type, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            float f = (float) reader.readDouble();
            reader.expect(' ');
            float f1 = (float) reader.readDouble();
            reader.expect(' ');
            float f2 = (float) reader.readDouble();
            return new YellowZapParticleOption(new Vec3(f, f1, f2));
        }

        public YellowZapParticleOption fromNetwork(ParticleType<YellowZapParticleOption> type, FriendlyByteBuf buf) {
            var positionsource = YellowZapParticleOption.fromNetwork(buf);
            return new YellowZapParticleOption(positionsource);
        }
    };
    private final Vec3 destination;

    public YellowZapParticleOption(Vec3 destination) {
        this.destination = destination;
    }

    public void writeToNetwork(FriendlyByteBuf pBuffer) {
        toNetwork(this.destination, pBuffer);
    }

    public String writeToString() {
        Vec3 vec3 = this.destination;
        double d0 = vec3.x();
        double d1 = vec3.y();
        double d2 = vec3.z();
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f", BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()), d0, d1, d2);
    }

    public ParticleType<YellowZapParticleOption> getType() {
        return ParticleRegistry.YELLOW_ZAP_PARTICLE.get();
    }

    public Vec3 getDestination() {
        return this.destination;
    }

    private static Vec3 fromNetwork(FriendlyByteBuf buf) {
        return new Vec3(buf.readInt() / 10f, buf.readInt() / 10f, buf.readInt() / 10f);
    }

    private static void toNetwork(Vec3 vec, FriendlyByteBuf buf) {
        buf.writeInt((int) (vec.x * 10));
        buf.writeInt((int) (vec.y * 10));
        buf.writeInt((int) (vec.z * 10));
    }
}
