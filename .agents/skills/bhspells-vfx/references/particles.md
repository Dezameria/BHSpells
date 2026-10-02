# BHSpells Particle Development Reference

Use this reference when authoring, modifying, or debugging Minecraft particles in BHSpells.

---

## 1. Simple vs. Typed Option Particles

### When to choose `SimpleParticleType`
- The particle needs only standard position, velocity, and an optional random variance.
- Visual parameters (color, size, lifetime) are fixed or derived purely from particle age/sprites.
- Examples: `ParticleRegistry.GREEN_CROSS_PARTICLE`, `ParticleRegistry.PINK_FIRE`, `ParticleRegistry.OAK_LEAF_PARTICLE`.

### When to choose custom `ParticleOptions`
- The particle requires dynamic runtime configuration from the server, such as:
  - Custom RGB/RGBA tint colors per cast
  - Dynamic radius or scale factor
  - 3D target direction / normal orientation vector
  - Emissive flag or intensity
- Examples: `ShockwaveParticleOptionCustom`, `CustomZapParticleOption`, `ColoredCherryParticleOption`.

---

## 2. Implementing Custom `ParticleOptions`

Custom particle options MUST maintain strict serialization symmetry across three layers:
1. **Network Buffer (`FriendlyByteBuf`):** `writeToNetwork()` and `DESERIALIZER.fromNetwork()` must write and read fields in the exact same sequence and types.
2. **Command / Parser (`StringReader`):** `writeToString()` and `DESERIALIZER.fromCommand()` must serialize and parse identically.
3. **Codec (`RecordCodecBuilder`):** Must match the fields for data serialization and datagen.

### Template:
```java
public class MyVfxParticleOption implements ParticleOptions {
    private final Vector3f color;
    private final float scale;
    private final boolean emissive;

    public static final Codec<MyVfxParticleOption> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.FLOAT.fieldOf("r").forGetter(o -> o.color.x),
            Codec.FLOAT.fieldOf("g").forGetter(o -> o.color.y),
            Codec.FLOAT.fieldOf("b").forGetter(o -> o.color.z),
            Codec.FLOAT.fieldOf("scale").forGetter(o -> o.scale),
            Codec.BOOL.fieldOf("emissive").forGetter(o -> o.emissive)
        ).apply(instance, (r, g, b, scale, emissive) ->
            new MyVfxParticleOption(new Vector3f(r, g, b), scale, emissive))
    );

    public static final Deserializer<MyVfxParticleOption> DESERIALIZER = new Deserializer<>() {
        @Override
        public MyVfxParticleOption fromCommand(ParticleType<MyVfxParticleOption> type, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            float r = reader.readFloat();
            reader.expect(' ');
            float g = reader.readFloat();
            reader.expect(' ');
            float b = reader.readFloat();
            reader.expect(' ');
            float scale = reader.readFloat();
            reader.expect(' ');
            boolean emissive = reader.readBoolean();
            return new MyVfxParticleOption(new Vector3f(r, g, b), scale, emissive);
        }

        @Override
        public MyVfxParticleOption fromNetwork(ParticleType<MyVfxParticleOption> type, FriendlyByteBuf buf) {
            float r = buf.readFloat();
            float g = buf.readFloat();
            float b = buf.readFloat();
            float scale = buf.readFloat();
            boolean emissive = buf.readBoolean();
            return new MyVfxParticleOption(new Vector3f(r, g, b), scale, emissive);
        }
    };

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeFloat(color.x);
        buf.writeFloat(color.y);
        buf.writeFloat(color.z);
        buf.writeFloat(scale);
        buf.writeBoolean(emissive);
    }

    @Override
    public ParticleType<MyVfxParticleOption> getType() {
        return ParticleRegistry.MY_VFX_PARTICLE.get();
    }
}
```

---

## 3. Custom Particle Class Implementation

### Base Class: `TextureSheetParticle`
Most billboard and sprite particles extend `TextureSheetParticle`:

```java
public class MyVfxParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    public MyVfxParticle(ClientLevel level, double x, double y, double z,
                         double vx, double vy, double vz,
                         SpriteSet sprites, MyVfxParticleOption options) {
        super(level, x, y, z, vx, vy, vz);
        this.sprites = sprites;
        this.lifetime = 20; // 1 second
        this.quadSize = options.getScale();
        this.rCol = options.getColor().x;
        this.gCol = options.getColor().y;
        this.bCol = options.getColor().z;
        this.alpha = 1.0f;
        this.hasPhysics = false; // Disable collision for pure magical effects

        // Pick initial sprite frame
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        // Fade out or advance animated sprite frames
        this.setSpriteFromAge(this.sprites);
        this.alpha = 1.0f - ((float) this.age / (float) this.lifetime);
    }

    @Override
    public int getLightColor(float partialTick) {
        // Return fullbright light coordinate (15728880) for glowing magic particles
        return 15728880;
    }

    @Override
    public ParticleRenderType getRenderType() {
        // Options: PARTICLE_SHEET_TRANSLUCENT, PARTICLE_SHEET_LIT, PARTICLE_SHEET_OPAQUE
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}
```

### Choosing `ParticleRenderType`
- `ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT`: Standard for transparent, fading particles (blended with background). Depth writing disabled, depth reading enabled.
- `ParticleRenderType.PARTICLE_SHEET_LIT`: Affected by lightmap and entity lighting.
- `ParticleRenderType.PARTICLE_SHEET_OPAQUE`: Fast, writes to depth buffer. No semi-transparency.
- `ParticleRenderType.CUSTOM`: Used when rendering custom geometry via `PoseStack` and `MultiBufferSource` (e.g., `PurpleLightningParticle`).

---

## 4. Special Velocity-Channel Packing Pattern

In cases where a `SimpleParticleType` is required (or for compatibility with external systems that only accept double velocities), integer or timestamp data can be packed into the velocity arguments (`xSpeed`, `ySpeed`, `zSpeed`).

### Exemplar: `DingEntityAfterImageParticle`
- **Sender (Server):**
  ```java
  double packedEntityId = Double.longBitsToDouble((long) targetEntity.getId());
  serverLevel.sendParticles(ParticleRegistry.DING.get(), x, y, z, 1, packedEntityId, 0, 0, 0);
  ```
- **Receiver / Provider (Client):**
  ```java
  int entityId = (int) Double.doubleToRawLongBits(xSpeed);
  Entity entity = level.getEntity(entityId);
  if (!(entity instanceof LivingEntity living) || living.isDeadOrDying()) {
      return null; // Abort particle creation if target is missing
  }
  ```
- **Safety Rule:** Whenever using velocity packing for entity tracking:
  1. Check `level.getEntity(id)` in provider before constructing the particle.
  2. In `particle.tick()`, verify the entity is still alive and not removed; call `this.remove()` immediately if invalid.

---

## 5. Particle Registration Checklist

When adding a new particle, follow this step-by-step checklist:

1. **`ParticleRegistry.java`**:
   - Register deferred type in `PARTICLE_TYPES.register("particle_id", ...)`
2. **Option Class (if typed)**:
   - Implement `ParticleOptions`, `CODEC`, `DESERIALIZER`, `writeToNetwork`, `writeToString`.
3. **Particle Class**:
   - Extends `TextureSheetParticle` or `Particle`. Implement `Provider` subclass.
4. **Client Registration (`BHSpellsClient.java`)**:
   - In `registerParticles(RegisterParticleProvidersEvent event)`:
     - `event.registerSpriteSet(ParticleRegistry.MY_PARTICLE.get(), MyParticle.Provider::new);`
     - OR `event.registerSpecial(..., new CustomProvider());`
5. **Particle JSON**:
   - Create `src/main/resources/assets/bhspells/particles/<particle_id>.json`:
     ```json
     {
       "textures": [
         "bhspells:particle_name_0",
         "bhspells:particle_name_1"
       ]
     }
     ```
6. **Texture Assets**:
   - Place textures in `src/main/resources/assets/bhspells/textures/particle/particle_name_*.png`.

---

## 6. Performance & Budget Guidelines

- **Count Budget:**
  - Continuous ambient auras: 1–3 particles per tick.
  - Cast bursts: 10–30 particles instantaneous.
  - Large spell impact: 30–60 particles instantaneous.
  - NEVER spawn >100 particles in a single tick per entity.
- **Lifetime:** Keep transient particles short (5–20 ticks / 0.25–1.0s). Long lifetimes (>60 ticks) create high particle counts and cause severe overdraw lag.
- **Broadcast Radius:** When spawning from `ServerLevel.sendParticles()`, pass explicit spread and count. If broadcasting to tracking players only, ensure recipient distance is bounded.
