package net.offkung.bhspells.spells.gold;

import com.gametechbc.traveloptics.entity.misc.TOScreenShakeEntity;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.entity.spells.fiery_dance.GoldenMarbleEntity;
import net.offkung.bhspells.event.GoldenMarbleManager;
import net.offkung.bhspells.registry.BHSchoolRegistry;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import net.offkung.bhspells.util.BHUtil;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SkyEaterSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "sky_eater");

    private static final ParticleEmitterInfo GOAT_HEAD = new ParticleEmitterInfo(BHSpells.id("goat_head"));
    private static final double RADIUS = 23.0D;
    private static final int DEBUFF_DURATION = 1200;
    private static final int BUFF_DURATION = 1200;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(BHSchoolRegistry.GOLD_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(30)
            .build();

    public SkyEaterSpell() {
        this.manaCostPerLevel = 6;
        this.baseSpellPower = 20;
        this.spellPowerPerLevel = 20;
        this.baseManaCost = 35;
        this.castTime = 10;
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
        return Optional.of(SoundEvents.ENDER_DRAGON_GROWL);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);

        if (!level.isClientSide) {
            AABB searchBox = entity.getBoundingBox().inflate(RADIUS);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, searchBox, e -> e != entity && e.distanceToSqr(entity) <= RADIUS * RADIUS);

            for (LivingEntity target : targets) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, DEBUFF_DURATION, 3, false, false, true));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, DEBUFF_DURATION, 3, false, false, true));
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, DEBUFF_DURATION, 3, false , false, true));
                target.addEffect(new MobEffectInstance(MobEffects.HUNGER, DEBUFF_DURATION, 1, false , false, true));
            }

            entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, BUFF_DURATION, 1, false, false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, BUFF_DURATION, 1, false, false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, BUFF_DURATION, 1, false, false, true));

            Vector3f red = new Vector3f(1f, 0f, 0f);
            Vector3f black = new Vector3f(0.0f, 0.0f, 0.0f);

            DustColorTransitionOptions dustOptions = new DustColorTransitionOptions(red, black, 6.0f);

            MagicManager.spawnParticles(level, dustOptions, entity.getX(), entity.getY(), entity.getZ(), 350, 0, 0, 0, 1, true);
            MagicManager.spawnParticles(level, new BlastwaveParticleOptions(red, 23f), entity.getX(), entity.getY() + .15f, entity.getZ(), 1, 0, 0, 0, 0, true);

            if (level instanceof ServerLevel serverLevel) {
                DustColorTransitionOptions sparkDust = new DustColorTransitionOptions(red, black, 2.0f);
                Vec3 impactCenter = entity.position().add(0.0, 0.1, 0.0);
                BHUtil.spawnImpactSparks(serverLevel, impactCenter, sparkDust, 250, 0.3, 0.9, 0.55f);
                BHUtil.spawnRedThunderAura(serverLevel, entity, 10);
            }

            float lookX = entity.getXRot() + 90.0F;
            float lookY = entity.getYRot() + 180.0F;
            float rollZ = 0.0F;

            lookX = lookX / 180.0F * 3.14F - 1.57F;
            lookY = lookY / 360.0F * 6.28F;
            rollZ = (rollZ + 180.0F) / 360.0F * 6.28F + 3.14F;

            Vec3 pos = entity.position().add(0, entity.getBbHeight() * 0.5, 0);
            TOScreenShakeEntity.createScreenShake(entity.level(), pos, 23.0f, 0.02f, 10, 0, 2, true);
            AAALevel.addParticle(level, 64.0, GOAT_HEAD.clone().position(entity.getX(), entity.getY(), entity.getZ()).rotation(-lookX, -lookY, -rollZ));
        }
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        super.onServerCastTick(level, spellLevel, entity, playerMagicData);

        if (level instanceof ServerLevel serverLevel) {
            spawnCastingParticles(serverLevel, entity);
        }
    }

    private void spawnCastingParticles(ServerLevel serverLevel, LivingEntity entity) {
        Vector3f fromColor = new Vector3f(1.0f, 0.0f, 0.0f);
        Vector3f toColor = new Vector3f(0.0f, 0.0f, 0.0f);

        DustColorTransitionOptions dustOptions = new DustColorTransitionOptions(fromColor, toColor, 1.5f);
        MagicManager.spawnParticles(serverLevel, dustOptions, entity.getX(), entity.getY(), entity.getZ(), 20, 0, 0, 0, 0.1, true);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.PREPARE_CROSS_ARMS;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.CAST_T_POSE;
    }
}
