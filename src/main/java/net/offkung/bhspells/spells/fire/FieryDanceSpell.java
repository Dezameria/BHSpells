package net.offkung.bhspells.spells.fire;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.entity.spells.fiery_dance.GoldenMarbleEntity;
import net.offkung.bhspells.event.GoldenMarbleManager;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.api.forgeevent.EntityStunEvent;
import yesman.epicfight.world.effect.EpicFightMobEffects;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FieryDanceSpell extends AbstractSpell {
    public static final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "fiery_dance");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(30)
            .build();

    public FieryDanceSpell() {
        this.manaCostPerLevel = 0;
        this.baseSpellPower = 0;
        this.spellPowerPerLevel = 0;
        this.castTime = 16;
        this.baseManaCost = 40;
    }

    public static boolean isCasting(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        MagicData magicData = MagicData.getPlayerMagicData(entity);
        return magicData != null && magicData.isCasting() && spellId.toString().equals(magicData.getCastingSpellId());
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.SHOCKWAVE_PREPARE.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.FIRE_EXTINGUISH);
    }

    @Override
    public boolean canBeInterrupted(@Nullable Player player) {
        return false;
    }

    @Override
    public void onServerPreCast(Level level, int spellLevel, LivingEntity entity, @Nullable MagicData playerMagicData) {
        super.onServerPreCast(level, spellLevel, entity, playerMagicData);
        if (!level.isClientSide) {
            entity.addEffect(new MobEffectInstance(EpicFightMobEffects.STUN_IMMUNITY.get(), getCastTime(spellLevel) + 10, 0, false, false, false));
        }
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity, @Nullable MagicData playerMagicData) {
        super.onServerCastTick(level, spellLevel, entity, playerMagicData);
        if (!level.isClientSide && !entity.hasEffect(EpicFightMobEffects.STUN_IMMUNITY.get())) {
            entity.addEffect(new MobEffectInstance(EpicFightMobEffects.STUN_IMMUNITY.get(), 10, 0, false, false, false));
        }
    }

    @Override
    public void onServerCastComplete(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData, boolean cancelled) {
        if (!level.isClientSide) {
            entity.removeEffect(EpicFightMobEffects.STUN_IMMUNITY.get());
        }
        super.onServerCastComplete(level, spellLevel, entity, playerMagicData, cancelled);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            int count = 6;
            List<GoldenMarbleEntity> marbles = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                float angleOffset = (float) (2 * Math.PI / count * i);
                GoldenMarbleEntity marble = new GoldenMarbleEntity(level, entity, angleOffset);
                marble.setLifetimeTicks(getDurationTicks(spellLevel, entity));
                level.addFreshEntity(marble);
                marbles.add(marble);
            }

            GoldenMarbleManager.registerGroup(entity, marbles);
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.NEUTRAL, 1.0f, 0.7f);
            MagicManager.spawnParticles(level, ParticleRegistry.DRAGON_FIRE_PARTICLE.get(), entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(), 200, 0.1, 0.1, 0.1, 0.2, true);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private int getDurationTicks(int spellLevel, LivingEntity caster) {
        return 1200;
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.PREPARE_CROSS_ARMS;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.CAST_T_POSE;
    }

    @Mod.EventBusSubscriber(modid = BHSpells.MODID)
    public static class FieryDanceEvents {
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onLivingAttack(LivingAttackEvent event) {
            LivingEntity target = event.getEntity();
            if (isCasting(target)) {
                if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                    return;
                }
                event.setCanceled(true);
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onLivingHurt(LivingHurtEvent event) {
            LivingEntity target = event.getEntity();
            if (isCasting(target)) {
                if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                    return;
                }
                event.setCanceled(true);
                event.setAmount(0.0F);
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onLivingDamage(LivingDamageEvent event) {
            LivingEntity target = event.getEntity();
            if (isCasting(target)) {
                if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                    return;
                }
                event.setCanceled(true);
                event.setAmount(0.0F);
            }
        }

        @SubscribeEvent
        public static void onEntityStun(EntityStunEvent event) {
            LivingEntity target = event.getStunnedEntityPatch().getOriginal();
            if (isCasting(target)) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onLivingKnockBack(LivingKnockBackEvent event) {
            LivingEntity target = event.getEntity();
            if (isCasting(target)) {
                event.setCanceled(true);
            }
        }
    }
}
