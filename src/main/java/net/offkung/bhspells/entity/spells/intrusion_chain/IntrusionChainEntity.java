package net.offkung.bhspells.entity.spells.intrusion_chain;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.particle.SparkParticleOptions;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.network.NetworkHooks;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.client.particle.ColoredCherryParticleOption;
import net.offkung.bhspells.registry.EntityRegistry;
import net.offkung.bhspells.registry.ParticleRegistry;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class IntrusionChainEntity extends Entity implements AntiMagicSusceptible, IEntityAdditionalSpawnData {
    public enum Type {
        BUFF(BHSpells.id("textures/entity/intrusion_chain/buff_chain.png"), new Vector3f(0.25f, 1.0f, 0.35f)),
        DEBUFF(BHSpells.id("textures/entity/intrusion_chain/debuff_chain.png"), new Vector3f(0.66f, 0.18f, 0.9f));

        private final ResourceLocation texture;
        private final Vector3f sparkColor;

        Type(ResourceLocation texture, Vector3f sparkColor) {
            this.texture = texture;
            this.sparkColor = sparkColor;
        }

        public ResourceLocation getTexture() {
            return this.texture;
        }

        public Vector3f getSparkColor() {
            return this.sparkColor;
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private static final int SEGMENT_COUNT = 20;
    private static final float SEGMENT_SIZE = 0.15f;
    public static final int VISUAL_WARMUP_TIME = 3;
    public static final float MAX_SAG_DISTANCE = 8.0f;
    public static final float SAG_SCALE = 1.2f;

    @Nullable
    private UUID ownerUUID;
    @Nullable
    private Entity cachedOwner;

    @Nullable
    private UUID victimUUID;
    @Nullable
    private Entity cachedVictim;

    public int warmup;

    private Type chainType = Type.BUFF;

    private final IntrusionChainPart[] parts;

    private boolean partsPlaced;

    public IntrusionChainEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.parts = new IntrusionChainPart[SEGMENT_COUNT];
        for (int i = 0; i < SEGMENT_COUNT; i++) {
            this.parts[i] = new IntrusionChainPart(this, SEGMENT_SIZE, SEGMENT_SIZE);
        }
        this.setId(ENTITY_COUNTER.getAndAdd(this.parts.length + 1) + 1);
        this.noCulling = true;
        this.noPhysics = true;
    }

    public IntrusionChainEntity(Level level, Entity owner, Entity victim, Type chainType) {
        this(EntityRegistry.INTRUSION_CHAIN.get(), level);
        this.chainType = chainType;
        setOwner(owner);
        setVictim(victim);
        setPos(owner.getX(), owner.getY(), owner.getZ());
        repositionParts();
    }

    public Type getChainType() {
        return this.chainType;
    }

    public Vec3 getBodyAnchor() {
        Entity owner = getOwner();
        double bodyHeight = owner != null ? owner.getBbHeight() * 0.5 : 1.0;
        return position().add(0.0, bodyHeight, 0.0);
    }

    public void setOwner(@Nullable Entity owner) {
        if (owner != null) {
            this.ownerUUID = owner.getUUID();
            this.cachedOwner = owner;
        }
    }

    @Nullable
    public Entity getOwner() {
        if (this.cachedOwner != null && !this.cachedOwner.isRemoved()) {
            return this.cachedOwner;
        } else if (this.ownerUUID != null && this.level() instanceof ServerLevel serverLevel) {
            this.cachedOwner = serverLevel.getEntity(this.ownerUUID);
            return this.cachedOwner;
        }
        return null;
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        return super.isAlliedTo(entity) || entity.getUUID().equals(ownerUUID);
    }

    public void setVictim(@Nullable Entity victim) {
        if (victim != null) {
            this.victimUUID = victim.getUUID();
            this.cachedVictim = victim;
        }
    }

    @Nullable
    public Entity getVictim() {
        if (this.cachedVictim != null && !this.cachedVictim.isRemoved()) {
            return this.cachedVictim;
        } else if (this.victimUUID != null && this.level() instanceof ServerLevel serverLevel) {
            Entity e = serverLevel.getEntity(this.victimUUID);
            if (e instanceof LivingEntity living) {
                this.cachedVictim = living;
                return living;
            }
        }
        return null;
    }


    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public PartEntity<?>[] getParts() {
        return parts;
    }

    @Override
    public void setId(int id) {
        super.setId(id);
        for (int i = 0; i < this.parts.length; i++) {
            this.parts[i].setId(id + i + 1);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (invulnerableTime > 0) {
            invulnerableTime--;
        }
        if (warmup < VISUAL_WARMUP_TIME) {
            warmup++;
        }
        if (!level().isClientSide) {
            Entity owner = getOwner();
            Entity victim = getVictim();
            if (owner == null || victim == null || victim.isRemoved()) {
                forceBreak();
                return;
            }
            setPos(owner.getX(), owner.getY(), owner.getZ());
        } else {
            chainParticlesAround(this.position(), 3);
            if (parts != null && parts.length > 0) {
                chainParticlesAround(parts[parts.length - 1].position(), 1);
            }
        }

        repositionParts();
    }

    private void chainParticlesAround(Vec3 pos, int count) {
        for (int i = 0; i < count; i++) {
            Vec3 random = Utils.getRandomVec3(0.1);

            if (this.chainType == Type.DEBUFF) {
                level().addParticle(ParticleHelper.UNSTABLE_ENDER, pos.x + random.x, pos.y + random.y, pos.z + random.z, random.x, random.y, random.z);
            } else {
                DustColorTransitionOptions greenDust = new DustColorTransitionOptions(new Vector3f(0.25f, 1.0f, 0.35f), new Vector3f(0.51F, 1F, 0.65F), 1.5F);
                level().addParticle(greenDust, pos.x + random.x, pos.y + random.y, pos.z + random.z, random.x, random.y, random.z);
            }
        }
    }

    private void repositionParts() {
        Entity victim = getVictim();
        Vec3 start = getBodyAnchor();
        Vec3 end = victim != null ? victim.position().add(0, victim.getBbHeight() * 0.5, 0) : start.add(0, 1, 0);
        int totalLinks = parts.length + 1;

        float distance = (float) start.distanceTo(end);
        float sagAmount = Math.max(0, 1.0f - distance / MAX_SAG_DISTANCE) * SAG_SCALE;

        for (int i = 0; i < parts.length; i++) {
            float t = (float) (i + 1) / totalLinks;
            Vec3 pos = start.lerp(end, t);
            pos = pos.add(0, -sagAmount * 4.0 * t * (1.0 - t), 0);

            if (partsPlaced) {
                parts[i].xo = parts[i].xOld = parts[i].getX();
                parts[i].yo = parts[i].yOld = parts[i].getY();
                parts[i].zo = parts[i].zOld = parts[i].getZ();
            }
            parts[i].setPos(pos);
            if (!partsPlaced) {
                // first placement: no interpolation sweep from the default (0,0,0) spawn position
                parts[i].xo = parts[i].xOld = pos.x;
                parts[i].yo = parts[i].yOld = pos.y;
                parts[i].zo = parts[i].zOld = pos.z;
            }
        }

        partsPlaced = true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (invulnerableTime > 0 || level().isClientSide || isInvulnerableTo(source)) {
            return false;
        }
        if (DamageSources.isFriendlyFireBetween(source.getEntity(), getOwner())) {
            return false;
        }
        if (!canBreak()) {
            return false; // debuff chain can't be severed by attacking it directly
        }
        invulnerableTime = 10;
        breakChain();
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    public boolean canBreak() {
        if (this.chainType == Type.BUFF) {
            return true;
        }
        Entity owner = getOwner();
        return !(owner instanceof LivingEntity livingOwner) || livingOwner.getHealth() <= 1.0F;
    }

    public void breakChain() {
        if (!canBreak()) return;
        forceBreak();
    }

    public void forceBreakPublic() {
        forceBreak();
    }

    public void forceBreak() {
        if (!level().isClientSide) {
            playSound(SoundEvents.CHAIN_BREAK);
            if (parts != null && parts.length > 0) {
                SparkParticleOptions sparkParticle = new SparkParticleOptions(chainType.getSparkColor());
                for (IntrusionChainPart part : parts) {
                    Vec3 partPos = part.position();
                    MagicManager.spawnParticles(level(), sparkParticle, partPos.x, partPos.y, partPos.z, 2, 0.08, 0.08, 0.08, 0.05, false);
                }
                Vec3 start = getBodyAnchor();
                Vec3 end = parts[parts.length - 1].position();
                int count = 4;
                for (int i = 0; i < count; i++) {
                    Vec3 vec3 = start.lerp(end, i / (float) count);
                    MagicManager.spawnParticles(level(), sparkParticle, vec3.x, vec3.y, vec3.z, 5, 0.05, 0.05, 0.05, 0.15, false);
                }
            }
            this.discard();
        }
    }

    @Override
    public void onAntiMagic(MagicData playerMagicData) {
        breakChain();
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUUID != null) {
            tag.putUUID("Owner", ownerUUID);
        }
        if (victimUUID != null) {
            tag.putUUID("Victim", victimUUID);
        }
        tag.putString("ChainType", chainType.name());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            this.ownerUUID = tag.getUUID("Owner");
            this.cachedOwner = null;
        }
        if (tag.hasUUID("Victim")) {
            this.victimUUID = tag.getUUID("Victim");
            this.cachedVictim = null;
        }
        if (tag.contains("ChainType")) {
            try {
                this.chainType = Type.valueOf(tag.getString("ChainType"));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        var owner = getOwner();
        buffer.writeInt(owner == null ? 0 : owner.getId());
        var victim = getVictim();
        buffer.writeInt(victim == null ? 0 : victim.getId());
        buffer.writeEnum(this.chainType);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        Entity owner = this.level().getEntity(additionalData.readInt());
        if (owner != null) {
            this.setOwner(owner);
        }
        Entity victim = this.level().getEntity(additionalData.readInt());
        if (victim != null) {
            this.cachedVictim = victim;
            this.victimUUID = victim.getUUID();
        }
        this.chainType = additionalData.readEnum(Type.class);
        repositionParts();
    }
}
