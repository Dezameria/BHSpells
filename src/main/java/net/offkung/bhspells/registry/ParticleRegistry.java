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
    public static final Supplier<SimpleParticleType> GREEN_CROSS_PARTICLE = PARTICLE_TYPES.register("green_cross", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> OAK_LEAF_PARTICLE = PARTICLE_TYPES.register("oak_leaf", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> GREEN_ENCHANTED_HIT_PARTICLE = PARTICLE_TYPES.register("green_enchanted_hit", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> GREEN_LINE_PARTICLE = PARTICLE_TYPES.register("green_line", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> YELLOW_ELECTRIC_PARTICLE = PARTICLE_TYPES.register("yellow_electric", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> WHITE_BREATH_PARTICLE = PARTICLE_TYPES.register("white_breath", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> PURPLE_LIGHTNING = PARTICLE_TYPES.register("purple_lightning", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> SPLATTER_SAKURA = PARTICLE_TYPES.register("splatter_sakura", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> RADIANT_SHATTER = PARTICLE_TYPES.register("radiant_shatter", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> GOLDEN_CRIT = PARTICLE_TYPES.register("golden_crit", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> PINK_DRAGON_FIRE = PARTICLE_TYPES.register("pink_dragon_fire", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> PINK_EMBERS = PARTICLE_TYPES.register("pink_embers", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> PINK_FIRE = PARTICLE_TYPES.register("pink_fire", () -> new SimpleParticleType(false));
    public static final Supplier<HitParticleType> FIRE_HIT_SLASH = PARTICLE_TYPES.register("fire_hit_slash", () -> new HitParticleType(true));
    public static final Supplier<SimpleParticleType> WHITE_FIRE = PARTICLE_TYPES.register("white_fire", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> WHITE_EMBER = PARTICLE_TYPES.register("white_ember", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> WHITE_FIRE_EMITTER = PARTICLE_TYPES.register("white_fire_emitter", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> RED_PLUM = PARTICLE_TYPES.register("red_plum", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> GILDED_HARE = PARTICLE_TYPES.register("gilded_hare", () -> new SimpleParticleType(false));

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

    public static final RegistryObject<ParticleType<FallingLeafParticleOption>> FALLING_LEAF_PARTICLE = PARTICLE_TYPES.register("falling_leaf", () -> new ParticleType<>(false, FallingLeafParticleOption.DESERIALIZER) {
        public Codec<FallingLeafParticleOption> codec() {
            return FallingLeafParticleOption.CODEC;
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

    public static final RegistryObject<ParticleType<ColoredMyceliumParticleOptions>> COLORED_MYCELIUM_PARTICLE = PARTICLE_TYPES.register("colored_mycelium", () -> new ParticleType<>(false, ColoredMyceliumParticleOptions.DESERIALIZER) {
        public Codec<ColoredMyceliumParticleOptions> codec() {
            return ColoredMyceliumParticleOptions.CODEC;
        }
    });

    public static final RegistryObject<ParticleType<GrayFlowerParticleOption>> GRAY_FLOWER_PARTICLE = PARTICLE_TYPES.register("gray_flower_front", () -> new ParticleType<>(false, GrayFlowerParticleOption.DESERIALIZER) {
        public Codec<GrayFlowerParticleOption> codec() {
            return GrayFlowerParticleOption.CODEC;
        }
    });

    public static final RegistryObject<ParticleType<AmethystShardParticleOption>> AMETHYST_SHARD = PARTICLE_TYPES.register("amethyst_shard", () -> new ParticleType<>(false, AmethystShardParticleOption.DESERIALIZER) {
        public Codec<AmethystShardParticleOption> codec() {
            return AmethystShardParticleOption.CODEC;
        }
    });

    public static final RegistryObject<ParticleType<ZapParticleOptionCustom>> ZAP_CUSTOM = PARTICLE_TYPES.register("zap_custom", () -> new ParticleType<>(false, ZapParticleOptionCustom.DESERIALIZER) {
        public Codec<ZapParticleOptionCustom> codec() {
            return ZapParticleOptionCustom.CODEC;
        }
    });

    public static final RegistryObject<ParticleType<ShockwaveParticleOptionCustom>> SHOCKWAVE_CUSTOM = PARTICLE_TYPES.register("shockwave_custom", () -> new ParticleType<>(false, ShockwaveParticleOptionCustom.DESERIALIZER) {
        public Codec<ShockwaveParticleOptionCustom> codec() {
            return ShockwaveParticleOptionCustom.CODEC;
        }
    });
    public static final Supplier<SimpleParticleType> DING = PARTICLE_TYPES.register("ding", () -> new SimpleParticleType(false));
    public static final RegistryObject<ParticleType<ShockingBeamParticleOption>> SHOCKING_BEAM = PARTICLE_TYPES.register("shocking_beam", () -> new ParticleType<>(false, ShockingBeamParticleOption.DESERIALIZER) {
        public Codec<ShockingBeamParticleOption> codec() {
            return ShockingBeamParticleOption.CODEC;
        }
    });

}
