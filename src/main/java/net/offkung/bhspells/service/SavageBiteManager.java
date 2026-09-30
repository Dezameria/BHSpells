package net.offkung.bhspells.service;

import net.offkung.bhspells.compat.api.AnimationCue;
import net.offkung.bhspells.compat.epicfight.EpicFightCompat;
import net.offkung.bhspells.config.SpellConfig;
import net.offkung.bhspells.network.savage_bite.SavageBiteNetwork;
import net.offkung.bhspells.network.savage_bite.SavageBiteStatePacket;
import net.offkung.bhspells.spells.gold.SavageBiteSpell;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.network.SyncManaPacket;
import io.redspace.ironsspellbooks.setup.Messages;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Authoritative Server Manager for Savage Bite lifecycle:
 * LUNGING -> LATCHED -> FINISHED.
 */
public final class SavageBiteManager {
    public enum State {
        LUNGING,
        LATCHED,
        FINISHED
    }

    public static class Session {
        public final LivingEntity caster;
        public final LivingEntity target;
        public final int spellLevel;
        public final SavageBiteSpell spell;
        public State state;
        public int ticksInState;
        public float steeringForward;
        public float steeringStrafe;
        public int lastInputTick;
        public boolean hasLatched;

        public Session(LivingEntity caster, LivingEntity target, int spellLevel, SavageBiteSpell spell) {
            this.caster = caster;
            this.target = target;
            this.spellLevel = spellLevel;
            this.spell = spell;
            this.state = State.LUNGING;
            this.ticksInState = 0;
            this.steeringForward = 0.0F;
            this.steeringStrafe = 0.0F;
            this.lastInputTick = 0;
            this.hasLatched = false;
        }
    }

    private static final Map<UUID, Session> ACTIVE_SESSIONS = new ConcurrentHashMap<>();

    private SavageBiteManager() {
    }

    public static boolean hasActiveSession(LivingEntity caster) {
        Session session = ACTIVE_SESSIONS.get(caster.getUUID());
        return session != null && session.state != State.FINISHED;
    }

    public static void startLunge(LivingEntity caster, LivingEntity target, int spellLevel, SavageBiteSpell spell) {
        if (caster.level().isClientSide || target == null || hasActiveSession(caster)) {
            return;
        }

        Session session = new Session(caster, target, spellLevel, spell);
        ACTIVE_SESSIONS.put(caster.getUUID(), session);

        SavageBiteNetwork.sendState(caster, target, SavageBiteStatePacket.STATE_LUNGING);
    }

    public static void updateSteeringInput(LivingEntity caster, float forward, float strafe) {
        Session session = ACTIVE_SESSIONS.get(caster.getUUID());
        if (session != null && session.state == State.LATCHED) {
            float safeF = Float.isFinite(forward) ? forward : 0.0F;
            float safeS = Float.isFinite(strafe) ? strafe : 0.0F;
            session.steeringForward = Mth.clamp(safeF, -1.0F, 1.0F);
            session.steeringStrafe = Mth.clamp(safeS, -1.0F, 1.0F);
            session.lastInputTick = session.ticksInState;
        }
    }

    public static void cancelByCaster(LivingEntity caster, String reason) {
        cancelByEntity(caster, reason);
    }

    public static void cancelByEntity(LivingEntity entity, String reason) {
        if (entity == null) {
            return;
        }
        Session session = ACTIVE_SESSIONS.get(entity.getUUID());
        if (session != null) {
            terminateSession(session, reason);
        }
        for (Session s : ACTIVE_SESSIONS.values()) {
            if (s.target == entity) {
                terminateSession(s, reason);
            }
        }
    }

    public static boolean isCasterLatched(LivingEntity caster) {
        Session session = ACTIVE_SESSIONS.get(caster.getUUID());
        return session != null && session.state == State.LATCHED;
    }

    public static boolean isTargetLatched(LivingEntity target) {
        for (Session session : ACTIVE_SESSIONS.values()) {
            if (session.target == target && session.state == State.LATCHED) {
                return true;
            }
        }
        return false;
    }

    public static void onServerTick() {
        if (ACTIVE_SESSIONS.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<UUID, Session>> it = ACTIVE_SESSIONS.entrySet().iterator();
        while (it.hasNext()) {
            Session session = it.next().getValue();
            session.ticksInState++;

            if (session.state == State.LUNGING) {
                tickLunging(session);
            } else if (session.state == State.LATCHED) {
                tickLatched(session);
            }

            if (session.state == State.FINISHED) {
                cleanupSession(session);
                it.remove();
            }
        }
    }

    private static void tickLunging(Session session) {
        LivingEntity caster = session.caster;
        LivingEntity target = session.target;

        if (!caster.isAlive() || !target.isAlive() || target.isRemoved() || caster.isSpectator() || target.isSpectator() || caster.level() != target.level()) {
            session.state = State.FINISHED;
            return;
        }

        Vec3 casterPos = caster.position();
        Vec3 targetFeet = target.position();
        double distSq = casterPos.distanceToSqr(targetFeet);

        // Cancel if target teleports away (> 16 blocks)
        if (distSq > 256.0D) {
            session.state = State.FINISHED;
            return;
        }

        // Latch only when proximity threshold (1.4 blocks) is reached
        if (distSq <= 1.96D) {
            session.state = State.LATCHED;
            session.hasLatched = true;
            session.ticksInState = 0;

            // Snap directly to target feet
            caster.teleportTo(targetFeet.x, targetFeet.y, targetFeet.z);
            caster.setDeltaMovement(target.getDeltaMovement());

            // Trigger Epic Fight swim animation
            EpicFightCompat.playAnimation(caster, AnimationCue.SAVAGE_BITE, 0.10F);

            // Sync latched state to clients
            SavageBiteNetwork.sendState(caster, target, SavageBiteStatePacket.STATE_LATCHED);
            return;
        }

        // Timeout if target not reached within 12 ticks
        if (session.ticksInState >= 12) {
            session.state = State.FINISHED;
            return;
        }

        // Apply lunge impulse toward target
        Vec3 dir = targetFeet.subtract(casterPos).normalize();
        caster.setDeltaMovement(dir.scale(1.15D));
        caster.hurtMarked = true;
    }

    private static void tickLatched(Session session) {
        LivingEntity caster = session.caster;
        LivingEntity target = session.target;

        // Termination conditions
        if (!caster.isAlive() || caster.getHealth() <= 0.0F || caster.isSpectator()) {
            terminateSession(session, "caster_dead_or_spectator");
            return;
        }
        if (!target.isAlive() || target.isRemoved() || target.isSpectator()) {
            terminateSession(session, "target_dead_or_spectator");
            return;
        }
        if (caster.level() != target.level()) {
            terminateSession(session, "dimension_change");
            return;
        }
        if (caster.distanceToSqr(target) > 256.0D) { // Teleport distance threshold (> 16 blocks)
            terminateSession(session, "target_teleported");
            return;
        }

        // Expire stale steering input if no packet received within 10 ticks
        if (session.ticksInState - session.lastInputTick > 10) {
            session.steeringForward = 0.0F;
            session.steeringStrafe = 0.0F;
        }

        // Authoritative positioning tethered to target's ankle
        Vec3 targetPos = target.position();
        Vec3 lookAngle = caster.getLookAngle();
        Vec3 forwardVec = new Vec3(lookAngle.x, 0.0, lookAngle.z).normalize();
        Vec3 strafeVec = new Vec3(-forwardVec.z, 0.0, forwardVec.x);

        Vec3 steering = forwardVec.scale(session.steeringForward * 0.18D)
                .add(strafeVec.scale(session.steeringStrafe * 0.18D));

        double targetX = targetPos.x + steering.x;
        double targetY = targetPos.y;
        double targetZ = targetPos.z + steering.z;

        caster.teleportTo(targetX, targetY, targetZ);
        caster.setDeltaMovement(target.getDeltaMovement().scale(0.85D).add(steering.scale(0.5D)));
        caster.fallDistance = 0.0F;

        // Periodic cadence: every 20 ticks (1 second)
        if (session.ticksInState % 20 == 0) {
            MagicData magicData = MagicData.getPlayerMagicData(caster);
            int manaDrain = SpellConfig.SavageBite.getManaDrainPerSecond();

            // Mana exhaustion check
            if (magicData.getMana() < manaDrain) {
                terminateSession(session, "mana_exhausted");
                return;
            }
            magicData.setMana(magicData.getMana() - manaDrain);
            if (caster instanceof ServerPlayer serverPlayer) {
                Messages.sendToPlayer(new SyncManaPacket(magicData), serverPlayer);
            }

            if (magicData.getMana() <= 0.0F) {
                terminateSession(session, "mana_depleted");
                return;
            }

            // Apply periodic damage (gentle bite)
            float damage = SpellConfig.SavageBite.getBaseDamage();
            DamageSources.applyDamage(target, damage, session.spell.getDamageSource(caster));

            // Refresh Slowness II
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1, false, false, true));

            // VFX & SFX at target's ankle
            if (caster.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.WAX_ON, target.getX(), target.getY() + 0.15D, target.getZ(), 8, 0.25D, 0.10D, 0.25D, 0.05D);
                serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.FOX_BITE, SoundSource.PLAYERS, 1.2F, 1.0F);
            }
        }
    }

    private static void terminateSession(Session session, String reason) {
        session.state = State.FINISHED;
    }

    private static void cleanupSession(Session session) {
        LivingEntity caster = session.caster;
        LivingEntity target = session.target;

        // 1. Release Epic Fight swim animation only if session actually latched
        if (session.hasLatched) {
            EpicFightCompat.stopAnimation(caster);
        }

        // 2. Notify clients
        SavageBiteNetwork.sendState(caster, target, SavageBiteStatePacket.STATE_END);

        // 3. Apply deferred cooldown only if session actually latched
        if (session.hasLatched) {
            MagicData magicData = MagicData.getPlayerMagicData(caster);
            int cooldownTicks = (int) (SpellConfig.SavageBite.getCooldown() * 20);
            magicData.getPlayerCooldowns().addCooldown(session.spell, cooldownTicks);

            if (caster instanceof ServerPlayer serverPlayer) {
                magicData.getPlayerCooldowns().syncToPlayer(serverPlayer);
            }
        }
    }

    public static void cleanupOnServerStopping() {
        for (Session session : ACTIVE_SESSIONS.values()) {
            cleanupSession(session);
        }
        ACTIVE_SESSIONS.clear();
    }
}
