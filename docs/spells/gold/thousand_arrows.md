# Thousand Arrows (thousand_arrows)

## ข้อมูลหลัก (Identity & Configuration)

- **ชื่อสกิล (Title):** Thousand Arrows
- **Registry ID:** `bhspells:thousand_arrows`
- **School:** Gold (`net.offkung.bhspells.registry.BHSchoolRegistry.GOLD` หรือ `SchoolRegistry.GOLD_RESOURCE`)
- **Class:** [`ThousandArrowsSpell.java`](file:///d:/Minecraft/Dev/ironspell_more/BHSpells/src/main/java/net/offkung/bhspells/spells/gold/ThousandArrowsSpell.java)
- **Rarity:** Uncommon
- **Maximum Level:** 1
- **Cast Type:** `Long`
- **Cast Duration:** 0 ticks
- **Cooldown:** 15 วินาที
- **Mana Cost:** 0
- **Language Keys:**
  - Name: `spell.bhspells.thousand_arrows`
  - Guide: `spell.bhspells.thousand_arrows.guide`

---

## คำอธิบายและความสามารถ (Description & Lore)

> ร่ายพลังเวทมนตร์สาย Gold เพื่อสร้างความเสียหายหรือควบคุมสถานะในพื้นที่การรบ

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

- **Cast Start Sound:** `Optional.of(SoundRegistry.ARROW_VOLLEY_PREPARE.get())`
- **Cast Finish Sound:** `Optional.of(SoundEvents.EVOKER_CAST_SPELL)`
- **Cast Start Animation:** `SpellAnimations.BOW_CHARGE_ANIMATION`
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
| 1 | 0 | 15s | Long | 0 ticks |
