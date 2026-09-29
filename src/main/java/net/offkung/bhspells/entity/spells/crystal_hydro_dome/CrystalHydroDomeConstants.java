package net.offkung.bhspells.entity.spells.crystal_hydro_dome;

public final class CrystalHydroDomeConstants {

    private CrystalHydroDomeConstants() {
    }

    public static final double RADIUS = 10.0;
    public static final double RADIUS_SQUARED = RADIUS * RADIUS;
    public static final double HEIGHT = 10.0;
    public static final double BELOW_CENTER_TOLERANCE = -1.0;

    public static final String DOME_TAG = "pers_liming_dome";
    public static final String REFLECTED_TAG = "bhspells_crystal_hydro_dome_reflected";

    public static final int DURATION_TICKS = 120;
    public static final int FALLBACK_END_GRACE_TICKS = 5;
    public static final int END_LINGER_TICKS = 30;
    public static final double DOME_HP = 150.0;

    public static final double OPEN_HEAL = 50.0;
    public static final double END_HEAL = 40.0;

    public static final double INSIDE_DOME_SHARE = 0.30;
    public static final double COUNTER_RATIO = 0.50;

    public static final int DEFAULT_IFRAME_WINDOW_TICKS = 10;

    public static final int STOP_REAPPLY_DURATION_TICKS = 5;

    public static final double PROJECTILE_SCAN_MARGIN = 8.0;
    public static final double SEGMENT_SAMPLE_STEP = 0.25;

    public static final double KNOCKBACK_RING_INNER_RADIUS = 10.0;
    public static final double KNOCKBACK_RING_OUTER_RADIUS = 13.0;
    public static final double KNOCKBACK_RING_BELOW = -2.0;
    public static final double KNOCKBACK_RING_ABOVE = 11.0;

    public static final double KNOCKBACK_STRENGTH = 1.5;

    public static final int HP_READOUT_INTERVAL_TICKS = 10;
    public static final double HP_READOUT_LOW_THRESHOLD = 0.30;
}
