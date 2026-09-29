package net.offkung.bhspells.entity.spells.crystal_hydro_dome;

import com.hm.efn.registries.EFNMobEffectRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import io.redspace.ironsspellbooks.entity.spells.AbstractConeProjectile;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.registry.BHSpellRegistry;
import net.offkung.bhspells.registry.EntityRegistry;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CrystalHydroDomeAoe extends AoeEntity {
    private static final int CLIENT_GLOW_INTERVAL = 3;
    private static final int CLIENT_GLOW_RISE_TICKS = 30;

    public static final int COUNTER_BOLT_SLOTS = 8;
    private static final EntityDataAccessor<Integer> DATA_COUNTER_SERIAL = SynchedEntityData.defineId(CrystalHydroDomeAoe.class, EntityDataSerializers.INT);
    @SuppressWarnings("unchecked")
    private static final EntityDataAccessor<Vector3f>[] DATA_COUNTER_TARGETS = new EntityDataAccessor[COUNTER_BOLT_SLOTS];
    static {
        for (int i = 0; i < COUNTER_BOLT_SLOTS; i++) {
            DATA_COUNTER_TARGETS[i] = SynchedEntityData.defineId(CrystalHydroDomeAoe.class, EntityDataSerializers.VECTOR3);
        }
    }

    public static final int END_STATE_ALIVE = 0;
    public static final int END_STATE_NATURAL = 1;
    public static final int END_STATE_BROKEN = 2;
    private static final EntityDataAccessor<Integer> DATA_END_STATE = SynchedEntityData.defineId(CrystalHydroDomeAoe.class, EntityDataSerializers.INT);

    private static final Set<CrystalHydroDomeAoe> ACTIVE_DOMES = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private double currentHp = CrystalHydroDomeConstants.DOME_HP;

    private boolean ended = false;

    private final Map<UUID, IFrameRecord> victimIFrames = new HashMap<>();

    private record IFrameRecord(long lastTick, double lastAmount) {
    }

    private boolean hpReadoutDirty = true;

    private int seenCounterSerial = 0;
    private final int[] counterBoltStartTick = new int[COUNTER_BOLT_SLOTS];
    private final int[] counterBoltSerial = new int[COUNTER_BOLT_SLOTS];
    private int endStartTick = 0;
    private int lingerTicks = 0;

    {
        Arrays.fill(counterBoltStartTick, Integer.MIN_VALUE / 2);
    }

    public CrystalHydroDomeAoe(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.setRadius((float) CrystalHydroDomeConstants.RADIUS);
    }

    public CrystalHydroDomeAoe(Level level) {
        this(EntityRegistry.CRYSTAL_HYDRO_DOME_AOE.get(), level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_COUNTER_SERIAL, 0);
        this.entityData.define(DATA_END_STATE, END_STATE_ALIVE);
        for (EntityDataAccessor<Vector3f> target : DATA_COUNTER_TARGETS) {
            this.entityData.define(target, new Vector3f());
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (key == DATA_END_STATE) {
            endStartTick = this.tickCount;
            return;
        }
        if (key != DATA_COUNTER_SERIAL || counterBoltStartTick == null || !this.level().isClientSide()) {
            return;
        }
        int serial = this.entityData.get(DATA_COUNTER_SERIAL);
        if (this.tickCount > 0) {
            for (int s = Math.max(seenCounterSerial, serial - COUNTER_BOLT_SLOTS); s < serial; s++) {
                int slot = Math.floorMod(s, COUNTER_BOLT_SLOTS);
                counterBoltStartTick[slot] = this.tickCount;
                counterBoltSerial[slot] = s;
            }
        }
        seenCounterSerial = serial;
    }

    public int getEndState() {
        return this.entityData.get(DATA_END_STATE);
    }

    public int getEndStartTick() {
        return endStartTick;
    }

    public int getCounterBoltStartTick(int slot) {
        return counterBoltStartTick[slot];
    }

    public int getCounterBoltSerial(int slot) {
        return counterBoltSerial[slot];
    }

    public Vector3f getCounterBoltOffset(int slot) {
        return this.entityData.get(DATA_COUNTER_TARGETS[slot]);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        ACTIVE_DOMES.add(this);
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        ACTIVE_DOMES.remove(this);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isRemoved()) {
            return;
        }
        if (this.level().isClientSide()) {
            if (getEndState() == END_STATE_ALIVE && this.tickCount % CLIENT_GLOW_INTERVAL == 0) {
                double angle = this.tickCount * 0.32;
                double radius = 0.7 + 0.25 * Math.sin(this.tickCount * 0.13);
                double height = 0.25 + 1.5 * (this.tickCount % CLIENT_GLOW_RISE_TICKS) / CLIENT_GLOW_RISE_TICKS;
                this.level().addParticle(ParticleTypes.GLOW, this.getX() + Math.cos(angle) * radius, this.getY() + height, this.getZ() + Math.sin(angle) * radius, -Math.sin(angle) * 0.012, 0.018, Math.cos(angle) * 0.012);
            }
            return;
        }

        if (ended) {
            lingerTicks++;
            if (getEndState() == END_STATE_BROKEN && (lingerTicks == 3 || lingerTicks == 7)) {
                playShatterTail(lingerTicks);
            }
            if (lingerTicks >= CrystalHydroDomeConstants.END_LINGER_TICKS) {
                this.discard();
            }
            return;
        }

        Entity owner = this.getOwner();
        if (!(owner instanceof LivingEntity caster) || !caster.isAlive() || caster.level() != this.level()) {
            endEarly();
            return;
        }
        if (currentHp <= 0) {
            endEarly();
            return;
        }
        if (!isInside(caster.position())) {
            endEarly();
            return;
        }

        scanProjectiles();
        flushHpReadout(caster);

        if (this.tickCount >= CrystalHydroDomeConstants.DURATION_TICKS + CrystalHydroDomeConstants.FALLBACK_END_GRACE_TICKS) {
            naturalEnd();
        }
    }

    @Override
    public void applyEffect(LivingEntity target) {
    }

    @Override
    public float getParticleCount() {
        return 0.0f;
    }

    @Override
    public Optional<ParticleOptions> getParticle() {
        return Optional.empty();
    }

    public static boolean isInside(Vec3 center, Vec3 point) {
        double dx = point.x - center.x;
        double dy = point.y - center.y;
        double dz = point.z - center.z;
        if (dy < CrystalHydroDomeConstants.BELOW_CENTER_TOLERANCE) {
            return false;
        }
        double verticalTerm = Math.max(dy, 0.0) * Math.max(dy, 0.0);
        return dx * dx + dz * dz + verticalTerm <= CrystalHydroDomeConstants.RADIUS_SQUARED;
    }

    public boolean isInside(Vec3 point) {
        return isInside(this.position(), point);
    }

    public static AABB searchBox(Vec3 center, double margin) {
        double r = CrystalHydroDomeConstants.RADIUS + margin;
        double bottom = CrystalHydroDomeConstants.BELOW_CENTER_TOLERANCE - margin;
        double top = CrystalHydroDomeConstants.HEIGHT + margin;
        return new AABB(center.x - r, center.y + bottom, center.z - r, center.x + r, center.y + top, center.z + r);
    }

    private <T extends LivingEntity> List<T> gatherInside(Class<T> type) {
        Vec3 center = this.position();
        return this.level().getEntitiesOfClass(type, searchBox(center, 0), e -> !(e instanceof ArmorStand) && e.isAlive() && isInside(e.position()));
    }

    public static CrystalHydroDomeAoe findDomeContaining(LivingEntity victim) {
        for (CrystalHydroDomeAoe dome : ACTIVE_DOMES) {
            if (dome.isRemoved() || dome.level() != victim.level()) {
                continue;
            }
            if (dome.isInside(victim.position())) {
                return dome;
            }
        }
        return null;
    }

    public static void endActiveDomeFor(LivingEntity caster) {
        for (CrystalHydroDomeAoe dome : List.copyOf(ACTIVE_DOMES)) {
            if (!dome.isRemoved() && dome.level() == caster.level() && dome.getOwner() == caster) {
                dome.endEarly();
            }
        }
    }

    public static void naturalEndFor(LivingEntity caster) {
        for (CrystalHydroDomeAoe dome : List.copyOf(ACTIVE_DOMES)) {
            if (!dome.isRemoved() && dome.level() == caster.level() && dome.getOwner() == caster) {
                dome.naturalEnd();
            }
        }
    }

    public double absorb(double amount) {
        double actual = Math.min(Math.max(amount, 0.0), currentHp);
        if (actual > 0) {
            currentHp -= actual;
            hpReadoutDirty = true;
        }
        return actual;
    }

    public double getCurrentHp() {
        return currentHp;
    }

    public void applyCounter(Entity attackerOrOwner, double domeAbsorbed) {
        if (!(attackerOrOwner instanceof Player player) || domeAbsorbed <= 0) {
            return;
        }
        Entity caster = this.getOwner();
        if (caster == null) {
            return;
        }
        double counterDamage = domeAbsorbed * CrystalHydroDomeConstants.COUNTER_RATIO;
        DamageSources.applyDamage(player, (float) counterDamage, getDamageSource(caster));
        pushCounterBolt(player);
    }

    private void pushCounterBolt(Entity target) {
        int serial = this.entityData.get(DATA_COUNTER_SERIAL);
        Vector3f offset = new Vector3f((float) (target.getX() - this.getX()), (float) (target.getY() + target.getBbHeight() * 0.5 - this.getY()), (float) (target.getZ() - this.getZ()));
        this.entityData.set(DATA_COUNTER_TARGETS[Math.floorMod(serial, COUNTER_BOLT_SLOTS)], offset);
        this.entityData.set(DATA_COUNTER_SERIAL, serial + 1);
    }

    private DamageSource getDamageSource(Entity causingEntity) {
        return (BHSpellRegistry.CRYSTAL_HYDRO_DOME.get()).getDamageSource(this, causingEntity);
    }

    public double gateOutsideHit(LivingEntity victim, DamageSource source, double amount) {
        if (source.is(DamageTypeTags.BYPASSES_COOLDOWN)) {
            recordIFrame(victim, amount);
            return amount;
        }
        int window = iFrameWindowTicks(source);
        if (window <= 0) {
            recordIFrame(victim, amount);
            return amount;
        }
        long now = this.level().getGameTime();
        IFrameRecord previous = victimIFrames.get(victim.getUUID());
        if (previous != null && (now - previous.lastTick()) < window) {
            if (amount <= previous.lastAmount()) {
                return 0.0;
            }
            double delta = amount - previous.lastAmount();
            victimIFrames.put(victim.getUUID(), new IFrameRecord(previous.lastTick(), amount));
            return delta;
        }
        recordIFrame(victim, amount);
        return amount;
    }

    private void recordIFrame(LivingEntity victim, double amount) {
        victimIFrames.put(victim.getUUID(), new IFrameRecord(this.level().getGameTime(), amount));
    }

    private static int iFrameWindowTicks(DamageSource source) {
        if (source instanceof SpellDamageSource spellDamageSource && spellDamageSource.getIFrames() >= 0) {
            return spellDamageSource.getIFrames();
        }
        return CrystalHydroDomeConstants.DEFAULT_IFRAME_WINDOW_TICKS;
    }

    private void flushHpReadout(LivingEntity caster) {
        boolean periodic = this.tickCount % CrystalHydroDomeConstants.HP_READOUT_INTERVAL_TICKS == 0;
        if (!hpReadoutDirty && !periodic) {
            return;
        }
        hpReadoutDirty = false;
        if (caster instanceof ServerPlayer player && isOnline(player)) {
            player.displayClientMessage(buildHpReadoutComponent(), true);
        }
    }

    private Component buildHpReadoutComponent() {
        int hp = Math.max(0, (int) Math.ceil(currentHp));
        int max = (int) Math.ceil(CrystalHydroDomeConstants.DOME_HP);
        boolean low = currentHp < CrystalHydroDomeConstants.DOME_HP * CrystalHydroDomeConstants.HP_READOUT_LOW_THRESHOLD;

        MutableComponent label = Component.translatable("ui.bhspells.crystal_hydro_dome_hp_label").withStyle(ChatFormatting.GREEN);
        MutableComponent heart = Component.literal("❤").withStyle(ChatFormatting.RED);
        MutableComponent value = Component.translatable("ui.bhspells.crystal_hydro_dome_hp_value", hp, max).withStyle(low ? ChatFormatting.RED : ChatFormatting.WHITE);

        return label.append(Component.literal(" ")).append(heart).append(Component.literal(" ")).append(value);
    }

    private void clearHpReadout() {
        Entity owner = this.getOwner();
        if (owner instanceof ServerPlayer player && isOnline(player)) {
            player.displayClientMessage(Component.empty(), true);
        }
    }

    private static boolean isOnline(ServerPlayer player) {
        return player.getServer() != null && player.getServer().getPlayerList().getPlayer(player.getUUID()) == player;
    }

    private void scanProjectiles() {
        Vec3 center = this.position();
        AABB box = searchBox(center, CrystalHydroDomeConstants.PROJECTILE_SCAN_MARGIN);
        List<Projectile> candidates = this.level().getEntitiesOfClass(Projectile.class, box, p -> true);
        for (Projectile projectile : candidates) {
            if (projectile == this || projectile instanceof AoeEntity || projectile instanceof AbstractConeProjectile || projectile instanceof ThrownEnderpearl) {
                continue;
            }
            if (projectile.getTags().contains(CrystalHydroDomeConstants.REFLECTED_TAG)) {
                continue;
            }

            Vec3 pos = projectile.position();
            if (isInside(center, pos)) {
                continue;
            }

            Vec3 to = pos.add(projectile.getDeltaMovement());
            if (!segmentEntersDome(center, pos, to)) {
                continue;
            }

            Entity owner = projectile.getOwner();
            if (owner != null && isInside(center, owner.position())) {
                continue;
            }

            handleIncomingProjectile(projectile, owner);
        }
    }

    private boolean segmentEntersDome(Vec3 center, Vec3 from, Vec3 to) {
        double length = from.distanceTo(to);
        if (length < 1.0e-6) {
            return isInside(center, to);
        }
        int steps = Math.max(1, (int) Math.ceil(length / CrystalHydroDomeConstants.SEGMENT_SAMPLE_STEP));
        for (int i = 1; i <= steps; i++) {
            Vec3 sample = from.lerp(to, (double) i / steps);
            if (isInside(center, sample)) {
                return true;
            }
        }
        return false;
    }

    private void handleIncomingProjectile(Projectile projectile, Entity owner) {
        double rawAmount;
        if (projectile instanceof AbstractArrow arrow) {
            rawAmount = predictArrowDamage(arrow);
            arrow.setDeltaMovement(arrow.getDeltaMovement().scale(-1.0));
            arrow.addTag(CrystalHydroDomeConstants.REFLECTED_TAG);
        } else if (projectile instanceof AbstractMagicProjectile magicProjectile) {
            rawAmount = magicProjectile.getDamage();
            projectile.discard();
        } else {
            rawAmount = 0.0;
            projectile.discard();
        }
        double absorbed = absorb(rawAmount);
        applyCounter(owner, absorbed);
    }

    private double predictArrowDamage(AbstractArrow arrow) {
        double velocityLength = arrow.getDeltaMovement().length();
        double base = Math.ceil(Mth.clamp(velocityLength * arrow.getBaseDamage(), 0.0, (double) Integer.MAX_VALUE));
        if (arrow.isCritArrow()) {
            int iBase = (int) base;
            double midpointBonus = (iBase / 2 + 1) / 2.0;
            base += midpointBonus;
        }
        return base;
    }

    private void endEarly() {
        finish(false);
    }

    private void naturalEnd() {
        finish(true);
    }

    private void finish(boolean natural) {
        if (ended) {
            return;
        }
        ended = true;

        if (natural && currentHp > 0) {
            healAndKnockback();
        }
        Entity owner = this.getOwner();
        if (owner instanceof LivingEntity livingOwner) {
            livingOwner.removeTag(CrystalHydroDomeConstants.DOME_TAG);
        }
        if (!natural && owner instanceof ServerPlayer serverPlayer) {
            Utils.serverSideCancelCast(serverPlayer);
        }
        clearHpReadout();
        victimIFrames.clear();
        ACTIVE_DOMES.remove(this);
        boolean broken = !natural || currentHp <= 0;
        this.entityData.set(DATA_END_STATE, broken ? END_STATE_BROKEN : END_STATE_NATURAL);
        playEndSound(broken);
    }

    private void playShatterTail(int tick) {
        double x = this.getX(), y = this.getY() + 2.0, z = this.getZ();
        if (tick == 3) {
            this.level().playSound(null, x, y, z, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 2.0f, 1.3f);
            this.level().playSound(null, x, y, z, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 2.0f, 1.2f);
        } else {
            this.level().playSound(null, x, y, z, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.5f, 1.7f);
            this.level().playSound(null, x, y, z, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.5f, 1.5f);
        }
    }

    private void playEndSound(boolean broken) {
        double x = this.getX(), y = this.getY() + 1.0, z = this.getZ();
        if (broken) {
            this.level().playSound(null, x, y, z, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 3.0f, 0.55f);
            this.level().playSound(null, x, y, z, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 3.0f, 0.9f);
            this.level().playSound(null, x, y, z, SoundRegistry.ICE_BLOCK_IMPACT.get(), SoundSource.PLAYERS, 3.0f, 0.9f);
            this.level().playSound(null, x, y, z, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 3.0f, 0.7f);
        } else {
            this.level().playSound(null, x, y, z, SoundRegistry.HOLY_CAST.get(), SoundSource.PLAYERS, 1.5f, 1.2f);
            this.level().playSound(null, x, y, z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 2.0f, 1.3f);
        }
    }

    private void healAndKnockback() {
        Vec3 center = this.position();

        for (LivingEntity target : gatherInside(LivingEntity.class)) {
            target.heal((float) CrystalHydroDomeConstants.END_HEAL);
        }

        double ringMargin = CrystalHydroDomeConstants.KNOCKBACK_RING_OUTER_RADIUS - CrystalHydroDomeConstants.RADIUS;
        AABB ringBox = searchBox(center, Math.max(ringMargin, CrystalHydroDomeConstants.KNOCKBACK_RING_ABOVE - CrystalHydroDomeConstants.HEIGHT));
        List<LivingEntity> ringTargets = this.level().getEntitiesOfClass(LivingEntity.class, ringBox, e -> !(e instanceof ArmorStand) && e.isAlive() && inKnockbackRing(center, e.position()));
        for (LivingEntity target : ringTargets) {
            Vec3 pos = target.position();
            double dx = center.x - pos.x;
            double dz = center.z - pos.z;
            target.knockback(CrystalHydroDomeConstants.KNOCKBACK_STRENGTH, dx, dz);
            if (target instanceof ServerPlayer serverPlayer) {
                serverPlayer.hurtMarked = true;
            }
        }
    }

    private static boolean inKnockbackRing(Vec3 center, Vec3 point) {
        double dy = point.y - center.y;
        if (dy < CrystalHydroDomeConstants.KNOCKBACK_RING_BELOW || dy > CrystalHydroDomeConstants.KNOCKBACK_RING_ABOVE) {
            return false;
        }
        double dx = point.x - center.x;
        double dz = point.z - center.z;
        double horizDistSq = dx * dx + dz * dz;
        double inner = CrystalHydroDomeConstants.KNOCKBACK_RING_INNER_RADIUS;
        double outer = CrystalHydroDomeConstants.KNOCKBACK_RING_OUTER_RADIUS;
        return horizDistSq >= inner * inner && horizDistSq <= outer * outer;
    }

    public static void cleanseHarmfulEffects(LivingEntity target) {
        for (MobEffectInstance instance : new ArrayList<>(target.getActiveEffects())) {
            if (instance.isInfiniteDuration()) {
                continue;
            }
            if (instance.getEffect().getCategory() != MobEffectCategory.HARMFUL) {
                continue;
            }
            target.removeEffect(instance.getEffect());
        }
        target.clearFire();
        target.setTicksFrozen(0);
    }
}
