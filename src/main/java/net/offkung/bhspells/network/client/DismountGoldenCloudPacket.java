package net.offkung.bhspells.network.client;

import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.offkung.bhspells.entity.spells.golden_cloud.GoldenCloudEntity;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import org.joml.Vector3f;

import java.util.function.Supplier;

public class DismountGoldenCloudPacket {
    public DismountGoldenCloudPacket() {
    }

    public DismountGoldenCloudPacket(FriendlyByteBuf buf) {
    }

    public static void encode(DismountGoldenCloudPacket msg, FriendlyByteBuf buf) {
    }

    public static void handle(DismountGoldenCloudPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            if (player.getVehicle() instanceof GoldenCloudEntity cloud) {
                Level level = player.level();
                Vec3 cloudPos = cloud.position();

                // 1. Get off from the golden cloud entity
                player.stopRiding();

                // 2. Apply cloud bless effect for 3 seconds (60 ticks)

                player.addEffect(new MobEffectInstance(MobEffectRegistry.FALL_DAMAGE_IMMUNITY.get(), 60, 0, false, false, true));

                // 3. Discard the golden cloud immediately
                cloud.discard();

                MagicManager.spawnParticles(level, ParticleTypes.CAMPFIRE_COSY_SMOKE, cloudPos.x, cloudPos.y + 0.3, cloudPos.z, 150, 0.6, 0.3, 0.6, 0.1, false);
                level.playSound(null, cloudPos.x, cloudPos.y, cloudPos.z, SoundEvents.BAT_TAKEOFF, SoundSource.NEUTRAL, 1.0f, 0.8f);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
