package net.offkung.bhspells.compat.epicfight;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.offkung.bhspells.compat.api.AnimationRequest;
import net.offkung.bhspells.compat.api.CompatResult;
import net.offkung.bhspells.compat.epicfight.client.EpicFightLegPoseHelper;
import net.offkung.bhspells.compat.epicfight.client.GildedHareEpicFightRenderCompat;
import net.offkung.bhspells.compat.epicfight.client.JadeAuraEpicFightRenderCompat;
import net.offkung.bhspells.compat.epicfight.common.animation.EpicFightAnimationPlayer;
import net.offkung.bhspells.compat.epicfight.common.animation.IronSpellAnimations;
import net.offkung.bhspells.compat.epicfight.common.particle.FractureVfx;

/**
 * Internal loaded bridge for direct Epic Fight interactions.
 * Loaded ONLY after confirming epicfight is present in ModList.
 */
public final class EpicFightLoadedBridge {
    private EpicFightLoadedBridge() {
    }

    public static void registerModEvents(IEventBus modEventBus) {
        modEventBus.addListener(IronSpellAnimations::registerAnimations);
        modEventBus.addListener(net.offkung.bhspells.compat.epicfight.skills.dingshenfa.DingShenFaSkills::buildSkills);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> {
                    GildedHareEpicFightRenderCompat.registerModEvents(modEventBus);
                    JadeAuraEpicFightRenderCompat.registerModEvents(modEventBus);
                });
    }

    public static CompatResult playAnimation(AnimationRequest request) {
        return EpicFightAnimationPlayer.play(request);
    }

    public static boolean trySpawnFracture(LivingEntity source, Level level, Vec3 samplePosition,
            int searchUp, int searchDown, double radius) {
        return FractureVfx.trySpawnFracture(source, level, samplePosition, searchUp, searchDown, radius);
    }

    public static boolean isBattleMode(LivingEntity entity) {
        return EpicFightLegPoseHelper.isBattleMode(entity);
    }

    public static boolean isDodging(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        try {
            yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch<?> patch =
                yesman.epicfight.world.capabilities.EpicFightCapabilities.getEntityPatch(entity, yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch.class);
            if (patch instanceof yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch<?> playerPatch) {
                yesman.epicfight.skill.SkillContainer container = playerPatch.getSkill(yesman.epicfight.skill.SkillSlots.DODGE);
                if (container != null && container.getSkill() != null) {
                    return container.isActivated();
                }
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    public static void stopAnimation(LivingEntity entity) {
        if (entity == null) {
            return;
        }
        yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch<?> patch =
            yesman.epicfight.world.capabilities.EpicFightCapabilities.getEntityPatch(entity, yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch.class);
        if (patch != null && patch.getAnimator() != null) {
            patch.playAnimationSynchronized(yesman.epicfight.gameasset.Animations.BIPED_IDLE, 0.15F);
        }
    }

    public static boolean triggerPhantomDodge(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        if (!entity.level().isClientSide) {
            yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch<?> patch =
                    yesman.epicfight.world.capabilities.EpicFightCapabilities.getEntityPatch(entity, yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch.class);
            if (patch != null) {
                var dodgeAnim = net.offkung.bhspells.compat.epicfight.skills.phantom_dodge.PhantomDodgeAnimations.getRandomDodgeAnimation(entity.getRandom());
                if (dodgeAnim != null) {
                    patch.playAnimationSynchronized(dodgeAnim, 0.1F);
                }
            }
        }
        net.offkung.bhspells.compat.epicfight.common.particle.AfterimageVfx.spawnWhiteAfterimage(entity.level(), entity);
        return true;
    }
}
