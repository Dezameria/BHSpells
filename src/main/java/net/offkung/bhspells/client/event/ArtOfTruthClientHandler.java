package net.offkung.bhspells.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.offkung.bhspells.BHSpells;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = BHSpells.MODID, value = Dist.CLIENT)
public class ArtOfTruthClientHandler {
    public static final int GLOW_COLOR = 0x6E54AB; // Purple (0.43, 0.33, 0.67)

    private static final Set<Integer> glowingTargetIds = new HashSet<>();

    public static boolean isTargetGlowingForCaster(Entity entity) {
        if (entity == null) return false;
        if (glowingTargetIds.isEmpty()) return false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        return glowingTargetIds.contains(entity.getId());
    }

    public static void setTargetGlowing(int entityId, boolean glowing) {
        if (glowing) {
            glowingTargetIds.add(entityId);
        } else {
            glowingTargetIds.remove(entityId);
        }
    }

    public static void clear() {
        glowingTargetIds.clear();
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }
}
