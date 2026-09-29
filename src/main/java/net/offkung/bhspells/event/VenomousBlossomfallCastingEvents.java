package net.offkung.bhspells.event;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.offkung.bhspells.spells.nature.VenomousBlossomfallSpell;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class VenomousBlossomfallCastingEvents {
    private static final Map<UUID, MovementAnchor> MOVEMENT_ANCHORS = new HashMap<>();

    private VenomousBlossomfallCastingEvents() {
    }

    public static void beginMovementLock(ServerPlayer player) {
        MOVEMENT_ANCHORS.put(player.getUUID(), MovementAnchor.at(player));
        enforceMovementLock(player);
    }

    public static void enforceMovementLock(ServerPlayer player) {
        MovementAnchor anchor = MOVEMENT_ANCHORS.computeIfAbsent(player.getUUID(), ignored -> MovementAnchor.at(player));
        if (!anchor.dimension.equals(player.level().dimension())) {
            MOVEMENT_ANCHORS.remove(player.getUUID());
            return;
        }

        long currentTick = player.level().getGameTime();
        if (anchor.lastEnforceTick == currentTick) {
            return;
        }
        anchor.lastEnforceTick = currentTick;

        double currentX = player.getX();
        double currentY = player.getY();
        double currentZ = player.getZ();

        double allowedY = Math.min(currentY, anchor.maximumY);
        anchor.maximumY = allowedY;

        double horizontalDisplacementSqr = (currentX - anchor.x) * (currentX - anchor.x) + (currentZ - anchor.z) * (currentZ - anchor.z);

        if (horizontalDisplacementSqr > 1.0E-6D || currentY > anchor.maximumY + 1.0E-4D) {
            player.setPos(anchor.x, allowedY, anchor.z);

            if (horizontalDisplacementSqr >= 0.0625D || currentY > anchor.maximumY + 0.1D) {
                player.connection.teleport(anchor.x, allowedY, anchor.z, player.getYRot(), player.getXRot());
            }
        }

        VenomousBlossomfallSpell.lockCasterMovement(player);
    }

    public static void releaseMovementLock(ServerPlayer player) {
        MOVEMENT_ANCHORS.remove(player.getUUID());
    }

    public static void updateChargeStage(ServerPlayer player, VenomousBlossomfallSpell.ChargeStage stage) {
        MovementAnchor anchor = MOVEMENT_ANCHORS.computeIfAbsent(player.getUUID(), ignored -> MovementAnchor.at(player));
        if (anchor.lastChargeStage == stage) {
            return;
        }

        anchor.lastChargeStage = stage;
        String translationKey = switch (stage) {
            case SHORT -> "ui.bhspells.venomous_blossomfall_charge_short";
            case MEDIUM -> "ui.bhspells.venomous_blossomfall_charge_medium";
            case FULL -> "ui.bhspells.venomous_blossomfall_charge_full";
        };
        player.displayClientMessage(Component.translatable(translationKey), true);

        if (stage == VenomousBlossomfallSpell.ChargeStage.MEDIUM) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.2F);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.5F, 1.4F);
        } else if (stage == VenomousBlossomfallSpell.ChargeStage.FULL) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.6F);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2F, 1.8F);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        MagicData magicData = MagicData.getPlayerMagicData(serverPlayer);
        if (magicData.isCasting() && "bhspells:venomous_blossomfall".equals(magicData.getCastingSpellId())) {
            enforceMovementLock(serverPlayer);
        } else {
            releaseMovementLock(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onUsingItemTick(LivingEntityUseItemEvent.Tick event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        MagicData magicData = MagicData.getPlayerMagicData(serverPlayer);
        if (magicData.isCasting() && "bhspells:venomous_blossomfall".equals(magicData.getCastingSpellId()) && VenomousBlossomfallSpell.isFullCharge(VenomousBlossomfallSpell.chargeProgress(magicData.getCastDuration(), magicData.getCastDurationRemaining())) && event.getDuration() <= VenomousBlossomfallSpell.FULL_CHARGE_HOLD_REFRESH_THRESHOLD) {
            event.setDuration(VenomousBlossomfallSpell.FULL_CHARGE_HOLD_USE_DURATION);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            releaseMovementLock(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            releaseMovementLock(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            releaseMovementLock(serverPlayer);
        }
    }

    private static final class MovementAnchor {
        private final ResourceKey<Level> dimension;
        private final double x;
        private final double z;
        private double maximumY;
        private VenomousBlossomfallSpell.ChargeStage lastChargeStage;
        private long lastEnforceTick = -1;

        private MovementAnchor(ResourceKey<Level> dimension, double x, double maximumY, double z) {
            this.dimension = dimension;
            this.x = x;
            this.maximumY = maximumY;
            this.z = z;
        }

        private static MovementAnchor at(ServerPlayer player) {
            return new MovementAnchor(player.level().dimension(), player.getX(), player.getY(), player.getZ());
        }
    }
}
