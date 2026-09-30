package net.offkung.bhspells.service;

import net.offkung.bhspells.compat.api.AnimationCue;
import net.offkung.bhspells.compat.epicfight.EpicFightCompat;
import net.offkung.bhspells.spells.ground.EarthRoarSpell;
import io.redspace.ironsspellbooks.damage.DamageSources;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-authoritative 36-tick state machine for Earth Roar:
 * - Ticks 0-11: Kneel & Charge earth energy (epicfight:biped/living/kneel)
 * - Ticks 12-19: Initial high-speed dash (epicfight:biped/living/sneak)
 * - Ticks 20-27: WOM Kick 3 attack dash (wom:biped/skill/kick_3) with swept collision & stun
 * - Ticks 28-35: Recovery & landing
 */
public final class EarthRoarDashManager {
    public static final int TOTAL_TIMELINE_TICKS = 36;
    public static final int CHARGE_END_TICK = 11;
    public static final int INITIAL_DASH_START_TICK = 12;
    public static final int INITIAL_DASH_END_TICK = 19;
    public static final int KICK_START_TICK = 20;
    public static final int KICK_END_TICK = 27;
    public static final int DASH_START_TICK = INITIAL_DASH_START_TICK;
    public static final int DASH_END_TICK = KICK_END_TICK;
    public static final double TOTAL_DASH_DISTANCE = EarthRoarSpell.DASH_DISTANCE;
    public static final int TOTAL_DASH_TICKS = DASH_END_TICK - DASH_START_TICK + 1; // 16 ticks
    public static final double BLOCKS_PER_DASH_TICK = TOTAL_DASH_DISTANCE / TOTAL_DASH_TICKS; // 2.5 blocks/tick = 40 blocks total
    public static final double MAX_STEP_UP = 1.25D;

    private static final Map<UUID, EarthRoarDashState> ACTIVE_DASHES = new ConcurrentHashMap<>();

    private EarthRoarDashManager() {}

    public static class EarthRoarDashState {
        public final UUID casterId;
        public final ServerLevel level;
        public final LivingEntity caster;
        public final Vec3 dashDirection;
        public final int spellLevel;
        public final float damage;
        public final EarthRoarSpell spell;
        public final Set<UUID> hitEntities = new HashSet<>();
        public int ticksElapsed = 0;
        public double distanceTraveled = 0.0D;
        public double lastFractureDistance = 0.0D;
        public boolean stopped = false;

        public EarthRoarDashState(LivingEntity caster, ServerLevel level, Vec3 dashDirection, int spellLevel, float damage, EarthRoarSpell spell) {
            this.casterId = caster.getUUID();
            this.level = level;
            this.caster = caster;
            this.dashDirection = dashDirection;
            this.spellLevel = spellLevel;
            this.damage = damage;
            this.spell = spell;
        }
    }

    public static void startDash(LivingEntity caster, int spellLevel, EarthRoarSpell spell) {
        if (caster == null || caster.level().isClientSide || !(caster.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        // Horizontal forward direction based on look angle
        Vec3 look = caster.getLookAngle();
        Vec3 horizontalDir = new Vec3(look.x, 0, look.z);
        if (horizontalDir.lengthSqr() < 1.0E-5D) {
            float yRotRad = (float) Math.toRadians(caster.getYRot());
            horizontalDir = new Vec3(-Math.sin(yRotRad), 0, Math.cos(yRotRad));
        } else {
            horizontalDir = horizontalDir.normalize();
        }

        float damage = spell.getDamage(spellLevel, caster);
        EarthRoarDashState state = new EarthRoarDashState(caster, serverLevel, horizontalDir, spellLevel, damage, spell);
        ACTIVE_DASHES.put(caster.getUUID(), state);

        // Play initial charge animation (kneel) and spawn ground fracture
        EpicFightCompat.playAnimation(caster, AnimationCue.EARTH_ROAR_CHARGE, 0.15F);
        EpicFightCompat.spawnFracture(caster, serverLevel, caster.position(), 2, 4, 3.2D);
        spawnGroundFractureVfx(serverLevel, caster.position());

        // Stomp sound immediately on cast
        serverLevel.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.PLAYERS, 1.2F, 0.7F);
        serverLevel.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.4F, 1.0F);
    }

    public static void tickDash(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide) {
            return;
        }

        EarthRoarDashState state = ACTIVE_DASHES.get(entity.getUUID());
        if (state == null) {
            return;
        }

        if (!state.caster.isAlive() || state.caster.isRemoved() || state.caster.isSpectator()
                || state.caster.level() != state.level
                || !state.level.getWorldBorder().isWithinBounds(state.caster.getBoundingBox())) {
            cancelDash(entity);
            return;
        }

        int tick = state.ticksElapsed;

        // Phase 1 (0-11): Kneel & charge earth energy (epicfight:biped/living/kneel)
        if (tick <= CHARGE_END_TICK) {
            state.caster.setDeltaMovement(state.caster.getDeltaMovement().multiply(0.1D, 1.0D, 0.1D));
            state.caster.hurtMarked = true;
            if (tick == 0) {
                spawnGroundFractureVfx(state.level, state.caster.position());
                EpicFightCompat.spawnFracture(state.caster, state.level, state.caster.position(), 2, 4, 3.2D);
                EpicFightCompat.playAnimation(state.caster, AnimationCue.EARTH_ROAR_CHARGE, 0.15F);
            }
            spawnLegEnergyVfx(state.level, state.caster, state.dashDirection);
        }
        // Phase 2 (12-19): Initial high-speed low dash (epicfight:biped/living/sneak)
        else if (tick <= INITIAL_DASH_END_TICK) {
            if (tick == INITIAL_DASH_START_TICK) {
                EpicFightCompat.playAnimation(state.caster, AnimationCue.EARTH_ROAR_DASH, 0.10F);
                EpicFightCompat.spawnFracture(state.caster, state.level, state.caster.position(), 2, 3, 2.4D);
                state.level.playSound(null, state.caster.getX(), state.caster.getY(), state.caster.getZ(),
                        SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.4F, 0.9F);
                state.level.playSound(null, state.caster.getX(), state.caster.getY(), state.caster.getZ(),
                        SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 1.2F, 1.5F);
            }
            if (!state.stopped && state.distanceTraveled < TOTAL_DASH_DISTANCE) {
                performDashStep(state);
            }
        }
        // Phase 3 (20-27): WOM Kick 3 attack dash (wom:biped/skill/kick_3)
        else if (tick <= KICK_END_TICK) {
            if (tick == KICK_START_TICK) {
                EpicFightCompat.playAnimation(state.caster, AnimationCue.EARTH_ROAR_KICK, 0.05F);
                EpicFightCompat.spawnFracture(state.caster, state.level, state.caster.position(), 2, 4, 3.5D);
                spawnGroundFractureVfx(state.level, state.caster.position());
                state.level.playSound(null, state.caster.getX(), state.caster.getY(), state.caster.getZ(),
                        SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.5F, 0.7F);
                state.level.playSound(null, state.caster.getX(), state.caster.getY(), state.caster.getZ(),
                        SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 1.2F, 1.4F);
            }
            if (!state.stopped && state.distanceTraveled < TOTAL_DASH_DISTANCE) {
                performDashStep(state);
            }
        }
        // Phase 4 (28-35): Recovery & landing
        else {
            state.caster.setDeltaMovement(state.caster.getDeltaMovement().multiply(0.5D, 1.0D, 0.5D));
            state.caster.hurtMarked = true;
        }

        state.ticksElapsed++;
        if (state.ticksElapsed >= TOTAL_TIMELINE_TICKS || state.stopped) {
            cancelDash(entity);
        }
    }

    private static void performDashStep(EarthRoarDashState state) {
        LivingEntity caster = state.caster;
        ServerLevel level = state.level;
        Vec3 dir = state.dashDirection;

        double targetDistThisTick = Math.min(BLOCKS_PER_DASH_TICK, TOTAL_DASH_DISTANCE - state.distanceTraveled);
        int subSteps = 5;
        double stepDist = targetDistThisTick / subSteps;

        Vec3 currentPos = caster.position();

        for (int i = 0; i < subSteps; i++) {
            Vec3 nextPos = currentPos.add(dir.scale(stepDist));

            // Check auto step-up if blocked ahead
            AABB movedBox = caster.getBoundingBox().move(nextPos.subtract(caster.position()));
            if (!level.noCollision(caster, movedBox)) {
                boolean stepped = false;
                // Try step-up between 0.25 and 1.25 blocks
                for (double up = 0.25D; up <= MAX_STEP_UP; up += 0.25D) {
                    Vec3 candidate = nextPos.add(0, up, 0);
                    AABB candidateBox = caster.getBoundingBox().move(candidate.subtract(caster.position()));
                    if (level.noCollision(caster, candidateBox)) {
                        nextPos = candidate;
                        stepped = true;
                        break;
                    }
                }
                if (!stepped) {
                    // Collision with an impassable wall: stop dash
                    state.stopped = true;
                    level.playSound(null, currentPos.x, currentPos.y, currentPos.z,
                            SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 1.2F);
                    break;
                }
            } else {
                // If on ground, snap slightly down if ground slopes downward
                if (caster.onGround()) {
                    Vec3 downCandidate = nextPos.subtract(0, 0.5D, 0);
                    AABB downBox = caster.getBoundingBox().move(downCandidate.subtract(caster.position()));
                    if (level.noCollision(caster, downBox)) {
                        // Check if ground exists within 1.25 blocks below
                        for (double down = 0.25D; down <= MAX_STEP_UP; down += 0.25D) {
                            Vec3 groundCandidate = nextPos.subtract(0, down, 0);
                            AABB groundBox = caster.getBoundingBox().move(groundCandidate.subtract(caster.position()));
                            if (!level.noCollision(caster, groundBox)) {
                                nextPos = nextPos.subtract(0, down - 0.25D, 0);
                                break;
                            }
                        }
                    }
                }
            }

            // Swept entity collision along this substep (expanded corridor 2.2D horizontally to catch grazing targets)
            AABB sweptAabb = new AABB(
                    Math.min(currentPos.x, nextPos.x) - 2.2D,
                    Math.min(currentPos.y, nextPos.y) - 0.5D,
                    Math.min(currentPos.z, nextPos.z) - 2.2D,
                    Math.max(currentPos.x, nextPos.x) + 2.2D,
                    Math.max(currentPos.y, nextPos.y) + caster.getBbHeight() + 0.6D,
                    Math.max(currentPos.z, nextPos.z) + 2.2D
            );

            // Filter targets excluding allies, friendly fire, prior hits, spectators, dead
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, sweptAabb,
                    e -> e != caster && e.isAlive() && !e.isSpectator()
                            && !state.hitEntities.contains(e.getUUID())
                            && !e.isAlliedTo(caster)
                            && !DamageSources.isFriendlyFireBetween(caster, e));

            for (LivingEntity target : targets) {
                state.hitEntities.add(target.getUUID());

                // 1. Break / disable shield
                EarthRoarStunService.breakShieldIfBlocking(target);

                // 2. Deal damage
                DamageSources.applyDamage(target, state.damage, state.spell.getDamageSource(caster));

                // 3. Outward lateral knockback (blows targets grazed on the sides away outward)
                Vec3 toTarget = target.position().subtract(currentPos);
                Vec3 leftNormal = new Vec3(-dir.z, 0, dir.x);
                double lateralOffset = toTarget.x * leftNormal.x + toTarget.z * leftNormal.z;
                Vec3 outwardSide;
                if (lateralOffset > 0.05D) {
                    outwardSide = leftNormal;
                } else if (lateralOffset < -0.05D) {
                    outwardSide = leftNormal.scale(-1.0D);
                } else {
                    outwardSide = (level.random.nextBoolean() ? leftNormal : leftNormal.scale(-1.0D));
                }

                double lateralDist = Math.abs(lateralOffset);
                double sideScale = Mth.clamp(1.2D + lateralDist * 0.9D, 1.4D, 2.6D);
                // Pure horizontal ground slide knockback (strictly no upward lift/floating)
                Vec3 kb = dir.scale(0.85D).add(outwardSide.scale(sideScale));

                // 4. Universal 100-tick Stun with ground slide knockback (strictly grounded, anti-float)
                EarthRoarStunService.applyStunWithKnockback(target, EarthRoarSpell.STUN_DURATION_TICKS, kb);

                // 5. Impact ground fracture under target
                EpicFightCompat.spawnFracture(caster, level, target.position(), 2, 3, 2.2D);
                spawnImpactFractureVfx(level, target.position());

                // Impact FX & audio
                level.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.9F, 1.2F);
                level.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.2F, 0.8F);
                level.sendParticles(ParticleTypes.EXPLOSION,
                        target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(),
                        1, 0, 0, 0, 0);
            }

            // Track horizontal distance traveled
            double horizontalStep = Math.sqrt(
                    (nextPos.x - currentPos.x) * (nextPos.x - currentPos.x) +
                    (nextPos.z - currentPos.z) * (nextPos.z - currentPos.z)
            );
            state.distanceTraveled += horizontalStep;
            currentPos = nextPos;

            // Continuous ground fractures every 4 blocks along dash path
            if (state.distanceTraveled - state.lastFractureDistance >= 4.0D) {
                EpicFightCompat.spawnFracture(caster, level, currentPos, 2, 3, 2.0D);
                state.lastFractureDistance = state.distanceTraveled;
            }

            // Cosmetic trail particles along path
            spawnDashTrailVfx(level, currentPos);

            if (state.stopped) {
                break;
            }
        }

        // Authoritative movement synchronization to client
        if (caster instanceof ServerPlayer player) {
            player.connection.teleport(currentPos.x, currentPos.y, currentPos.z, player.getYRot(), player.getXRot());
        } else {
            caster.setPos(currentPos.x, currentPos.y, currentPos.z);
        }
        caster.setDeltaMovement(dir.scale(1.4D));
        caster.hurtMarked = true;
        caster.hasImpulse = true;
        caster.resetFallDistance();
    }

    private static void spawnGroundFractureVfx(ServerLevel level, Vec3 pos) {
        BlockPos groundPos = BlockPos.containing(pos).below();
        BlockState state = level.getBlockState(groundPos);
        if (state.getRenderShape() != RenderShape.INVISIBLE) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                    pos.x, pos.y + 0.1D, pos.z,
                    25, 0.6D, 0.1D, 0.6D, 0.15D);
        }
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                pos.x, pos.y + 0.1D, pos.z,
                10, 0.5D, 0.1D, 0.5D, 0.05D);
    }

    private static void spawnImpactFractureVfx(ServerLevel level, Vec3 pos) {
        BlockPos groundPos = BlockPos.containing(pos).below();
        BlockState state = level.getBlockState(groundPos);
        if (state.getRenderShape() != RenderShape.INVISIBLE) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                    pos.x, pos.y + 0.1D, pos.z,
                    16, 0.6D, 0.2D, 0.6D, 0.22D);
        }
    }

    private static void spawnLegEnergyVfx(ServerLevel level, LivingEntity caster, Vec3 dir) {
        Vec3 pos = caster.position().add(0, 0.3D, 0);
        level.sendParticles(ParticleTypes.CRIT,
                pos.x, pos.y, pos.z,
                6, 0.3D, 0.3D, 0.3D, 0.1D);
        level.sendParticles(ParticleTypes.POOF,
                pos.x, pos.y, pos.z,
                3, 0.2D, 0.1D, 0.2D, 0.02D);
    }

    private static void spawnDashTrailVfx(ServerLevel level, Vec3 pos) {
        BlockPos groundPos = BlockPos.containing(pos).below();
        BlockState blockState = level.getBlockState(groundPos);
        if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, blockState),
                    pos.x, pos.y + 0.1D, pos.z,
                    5, 0.3D, 0.1D, 0.3D, 0.12D);
        }
        level.sendParticles(ParticleTypes.SWEEP_ATTACK,
                pos.x, pos.y + 0.5D, pos.z,
                1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.POOF,
                pos.x, pos.y + 0.1D, pos.z,
                2, 0.2D, 0.1D, 0.2D, 0.05D);
    }

    public static boolean hasActiveDash(LivingEntity entity) {
        return entity != null && ACTIVE_DASHES.containsKey(entity.getUUID());
    }

    public static void cancelDash(LivingEntity entity) {
        if (entity != null) {
            ACTIVE_DASHES.remove(entity.getUUID());
        }
    }
}
