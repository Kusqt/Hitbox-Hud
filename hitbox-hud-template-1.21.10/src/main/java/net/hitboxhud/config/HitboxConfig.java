package net.hitboxhud.config;

/**
 * ================================================================
 *  ALL USER-TWEAKABLE VALUES LIVE IN THIS FILE.
 *  Colors are 0-255 int channels (r, g, b, a). Distances are in blocks.
 * ================================================================
 */
public final class HitboxConfig {

    private HitboxConfig() {}

    // ---------------------------------------------------------------
    // 1. DISTANCE THRESHOLDS (blocks, measured eye-pos -> target pos)
    // ---------------------------------------------------------------
    public static double FAR_THRESHOLD = 3.0;   // beyond this -> "out of reach" gray
    public static double EDGE_THRESHOLD = 2.5;  // between EDGE and FAR -> yellow, below EDGE -> green

    // ---------------------------------------------------------------
    // 2. WIREFRAME COLORS  {r, g, b, a}  (0-255)
    // ---------------------------------------------------------------
    public static int[] COLOR_OUT_OF_REACH = {150, 150, 150, 140}; // desaturated gray
    public static int[] COLOR_EDGE_RANGE   = {255, 235, 20, 230};  // bright yellow
    public static int[] COLOR_IN_REACH     = {40, 255, 60, 235};   // bright green

    // ---------------------------------------------------------------
    // 3. TRANSLUCENT BODY FILL  {r, g, b, a}  (0-255)
    //    alpha 38/255 ~= 15% opacity, as requested.
    // ---------------------------------------------------------------
    public static int[] FILL_COLOR = {255, 25, 25, 38};

    // ---------------------------------------------------------------
    // 4. TOP-FACE FACING INDICATOR (replaces the vanilla look-vector line)
    // ---------------------------------------------------------------
    public static int[] TOP_CAP_COLOR = {25, 235, 235, 110};

    // ---------------------------------------------------------------
    // 5. MISC
    // ---------------------------------------------------------------
    public static boolean ENABLE_FILL = true;
    public static boolean ENABLE_TOP_CAP = true;
    public static boolean ENABLE_WIREFRAME = true;

    /** Returns the wireframe color for the given eye-to-target distance. */
    public static int[] colorForDistance(double distance) {
        if (distance > FAR_THRESHOLD) {
            return COLOR_OUT_OF_REACH;
        } else if (distance >= EDGE_THRESHOLD) {
            return COLOR_EDGE_RANGE;
        } else {
            return COLOR_IN_REACH;
        }
    }
}
