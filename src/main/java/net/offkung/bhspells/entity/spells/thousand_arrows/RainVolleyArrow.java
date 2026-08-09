package net.offkung.bhspells.entity.spells.thousand_arrows;

import io.redspace.ironsspellbooks.entity.spells.small_magic_arrow.SmallMagicArrow;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

public class RainVolleyArrow extends SmallMagicArrow {
    private RainVolleyEntity sourceVolley;

    public RainVolleyArrow(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public RainVolleyArrow(Level levelIn, Entity shooter) {
        super(levelIn, shooter);
    }

    public void setSourceVolley(RainVolleyEntity sourceVolley) {
        this.sourceVolley = sourceVolley;
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult entityHitResult) {
        Entity entity = entityHitResult.getEntity();

        super.onHitEntity(entityHitResult); // original damage/knockback/bounce logic

        if (!level().isClientSide && entity instanceof LivingEntity livingEntity && sourceVolley != null) {
            sourceVolley.addStack(livingEntity);
        }

        if (!level().isClientSide) {
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        super.onHitBlock(pResult);

        if (!level().isClientSide) {
            this.discard();
        }
    }
}
