package net.offkung.bhspells.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.entity.spells.six_petal_waltz.PetalWaltzSword;
import net.offkung.bhspells.network.PacketHandler;
import net.offkung.bhspells.network.client.SwordDashPacket;

@Mod.EventBusSubscriber(modid = BHSpells.MODID, value = Dist.CLIENT)
public class SwordDashClientHandler {
    private static final double MAX_RANGE = 200.0;
    private static final double RAY_TOLERANCE_SQR = 2.25;

    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        tryTriggerDash(event.getEntity());
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        tryTriggerDash(event.getEntity());
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        tryTriggerDash(event.getEntity());
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        tryTriggerDash(event.getEntity());
    }

    private static void tryTriggerDash(Player player) {
        if (!player.level().isClientSide) return;

        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(MAX_RANGE));
        AABB searchArea = new AABB(start, end).inflate(3.0);

        PetalWaltzSword closest = null;
        double minDistanceSqr = Double.MAX_VALUE;

        for (PetalWaltzSword sword : player.level().getEntitiesOfClass(PetalWaltzSword.class, searchArea)) {
            if (!sword.isOwnedBy(player) || sword.isActivated() || sword.isReturning()) {
                continue;
            }

            Vec3 toSword = sword.position().subtract(start);
            double projectionLength = toSword.dot(look);

            if (projectionLength <= 0 || projectionLength > MAX_RANGE) {
                continue;
            }

            Vec3 projection = look.scale(projectionLength);
            double distSqr = toSword.subtract(projection).lengthSqr();

            if (distSqr <= RAY_TOLERANCE_SQR && distSqr < minDistanceSqr) {
                minDistanceSqr = distSqr;
                closest = sword;
            }
        }

        if (closest != null) {
            PacketHandler.INSTANCE.sendToServer(new SwordDashPacket(closest.getId()));
        }
    }

    public static boolean isSwordGlowing(PetalWaltzSword sword, int ownerId) {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && mc.player.getId() == ownerId;
    }
}
