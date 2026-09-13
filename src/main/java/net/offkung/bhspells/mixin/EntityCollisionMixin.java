package net.offkung.bhspells.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.offkung.bhspells.effect.DragonFrostHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;

@Mixin(Entity.class)
public abstract class EntityCollisionMixin {
    @ModifyVariable(method = "collideBoundingBox", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static List<VoxelShape> bhspells$airWalkCollision(List<VoxelShape> shapes, Entity entity, Vec3 movement, AABB bb, Level level) {
        if (entity instanceof Player player && DragonFrostHandler.shouldProvideAirFloor(player, movement, bb)) {
            List<VoxelShape> newShapes = new ArrayList<>(shapes);
            double floorY = bb.minY;
            AABB floorAabb = new AABB(bb.minX - 1.0, floorY - 0.5, bb.minZ - 1.0, bb.maxX + 1.0, floorY, bb.maxZ + 1.0);
            newShapes.add(Shapes.create(floorAabb));
            return newShapes;
        }
        return shapes;
    }
}
