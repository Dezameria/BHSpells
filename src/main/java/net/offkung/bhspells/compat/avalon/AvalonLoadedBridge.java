package net.offkung.bhspells.compat.avalon;

import net.offkung.bhspells.compat.api.CompatResult;
import net.offkung.bhspells.compat.api.VfxRequest;
import net.offkung.bhspells.compat.avalon.particle.AvalonVfx;

public final class AvalonLoadedBridge {
    private AvalonLoadedBridge() {
    }

    static CompatResult spawnVfx(VfxRequest request) {
        return AvalonVfx.spawnVfx(request);
    }
}
