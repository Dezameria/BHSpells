package net.offkung.bhspells.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.offkung.bhspells.entity.spells.supporting_bamboo.SupportingBamboo;

import java.util.List;

@Mod.EventBusSubscriber
public class SupportingBambooEvents {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();

        // Only run on the server
        if (target.level().isClientSide) return;

        // Ignore non-combat damage (fire, fall, drowning, etc.)
        DamageSource source = event.getSource();
        if (source.is(DamageTypeTags.BYPASSES_EFFECTS)) return;
        if (source.is(DamageTypeTags.BYPASSES_ARMOR)) return;
        if (source.is(DamageTypeTags.IS_FIRE)) return;
        if (source.is(DamageTypeTags.IS_FALL)) return;
        if (source.is(DamageTypeTags.IS_DROWNING)) return;
        if (source.is(DamageTypeTags.IS_EXPLOSION)) return;

        // Must be a projectile, player, or mob attack
        boolean isProjectile = source.getDirectEntity() instanceof Projectile;
        boolean isPlayerOrMob = source.getDirectEntity() instanceof LivingEntity;
        if (!isProjectile && !isPlayerOrMob) return;

        // Find any active SupportingBamboo whose owner is this target
        if (!(target.level() instanceof ServerLevel serverLevel)) return;

        Vec3 targetPos = target.position();
        double searchRadius = SupportingBamboo.RADIUS + 5; // small extra margin
        AABB searchBox = new AABB(
                targetPos.x - searchRadius, targetPos.y - 5, targetPos.z - searchRadius,
                targetPos.x + searchRadius, targetPos.y + 15, targetPos.z + searchRadius
        );

        List<SupportingBamboo> bamboos = serverLevel.getEntitiesOfClass(
                SupportingBamboo.class,
                searchBox,
                bamboo -> {
                    if (!bamboo.isAlive()) return false;
                    LivingEntity owner = bamboo.getOwner();
                    if (owner == null || owner != target) return false;
                    // Caster must be within the bamboo's radius
                    return bamboo.position().distanceTo(targetPos) <= SupportingBamboo.RADIUS;
                }
        );

        for (SupportingBamboo bamboo : bamboos) {
            bamboo.triggerRetaliationWave();
        }
    }
}
