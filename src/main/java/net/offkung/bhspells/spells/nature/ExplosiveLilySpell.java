package net.offkung.bhspells.spells.nature;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.RaycastBuilder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import io.redspace.ironsspellbooks.network.casting.SyncTargetingDataPacket;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.setup.PacketDistributor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.entity.spells.explosive_lily.ExplosiveLilyBall;

import java.util.List;
import java.util.Optional;

public class ExplosiveLilySpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "explosive_lily");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 1)));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(16)
            .build();

    public ExplosiveLilySpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 20;
        this.spellPowerPerLevel = 2;
        this.castTime = 10;
        this.baseManaCost = 40;
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.NATURE_CAST.get());
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

    private LivingEntity getTargetEntity(Level level, LivingEntity caster) {
        HitResult raycast = RaycastBuilder.begin(level, caster)
                .range(32)
                .checkForBlocks(true)
                .bbInflation(0.5f)
                .build();

        if (raycast.getType() == HitResult.Type.ENTITY) {
            var entity = ((EntityHitResult) raycast).getEntity();
            if (entity instanceof LivingEntity livingTarget && livingTarget.isAlive() && livingTarget != caster) {
                return livingTarget;
            }
        }
        return null;
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        LivingEntity target = getTargetEntity(level, entity);
        if (target != null) {
            playerMagicData.setAdditionalCastData(new TargetEntityCastData(target));
            if (entity instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new SyncTargetingDataPacket(this, List.of(target.getUUID())));
            }
            return true;
        }
        return false;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            LivingEntity target = null;
            if (playerMagicData.getAdditionalCastData() instanceof TargetEntityCastData targetData && level instanceof ServerLevel serverLevel) {
                target = targetData.getTarget(serverLevel);
            }
            if (target == null) {
                target = getTargetEntity(level, entity);
            }

            Vec3 spawnPos = entity.getEyePosition().add(entity.getLookAngle().normalize().scale(0.5));
            ExplosiveLilyBall lily = new ExplosiveLilyBall(level, entity, target);
            lily.setPos(spawnPos);
            lily.setDamage(getDamage(spellLevel, entity));

            if (target != null) {
                Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
                Vec3 dir = targetPos.subtract(spawnPos).normalize();
                lily.setDeltaMovement(dir.scale(lily.getSpeed()));
            } else {
                lily.setDeltaMovement(entity.getLookAngle().normalize().scale(lily.getSpeed()));
            }

            level.addFreshEntity(lily);
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private float getDamage(int spellLevel, LivingEntity caster) {
        return 20.0f + (spellLevel - 1) * 2.0f;
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ONE_HANDED_RAY_CHARGE;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.ONE_HANDED_RAY_SHOOT;
    }
}
