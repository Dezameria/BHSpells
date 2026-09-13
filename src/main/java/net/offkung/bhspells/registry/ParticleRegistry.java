package net.offkung.bhspells.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.client.particle.*;
import yesman.epicfight.particle.HitParticleType;

import java.util.function.Supplier;

public class ParticleRegistry {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, BHSpells.MODID);

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }

    public static final Supplier<SimpleParticleType> BUBBLE_SPLASH_PARTICLE = PARTICLE_TYPES.register("bubble_splash", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> GOLD_SPARKLE_PARTICLE = PARTICLE_TYPES.register("gold_sparkle", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> GREEN_CROSS_PARTICLE = PARTICLE_TYPES.register("green_cross", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> OAK_LEAF_PARTICLE = PARTICLE_TYPES.register("oak_leaf", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> GREEN_ENCHANTED_HIT_PARTICLE = PARTICLE_TYPES.register("green_enchanted_hit", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> GREEN_LINE_PARTICLE = PARTICLE_TYPES.register("green_line", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> YELLOW_ELECTRIC_PARTICLE = PARTICLE_TYPES.register("yellow_electric", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> WHITE_BREATH_PARTICLE = PARTICLE_TYPES.register("white_breath", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> PURPLE_LIGHTNING = PARTICLE_TYPES.register("purple_lightning", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> SPLATTER_SAKURA = PARTICLE_TYPES.register("splatter_sakura", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> RADIANT_SHATTER = PARTICLE_TYPES.register("radiant_shatter", () -> new SimpleParticleType(false));

    public static final RegistryObject<HitParticleType> FIRE_HIT_SLASH = PARTICLE_TYPES.register("fire_hit_slash", () -> new HitParticleType(true));;

    public static final RegistryObject<ParticleType<CustomZapParticleOption>> CUSTOM_ZAP_PARTICLE = PARTICLE_TYPES.register("custom_zap", () -> new ParticleType<>(false, CustomZapParticleOption.DESERIALIZER) {
        public Codec<CustomZapParticleOption> codec() {
            return CustomZapParticleOption.CODEC;
        }
    });

    public static final RegistryObject<ParticleType<ColoredEndRodParticleOption>> COLORED_END_ROD_PARTICLE = PARTICLE_TYPES.register("colored_end_rod", () -> new ParticleType<>(false, ColoredEndRodParticleOption.DESERIALIZER) {
        public Codec<ColoredEndRodParticleOption> codec() {
            return ColoredEndRodParticleOption.CODEC;
        }
    });

    public static final RegistryObject<ParticleType<GreenCatParticleOption>> GREEN_CAT = PARTICLE_TYPES.register("green_cat", () -> new ParticleType<>(false, GreenCatParticleOption.DESERIALIZER) {
        public Codec<GreenCatParticleOption> codec() {
            return GreenCatParticleOption.CODEC;
        }
    });

    public static final RegistryObject<ParticleType<ColoredCherryParticleOption>> COLORED_CHERRY_PARTICLE = PARTICLE_TYPES.register("colored_cherry", () -> new ParticleType<>(false, ColoredCherryParticleOption.DESERIALIZER) {
        public Codec<ColoredCherryParticleOption> codec() {
            return ColoredCherryParticleOption.CODEC;
        }
    });

    public static final RegistryObject<ParticleType<BlinkLeafParticleOptions>> BLINK_LEAF = PARTICLE_TYPES.register("blink_leaf", () -> new ParticleType<>(false, BlinkLeafParticleOptions.DESERIALIZER) {
        public Codec<BlinkLeafParticleOptions> codec() {
            return BlinkLeafParticleOptions.CODEC;
        }
    });

    public static final RegistryObject<ParticleType<EmbraceLeafParticleOption>> EMBRACE_LEAF = PARTICLE_TYPES.register("embrace_leaf", () -> new ParticleType<>(false, EmbraceLeafParticleOption.DESERIALIZER) {
        public Codec<EmbraceLeafParticleOption> codec() {
            return EmbraceLeafParticleOption.CODEC;
        }
    });

    public static final RegistryObject<ParticleType<EmbraceMoteParticleOption>> EMBRACE_MOTE = PARTICLE_TYPES.register("embrace_mote", () -> new ParticleType<>(false, EmbraceMoteParticleOption.DESERIALIZER) {
        public Codec<EmbraceMoteParticleOption> codec() {
            return EmbraceMoteParticleOption.CODEC;
        }
    });
}
