package net.offkung.bhspells.service;

import net.offkung.bhspells.registry.MobEffectsRegistry;
import io.redspace.ironsspellbooks.network.casting.CancelCastPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Universal stun and control-lock service for Earth Roar.
 * Guarantees a full 100-tick (5.0s) stun across ALL LivingEntities, including bosses.
 * Handles shield destruction/disabling, motion freezing, and attack/cast cancellation.
 * Strictly prevents floating: suppresses upward lift, strips levitation, and keeps entities grounded.
 */
@Mod.EventBusSubscriber
public final class EarthRoarStunService {
    public static final String TAG_EARTH_ROAR_STUNNED = "bhspells:earth_roar_stunned";
    private static final String DATA_ORIGINAL_NO_AI = "bhspells:er_stun_original_no_ai";
    private static final String DATA_END_TICK = "bhspells:er_stun_end_tick";

    private static final Map<UUID, StunTracker> ACTIVE_STUNS = new ConcurrentHashMap<>();

    private record StunTracker(UUID entityUuid, int startTick, int endTick, int knockbackEndTick, boolean originalNoAi) {}

    private EarthRoarStunService() {}

    /**
     * Breaks or disables any actively blocking shield on the target.
     */
    public static void breakShieldIfBlocking(LivingEntity target) {
        if (target == null || target.level().isClientSide) {
            return;
        }

        if (target.isUsingItem()) {
            ItemStack activeItem = target.getUseItem();
            boolean isShield = activeItem.is(Items.SHIELD) || activeItem.canPerformAction(ToolActions.SHIELD_BLOCK);
            if (isShield) {
                InteractionHand hand = target.getUsedItemHand();
                target.stopUsingItem();
                if (target instanceof Player player) {
                    player.disableShield(true);
                    if (player instanceof ServerPlayer serverPlayer) {
                        serverPlayer.getCooldowns().addCooldown(activeItem.getItem(), 100);
                    }
                }
                activeItem.hurtAndBreak(50, target, (e) -> e.broadcastBreakEvent(hand));
            }
        }
    }

    /**
     * Applies the universal 100-tick (5.0 second) Earth Roar stun to the target without initial knockback.
     */
    public static void applyStun(LivingEntity target, int durationTicks) {
        applyStunWithKnockback(target, durationTicks, null);
    }

    /**
     * Applies the universal 100-tick (5.0 second) Earth Roar stun to the target while allowing
     * a purely horizontal ground slide trajectory (~10 ticks) without any floating or upward launch.
     */
    public static void applyStunWithKnockback(LivingEntity target, int durationTicks, Vec3 knockbackVelocity) {
        if (target == null || target.level().isClientSide) {
            return;
        }

        // Strip any active levitation to guarantee no floating
        target.removeEffect(MobEffects.LEVITATION);

        // Apply strictly horizontal ground velocity - no upward impulse or float
        if (knockbackVelocity != null) {
            Vec3 flatVelocity = new Vec3(knockbackVelocity.x, Math.min(target.getDeltaMovement().y, 0.0D), knockbackVelocity.z);
            target.setDeltaMovement(flatVelocity);
            target.hasImpulse = true;
            target.hurtMarked = true;
            if (target instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(target));
            }
        }

        if (!target.isAlive()) {
            return;
        }

        // Cancel any active spellcasting or item usage
        target.stopUsingItem();
        if (target instanceof ServerPlayer serverPlayer) {
            CancelCastPacket.cancelCast(serverPlayer, false);
        }

        int currentTick = (int) target.level().getGameTime();
        int newEndTick = currentTick + durationTicks;
        int knockbackDuration = (knockbackVelocity != null ? 10 : 0);
        int knockbackEndTick = currentTick + knockbackDuration;

        boolean originalNoAi = false;
        StunTracker existing = ACTIVE_STUNS.get(target.getUUID());
        if (existing != null) {
            // Preserve original AI state if already stunned; only extend duration
            originalNoAi = existing.originalNoAi;
            newEndTick = Math.max(existing.endTick, newEndTick);
        } else if (target.getPersistentData().contains(DATA_ORIGINAL_NO_AI)) {
            originalNoAi = target.getPersistentData().getBoolean(DATA_ORIGINAL_NO_AI);
        } else if (target instanceof Mob mob) {
            originalNoAi = mob.isNoAi();
            target.getPersistentData().putBoolean(DATA_ORIGINAL_NO_AI, originalNoAi);
        }

        target.addTag(TAG_EARTH_ROAR_STUNNED);
        target.getPersistentData().putInt(DATA_END_TICK, newEndTick);

        if (target instanceof Mob mob) {
            mob.getNavigation().stop();
            mob.setTarget(null);
            // Only set NoAI immediately if already stationary on ground and not in slide
            if (knockbackVelocity == null && target.onGround()) {
                mob.setNoAi(true);
            }
        }

        if (knockbackVelocity == null && target.onGround()) {
            target.setDeltaMovement(Vec3.ZERO);
            target.hasImpulse = true;
        }

        ACTIVE_STUNS.put(target.getUUID(), new StunTracker(target.getUUID(), currentTick, newEndTick, knockbackEndTick, originalNoAi));

        // Add visual MobEffect without potion swirl particles (visible: false, showIcon: true)
        target.addEffect(new MobEffectInstance(MobEffectsRegistry.EARTH_ROAR_STUN.get(), durationTicks, 0, false, false, true));
    }

    public static boolean isStunned(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        return entity.getTags().contains(TAG_EARTH_ROAR_STUNNED) || ACTIVE_STUNS.containsKey(entity.getUUID());
    }

    public static void enforceStun(LivingEntity entity) {
        StunTracker tracker = ACTIVE_STUNS.get(entity.getUUID());
        int currentTick = (int) entity.level().getGameTime();
        enforceStun(entity, tracker, currentTick);
    }

    public static void enforceStun(LivingEntity entity, StunTracker tracker, int currentTick) {
        if (entity == null || entity.level().isClientSide) {
            return;
        }

        if (entity instanceof ServerPlayer serverPlayer) {
            CancelCastPacket.cancelCast(serverPlayer, false);
        }
        entity.stopUsingItem();

        // While in knockback slide phase, let horizontal slide continue along ground
        boolean inKnockback = tracker != null && currentTick < tracker.knockbackEndTick;

        if (inKnockback) {
            if (entity instanceof Mob mob) {
                mob.getNavigation().stop();
                mob.setTarget(null);
            }
            // Strict anti-float: immediately nullify any upward velocity
            if (entity.getDeltaMovement().y > 0.0D) {
                entity.setDeltaMovement(entity.getDeltaMovement().x, 0.0D, entity.getDeltaMovement().z);
                entity.hasImpulse = true;
            }
        } else {
            // Once knockback settles: firmly pin the entity to the ground (no floating)
            if (entity.onGround()) {
                if (entity instanceof Mob mob && !mob.isNoAi()) {
                    mob.setNoAi(true);
                    mob.getNavigation().stop();
                    mob.setTarget(null);
                }
                entity.setDeltaMovement(0, 0, 0);
                entity.hasImpulse = true;
            } else {
                // If airborne for any reason (e.g. falling or ledge), keep NoAi false so gravity pulls entity down quickly
                if (entity instanceof Mob mob && mob.isNoAi()) {
                    mob.setNoAi(false);
                }
                // Pull downwards with gravity towards the ground - never allow hovering
                entity.setDeltaMovement(0, Math.min(entity.getDeltaMovement().y - 0.08D, -0.3D), 0);
                entity.hasImpulse = true;
            }
        }
    }

    public static void cleanupStun(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide) {
            return;
        }

        entity.removeTag(TAG_EARTH_ROAR_STUNNED);
        entity.getPersistentData().remove(DATA_END_TICK);
        StunTracker tracker = ACTIVE_STUNS.remove(entity.getUUID());

        if (entity instanceof Mob mob) {
            boolean originalNoAi = tracker != null ? tracker.originalNoAi : entity.getPersistentData().getBoolean(DATA_ORIGINAL_NO_AI);
            entity.getPersistentData().remove(DATA_ORIGINAL_NO_AI);
            mob.setNoAi(originalNoAi);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity == null || entity.level().isClientSide) {
            return;
        }

        if (isStunned(entity)) {
            int currentTick = (int) entity.level().getGameTime();
            StunTracker tracker = ACTIVE_STUNS.get(entity.getUUID());
            int endTick = tracker != null ? tracker.endTick : entity.getPersistentData().getInt(DATA_END_TICK);

            if (currentTick >= endTick || !entity.isAlive()) {
                cleanupStun(entity);
            } else {
                enforceStun(entity, tracker, currentTick);
            }
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (isStunned(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker && isStunned(attacker)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (isStunned(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (isStunned(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (isStunned(event.getEntity())) {
            event.getEntity().setDeltaMovement(event.getEntity().getDeltaMovement().multiply(1.0D, 0.0D, 1.0D));
        }
    }
}
