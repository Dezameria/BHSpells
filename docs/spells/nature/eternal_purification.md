# Eternal Purification (eternal_purification)

## ข้อมูลหลัก (Identity & Configuration)

- **ชื่อสกิล (Title):** Eternal Purification
- **Registry ID:** `bhspells:eternal_purification`
- **School:** Nature (`net.offkung.bhspells.registry.BHSchoolRegistry.NATURE` หรือ `SchoolRegistry.NATURE_RESOURCE`)
- **Class:** [`EternalPurificationSpell.java`](file:///d:/Minecraft/Dev/ironspell_more/BHSpells/src/main/java/net/offkung/bhspells/spells/nature/EternalPurificationSpell.java)
- **Rarity:** Epic
- **Maximum Level:** 1
- **Cast Type:** `Long`
- **Cast Duration:** 0 ticks
- **Cooldown:** 60 วินาที
- **Mana Cost:** 0
- **Language Keys:**
  - Name: `spell.bhspells.eternal_purification`
  - Guide: `spell.bhspells.eternal_purification.guide`

---

## คำอธิบายและความสามารถ (Description & Lore)

> ร่ายพลังเวทมนตร์สาย Nature เพื่อสร้างความเสียหายหรือควบคุมสถานะในพื้นที่การรบ

---

## พฤติกรรมและกลไกการทำงาน (Gameplay & Mechanics)

1. **การร่ายและเป้าหมาย (Casting & Targeting):**
   - รูปแบบการร่าย: **Long** ใช้เวลา 0 ticks ในการร่าย
   - ตรวจสอบเงื่อนไขก่อนร่ายผ่าน `checkPreCastConditions`
   - ตรวจจับเป้าหมายที่เป็นศัตรูและปฏิบัติตามกฎ Friendly Fire (`DamageSources.isFriendlyFireBetween`)
2. **การทำงานของเวทมนตร์ (Execution Flow):**
   - เมื่อร่ายสำเร็จ `onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData)` จะทำงาน
   - มีการคำนวณสเกลพลังตาม Spell Power และระดับเลเวลของคาถา

---

## เสียงและเอฟเฟกต์ (Audio & Visuals)

- **Cast Start Sound:** `Optional.of(BHSoundRegistry.RUMBLE_1.get())`
- **Cast Finish Sound:** `Optional.of(BHSoundRegistry.BREAK_LARGE.get())`
- **Cast Start Animation:** `BHSpellAnimations.PURIFICATION_PILLAR_CHARGE`
- **Cast Finish Animation:** `BHSpellAnimations.PURIFICATION_PILLAR_CAST`

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
| 1 | 0 | 60s | Long | 0 ticks |
