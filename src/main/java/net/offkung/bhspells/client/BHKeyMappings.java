package net.offkung.bhspells.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class BHKeyMappings {
    public static final String KEY_CATEGORY = "key.category.bhspells";
    public static final KeyMapping DISMOUNT_GOLDEN_CLOUD = new KeyMapping(
            "key.bhspells.dismount_golden_cloud",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            KEY_CATEGORY
    );
}
