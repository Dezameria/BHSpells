package net.offkung.bhspells.spells.gold;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.EarthquakeAoe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.entity.spells.resounding_radiant.RadiantCrystalEntity;
import net.offkung.bhspells.entity.spells.resounding_radiant.RadiantFieldAoe;
import net.offkung.bhspells.registry.BHSchoolRegistry;
import net.offkung.bhspells.registry.BHSoundRegistry;
import net.offkung.bhspells.registry.ParticleRegistry;
import net.offkung.bhspells.spells.BHSpellAnimations;

import java.util.List;
import java.util.Optional;

public class ShiningRadiantSpell extends AbstractSpell {
    private final ResourceLocation spellID = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "shining_radiant");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", getDamageText(spellLevel, caster)));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(BHSchoolRegistry.GOLD_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(20)
            .build();

    public ShiningRadiantSpell() {
        this.manaCostPerLevel = 20;
        this.baseSpellPower = 30;
        this.spellPowerPerLevel = 2;
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
    public AnimationHolder getCastStartAnimation() {
        return BHSpellAnimations.PURIFICATION_PILLAR_CHARGE;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return BHSpellAnimations.PURIFICATION_PILLAR_CAST;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellID;
    }

    @Override
    public void onClientCast(Level level, int spellLevel, LivingEntity entity, ICastData castData) {
        super.onClientCast(level, spellLevel, entity, castData);
        entity.setYBodyRot(entity.getYRot());
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(BHSoundRegistry.RUMBLE_1.get());
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        float radius = 0.5f;
        Vec3 forward = entity.getForward().multiply(1, 0, 1).normalize();
        Vec3 start = entity.getEyePosition().subtract(0, 0.5, 0).add(forward.scale(1.5));
        float count = 4;

        //Slash hit and damage
        for (int i = 0; i < count; i++) {
            Vec3 hitLocation = start.add(forward.scale(i));
            var entities = level.getEntities(entity, AABB.ofSize(hitLocation, radius, radius * 5, radius));
            var damageSource = this.getDamageSource(entity);
            for (Entity targetEntity : entities) {
                if (targetEntity.isAlive() && targetEntity.isPickable() && Utils.hasLineOfSight(level, hitLocation.add(0, 1, 0), targetEntity.getBoundingBox().getCenter(), true)) {
                    if (DamageSources.applyDamage(targetEntity, getDamage(spellLevel, entity), damageSource)) {
                        targetEntity.invulnerableTime = 0;
                        MagicManager.spawnParticles(level, ParticleRegistry.RADIANT_SHATTER.get(), targetEntity.getX(), targetEntity.getY() + targetEntity.getBbHeight() * .5f, targetEntity.getZ(), 5, targetEntity.getBbWidth() * .5f, targetEntity.getBbHeight() * .5f, targetEntity.getBbWidth() * .5f, .03, false);
                        EnchantmentHelper.doPostDamageEffects(entity, targetEntity);
                    }
                }
            }
        }

        //Spawning crystals in a forward-facing half-circle around the caster, sweeping from the caster's left to right
        float damage =  getDamage(spellLevel, entity);
        float minScale = 1.0f;
        float maxScale = 3.0f;
        int crystalCount = 15;
        float arcRadius = 3.0f;
        Vec3 center = entity.position();
        int spawnIndex = 0;

        //crystals stand for ~20 seconds, matching the lingering radiant field below
        int crystalRestTime = RadiantFieldAoe.LIFETIME_TICKS - RadiantCrystalEntity.RISE_TIME - RadiantCrystalEntity.LOWER_TIME;

        for (int i = 0; i < crystalCount; i++) {
            float t = crystalCount > 1 ? i / (float) (crystalCount - 1) : 0.5f;
            //+90deg points to the caster's left, -90deg to the caster's right, so we sweep left -> right
            float angle = Mth.lerp(t, Mth.PI * 0.5f, -Mth.PI * 0.5f);
            Vec3 dir = forward.yRot(angle).normalize();
            Vec3 anchor = center.add(dir.scale(arcRadius));

            //biggest crystals at the far left/right edges of the arc, smallest toward the middle
            float edge = Math.abs(t - 0.5f) * 2f;
            float scale = Mth.lerp(edge, minScale, maxScale);
            boolean isBiggestCrystal = i == 0 || i == crystalCount - 1;
            if (isBiggestCrystal) {
                scale = maxScale * 1.2f;
            }

            //the main arc crystal plus a small cluster of lesser crystals scattered around it
            int cluster = 1 + Utils.random.nextIntBetweenInclusive(2, 3);
            for (int j = 0; j < cluster; j++) {
                boolean isMain = j == 0;
                Vec3 spawn = anchor;
                float crystalScale = scale;
                int delay = i;
                if (!isMain) {
                    double spread = 0.6 + Utils.random.nextDouble() * 1.1;
                    float offAngle = Utils.random.nextFloat() * Mth.TWO_PI;
                    spawn = anchor.add(Mth.cos(offAngle) * spread, 0, Mth.sin(offAngle) * spread);
                    crystalScale = Mth.lerp(Utils.random.nextFloat(), minScale * 0.5f, Math.max(minScale, scale * 0.7f));
                    delay = i + 1 + Utils.random.nextIntBetweenInclusive(0, 3);
                }
                spawn = Utils.moveToRelativeGroundLevel(level, spawn.add(0, 1, 0), 8).add(0, 0.1, 0);

                BlockPos below = BlockPos.containing(spawn).below();
                if (level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
                    RadiantCrystalEntity crystal = new RadiantCrystalEntity(level, entity);
                    if (spawnIndex % 3 == 0) {
                        crystal.setSilent(true);
                    }
                    crystal.setCrystalSize(crystalScale);
                    crystal.moveTo(spawn);
                    crystal.setWaitTime(delay);
                    crystal.setRestTime(crystalRestTime);
                    //edge crystals deal 75% dmg, main arc crystals 30%, scattered cluster crystals 15%
                    float dmgMul = isBiggestCrystal ? 0.75f : (isMain ? 0.3f : 0.15f);
                    crystal.setDamage(damage * dmgMul);
                    //face the crystal outward from the caster, with a little jitter
                    crystal.setYRot((float) (-Mth.atan2(dir.x, dir.z) * (180f / Math.PI)) + Utils.random.nextIntBetweenInclusive(-25, 25));
                    crystal.setXRot(Utils.random.nextIntBetweenInclusive(-15, 15));
                    level.addFreshEntity(crystal);
                    spawnIndex++;
                }
            }
        }

        //Lingering radiant field at the cast location (does not follow the caster): green blastwave pulse + crippling aura
        Vec3 fieldCenter = Utils.moveToRelativeGroundLevel(level, entity.position().add(0, 1, 0), 3).add(0, 0.1, 0);

        RadiantFieldAoe field = new RadiantFieldAoe(level, entity);
        field.moveTo(fieldCenter);
        field.setDuration(RadiantFieldAoe.LIFETIME_TICKS);
        level.addFreshEntity(field);

        //A quake churns the same ground for the field's lifetime
        EarthquakeAoe earthquake = new EarthquakeAoe(level);
        earthquake.setOwner(entity);
        earthquake.moveTo(fieldCenter);
        earthquake.setCircular();
        earthquake.setRadius(RadiantFieldAoe.FIELD_RADIUS);
        earthquake.setDuration(RadiantFieldAoe.LIFETIME_TICKS);
        earthquake.setDamage(damage * 0.15f);
        earthquake.setSlownessAmplifier(1);
        level.addFreshEntity(earthquake);

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private float getDamage(int spellLevel, LivingEntity entity) {
        return getSpellPower(spellLevel, entity) + Utils.getWeaponDamage(entity);
    }

    private String getDamageText(int spellLevel, LivingEntity entity) {
        if (entity != null) {
            float weaponDamage = Utils.getWeaponDamage(entity);
            String plus = "";
            if (weaponDamage > 0) {
                plus = String.format(" (+%s)", Utils.stringTruncation(weaponDamage, 1));
            }
            String damage = Utils.stringTruncation(getDamage(spellLevel, entity), 1);
            return damage + plus;
        }
        return "" + getSpellPower(spellLevel, entity);
    }
}
