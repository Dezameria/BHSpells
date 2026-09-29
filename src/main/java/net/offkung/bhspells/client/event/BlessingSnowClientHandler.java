package net.offkung.bhspells.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.entity.spells.blessing_snow.RadiusSnowRingEntity;
import net.offkung.bhspells.network.PacketHandler;
import net.offkung.bhspells.network.client.BlessingSnowTargetSelectPacket;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(modid = BHSpells.MODID, value = Dist.CLIENT)
public class BlessingSnowClientHandler {
    private static long lastTargetSelectTime = 0;

    // Entity IDs of targets selected for healing by the caster
    private static final Set<Integer> glowingTargetIds = new HashSet<>();

    public static boolean isTargetGlowingForCaster(Entity entity) {
        if (entity == null) return false;
        if (glowingTargetIds.isEmpty()) return false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;

        List<RadiusSnowRingEntity> rings = mc.player.level().getEntitiesOfClass(
                RadiusSnowRingEntity.class,
                mc.player.getBoundingBox().inflate(35.0),
                ring -> ring.getOwner() == mc.player && !ring.isRemoved()
        );
        if (rings.isEmpty()) {
            glowingTargetIds.clear();
            return false;
        }

        RadiusSnowRingEntity ring = rings.get(0);
        if (ring.distanceTo(entity) > ring.getRadius()) {
            return false;
        }

        return glowingTargetIds.contains(entity.getId());
    }

    public static boolean hasActiveRing(Player player) {
        if (player == null) return false;
        List<RadiusSnowRingEntity> rings = player.level().getEntitiesOfClass(RadiusSnowRingEntity.class, player.getBoundingBox().inflate(35.0), ring -> ring.getOwner() == player && !ring.isRemoved());
        return !rings.isEmpty();
    }

    public static void setTargetGlowing(int entityId, boolean glowing) {
        if (glowing) {
            glowingTargetIds.add(entityId);
        } else {
            glowingTargetIds.remove(entityId);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Clean up glowing targets if no active ring is present
        if (!glowingTargetIds.isEmpty() && !hasActiveRing(mc.player)) {
            glowingTargetIds.clear();
        }
    }

    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        trySelectTarget(event.getEntity(), null);
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (trySelectTarget(event.getEntity(), null)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (trySelectTarget(event.getEntity(), null)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() == InteractionHand.MAIN_HAND) {
            if (trySelectTarget(event.getEntity(), event.getTarget() instanceof LivingEntity living ? living : null)) {
                event.setCanceled(true);
            }
        }
    }

    private static boolean trySelectTarget(Player player, LivingEntity directlyClicked) {
        if (!player.level().isClientSide) return false;
        if (!player.isShiftKeyDown()) return false;

        long now = System.currentTimeMillis();
        if (now - lastTargetSelectTime < 250) return false;

        // Check if player has an active RadiusSnowRingEntity
        List<RadiusSnowRingEntity> rings = player.level().getEntitiesOfClass(
                RadiusSnowRingEntity.class,
                player.getBoundingBox().inflate(35.0),
                ring -> ring.getOwner() == player && !ring.isRemoved()
        );

        if (rings.isEmpty()) return false;

        RadiusSnowRingEntity ring = rings.get(0);
        float maxRange = ring.getRadius();

        LivingEntity target = directlyClicked;
        if (target != null && (target == player || !target.isAlive() || player.distanceTo(target) > maxRange)) {
            target = null;
        }

        if (target == null && player instanceof LocalPlayer localPlayer) {
            target = raycastLivingEntity(localPlayer, maxRange);
        }

        if (target != null && target != player && target.isAlive()) {
            lastTargetSelectTime = now;
            if (glowingTargetIds.contains(target.getId())) {
                glowingTargetIds.remove(target.getId());
            } else {
                glowingTargetIds.add(target.getId());
            }
            PacketHandler.INSTANCE.sendToServer(new BlessingSnowTargetSelectPacket(target.getId()));
            return true;
        }

        return false;
    }

    private static LivingEntity raycastLivingEntity(LocalPlayer player, double maxRange) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(maxRange));
        AABB searchArea = new AABB(start, end).inflate(2.0);

        LivingEntity closest = null;
        double minDistSqr = Double.MAX_VALUE;

        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, searchArea)) {
            if (entity == player || !entity.isAlive() || entity.isSpectator()) continue;

            Vec3 toEntity = entity.getEyePosition().subtract(start);
            double projection = toEntity.dot(look);
            if (projection <= 0 || projection > maxRange) continue;

            Vec3 projVec = look.scale(projection);
            double distSqr = toEntity.subtract(projVec).lengthSqr();

            double tolerance = Math.max(entity.getBbWidth(), 1.0);
            if (distSqr <= tolerance * tolerance && distSqr < minDistSqr) {
                minDistSqr = distSqr;
                closest = entity;
            }
        }

        return closest;
    }
}
