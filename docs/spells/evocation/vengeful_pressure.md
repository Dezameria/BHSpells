# Vengeful Pressure (แรงดันวิญญาณอาฆาต / 怨灵威压)

| Item | Value |
| --- | --- |
| Spell id | `bhspells:vengeful_pressure` |
| School / rarity / max level | Evocation / Legendary / 5 |
| Spell cast type | Instant (Stance Toggle Domain Activation) |
| Mana Cost | 100 (+25/level) on activation, 0 upkeep |
| Cooldown | 45.0 seconds (triggered only upon closing the domain) |
| Base Radius | 64.0 blocks (+14.0 blocks/level, reaching 120.0 blocks at Level 5) |
| Architecture | Reusable `ServerPressureManager` + `ClientPressureManager` Torrential Curtain Framework |
| Visual Concept | Camera-Centric 360° Malice Green Torrential Deluge (`0x22FF55`) + Top/Bottom Surging Screen Energy Wash |

## System Overview & Architecture

Vengeful Pressure operates as a colossal domain expansion (64 to 120 blocks):
- **Stance Toggle Mode**: Casting once opens the domain around the caster. It remains active indefinitely with zero continuous mana drain until the caster casts again to close it, triggering a 45-second cooldown.
- **Server Gameplay Truth**: Evaluated every 5 ticks via spatial bounding box (`AABB`). Enforces severe debuffs on hostile and non-allied targets: `MobEffects.DARKNESS`, `MobEffects.WEAKNESS` II, armor shred (-4.0 armor modifier), attack damage drain (-3.0), server-authoritative jump lockout (`TAG_ROOTED`), and kneel/knockdown reactions.
- **Client Torrential Curtain (Full 3D Heaven-to-Earth / ทั่วฟ้าดิน)**: Employs 3-tier camera-centric procedural streak generation across a 48-block cylindrical volume around the active viewer. Features sky condensation (falling from 20-48 blocks high in the heavens), mid-air atmospheric surges, and colossal sky-to-earth spiritual pillars (up to 38+ blocks tall) plunging downwards and compressing into the ground with high-speed texture streaming.
- **Screen & Audio Presentation**: Animated top and bottom streaming energy waves, heavy vignette, breathing pulse, subtle chromatic fringe, and rhythmic screen shake via `TOFollowingScreenShakeEntity`.

## Targeting & Reaction States

### Targeting
- Affects all hostile mobs, PvP targets, and non-allied living entities within the 64-120 block radius.
- The caster and allied entities are immune to debuffs and receive mild visual aura feedback (35% intensity).
- Cleans up all tags and attribute modifiers immediately when a target exits the domain or when the domain is collapsed.

### Reaction States & Thresholds
Normalized intensity `[0.0 - 1.0]`:
1. **NONE / Mild (0.00 - 0.30)**: Slight visual wash and darkness.
2. **STAGGER (0.30 - 0.45)**: `SpiritualPressureEffect` I, Weakness, stagger reaction animation.
3. **CROUCH (0.45 - 0.60)**: `SpiritualPressureEffect` I, Weakness, crouch reaction animation.
4. **KNEEL (0.60 - 0.85)**: `SpiritualPressureEffect` II, Slowness II, Weakness, Armor shred, server jump lockout (`TAG_ROOTED`), kneel reaction animation.
5. **KNOCKDOWN (0.85 - 1.00)**: `SpiritualPressureEffect` III, Slowness IV, Darkness, Armor shred, attack drain, server jump lockout (`TAG_ROOTED`), knockdown reaction animation.

## Configuration (`SpellConfig.VengefulPressure`)

- `base_mana`: 100
- `mana_per_level`: 25
- `cooldown_seconds`: 45.0
- `base_radius`: 64.0
- `radius_per_level`: 14.0
- `global_streak_budget`: 800
- `curtain_radius`: 48.0
