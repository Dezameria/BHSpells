package net.offkung.bhspells.event;

import com.hm.efn.registries.EFNMobEffectRegistry;
import io.redspace.ironsspellbooks.damage.DamageSources;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.offkung.bhspells.entity.spells.intrusion_chain.IntrusionChainEntity;
import net.offkung.bhspells.registry.DamageSourcesRegistry;
import net.offkung.bhspells.registry.MobEffectsRegistry;

import javax.annotation.Nullable;
import java.util.*;

@Mod.EventBusSubscriber
public class IntrusionChainManager {
    private static final Map<UUID, ChainState> ACTIVE_STATES = new HashMap<>();

    private static final double SELECT_RANGE = 10.0;
    private static final double MAX_LINK_DISTANCE = 30.0;
    private static final int EFFECT_REFRESH_TICKS = 30;
    private static final int MAX_ALLIES = 2;

    private IntrusionChainManager() {
    }

    public static boolean isActive(LivingEntity caster) {
        return ACTIVE_STATES.containsKey(caster.getUUID());
    }

    public static void onCast(LivingEntity caster) {
        onCast(caster, IntrusionChainEntity.Type.BUFF);
    }

    public static void onCast(LivingEntity caster, IntrusionChainEntity.Type chainType) {
        if (!(caster.level() instanceof ServerLevel casterLevel)) return;

        ChainState state = ACTIVE_STATES.get(caster.getUUID());

        // Re-casting while aiming at an already-linked target forcibly severs that chain immediately.
        if (state != null && tryForceBreakLinkedTarget(caster, state, casterLevel)) {
            return;
        }

        if (state == null) {
            state = new ChainState(chainType, casterLevel);
            ACTIVE_STATES.put(caster.getUUID(), state);
            sendActionbar(caster, chainType == IntrusionChainEntity.Type.BUFF ? "เลือกพันธมิตร §a2§r คนเพื่อผูกมัด" : "เลือกศัตรู §c2§r คนเพื่อผูกมัด");
        }

        if (state.chains.size() >= MAX_ALLIES) {
            sendActionbar(caster, chainType == IntrusionChainEntity.Type.BUFF ? "§cมีพันธมิตร 2 คนผูดมัดกับคุณอยู่แล้ว" : "§cมีศัตรู 2 คนผูดมัดกับคุณอยู่แล้ว");
            return;
        }

        trySelectTarget(caster, state);
    }

    private static void trySelectTarget(LivingEntity caster, ChainState state) {
        Level level = caster.level();
        LivingEntity target = getRaycastAllyTarget(level, caster, SELECT_RANGE);

        if (target == null) {
            sendActionbar(caster, remainingMessage(state));
            return;
        }

        if (target == caster || state.chains.containsKey(target.getUUID())) {
            sendActionbar(caster, state.type == IntrusionChainEntity.Type.BUFF ? "§aพันธมิตรคนนั้นผูกมัดแล้ว" : "§cศัตรูคนนั้นผูกมัดแล้ว");
            return;
        }

        IntrusionChainEntity chain = new IntrusionChainEntity(level, caster, target, state.type);
        level.addFreshEntity(chain);
        state.chains.put(target.getUUID(), chain);

        applyChainManaCheckEffect(caster);

        if (state.type == IntrusionChainEntity.Type.BUFF) {
            applyAllyBuff(target);
            applyCasterActiveEffect(caster);
        } else {
            applyEnemyPoison(target);
            applyEnemySlow(target);
        }

        if (state.chains.size() >= MAX_ALLIES) {
            sendActionbar(caster, state.type == IntrusionChainEntity.Type.BUFF ? "§aผูกมัดพันธมิตรทั้งสองสำเร็จ!" : "§cผูกมัดศัตรูทั้งสองสำเร็จ!");
            if (state.type == IntrusionChainEntity.Type.BUFF) {
                applyCasterBuff(caster);
            }
        } else {
            sendActionbar(caster, remainingMessage(state));
        }
    }

    private static boolean tryForceBreakLinkedTarget(LivingEntity caster, ChainState state, ServerLevel level) {
        LivingEntity target = getRaycastAllyTarget(level, caster, SELECT_RANGE);
        if (target == null || !state.chains.containsKey(target.getUUID())) {
            return false;
        }

        boolean wasFullyConnected = state.chains.size() >= MAX_ALLIES;

        IntrusionChainEntity chain = state.chains.remove(target.getUUID());
        if (chain != null && !chain.isRemoved()) {
            chain.forceBreakPublic();
        }

        if (state.type == IntrusionChainEntity.Type.BUFF) {
            removeAllyBuff(target);
        } else {
            removeEnemyDebuff(target);
        }

        if (state.chains.isEmpty()) {
            removeCasterBuff(caster);
            removeCasterActiveEffect(caster);
            ACTIVE_STATES.remove(caster.getUUID());
            sendActionbar(caster, state.type == IntrusionChainEntity.Type.BUFF ? "§cเส้นใยพันธมิตรถูกตัดขาดทั้งหมด" : "§cเส้นใยศัตรูถูกตัดขาดทั้งหมด");
        } else {
            if (wasFullyConnected) {
                removeCasterBuff(caster);
            }
            sendActionbar(caster, remainingMessage(state));
        }

        return true;
    }

    private static String remainingMessage(ChainState state) {
        int remaining = MAX_ALLIES - state.chains.size();
        if (state.type == IntrusionChainEntity.Type.BUFF) {
            return remaining == 1 ? "เลือกพันธมิตร §a1§r คนเพื่อผูกมัด" : "เลือกพันธมิตร§a " + remaining + "§r คนเพื่อผูกมัด";
        } else {
            return remaining == 1 ? "เลือกศัตรู §c1§r คนเพื่อผูกมัด" : "เลือกศัตรู§c " + remaining + "§r คนเพื่อผูกมัด";
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide) return;
        if (!(event.level instanceof ServerLevel serverLevel)) return;

        Iterator<Map.Entry<UUID, ChainState>> it = ACTIVE_STATES.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, ChainState> entry = it.next();
            ChainState state = entry.getValue();

            ServerLevel trackedLevel = state.cachedCaster != null && state.cachedCaster.level() instanceof ServerLevel liveLevel ? liveLevel : state.casterLevel;
            if (trackedLevel != serverLevel) continue;

            LivingEntity caster = resolveLiving(serverLevel, entry.getKey(), state.cachedCaster);
            state.cachedCaster = caster;

            if (caster == null || !caster.isAlive()) {
                for (IntrusionChainEntity chain : state.chains.values()) {
                    if (!chain.isRemoved()) {
                        chain.forceBreakPublic();
                    }
                }
                it.remove();
                continue;
            }

            if (caster.hasEffect(MobEffectsRegistry.CHAIN_CANCEL.get())) {
                forceCancel(caster, state, serverLevel);
                it.remove();
                continue;
            }

            // Debuff chains only release once the caster is at 1 HP or below.
            if (state.type == IntrusionChainEntity.Type.DEBUFF && caster.getHealth() <= 1.0F) {
                for (Map.Entry<UUID, IntrusionChainEntity> chainEntry : state.chains.entrySet()) {
                    if (!chainEntry.getValue().isRemoved()) {
                        chainEntry.getValue().forceBreakPublic();
                    }
                    LivingEntity enemy = resolveLivingById(serverLevel, chainEntry.getKey());
                    if (enemy != null) {
                        removeEnemyDebuff(enemy);
                    }
                }
                removeCasterActiveEffect(caster);
                sendActionbar(caster, "§cเส้นใยถูกคลายออกเนื่องจากคุณล้ม");
                it.remove();
                continue;
            }

            boolean wasFullyConnected = state.chains.size() >= MAX_ALLIES;

            Iterator<Map.Entry<UUID, IntrusionChainEntity>> chainIt = state.chains.entrySet().iterator();
            while (chainIt.hasNext()) {
                Map.Entry<UUID, IntrusionChainEntity> chainEntry = chainIt.next();
                IntrusionChainEntity chain = chainEntry.getValue();
                LivingEntity ally = resolveLivingById(serverLevel, chainEntry.getKey());

                boolean tooFar = ally != null && ally.distanceToSqr(caster) > MAX_LINK_DISTANCE * MAX_LINK_DISTANCE;

                if (chain.isRemoved() || tooFar) {
                    if (!chain.isRemoved() && tooFar) {
                        chain.forceBreakPublic();
                        applyChainBreak(caster);
                    }
                    if (ally != null) {
                        if (state.type == IntrusionChainEntity.Type.BUFF) {
                            removeAllyBuff(ally);
                        } else {
                            removeEnemyDebuff(ally);
                        }
                    }
                    chainIt.remove();
                }
            }

            boolean stillFullyConnected = state.chains.size() >= MAX_ALLIES;

            if (wasFullyConnected && !stillFullyConnected) {
                removeCasterBuff(caster);
                sendActionbar(caster, remainingMessage(state));
            }

            if (state.chains.isEmpty()) {
                removeCasterBuff(caster);
                removeCasterActiveEffect(caster);
                it.remove();
                continue;
            }

            if (caster.tickCount % EFFECT_REFRESH_TICKS == 0) {
                for (UUID targetUUID : state.chains.keySet()) {
                    LivingEntity target = resolveLivingById(serverLevel, targetUUID);
                    if (target == null) continue;

                    if (state.type == IntrusionChainEntity.Type.BUFF) {
                        applyAllyBuff(target);
                        applyCasterActiveEffect(caster);
                    } else {
                        applyEnemySlow(target);
                    }
                }
                if (state.type == IntrusionChainEntity.Type.BUFF && stillFullyConnected) {
                    applyCasterBuff(caster);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onCasterHurt(LivingHurtEvent event) {
        LivingEntity caster = event.getEntity();
        ChainState state = ACTIVE_STATES.get(caster.getUUID());
        if (state == null || state.type != IntrusionChainEntity.Type.DEBUFF) return;
        if (state.applyingSharedDamage) return; // reentrancy guard
        if (!(caster.level() instanceof ServerLevel serverLevel)) return;

        float sharedAmount = event.getAmount();
        if (sharedAmount <= 0) return;

        DamageSource sourceOfCasterDamage = event.getSource();

        state.applyingSharedDamage = true;
        try {
            for (UUID targetUUID : state.chains.keySet()) {
                LivingEntity enemy = resolveLivingById(serverLevel, targetUUID);
                if (enemy != null && enemy.isAlive()) {
                    DamageSources.applyDamage(enemy, sharedAmount, sourceOfCasterDamage);
                }
            }
        } finally {
            state.applyingSharedDamage = false;
        }
    }

    private static void forceCancel(LivingEntity caster, ChainState state, ServerLevel level) {
        for (Map.Entry<UUID, IntrusionChainEntity> entry : state.chains.entrySet()) {
            IntrusionChainEntity chain = entry.getValue();
            if (!chain.isRemoved()) {
                chain.forceBreakPublic();
            }
            LivingEntity ally = resolveLivingById(level, entry.getKey());
            if (ally != null) {
                if (state.type == IntrusionChainEntity.Type.BUFF) {
                    removeAllyBuff(ally);
                } else {
                    removeEnemyDebuff(ally);
                }
            }
        }
        applyChainBreak(caster);
        removeCasterBuff(caster);
        removeCasterActiveEffect(caster);
        sendActionbar(caster, "§cเส้นด้ายถูกตัดขาดแล้ว");
    }

    private static void applyChainManaCheckEffect(LivingEntity caster) {
        caster.addEffect(new MobEffectInstance(MobEffectsRegistry.CHAIN_MANA_CHECK.get(), 20, 0, false, false, true));
    }

    private static void applyAllyBuff(LivingEntity ally) {
        ally.addEffect(new MobEffectInstance(EFNMobEffectRegistry.ATTACK_SPEED_INCREASE.get(), EFFECT_REFRESH_TICKS + 10, 0, false, false, true));
        ally.addEffect(new MobEffectInstance(MobEffectsRegistry.CHAIN_BUFF_ALLY.get(), EFFECT_REFRESH_TICKS + 10, 0, false, false, true));
        ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, EFFECT_REFRESH_TICKS + 10, 0, false, false, true));
        ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, EFFECT_REFRESH_TICKS + 10, 2, false, false, true));
    }

    private static void removeAllyBuff(LivingEntity ally) {
        ally.removeEffect(EFNMobEffectRegistry.ATTACK_SPEED_INCREASE.get());
        ally.removeEffect(MobEffectsRegistry.CHAIN_BUFF_ALLY.get());
        ally.removeEffect(MobEffects.DAMAGE_RESISTANCE);
    }

    private static void applyEnemyPoison(LivingEntity enemy) {
        enemy.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 4, false, true, true));
    }

    private static void applyEnemySlow(LivingEntity enemy) {
        enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, EFFECT_REFRESH_TICKS + 10, 1, false, false, true));
        enemy.addEffect(new MobEffectInstance(MobEffectsRegistry.CHAIN_DEBUFF_TARGET.get(), EFFECT_REFRESH_TICKS + 10, 0, false, false, true));
    }

    private static void removeEnemyDebuff(LivingEntity enemy) {
        enemy.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        enemy.removeEffect(MobEffectsRegistry.CHAIN_DEBUFF_TARGET.get());
    }

    private static void applyCasterBuff(LivingEntity caster) {
        caster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, EFFECT_REFRESH_TICKS + 10, 1, false, false, true));
        caster.addEffect(new MobEffectInstance(MobEffects.REGENERATION, EFFECT_REFRESH_TICKS + 10, 2, false, false, true));
    }

    private static void removeCasterBuff(LivingEntity caster) {
        caster.removeEffect(MobEffects.MOVEMENT_SPEED);
    }

    private static void applyCasterActiveEffect(LivingEntity caster) {
        caster.addEffect(new MobEffectInstance(MobEffectsRegistry.INTRUSION_CHAIN.get(), EFFECT_REFRESH_TICKS + 10, 0, false, false, true));
    }

    private static void removeCasterActiveEffect(LivingEntity caster) {
        caster.removeEffect(MobEffectsRegistry.INTRUSION_CHAIN.get());
    }

    private static void applyChainBreak(LivingEntity caster) {
        caster.addEffect(new MobEffectInstance(MobEffectsRegistry.CHAIN_BREAK.get(), 20, 0, false, false, true));
    }

    @Nullable
    private static LivingEntity resolveLiving(ServerLevel level, UUID uuid, @Nullable LivingEntity cached) {
        if (cached != null && cached.isAlive() && !cached.isRemoved()) {
            return cached;
        }
        return resolveLivingById(level, uuid);
    }

    @Nullable
    private static LivingEntity resolveLivingById(ServerLevel level, UUID uuid) {
        Entity entity = level.getEntity(uuid);
        return entity instanceof LivingEntity living && living.isAlive() ? living : null;
    }

    private static void sendActionbar(LivingEntity entity, String message) {
        if (entity instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(Component.translatable(message), true);
        }
    }

    @Nullable
    private static LivingEntity getRaycastAllyTarget(Level level, LivingEntity caster, double range) {
        Vec3 eyePos = caster.getEyePosition(1.0F);
        Vec3 lookVec = caster.getViewVector(1.0F);
        Vec3 endPos = eyePos.add(lookVec.scale(range));

        HitResult blockHit = level.clip(new ClipContext(eyePos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        double actualRange = blockHit.getType() != HitResult.Type.MISS ? eyePos.distanceTo(blockHit.getLocation()) : range;
        Vec3 limitedEndPos = eyePos.add(lookVec.scale(actualRange));

        AABB searchBox = new AABB(eyePos, limitedEndPos).inflate(0.5F);
        List<Entity> potentialTargets = level.getEntities(caster, searchBox, entity -> entity instanceof LivingEntity living && !living.isSpectator() && living.isAlive() && living != caster);

        LivingEntity closestEntity = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : potentialTargets) {
            Vec3 toEntity = entity.getBoundingBox().getCenter().subtract(eyePos);
            double distance = toEntity.length();
            if (distance > actualRange) continue;

            Vec3 toEntityNorm = toEntity.normalize();
            double alignment = lookVec.dot(toEntityNorm);
            if (alignment < 0.995) continue;

            HitResult entityCheck = level.clip(new ClipContext(eyePos, entity.getBoundingBox().getCenter(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
            if ((entityCheck.getType() == HitResult.Type.MISS || eyePos.distanceTo(entityCheck.getLocation()) >= distance - 0.1) && distance < closestDistance) {
                closestDistance = distance;
                closestEntity = (LivingEntity) entity;
            }
        }

        return closestEntity;
    }

    private static class ChainState {
        final Map<UUID, IntrusionChainEntity> chains = new HashMap<>();
        final IntrusionChainEntity.Type type;
        LivingEntity cachedCaster;
        ServerLevel casterLevel;
        boolean applyingSharedDamage = false;

        ChainState(IntrusionChainEntity.Type type, ServerLevel casterLevel) {
            this.type = type;
            this.casterLevel = casterLevel;
        }
    }
}
