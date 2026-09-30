# Wing of Wind (wing_of_wind)

## ข้อมูลหลัก (Identity & Configuration)

- **ชื่อสกิล (Title):** Wing of Wind
- **Registry ID:** `bhspells:wing_of_wind`
- **School:** Nature (`net.offkung.bhspells.registry.BHSchoolRegistry.NATURE` หรือ `SchoolRegistry.NATURE_RESOURCE`)
- **Class:** [`WingOfWindSpell.java`](file:///d:/Minecraft/Dev/ironspell_more/BHSpells/src/main/java/net/offkung/bhspells/spells/nature/WingOfWindSpell.java)
- **Rarity:** Rare
- **Maximum Level:** 1
- **Cast Type:** `Instant`
- **Cast Duration:** 0 ticks
- **Cooldown:** 16 วินาที
- **Mana Cost:** 0
- **Language Keys:**
  - Name: `spell.bhspells.wing_of_wind`
  - Guide: `spell.bhspells.wing_of_wind.guide`

---

## คำอธิบายและความสามารถ (Description & Lore)

> ร่ายพลังเวทมนตร์สาย Nature เพื่อสร้างความเสียหายหรือควบคุมสถานะในพื้นที่การรบ

---

## พฤติกรรมและกลไกการทำงาน (Gameplay & Mechanics)

1. **การร่ายและเป้าหมาย (Casting & Targeting):**
   - รูปแบบการร่าย: **Instant** ใช้เวลา 0 ticks ในการร่าย
   - ตรวจสอบเงื่อนไขก่อนร่ายผ่าน `checkPreCastConditions`
   - ตรวจจับเป้าหมายที่เป็นศัตรูและปฏิบัติตามกฎ Friendly Fire (`DamageSources.isFriendlyFireBetween`)
2. **การทำงานของเวทมนตร์ (Execution Flow):**
   - เมื่อร่ายสำเร็จ `onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData)` จะทำงาน
   - มีการคำนวณสเกลพลังตาม Spell Power และระดับเลเวลของคาถา
   - ส่งผลต่อสถานะ Effect: `WING_OF_WIND_FLIGHT`, `WING_OF_WIND_IMMOBILIZE`

---

## เสียงและเอฟเฟกต์ (Audio & Visuals)

- **Cast Start Sound:** `SoundEvents.EMPTY`
- **Cast Finish Sound:** `Optional.of(SoundRegistry.EVOCATION_CAST.get())`
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
| 1 | 0 | 16s | Instant | 0 ticks |
