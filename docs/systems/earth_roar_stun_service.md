# ระบบการสตั้นและควบคุมเป้าหมายเด็ดขาด (Earth Roar Stun & Universal Control Service)

เอกสารนี้อธิบายสถาปัตยกรรมและการทำงานของระบบ **Stun & Control Lock Service** ในคลาส [`EarthRoarStunService.java`](file:///d:/Minecraft/Dev/ironspell_more/BHSpells/src/main/java/net/offkung/bhspells/service/EarthRoarStunService.java) ซึ่งรับผิดชอบการสตั้น ควบคุมการเคลื่อนที่ และหยุดการกระทำของ Entity ทุกชนิดใน Minecraft อย่างสมบูรณ์และเด็ดขาด

---

## 1. วัตถุประสงค์และปรัชญาการออกแบบ (Design Goals)

1. **Universal Guarantee:** ต้องสามารถสตั้นและตรึงเป้าหมายได้ทุกชนิด ทั้งผู้เล่น มอนสเตอร์ สัตว์ และ **บอสระดับสูง** (เช่น Ender Dragon, Wither, Cataclysm Bosses)
2. **Server-Authoritative Control:** ตรรกะการสตั้นและยกเลิกการกระทำทั้งหมดต้องควบคุมผ่าน Server ไม่พึ่งพาแอนิเมชันของ Client เพียงอย่างเดียว
3. **Anti-Float Guarantee:** ป้องกันไม่ให้เป้าหมายลอยเคว้งกลางอากาศอย่างเด็ดขาด โดยตัดแรงยกในแกน Y, ลบล้างสถานะ Levitation และบังคับให้แนบติดพื้นดิน
4. **Shield Breaker:** ทำลายหรือปิดการใช้งานโล่ป้องกัน (Shield Break / Disable) ก่อนที่จะสร้างดาเมจหรือล็อกสถานะ

---

## 2. โครงสร้างและการจัดเก็บ State (Data Structure)

ระบบทำงานเป็นแบบ Singleton Event Subscriber (`@Mod.EventBusSubscriber`) โดยมีแท็กและโครงสร้างข้อมูลหลัก:

```java
public static final String TAG_EARTH_ROAR_STUNNED = "bhspells:earth_roar_stunned";
private static final String DATA_ORIGINAL_NO_AI = "bhspells:er_stun_original_no_ai";
private static final String DATA_END_TICK = "bhspells:er_stun_end_tick";

private static final Map<UUID, StunTracker> ACTIVE_STUNS = new ConcurrentHashMap<>();

private record StunTracker(UUID entityUuid, int startTick, int endTick, int knockbackEndTick, boolean originalNoAi) {}
```

- **`ACTIVE_STUNS`:** ใช้ `ConcurrentHashMap` เพื่อรองรับ Thread-Safety ในการเข้าถึงข้อมูลจากหลาย Event
- **`TAG_EARTH_ROAR_STUNNED`:** แท็ก NBT ฝังลงใน `PersistentData` ของ Entity เพื่อให้ระบบอื่นๆ สามารถตรวจสอบสถานะสตั้นได้ทันที

---

## 3. ลำดับขั้นตอนการทำงาน (Execution Flow)

### 3.1 การเริ่มใช้งานสถานะสตั้น (`applyStun`)
เมื่อสกิล [`EarthRoarSpell`](file:///d:/Minecraft/Dev/ironspell_more/BHSpells/src/main/java/net/offkung/bhspells/spells/ground/EarthRoarSpell.java) หรือระบบควบคุมเรียกใช้งาน `applyStun(LivingEntity target, int durationTicks, int knockbackTicks)`:
1. **Shield Break:** ตรวจสอบว่าเป้าหมายกำลังถือโล่ป้องกันหรือไม่ หากถืออยู่ จะสั่งปิดการใช้งานโล่ (`player.disableShield(true)`) และเล่นเสียงโล่แตก `SoundEvents.SHIELD_BREAK`
2. **AI Freeze:** หากเป้าหมายเป็น `Mob` จะทำการตั้งค่า `setNoAi(true)` ชั่วคราวเพื่อหยุดการทำงานของ AI ทั้งหมด และจำค่าเดิมไว้
3. **Motion Lock:** รีเซ็ตความเร็วของ Entity เป็นศูนย์ (`setDeltaMovement(Vec3.ZERO)`) และส่ง `ClientboundSetEntityMotionPacket` ไปบังคับฝั่ง Client ทันที
4. **Debuff Injection:** ให้สถานะ `MobEffects.MOVEMENT_SLOWDOWN` ระดับ 6 (Amplifier 5) และ `MobEffects.WEAKNESS` ระดับ 4
5. **Casting Cancellation:** ส่งแพ็กเก็ต `CancelCastPacket` ยกเลิกการร่ายเวทที่เป้าหมายกำลังร่ายอยู่ทันที
6. **VFX & Audio:** เล่นเสียงสตั้น `SoundRegistry.STUN_IMPACT` และปล่อยอนุภาคสะเก็ดไฟฟ้า `ParticleTypes.ELECTRIC_SPARK` รอบศีรษะของเป้าหมาย

### 3.2 การป้องกันการกระทำระหว่างติดสตั้น (Event Interception)
- **`LivingAttackEvent` / `AttackEntityEvent`:** ดักจับการโจมตี หากผู้โจมตีมีแท็ก `TAG_EARTH_ROAR_STUNNED` การโจมตีจะถูกยกเลิก (`event.setCanceled(true)`) ทันที
- **`PlayerInteractEvent`:** ยกเลิกการคลิกขวาใช้งานไอเทมทุกชนิดระหว่างติดสตั้น
- **`LivingEvent.LivingTickEvent`:** ในทุกๆ Tick ของเซิร์ฟเวอร์ จะตรวจสอบเป้าหมายที่ติดสตั้น:
  - บังคับความเร็วแกน Y ไม่ให้กระโดดขึ้น
  - ล้างสถานะ `MobEffects.LEVITATION` ออกทันที
  - เมื่อครบกำหนดเวลา (`currentTick >= tracker.endTick`) จะคืนค่า AI ให้กับม็อบ ล้างแท็ก NBT และลบออกจาก `ACTIVE_STUNS` อย่างปลอดภัย

---

## 4. สถานะความสอดคล้องและการเชื่อมต่อ (Integration Status)

- [x] รองรับ Forge Server & Dedicated Server ปราศจาก Client Class Leaks
- [x] ตรวจสอบ Friendly Fire ป้องกันการสตั้นพวกเดียวกัน
- [x] ทำงานร่วมกับ [`EarthRoarSpell`](file:///d:/Minecraft/Dev/ironspell_more/BHSpells/src/main/java/net/offkung/bhspells/spells/ground/EarthRoarSpell.java) และระบบ Universal Control ใน BHSpells
