# เอกสารระบบ BeeHouse Spells (BHSpells)

เอกสารชุดนี้อธิบายโครงสร้างและพฤติกรรมปัจจุบันของ **BHSpells (BeeHouse Spells)** สำหรับ Minecraft Forge 1.20.1 (`net.offkung.bhspells`, Mod ID: `bhspells`)

ปัจจุบันมีเอกสารข้อกำหนดครบถ้วนสำหรับชุดเวทมนตร์ทั้งหมดรวม **63 เวทใน 7 สายเวท** ได้แก่ Fire 10, Lightning 5, Nature 12, Aqua 9, Gold 15, Ground 9 และ Evocation 3 พร้อมระบบ Subsystem หลักของม็อดทั้งหมด

## แผนที่เอกสาร

### สารบัญและข้อกำหนดเวทมนตร์
- [รายการเวททั้งหมด](all_spells.md) — สรุปเวททั้ง 63 รายการตาม registry พร้อมลิงก์ไปยัง specification ของแต่ละเวท
- [เอกสารรายสกิล](spells/README.md) — สารบัญและกฎการดูแลเอกสารรายเวทครบทั้ง 63 สกิล
- [กลไกการร่ายและวงจรชีวิต](spell_mechanics.md) — flow ของ Instant, Long, Continuous, Recast และความรับผิดชอบของ server/client

### สถาปัตยกรรมและระบบหลัก (Core Systems)
- [สถาปัตยกรรมระบบ](system_architecture.md) — registry, package, entity, renderer, particle และ subsystem หลัก
- [ระบบสตั้นและควบคุมเป้าหมายเด็ดขาด](systems/earth_roar_stun_service.md) — สถาปัตยกรรม Stun & Control Lock Service, Shield Break และ Anti-Float
- [สายเวทมนตร์ใหม่ Gold & Ground](systems/custom_magic_schools.md) — การลงทะเบียนสายเวททองคำและปฐพี, Focus Tags, Spell Power และ Attributes
- [ระบบการสั่นสะเทือนหน้าจอแบบ Native](systems/screen_shake_system.md) — Screen Shake Network Packet, Viewport Event และ Smooth Camera Decay
- [ระบบโซ่พันธนาการและเชื่อมโยงพลัง](systems/chain_and_binding_mechanics.md) — Intrusion Chain (Buff/Debuff) และ Gold Chain Arcane Shackles
- [แผนผังและสถาปัตยกรรมระบบแรงดันวิญญาณ](spiritual_pressure_architecture_and_roadmap.md) — แผนแม่บท สเปกระบบ และการออกแบบ Spiritual Pressure / Reiatsu Field System
- [สถาปัตยกรรม compatibility](compat_architecture.md) — การเชื่อมต่อกับม็อดและ API ภายนอก (Epic Fight, Avalon, AAA Particles)
- [รายงานวิเคราะห์ Multiplayer และความเสถียร](multiplayer_and_crash_analysis.md) — การวิเคราะห์จุดเสี่ยง Crash, ปัญหา Network และตรรกะใน Multiplayer

## Source of truth

เมื่อตรวจสอบค่าหรือพฤติกรรม ให้ใช้ลำดับความน่าเชื่อถือดังนี้:

1. Implementation และ registry ใน `src/main/java/net/offkung/bhspells`
2. เอกสารรายสกิลใน `docs/spells/<school>/<spell_id>.md`
3. เอกสารสรุประดับระบบในโฟลเดอร์ `docs` และ `docs/systems`

`docs/all_spells.md` เป็นสารบัญภาพรวม ไม่ใช้แทน specification รายสกิล

## การดูแลเอกสาร

- การเพิ่ม ลบ ปรับสมดุล หรือเปลี่ยนพฤติกรรมของเวท ต้องอัปเดตเอกสารรายสกิลใน change เดียวกัน
- การเพิ่มหรือลบเวทต้องซิงก์ `BHSpellRegistry`, `docs/spells/README.md` และ `docs/all_spells.md`
- การเปลี่ยน entity, renderer, packet หรือเส้นแบ่ง server/client ต้องอัปเดต `spell_mechanics.md` และ `system_architecture.md` เมื่อภาพรวมระบบเปลี่ยน
- ต้องรัน build/test ที่เกี่ยวข้องและระบุ runtime behavior ที่ยังต้องตรวจในเกม
