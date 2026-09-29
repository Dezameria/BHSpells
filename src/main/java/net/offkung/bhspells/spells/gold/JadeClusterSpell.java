package net.offkung.bhspells.spells.gold;

import com.gametechbc.traveloptics.api.particle.AdvancedSphereParticleManager;
import com.gametechbc.traveloptics.api.particle.ParticleDirection;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.spells.target_area.TargetedAreaEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.entity.spells.jade_cluster.JadeClusterEntity;
import net.offkung.bhspells.registry.BHSchoolRegistry;
import net.offkung.bhspells.registry.MobEffectsRegistry;

import java.util.List;
import java.util.Optional;

public class JadeClusterSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "jade_cluster");

    public static final int MAX_JADES = 3;
    public static final int COOLDOWN_SECONDS = 30;
    public static final float RADIUS = 10.0F;
    public static final int LIFETIME_TICKS = 300; // 15 seconds

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(BHSchoolRegistry.GOLD_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(0)
            .build();

    public JadeClusterSpell() {
        this.manaCostPerLevel = 6;
        this.baseSpellPower = 20;
        this.spellPowerPerLevel = 1;
        this.baseManaCost = 35;
        this.castTime = 0;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(20.0F * getEntityPowerMultiplier(caster), 1)),
                Component.translatable("ui.irons_spellbooks.aoe_damage", Utils.stringTruncation(60.0F * getEntityPowerMultiplier(caster), 1)),
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(RADIUS, 1)),
                Component.translatable("ui.irons_spellbooks.duration", Utils.timeFromTicks(LIFETIME_TICKS, 1)),
                Component.translatable("ui.irons_spellbooks.cooldown", COOLDOWN_SECONDS)
        );
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
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
        return Optional.of(SoundEvents.STONE_PLACE);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity caster, MagicData playerMagicData) {
        if (level.isClientSide) {
            return true;
        }

        if (JadeClusterEntity.getActiveClusterCount(caster.getUUID()) >= MAX_JADES) {
            if (caster instanceof ServerPlayer player) {
                player.displayClientMessage(Component.translatable("ui.bhspells.jade_cluster_max_active"), true);
            }
            return false;
        }

        if (!(caster instanceof AbstractSpellCastingMob)) {
            Vec3 spawnPosition = this.performRaycast(caster, this.getRaycastRange());
            if (spawnPosition == null) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            this.spawnJadeCluster(level, entity, spellLevel, playerMagicData);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private void spawnJadeCluster(Level level, LivingEntity caster, int spellLevel, MagicData playerMagicData) {
        Vec3 spawnPosition;
        if (caster instanceof AbstractSpellCastingMob) {
            spawnPosition = this.calculateFrontPosition(caster, 2.0D);
        } else {
            spawnPosition = this.performRaycast(caster, this.getRaycastRange());
            if (spawnPosition == null) {
                return;
            }
        }

        if (caster instanceof ServerPlayer serverPlayer && level instanceof ServerLevel serverLevel) {
            spawnCasterOnlySphereParticles(serverLevel, serverPlayer, new Vec3(spawnPosition.x, spawnPosition.y + 1.0D, spawnPosition.z), 50, new BlockParticleOption(ParticleTypes.BLOCK, Blocks.EMERALD_BLOCK.defaultBlockState()), 2.0D);
        } else {
            AdvancedSphereParticleManager.spawnParticles(level, spawnPosition.x, spawnPosition.y + 1.0D, spawnPosition.z, 50, new BlockParticleOption(ParticleTypes.BLOCK, Blocks.EMERALD_BLOCK.defaultBlockState()), ParticleDirection.OUTWARD, 2.0F, false);
        }
        JadeClusterEntity cluster = new JadeClusterEntity(level, caster);
        cluster.setPos(spawnPosition.x, spawnPosition.y, spawnPosition.z);
        cluster.setMaxAge(LIFETIME_TICKS);
        cluster.setRadius(RADIUS);
        cluster.setDamageMultiplier(this.getEntityPowerMultiplier(caster));
        level.addFreshEntity(cluster);

        TargetedAreaEntity visualEntity = TargetedAreaEntity.createTargetAreaEntity(level, spawnPosition, RADIUS, JadeClusterEntity.VISUAL_COLOR, cluster);
        visualEntity.setDuration(LIFETIME_TICKS);
        visualEntity.setShouldFade(true);
        cluster.setVisualEntity(visualEntity);

        // When caster put the jade cluster on, they get 1 second glowing mob effect
        caster.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20, 0, false, false, true));

        // After the caster placed all 3 jades, caster gets MobEffectsRegistry.JADE_CLUSTER for 1 second
        int activeCount = JadeClusterEntity.getActiveClusterCount(caster.getUUID());
        if (activeCount >= MAX_JADES) {
            caster.addEffect(new MobEffectInstance(MobEffectsRegistry.JADE_CLUSTER.get(), 20, 0, false, false, true));
            playerMagicData.getPlayerCooldowns().addCooldown(this, COOLDOWN_SECONDS * 20);
        } else {
            playerMagicData.getPlayerCooldowns().addCooldown(this, 10);
        }
    }

    private Vec3 performRaycast(LivingEntity caster, double range) {
        Vec3 eyePos = caster.getEyePosition();
        Vec3 lookDirection = caster.getLookAngle();
        Vec3 endPos = eyePos.add(lookDirection.scale(range));
        BlockHitResult hitResult = caster.level().clip(new ClipContext(eyePos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos hitPos = hitResult.getBlockPos();
            return new Vec3((double)hitPos.getX() + 0.5D, (double)hitPos.getY() + 1.0D, (double)hitPos.getZ() + 0.5D);
        } else {
            return null;
        }
    }

    private Vec3 calculateFrontPosition(LivingEntity caster, double distance) {
        Vec3 lookDirection = caster.getLookAngle();
        Vec3 horizontalLook = (new Vec3(lookDirection.x, 0.0D, lookDirection.z)).normalize();
        return caster.position().add(horizontalLook.scale(distance));
    }

    private double getRaycastRange() {
        return 30.0D;
    }

    private void spawnCasterOnlySphereParticles(ServerLevel serverLevel, ServerPlayer caster, Vec3 center, int count, ParticleOptions particle, double speed) {
        RandomSource random = serverLevel.getRandom();
        for (int i = 0; i < count; ++i) {
            double u = random.nextDouble() * 2.0D * Math.PI;
            double v = Math.acos(2.0D * random.nextDouble() - 1.0D);
            double x = Math.sin(v) * Math.cos(u);
            double y = Math.sin(v) * Math.sin(u);
            double z = Math.cos(v);

            Vec3 motion = new Vec3(x, y, z).normalize().scale(speed);
            serverLevel.sendParticles(caster, particle, true, center.x, center.y, center.z, 0, motion.x, motion.y, motion.z, 0.1D);
        }
    }
}
