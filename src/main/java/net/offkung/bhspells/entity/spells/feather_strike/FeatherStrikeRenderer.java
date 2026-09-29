package net.offkung.bhspells.entity.spells.feather_strike;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public class FeatherStrikeRenderer extends EntityRenderer<FeatherStrike> {
    private static final ItemStack FEATHER_STACK = new ItemStack(Items.FEATHER);

    public FeatherStrikeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.15f;
    }

    @Override
    public void render(FeatherStrike entity, float yaw, float partialTicks, PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();
        float yRot = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
        float xRot = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(xRot));
        poseStack.scale(0.85f, 0.85f, 0.85f);
        poseStack.mulPose(Axis.XP.rotationDegrees(45.0f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(135.0f));
        Minecraft.getInstance().getItemRenderer().renderStatic(FEATHER_STACK, ItemDisplayContext.GROUND, light, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, entity.level(), entity.getId());
        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull FeatherStrike entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
