package net.offkung.bhspells.entity.spells.fiery_dance;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public class GoldenMarbleRenderer extends EntityRenderer<GoldenMarbleEntity> {
    private static final ItemStack MARBLE_STACK = new ItemStack(Items.SNOWBALL);

    public GoldenMarbleRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0f;
    }

    @Override
    public void render(GoldenMarbleEntity entity, float yaw, float partialTicks, PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();
        poseStack.scale(0.5f, 0.5f, 0.5f); // tune size
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.tickCount * 4f)); // optional self-spin, purely cosmetic
        Minecraft.getInstance().getItemRenderer().renderStatic(MARBLE_STACK, ItemDisplayContext.GROUND, light, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, entity.level(), 0);
        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull GoldenMarbleEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
