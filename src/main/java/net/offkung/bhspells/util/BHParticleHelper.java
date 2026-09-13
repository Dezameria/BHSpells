package net.offkung.bhspells.util;

import com.gametechbc.traveloptics.particle.glowing_enchantment.GlowingEnchantmentParticleOptions;
import io.redspace.ironsspellbooks.particle.SparkParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.offkung.bhspells.client.particle.ColoredEndRodParticleOption;
import net.offkung.bhspells.registry.ParticleRegistry;
import org.joml.Vector3f;

public class BHParticleHelper {
    public static final ParticleOptions GREEN_ENCHANTED_HIT = ParticleRegistry.GREEN_ENCHANTED_HIT_PARTICLE.get();
    public static final ParticleOptions YELLOW_ELECTRIC = ParticleRegistry.YELLOW_ELECTRIC_PARTICLE.get();
    public static final ParticleOptions OAK_LEAF = ParticleRegistry.OAK_LEAF_PARTICLE.get();
    public static final ParticleOptions COLORED_END_ROD = new ColoredEndRodParticleOption(1.0F, 1.0F, 1.0F);
    public static final ParticleOptions PILLAR_GREEN_PASSIVE_ENCHANT = new GlowingEnchantmentParticleOptions(new Vector3f(0.458824F, 0.796078F, 0.309804F), 0.06F, false, 12);
    public static final ParticleOptions RED_SPARKS = new SparkParticleOptions(new Vector3f(1, 0f, 0f));
    public static final ParticleOptions ORANGE_SPARKS = new SparkParticleOptions(new Vector3f(1f, 0.35f, 0f));
}
