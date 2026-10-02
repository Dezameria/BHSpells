# BHSpells VFX Exemplar Map

Use this map to locate working analogues across each visual technique in BHSpells. Always reference the closest complete chain before writing new VFX code.

---

## 1. Particle Types & Providers

### A. Simple Sprite Particle
- **Registry:** `net.offkung.bhspells.registry.ParticleRegistry.GREEN_CROSS_PARTICLE` (`SimpleParticleType(false)`)
- **Particle Class:** `net.offkung.bhspells.client.particle.GreenCrossParticle` (extends `TextureSheetParticle`)
- **Provider Registration:** `net.offkung.bhspells.client.BHSpellsClient.registerParticles()` via `event.registerSpriteSet(..., GreenCrossParticle.Provider::new)`
- **Assets:**
  - Definition: `assets/bhspells/particles/green_cross.json`
  - Texture: `assets/bhspells/textures/particle/green_cross.png`
- **Spawn (Server):** `serverLevel.sendParticles(ParticleRegistry.GREEN_CROSS_PARTICLE.get(), x, y, z, count, dx, dy, dz, speed)`
- **Spawn (Client):** `level.addParticle(ParticleRegistry.GREEN_CROSS_PARTICLE.get(), x, y, z, vx, vy, vz)`

### B. Custom Option Particle (Rich Data via Codec & Deserializer)
- **Registry:** `ParticleRegistry.SHOCKWAVE_CUSTOM` (`ParticleType<ShockwaveParticleOptionCustom>`)
- **Option Class:** `net.offkung.bhspells.client.particle.ShockwaveParticleOptionCustom` (implements `ParticleOptions`, defines `CODEC`, `DESERIALIZER`, `writeToNetwork`, `writeToString`)
- **Particle Class:** `net.offkung.bhspells.client.particle.ShockwaveParticleCustom` (custom expanding radius, orientation direction matrix, emissive `ParticleRenderType`)
- **Provider Registration:** `BHSpellsClient.registerParticles()` via `event.registerSpriteSet(ParticleRegistry.SHOCKWAVE_CUSTOM.get(), ShockwaveParticleCustom.Provider::new)`
- **Spawn:** `serverLevel.sendParticles(new ShockwaveParticleOptionCustom(color, radius, fullbright, dir), x, y, z, 1, 0, 0, 0, 0)`

### C. Direct Geometry & Special Particle (Citadel / Lightning)
- **Registry:** `ParticleRegistry.PURPLE_LIGHTNING` (`SimpleParticleType(false)`)
- **Particle Class:** `net.offkung.bhspells.client.particle.PurpleLightningParticle` (uses `ParticleRenderType.CUSTOM`, Citadel `LightningRender`, `MultiBufferSource.BufferSource`, and `PoseStack`)
- **Provider Registration:** `BHSpellsClient.registerParticles()` via `event.registerSpecial(..., new PurpleLightningParticle.Provider())`

### D. Velocity-Packed Entity ID Particle
- **Registry:** `ParticleRegistry.DING` (`SimpleParticleType(false)`)
- **Particle Class:** `net.offkung.bhspells.client.particle.DingEntityAfterImageParticle`
- **Mechanism:** Entity ID is bit-packed into the `xSpeed` argument (`Double.doubleToRawLongBits(entityId)`). The particle queries `level.getEntity(entityId)` and terminates if the entity is dead or missing.
- **Provider Registration:** `BHSpellsClient.registerParticles()` via `event.registerSpriteSet(..., DingEntityAfterImageParticle.Provider::new)`

---

## 2. Model-Spawned Visual Entities

### A. GeckoLib Animated Entity
- **Entity Registry:** `net.offkung.bhspells.registry.EntityRegistry.GOLDEN_GATE_ENTITY`
- **Entity Class:** `net.offkung.bhspells.entity.spells.golden_gate.GoldenGateEntity` (implements `GeoEntity`, `AnimatableInstanceCache`, registers `AnimationController`, syncs `DATA_IS_OPENING`/`DATA_IS_CLOSING`)
- **GeoModel Class:** `net.offkung.bhspells.entity.spells.golden_gate.GoldenGateModel`
- **Renderer Class:** `net.offkung.bhspells.entity.spells.golden_gate.GoldenGateRenderer` (extends `GeoEntityRenderer<GoldenGateEntity>`, overrides `getRenderType` to return `RenderType.energySwirl`)
- **Client Registration:** `BHSpellsClient.rendererRegister()` via `event.registerEntityRenderer(EntityRegistry.GOLDEN_GATE_ENTITY.get(), GoldenGateRenderer::new)`
- **Assets:**
  - Model: `assets/bhspells/geo/golden_gate_entity.geo.json`
  - Animation: `assets/bhspells/animations/golden_gate.animation.json`
  - Texture: `assets/bhspells/textures/entity/golden_gate/golden_gate.png`
- **Other Examples:** `FlamesEagleEntity`, `SupportingBamboo`, `LotusPetal`, `GaleDriveVortexEntity`

### B. Vanilla Layer-Based EntityModel
- **Entity Registry:** `EntityRegistry.RADIANT_CRYSTAL`
- **Entity Class:** `net.offkung.bhspells.entity.spells.resounding_radiant.RadiantCrystalEntity`
- **Model Class:** `RadiantCrystalRenderer.RadiantCrystalModel` (extends `EntityModel<RadiantCrystalEntity>`, defines `ModelLayerLocation`, `MeshDefinition`, `PartDefinition`)
- **Renderer Class:** `net.offkung.bhspells.entity.spells.resounding_radiant.RadiantCrystalRenderer`
- **Client Registration:**
  - Layer Definition: `BHSpellsClient.onRegisterLayers()` via `event.registerLayerDefinition(RadiantCrystalModel.LAYER_LOCATION, RadiantCrystalModel::createBodyLayer)`
  - Renderer: `BHSpellsClient.rendererRegister()` via `event.registerEntityRenderer(..., RadiantCrystalRenderer::new)`
- **Texture:** `assets/bhspells/textures/entity/radiant_crystal/radiant_crystal.png`
- **Other Examples:** `AzureVenomNeedleRenderer`, `GlacialSpikeRenderer`, `GlacialTombRenderer`

### C. Direct Quad / Polygon Flipbook Entity
- **Entity Registry:** `EntityRegistry.JADE_BRUSH_SLASH`
- **Entity Class:** `net.offkung.bhspells.entity.spells.jade_brush_slash.JadeBrushSlash`
- **Renderer Class:** `net.offkung.bhspells.entity.spells.jade_brush_slash.JadeBrushSlashRenderer`
- **Rendering Mechanism:** Draws oriented quads directly into `VertexConsumer` via `poseMatrix` and `normalMatrix`, utilizes `RenderType.entityCutoutNoCull`, animated frame array `TEXTURES[tickCount / 2 % len]`, and fullbright `uv2(15728880)`.
- **Textures:** `assets/bhspells/textures/entity/jade_brush_slash/red_beryl_slash_1..4.png`

### D. Procedural Geodesic Dome Mesh
- **Entity Registry:** `EntityRegistry.CRYSTAL_HYDRO_DOME_AOE`
- **Renderer Class:** `net.offkung.bhspells.entity.spells.crystal_hydro_dome.CrystalHydroDomeRenderer`
- **Key Techniques:**
  - Custom `RenderType` with `ADDITIVE_ALPHA_TRANSPARENCY` (`SRC_ALPHA`, `ONE`), emissive shader, no depth write (`WriteMaskStateShard(true, false)`).
  - Precomputed geodesic dome geometry cached in `ConcurrentHashMap<MeshKey, MeshData>`.
  - Multi-pass rendering: outer shell, inner shell, rotating energy ripples, lotus geometry.

### E. Instanced Unit Arrays
- **Entity Registry:** `EntityRegistry.AMETHYST_DECREE_CASTER_RING`
- **Renderer Class:** `net.offkung.bhspells.entity.spells.amethyst_decree.AmethystDecreeCasterRingRenderer`
- **Model Class:** `CrystalUnitModel` (small and large baked `ModelPart` layers)
- **Mechanism:** Repeated unit models rendered iteratively with computed coordinate/rotation transforms (`CrystalTransform`) around ring patterns.

---

## 3. Player & Living Entity Render Layers

- **Client Registration:** `BHSpellsClient.registerRenderers(EntityRenderersEvent.AddLayers)` via `addLayerToPlayerSkin(event, "default")` and `addLayerToPlayerSkin(event, "slim")`
- **Examples:**
  - `GildedHarePlayerLayer`: attaches procedural rabbit ear ribbons and leg ribbons via `GildedHareRibbonGeometry` onto player head/limb bones conditioned on `MobEffectsRegistry.GILDED_HARE`.
  - `BHChargeSpellLayer`: renders charge aura during spell cast.
  - `GalePiercerChargeLayer`: arrow/vortex charging visuals.
  - `EnergySwirlLayer.Vanilla`: wrapping swirl texture on players under `MobEffectsRegistry.RED_CHARGED`.

### B. Global Living Entity Status Overlays (`RenderLivingEvent.Post`)
Located in `net.offkung.bhspells.client.event.*` because they hook into the Forge event bus rather than entity-specific renderer registries. Use this when an effect must render on **any entity or mob** (not just players) afflicted with a status effect.
- **Examples:**
  - `ShockingEffectClientEvents`: draws crawling electric arcs across procedural body anchors (head, shoulders, chest, arms, legs, ground) on any entity afflicted with `MobEffectsRegistry.SHOCKING`.
  - `JadeAuraRenderEvents`: draws swirling jade aura ribbons around afflicted entities.
  - `GildedHareBindingRenderEvents`: renders binding cocoon ribbons on stunned targets.
  - `VenomousBlossomfallClientEvents`: floating lotus flower seals and poison needles over targets.

---

## 4. Effekseer & AAAParticles Integration

- **Asset Location:** `assets/bhspells/effeks/*.efkefc` + subfolder textures.
- **Server-Side Trigger:**
  ```java
  private static final ParticleEmitterInfo FX = new ParticleEmitterInfo(BHSpells.id("pure_white_flame"));
  AAALevel.addParticle(serverLevel, 64.0, FX.clone().position(pos).rotationFromForward(dir, 0f).scale(1.0f));
  ```
- **Stop & Cleanup Packets:** For continuous or loop effects, send stop packets across network:
  - `StopHymnEffekPacket` (`HymnofPurificationSpell` / `HymnofPurificationClientEvents`)
  - `StopYinInkEffekPacket` (`YinInkCascadeSpell` / `YinInkCascadeClientEvents`)

---

## 5. Screen Shake, Impact & Fracture

- **Screen Shake:**
  - Packet: `net.offkung.bhspells.network.server.ScreenShakePacket`
  - Client Handler: `net.offkung.bhspells.client.event.ClientScreenShakeEvent.shake(power)`
  - Usage: `PacketHandler.sendToTrackingPlayers(new ScreenShakePacket(intensity, pos), serverLevel, pos)`
- **Fracture (Epic Fight):**
  - Helper: `net.offkung.bhspells.compat.epicfight.EpicFightFractureHelper.trySpawnFracture(entity, level, origin, count, radius, scale)`
- **Coordinated VFX Helpers:**
  - `net.offkung.bhspells.client.particle.GildedHareVfx`: bundles dust auras, kick impact particles, burst directions, sound events, and stun cocoon visuals.
  - `net.offkung.bhspells.client.particle.JadeAuraVfx`: handles swirling aura rings and periodic spark emissions.
