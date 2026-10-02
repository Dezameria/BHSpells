# BHSpells Visual Entities & Model Rendering

Use this reference when creating or modifying in-world visual entities, animated 3D models, procedural meshes, or direct quad renderers in BHSpells.

---

## 1. Choosing the Right Visual Entity Technique

| Visual Need | Recommended Approach | Key Exemplar |
|---|---|---|
| Complex rigged & animated 3D mesh (gates, creatures, animated foliage, swirling vortexes) | **GeckoLib Entity** (`GeoEntity` + `GeoModel` + `GeoEntityRenderer`) | `GoldenGateEntity`, `FlamesEagleEntity` |
| Modular mechanical/blocky Minecraft-style model (crystals, floating needles, spikes) | **Vanilla EntityModel** (`ModelLayerLocation` + `LayerDefinition`) | `RadiantCrystalRenderer`, `AzureVenomNeedleRenderer` |
| Flat oriented effects, slashes, circular decals, expanding shock rings | **Direct Quad / Polygon Renderer** (`EntityRenderer` + `VertexConsumer`) | `JadeBrushSlashRenderer` |
| Large atmospheric dome, curved shield barrier, energy field | **Procedural Geodesic Mesh** with Geometry Caching | `CrystalHydroDomeRenderer` |
| Geometric unit formations (rings of floating blades or crystals) | **Instanced Unit Model** | `AmethystDecreeCasterRingRenderer` |

---

## 2. GeckoLib Animated Visual Entities

### Architecture
- **Entity:** Implements `GeoEntity`. Holds `AnimatableInstanceCache` (`GeckoLibUtil.createInstanceCache(this)`).
- **Synchronized State:** Use `SynchedEntityData` (`EntityDataAccessor<Boolean>` or integer state) to notify client when to transition animations.
- **Model:** Extends `GeoModel<T>`. Points to `.geo.json`, `.animation.json`, and `.png` texture.
- **Renderer:** Extends `GeoEntityRenderer<T>`. Handles custom `RenderType`, scaling, and rotation.

### Entity Setup Template
```java
public class MyVisualEntity extends Entity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final EntityDataAccessor<Boolean> DATA_ACTIVE = 
        SynchedEntityData.defineId(MyVisualEntity.class, EntityDataSerializers.BOOLEAN);

    public MyVisualEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_ACTIVE, true);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, event -> {
            if (this.entityData.get(DATA_ACTIVE)) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
            }
            return event.setAndContinue(RawAnimation.begin().thenPlay("finish"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && this.tickCount >= 100) { // 5-second lifetime
            this.discard();
        }
    }
}
```

### Renderer Customizations
In `GeoEntityRenderer`, you can customize:
1. **RenderType:** Override `getRenderType(...)` to use glowing or magical render modes:
   ```java
   @Override
   public RenderType getRenderType(MyVisualEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
       // Examples: RenderType.energySwirl(texture, 0, 0), RenderType.entityTranslucent(texture), RenderType.entityCutoutNoCull(texture)
       return RenderType.energySwirl(texture, 0, 0);
   }
   ```
2. **Transforms & Orientation:** Override `preRender(...)`:
   ```java
   @Override
   public void preRender(PoseStack poseStack, MyVisualEntity animatable, BakedGeoModel model,
                         @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer,
                         boolean isReRender, float partialTick, int packedLight, int packedOverlay,
                         float red, float green, float blue, float alpha) {
       poseStack.mulPose(Axis.YP.rotationDegrees(-animatable.getYRot()));
       poseStack.mulPose(Axis.XP.rotationDegrees(animatable.getXRot()));
       super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
   }
   ```

---

## 3. Vanilla Layer-Based Models (`EntityModel`)

When creating a model using Minecraft's built-in `EntityModel`:

### Step 1: Define `ModelLayerLocation` and `LayerDefinition`
```java
public static class MyModel extends EntityModel<MyEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = 
        new ModelLayerLocation(BHSpells.id("my_visual"), "main");
    private final ModelPart root;

    public MyModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("cube",
            CubeListBuilder.create().texOffs(0, 0).addBox(-4F, -4F, -4F, 8F, 8F, 8F),
            PartPose.ZERO
        );
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(MyEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Dynamic procedural animations (bobbing, rotation)
        this.root.yRot = ageInTicks * 0.05F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
```

### Step 2: Register in `BHSpellsClient.java`
Must register in TWO separate event handlers:
1. `EntityRenderersEvent.RegisterLayerDefinitions`:
   ```java
   event.registerLayerDefinition(MyModel.LAYER_LOCATION, MyModel::createBodyLayer);
   ```
2. `EntityRenderersEvent.RegisterRenderers`:
   ```java
   event.registerEntityRenderer(EntityRegistry.MY_ENTITY.get(), MyRenderer::new);
   ```

---

## 4. Direct Quad / Polygon Renderers

For dynamic slashing waves, flat impact decals, or expanding rings where a 3D model is unnecessary:

### Exemplar: `JadeBrushSlashRenderer`
```java
public void render(JadeBrushSlash entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
    poseStack.pushPose();
    poseStack.mulPose(Axis.YP.rotationDegrees(90.0F - entity.getYRot()));
    poseStack.mulPose(Axis.ZP.rotationDegrees(entity.getXRot()));

    PoseStack.Pose pose = poseStack.last();
    Matrix4f poseMatrix = pose.pose();
    Matrix3f normalMatrix = pose.normal();

    // Select animated flipbook frame based on tickCount
    ResourceLocation texture = TEXTURES[entity.tickCount / 2 % TEXTURES.length];
    VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));

    float halfWidth = entity.getBbWidth() * 0.75F;
    float height = entity.getBbHeight() * 0.5F;
    int fullbright = 15728880; // Emissive light coordinate

    // Build quad vertices (CCW winding for front face)
    consumer.vertex(poseMatrix, -halfWidth, height, -halfWidth).color(255, 255, 255, 255).uv(0.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(fullbright).normal(normalMatrix, 0.0F, 1.0F, 0.0F).endVertex();
    consumer.vertex(poseMatrix, halfWidth, height, -halfWidth).color(255, 255, 255, 255).uv(1.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(fullbright).normal(normalMatrix, 0.0F, 1.0F, 0.0F).endVertex();
    consumer.vertex(poseMatrix, halfWidth, height, halfWidth).color(255, 255, 255, 255).uv(1.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(fullbright).normal(normalMatrix, 0.0F, 1.0F, 0.0F).endVertex();
    consumer.vertex(poseMatrix, -halfWidth, height, halfWidth).color(255, 255, 255, 255).uv(0.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(fullbright).normal(normalMatrix, 0.0F, 1.0F, 0.0F).endVertex();

    poseStack.popPose();
    super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
}
```

---

## 5. Procedural Geodesic Meshes & Memory Caching

### Exemplar: `CrystalHydroDomeRenderer`
When generating large complex 3D geometry (spheres, hemispheres, ripples) procedurally:
1. **Geometry Caching:** NEVER recalculate sin/cos vertex tables every frame. Store vertices in a static `ConcurrentHashMap<MeshKey, MeshData>`.
2. **Custom RenderType Composition:**
   - Use additive blending (`SRC_ALPHA`, `ONE`).
   - Disable depth mask (`depthMask(false)`) for translucent energy shells to prevent ugly clipping boxes around entities inside.
   - Use emissive shader (`GameRenderer::getRendertypeEntityTranslucentEmissiveShader`).

---

## 6. Visual Entity Lifecycle & Multiplayer Invariants

1. **Pure Presentation vs. Gameplay:**
   - Visual entities should not own authoritative damage or collision mechanics.
   - If a spell creates an AOE effect, keep damage logic on a dedicated server-side entity or AOE field, and let the visual entity handle rendering.
2. **Cleanup Conditions:**
   - Define deterministic lifetime (`tickCount >= maxLife -> discard()`).
   - Discard visual entity if its linked caster dies, logs off, or changes dimensions.
3. **Tracking & Client Culling:**
   - Set sensible tracking ranges in `EntityRegistry.java` (e.g., `updateInterval(1)`, `clientTrackingRange(64)`).
   - If rendering large domes or high skybeams, override `shouldRender(entity, camera, camX, camY, camZ)` to return `true` to prevent aggressive frustum culling.
