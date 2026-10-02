# BHSpells Render Layers, Shaders & Textures

Use this reference when authoring player/living entity render layers, composing custom `RenderType` pipelines, manipulating blend modes, or creating UV texture animations.

---

## 1. Player & Living Entity Render Layers (`RenderLayer`)

Render layers allow rendering custom visual elements (auras, ribbons, horns, wings, charge effects) directly attached to living entities or players.

### Registration Pattern in `BHSpellsClient.java`
Render layers MUST be registered for both player skin models (`"default"` and `"slim"`):

```java
@SubscribeEvent
public static void registerRenderers(final EntityRenderersEvent.AddLayers event) {
    addLayerToPlayerSkin(event, "default");
    addLayerToPlayerSkin(event, "slim");
}

private static void addLayerToPlayerSkin(EntityRenderersEvent.AddLayers event, String skinName) {
    EntityRenderer<? extends Player> render = event.getSkin(skinName);
    if (render instanceof LivingEntityRenderer livingRenderer) {
        livingRenderer.addLayer(new GildedHarePlayerLayer(livingRenderer));
        livingRenderer.addLayer(new BHChargeSpellLayer.Vanilla<>(livingRenderer));
    }
}
```

### Implementing a `RenderLayer`
```java
public class MyPlayerVfxLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation TEXTURE = BHSpells.id("textures/entity/my_vfx/ribbon.png");

    public MyPlayerVfxLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        // 1. Guard conditions
        if (!player.hasEffect(MobEffectsRegistry.MY_EFFECT.get())) {
            return;
        }
        if (EpicFightCompat.isBattleMode(player)) {
            // Respect optional compatibility guards if animations conflict
            return;
        }

        // 2. Select RenderType
        RenderType renderType = RenderHelper.CustomerRenderType.magic(TEXTURE);
        VertexConsumer consumer = bufferSource.getBuffer(renderType);

        // 3. Attach to bone pose
        poseStack.pushPose();
        // Attaches transform to the player's head bone:
        this.getParentModel().head.translateAndRotate(poseStack);

        // 4. Draw geometry relative to bone
        MyVfxGeometry.renderDecorations(poseStack, consumer, ageInTicks);

        poseStack.popPose();
    }
}
```

### B. Global Mob & Living Entity Overlays (`RenderLivingEvent.Post`)
While `RenderLayer` is clean for players (`PlayerModel`), adding a custom layer to *every mob and entity in Minecraft* (zombies, creepers, modded mobs) is difficult and brittle.

Instead, BHSpells uses **`RenderLivingEvent.Post` in `net.offkung.bhspells.client.event.*`** (such as `ShockingEffectClientEvents` and `JadeAuraRenderEvents`):
1. **Hook:** Listens to `RenderLivingEvent.Post<?, ?>` on `Bus.FORGE`.
2. **Filter:** Checks `entity.hasEffect(...)` and distance culling (`camera.getPosition().distanceToSqr(entity.position()) > MAX_DIST_SQR`).
3. **Anchor Calculations:** Uses entity bounding box (`entity.getBbWidth()`, `entity.getBbHeight()`) to calculate dynamic anchor points (head, shoulders, chest, arms, legs, ground).
4. **Draw:** Grabs `event.getPoseStack()` and `event.getMultiBufferSource().getBuffer(...)` and draws electrical arcs or ribbon auras directly around the afflicted mob.
5. **Periodic Sound & Particle:** Complemented by `TickEvent.ClientTickEvent` in the same class to spawn crackling particles and audio locally around the afflicted targets.

---

## 2. Composing Custom `RenderType` Pipelines

> **Important Architecture Note:**  
> BHSpells achieves custom visual effects (additive auras, glowing domes, un-culled shells) by composing Minecraft's `RenderStateShard` parameters inside custom `RenderType` builders. The project does not currently register raw standalone GLSL shaders; it binds Mojang's shader states (`GameRenderer::getRendertypeEntityTranslucentEmissiveShader`, etc.) with bespoke blend and depth configurations.

### Additive Glowing Translucent `RenderType` (Exemplar from `CrystalHydroDomeRenderer`)
```java
private static final RenderStateShard.TransparencyStateShard ADDITIVE_ALPHA_TRANSPARENCY =
    new RenderStateShard.TransparencyStateShard("bhspells_additive_alpha", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });

public static RenderType buildGlowingVfxRenderType(ResourceLocation texture) {
    RenderType.CompositeState state = RenderType.CompositeState.builder()
        .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeEntityTranslucentEmissiveShader))
        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
        .setTransparencyState(ADDITIVE_ALPHA_TRANSPARENCY)
        .setCullState(new RenderStateShard.CullStateShard(false))          // Double-sided (no culling)
        .setLightmapState(new RenderStateShard.LightmapStateShard(false))  // Full emissive (ignore world light)
        .setOverlayState(new RenderStateShard.OverlayStateShard(true))
        .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, false)) // Write COLOR, disable DEPTH WRITE
        .createCompositeState(false);

    return RenderType.create("bhspells_glowing_vfx", DefaultVertexFormat.NEW_ENTITY,
                             VertexFormat.Mode.QUADS, 256, false, true, state);
}
```

### Key Render State Shards Explained:
- **`WriteMaskStateShard(true, false)`:** Crucial for transparent energy fields! Writes color (RGB) but leaves depth buffer untouched. This prevents "clipping boxes" where the shield would hide water, particles, or entities drawn behind it.
- **`TransparencyStateShard` (Additive):** Source alpha with Dest Factor ONE creates intense glowing hot-spots where layers overlap.
- **`CullStateShard(false)`:** Renders both front and back faces of quads.

### Existing Utility Shorthands:
Before building a custom `RenderType` from scratch, check if an existing helper satisfies the requirement:
- `RenderHelper.CustomerRenderType.magic(texture)`: Standard Iron's Spells translucent magic rendering.
- `RenderType.energySwirl(texture, uOffset, vOffset)`: Vanilla energy shield swirl.
- `RenderType.entityCutoutNoCull(texture)`: Sharp cutout alpha, double-sided, illuminated.
- `RenderType.entityTranslucentEmissive(texture)`: Glowing translucent texture.

---

## 3. Textures, UV Animations & Flipbooks

### A. Programmatic Frame Sequences (Flipbooks)
Used in `JadeBrushSlashRenderer` to cycle texture frames based on entity age:
```java
private static final ResourceLocation[] FRAMES = new ResourceLocation[] {
    BHSpells.id("textures/entity/slash/frame_1.png"),
    BHSpells.id("textures/entity/slash/frame_2.png"),
    BHSpells.id("textures/entity/slash/frame_3.png"),
    BHSpells.id("textures/entity/slash/frame_4.png")
};

public ResourceLocation getTextureLocation(MyEntity entity) {
    int frameIndex = (entity.tickCount / 2) % FRAMES.length;
    return FRAMES[frameIndex];
}
```

### B. Scrolling / Flowing UVs (Beams & Cylinders)
Used in `GreenSunbeamRenderer` to create continuous upward or downward streaming energy:
```java
float deltaTicks = entity.tickCount + partialTicks;
float deltaUV = -deltaTicks % 10;
float maxV = Mth.frac(deltaUV * 0.2F - (float) Mth.floor(deltaUV * 0.1F));
float minV = -1.0F + maxV;

// Apply minV and maxV to vertex coordinates:
consumer.vertex(poseMatrix, x1, yMin, z1).color(r, g, b, a).uv(u1, minV).overlayCoords(NO_OVERLAY).uv2(FULL_BRIGHT).normal(norm, 0, 1, 0).endVertex();
consumer.vertex(poseMatrix, x2, yMax, z2).color(r, g, b, a).uv(u2, maxV).overlayCoords(NO_OVERLAY).uv2(FULL_BRIGHT).normal(norm, 0, 1, 0).endVertex();
```

### C. Emissive Light Values (`uv2`)
- Always use `15728880` or `LightTexture.FULL_BRIGHT` for `uv2` on magical, electric, or fiery elements so they do not darken inside caves or at nighttime.
