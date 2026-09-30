# Crystal Hydro-Dome (crystal_hydro_dome)

## ข้อมูลหลัก (Identity & Configuration)

- **ชื่อสกิล (Title):** Crystal Hydro-Dome
- **Registry ID:** `bhspells:crystal_hydro_dome`
- **School:** Aqua (`net.offkung.bhspells.registry.BHSchoolRegistry.AQUA` หรือ `SchoolRegistry.AQUA_RESOURCE`)
- **Class:** [`CrystalHydroDomeSpell.java`](file:///d:/Minecraft/Dev/ironspell_more/BHSpells/src/main/java/net/offkung/bhspells/spells/aqua/CrystalHydroDomeSpell.java)
- **Rarity:** Rare
- **Maximum Level:** 1
- **Cast Type:** `Continuous`
- **Cast Duration:** 0 ticks
- **Cooldown:** 0.0 วินาที
- **Mana Cost:** 0
- **Language Keys:**
  - Name: `spell.bhspells.crystal_hydro_dome`
  - Guide: `spell.bhspells.crystal_hydro_dome.guide`

---

## คำอธิบายและความสามารถ (Description & Lore)

> ร่ายพลังเวทมนตร์สาย Aqua เพื่อสร้างความเสียหายหรือควบคุมสถานะในพื้นที่การรบ

---

## พฤติกรรมและกลไกการทำงาน (Gameplay & Mechanics)

1. **การร่ายและเป้าหมาย (Casting & Targeting):**
   - รูปแบบการร่าย: **Continuous** ใช้เวลา 0 ticks ในการร่าย
   - ตรวจสอบเงื่อนไขก่อนร่ายผ่าน `checkPreCastConditions`
   - ตรวจจับเป้าหมายที่เป็นศัตรูและปฏิบัติตามกฎ Friendly Fire (`DamageSources.isFriendlyFireBetween`)
2. **การทำงานของเวทมนตร์ (Execution Flow):**
   - เมื่อร่ายสำเร็จ `onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData)` จะทำงาน
   - มีการคำนวณสเกลพลังตาม Spell Power และระดับเลเวลของคาถา

---

## เสียงและเอฟเฟกต์ (Audio & Visuals)

- **Cast Start Sound:** `SoundEvents.EMPTY`
- **Cast Finish Sound:** `SoundEvents.EMPTY`
- **Cast Start Animation:** `AnimationHolder.none()`
- **Cast Finish Animation:** `AnimationHolder.none()`

---

## สถานะความสอดคล้อง (Synchronization Status)

- [x] ลงทะเบียนใน `BHSpellRegistry` ภายใต้ Namespace `bhspells`
- [x] ซิงโครไนซ์ Language Key ใน `assets/bhspells/lang/en_us.json`
- [x] แยกคลาส Client/Server อย่างปลอดภัย ไม่เรียกใช้คลาส Client ฝั่ง Dedicated Server
- [x] รองรับระบบความปลอดภัย Friendly Fire ป้องกันการทำร้ายพวกเดียวกัน

---

## ตารางค่าพลังและการสเกล (Scaling & Balance)

| Spell Level | Mana Cost | Cooldown | Cast Type | Cast Duration |
| :---: | :---: | :---: | :---: | :---: |
| 1 | 0 | 0.0s | Continuous | 0 ticks |
