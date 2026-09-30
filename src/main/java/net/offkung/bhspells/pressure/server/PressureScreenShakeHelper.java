package net.offkung.bhspells.pressure.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import net.offkung.bhspells.network.PacketHandler;
import net.offkung.bhspells.network.server.ScreenShakePacket;

/**
 * Native helper for invoking screen shake using BHSpells ScreenShakePacket.
 */
public final class PressureScreenShakeHelper {
    private PressureScreenShakeHelper() {
    }

    public static void applyScreenShake(Level level, LivingEntity caster, float radius, float intensity, int durationTicks) {
        if (level.isClientSide || caster == null || !caster.isAlive()) {
            return;
        }
        try {
            if (caster instanceof ServerPlayer player) {
                PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ScreenShakePacket(intensity, caster.position()));
            } else {
                PacketHandler.INSTANCE.send(
                        PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                                caster.getX(), caster.getY(), caster.getZ(),
                                radius, level.dimension()
                        )),
                        new ScreenShakePacket(intensity, caster.position())
                );
            }
        } catch (Throwable ignored) {
        }
    }
}
