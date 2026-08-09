package net.offkung.bhspells.entity.spells.eternal_purification;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public interface IPurificationEntity {
    float getResonanceMultiplier();

    default void onResonanceActivated(PurificationPillarEntity pillar, PurificationPillarEntity.Stage stage, float resonanceRadius, float baseDamage, float finalDamage, LivingEntity owner) {
    }

    default void onResonanceShockwave(PurificationPillarEntity pillar, PurificationPillarEntity.Stage stage, LivingEntity owner, LivingEntity target, Level world, Vec3 resonancePosition, float resonanceRadius, double distanceFromSource, float baseDamage, float finalDamage) {
    }

    default Vector3f getResonanceParticleColor() {
        return new Vector3f(1.0F, 0.42F, 0.29F);
    }
}
