package net.offkung.bhspells.spells.gold;

import com.gametechbc.traveloptics.spells.TravelopticsSpellAnimations;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.network.debug.PlayPlayerAnimationPacket;
import io.redspace.ironsspellbooks.setup.PacketDistributor;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import com.gametechbc.traveloptics.util.TravelopticsParticleHelper;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.client.particle.ColoredCherryParticleOption;
import net.offkung.bhspells.client.particle.FallingLeafParticleOption;
import net.offkung.bhspells.entity.spells.jade_brush_slash.JadeBrushSlash;
import net.offkung.bhspells.entity.spells.red_beryl_bird.RedBerylBird;
import net.offkung.bhspells.registry.BHSchoolRegistry;
import net.offkung.bhspells.registry.BHSoundRegistry;
import net.offkung.bhspells.registry.ParticleRegistry;
import net.minecraft.sounds.SoundEvent;
import org.joml.Vector3f;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Mod.EventBusSubscriber
public class JadeWaveSpell extends AbstractSpell {
    private static JadeWaveSpell INSTANCE;

    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "jade_wave");

    public enum SeasonWave {
        AUTUMN(new ParticleEmitterInfo(BHSpells.id("autumn_wave")), new Vector3f(1f, 0.47f, 0.04f), BHSoundRegistry.AUTUMN_WAVE),
        SUMMER(new ParticleEmitterInfo(BHSpells.id("summer_wave")), new Vector3f(1f, 0.44f, 0.44f), BHSoundRegistry.SUMMER_WAVE),
        SPRING(new ParticleEmitterInfo(BHSpells.id("spring_wave")), new Vector3f(1f, 0.84f, 0.06f), BHSoundRegistry.SPRING_WAVE),
        RAINY(new ParticleEmitterInfo(BHSpells.id("rainy_wave")), new Vector3f(0.26f, 1f, 0.68f), BHSoundRegistry.RAINY_WAVE),
        WINTER(new ParticleEmitterInfo(BHSpells.id("winter_wave")), new Vector3f(0.95f, 0.98f, 1.00f), BHSoundRegistry.WINTER_WAVE);

        private final ParticleEmitterInfo emitterInfo;
        private final Vector3f leafColor;
        private final RegistryObject<SoundEvent> soundEvent;

        SeasonWave(ParticleEmitterInfo emitterInfo, Vector3f leafColor, RegistryObject<SoundEvent> soundEvent) {
            this.emitterInfo = emitterInfo;
            this.leafColor = leafColor;
            this.soundEvent = soundEvent;
        }

        public ParticleEmitterInfo getEmitterInfo() {
            return emitterInfo;
        }

        public Vector3f getLeafColor() {
            return leafColor;
        }

        public SoundEvent getSoundEvent() {
            return soundEvent.get();
        }
    }

    private static final SeasonWave[] WAVES = SeasonWave.values();
    private static final int WAVE_INTERVAL_TICKS = 30;
    private static final double DAMAGE_RADIUS = 5.0;

    private static final int BIRD_COUNT = 5;
    private static final double BIRD_SEEK_RADIUS = 10.0;
    private static final float BIRD_DAMAGE = 8.0f;
    private static final double BIRD_SPAWN_SPREAD_RADIUS = 1.2;
    private static final double BIRD_DIRECTION_SPREAD_DEGREES = 25.0;
    private static final double JUMP_VELOCITY = 0.42;
    private static final int JADE_BRUSH_SLASH_DELAY_TICKS = 4; // 1 second

    private static final Map<UUID, ActiveSequence> ACTIVE_SEQUENCES = new ConcurrentHashMap<>();
    private static final Queue<PendingSlash> PENDING_SLASHES = new ConcurrentLinkedQueue<>();

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(BHSchoolRegistry.GOLD_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(0)
            .build();

    public JadeWaveSpell() {
        this.manaCostPerLevel = 20;
        this.baseSpellPower = 20;
        this.spellPowerPerLevel = 2;
        this.castTime = 0;
        this.baseManaCost = 40;
        INSTANCE = this;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.radius", (int) DAMAGE_RADIUS)
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

    public float getDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);

        if (!level.isClientSide) {
            // Wave 1 (Autumn): Trigger immediately on cast
            triggerWave(level, entity, spellLevel, SeasonWave.AUTUMN, playerMagicData);

            // Queue remaining waves: Summer, Spring, Rainy, Winter
            ACTIVE_SEQUENCES.put(entity.getUUID(), new ActiveSequence(
                    entity.getUUID(),
                    level.dimension(),
                    spellLevel,
                    0, // Currently active wave is AUTUMN (index 0)
                    1, // Next wave is SUMMER (index 1)
                    WAVE_INTERVAL_TICKS
            ));
        }
    }

    private List<LivingEntity> findNearbyBirdTargets(Level level, LivingEntity caster) {
        AABB searchBox = caster.getBoundingBox().inflate(BIRD_SEEK_RADIUS);
        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, searchBox, target ->
                target != caster &&
                target.isAlive() &&
                !target.isSpectator() &&
                target.distanceTo(caster) <= BIRD_SEEK_RADIUS &&
                !DamageSources.isFriendlyFireBetween(caster, target) &&
                caster.hasLineOfSight(target)
        );
        candidates.sort(Comparator.comparingDouble(caster::distanceTo));
        return candidates;
    }

    private void spawnRedBerylBirds(Level level, LivingEntity caster) {
        List<LivingEntity> candidates = findNearbyBirdTargets(level, caster);
        if (candidates.isEmpty()) {
            return;
        }

        Vec3 basePos = caster.position().add(0, caster.getBbHeight() * 0.5, 0);
        for (int i = 0; i < BIRD_COUNT; i++) {
            List<LivingEntity> pool = candidates.subList(0, Math.min(5, candidates.size()));
            LivingEntity target = pool.get(level.getRandom().nextInt(pool.size()));

            // Spread birds around the caster in a small ring so they don't spawn stacked on each other.
            double angle = (2.0 * Math.PI * i) / BIRD_COUNT;
            Vec3 spawnOffset = new Vec3(Math.cos(angle), 0, Math.sin(angle)).scale(BIRD_SPAWN_SPREAD_RADIUS);
            Vec3 spawnPos = basePos.add(spawnOffset);

            RedBerylBird bird = new RedBerylBird(level, caster);
            bird.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            bird.setDamage(BIRD_DAMAGE);

            level.playSound(
                    null,
                    spawnPos.x,
                    spawnPos.y,
                    spawnPos.z,
                    BHSoundRegistry.UNLEASH_BIRDS.get(),
                    caster instanceof Player ? SoundSource.PLAYERS : SoundSource.NEUTRAL,
                    1.0f,
                    1.0f
            );

            Vec3 toTarget = target.getBoundingBox().getCenter().subtract(spawnPos);
            Vec3 baseDirection = toTarget.lengthSqr() > 1.0E-6 ? toTarget.normalize() : caster.getLookAngle();
            bird.shoot(spreadDirection(baseDirection, level.getRandom()));

            bird.setTarget(target);
            level.addFreshEntity(bird);
        }
    }

    // Nudges a direction vector off-axis by a random small angle so birds sharing (or
    // approaching) the same target visibly fan out instead of flying in a single overlapping line.
    private Vec3 spreadDirection(Vec3 direction, RandomSource random) {
        Vec3 arbitrary = Math.abs(direction.y) < 0.99 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        Vec3 perpendicular1 = direction.cross(arbitrary).normalize();
        Vec3 perpendicular2 = direction.cross(perpendicular1).normalize();

        double maxOffset = Math.tan(Math.toRadians(BIRD_DIRECTION_SPREAD_DEGREES));
        double offset1 = (random.nextDouble() * 2.0 - 1.0) * maxOffset;
        double offset2 = (random.nextDouble() * 2.0 - 1.0) * maxOffset;

        return direction.add(perpendicular1.scale(offset1)).add(perpendicular2.scale(offset2)).normalize();
    }

    private void playCasterAnimation(LivingEntity caster, AnimationHolder animation) {
        if (!(caster instanceof ServerPlayer)) {
            return;
        }
        animation.getForPlayer().ifPresent(animationId -> PacketDistributor.sendToPlayersTrackingEntityAndSelf(caster, new PlayPlayerAnimationPacket(caster.getUUID(), animationId)));
    }

    private void spawnJadeBrushSlash(Level level, LivingEntity caster, MagicData playerMagicData) {
        boolean mirrored = playerMagicData.getCastingEquipmentSlot().equals(SpellSelectionManager.OFFHAND);
        JadeBrushSlash jadeBrushSlash = new JadeBrushSlash(level, mirrored);
        jadeBrushSlash.setOwner(caster);

        // The renderer draws the slash quad centered getBbHeight()*0.5 above the entity's own
        // position, so pin that midpoint to the caster's body center instead of its feet/eyes.
        double slashHalfHeight = jadeBrushSlash.getBbHeight() * 0.5;
        double casterCenterY = caster.getY() + caster.getBbHeight() * 0.5;
        jadeBrushSlash.setPos(caster.getX(), casterCenterY - slashHalfHeight, caster.getZ());

        jadeBrushSlash.setYRot(caster.getYRot());
        jadeBrushSlash.setXRot(caster.getXRot());
        level.addFreshEntity(jadeBrushSlash);
    }

    public void triggerWave(Level level, LivingEntity caster, int spellLevel, SeasonWave wave, MagicData playerMagicData) {
        level.playSound(
                null,
                caster.getX(),
                caster.getY(),
                caster.getZ(),
                wave.getSoundEvent(),
                caster instanceof Player ? SoundSource.PLAYERS : SoundSource.NEUTRAL,
                1.0f,
                1.2f
        );

        playCasterAnimation(caster, SpellAnimations.SLASH_ANIMATION);

        PENDING_SLASHES.add(new PendingSlash(caster.getUUID(), level.dimension(), playerMagicData, JADE_BRUSH_SLASH_DELAY_TICKS));

        AAALevel.addParticle(
                level,
                64.0,
                wave.getEmitterInfo().clone().bindOnEntity(caster).scale(1, 1, 1)
        );

        // 3. Colored cherry leaves spreading out from caster (size 0.7)
        if (level instanceof ServerLevel serverLevel) {
            Vec3 center = caster.position().add(0, caster.getBbHeight() * 0.45, 0);
            ColoredCherryParticleOption cherryOption = new ColoredCherryParticleOption(wave.getLeafColor(), 0.7f);

            int radialCount = 36;
            double outwardSpeed = 0.42;
            for (int i = 0; i < radialCount; i++) {
                double angle = (2.0 * Math.PI * i) / radialCount;
                double vx = Math.cos(angle) * outwardSpeed;
                double vz = Math.sin(angle) * outwardSpeed;
                double vy = 0.05 + serverLevel.random.nextDouble() * 0.08;
                serverLevel.sendParticles(cherryOption, center.x, center.y, center.z, 0, vx, vy, vz, 1.0);
            }

            serverLevel.sendParticles(cherryOption, center.x, center.y, center.z, 20, 0.8, 0.3, 0.8, 0.15);
        }

        // 4. Area damage to entities within 5 blocks
        AABB searchBox = caster.getBoundingBox().inflate(DAMAGE_RADIUS);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, searchBox, target ->
                target != caster &&
                target.isAlive() &&
                !target.isSpectator() &&
                target.distanceTo(caster) <= DAMAGE_RADIUS &&
                !DamageSources.isFriendlyFireBetween(caster, target)
        );

        float damage = getDamage(spellLevel, caster);
        DamageSource damageSource = getDamageSource(caster);
        for (LivingEntity target : targets) {
            DamageSources.applyDamage(target, damage, damageSource);
            level.playSound(
                    null,
                    target.getX(),
                    target.getY(),
                    target.getZ(),
                    BHSoundRegistry.JADE_WAVE_HIT.get(),
                    SoundSource.NEUTRAL,
                    1.6f,
                    1.0f
            );
        }

        spawnRedBerylBirds(level, caster);

        // 5. Dash forward ~1.5 blocks
        Vec3 look = caster.getLookAngle();
        Vec3 forward = new Vec3(look.x, 0, look.z);
        if (forward.lengthSqr() > 1.0E-5) {
            forward = forward.normalize();
        } else {
            forward = caster.getForward();
        }

        double horizontalSpeed = caster.onGround() ? 1.5 : 0.35;
        double verticalLift = JUMP_VELOCITY;
        Vec3 dashMotion = forward.scale(horizontalSpeed).add(0, verticalLift, 0);

        caster.setDeltaMovement(dashMotion);
        caster.hasImpulse = true;
        caster.hurtMarked = true;

        if (caster instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
        }
    }

    private void spawnContinuousWaveParticles(ServerLevel serverLevel, LivingEntity caster, SeasonWave wave) {
        RandomSource random = serverLevel.random;
        double baseX = caster.getX();
        double baseY = caster.getY();
        double baseZ = caster.getZ();

        int particlesPerTick = 3;
        for (int i = 0; i < particlesPerTick; i++) {
            double x = baseX + (random.nextDouble() * 2.0 - 1.0) * DAMAGE_RADIUS;
            double z = baseZ + (random.nextDouble() * 2.0 - 1.0) * DAMAGE_RADIUS;

            switch (wave) {
                case AUTUMN -> {
                    // Orange leaves falling from above down to the ground
                    double y = baseY + 3.0 + random.nextDouble() * 2.5;
                    FallingLeafParticleOption leaf = new FallingLeafParticleOption(wave.getLeafColor(), 0.45f);
                    serverLevel.sendParticles(leaf, x, y, z, 0, 0.0, -0.12, 0.0, 1.0);
                }
                case SUMMER -> {
                    // Fire climbing from the ground up into the sky
                    double y = baseY + 0.05;
                    serverLevel.sendParticles(ParticleHelper.FIRE_EMITTER, x, y, z, 0, 0.0, 0.2, 0.0, 1.0);
                }
                case SPRING -> {
                    // Sakura splatter raining down from the sky
                    double y = baseY + 1.0 + random.nextDouble() * 2.5;
                    serverLevel.sendParticles(ParticleRegistry.SPLATTER_SAKURA.get(), x, y, z, 0, 0.50, 0, 0.50, 1.0);
                }
                case RAINY -> {
                    // Raindrops falling from above
                    double y = baseY + 4.0 + random.nextDouble() * 2.0;
                    serverLevel.sendParticles(TravelopticsParticleHelper.WATER_DROP, x, y, z, 0, 0.0, -0.6, 0.0, 1.0);
                }
                case WINTER -> {
                    // Snow dust drifting down from above
                    double y = baseY + 3.0 + random.nextDouble() * 2.5;
                    serverLevel.sendParticles(ParticleHelper.SNOWFLAKE, x, y, z, 0, 0.0, -0.05, 0.0, 1.0);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide) {
            return;
        }
        if ((ACTIVE_SEQUENCES.isEmpty() && PENDING_SLASHES.isEmpty()) || !(event.level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!PENDING_SLASHES.isEmpty()) {
            Iterator<PendingSlash> pendingIterator = PENDING_SLASHES.iterator();
            while (pendingIterator.hasNext()) {
                PendingSlash pending = pendingIterator.next();
                if (!pending.dimension.equals(serverLevel.dimension())) {
                    continue;
                }

                pending.ticksRemaining--;
                if (pending.ticksRemaining <= 0) {
                    Entity entity = serverLevel.getEntity(pending.casterId);
                    if (entity instanceof LivingEntity caster && caster.isAlive() && !caster.isRemoved() && INSTANCE != null) {
                        INSTANCE.spawnJadeBrushSlash(serverLevel, caster, pending.playerMagicData);
                    }
                    pendingIterator.remove();
                }
            }
        }

        Iterator<Map.Entry<UUID, ActiveSequence>> iterator = ACTIVE_SEQUENCES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, ActiveSequence> entry = iterator.next();
            ActiveSequence sequence = entry.getValue();

            if (!sequence.dimension.equals(serverLevel.dimension())) {
                continue;
            }

            Entity entity = serverLevel.getEntity(sequence.casterId);
            if (!(entity instanceof LivingEntity caster) || !caster.isAlive() || caster.isRemoved()) {
                iterator.remove();
                continue;
            }

            if (INSTANCE != null) {
                INSTANCE.spawnContinuousWaveParticles(serverLevel, caster, WAVES[sequence.currentWaveIndex]);
            }

            sequence.ticksUntilNextWave--;
            if (sequence.ticksUntilNextWave <= 0) {
                if (sequence.nextWaveIndex < WAVES.length && INSTANCE != null) {
                    SeasonWave wave = WAVES[sequence.nextWaveIndex];
                    INSTANCE.triggerWave(serverLevel, caster, sequence.spellLevel, wave, MagicData.getPlayerMagicData(caster));
                    sequence.currentWaveIndex = sequence.nextWaveIndex;
                    sequence.nextWaveIndex++;
                    sequence.ticksUntilNextWave = WAVE_INTERVAL_TICKS;
                } else {
                    // Last wave's continuous particle duration has now finished
                    iterator.remove();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ACTIVE_SEQUENCES.remove(event.getEntity().getUUID());
        PENDING_SLASHES.removeIf(pending -> pending.casterId.equals(event.getEntity().getUUID()));
    }

    private static class PendingSlash {
        final UUID casterId;
        final ResourceKey<Level> dimension;
        final MagicData playerMagicData;
        int ticksRemaining;

        PendingSlash(UUID casterId, ResourceKey<Level> dimension, MagicData playerMagicData, int ticksRemaining) {
            this.casterId = casterId;
            this.dimension = dimension;
            this.playerMagicData = playerMagicData;
            this.ticksRemaining = ticksRemaining;
        }
    }

    private static class ActiveSequence {
        final UUID casterId;
        final ResourceKey<Level> dimension;
        final int spellLevel;
        int currentWaveIndex;
        int nextWaveIndex;
        int ticksUntilNextWave;

        ActiveSequence(UUID casterId, ResourceKey<Level> dimension, int spellLevel, int currentWaveIndex, int nextWaveIndex, int ticksUntilNextWave) {
            this.casterId = casterId;
            this.dimension = dimension;
            this.spellLevel = spellLevel;
            this.currentWaveIndex = currentWaveIndex;
            this.nextWaveIndex = nextWaveIndex;
            this.ticksUntilNextWave = ticksUntilNextWave;
        }
    }
}
