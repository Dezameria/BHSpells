# BHSpells VFX Orchestration, Impact Feel & Balancing

Use this reference when designing coordinated visual effects, impact feedback (screen shake, ground fractures), Effekseer integration, and visual readability.

---

## 1. Authority & Multi-Layered VFX Orchestration

Great combat VFX in BHSpells is rarely just a single particle. It combines multiple coordinated layers:
1. **Primary Shape / Effect:** 3D model, slash entity, or Effekseer arc.
2. **Atmospheric Embers / Dust:** Falling leaves, embers, sparks, or magic motes.
3. **Ground / Physical Impact:** Epic Fight ground fracture or shockwave ring.
4. **Screen Feel:** Bounded screen shake packet.
5. **Auditory Reinforcement:** Layered sound effects (sharp snap + low rumble).

### Reusable VFX Utility Pattern (`*Vfx.java`)
When a spell or effect has complex multi-step impact visuals, bundle them into a dedicated client/particle VFX utility class (e.g., `GildedHareVfx.java`, `JadeAuraVfx.java`):

```java
public class MySpellVfx {
    // 1. Harmonious color palette
    public static final Vector3f COLOR_CORE = new Vector3f(1.0F, 0.85F, 0.3F);
    public static final Vector3f COLOR_ACCENT = new Vector3f(0.95F, 0.5F, 0.1F);

    public static void spawnImpact(ServerLevel level, Vec3 origin, Vec3 direction, float power) {
        // A. Primary particle burst
        level.sendParticles(ParticleRegistry.MY_BURST.get(), origin.x, origin.y, origin.z, 20, 0.3, 0.3, 0.3, 0.08);

        // B. Dynamic shockwave orientation
        ShockwaveParticleOptionCustom shockwave = new ShockwaveParticleOptionCustom(COLOR_CORE, 3.5F * power, true, new Vector3f((float) direction.x, (float) direction.y, (float) direction.z));
        level.sendParticles(shockwave, origin.x, origin.y, origin.z, 1, 0, 0, 0, 0);

        // C. Ground fracture (Epic Fight integration)
        EpicFightFractureHelper.trySpawnFracture(null, level, origin, 2, 4, 3.0D);

        // D. Screen shake to nearby tracking players
        PacketHandler.sendToTrackingPlayers(new ScreenShakePacket(0.6F * power, origin), level, origin);

        // E. Layered audio feedback
        level.playSound(null, origin.x, origin.y, origin.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.2F, 1.1F);
        level.playSound(null, origin.x, origin.y, origin.z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5F, 1.4F);
    }
}
```

---

## 2. Effekseer & AAAParticles Integration

BHSpells supports high-fidelity 3D particle animations authored in Effekseer (`.efkefc`).

### File Placement
- Effekseer binary files: `src/main/resources/assets/bhspells/effeks/<effect_name>.efkefc`
- Any supporting image textures: `src/main/resources/assets/bhspells/effeks/textures/`

### Server-Side Triggering via `AAALevel`
```java
// Define emitter info once:
private static final ParticleEmitterInfo MY_EFFEK_FX = 
    new ParticleEmitterInfo(BHSpells.id("pure_white_flame"));

// Inside spell onCast or event:
Vec3 effekPos = caster.position().add(0, caster.getEyeHeight() * 0.6, 0).add(forward.scale(2.0));
AAALevel.addParticle(
    serverLevel, 
    64.0, // Max view distance
    MY_EFFEK_FX.clone()
        .position(effekPos)
        .rotationFromForward(forward, (float) Math.PI)
        .scale(0.75f)
);
```

### Looping Effects & Stop Packets
If an Effekseer effect loops continuously during a channel or lingering spell (e.g., `HymnofPurificationSpell`, `YinInkCascadeSpell`):
1. **Track the Effect ID / Token** on the client or entity.
2. **Send a Stop Packet:** When the channel finishes or is interrupted, the server must broadcast a stop packet (e.g., `StopHymnEffekPacket`, `StopYinInkEffekPacket`) to tracking clients.
3. **Client Event Listener:** In `ClientEvents.java`, call the stop/kill routine on the active emitter.
4. **Mandatory Cleanup:** Ensure the effect is terminated if the caster dies, disconnects, or changes dimensions.

---

## 3. Screen Shake Tuning

Screen shake provides tactile force to hits, but excessive shake disorients players and induces motion sickness.

### Recommended Screen Shake Power Levels:
- **Light Impact (Fast projectile, melee combo step):** `0.1F – 0.25F`
- **Medium Hit (Heavy spell impact, charged blast):** `0.35F – 0.6F`
- **Massive / Ultimate Explosion (Meteor, full burst):** `0.7F – 1.0F`
- **Maximum Ceiling:** Avoid setting `power > 1.2F` under any standard spell circumstance.

### Dispatching Screen Shake:
```java
// Dispatch only to players within proximity or tracking:
PacketHandler.sendToTrackingPlayers(new ScreenShakePacket(power, impactPos), serverLevel, impactPos);
```

---

## 4. Visual Balance & Readability Principles

1. **Clear Combat Vision:**
   - Visual effects should communicate hitboxes, danger zones, and spell states clearly.
   - Do NOT obscure the target or caster's camera with dense, opaque black or white particle walls. Use semi-transparency (`alpha <= 0.8`) and additive blending for magic fire.
2. **Harmonious Color Palettes:**
   - Define a curated set of 2–3 colors (Core, Highlight, Shadow) rather than random rainbow hues.
   - Maintain school identities (e.g., Gold: pale light gold `(1.0, 0.88, 0.4)` + deep amber `(0.98, 0.70, 0.05)`).
3. **Sound & VFX Pairing:**
   - Fast snappy visuals pair with high-pitched chimes or sharp whooshes.
   - Heavy lingering ground fissures pair with low-frequency impacts and bass rumbles.
