package net.offkung.bhspells.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.spells.fire_arrow.FireArrowRenderer;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.util.DefaultBipedBoneIdents;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.entity.spells.thousand_arrows.MagicArrowRenderer;
import net.offkung.bhspells.entity.spells.thousand_arrows.MagicCastingArrowRenderer;
import net.offkung.bhspells.registry.BHSpellRegistry;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtils;

public class BHChargeSpellLayer {
    public static class Vanilla<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {

        public Vanilla(RenderLayerParent<T, M> pRenderer) {
            super(pRenderer);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource bufferSource, int pPackedLight, T entity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
            var syncedSpellData = ClientMagicData.getSyncedSpellData(entity);
            if (!syncedSpellData.isCasting()) {
                return;
            }
            var spellId = syncedSpellData.getCastingSpellId();
            poseStack.pushPose();
            this.getParentModel().translateToHand(HumanoidArm.RIGHT, poseStack);
            handleRender(poseStack, bufferSource, pPackedLight, entity, spellId, false);
            poseStack.popPose();
        }
    }

    private static <T extends LivingEntity> void handleRender(PoseStack poseStack, MultiBufferSource bufferSource, int pPackedLight, T entity, String spellId, boolean offhand) {
        if (spellId.equals(BHSpellRegistry.THOUSAND_ARROWS.get().getSpellId())) {
            poseStack.translate(((float) (offhand ? -1 : 1) / 32.0F), .5, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            MagicCastingArrowRenderer.renderModel(poseStack, bufferSource);
        }
    }

    public static class Geo extends GeoRenderLayer<AbstractSpellCastingMob> {
        public Geo(GeoEntityRenderer<AbstractSpellCastingMob> entityRenderer) {
            super(entityRenderer);
        }

        @Override
        public void render(PoseStack poseStack, AbstractSpellCastingMob entity, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            var syncedSpellData = ClientMagicData.getSyncedSpellData(entity);
            var spellId = syncedSpellData.getCastingSpellId();
            var handOpt = bakedModel.getBone(DefaultBipedBoneIdents.RIGHT_HAND_BONE_IDENT);
            var armOpt = bakedModel.getBone("right_arm");
            if (handOpt.isEmpty() || armOpt.isEmpty()) {
                return;
            }
            var hand = handOpt.get();
            var arm = armOpt.get();
            poseStack.pushPose();
            RenderUtils.translateToPivotPoint(poseStack, arm);
            RenderUtils.rotateMatrixAroundBone(poseStack, arm);
            RenderUtils.translateAwayFromPivotPoint(poseStack, arm);
            poseStack.translate(-(arm.getPivotX() - hand.getPivotX()) / 16f, (arm.getPivotY() - hand.getPivotY()) / 16f, (arm.getPivotZ() - hand.getPivotZ()) / 16f);

            handleRender(poseStack, bufferSource, packedLight, entity, spellId, false);

            poseStack.popPose();
        }
    }
}
