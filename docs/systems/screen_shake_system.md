# ระบบการสั่นสะเทือนหน้าจอแบบ Native (Screen Shake Network & Camera Pipeline)

เอกสารนี้อธิบายสถาปัตยกรรมการส่งข้อมูลเครือข่ายและการเรนเดอร์มุมกล้องสั่นไหวของระบบ **Screen Shake** ในม็อด BHSpells ซึ่งถูกออกแบบใหม่ให้ทำงานแบบ Native โดยตรง ไม่พึ่งพาคลาสจากม็อดภายนอก (เช่น TravelOptics) ทำให้ปลอดภัยต่อทั้งไคลเอนต์และเซิร์ฟเวอร์มัลติเพลเยอร์

---

## 1. วัตถุประสงค์และโครงสร้าง (Overview)

1. **Native Network Packet:** ควบคุมการส่งข้อมูลแรงสั่นสะเทือนจาก Server สู่ Client ผ่าน Forge Network Pipeline ด้วย [`ScreenShakePacket.java`](file:///d:/Minecraft/Dev/ironspell_more/BHSpells/src/main/java/net/offkung/bhspells/network/server/ScreenShakePacket.java)
2. **Camera Shake Manipulation:** ปรับแต่งมุมมองกล้องของไคลเอนต์ผ่าน Forge Viewport Event ด้วย [`ClientScreenShakeEvent.java`](file:///d:/Minecraft/Dev/ironspell_more/BHSpells/src/main/java/net/offkung/bhspells/client/event/ClientScreenShakeEvent.java)
3. **Smooth Exponential Decay:** ลดทอนความสั่นไหวอย่างนุ่มนวลทุกๆ Tick เพื่อไม่ให้เกิดอาการกระตุกหรือเวียนศีรษะ

---

## 2. โปรโตคอลเครือข่าย (Network Protocol)

แพ็กเก็ตถูกลงทะเบียนใน `PacketHandler.java` ด้วยทิศทาง `PLAY_TO_CLIENT`:

```java
public class ScreenShakePacket {
    private final float power;
    private final double x;
    private final double y;
    private final double z;

    public ScreenShakePacket(float power, Vec3 center) {
        this.power = power;
        this.x = center.x;
        this.y = center.y;
        this.z = center.z;
    }
}
```

- **Payload:** บรรจุค่าความแรง (`power: float`) และพิกัดศูนย์กลาง (`x, y, z: double`) ขนาดข้อมูลเพียง 28 bytes ต่อแพ็กเก็ต
- **Client Gate:** ประมวลผลผ่าน `@OnlyIn(Dist.CLIENT)` และ `ctx.get().enqueueWork()` เพื่อความปลอดภัยของ Thread

---

## 3. การคำนวณมุมกล้องและการลดทอนแรงสั่น (Camera Noise & Decay)

ระบบฝั่งไคลเอนต์ดักจับ Event สองจุด:

1. **การเขย่ามุมกล้อง (`ViewportEvent.ComputeCameraAngles`):**
   ```java
   float noiseX = (random.nextFloat() - 0.5F) * shakeIntensity * 1.2F;
   float noiseY = (random.nextFloat() - 0.5F) * shakeIntensity * 1.2F;
   event.setPitch(event.getPitch() + noiseX);
   event.setYaw(event.getYaw() + noiseY);
   event.setRoll(event.getRoll() + (random.nextFloat() - 0.5F) * shakeIntensity * 0.8F);
   ```
   เพิ่มสัญญาณรบกวน (Noise) ทั้งในแนว Pitch (เงย-ก้ม), Yaw (หันซ้าย-ขวา) และ Roll (เอียงข้าง) ทำให้ได้ความรู้สึกแรงกระแทกจากระเบิดหรือเวทมนตร์อย่างสมจริง

2. **การลดทอนแรงสั่น (`TickEvent.ClientTickEvent`):**
   ```java
   shakeIntensity *= 0.92F;
   if (shakeIntensity < 0.01F) {
       shakeIntensity = 0.0F;
   }
   ```
   ลดทอนความเข้มข้นลง 8% ต่อ Tick (Exponential Decay Factor 0.92) ทำให้มุมกล้องกลับคืนสู่สภาพเดิมอย่างเป็นธรรมชาติภายใน 15-25 Ticks

---

## 4. ผู้เรียกใช้งานหลักในระบบ (System Callers)

- [`PressureScreenShakeHelper.java`](file:///d:/Minecraft/Dev/ironspell_more/BHSpells/src/main/java/net/offkung/bhspells/pressure/server/PressureScreenShakeHelper.java): ส่งสัญญาณสั่นสะเทือนเมื่อเกิดแรงดันวิญญาณปะทะหรือระเบิดอาณาเขต
- [`CrimsonRainBathesMoonSpell.java`](file:///d:/Minecraft/Dev/ironspell_more/BHSpells/src/main/java/net/offkung/bhspells/spells/aqua/CrimsonRainBathesMoonSpell.java): สั่นไหวหน้าจอในจังหวะพายุหมุนและหอกสายฝนตกกระทบ
- [`EarthRoarSpell.java`](file:///d:/Minecraft/Dev/ironspell_more/BHSpells/src/main/java/net/offkung/bhspells/spells/ground/EarthRoarSpell.java): สั่นสะเทือนเมื่อเกิดแผ่นดินไหวคำราม
