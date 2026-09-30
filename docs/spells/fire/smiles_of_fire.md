# Smiles of Fire (smiles_of_fire)

## ข้อมูลหลัก (Identity & Configuration)

- **ชื่อสกิล (Title):** Smiles of Fire
- **Registry ID:** `bhspells:smiles_of_fire`
- **School:** Fire (`net.offkung.bhspells.registry.BHSchoolRegistry.FIRE` หรือ `SchoolRegistry.FIRE_RESOURCE`)
- **Class:** [`SmilesOfFireSpell.java`](file:///d:/Minecraft/Dev/ironspell_more/BHSpells/src/main/java/net/offkung/bhspells/spells/fire/SmilesOfFireSpell.java)
- **Rarity:** Rare
- **Maximum Level:** 1
- **Cast Type:** `Long`
- **Cast Duration:** 0 ticks
- **Cooldown:** 30 วินาที
- **Mana Cost:** 0
- **Language Keys:**
  - Name: `spell.bhspells.smiles_of_fire`
  - Guide: `spell.bhspells.smiles_of_fire.guide`

---

## คำอธิบายและความสามารถ (Description & Lore)

> ร่ายพลังเวทมนตร์สาย Fire เพื่อสร้างความเสียหายหรือควบคุมสถานะในพื้นที่การรบ

---

## พฤติกรรมและกลไกการทำงาน (Gameplay & Mechanics)

1. **การร่ายและเป้าหมาย (Casting & Targeting):**
   - รูปแบบการร่าย: **Long** ใช้เวลา 0 ticks ในการร่าย
   - ตรวจสอบเงื่อนไขก่อนร่ายผ่าน `checkPreCastConditions`
   - ตรวจจับเป้าหมายที่เป็นศัตรูและปฏิบัติตามกฎ Friendly Fire (`DamageSources.isFriendlyFireBetween`)
2. **การทำงานของเวทมนตร์ (Execution Flow):**
   - เมื่อร่ายสำเร็จ `onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData)` จะทำงาน
   - มีการคำนวณสเกลพลังตาม Spell Power และระดับเลเวลของคาถา
   - ส่งผลต่อสถานะ Effect: `SMILES_OF_FIRE_CD`, `SMILES_OF_FIRE`

---

## เสียงและเอฟเฟกต์ (Audio & Visuals)

- **Cast Start Sound:** `Optional.of(SoundRegistry.FIRE_CAST.get())`
- **Cast Finish Sound:** `Optional.of(SoundRegistry.FIRE_BOMB_CAST.get())`
- **Cast Start Animation:** `SpellAnimations.ONE_HANDED_HORIZONTAL_SWING_ANIMATION`
- **Cast Finish Animation:** `AnimationHolder.pass()`

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
| 1 | 0 | 30s | Long | 0 ticks |
