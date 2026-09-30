# Phantom Dodge (เธซเธฅเธเธซเธฅเธตเธเธ เธฒเธเธฅเธงเธเธ•เธฒ)

| Item | Value |
| --- | --- |
| Spell id | `bhspells:phantom_dodge` |
| School / rarity / max level | Evocation / Epic / 1 |
| Spell cast type | Instant (Self-cast) |
| Base mana / mana per level | 50 / 0 |
| Cooldown | 50 seconds (1000 ticks) |
| Base duration | 200 ticks (10.0 seconds) |
| Duration scaling | +100 ticks (+5.0 seconds) per level above 1 |
| Base dodge charges | 3 charges |
| Charge scaling | +2 charges per level above 1 |
| MobEffect id | `bhspells:phantom_dodge` (Beneficial) |
| Epic Fight animations | 13 dodge variants in `biped/spells/dodge/` |
| Particle VFX | Epic Fight White Afterimage (`EpicFightParticles.WHITE_AFTERIMAGE`) |

---

## Spell Overview & Mechanics

**Phantom Dodge** is an Evocation defensive buff that envelops the caster in deceptive illusory duplicates. While active, any incoming damage directed towards the caster is authoritatively intercepted and completely negated, triggering a dynamic Epic Fight dodge animation paired with a dramatic white afterimage.

### Level Scaling & Command Cast

While the default maximum level in `DefaultConfig` is set to `1` for standard spellbook progression, casting via commands (such as `/cast bhspells:phantom_dodge <level>`) scales the duration and number of dodges:
- **Duration Formula**: `200 + 100 * (level - 1)` ticks (Level 1: 10s, Level 2: 15s, Level 3: 20s).
- **Dodge Charges Formula**: `3 + 2 * (level - 1)` charges (Level 1: 3 dodges, Level 2: 5 dodges, Level 3: 7 dodges).
- Casting refreshes and resets the buff to full charges, preventing stale hidden-effect stacking.

### Cancellation & Stance Toggle (เธเธฒเธฃเธขเธเน€เธฅเธดเธเธเธฑเธเธเนเธญเธเน€เธงเธฅเธฒ)

- **Early Cancellation / Recast Toggle**: If the player already has `PhantomDodgeEffect` active, casting the spell again (or Sneak-Casting) immediately cancels the buff:
  - `canBeCastedBy` bypasses standard cooldown and mana checks when `player.hasEffect(PHANTOM_DODGE)`, allowing the caster to cancel the stance freely without consuming additional mana.
  - In `checkPreCastConditions`, `PhantomDodgeEffect` is removed, a client message `Phantom Dodge cancelled.` is displayed, standard cooldown is applied and synchronized, and the cast returns `false`.
  - Tooltips display `Recast to cancel active dodge stance` (`ui.bhspells.phantom_dodge_toggle_info`).

---

## Authoritative Damage Interception

1. **Event Priority**: Damage negation is handled server-side in `PhantomDodgeEvents.onLivingAttack` subscribed at `EventPriority.HIGH`.
2. **Negation**: When an attack targets an entity possessing `PhantomDodgeEffect`, the `LivingAttackEvent` is canceled (`event.setCanceled(true)`), reducing incoming damage to zero and preventing knockback or hurt cooldown.
3. **Charge Consumption**:
   - The effect's amplifier represents `remainingCharges - 1`.
   - Each negated hit decrements the amplifier by 1 while preserving the remaining duration window.
   - When the final charge (`amplifier == 0`) is consumed, `PhantomDodgeEffect` is removed from the entity.
4. **Bypass Rules**: Attacks tagged with `DamageTypeTags.BYPASSES_INVULNERABILITY` (e.g. falling into the void or `/kill` commands) are intentionally not intercepted, preserving expected server integrity. All other damage types (melee, projectiles, magic, explosions, fire, fall damage) are evaded.

---

## Epic Fight Animation & Afterimage Integration

Each time an incoming hit is successfully evaded, the server triggers the safe facade `EpicFightCompat.triggerPhantomDodge(target)`:

### 1. 13 Dodge Animation Pool
The system randomly selects one of 13 registered animations located in `assets/bhspells/animmodels/animations/biped/spells/dodge/`:
- **Yamato 4-Way Dodges**:
  - `biped/spells/dodge/dmcyamato_dodge_b` (Backward)
  - `biped/spells/dodge/dmcyamato_dodge_f` (Forward)
  - `biped/spells/dodge/dmcyamato_dodge_l` (Left)
  - `biped/spells/dodge/dmcyamato_dodge_r` (Right)
- **Standard Dodges**:
  - `biped/spells/dodge/dodge_b` (Backward roll/dodge)
  - `biped/spells/dodge/dodge_f` (Forward roll/dodge)
- **Murasama Dodges**:
  - `biped/spells/dodge/hf_murasama_dodge_b` (Backward)
  - `biped/spells/dodge/hf_murasama_dodge_f` (Forward)
- **Stylish Evasion**:
  - `biped/spells/dodge/perfect_dodge` (Sekiro / Perfect dodge maneuver)
- **Quick Steps**:
  - `biped/spells/dodge/step_b` (Backward quick step)
  - `biped/spells/dodge/step_f` (Forward quick step)
  - `biped/spells/dodge/step_l` (Left quick step)
  - `biped/spells/dodge/step_r` (Right quick step)

These clips are registered via `PhantomDodgeAnimations.registerAnimations` as `DodgeAnimation` with a 0-length invulnerability interval (`0.0F, 0.0F`), ensuring game balance and charge consumption remain the sole authority.

### 2. Epic Fight White Afterimage
- Dispatched via `AfterimageVfx.spawnWhiteAfterimage(level, entity)`.
- Broadcasts `EpicFightParticles.WHITE_AFTERIMAGE` with the entity's ID encoded into the `xDist` parameter (`Double.longBitsToDouble(entity.getId())`).
- Visible to all nearby tracking clients, rendering an ethereal white snapshot of the caster's pose at the evasion point.

### 3. Graceful Fallback Without Epic Fight
- When Epic Fight is absent or linkage fails, `EpicFightCompat` safely catches linkage errors and disables animation playback.
- Damage negation, charge tracking, effect duration, UI tooltips, and audio feedback (`SoundEvents.PLAYER_ATTACK_SWEEP`) remain 100% functional.

---

## Assets and Localization

- **Spell Icon**: `assets/bhspells/textures/gui/spell_icons/phantom_dodge.png`
- **MobEffect Icon**: `assets/bhspells/textures/mob_effect/phantom_dodge.png`
- **Translation Keys (`en_us.json`)**:
  - `spell.bhspells.phantom_dodge`: "Phantom Dodge"
  - `spell.bhspells.phantom_dodge.guide`: Description guide
  - `effect.bhspells.phantom_dodge`: "Phantom Dodge"
  - `ui.bhspells.phantom_dodge_duration`: "Duration: %s sec"
  - `ui.bhspells.phantom_dodge_charges`: "Dodge Charges: %s"
  - `ui.bhspells.phantom_dodge_cancelled`: "Phantom Dodge cancelled."
  - `ui.bhspells.phantom_dodge_toggle_info`: "Recast to cancel active dodge stance"

