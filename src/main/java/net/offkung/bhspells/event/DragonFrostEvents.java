package net.offkung.bhspells.event;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.offkung.bhspells.effect.DragonFrostHandler;
import net.offkung.bhspells.mixin.LivingEntityAccessor;
import net.offkung.bhspells.registry.MobEffectsRegistry;

@Mod.EventBusSubscriber
public class DragonFrostEvents {
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player && player.hasEffect(MobEffectsRegistry.DRAGON_FROST.get())) {
            if (event.getSource().is(DamageTypeTags.IS_FALL)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;
        DragonFrostHandler.onPlayerTick(player);

        if (!player.hasEffect(MobEffectsRegistry.DRAGON_FROST.get())) {
            return;
        }

        // Allow mid-air jumping to initiate air-walking if player is airborne (e.g. walked off ledge)
        if (player instanceof LivingEntityAccessor accessor) {
            boolean jumping = accessor.bhspells$isJumping();
            boolean wasJumping = player.getPersistentData().getBoolean(DragonFrostHandler.WAS_JUMPING_TAG);
            player.getPersistentData().putBoolean(DragonFrostHandler.WAS_JUMPING_TAG, jumping);

            if (jumping && !wasJumping && !player.onGround() && !player.isInWater() && !player.isInLava() && !player.getAbilities().flying) {
                if (!player.getPersistentData().getBoolean(DragonFrostHandler.JUMPED_TAG)) {
                    Vec3 motion = player.getDeltaMovement();
                    player.setDeltaMovement(motion.x, 0.42, motion.z);
                    DragonFrostHandler.onJump(player);
                }
            }
        }
    }
}
