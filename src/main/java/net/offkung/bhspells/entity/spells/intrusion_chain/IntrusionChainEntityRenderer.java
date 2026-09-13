package net.offkung.bhspells.entity.spells.intrusion_chain;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsspellbooks.entity.spells.ender_chain.EnderChain;
import io.redspace.ironsspellbooks.render.RenderHelper;
import io.redspace.ironsspellbooks.render.SpellRenderingHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class IntrusionChainEntityRenderer extends EntityRenderer<IntrusionChainEntity> {

    private static final float CHAIN_WIDTH = 0.4f;

    public IntrusionChainEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0f;
    }

    @Override
    public void render(IntrusionChainEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        PartEntity<?>[] parts = entity.getParts();
        if (parts == null || parts.length == 0) {
            return;
        }
        Entity owner = entity.getOwner();
        Entity victim = entity.getVictim();
        if (owner == null || victim == null) {
            // endpoints not resolved on the client yet - skip a frame instead of drawing a stray chain
            return;
        }

        Vec3 origin = entity.position();

        List<Vec3> nodes = new ArrayList<>(parts.length + 2);
        nodes.add(entity.getBodyAnchor().subtract(origin));
        for (PartEntity<?> part : parts) {
            nodes.add(new Vec3(Mth.lerp(partialTick, part.xo, part.getX()), Mth.lerp(partialTick, part.yo, part.getY()), Mth.lerp(partialTick, part.zo, part.getZ())).subtract(origin));
        }
        nodes.add(victim.getPosition(partialTick).add(0, victim.getBbHeight() * 0.5, 0).subtract(origin));

        float warmup = Mth.clamp((entity.warmup + partialTick) / EnderChain.VISUAL_WARMUP_TIME, 0, 1f);
        renderChain(entity.getChainType().getTexture(), nodes, poseStack, bufferSource, warmup);

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void renderChain(ResourceLocation texture, List<Vec3> nodes, PoseStack poseStack, MultiBufferSource bufferSource, float warmupPercent) {
        if (nodes.size() < 2) {
            return;
        }

        float totalLength = 0f;
        for (int i = 0; i < nodes.size() - 1; i++) {
            totalLength += (float) nodes.get(i).distanceTo(nodes.get(i + 1));
        }
        if (totalLength < 1.0E-4f) {
            return;
        }

        float hiddenLength = totalLength * (1f - Mth.clamp(warmupPercent, 0f, 1f));
        VertexConsumer consumer = bufferSource.getBuffer(RenderHelper.CustomerRenderType.magicNoCull(texture));

        float travelled = 0f;
        for (int i = 0; i < nodes.size() - 1; i++) {
            Vec3 a = nodes.get(i);
            Vec3 b = nodes.get(i + 1);
            float segLen = (float) a.distanceTo(b);
            if (segLen < 1.0E-5f) {
                continue;
            }

            float segStart = travelled;
            float segEnd = travelled + segLen;
            travelled = segEnd;

            if (segEnd <= hiddenLength) {
                continue;
            }

            Vec3 from = a;
            float uFrom = segStart;
            if (segStart < hiddenLength) {
                from = a.lerp(b, (hiddenLength - segStart) / segLen);
                uFrom = hiddenLength;
            }

            float localLen = (float) from.distanceTo(b);
            Vec3 direction = b.subtract(from).scale(1.0 / localLen);
            Quaternionf rotation = new Quaternionf().rotationTo(new Vector3f(0, 0, 1), new Vector3f((float) direction.x, (float) direction.y, (float) direction.z));

            poseStack.pushPose();
            poseStack.translate(from.x, from.y, from.z);
            poseStack.mulPose(rotation);

            PoseStack.Pose pose = poseStack.last();
            Vec3 localFrom = Vec3.ZERO;
            Vec3 localTo = new Vec3(0, 0, localLen);
            SpellRenderingHelper.drawQuad(localFrom, localTo, CHAIN_WIDTH, 0, pose, consumer, 255, 255, 255, 255, uFrom, segEnd);
            SpellRenderingHelper.drawQuad(localFrom, localTo, 0, CHAIN_WIDTH, pose, consumer, 255, 255, 255, 255, uFrom, segEnd);

            poseStack.popPose();
        }
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@Nonnull IntrusionChainEntity entity) {
        return entity.getChainType().getTexture();
    }
}
