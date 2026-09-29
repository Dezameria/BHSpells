package net.offkung.bhspells.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.offkung.bhspells.entity.spells.crystal_hydro_dome.CrystalHydroDomeAoe;
import net.offkung.bhspells.entity.spells.crystal_hydro_dome.CrystalHydroDomeConstants;

@Mod.EventBusSubscriber
public class CrystalHydroDomeEvents {
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) {
            return;
        }
        if (victim instanceof ArmorStand) {
            return;
        }
        CrystalHydroDomeAoe dome = CrystalHydroDomeAoe.findDomeContaining(victim);
        if (dome == null) {
            return;
        }
        DamageSource source = event.getSource();
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }
        if (event.getAmount() <= 0) {
            return;
        }

        Entity attackerEntity = source.getEntity();
        if (!(attackerEntity instanceof LivingEntity attacker) || dome.isInside(attacker.position())) {
            return;
        }

        event.setCanceled(true);
        double gated = dome.gateOutsideHit(victim, source, event.getAmount());
        double absorbed = dome.absorb(gated);
        dome.applyCounter(attacker, absorbed);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim instanceof ArmorStand) {
            return;
        }
        CrystalHydroDomeAoe dome = CrystalHydroDomeAoe.findDomeContaining(victim);
        if (dome == null) {
            return;
        }
        DamageSource source = event.getSource();
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }
        float amount = event.getAmount();
        if (amount <= 0) {
            return;
        }

        Entity attackerEntity = source.getEntity();
        boolean attackerOutside = attackerEntity instanceof LivingEntity attacker && !dome.isInside(attacker.position());
        if (attackerOutside) {
            return;
        }

        double domeShare = amount * CrystalHydroDomeConstants.INSIDE_DOME_SHARE;
        dome.absorb(domeShare);
        event.setAmount((float) (amount * (1.0 - CrystalHydroDomeConstants.INSIDE_DOME_SHARE)));
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CrystalHydroDomeAoe.endActiveDomeFor(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        event.getEntity().removeTag(CrystalHydroDomeConstants.DOME_TAG);
    }
}
