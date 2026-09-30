# Fire Bird (firebird)

## ข้อมูลหลัก (Identity & Configuration)

- **ชื่อสกิล (Title):** Fire Bird
- **Registry ID:** `bhspells:firebird`
- **School:** Fire (`net.offkung.bhspells.registry.BHSchoolRegistry.FIRE` หรือ `SchoolRegistry.FIRE_RESOURCE`)
- **Class:** [`FireBirdSpell.java`](file:///d:/Minecraft/Dev/ironspell_more/BHSpells/src/main/java/net/offkung/bhspells/spells/fire/FireBirdSpell.java)
- **Rarity:** Rare
- **Maximum Level:** 1
- **Cast Type:** `Instant`
- **Cast Duration:** 0 ticks
- **Cooldown:** 30 วินาที
- **Mana Cost:** 0
- **Language Keys:**
  - Name: `spell.bhspells.firebird`
  - Guide: `spell.bhspells.firebird.guide`

---

## คำอธิบายและความสามารถ (Description & Lore)

> ร่ายพลังเวทมนตร์สาย Fire เพื่อสร้างความเสียหายหรือควบคุมสถานะในพื้นที่การรบ

---

## พฤติกรรมและกลไกการทำงาน (Gameplay & Mechanics)

1. **การร่ายและเป้าหมาย (Casting & Targeting):**
   - รูปแบบการร่าย: **Instant** ใช้เวลา 0 ticks ในการร่าย
   - ตรวจสอบเงื่อนไขก่อนร่ายผ่าน `checkPreCastConditions`
   - ตรวจจับเป้าหมายที่เป็นศัตรูและปฏิบัติตามกฎ Friendly Fire (`DamageSources.isFriendlyFireBetween`)
2. **การทำงานของเวทมนตร์ (Execution Flow):**
   - เมื่อร่ายสำเร็จ `onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData)` จะทำงาน
   - มีการคำนวณสเกลพลังตาม Spell Power และระดับเลเวลของคาถา

---

## เสียงและเอฟเฟกต์ (Audio & Visuals)

- **Cast Start Sound:** `Optional.of(BHSoundRegistry.FIRE_IMPACT_SPELL.get())`
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
| 1 | 0 | 30s | Instant | 0 ticks |
