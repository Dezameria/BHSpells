package net.offkung.bhspells.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.offkung.bhspells.registry.ParticleRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Locale;

public class ShockingBeamParticleOption implements ParticleOptions {
    private final Vec3 destination;
    private final float scale;
    private final long seed;

    public static final Codec<ShockingBeamParticleOption> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("x").forGetter(option -> option.destination.x),
            Codec.DOUBLE.fieldOf("y").forGetter(option -> option.destination.y),
            Codec.DOUBLE.fieldOf("z").forGetter(option -> option.destination.z),
            Codec.FLOAT.fieldOf("scale").forGetter(option -> option.scale),
            Codec.LONG.fieldOf("seed").forGetter(option -> option.seed))
            .apply(instance, (x, y, z, scale, seed) -> new ShockingBeamParticleOption(new Vec3(x, y, z), scale, seed)));

    @SuppressWarnings("deprecation")
    public static final Deserializer<ShockingBeamParticleOption> DESERIALIZER = new Deserializer<>() {
        @Override
        public ShockingBeamParticleOption fromCommand(ParticleType<ShockingBeamParticleOption> particleType,
                StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            double x = reader.readDouble();
            reader.expect(' ');
            double y = reader.readDouble();
            reader.expect(' ');
            double z = reader.readDouble();
            reader.expect(' ');
            float scale = reader.readFloat();
            long seed = 0L;
            if (reader.canRead() && reader.peek() == ' ') {
                reader.expect(' ');
                seed = reader.readLong();
            }
            return new ShockingBeamParticleOption(new Vec3(x, y, z), scale, seed);
        }

        @Override
        public ShockingBeamParticleOption fromNetwork(ParticleType<ShockingBeamParticleOption> particleType,
                FriendlyByteBuf buf) {
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            float scale = buf.readFloat();
            long seed = buf.readLong();
            return new ShockingBeamParticleOption(new Vec3(x, y, z), scale, seed);
        }
    };

    public ShockingBeamParticleOption(Vec3 destination, float scale, long seed) {
        this.destination = destination;
        this.scale = scale;
        this.seed = seed;
    }

    public ShockingBeamParticleOption(Vec3 destination, float scale) {
        this(destination, scale, 0L);
    }

    public ShockingBeamParticleOption(Vec3 destination) {
        this(destination, 1.0f, 0L);
    }

    @Override
    public ParticleType<ShockingBeamParticleOption> getType() {
        return ParticleRegistry.SHOCKING_BEAM.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeDouble(this.destination.x);
        buf.writeDouble(this.destination.y);
        buf.writeDouble(this.destination.z);
        buf.writeFloat(this.scale);
        buf.writeLong(this.seed);
    }

    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f %d",
                ForgeRegistries.PARTICLE_TYPES.getKey(getType()),
                this.destination.x, this.destination.y, this.destination.z, this.scale, this.seed);
    }

    public Vec3 getDestination() {
        return this.destination;
    }

    public float getScale() {
        return this.scale;
    }

    public long getSeed() {
        return this.seed;
    }
}
