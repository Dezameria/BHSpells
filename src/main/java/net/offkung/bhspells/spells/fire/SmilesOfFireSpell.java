package net.offkung.bhspells.spells.fire;

import com.gametechbc.traveloptics.entity.misc.TOScreenShakeEntity;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.RaycastBuilder;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.network.debug.PlayPlayerAnimationPacket;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.render.animation.AnimationHelper;
import io.redspace.ironsspellbooks.setup.PacketDistributor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.entity.spells.flames_eagle.FlamesEagleEntity;
import net.offkung.bhspells.registry.BHSoundRegistry;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SmilesOfFireSpell extends AbstractSpell {
    private static final Map<UUID, UUID> ACTIVE_EAGLES = new ConcurrentHashMap<>();
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "smiles_of_fire");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(30)
            .build();

    public SmilesOfFireSpell() {
        this.manaCostPerLevel = 0;
        this.baseSpellPower = 0;
        this.spellPowerPerLevel = 0;
        this.castTime = 15;
        this.baseManaCost = 40;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", 40),
                Component.translatable("ui.bhspells.smiles_of_fire.recast")
        );
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
        return Optional.of(SoundRegistry.FIRE_CAST.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.FIRE_BOMB_CAST.get());
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ONE_HANDED_HORIZONTAL_SWING_ANIMATION;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return AnimationHolder.pass();
    }

    @Override
    public boolean canBeInterrupted(Player player) {
        return false;
    }

    @Override
    public int getRecastCount(int spellLevel, @Nullable LivingEntity entity) {
        return 2;
    }

    @Override
    public int getEffectiveCastTime(int spellLevel, @Nullable LivingEntity entity) {
        if (entity != null) {
            MagicData magicData = MagicData.getPlayerMagicData(entity);
            if (magicData.getPlayerRecasts().hasRecastForSpell(this)) {
                return 0;
            }
        }
        return super.getEffectiveCastTime(spellLevel, entity);
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        if (entity.hasEffect(MobEffectsRegistry.SMILES_OF_FIRE_CD.get())) {
            return false;
        }
        if (playerMagicData.getPlayerRecasts().hasRecastForSpell(this)) {
            return true;
        }
        return super.checkPreCastConditions(level, spellLevel, entity, playerMagicData);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            if (playerMagicData.getPlayerRecasts().hasRecastForSpell(this)) {
                // Play animation packet with slash animation on recast
                playCasterAnimation(entity, SpellAnimations.SLASH_ANIMATION);

                // Recast: Smoothly redirect and home towards the nearest target where the caster is aiming
                UUID eagleUuid = ACTIVE_EAGLES.get(entity.getUUID());
                if (eagleUuid != null && level instanceof ServerLevel serverLevel) {
                    Entity existing = serverLevel.getEntity(eagleUuid);
                    if (existing instanceof FlamesEagleEntity eagle && eagle.isAlive()) {
                        LivingEntity targetEntity = findAimTargetEntity(level, entity, eagle);
                        Vec3 fallbackPos = getAimTargetPos(level, entity);
                        eagle.redirectTowards(targetEntity, fallbackPos);
                        level.playSound(null, eagle.getX(), eagle.getY(), eagle.getZ(), SoundRegistry.FIRE_BOMB_CAST.get(), SoundSource.PLAYERS, 1.2f, 1.0f);
                    }
                }

                // Apply SMILES_OF_FIRE_CD for 1 second (20 ticks) on recast
                entity.addEffect(new MobEffectInstance(MobEffectsRegistry.SMILES_OF_FIRE_CD.get(), 20, 0));
            } else {
                // Initial cast: Summon and launch FlamesEagleEntity
                discardActiveEagle(level, entity);

                boolean empowered = false;
                var smilesEffect = entity.getEffect(MobEffectsRegistry.SMILES_OF_FIRE.get());
                if (smilesEffect != null && smilesEffect.getAmplifier() >= 4) {
                    empowered = true;
                }

                Vec3 spawnPos = entity.getEyePosition().add(entity.getLookAngle().normalize().scale(1.5));
                Vec3 targetPos = getAimTargetPos(level, entity);
                Vec3 dir = targetPos.subtract(spawnPos).normalize();
                if (dir.dot(entity.getLookAngle()) < 0) {
                    dir = entity.getLookAngle().normalize();
                }

                FlamesEagleEntity eagle = new FlamesEagleEntity(level, entity);
                eagle.setPos(spawnPos);
                eagle.setEmpowered(empowered);
                eagle.setDamage(empowered ? 80.0F : 40.0F);
                eagle.shoot(dir);

                level.addFreshEntity(eagle);
                ACTIVE_EAGLES.put(entity.getUUID(), eagle.getUUID());
                level.playSound(null, eagle.getX(), eagle.getY(), eagle.getZ(), BHSoundRegistry.EAGLE_SCREAM.get(), SoundSource.PLAYERS, 1.3f, 1.0f);
                level.playSound(null, eagle.getX(), eagle.getY(), eagle.getZ(), SoundRegistry.FIRE_ERUPTION_SLAM.get(), SoundSource.PLAYERS, 1.3f, 1.0f);
                TOScreenShakeEntity.createScreenShake(level, entity.position(), 7.0F, 0.07F, 10, 0, 5, true);

                if (empowered) {
                    entity.removeEffect(MobEffectsRegistry.SMILES_OF_FIRE.get());
                    entity.addEffect(new MobEffectInstance(MobEffectsRegistry.SMILES_OF_FIRE.get(), FlamesEagleEntity.MAX_LIFETIME, 5, false, false, false));
                } else {
                    entity.addEffect(new MobEffectInstance(MobEffectsRegistry.SMILES_OF_FIRE.get(), FlamesEagleEntity.MAX_LIFETIME, 1, false, false, false));
                }

                playerMagicData.getPlayerRecasts().addRecast(
                        new RecastInstance(getSpellId(), spellLevel, 2, FlamesEagleEntity.MAX_LIFETIME, castSource, null),
                        playerMagicData
                );
            }
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public void onClientCast(Level level, int spellLevel, LivingEntity entity, ICastData castData) {
        super.onClientCast(level, spellLevel, entity, castData);
        if (entity instanceof Player player) {
            MagicData magicData = MagicData.getPlayerMagicData(player);
            if (magicData.getPlayerRecasts().hasRecastForSpell(this)) {
                SpellAnimations.SLASH_ANIMATION.getForPlayer().ifPresent(animId -> AnimationHelper.animatePlayerStart(player, animId));
            }
        }
    }

    private void playCasterAnimation(LivingEntity caster, AnimationHolder animation) {
        if (!(caster instanceof ServerPlayer)) {
            return;
        }
        animation.getForPlayer().ifPresent(animationId -> PacketDistributor.sendToPlayersTrackingEntityAndSelf(caster, new PlayPlayerAnimationPacket(caster.getUUID(), animationId)));
    }

    @Override
    public void onRecastFinished(ServerPlayer serverPlayer, RecastInstance recastInstance, RecastResult recastResult, ICastDataSerializable castDataSerializable) {
        ACTIVE_EAGLES.remove(serverPlayer.getUUID());
        super.onRecastFinished(serverPlayer, recastInstance, recastResult, castDataSerializable);
    }

    private void discardActiveEagle(Level level, LivingEntity entity) {
        UUID eagleUuid = ACTIVE_EAGLES.remove(entity.getUUID());
        if (eagleUuid != null && level instanceof ServerLevel serverLevel) {
            Entity existing = serverLevel.getEntity(eagleUuid);
            if (existing instanceof FlamesEagleEntity eagle && eagle.isAlive()) {
                eagle.discard();
            }
        }
    }

    private @Nullable LivingEntity findAimTargetEntity(Level level, LivingEntity caster, FlamesEagleEntity eagle) {
        // 1. Direct raycast check
        HitResult raycast = RaycastBuilder.begin(level, caster)
                .range(64)
                .checkForBlocks(true)
                .bbInflation(0.5f)
                .build();

        if (raycast.getType() == HitResult.Type.ENTITY) {
            var hit = ((EntityHitResult) raycast).getEntity();
            if (hit instanceof LivingEntity living && living.isAlive() && living != caster && !DamageSources.isFriendlyFireBetween(living, caster) && !eagle.hasHitEntity(living)) {
                return living;
            }
        }

        // 2. Search nearest target to caster's aim ray within 64 blocks
        Vec3 eyePos = caster.getEyePosition();
        Vec3 lookVec = caster.getLookAngle().normalize();
        double maxRange = 64.0;
        AABB searchBox = caster.getBoundingBox().inflate(maxRange);

        LivingEntity bestTarget = null;
        double closestScore = Double.MAX_VALUE;

        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, searchBox, target ->
                target != caster && target.isAlive() && !target.isSpectator() && !DamageSources.isFriendlyFireBetween(target, caster) && !eagle.hasHitEntity(target)
        );

        for (LivingEntity candidate : entities) {
            Vec3 center = candidate.getBoundingBox().getCenter();
            Vec3 toCandidate = center.subtract(eyePos);
            double projection = toCandidate.dot(lookVec);
            if (projection > 0 && projection <= maxRange) {
                Vec3 pointOnRay = eyePos.add(lookVec.scale(projection));
                double distToRay = pointOnRay.distanceTo(center);
                // Allow up to 6 blocks deviation from crosshair line
                if (distToRay <= 6.0) {
                    // Check line of sight from caster to candidate center
                    HitResult blockCheck = level.clip(new ClipContext(eyePos, center, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
                    if (blockCheck.getType() == HitResult.Type.MISS || blockCheck.getLocation().distanceTo(center) < 1.0) {
                        double score = distToRay * 3.0 + projection * 0.1;
                        if (score < closestScore) {
                            closestScore = score;
                            bestTarget = candidate;
                        }
                    }
                }
            }
        }

        return bestTarget;
    }

    private Vec3 getAimTargetPos(Level level, LivingEntity caster) {
        HitResult raycast = RaycastBuilder.begin(level, caster)
                .range(64)
                .checkForBlocks(true)
                .bbInflation(0.5f)
                .build();

        if (raycast.getType() == HitResult.Type.ENTITY) {
            var hitEntity = ((EntityHitResult) raycast).getEntity();
            if (hitEntity instanceof LivingEntity living && living.isAlive() && living != caster) {
                return living.getBoundingBox().getCenter();
            }
        }
        if (raycast.getType() == HitResult.Type.BLOCK) {
            return raycast.getLocation();
        }
        return caster.getEyePosition().add(caster.getLookAngle().normalize().scale(64.0));
    }
}

