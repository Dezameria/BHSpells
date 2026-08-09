package net.offkung.bhspells.entity.spells.explosive_lily;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.render.RenderHelper;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.offkung.bhspells.BHSpells;

import static net.offkung.bhspells.entity.spells.explosive_lily.ExplosiveLilyBall.lifetime;

public class ExplosiveLilyRenderer extends EntityRenderer<ExplosiveLilyBall> {
    public static final ModelLayerLocation MODEL_LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "explosive_lily_ball_model"), "main");
    private static final ResourceLocation[] SWIRL_TEXTURES = {
            BHSpells.id("textures/entity/explosive_lily_ball/explosive_lily_ball_0.png"),
            BHSpells.id("textures/entity/explosive_lily_ball/explosive_lily_ball_1.png"),
            BHSpells.id("textures/entity/explosive_lily_ball/explosive_lily_ball_2.png"),
            BHSpells.id("textures/entity/explosive_lily_ball/explosive_lily_ball_3.png"),
            BHSpells.id("textures/entity/explosive_lily_ball/explosive_lily_ball_4.png")
    };

    private final ModelPart orb;

    public ExplosiveLilyRenderer(EntityRendererProvider.Context context) {
        super(context);
        ModelPart modelpart = context.bakeLayer(MODEL_LAYER_LOCATION);
        this.orb = modelpart.getChild("orb");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("orb", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        return LayerDefinition.create(meshdefinition, 8, 8);
    }

    @Override
    public void render(ExplosiveLilyBall entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        if (entity.isAttached()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0, entity.getBoundingBox().getYsize() * .5f, 0);
        VertexConsumer consumer;
        for (int i = 0; i < 3; i++) {
            poseStack.pushPose();
            float r = 0.62f;
            float g = 1f;
            float b = 0.54f;
            r = Mth.clamp(r + r * i, 0, 1f);
            g = Mth.clamp(g + g * i, 0, 1f);
            b = Mth.clamp(b + b * i, 0, 1f);
            float f = entity.tickCount + partialTicks + i * 777;
            float swirlX = Mth.cos(.065f * f) * 180;
            float swirlY = Mth.sin(.065f * f) * 180;
            float swirlZ = Mth.cos(.065f * f + 5464) * 180;
            float scalePerLayer = 0.2f;
            poseStack.mulPose(Axis.XP.rotationDegrees(swirlX * (int) Math.pow(-1, i)));
            poseStack.mulPose(Axis.YP.rotationDegrees(swirlY * (int) Math.pow(-1, i)));
            poseStack.mulPose(Axis.ZP.rotationDegrees(swirlZ * (int) Math.pow(-1, i)));
            consumer = bufferSource.getBuffer(RenderHelper.CustomerRenderType.magic(getSwirlTextureLocation(entity, i * i)));
            float scale = 2f - i * scalePerLayer;
            if (entity.tickCount > lifetime - 10) {
                float f2 = (entity.tickCount + partialTicks - (lifetime - 5)) * .4f;
                scale += i == 0 ? f2 : -f2;
            }
            poseStack.scale(scale, scale, scale);
            this.orb.render(poseStack, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, r, g, b,1f);
            poseStack.popPose();
        }
        poseStack.popPose();

        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }

    @Override
    public ResourceLocation getTextureLocation(ExplosiveLilyBall entity) {
        return SWIRL_TEXTURES[0];
    }

    private ResourceLocation getSwirlTextureLocation(ExplosiveLilyBall entity, int offset) {
        int frame = (entity.tickCount + offset) % SWIRL_TEXTURES.length;
        return SWIRL_TEXTURES[frame];
    }
}
