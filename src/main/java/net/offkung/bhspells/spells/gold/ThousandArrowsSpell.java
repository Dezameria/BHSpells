package net.offkung.bhspells.spells.gold;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.RaycastBuilder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.entity.spells.thousand_arrows.SkyArrowProjectile;
import net.offkung.bhspells.registry.BHSchoolRegistry;

import java.util.List;
import java.util.Optional;

public class ThousandArrowsSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "thousand_arrows");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.projectile_count", getCount(spellLevel, caster)));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(BHSchoolRegistry.GOLD_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(15)
            .build();

    public ThousandArrowsSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 0;
        this.castTime = 10;
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
        return Optional.of(SoundRegistry.ARROW_VOLLEY_PREPARE.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.EVOKER_CAST_SPELL);
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        Utils.preCastTargetHelper(level, entity, playerMagicData, this, 48, .25f, false);
        return true;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        Vec3 targetLocation = null;
        if (playerMagicData.getAdditionalCastData() instanceof TargetEntityCastData castTargetingData) {
            targetLocation = castTargetingData.getTargetPosition((ServerLevel) level);
        }
        if (targetLocation == null) {
            targetLocation = RaycastBuilder.begin(level, entity)
                    .range(100)
                    .checkForBlocks(true)
                    .build()
                    .getLocation();
        }
        Vec3 casterPos = entity.position();
        Vec3 horizontalOffset = new Vec3(targetLocation.x - casterPos.x, 0, targetLocation.z - casterPos.z);
        double horizontalDistance = Math.min(horizontalOffset.length(), 20.0);
        Vec3 horizontalDir = horizontalOffset.lengthSqr() > 1.0E-4 ? horizontalOffset.normalize() : entity.getLookAngle();

        Vec3 skyDestination = new Vec3(
                casterPos.x + horizontalDir.x * horizontalDistance,
                targetLocation.y + 10.0,
                casterPos.z + horizontalDir.z * horizontalDistance
        );

        Vec3 spawnPos = entity.getEyePosition().add(entity.getLookAngle().scale(0.5));
        Vec3 launchDirection = skyDestination.subtract(spawnPos).normalize();

        SkyArrowProjectile skyArrow = new SkyArrowProjectile(level, entity);
        skyArrow.setPos(spawnPos);
        skyArrow.setDestination(skyDestination);
        skyArrow.setPerArrowDamage(getDamage(spellLevel, entity));
        skyArrow.shoot(launchDirection.scale(skyArrow.getSpeed()));
        level.addFreshEntity(skyArrow);

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private int getCount(int spellLevel, LivingEntity entity) {
        return getRows(spellLevel, entity) * getArrowsPerRow(spellLevel, entity);
    }

    private int getRows(int spellLevel, LivingEntity entity) {
        return 4 + spellLevel;
    }

    private int getArrowsPerRow(int spellLevel, LivingEntity entity) {
        return 5 + spellLevel / 2;
    }

    private float getDamage(int spellLevel, LivingEntity entity) {
        return this.getSpellPower(spellLevel, entity) * .25f;
    }

    @Override
    public SpellDamageSource getDamageSource(Entity projectile, Entity attacker) {
        return super.getDamageSource(projectile, attacker);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.BOW_CHARGE_ANIMATION;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return AnimationHolder.none();
    }
}
