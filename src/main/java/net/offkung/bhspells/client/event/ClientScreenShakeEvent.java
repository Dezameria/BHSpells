package net.offkung.bhspells.client.event;

import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Random;

public class ClientScreenShakeEvent {
    private static final Random random = new Random();
    private static float shakeIntensity = 0.0F;

    public static void shake(float power) {
        shakeIntensity = Math.min(shakeIntensity + power * 0.1F, 2.0F);
    }

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        if (shakeIntensity > 0.0F) {
            float noiseX = (random.nextFloat() - 0.5F) * shakeIntensity * 1.2F;
            float noiseY = (random.nextFloat() - 0.5F) * shakeIntensity * 1.2F;
            event.setPitch(event.getPitch() + noiseX);
            event.setYaw(event.getYaw() + noiseY);
            event.setRoll(event.getRoll() + (random.nextFloat() - 0.5F) * shakeIntensity * 0.8F);
        }

    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && shakeIntensity > 0.0F) {
            shakeIntensity *= 0.92F;
            if (shakeIntensity < 0.01F) {
                shakeIntensity = 0.0F;
            }
        }

    }
}
