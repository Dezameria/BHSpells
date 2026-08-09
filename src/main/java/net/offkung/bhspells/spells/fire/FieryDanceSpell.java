package net.offkung.bhspells.spells.fire;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.entity.spells.fiery_dance.GoldenMarbleEntity;
import net.offkung.bhspells.event.GoldenMarbleManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FieryDanceSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "fiery_dance");

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
}
