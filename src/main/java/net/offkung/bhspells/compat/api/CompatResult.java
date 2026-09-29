package net.offkung.bhspells.compat.api;

public enum CompatResult {
    APPLIED,
    UNAVAILABLE,
    UNSUPPORTED,
    FAILED;

    public boolean isApplied() {
        return this == APPLIED;
    }
}
