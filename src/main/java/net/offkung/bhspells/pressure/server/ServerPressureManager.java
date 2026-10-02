package net.offkung.bhspells.pressure.server;

import net.offkung.bhspells.pressure.PressureReactionDispatcher;
import net.offkung.bhspells.config.SpellConfig;
import net.offkung.bhspells.pressure.PressureFieldData;
import net.offkung.bhspells.pressure.PressureReaction;
import net.offkung.bhspells.pressure.network.PressureNetwork;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import io.redspace.ironsspellbooks.damage.DamageSources;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-authoritative manager for spiritual pressure fields and torrential domains.
 * Manages field lifecycle, toggle stance states, 5-tick spatial AoE queries,
 * strongest-field-wins overlap resolution, periodic lightning strikes, rhythmic screen shakes,
 * and state-transition network synchronization.
 */
public final class ServerPressureManager {
    public static final String TAG_ROOTED = "spiritual_pressure_rooted";
    public static final int EVALUATION_INTERVAL = 5;

    public static final UUID VENGEFUL_ARMOR_UUID = UUID.fromString("c0a80101-7001-4999-b111-000000000001");
    public static final UUID VENGEFUL_ATTACK_UUID = UUID.fromString("c0a80101-7001-4999-b111-000000000002");

    private static final Map<UUID, ServerPressureField> ACTIVE_FIELDS = new ConcurrentHashMap<>();
    private static final Map<UUID, Set<UUID>> FIELD_VIEWERS = new ConcurrentHashMap<>();
    private static final Map<ResourceKey<Level>, Map<UUID, PressureReaction>> DIMENSION_ENTITY_REACTIONS = new ConcurrentHashMap<>();
    private static final Map<ResourceKey<Level>, Set<UUID>> DIMENSION_VENGEFUL_ENTITIES = new ConcurrentHashMap<>();
    private static final Set<UUID> ROOTED_ENTITIES = ConcurrentHashMap.newKeySet();

    public static void clearAll() {
        ACTIVE_FIELDS.clear();
        FIELD_VIEWERS.clear();
        DIMENSION_ENTITY_REACTIONS.clear();
        DIMENSION_VENGEFUL_ENTITIES.clear();
        ROOTED_ENTITIES.clear();
    }

    private ServerPressureManager() {
    }

    public static boolean isRooted(LivingEntity entity) {
        return ROOTED_ENTITIES.contains(entity.getUUID()) || entity.getTags().contains(TAG_ROOTED);
    }

    public static void releaseEntity(ServerLevel level, UUID targetId) {
        if (targetId == null) {
            return;
        }
        ROOTED_ENTITIES.remove(targetId);
        if (level != null) {
            ResourceKey<Level> dimKey = level.dimension();
            Map<UUID, PressureReaction> dimReactions = DIMENSION_ENTITY_REACTIONS.get(dimKey);
            PressureReaction oldReaction = (dimReactions != null) ? dimReactions.remove(targetId) : null;

            Set<UUID> vengeful = DIMENSION_VENGEFUL_ENTITIES.get(dimKey);
            if (vengeful != null) {
                vengeful.remove(targetId);
            }

            Entity entity = level.getEntity(targetId);
            if (entity instanceof LivingEntity living) {
                living.removeTag(TAG_ROOTED);
                removeVengefulModifiers(living);
                PressureNetwork.broadcastReaction(living, PressureReaction.NONE);
                PressureReactionDispatcher.dispatch(living, oldReaction != null ? oldReaction : PressureReaction.NONE, PressureReaction.NONE);
            }
        }
    }

    public static UUID startField(ServerLevel level, PressureFieldData data) {
        ServerPressureField field = new ServerPressureField(data, level);
        ACTIVE_FIELDS.put(data.fieldId(), field);
        Set<UUID> viewers = Collections.newSetFromMap(new ConcurrentHashMap<>());
        Vec3 center = field.getCurrentCenter();
        double radius = data.radius();
        double viewDistSq = (radius + 96.0D) * (radius + 96.0D);
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(center) <= viewDistSq) {
                viewers.add(player.getUUID());
            }
        }
        FIELD_VIEWERS.put(data.fieldId(), viewers);
        PressureNetwork.broadcastStartField(field);
        if (level.getPlayerByUUID(data.ownerUuid()) instanceof ServerPlayer serverPlayer) {
            if (viewers.add(serverPlayer.getUUID())) {
                PressureNetwork.sendStartFieldToPlayer(serverPlayer, data);
            }
        }

        // Initial screen shake burst
        Entity owner = field.getOwnerEntity();
        if (owner instanceof LivingEntity livingOwner) {
            PressureScreenShakeHelper.applyScreenShake(level, livingOwner, data.radius(), 0.02F, 30);
        }

        return data.fieldId();
    }

    public static void stopField(UUID fieldId) {
        ServerPressureField field = ACTIVE_FIELDS.remove(fieldId);
        Set<UUID> viewers = FIELD_VIEWERS.remove(fieldId);
        if (field != null) {
            field.markExpired();
            if (viewers != null) {
                for (UUID viewerId : viewers) {
                    ServerPlayer viewer = field.getLevel().getServer().getPlayerList().getPlayer(viewerId);
                    if (viewer != null) {
                        PressureNetwork.sendEndFieldToPlayer(viewer, fieldId);
                    }
                }
            }
            PressureNetwork.broadcastEndField(field.getLevel(), fieldId, field.getCurrentCenter(), field.getData().radius());
            boolean hasRemainingInDimension = false;
            for (ServerPressureField f : ACTIVE_FIELDS.values()) {
                if (f.getLevel() == field.getLevel()) {
                    hasRemainingInDimension = true;
                    break;
                }
            }
            if (!hasRemainingInDimension) {
                cleanupLevelReactions(field.getLevel());
            }
        }
    }

    public static void stopByOwnerAndSpell(UUID ownerUuid, String sourceSpellId) {
        List<UUID> toStop = new ArrayList<>();
        for (ServerPressureField field : ACTIVE_FIELDS.values()) {
            if (field.getOwnerUuid().equals(ownerUuid) && spellIdMatches(field.getSourceSpellId(), sourceSpellId)) {
                toStop.add(field.getFieldId());
            }
        }
        for (UUID id : toStop) {
            stopField(id);
        }
    }

    public static void stopAllByOwner(UUID ownerUuid) {
        List<UUID> toStop = new ArrayList<>();
        for (ServerPressureField field : ACTIVE_FIELDS.values()) {
            if (field.getOwnerUuid().equals(ownerUuid)) {
                toStop.add(field.getFieldId());
            }
        }
        for (UUID id : toStop) {
            stopField(id);
        }
    }

    public static boolean hasActiveField(UUID ownerUuid) {
        for (ServerPressureField field : ACTIVE_FIELDS.values()) {
            if (field.getOwnerUuid().equals(ownerUuid) && !field.isExpired()) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasActiveField(UUID ownerUuid, String sourceSpellId) {
        for (ServerPressureField field : ACTIVE_FIELDS.values()) {
            if (field.getOwnerUuid().equals(ownerUuid) && spellIdMatches(field.getSourceSpellId(), sourceSpellId) && !field.isExpired()) {
                return true;
            }
        }
        return false;
    }

    public static void syncToPlayer(ServerPlayer player) {
        for (ServerPressureField field : ACTIVE_FIELDS.values()) {
            if (field.getLevel().dimension().equals(player.level().dimension()) && !field.isExpired()) {
                double viewDistSq = (field.getData().radius() + 96.0D) * (field.getData().radius() + 96.0D);
                if (player.distanceToSqr(field.getCurrentCenter()) <= viewDistSq) {
                    Set<UUID> viewers = FIELD_VIEWERS.computeIfAbsent(field.getFieldId(), k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
                    if (viewers.add(player.getUUID())) {
                        PressureNetwork.sendStartFieldToPlayer(player, field.getData());
                    }
                }
            }
        }
    }

    public static void onPlayerLoggedOut(ServerPlayer player) {
        stopAllByOwner(player.getUUID());
        for (Set<UUID> viewers : FIELD_VIEWERS.values()) {
            viewers.remove(player.getUUID());
        }
    }

    public static void tickLevel(ServerLevel level) {
        List<UUID> toRemove = new ArrayList<>();
        for (ServerPressureField field : ACTIVE_FIELDS.values()) {
            if (field.getLevel() != level) {
                continue;
            }

            field.incrementTick();
            if (field.isExpired()) {
                toRemove.add(field.getFieldId());
            }
        }

        for (UUID id : toRemove) {
            stopField(id);
        }

        boolean hasFieldsInDimension = false;
        for (ServerPressureField field : ACTIVE_FIELDS.values()) {
            if (field.getLevel() == level) {
                hasFieldsInDimension = true;
                break;
            }
        }

        if (!hasFieldsInDimension) {
            cleanupLevelReactions(level);
            return;
        }

        long gameTime = level.getGameTime();
        if (gameTime % EVALUATION_INTERVAL == 0) {
            evaluateSpatialFields(level, gameTime);
        }
    }

    private static void evaluateSpatialFields(ServerLevel level, long gameTime) {
        ResourceKey<Level> dimKey = level.dimension();
        Map<UUID, PressureReaction> dimReactions = DIMENSION_ENTITY_REACTIONS.computeIfAbsent(dimKey, k -> new ConcurrentHashMap<>());
        Set<UUID> vengefulEntities = DIMENSION_VENGEFUL_ENTITIES.computeIfAbsent(dimKey, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));

        Map<UUID, Float> strongestIntensityMap = new HashMap<>();
        Map<UUID, LivingEntity> affectedEntities = new HashMap<>();
        Map<UUID, String> strongestSpellMap = new HashMap<>();
        Set<UUID> currentVengefulTargets = new HashSet<>();

        for (ServerPressureField field : ACTIVE_FIELDS.values()) {
            if (field.getLevel() != level || field.isExpired()) {
                continue;
            }

            Vec3 center = field.getCurrentCenter();
            float radius = field.getData().radius();
            double radiusSq = radius * radius;
            AABB bounds = new AABB(
                    center.x - radius, center.y - radius, center.z - radius,
                    center.x + radius, center.y + radius, center.z + radius
            );

            // Viewer synchronization for entering and exiting players
            Set<UUID> viewers = FIELD_VIEWERS.computeIfAbsent(field.getFieldId(), k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
            Set<UUID> visibleNow = new HashSet<>();
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(center) <= (radius + 96.0D) * (radius + 96.0D)) {
                    visibleNow.add(player.getUUID());
                    if (viewers.add(player.getUUID())) {
                        PressureNetwork.sendStartFieldToPlayer(player, field.getData());
                    }
                }
            }
            Iterator<UUID> vIterSync = viewers.iterator();
            while (vIterSync.hasNext()) {
                UUID vId = vIterSync.next();
                if (!visibleNow.contains(vId)) {
                    ServerPlayer viewer = level.getServer().getPlayerList().getPlayer(vId);
                    if (viewer != null) {
                        PressureNetwork.sendEndFieldToPlayer(viewer, field.getFieldId());
                    }
                    vIterSync.remove();
                }
            }

            Entity owner = field.getOwnerEntity();
            if (owner instanceof LivingEntity livingOwner && field.canTriggerScreenShake(gameTime)) {
                PressureScreenShakeHelper.applyScreenShake(level, livingOwner, radius, 0.012F, 25);
            }

            List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, bounds, entity -> {
                if (!entity.isAlive() || entity.isSpectator()) {
                    return false;
                }
                if (entity.getUUID().equals(field.getOwnerUuid())) {
                    return false;
                }
                if (owner instanceof LivingEntity livingOwner) {
                    if (livingOwner.isAlliedTo(entity) || DamageSources.isFriendlyFireBetween(livingOwner, entity)) {
                        return false;
                    }
                }
                return true;
            });

            boolean isTempest = field.getSourceSpellId().endsWith("tempest_reiatsu");
            boolean isVengeful = field.getSourceSpellId().endsWith("vengeful_pressure");

            List<LivingEntity> validTargetsInsideRadius = new ArrayList<>();

            for (LivingEntity target : nearby) {
                double distSq = target.distanceToSqr(center);
                if (distSq > radiusSq) {
                    continue;
                }

                validTargetsInsideRadius.add(target);

                double dist = Math.sqrt(distSq);
                float normalizedDist = (float) (dist / radius);
                float distanceFactor = 1.0F - normalizedDist * 0.65F;
                float fieldIntensity = field.getData().intensity() * distanceFactor;

                UUID targetId = target.getUUID();
                float currentIntensity = strongestIntensityMap.getOrDefault(targetId, 0.0F);
                if (fieldIntensity > currentIntensity) {
                    strongestIntensityMap.put(targetId, fieldIntensity);
                    affectedEntities.put(targetId, target);
                    strongestSpellMap.put(targetId, field.getSourceSpellId());
                }
            }

            // Periodic Tempest lightning strikes
            if (isTempest && field.canTriggerStrike(gameTime) && !validTargetsInsideRadius.isEmpty()) {
                LivingEntity strikeTarget = validTargetsInsideRadius.get(level.random.nextInt(validTargetsInsideRadius.size()));
                float strikeDmg = SpellConfig.TempestReiatsu.getBaseDamage() + (field.getData().spellLevel() - 1) * SpellConfig.TempestReiatsu.getDamagePerLevel();
                DamageSources.applyDamage(strikeTarget, strikeDmg, level.damageSources().lightningBolt());
                strikeTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 35, 2, false, true, true));

                // Strike stun reaction
                PressureNetwork.broadcastReaction(strikeTarget, PressureReaction.KNEEL);
                PressureReactionDispatcher.dispatch(strikeTarget, dimReactions.getOrDefault(strikeTarget.getUUID(), PressureReaction.NONE), PressureReaction.KNEEL);

                PressureNetwork.broadcastLightningStrike(level, strikeTarget.position(), 0xFF1493, radius);
                level.playSound(null, strikeTarget.getX(), strikeTarget.getY(), strikeTarget.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 1.0F, 1.2F);

                int minInterval = SpellConfig.TempestReiatsu.getStrikeIntervalMin();
                int maxInterval = SpellConfig.TempestReiatsu.getStrikeIntervalMax();
                field.scheduleNextStrike(gameTime, minInterval + level.random.nextInt(Math.max(1, maxInterval - minInterval + 1)));
            }
        }

        // Apply gameplay effects & detect state transitions
        for (Map.Entry<UUID, Float> entry : strongestIntensityMap.entrySet()) {
            UUID targetId = entry.getKey();
            float intensity = entry.getValue();
            LivingEntity target = affectedEntities.get(targetId);
            if (target == null) {
                continue;
            }

            PressureReaction newReaction = PressureReaction.fromIntensity(intensity);
            PressureReaction oldReaction = dimReactions.getOrDefault(targetId, PressureReaction.NONE);

            if (newReaction != oldReaction) {
                dimReactions.put(targetId, newReaction);
                PressureNetwork.broadcastReaction(target, newReaction);
                PressureReactionDispatcher.dispatch(target, oldReaction, newReaction);
            }

            int amplifier = switch (newReaction) {
                case STAGGER, CROUCH -> 0;
                case KNEEL -> 1;
                case KNOCKDOWN -> 2;
                case NONE -> 0;
            };

            target.addEffect(new MobEffectInstance(
                    MobEffectsRegistry.SPIRITUAL_PRESSURE.get(),
                    15,
                    amplifier,
                    false,
                    true,
                    true
            ));

            if (newReaction == PressureReaction.KNEEL || newReaction == PressureReaction.KNOCKDOWN) {
                int slownessAmp = (newReaction == PressureReaction.KNOCKDOWN) ? 4 : 2;
                target.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        15,
                        slownessAmp,
                        false,
                        false,
                        false
                ));
                ROOTED_ENTITIES.add(targetId);
                target.addTag(TAG_ROOTED);
            } else {
                ROOTED_ENTITIES.remove(targetId);
                target.removeTag(TAG_ROOTED);
            }

            // Vengeful Pressure specific debuffs (only applied if the dominant field is Vengeful)
            String winningSpellId = strongestSpellMap.getOrDefault(targetId, "");
            if (winningSpellId.endsWith("vengeful_pressure")) {
                currentVengefulTargets.add(targetId);
                vengefulEntities.add(targetId);
                target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 30, 0, false, false, false));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 30, 1, false, false, false));

                applyOrUpdateVengefulModifiers(target, newReaction);
            }
        }

        // Clear restrictions for entities that exited Vengeful domains
        Iterator<UUID> vIter = vengefulEntities.iterator();
        while (vIter.hasNext()) {
            UUID targetId = vIter.next();
            if (!currentVengefulTargets.contains(targetId)) {
                Entity entity = level.getEntity(targetId);
                if (entity instanceof LivingEntity living) {
                    removeVengefulModifiers(living);
                }
                vIter.remove();
            }
        }

        // Clear restrictions for entities that exited all fields in this dimension
        Iterator<Map.Entry<UUID, PressureReaction>> iter = dimReactions.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<UUID, PressureReaction> entry = iter.next();
            UUID targetId = entry.getKey();
            if (!strongestIntensityMap.containsKey(targetId)) {
                ROOTED_ENTITIES.remove(targetId);
                Entity entity = level.getEntity(targetId);
                if (entity instanceof LivingEntity living) {
                    living.removeTag(TAG_ROOTED);
                    removeVengefulModifiers(living);
                    PressureNetwork.broadcastReaction(living, PressureReaction.NONE);
                    PressureReactionDispatcher.dispatch(living, entry.getValue(), PressureReaction.NONE);
                }
                iter.remove();
            }
        }
    }

    private static void applyOrUpdateVengefulModifiers(LivingEntity target, PressureReaction reaction) {
        var armorAttr = target.getAttribute(Attributes.ARMOR);
        if (armorAttr != null) {
            boolean shouldHaveArmorShred = (reaction == PressureReaction.KNEEL || reaction == PressureReaction.KNOCKDOWN);
            boolean hasModifier = armorAttr.getModifier(VENGEFUL_ARMOR_UUID) != null;
            if (shouldHaveArmorShred && !hasModifier) {
                armorAttr.addTransientModifier(new AttributeModifier(
                        VENGEFUL_ARMOR_UUID,
                        "Vengeful Armor Shred",
                        -4.0D,
                        AttributeModifier.Operation.ADDITION
                ));
            } else if (!shouldHaveArmorShred && hasModifier) {
                armorAttr.removeModifier(VENGEFUL_ARMOR_UUID);
            }
        }

        var attackAttr = target.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttr != null) {
            boolean shouldHaveAttackDrain = (reaction == PressureReaction.KNOCKDOWN);
            boolean hasModifier = attackAttr.getModifier(VENGEFUL_ATTACK_UUID) != null;
            if (shouldHaveAttackDrain && !hasModifier) {
                attackAttr.addTransientModifier(new AttributeModifier(
                        VENGEFUL_ATTACK_UUID,
                        "Vengeful Attack Drain",
                        -3.0D,
                        AttributeModifier.Operation.ADDITION
                ));
            } else if (!shouldHaveAttackDrain && hasModifier) {
                attackAttr.removeModifier(VENGEFUL_ATTACK_UUID);
            }
        }
    }

    private static void removeVengefulModifiers(LivingEntity living) {
        var armorAttr = living.getAttribute(Attributes.ARMOR);
        if (armorAttr != null && armorAttr.getModifier(VENGEFUL_ARMOR_UUID) != null) {
            armorAttr.removeModifier(VENGEFUL_ARMOR_UUID);
        }

        var attackAttr = living.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttr != null && attackAttr.getModifier(VENGEFUL_ATTACK_UUID) != null) {
            attackAttr.removeModifier(VENGEFUL_ATTACK_UUID);
        }
    }

    private static void cleanupLevelReactions(ServerLevel level) {
        ResourceKey<Level> dimKey = level.dimension();
        Map<UUID, PressureReaction> dimReactions = DIMENSION_ENTITY_REACTIONS.remove(dimKey);
        if (dimReactions != null) {
            for (UUID targetId : dimReactions.keySet()) {
                ROOTED_ENTITIES.remove(targetId);
                Entity entity = level.getEntity(targetId);
                if (entity instanceof LivingEntity living) {
                    living.removeTag(TAG_ROOTED);
                    removeVengefulModifiers(living);
                    PressureNetwork.broadcastReaction(living, PressureReaction.NONE);
                    PressureReactionDispatcher.dispatch(living, dimReactions.getOrDefault(targetId, PressureReaction.NONE), PressureReaction.NONE);
                }
            }
            dimReactions.clear();
        }

        Set<UUID> vEntities = DIMENSION_VENGEFUL_ENTITIES.remove(dimKey);
        if (vEntities != null) {
            for (UUID targetId : vEntities) {
                Entity entity = level.getEntity(targetId);
                if (entity instanceof LivingEntity living) {
                    removeVengefulModifiers(living);
                }
            }
            vEntities.clear();
        }
    }

    public static boolean spellIdMatches(String a, String b) {
        if (a == null || b == null) return false;
        if (a.equals(b)) return true;
        String pureA = a.contains(":") ? a.substring(a.indexOf(':') + 1) : a;
        String pureB = b.contains(":") ? b.substring(b.indexOf(':') + 1) : b;
        return pureA.equals(pureB);
    }
}