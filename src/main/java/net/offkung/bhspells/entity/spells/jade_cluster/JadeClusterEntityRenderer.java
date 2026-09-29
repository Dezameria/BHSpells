package net.offkung.bhspells.entity.spells.jade_cluster;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class JadeClusterEntityRenderer extends GeoEntityRenderer<JadeClusterEntity> {
    public JadeClusterEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new JadeClusterEntityModel());
        this.addRenderLayer(new JadeClusterEntityLayer(this));
    }

    @Override
    public boolean shouldRender(JadeClusterEntity entity, Frustum camera, double camX, double camY, double camZ) {
        Player localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null || !entity.isOwnedBy(localPlayer)) {
            return false;
        }
        return super.shouldRender(entity, camera, camX, camY, camZ);
    }

    @Override
    public void render(@NotNull JadeClusterEntity entity, float entityYaw, float partialTick, PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        Player localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null || !entity.isOwnedBy(localPlayer)) {
            return;
        }

        poseStack.pushPose();
        float scale = 2.0F;
        poseStack.scale(scale, scale, scale);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();

        renderLifetimeText(entity, poseStack, bufferSource);
    }

    private void renderLifetimeText(JadeClusterEntity entity, PoseStack poseStack, MultiBufferSource bufferSource) {
        int remainingTicks = Math.max(0, entity.getMaxAge() - entity.getAge());
        int remainingSeconds = (remainingTicks + 19) / 20;
        Component text = Component.literal(remainingSeconds + "s");

        poseStack.pushPose();
        poseStack.translate(0.0D, 4.2D, 0.0D);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(-0.025F, -0.025F, 0.025F);

        Matrix4f matrix4f = poseStack.last().pose();
        Font font = this.getFont();
        float xOffset = (float)(-font.width(text) / 2);

        font.drawInBatch(text, xOffset, 0, 0xFFFFFFFF, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);

        poseStack.popPose();
    }
}
