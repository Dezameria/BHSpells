# BHSpells VFX Debugging & Verification

Use this guide to diagnose rendering glitches, crashes, invisible effects, or performance bottlenecks in BHSpells.

---

## 1. Systematic Layer-by-Layer Diagnostics

### Problem A: Pink & Black Checkerboard Texture
- **Cause:** Minecraft cannot find the registered texture asset.
- **Checklist:**
  1. Check casing: paths MUST be strictly lowercase snake_case (e.g. `assets/bhspells/textures/particle/my_vfx.png`).
  2. Check namespace: `BHSpells.id("textures/...")` versus `ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "textures/...")`.
  3. For particles: verify `assets/bhspells/particles/<particle_id>.json` contains the correct texture reference (note: without `.png` extension inside particle JSON!).

### Problem B: Particle is Completely Invisible
- **Checklist:**
  1. **Client Registration:** Was `event.registerSpriteSet(...)` or `event.registerSpecial(...)` called in `BHSpellsClient.registerParticles()`?
  2. **Spawn Level:** Did the server call `serverLevel.sendParticles()` or did the client call `level.addParticle()`? Check that the coordinates are valid (not `NaN` or inside a solid block).
  3. **Lifetime & Alpha:** Did `lifetime` initialize to `0`? Is `alpha` set to `0.0f`?
  4. **Light Level:** Is the particle rendering in darkness without emissive light? Override `getLightColor()` to return `15728880`.

### Problem C: Deserializer / Network Crash when Spawning Particle
- **Symptom:** Disconnect or crash when `serverLevel.sendParticles()` is called with custom `ParticleOptions`.
- **Cause:** Asymmetric read/write order in network buffer.
- **Checklist:** Compare `writeToNetwork(FriendlyByteBuf)` and `DESERIALIZER.fromNetwork(..., FriendlyByteBuf)` line-by-line. The order and types of floats, booleans, and ints must match identically.

### Problem D: Entity Model is Invisible or Crashes on Launch
- **Checklist:**
  1. **Renderer Registration:** Is `event.registerEntityRenderer(...)` present in `BHSpellsClient.rendererRegister()`?
  2. **Layer Definition Registration:** For vanilla `EntityModel`, was `event.registerLayerDefinition(LAYER_LOCATION, ...)` registered in `EntityRenderersEvent.RegisterLayerDefinitions`? Missing this causes a null root model part crash.
  3. **GeckoLib Paths:** In `GeoModel`, verify that `getModelResource`, `getTextureResource`, and `getAnimationResource` point to existing `.geo.json`, `.animation.json`, and `.png` files.
  4. **Frustum Culling:** If the entity is large (like a dome, skybeam, or giant gate), does it disappear when looking away? Override `shouldRender(...)` in the entity renderer to return `true`.

### Problem E: Black Rectangles around Transparent Textures
- **Cause:** The `RenderType` being used does not enable blending or has depth write enabled without alpha testing.
- **Fix:**
  - Use `RenderType.entityCutoutNoCull(...)` for hard-edge alpha textures.
  - Use `RenderType.entityTranslucent(...)` or `RenderHelper.CustomerRenderType.magic(...)` for semi-transparent glowing textures.
  - Ensure PNG files have true transparent alpha pixels rather than solid black backgrounds.

### Problem F: Energy Shell Clips Holes in Water or Entities
- **Cause:** Transparent geometry is writing into the Z-buffer (depth mask enabled).
- **Fix:** Use `WriteMaskStateShard(true, false)` or `depthMask(false)` in your custom `RenderType.CompositeState`.

### Problem G: Dedicated Server Crash (`NoClassDefFoundError: net/minecraft/client/...`)
- **Cause:** Client-only class imported or referenced in common or server code.
- **Checklist:**
  - Common entity classes, spells, and network packets MUST NOT import `net.minecraft.client.*`.
  - Client event listeners, models, and renderers must be marked with `@Mod.EventBusSubscriber(value = Dist.CLIENT, ...)` or called exclusively through client setup events.
  - Test compiling with `.\gradlew.bat compileJava` and ensure no client classes leak into server logic.

---

## 2. Verification Protocol

Before completing any VFX task, perform these verification steps:

### 1. Compile Verification
Run the Gradle compiler from the workspace root:
```powershell
.\gradlew.bat compileJava
```
Ensure zero syntax errors, valid type signatures, and clean imports.

### 2. Manual Runtime In-Game Checks
Because automated compilers cannot judge aesthetics, verify the following manually in `runClient`:
- **Day & Night Appearance:** Verify glowing elements remain vibrant in direct daylight and do not black out in pitch-black caves.
- **Winding & Culling:** Move the camera around the entity (front, back, above, below). Quads should not disappear when viewed from the reverse side unless culling is intentional.
- **Visual Timing:** Check that particle bursts, sounds, and animations synchronize cleanly with the spell cast duration and projectile impacts.
- **Multiplayer & Disconnect Safety:** If an effect is spawned in-world, confirm it is removed cleanly when the player dies, disconnects, or when its lifetime expires.
- **Framerate & Overdraw:** Ensure that multiple simultaneous casts do not cause catastrophic FPS drops.

### 3. Spell Documentation Alignment
If the VFX belongs to a spell, open `docs/spells/<school>/<spell_id>.md` and verify that the particle IDs, visual descriptions, and audio cues documented match the final implementation.
