package net.offkung.bhspells.spells.gold;

import com.gametechbc.traveloptics.entity.misc.TOScreenShakeEntity;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.client.particle.CustomZapParticleOption;
import net.offkung.bhspells.registry.BHSchoolRegistry;
import net.offkung.bhspells.util.BHUtil;
import org.joml.Vector3f;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = BHSpells.MODID)
public class SkyEaterSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "sky_eater");

    private static final ParticleEmitterInfo GOAT_HEAD = new ParticleEmitterInfo(BHSpells.id("goat_head"));
    private static final double RADIUS = 23.0D;
    private static final int DEBUFF_DURATION = 100;
    private static final int BUFF_DURATION = 1200; // 1 minute (1200 ticks)
    private static final int AURA_INTERVAL_TICKS = 20; // 1 second
    private static final int REAPPLY_INTERVAL_TICKS = 40; // 2 seconds
    private static final int IMPACT_SPARKS_COUNT = 250;
    private static final int RED_THUNDER_AURA_COUNT = 5;

    private static final Vector3f RED = new Vector3f(1.0f, 0.0f, 0.0f);
    private static final Vector3f BLACK = new Vector3f(0.0f, 0.0f, 0.0f);
    private static final DustColorTransitionOptions SPARK_DUST = new DustColorTransitionOptions(RED, BLACK, 2.0f);
    private static final Vector3f RED_ZAP_COLOR = new Vector3f(1.0f, 0.0f, 0.0f);

    private static final Map<UUID, ActiveAura> ACTIVE_AURAS = new ConcurrentHashMap<>();

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(BHSchoolRegistry.GOLD_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(30)
            .build();

    public SkyEaterSpell() {
        this.manaCostPerLevel = 6;
        this.baseSpellPower = 20;
        this.spellPowerPerLevel = 20;
        this.baseManaCost = 35;
        this.castTime = 10;
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
        return Optional.of(SoundRegistry.SHOCKWAVE_PREPARE.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.ENDER_DRAGON_GROWL);
    }

    @Override
    public boolean canBeInterrupted(Player player) {
        return false;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);

        if (!level.isClientSide) {
            applyDebuffs(entity, level);

            entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, BUFF_DURATION, 1, false, false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, BUFF_DURATION, 1, false, false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, BUFF_DURATION, 1, false, false, true));

            DustColorTransitionOptions dustOptions = new DustColorTransitionOptions(RED, BLACK, 6.0f);

            MagicManager.spawnParticles(level, dustOptions, entity.getX(), entity.getY(), entity.getZ(), 350, 0, 0, 0, 1, true);
            MagicManager.spawnParticles(level, new BlastwaveParticleOptions(RED, 23f), entity.getX(), entity.getY() + .15f, entity.getZ(), 1, 0, 0, 0, 0, true);

            if (level instanceof ServerLevel serverLevel) {
                Vec3 impactCenter = entity.position().add(0.0, 0.1, 0.0);
                BHUtil.spawnImpactSparks(serverLevel, impactCenter, SPARK_DUST, IMPACT_SPARKS_COUNT, 0.3, 0.9, 0.55f);
                BHUtil.spawnRedZapAura(serverLevel, entity, RED_ZAP_COLOR, RED_THUNDER_AURA_COUNT, (float) RADIUS);
            }

            ACTIVE_AURAS.put(entity.getUUID(), new ActiveAura(entity.getUUID(), level.dimension(), BUFF_DURATION));

            float lookX = entity.getXRot() + 90.0F;
            float lookY = entity.getYRot() + 180.0F;
            float rollZ = 0.0F;

            lookX = lookX / 180.0F * 3.14F - 1.57F;
            lookY = lookY / 360.0F * 6.28F;
            rollZ = (rollZ + 180.0F) / 360.0F * 6.28F + 3.14F;

            Vec3 pos = entity.position().add(0, entity.getBbHeight() * 0.5, 0);
            TOScreenShakeEntity.createScreenShake(entity.level(), pos, 23.0f, 0.02f, 10, 0, 2, true);
            AAALevel.addParticle(level, 64.0, GOAT_HEAD.clone().position(entity.getX(), entity.getY(), entity.getZ()).rotation(-lookX, -lookY, -rollZ));
        }
    }

    private static void applyDebuffs(LivingEntity caster, Level level) {
        AABB searchBox = caster.getBoundingBox().inflate(RADIUS);
        List<LivingEntity> targets = level.getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                e -> e != caster && e.isAlive() && !e.isSpectator() && e.distanceToSqr(caster) <= RADIUS * RADIUS
        );

        for (LivingEntity target : targets) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, DEBUFF_DURATION, 3, false, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, DEBUFF_DURATION, 3, false, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, DEBUFF_DURATION, 3, false, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.HUNGER, DEBUFF_DURATION, 1, false, false, true));
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide) {
            return;
        }
        if (ACTIVE_AURAS.isEmpty() || !(event.level instanceof ServerLevel serverLevel)) {
            return;
        }

        Iterator<Map.Entry<UUID, ActiveAura>> iterator = ACTIVE_AURAS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, ActiveAura> entry = iterator.next();
            ActiveAura aura = entry.getValue();

            if (!aura.dimension.equals(serverLevel.dimension())) {
                continue;
            }

            Entity entity = serverLevel.getEntity(aura.casterId);
            if (!(entity instanceof LivingEntity caster) || !caster.isAlive() || caster.isRemoved()) {
                iterator.remove();
                continue;
            }

            aura.ticksRemaining--;
            int elapsed = BUFF_DURATION - aura.ticksRemaining;

            if (elapsed > 0 && elapsed % AURA_INTERVAL_TICKS == 0) {
                BHUtil.spawnRedZapAura(serverLevel, caster, RED_ZAP_COLOR, RED_THUNDER_AURA_COUNT, (float) RADIUS);
                MagicManager.spawnParticles(serverLevel, SPARK_DUST, caster.getX(), caster.getY(), caster.getZ(), 1200, 20, 10, 20, 0.02, true);
                MagicManager.spawnParticles(serverLevel, new BlastwaveParticleOptions(RED, 23f), caster.getX(), caster.getY() + .15f, caster.getZ(), 1, 0, 0, 0, 0, true);
                serverLevel.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundRegistry.LIGHTNING_WOOSH_01.get(), SoundSource.NEUTRAL, 1.3f, 1.0f);
            }

            if (elapsed > 0 && elapsed % REAPPLY_INTERVAL_TICKS == 0) {
                applyDebuffs(caster, serverLevel);
            }

            if (aura.ticksRemaining <= 0) {
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ACTIVE_AURAS.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        ActiveAura aura = ACTIVE_AURAS.get(event.getEntity().getUUID());
        if (aura != null) {
            aura.dimension = event.getTo();
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        ACTIVE_AURAS.remove(event.getEntity().getUUID());
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        super.onServerCastTick(level, spellLevel, entity, playerMagicData);

        if (level instanceof ServerLevel serverLevel) {
            spawnCastingParticles(serverLevel, entity);
        }
    }

    private void spawnCastingParticles(ServerLevel serverLevel, LivingEntity entity) {
        DustColorTransitionOptions dustOptions = new DustColorTransitionOptions(RED, BLACK, 1.5f);
        MagicManager.spawnParticles(serverLevel, dustOptions, entity.getX(), entity.getY(), entity.getZ(), 20, 0, 0, 0, 0.1, true);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.PREPARE_CROSS_ARMS;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.CAST_T_POSE;
    }

    private static class ActiveAura {
        final UUID casterId;
        ResourceKey<Level> dimension;
        int ticksRemaining;

        ActiveAura(UUID casterId, ResourceKey<Level> dimension, int ticksRemaining) {
            this.casterId = casterId;
            this.dimension = dimension;
            this.ticksRemaining = ticksRemaining;
        }
    }
}
