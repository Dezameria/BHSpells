package net.offkung.bhspells.spells.aqua;

import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;

public class InfiniteRecastInstance extends RecastInstance {
    public InfiniteRecastInstance() {
        super();
    }

    public InfiniteRecastInstance(String spellId, int spellLevel, int ticksToLive, CastSource castSource, int remainingRecasts) {
        super(spellId, spellLevel, 1, ticksToLive, castSource, null);
        this.remainingRecasts = remainingRecasts;
        this.totalRecasts = 1;
    }

    public void setRemainingRecasts(int remainingRecasts) {
        this.remainingRecasts = remainingRecasts;
    }
}
