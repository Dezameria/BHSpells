package net.offkung.bhspells.spells.gold;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.RaycastBuilder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import io.redspace.ironsspellbooks.entity.spells.ice_block.IceBlockProjectile;
import io.redspace.ironsspellbooks.network.casting.SyncTargetingDataPacket;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.setup.PacketDistributor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.entity.spells.stone_crumble.StoneCrumbleProjectile;
import net.offkung.bhspells.registry.BHSchoolRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class StoneCrumbleSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "stone_crumble");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getSpellPower(spellLevel, caster), 1)));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(BHSchoolRegistry.GOLD_RESOURCE)
            .setMaxLevel(6)
            .setCooldownSeconds(15)
            .build();

    public StoneCrumbleSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 14;
        this.spellPowerPerLevel = 2;
        this.castTime = 25;
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
        return Optional.of(SoundRegistry.ICE_BLOCK_CAST.get());
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        Vec3 aimPoint = getAimPoint(level, entity, 32);
        List<LivingEntity> nearbyTargets = getNearbyTargets(level, entity, aimPoint, 10, 5);
        if (!nearbyTargets.isEmpty() && entity instanceof ServerPlayer serverPlayer) {
            List<UUID> uuids = nearbyTargets.stream().map(LivingEntity::getUUID).toList();
            PacketDistributor.sendToPlayer(serverPlayer, new SyncTargetingDataPacket(this, uuids));
        }
        return true;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        Vec3 aimPoint = getAimPoint(level, entity, 32);
        List<LivingEntity> targets = getNearbyTargets(level, entity, aimPoint, 10, 5);

        for (LivingEntity target : targets) {
            int spawnheight = 4 + (int) (target.getBbHeight() * 0.5f);
            Vec3 spawn = target.position();

            StoneCrumbleProjectile stoneCrumble = new StoneCrumbleProjectile(level, entity, target);
            stoneCrumble.moveTo(raiseWithCollision(spawn, spawnheight, level));
            if (!level.collidesWithSuffocatingBlock(stoneCrumble, stoneCrumble.getBoundingBox())) {
                stoneCrumble.noPhysics = true;
            }
            stoneCrumble.setAirTime(35);
            stoneCrumble.setDamage(getDamage(spellLevel, entity));
            level.addFreshEntity(stoneCrumble);
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private Vec3 getAimPoint(Level level, LivingEntity caster, int maxRange) {
        HitResult raycast = RaycastBuilder.begin(level, caster)
                .range(maxRange)
                .checkForBlocks(true)
                .bbInflation(.25f)
                .build();

        if (raycast.getType() == HitResult.Type.ENTITY) {
            return ((EntityHitResult) raycast).getEntity().position();
        } else if (raycast.getType() == HitResult.Type.BLOCK) {
            return raycast.getLocation();
        } else {
            // MISS - fall back to a point maxRange blocks out along the look vector
            return caster.getEyePosition().add(caster.getLookAngle().normalize().scale(maxRange));
        }
    }

    private List<LivingEntity> getNearbyTargets(Level level, LivingEntity caster, Vec3 center, double range, int maxTargets) {
        AABB searchBox = new AABB(
                center.x - range, center.y - range, center.z - range,
                center.x + range, center.y + range, center.z + range
        );
        return level.getEntitiesOfClass(LivingEntity.class, searchBox)
                .stream()
                .filter(e -> e != caster)
                .filter(LivingEntity::isAlive)
                .filter(e -> !e.isSpectator())
                .filter(e -> e.position().distanceToSqr(center) <= range * range)
                .filter(e -> Utils.hasLineOfSight(level, caster, e, false))
                .filter(e -> !DamageSources.isFriendlyFireBetween(caster, e))
                .sorted(Comparator.comparingDouble(e -> e.position().distanceToSqr(center)))
                .limit(maxTargets)
                .collect(Collectors.toList());
    }

    private Vec3 raiseWithCollision(Vec3 start, int blocks, Level level) {
        for (int i = 0; i < blocks; i++) {
            Vec3 raised = start.add(0, 1, 0);
            if (level.getBlockState(BlockPos.containing(raised)).isAir())
                start = raised;
            else
                break;
        }
        return start;
    }

    @Override
    public SpellDamageSource getDamageSource(@Nullable Entity projectile, Entity attacker) {
        return super.getDamageSource(projectile, attacker).setIFrames(0);
    }

    private float getDamage(int spellLevel, LivingEntity entity) {
        return this.getSpellPower(spellLevel, entity);
    }
}
