package net.offkung.bhspells.spells.ground;

import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.client.particle.ShockingBeamParticleOption;
import net.offkung.bhspells.config.SpellConfig;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.CameraShakeData;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class ShockingSpell extends AbstractSpell {
    // ==========================================
    // 1. SPELL ID
    // ==========================================
    public static final String SPELL_ID_STR = "shocking";
    private final ResourceLocation spellId = new ResourceLocation(BHSpells.MODID, SPELL_ID_STR);
    private static final ResourceLocation GROUND_SCHOOL_RESOURCE = ResourceLocation.fromNamespaceAndPath("bhspells", "ground");

    // ==========================================
    // 2. TUNING CONSTANTS (Code Defaults)
    // ==========================================
    public static final float BASE_DAMAGE = 8.0F;
    public static final float DAMAGE_PER_LEVEL = 1.5F;
    public static final int BASE_MANA_COST = 50;
    public static final int MANA_COST_PER_LEVEL = 5;
    public static final double COOLDOWN_SECONDS = 15.0;

    public static final float RANGE = 8.0F;            // ความยาว (Length) ในแนวพุ่งไปข้างหน้า 8 บล็อก
    public static final float BEAM_HALF_WIDTH = 0.9F;  // รัศมีความกว้าง (Half Width) = กว้างรวม 1.8 บล็อก
    public static final float BEAM_HALF_HEIGHT = 0.9F; // รัศมีความสูง (Half Height) = สูงรวม 1.8 บล็อก
    public static final int EFFECT_DURATION_TICKS = 160; // ติดสถานะช็อคกิ้ง 8 วินาที (160 ticks)
    public static final float BEAM_PARTICLE_SCALE = 2.5F;

    // ==========================================
    // 3. UNIT INFO / DESCRIPTION
    // ==========================================
    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage",
                        Utils.stringTruncation(getDamage(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.distance",
                        Utils.stringTruncation(RANGE, 0)),
                Component.translatable("ui.irons_spellbooks.effect_length",
                        Utils.stringTruncation(EFFECT_DURATION_TICKS / 20.0F, 0))
        );
    }

    // ==========================================
    // 4. DEFAULT CONFIG
    // ==========================================
    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(GROUND_SCHOOL_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(COOLDOWN_SECONDS)
            .build();

    // ==========================================
    // 5. CONSTRUCTOR
    // ==========================================
    public ShockingSpell() {
        this.manaCostPerLevel = MANA_COST_PER_LEVEL;
        this.baseSpellPower = (int) BASE_DAMAGE;
        this.spellPowerPerLevel = (int) DAMAGE_PER_LEVEL;
        this.castTime = 0;
        this.baseManaCost = BASE_MANA_COST;
    }

    // ==========================================
    // 6. GETTERS & OVERRIDES
    // ==========================================
    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public SchoolType getSchoolType() {
        SchoolType school = SchoolRegistry.getSchool(GROUND_SCHOOL_RESOURCE);
        return school != null ? school : SchoolRegistry.EVOCATION.get();
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public int getManaCost(int spellLevel) {
        return SpellConfig.Shocking.getBaseMana() + (spellLevel - 1) * SpellConfig.Shocking.getManaPerLevel();
    }

    @Override
    public int getSpellCooldown() {
        return (int) (SpellConfig.Shocking.getCooldown() * 20);
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.LIGHTNING_LANCE_CAST.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.LIGHTNING_LANCE_CAST.get());
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.SLASH_ANIMATION;
    }

    public float getDamage(int spellLevel, LivingEntity caster) {
        float baseDmg = SpellConfig.Shocking.getBaseDamage() + (spellLevel - 1) * SpellConfig.Shocking.getDamagePerLevel();
        return getSpellPower(spellLevel, caster) * (baseDmg / BASE_DAMAGE);
    }

    // ==========================================
    // 6. CAST LOGIC
    // ==========================================
    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        Vec3 eyePos = entity.getEyePosition();
        Vec3 lookAngle = entity.getLookAngle().normalize();

        // คำนวณจุดปลายทางสูงสุด 8 บล็อก ตัดระยะเมื่อชนบล็อกทึบ
        Vec3 maxRangePos = eyePos.add(lookAngle.scale(RANGE));
        HitResult blockHit = Utils.raycastForBlock(level, eyePos, maxRangePos, ClipContext.Fluid.NONE);
        Vec3 endPos = blockHit.getType() == HitResult.Type.BLOCK ? blockHit.getLocation() : maxRangePos;

        // คำนวณจุดปล่อยลำแสงจากมือผู้ร่าย (Hand-origin offset) เพื่อความสวยงามสมจริง
        Vec3 rightVec = lookAngle.cross(new Vec3(0, 1, 0)).normalize();
        if (rightVec.lengthSqr() < 0.001) {
            rightVec = new Vec3(1, 0, 0);
        }
        Vec3 handOrigin = eyePos.add(rightVec.scale(0.25D)).add(0, -0.22D, 0).add(lookAngle.scale(0.35D));

        long castSeed = level.random.nextLong();

        // ส่ง Particle ลำแสง Emerald Arc Discharge สีเขียวมรกต เฉพาะผู้เล่นในระยะการมองเห็น (64 บล็อก)
        if (level instanceof ServerLevel serverLevel) {
            ShockingBeamParticleOption beamParticle = new ShockingBeamParticleOption(endPos, BEAM_PARTICLE_SCALE, castSeed);
            double maxObserverDistSq = 64.0D * 64.0D;
            for (ServerPlayer player : serverLevel.players()) {
                if (player.distanceToSqr(handOrigin) <= maxObserverDistSq) {
                    serverLevel.sendParticles(player,
                            beamParticle,
                            true,
                            handOrigin.x, handOrigin.y, handOrigin.z,
                            1, 0, 0, 0, 0);
                }
            }
        }

        // เล่นเสียงปล่อยคลื่นพลังและสั่นหน้าจอ
        level.playSound(null, eyePos.x, eyePos.y, eyePos.z,
                SoundRegistry.LIGHTNING_LANCE_CAST.get(), SoundSource.PLAYERS, 2.0F, 1.2F);
        CameraShakeManager.addCameraShake(new CameraShakeData(8, entity.position(), 10.0F));

        // คำนวณ 3D Volume (กว้าง x ยาว x สูง) ทรงกระบอก/กล่องเจาะทะลวงด้านหน้า
        performPiercingVolumeAttack(level, entity, spellLevel, eyePos, endPos);

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    /**
     * ค้นหาและโจมตีศัตรูทั้งหมดที่อยู่ในพื้นที่ 3D ด้านหน้า (กว้าง x ยาว x สูง) ตามแนววิถีลำแสง
     */
    private void performPiercingVolumeAttack(Level level, LivingEntity caster, int spellLevel, Vec3 startPos, Vec3 endPos) {
        Vec3 seg = endPos.subtract(startPos);
        double segLenSq = seg.lengthSqr();
        if (segLenSq < 0.0001D) {
            return;
        }

        // 1. Broad-phase AABB คลุมทั้งความกว้าง ความยาว และความสูง
        AABB broadBox = new AABB(startPos, endPos).inflate(BEAM_HALF_WIDTH + 0.5D, BEAM_HALF_HEIGHT + 0.5D, BEAM_HALF_WIDTH + 0.5D);

        List<LivingEntity> potentialTargets = level.getEntitiesOfClass(LivingEntity.class, broadBox,
                target -> target != caster && target.isAlive() && !target.isSpectator()
                        && !DamageSources.isFriendlyFireBetween(caster, target));

        float impactDamage = getDamage(spellLevel, caster);

        // 2. Narrow-phase ตรวจสอบระยะห่างทรง 3D จากแกนลำแสง (Width, Height, Length)
        for (LivingEntity target : potentialTargets) {
            AABB targetBox = target.getBoundingBox();
            Vec3 targetCenter = targetBox.getCenter();

            // ฉายเวกเตอร์หาจุดตัดที่ใกล้ที่สุดบนแกนลำแสง
            double t = Math.max(0.0D, Math.min(1.0D, targetCenter.subtract(startPos).dot(seg) / segLenSq));
            Vec3 projPoint = startPos.add(seg.scale(t));

            // ตรวจสอบระยะแนวนอน (กว้าง) และระยะแนวตั้ง (สูง) โดยบวก Hitbox ครึ่งหนึ่งของเป้าหมาย
            double dx = targetCenter.x - projPoint.x;
            double dy = targetCenter.y - projPoint.y;
            double dz = targetCenter.z - projPoint.z;
            double horizontalDist = Math.sqrt(dx * dx + dz * dz);
            double verticalDist = Math.abs(dy);

            double targetHalfWidth = target.getBbWidth() * 0.5D;
            double targetHalfHeight = target.getBbHeight() * 0.5D;

            if (horizontalDist <= (BEAM_HALF_WIDTH + targetHalfWidth) && verticalDist <= (BEAM_HALF_HEIGHT + targetHalfHeight)) {
                // สร้างความเสียหายกระแทกตั้งต้น (Initial Impact Damage)
                DamageSources.applyDamage(target, impactDamage, getDamageSource(caster));

                // ใส่สถานะช็อคกิ้ง (Shocking Effect) 8 วินาที (160 ticks)
                // amplifier = spellLevel - 1 สำหรับคำนวณ DoT Scaling ใน ShockingEffect
                target.addEffect(new MobEffectInstance(
                        MobEffectsRegistry.SHOCKING.get(),
                        EFFECT_DURATION_TICKS,
                        spellLevel - 1,
                        false,
                        false,
                        true
                ));
            }
        }
    }
}
