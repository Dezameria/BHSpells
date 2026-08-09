package net.offkung.bhspells.spells.nature;

import com.gametechbc.traveloptics.api.particle.AdvancedCylinderParticleManager;
import com.gametechbc.traveloptics.api.particle.AdvancedSphereParticleManager;
import com.gametechbc.traveloptics.api.particle.ParticleDirection;
import com.gametechbc.traveloptics.entity.misc.TOScreenShakeEntity;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.entity.spells.eternal_purification.LotusPetal;
import net.offkung.bhspells.entity.spells.eternal_purification.PurificationPillarEntity;
import net.offkung.bhspells.registry.BHSoundRegistry;
import net.offkung.bhspells.spells.BHSpellAnimations;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EternalPurificationSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "eternal_purification");
    private static final double PILLAR_SQUARE_OFFSET = 8.0;
    private static final int PILLAR_LIFETIME_TICKS = 200;

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.duration", Utils.timeFromTicks((float)this.getDuration(spellLevel), 2)), Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(this.getShockwaveRadius(spellLevel), 2)));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(60)
            .build();

    public EternalPurificationSpell() {
        this.manaCostPerLevel = 0;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 0;
        this.castTime = 10;
        this.baseManaCost = 100;
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
        return Optional.of(BHSoundRegistry.RUMBLE_1.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(BHSoundRegistry.BREAK_LARGE.get());
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return BHSpellAnimations.PURIFICATION_PILLAR_CHARGE;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return BHSpellAnimations.PURIFICATION_PILLAR_CAST;
    }

    @Override
    public int getEffectiveCastTime(int spellLevel, @Nullable LivingEntity entity) {
        return this.getCastTime(spellLevel);
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity, @Nullable MagicData playerMagicData) {
        Vec3 spawnPosition = this.calculateSpawnPosition(entity, this.getSpawnOffset());
        AdvancedSphereParticleManager.spawnParticles(level, spawnPosition.x, spawnPosition.y + (double)0.5F, spawnPosition.z, 3, new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BASALT.defaultBlockState()), ParticleDirection.INWARD, 2.5F, false);
        if (playerMagicData != null && (playerMagicData.getCastDurationRemaining() + 1) % 5 == 0) {
            TOScreenShakeEntity.createScreenShake(level, entity.position(), 12.0F, 0.008F, 6, 0, 0, false);
        }

        super.onServerCastTick(level, spellLevel, entity, playerMagicData);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        this.removeExistingPillars(level, entity, 64.0F);
        this.spawnPurificationPillars(level, entity, spellLevel);
        TOScreenShakeEntity.createScreenShake(level, entity.position(), 20.0F, 0.03F, 5, 5, 5, true);

        LotusPetal lotusPetal = new LotusPetal(level, this.getDuration(spellLevel));
        lotusPetal.setPos(entity.position());
        level.addFreshEntity(lotusPetal);

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private void removeExistingPillars(Level level, LivingEntity caster, double radius) {
        Vec3 casterPos = caster.position();
        AABB searchArea = new AABB(casterPos.x - radius, casterPos.y - radius, casterPos.z - radius, casterPos.x + radius, casterPos.y + radius, casterPos.z + radius);

        for(PurificationPillarEntity pillar : level.getEntitiesOfClass(PurificationPillarEntity.class, searchArea, (entity) -> entity != null && entity.isAlive() && entity.getSummoner() != null && entity.getSummoner().getUUID().equals(caster.getUUID()))) {
            double distance = pillar.position().distanceTo(casterPos);
            if (distance <= radius) {
                AdvancedCylinderParticleManager.spawnParticles(level, pillar.position(), 80, new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DEEPSLATE.defaultBlockState()), ParticleDirection.OUTWARD, 1.5F, 3.5F, 0.0F, 0.0F, 0.0F, 0.03, false);
                pillar.playSound(BHSoundRegistry.BREAK_LARGE.get(), 4.0F, 1.0F);
                pillar.discard();
            }
        }
    }

    private void spawnPurificationPillars(Level level, LivingEntity caster, int spellLevel) {
        Vec3 center = caster.position();

        double[][] offsets = {
                {  PILLAR_SQUARE_OFFSET,  PILLAR_SQUARE_OFFSET },
                {  PILLAR_SQUARE_OFFSET, -PILLAR_SQUARE_OFFSET },
                { -PILLAR_SQUARE_OFFSET,  PILLAR_SQUARE_OFFSET },
                { -PILLAR_SQUARE_OFFSET, -PILLAR_SQUARE_OFFSET }
        };

        List<PurificationPillarEntity> group = new ArrayList<>();
        for (double[] offset : offsets) {
            double px = center.x + offset[0];
            double pz = center.z + offset[1];

            AdvancedSphereParticleManager.spawnParticles(level, px, center.y + 1.0F, pz, 40,
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BASALT.defaultBlockState()),
                    ParticleDirection.OUTWARD, 0.8, false);

            PurificationPillarEntity pillar = new PurificationPillarEntity(level, caster);
            pillar.setPos(px, center.y, pz);
            pillar.setDamage(this.getDamage(spellLevel, caster));
            pillar.setMaxAge(PILLAR_LIFETIME_TICKS);
            pillar.setShockwaveRadius(this.getShockwaveRadius(spellLevel));
            pillar.setResonanceSearchRadius(12.0F);
            pillar.setErodeAmplifier(this.getErodeAmplifier(spellLevel));
            pillar.setGroupCenter(center);
            level.addFreshEntity(pillar);
            group.add(pillar);
        }

        if (!group.isEmpty()) {
            group.get(0).setShockwaveEmitter(true);
        }
    }

    private Vec3 calculateSpawnPosition(LivingEntity caster, double offset) {
        Vec3 lookDirection = caster.getLookAngle();
        return caster.position().add(lookDirection.x * offset, 0.0F, lookDirection.z * offset);
    }

    private double getSpawnOffset() {
        return 6.0F;
    }

    private float getShockwaveRadius(int spellLevel) {
        return 12.0F;
    }

    public int getDuration(int spellLevel) {
        return PILLAR_LIFETIME_TICKS;
    }

    private float getDamage(int spellLevel, LivingEntity entity) {
        return this.getSpellPower(spellLevel, entity) * 3.0F;
    }

    private int getErodeAmplifier(int spellLevel) {
        return spellLevel * 2;
    }
}
