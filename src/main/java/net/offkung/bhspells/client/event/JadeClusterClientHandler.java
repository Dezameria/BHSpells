package net.offkung.bhspells.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.entity.spells.jade_cluster.JadeClusterEntity;
import net.offkung.bhspells.network.PacketHandler;
import net.offkung.bhspells.network.client.ExplodeJadeClusterPacket;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = BHSpells.MODID, value = Dist.CLIENT)
public class JadeClusterClientHandler {
    private static final double MAX_RANGE = 50.0;
    private static final double RAY_TOLERANCE_SQR = 2.25;

    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        tryDetonateJade(event.getEntity());
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (tryDetonateJade(event.getEntity())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (tryDetonateJade(event.getEntity())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (tryDetonateJade(event.getEntity())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    private static boolean tryDetonateJade(Player player) {
        if (player == null || !player.level().isClientSide) return false;

        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(MAX_RANGE));
        AABB searchArea = new AABB(start, end).inflate(3.0);

        JadeClusterEntity closest = null;
        double minDistanceSqr = Double.MAX_VALUE;

        for (JadeClusterEntity cluster : player.level().getEntitiesOfClass(JadeClusterEntity.class, searchArea)) {
            if (!cluster.isAlive() || !cluster.isOwnedBy(player)) {
                continue;
            }

            AABB box = cluster.getBoundingBox().inflate(0.5);
            Optional<Vec3> clip = box.clip(start, end);
            if (clip.isPresent()) {
                double distSqr = start.distanceToSqr(clip.get());
                if (distSqr < minDistanceSqr) {
                    minDistanceSqr = distSqr;
                    closest = cluster;
                }
            } else {
                Vec3 toCluster = cluster.position().add(0, cluster.getBbHeight() * 0.5, 0).subtract(start);
                double projectionLength = toCluster.dot(look);

                if (projectionLength <= 0 || projectionLength > MAX_RANGE) {
                    continue;
                }

                Vec3 projection = look.scale(projectionLength);
                double distSqr = toCluster.subtract(projection).lengthSqr();

                if (distSqr <= RAY_TOLERANCE_SQR && distSqr < minDistanceSqr) {
                    minDistanceSqr = distSqr;
                    closest = cluster;
                }
            }
        }

        if (closest != null) {
            PacketHandler.INSTANCE.sendToServer(new ExplodeJadeClusterPacket(closest.getId()));
            return true;
        }

        return false;
    }

    public static void handleAmbientParticles(JadeClusterEntity entity) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && entity.isOwnedBy(mc.player)) {
            entity.spawnAmbientParticles();
        }
    }
}
