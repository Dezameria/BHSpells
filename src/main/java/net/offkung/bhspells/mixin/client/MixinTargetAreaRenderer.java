package net.offkung.bhspells.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.ironsspellbooks.entity.spells.target_area.TargetAreaRenderer;
import io.redspace.ironsspellbooks.entity.spells.target_area.TargetedAreaEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.offkung.bhspells.entity.spells.jade_cluster.JadeClusterEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TargetAreaRenderer.class, remap = false)
public class MixinTargetAreaRenderer {
    @Inject(
            method = "render(Lio/redspace/ironsspellbooks/entity/spells/target_area/TargetedAreaEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void bhspells$cancelRenderForNonOwnerJadeCluster(
            TargetedAreaEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            CallbackInfo ci
    ) {
        Player localPlayer = Minecraft.getInstance().player;

        // If this TargetedAreaEntity is directly owned by a JadeClusterEntity
        if (entity.getOwner() instanceof JadeClusterEntity cluster) {
            if (localPlayer == null || !cluster.isOwnedBy(localPlayer)) {
                ci.cancel();
            }
            return;
        }

        // If the color matches JadeClusterEntity's visual ring, check nearby clusters
        if (entity.getColorRaw() == JadeClusterEntity.VISUAL_COLOR) {
            AABB searchBox = entity.getBoundingBox().inflate(3.0);
            for (JadeClusterEntity cluster : entity.level().getEntitiesOfClass(JadeClusterEntity.class, searchBox)) {
                if (cluster.getVisualEntityId() == entity.getId() || cluster.distanceToSqr(entity) <= 4.0) {
                    if (localPlayer == null || !cluster.isOwnedBy(localPlayer)) {
                        ci.cancel();
                    }
                    return;
                }
            }
            // If it is a jade cluster ring and no cluster owned by local player was found, hide from other players
            ci.cancel();
        }
    }
}
