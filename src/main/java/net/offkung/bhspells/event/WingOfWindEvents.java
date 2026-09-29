package net.offkung.bhspells.event;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.offkung.bhspells.registry.MobEffectsRegistry;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.offkung.bhspells.entity.spells.feather_strike.FeatherStrike;
import net.offkung.bhspells.spells.nature.WingOfWindSpell;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class WingOfWindEvents {
    private static final String SPELL_ID = "bhspells:wing_of_wind";
    private static final String WAS_IMMOBILIZED_TAG = "WingOfWindWasImmobilized";

    public static class BarrageState {
        public int feathersLeft = WingOfWindSpell.FEATHERS_PER_UNLEASH;
        public int ticksUntilNext = 0;
        public int featherIndex = 0;
        public float damage;
        public int spellLevel;

        public BarrageState(float damage, int spellLevel) {
            this.damage = damage;
            this.spellLevel = spellLevel;
        }
    }

    private static final Map<UUID, BarrageState> ACTIVE_BARRAGES = new HashMap<>();

    public static void startBarrage(LivingEntity caster, float damage, int spellLevel) {
        BarrageState state = new BarrageState(damage, spellLevel);
        ACTIVE_BARRAGES.put(caster.getUUID(), state);
        // Fire first feather immediately on cast
        fireNextFeather(caster, state);
        state.ticksUntilNext = 2;
    }

    private static void tickBarrage(Player player) {
        BarrageState state = ACTIVE_BARRAGES.get(player.getUUID());
        if (state == null) {
            return;
        }

        if (player.isDeadOrDying() || player.isRemoved() || !player.hasEffect(MobEffectsRegistry.WING_OF_WIND_IMMOBILIZE.get())) {
            ACTIVE_BARRAGES.remove(player.getUUID());
            return;
        }

        state.ticksUntilNext--;
        if (state.ticksUntilNext <= 0) {
            fireNextFeather(player, state);
            state.ticksUntilNext = 2;
            if (state.feathersLeft <= 0) {
                ACTIVE_BARRAGES.remove(player.getUUID());
            }
        }
    }

    private static void fireNextFeather(LivingEntity caster, BarrageState state) {
        Level level = caster.level();
        if (level.isClientSide || !caster.isAlive()) {
            return;
        }

        Vec3 eyePos = caster.getEyePosition();
        Vec3 look = caster.getLookAngle().normalize();
        Vec3 right = look.cross(new Vec3(0, 1, 0)).normalize();
        if (right.lengthSqr() < 1.0E-4) {
            right = new Vec3(1, 0, 0);
        }
        Vec3 up = right.cross(look).normalize();

        int i = state.featherIndex;
        double side = (i % 2 == 0) ? 1.0 : -1.0;
        double dist = 0.5 + (i / 2) * 0.28;
        double heightOffset = (i / 2) * 0.12 - 0.15;
        Vec3 spawnPos = eyePos.add(right.scale(side * dist)).add(up.scale(heightOffset)).subtract(look.scale(0.25));

        // Dynamically find aim target based on where the caster is CURRENTLY aiming
        LivingEntity target = WingOfWindSpell.findAimTarget(level, caster, WingOfWindSpell.AIM_MAX_RANGE, WingOfWindSpell.AIM_MIN_DOT);

        FeatherStrike feather = new FeatherStrike(level, caster);
        feather.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        feather.setDamage(state.damage);
        feather.setDelayTicks(0);
        feather.setSeekAmount(0.6f);
        if (target != null) {
            feather.setTarget(target);
        }
        Vec3 targetPoint = (target != null) ? target.getBoundingBox().getCenter() : eyePos.add(look.scale(30.0));
        Vec3 toTarget = targetPoint.subtract(spawnPos);
        if (toTarget.lengthSqr() < 1.0E-4) {
            toTarget = look;
        }
        feather.shoot(toTarget.x, toTarget.y, toTarget.z, 1.6F, 4.0F);
        level.addFreshEntity(feather);

        level.playSound(null, spawnPos.x, spawnPos.y, spawnPos.z, SoundEvents.BAT_TAKEOFF, SoundSource.PLAYERS, 0.8F, 1.3F + level.getRandom().nextFloat() * 0.3F);

        state.featherIndex++;
        state.feathersLeft--;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;

        // 1. Immobilize handling: zero movement and freeze position while allowing camera rotation
        if (player.hasEffect(MobEffectsRegistry.WING_OF_WIND_IMMOBILIZE.get())) {
            player.getPersistentData().putBoolean(WAS_IMMOBILIZED_TAG, true);
            player.setDeltaMovement(0, 0, 0);
            player.hasImpulse = true;
            player.fallDistance = 0;

            if (!player.level().isClientSide) {
                tickBarrage(player);
            }
            return;
        }

        // Check if player just exited immobilization
        if (player.getPersistentData().getBoolean(WAS_IMMOBILIZED_TAG)) {
            player.getPersistentData().remove(WAS_IMMOBILIZED_TAG);
            ACTIVE_BARRAGES.remove(player.getUUID());
            if (player.hasEffect(MobEffectsRegistry.WING_OF_WIND_FLIGHT.get())) {
                Vec3 look = player.getLookAngle().normalize();
                Vec3 boost = new Vec3(look.x * 0.85, Math.max(look.y * 0.7 + 0.3, 0.3), look.z * 0.85);
                player.setDeltaMovement(boost);
                player.hasImpulse = true;
                player.setOnGround(false);
                player.startFallFlying();
                if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
                }
            }
        }

        // 2. Flight handling
        if (player.hasEffect(MobEffectsRegistry.WING_OF_WIND_FLIGHT.get())) {
            // Keep fall flying engaged while in the air
            if (!player.onGround() && !player.isFallFlying()) {
                player.setOnGround(false);
                player.startFallFlying();
            }

            // Maintain steady cruising flight speed
            if (player.isFallFlying()) {
                Vec3 look = player.getLookAngle().normalize();
                Vec3 motion = player.getDeltaMovement();
                double currentSpeed = motion.length();
                double targetSpeed = 1.0;
                Vec3 targetMotion = look.scale(targetSpeed);
                double blendFactor = (currentSpeed < targetSpeed) ? 0.12 : 0.08;
                player.setDeltaMovement(motion.add(targetMotion.subtract(motion).scale(blendFactor)));
                player.hasImpulse = true;
            }
        } else if (!player.level().isClientSide) {
            // If player lost WING_OF_WIND_FLIGHT, remove any active recasts for wing_of_wind
            MagicData magicData = MagicData.getPlayerMagicData(player);
            if (magicData.getPlayerRecasts().hasRecastForSpell(SPELL_ID)) {
                var recast = magicData.getPlayerRecasts().getRecastInstance(SPELL_ID);
                if (recast != null) {
                    magicData.getPlayerRecasts().removeRecast(recast, RecastResult.USED_ALL_RECASTS);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player && player.hasEffect(MobEffectsRegistry.WING_OF_WIND_FLIGHT.get())) {
            if (event.getSource().is(DamageTypeTags.IS_FALL) || event.getSource().is(DamageTypes.FLY_INTO_WALL)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity().hasEffect(MobEffectsRegistry.WING_OF_WIND_IMMOBILIZE.get())) {
            event.getEntity().setDeltaMovement(event.getEntity().getDeltaMovement().multiply(1, 0, 1));
        }
    }
}
