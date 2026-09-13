package net.offkung.bhspells.event;

import com.gametechbc.traveloptics.entity.misc.TOScreenShakeEntity;
import com.github.L_Ender.cataclysm.init.ModSounds;
import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import mod.chloeprime.aaaparticles.api.client.effekseer.ParticleEmitter;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.entity.spells.firebird.FireSlashProjectile;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import net.offkung.bhspells.util.BHUtil;

import java.util.*;

@Mod.EventBusSubscriber
public class FireBodyManager {
    private static final Map<UUID, FireBodyData> ACTIVE = new HashMap<>();

    private static final int LEVEL1_DURATION = 400;
    private static final int RESET_DURATION = 200;
    private static final int HITS_TO_LEVEL_UP = 5;
    private static final int MAX_MARK_STACKS = 20;
    private static final float MARK_DAMAGE_PER_STACK = 3.0f;
    private static final int FIRE_TICKS_ON_HIT = 140;
    private static final int FIRE_TICKS_ON_MARK_INCREASE = 100;

    private static final ParticleEmitterInfo HALLOWED_ASCENSION = new ParticleEmitterInfo(BHSpells.id("hallowed_ascension"));

    private static final UUID KNOCKBACK_IMMUNITY_ID = UUID.fromString("6f2e6f8e-6a3f-4e2a-9d3b-2f6b7f1a9c3d");

    private FireBodyManager() {
    }

    public static boolean isActive(LivingEntity caster) {
        return ACTIVE.containsKey(caster.getUUID());
    }

    public static int getLevel(LivingEntity caster) {
        FireBodyData data = ACTIVE.get(caster.getUUID());
        return data != null ? data.level : 0;
    }

    public static void start(LivingEntity caster) {
        ACTIVE.put(caster.getUUID(), new FireBodyData());
    }

    public static void registerHit(LivingEntity caster, LivingEntity target) {
        FireBodyData data = ACTIVE.get(caster.getUUID());
        if (data == null) return;

        target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), FIRE_TICKS_ON_HIT));

        int targetHits = data.targetHits.getOrDefault(target.getUUID(), 0) + 1;
        if (targetHits >= HITS_TO_LEVEL_UP) {
            data.targetHits.put(target.getUUID(), 0);
            int currentStack = data.marks.getOrDefault(target.getUUID(), 0);
            if (currentStack < MAX_MARK_STACKS) {
                int newStack = currentStack + 1;
                data.marks.put(target.getUUID(), newStack);
                sendActionbar(caster, "§eMarks: §c" + newStack + "§e/" + MAX_MARK_STACKS);
                if (data.level >= 3) {
                    caster.setRemainingFireTicks(Math.max(caster.getRemainingFireTicks(), FIRE_TICKS_ON_MARK_INCREASE));
                    caster.addEffect(new MobEffectInstance(MobEffectsRegistry.INCINERATION.get(), FIRE_TICKS_ON_MARK_INCREASE, 4, false, false));
                }
                target.addEffect(new MobEffectInstance(MobEffectsRegistry.INCINERATION.get(), FIRE_TICKS_ON_MARK_INCREASE, newStack - 1, false, false));
            }
        } else {
            data.targetHits.put(target.getUUID(), targetHits);
        }

        data.consecutiveHits++;
        if (data.consecutiveHits >= HITS_TO_LEVEL_UP) {
            data.consecutiveHits = 0;
            data.remainingTicks = RESET_DURATION;
            if (data.level < 3) {
                data.level++;
            }
            caster.addEffect(new MobEffectInstance(MobEffectsRegistry.FIRE_BODY_LEVEL2.get(), 20, Math.max(0, data.level - 2), false, false, true));
        }
    }

    private static void sendActionbar(LivingEntity entity, String message) {
        if (entity instanceof Player player) {
            player.displayClientMessage(Component.literal(message), true);
        }
    }

    public static void stop(LivingEntity caster) {
        caster.removeEffect(MobEffectsRegistry.FIRE_BODY_LEVEL2.get());
        removeKnockbackImmunity(caster);
        ACTIVE.remove(caster.getUUID());
    }

    public static void triggerMultiSlash(LivingEntity caster) {
        FireBodyData data = ACTIVE.get(caster.getUUID());
        if (data == null) return;

        FireSlashProjectile slash = new FireSlashProjectile(caster.level(), caster);
        slash.setPos(caster.getEyePosition());
        slash.setYRot(caster.getYRot());
        slash.setXRot(caster.getXRot());
        slash.setMarks(data.marks);
        slash.setMarkBased(true);
        caster.level().addFreshEntity(slash);
    }

    public static int getMarkStacks(LivingEntity caster, Entity target) {
        FireBodyData data = ACTIVE.get(caster.getUUID());
        if (data == null) return 0;
        return data.marks.getOrDefault(target.getUUID(), 0);
    }

    public static float getMarkDamage(LivingEntity caster, Entity target) {
        int stacks = getMarkStacks(caster, target);
        return stacks > 0 ? MARK_DAMAGE_PER_STACK * stacks : MARK_DAMAGE_PER_STACK;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        LivingEntity caster = event.player;
        FireBodyData data = ACTIVE.get(caster.getUUID());
        if (data == null) return;

        Level level = caster.level();

        if (!level.isClientSide) {
            data.remainingTicks--;
            if (data.remainingTicks <= 0) {
                stop(caster);
                return;
            }

            if (data.level >= 2) {
                clearHarmfulEffects(caster);
                applyKnockbackImmunity(caster);
            }
            if (data.level >= 3) {
                applySpeedBuff(caster);
            }

            if (level instanceof ServerLevel serverLevel) {
                tickLevelRingParticles(caster, data, serverLevel);
            }

            Vec3 pos = caster.position();
            MagicManager.spawnParticles(level, ParticleRegistry.DRAGON_FIRE_PARTICLE.get(), pos.x, pos.y + caster.getBbHeight() * 0.5, pos.z, 6, caster.getBbWidth() * 0.4, caster.getBbHeight() * 0.4, caster.getBbWidth() * 0.4, 0.01, false);
        }
    }

    private static void tickLevelRingParticles(LivingEntity caster, FireBodyData data, ServerLevel level) {
        if (data.level < 2) return;
        if (data.level == data.lastRingLevel) return;

        data.lastRingLevel = data.level;

        Vec3 center = caster.position().add(0, caster.getBbHeight() * 0.5, 0);

        if (data.level == 2) {
            spawnHorizontalFlameRing(level, center, 64, 0.6);
            BHUtil.createHexagramParticle(ParticleRegistry.FIRE_PARTICLE.get(), level, center, 20, 5, 0);
            TOScreenShakeEntity.createScreenShake(level, center, 5.0F, 0.015F, 15, 0, 5, true);
            level.playSound(null, center.x, center.y, center.z, ACSoundRegistry.TEPHRA_HIT.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);
        } else if (data.level >= 3) {
            BHUtil.createSphereParticles(level, center, ParticleRegistry.EMBER_PARTICLE.get(), 0.35, 0.35, 0.7, 160);
            float lookX = caster.getXRot() + 90.0F;
            float lookY = caster.getYRot() + 180.0F;
            float rollZ = 0.0F;

            lookX = lookX / 180.0F * 3.14F - 1.57F;
            lookY = lookY / 360.0F * 6.28F;
            rollZ = (rollZ + 180.0F) / 360.0F * 6.28F + 3.14F;

            AAALevel.addParticle(level, 64.0, HALLOWED_ASCENSION.clone().position(center.x, center.y + 0.5, center.z).rotation(-lookX, -lookY, -rollZ));
            TOScreenShakeEntity.createScreenShake(level, center, 5.0F, 0.015F, 15, 0, 5, true);
            level.playSound(null, center.x, center.y, center.z, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.NEUTRAL, 1.0f, 0.5f);
            level.playSound(null, center.x, center.y, center.z, SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 1.0f, 0f);
        }
    }

    private static void spawnHorizontalFlameRing(ServerLevel level, Vec3 center, int n, double speed) {
        for (int i = 0; i < n; ++i) {
            double theta = (Math.PI * 2D) * ((double) i / n);
            double dx = Math.sin(theta) * speed;
            double dz = Math.cos(theta) * speed;

            level.sendParticles(ParticleRegistry.EMBER_PARTICLE.get(), center.x, center.y, center.z, 0, dx, 0.0, dz, 1.0);
        }
    }

    private static void clearHarmfulEffects(LivingEntity caster) {
        List<MobEffect> toRemove = new ArrayList<>();
        for (MobEffectInstance instance : caster.getActiveEffects()) {
            if (instance.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                toRemove.add(instance.getEffect());
            }
        }
        for (MobEffect effect : toRemove) {
            caster.removeEffect(effect);
        }
    }

    private static void applyKnockbackImmunity(LivingEntity caster) {
        AttributeInstance attribute = caster.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (attribute == null || attribute.getModifier(KNOCKBACK_IMMUNITY_ID) != null) return;

        attribute.addTransientModifier(new AttributeModifier(KNOCKBACK_IMMUNITY_ID, "Fire Body knockback immunity", 1.0, AttributeModifier.Operation.ADDITION));
    }

    private static void removeKnockbackImmunity(LivingEntity caster) {
        AttributeInstance attribute = caster.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (attribute == null) return;

        attribute.removeModifier(KNOCKBACK_IMMUNITY_ID);
    }

    private static void applySpeedBuff(LivingEntity caster) {
        MobEffectInstance current = caster.getEffect(MobEffects.MOVEMENT_SPEED);
        if (current == null || current.getDuration() < 5) {
            caster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30, 1, true, false, true));
        }
    }

    private static class FireBodyData {
        int level = 1;
        int remainingTicks = LEVEL1_DURATION;
        int consecutiveHits = 0;
        final Map<UUID, Integer> marks = new HashMap<>();
        final Map<UUID, Integer> targetHits = new HashMap<>();

        int lastRingLevel = 1;
    }
}
