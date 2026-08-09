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

public class ColoredEndRodParticleOption implements ParticleOptions {
    public static final Codec<ColoredEndRodParticleOption> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("red").forGetter(ColoredEndRodParticleOption::getRed),
                    Codec.FLOAT.fieldOf("green").forGetter(ColoredEndRodParticleOption::getGreen),
                    Codec.FLOAT.fieldOf("blue").forGetter(ColoredEndRodParticleOption::getBlue)
            ).apply(instance, ColoredEndRodParticleOption::new)
    );

    @SuppressWarnings("deprecation")
    public static final ParticleOptions.Deserializer<ColoredEndRodParticleOption> DESERIALIZER = new ParticleOptions.Deserializer<>() {
        @Override
        public ColoredEndRodParticleOption fromCommand(ParticleType<ColoredEndRodParticleOption> type, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            float r = reader.readFloat();
            reader.expect(' ');
            float g = reader.readFloat();
            reader.expect(' ');
            float b = reader.readFloat();
            return new ColoredEndRodParticleOption(r, g, b);
        }

        @Override
        public ColoredEndRodParticleOption fromNetwork(ParticleType<ColoredEndRodParticleOption> type, FriendlyByteBuf buf) {
            return new ColoredEndRodParticleOption(buf.readFloat(), buf.readFloat(), buf.readFloat());
        }
    };

    private final float red;
    private final float green;
    private final float blue;

    public ColoredEndRodParticleOption(float red, float green, float blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeFloat(this.red);
        buffer.writeFloat(this.green);
        buffer.writeFloat(this.blue);
    }

    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f", BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()), this.red, this.green, this.blue);
    }

    @Override
    public ParticleType<ColoredEndRodParticleOption> getType() {
        return ParticleRegistry.COLORED_END_ROD_PARTICLE.get();
    }

    public float getRed() {
        return this.red;
    }

    public float getGreen() {
        return this.green;
    }

    public float getBlue() {
        return this.blue;
    }
}
