package net.offkung.bhspells.spells.gold;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
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
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.client.particle.GildedHareVfx;
import net.offkung.bhspells.config.SpellConfig;
import net.offkung.bhspells.registry.BHSchoolRegistry;
import net.offkung.bhspells.registry.MobEffectsRegistry;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class GildedHareSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "gilded_hare");

    public static final int BASE_MANA_COST = 80;
    public static final int MANA_COST_PER_LEVEL = 0;
    public static final double COOLDOWN_SECONDS = 60.0D;

    public static final int BUFF_DURATION_TICKS = 2400;
    public static final int COMBO_WINDOW_TICKS = 100;
    public static final int FINISHER_COOLDOWN_TICKS = 200;
    public static final int SLOWNESS_DURATION_TICKS = 20;
    public static final int STUN_DURATION_TICKS = 20;
    public static final int BLINDNESS_DURATION_TICKS = 60;

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.duration", Utils.timeFromTicks(BUFF_DURATION_TICKS, 1)),
                Component.translatable("ui.bhspells.gilded_hare.buffs"),
                Component.translatable("ui.bhspells.gilded_hare.finisher")
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(BHSchoolRegistry.GOLD_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(COOLDOWN_SECONDS)
            .build();

    public GildedHareSpell() {
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.baseManaCost = BASE_MANA_COST;
        this.manaCostPerLevel = MANA_COST_PER_LEVEL;
        this.castTime = 0; // Instant cast
    }

    @Override
    public int getManaCost(int spellLevel) {
        return SpellConfig.GildedHare.getBaseMana() + (spellLevel - 1) * SpellConfig.GildedHare.getManaPerLevel();
    }

    @Override
    public int getSpellCooldown() {
        return (int) (SpellConfig.GildedHare.getCooldown() * 20);
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.ENCHANTMENT_TABLE_USE);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.SELF_CAST_ANIMATION;
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData magicData) {
        if (entity.hasEffect(MobEffectsRegistry.GILDED_HARE.get())) {
            return false;
        }
        return super.checkPreCastConditions(level, spellLevel, entity, magicData);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, BUFF_DURATION_TICKS, 0, false, false, true));
        entity.addEffect(new MobEffectInstance(MobEffects.JUMP, BUFF_DURATION_TICKS, 0, false, false, true));
        entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, BUFF_DURATION_TICKS, 0, false, false, true));
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, BUFF_DURATION_TICKS, 0, false, false, true));

        entity.addEffect(new MobEffectInstance(MobEffectsRegistry.GILDED_HARE.get(), BUFF_DURATION_TICKS, 0, false, false, true));

        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2F, 1.4F);
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.2F);

        if (level instanceof ServerLevel serverLevel) {
            double cx = entity.getX();
            double cy = entity.getY() + 0.1D;
            double cz = entity.getZ();

            for (int i = 0; i < 24; i++) {
                double angle = (2.0D * Math.PI / 24) * i;
                double vx = Math.cos(angle) * 0.18D;
                double vz = Math.sin(angle) * 0.18D;
                serverLevel.sendParticles(new DustParticleOptions(GildedHareVfx.COLOR_BRIGHT_GOLD, 1.1F), cx, cy + 0.3D, cz, 1, vx, 0.08D, vz, 0.02D);
            }
            serverLevel.sendParticles(ParticleTypes.WAX_OFF, cx, cy + 0.8D, cz, 15, 0.3D, 0.5D, 0.3D, 0.05D);
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
