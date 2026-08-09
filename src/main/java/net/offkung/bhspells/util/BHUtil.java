package net.offkung.bhspells.util;

import com.gametechbc.traveloptics.api.utils.TOGeneralUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import org.joml.Vector3f;

public class BHUtil {
    public static boolean isAlly(LivingEntity owner, LivingEntity target) {
        return owner.getTeam() != null && owner.getTeam().isAlliedTo(target.getTeam());
    }

    public static boolean isTamed(LivingEntity target) {
        if (target instanceof TamableAnimal tamableAnimal) {
            return tamableAnimal.isTame();
        } else {
            return false;
        }
    }
    public static Vector3f hexToVector3f(String hexColor) {
        String cleanHex = hexColor.startsWith("#") ? hexColor.substring(1) : hexColor;
        int rgb = Integer.parseInt(cleanHex, 16);
        float red = (float)(rgb >> 16 & 255) / 255.0F;
        float green = (float)(rgb >> 8 & 255) / 255.0F;
        float blue = (float)(rgb & 255) / 255.0F;
        return new Vector3f(red, green, blue);
    }

    public static Vector3f hexToVector3f(int hexColor) {
        float red = (float)(hexColor >> 16 & 255) / 255.0F;
        float green = (float)(hexColor >> 8 & 255) / 255.0F;
        float blue = (float)(hexColor & 255) / 255.0F;
        return new Vector3f(red, green, blue);
    }
}
