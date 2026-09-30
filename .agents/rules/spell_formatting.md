# Spell Class Code Formatting Rules

New spell classes, and existing spell classes undergoing a structural refactor, follow the BHSpells-preferred declaration sequence inside the class body:

```
spell identity -> constants/static fields -> optional unit info -> default config field -> constructor -> standard getters/overrides -> spell logic/helpers
```

Do not mechanically reorder an otherwise untouched legacy BHSpells class only to satisfy this convention. For a focused behavior or balance change, preserve the class's stable local structure and place new members in the nearest matching section.

---

## Standard Member Ordering

Every spell class must declare its members in the following order:

### 1. Spell Identity
Declare the spell identity and resource location at the top of the class body:
- Keep the established local form: an instance `spellId` or a static `SPELL_ID`.
- Add `SPELL_ID_STR` only when networking, stance tracking, domain registration, or another consumer needs the string form.
- Do not add duplicate identity fields solely to match an example.

```java
// ==========================================
// 1. SPELL ID
// ==========================================
public static final String SPELL_ID_STR = "bhspells:<spell_name>";
private final ResourceLocation spellId = new ResourceLocation(BHSpells.MODID, "<spell_name>");
```

---

### 2. Constants and Static Fields
Declare tuning constants, base values, per-level scalings, durations, cooldowns, ranges, and other class-wide static state after the spell identity:
- Use the narrowest visibility required by consumers. Prefer `private static final` when no other class needs the value; use `public static final` for intentionally shared values.
- Clear naming convention (`BASE_MANA_COST`, `MANA_COST_PER_LEVEL`, `COOLDOWN_SECONDS`, `BASE_DAMAGE`, `DAMAGE_PER_LEVEL`, etc.).

```java
// ==========================================
// 2. CONSTANTS (Tuning & Code Defaults)
// ==========================================
private static final int BASE_MANA_COST = 50;
private static final int MANA_COST_PER_LEVEL = 10;
private static final double COOLDOWN_SECONDS = 15.0;
private static final float BASE_DAMAGE = 10.0F;
private static final float DAMAGE_PER_LEVEL = 2.0F;
```

---

### 3. Optional Unit Info / Description
When a spell supplies custom tooltip rows, implement `@Override public List<MutableComponent> getUniqueInfo(...)` after the constants:
- Supplies the tooltip and description lines displayed in-game (damage, radius, duration, range, special notes).
- Placing this immediately after constants keeps the user-facing numbers and their presentation directly visible alongside the tuning defaults.
- Omit this override when the inherited tooltip is sufficient; do not add an empty implementation for formatting purposes.

```java
// ==========================================
// 3. UNIT INFO / DESCRIPTION (Tooltips)
// ==========================================
@Override
public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
    return List.of(
        Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 1)),
        Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(getRadius(spellLevel, caster), 1))
    );
}
```

---

### 4. Default Config Field
Declare the `DefaultConfig` field after `getUniqueInfo`, or after the constants when no custom unit info exists:
- Configures `minRarity`, `schoolResource`, `maxLevel`, `cooldownSeconds`.
- Keep the field before the constructor. Place `getDefaultConfig()` with the standard overrides after the constructor, matching BHSpells classes.

```java
// ==========================================
// 4. DEFAULT CONFIG
// ==========================================
private final DefaultConfig defaultConfig = new DefaultConfig()
        .setMinRarity(SpellRarity.RARE)
        .setSchoolResource(SchoolRegistry.LIGHTNING_RESOURCE)
        .setMaxLevel(5)
        .setCooldownSeconds(COOLDOWN_SECONDS)
        .build();
```

---

### 5. Constructor
Initializes base properties inherited from `AbstractSpell`:
- `this.baseManaCost = BASE_MANA_COST;`
- `this.manaCostPerLevel = MANA_COST_PER_LEVEL;`
- `this.baseSpellPower = ...;`
- `this.spellPowerPerLevel = ...;`
- `this.castTime = ...;`

---

### 6. Getters and Standard Overrides
Basic metadata and spell engine overrides:
- `getDefaultConfig()`
- `getSpellResource()`
- `getManaCost(int spellLevel)` (referencing `SpellConfig.<SpellName>` if configurable)
- `getSpellCooldown()`
- `getCastType()`
- `getCastFinishAnimation()` / `getCastStartSound()`

```java
@Override
public DefaultConfig getDefaultConfig() {
    return defaultConfig;
}
```

---

### 7. Pre-Cast Conditions and Casting Logic
Pre-conditions, execution, and helper methods:
- `canBeCastedBy(...)`
- `checkPreCastConditions(...)`
- `onCast(...)`
- Spell-specific calculations such as `getDamage(...)` or `getRadius(...)` may sit immediately before or after the lifecycle method they support.
- Private entity-spawning, targeting, particle, and other implementation helpers normally follow the public/override methods.

---

## Summary Checklist

When creating or refactoring a spell:
- [ ] **Spell identity** is at the top without unnecessary duplicate forms.
- [ ] **Constants/static fields** follow and use only the visibility their consumers require.
- [ ] Optional **Unit Info (`getUniqueInfo`)** follows constants when custom tooltip rows are needed.
- [ ] The **`defaultConfig` field** is declared before the constructor.
- [ ] The **constructor** comes before `getDefaultConfig()` and the other standard getters/overrides.
- [ ] **Pre-cast checks, cast lifecycle, calculations, and private helpers** form the final logic section.
