package net.offkung.bhspells.entity.spells.amethyst_decree;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.offkung.bhspells.BHSpells;

public class CrystalUnitModel {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "textures/entity/amethyst_crystal/amethyst_crystal.png");
    public static final ModelLayerLocation SMALL_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "amethyst_crystal_small"), "main");
    public static final ModelLayerLocation LARGE_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "amethyst_crystal_large"), "main");

    public static LayerDefinition createSmallLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("small_core",
                CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, -7.0f, -1.0f, 2.0f, 7.0f, 2.0f),
                PartPose.ZERO);
        root.addOrReplaceChild("small_shard",
                CubeListBuilder.create().texOffs(0, 9).addBox(-1.0f, -4.0f, -1.0f, 2.0f, 4.0f, 2.0f),
                PartPose.offsetAndRotation(1.5f, -1.5f, 0.5f, 0.0f, 0.0f, 0.45f));
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static LayerDefinition createLargeLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("large_core",
                CubeListBuilder.create().texOffs(10, 0).addBox(-2.0f, -14.0f, -2.0f, 4.0f, 14.0f, 4.0f),
                PartPose.ZERO);
        root.addOrReplaceChild("large_shard_a",
                CubeListBuilder.create().texOffs(10, 19).addBox(-1.5f, -8.0f, -1.5f, 3.0f, 8.0f, 3.0f),
                PartPose.offsetAndRotation(2.5f, -3.0f, 1.0f, 0.0f, 0.0f, 0.35f));
        root.addOrReplaceChild("large_shard_b",
                CubeListBuilder.create().texOffs(23, 19).addBox(-1.5f, -8.0f, -1.5f, 3.0f, 8.0f, 3.0f),
                PartPose.offsetAndRotation(-2.5f, -3.0f, -1.0f, 0.0f, 0.0f, -0.35f));
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static void renderInstance(ModelPart smallUnit, ModelPart largeUnit, PoseStack poseStack, VertexConsumer consumer, int light, int overlay, CrystalTransform transform, float riseOffset) {
        renderInstance(smallUnit, largeUnit, poseStack, consumer, light, overlay, transform, riseOffset, 1.0f);
    }

    public static void renderInstance(ModelPart smallUnit, ModelPart largeUnit, PoseStack poseStack, VertexConsumer consumer, int light, int overlay, CrystalTransform transform, float riseOffset, float extraScale) {
        poseStack.pushPose();
        poseStack.translate(transform.dx(), transform.dy(), transform.dz());
        poseStack.mulPose(Axis.YP.rotationDegrees(transform.yawDeg()));
        poseStack.mulPose(Axis.XP.rotationDegrees(transform.tiltDeg()));
        float scale = transform.scale() * extraScale;
        poseStack.scale(scale, -scale, scale);
        poseStack.translate(0.0, -riseOffset * 16.0, 0.0);
        ModelPart unit = transform.large() ? largeUnit : smallUnit;
        unit.render(poseStack, consumer, light, overlay, 1.0f, 1.0f, 1.0f, 1.0f);
        poseStack.popPose();
    }
}
