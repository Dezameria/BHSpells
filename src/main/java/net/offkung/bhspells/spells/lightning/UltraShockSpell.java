package net.offkung.bhspells.spells.lightning;

import com.gametechbc.traveloptics.api.init.TravelopticsSchools;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.CameraShakeData;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.target_area.TargetedAreaEntity;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.client.particle.CustomZapParticleOption;
import net.offkung.bhspells.entity.spells.dark_rainfall.DarkRainFallAoe;
import net.offkung.bhspells.util.BHParticleHelper;

import java.util.List;
import java.util.Optional;

public class UltraShockSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "ultrashock");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(getRadius(), 2))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(TravelopticsSchools.AQUA_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(30)
            .build();

    public UltraShockSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 1;
        this.castTime = 1200;
        this.baseManaCost = 70;
    }

    @Override
    public CastType getCastType() {
        return CastType.CONTINUOUS;
    }

    @Override
    public boolean canBeInterrupted(Player player) {
        return false;
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
        return Optional.of(SoundRegistry.SHOCKWAVE_CAST.get());
    }

    @Override
    public void onServerPreCast(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        super.onServerPreCast(level, spellLevel, entity, playerMagicData);

        float radius = getRadius();
        int duration = getEffectiveCastTime(spellLevel, entity);

        TargetedAreaEntity visualEntity = TargetedAreaEntity.createTargetAreaEntity(level, entity.position(), radius, 0xFFD700);
        visualEntity.setDuration(duration);
        visualEntity.setOwner(entity);
        visualEntity.setShouldFade(true);
        level.addFreshEntity(visualEntity);

        DarkRainFallAoe fogAoe = new DarkRainFallAoe(level);
        fogAoe.setPos(entity.getX(), entity.getY(), entity.getZ());
        fogAoe.setOwner(entity);
        fogAoe.setRadius(radius);
        fogAoe.setDuration(duration);
        fogAoe.setRain(true);
        fogAoe.setBolt(true);
        fogAoe.setFogColor(.4f, .4f, .4f);
        fogAoe.setFogScale(1.5f);
        level.addFreshEntity(fogAoe);
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        if (entity.tickCount % 6 == 0) {
            performZap(level, spellLevel, entity);
        }
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        performZap(level, spellLevel, entity);
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private void performZap(Level level, int spellLevel, LivingEntity entity) {
        float radius = getRadius();
        MagicManager.spawnParticles(level, BHParticleHelper.YELLOW_ELECTRIC, entity.getX(), entity.getY() + 1, entity.getZ(), 80, .25, .25, .25, 0.7f + radius * .1f, false);
        CameraShakeManager.addCameraShake(new CameraShakeData(level, 30, entity.position(), radius * 2));

        Vec3 start = entity.getBoundingBox().getCenter();
        level.getEntities(entity, entity.getBoundingBox().inflate(radius, radius, radius), (target) -> !DamageSources.isFriendlyFireBetween(target, entity) && Utils.hasLineOfSight(level, entity, target, true)).forEach(target -> {
            if (target instanceof LivingEntity livingEntity && livingEntity.distanceToSqr(entity) < radius * radius) {
                Vec3 dest = livingEntity.getBoundingBox().getCenter();
                ((ServerLevel) level).sendParticles(new CustomZapParticleOption(dest), start.x, start.y, start.z, 1, 0, 0, 0, 0);
                MagicManager.spawnParticles(level, BHParticleHelper.YELLOW_ELECTRIC, livingEntity.getX(), livingEntity.getY() + livingEntity.getBbHeight() / 2, livingEntity.getZ(), 10, livingEntity.getBbWidth() / 3, livingEntity.getBbHeight() / 3, livingEntity.getBbWidth() / 3, 0.1, false);
            }
        });
        for (int i = 0; i < 3 + radius * 0.5f; i++) {
            Vec3 dest = Utils.getRandomVec3(1).add(0, 0.75, 0).scale(radius).multiply(0.75f, 0.25f, 0.75f).add(start);
            ((ServerLevel) level).sendParticles(new CustomZapParticleOption(dest), start.x, start.y, start.z, 1, 0, 0, 0, 0);
        }
    }

    public float getRadius() {
        return 40;
    }

    public float getDamage(int spellLevel, LivingEntity caster) {
        return 4 + (getSpellPower(spellLevel, caster) * .75f);
    }

    @Override
    public void playSound(Optional<SoundEvent> sound, Entity entity) {
        sound.ifPresent((soundEvent -> entity.playSound(soundEvent, 3.0f, .9f + Utils.random.nextFloat() * .2f)));
    }

    @Override
    public boolean shouldAIStopCasting(int spellLevel, Mob mob, LivingEntity target) {
        return mob.distanceToSqr(target) > (10 * 10) * 1.2;
    }
}
