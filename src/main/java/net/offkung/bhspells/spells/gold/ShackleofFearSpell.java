package net.offkung.bhspells.spells.gold;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.config.SpellConfig;
import net.offkung.bhspells.entity.spells.gold_chain.ArcaneShackleProjectile;
import net.offkung.bhspells.registry.BHSchoolRegistry;
import net.offkung.bhspells.registry.BHSpellRegistry;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class ShackleofFearSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "shackle_of_fear");

    public static final float CHAIN_HEALTH = 15.0F;
    public static final float CHAIN_HEALTH_PER_LEVEL = 3.0F;
    public static final int BASE_MANA_COST = 40;
    public static final int MANA_COST_PER_LEVEL = 8;
    public static final double COOLDOWN_SECONDS = 0.0;
    public static final int CAST_TIME_TICKS = 10;

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.hp", Utils.stringTruncation(getChainHealth(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.duration", Utils.timeFromTicks(getChainDuration(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(getLashRadius(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.slowness_effect", 6));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(BHSchoolRegistry.GOLD_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(COOLDOWN_SECONDS)
            .build();

    public ShackleofFearSpell() {
        this.baseSpellPower = (int) CHAIN_HEALTH;
        this.spellPowerPerLevel = (int) CHAIN_HEALTH_PER_LEVEL;
        this.baseManaCost = BASE_MANA_COST;
        this.manaCostPerLevel = MANA_COST_PER_LEVEL;
        this.castTime = CAST_TIME_TICKS;
    }

    @Override
    public int getManaCost(int spellLevel) {
        return SpellConfig.ShackleofFear.getBaseMana() + (spellLevel - 1) * SpellConfig.ShackleofFear.getManaPerLevel();
    }

    @Override
    public int getSpellCooldown() {
        return (int) (SpellConfig.ShackleofFear.getCooldown() * 20);
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.CHAIN_STEP);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        ArcaneShackleProjectile projectile = new ArcaneShackleProjectile(level, entity);
        projectile.setPos(entity.position().add(0, entity.getEyeHeight() - projectile.getBoundingBox().getYsize() * 0.5f, 0).add(entity.getForward()));
        projectile.shoot(entity.getLookAngle());
        projectile.setChainHealth(getChainHealth(spellLevel, entity));
        projectile.setChainLifetime(getChainDuration(spellLevel, entity));
        projectile.setLashRadius(getLashRadius(spellLevel, entity));
        projectile.setRestraintStrength(0.015f);
        level.addFreshEntity(projectile);
        level.playSound(null, entity.blockPosition(), net.minecraft.sounds.SoundEvents.TRIDENT_THROW, entity.getSoundSource(), 1f, 0.7f + entity.getRandom().nextFloat() * .1f);
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private float getChainHealth(int spellLevel, LivingEntity entity) {
        float base = SpellConfig.ShackleofFear.getChainHealth();
        float perLevel = SpellConfig.ShackleofFear.getChainHealthPerLevel();
        return (base + (spellLevel - 1) * perLevel) * getEntityPowerMultiplier(entity);
    }

    private int getChainDuration(int spellLevel, LivingEntity entity) {
        return 20 * 20;
    }

    private float getLashRadius(int spellLevel, LivingEntity entity) {
        return 5f;
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ONE_HANDED_HORIZONTAL_SWING_ANIMATION;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return AnimationHolder.pass();
    }
}
