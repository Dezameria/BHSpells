package net.offkung.bhspells.compat.api;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record VfxRequest(LivingEntity source, Level level, Vec3 position, Vec3 direction, double radius, VfxCue cue) {
    public static VfxRequest at(Level level, Vec3 position, VfxCue cue) {
        return new VfxRequest(null, level, position, Vec3.ZERO, 1.0D, cue);
    }

    public static VfxRequest of(LivingEntity source, Vec3 position, double radius, VfxCue cue) {
        return new VfxRequest(source, source.level(), position, source.getLookAngle(), radius, cue);
    }
}
