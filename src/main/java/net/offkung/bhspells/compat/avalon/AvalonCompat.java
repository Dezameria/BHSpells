package net.offkung.bhspells.compat.avalon;

import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.compat.CompatMods;
import net.offkung.bhspells.compat.api.CompatResult;
import net.offkung.bhspells.compat.api.VfxRequest;

public final class AvalonCompat {
    private static boolean linkageFailed;

    private AvalonCompat() {
    }

    public static boolean isAvailable() {
        return CompatMods.isAvalonLoaded() && !linkageFailed;
    }

    public static CompatResult spawnVfx(VfxRequest request) {
        if (!isAvailable() || request == null) {
            return CompatResult.UNAVAILABLE;
        }

        try {
            return AvalonLoadedBridge.spawnVfx(request);
        } catch (LinkageError error) {
            linkageFailed = true;
            BHSpells.LOGGER.error("Avalon VFX invocation failed due to linkage error", error);
            return CompatResult.FAILED;
        }
    }
}
