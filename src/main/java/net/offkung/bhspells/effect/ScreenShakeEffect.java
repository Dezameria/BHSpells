package net.offkung.bhspells.effect;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.PacketDistributor;
import net.offkung.bhspells.network.PacketHandler;
import net.offkung.bhspells.network.server.ScreenShakePacket;
import org.jetbrains.annotations.NotNull;

public class ScreenShakeEffect extends MobEffect {
    public ScreenShakeEffect() {
        super(MobEffectCategory.HARMFUL, 16711680);
    }

    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        if (entity instanceof ServerPlayer player) {
            float power = 1.0F + (float)amplifier * 0.5F;
            PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ScreenShakePacket(power, player.position()));
        }
    }

    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 5 == 0;
    }
}
