package com.mcbe.client;

/**
 * JNI bridge to the native client library.
 *
 * When you have real offsets from Ghidra, the native methods
 * below will return real values from Minecraft's memory.
 * Until then they return safe defaults so the app still runs.
 */
public class ClientBridge {

    static {
        try {
            System.loadLibrary("mcbe_client");
        } catch (UnsatisfiedLinkError e) {
            // Native lib not yet implemented - app still runs with defaults
        }
    }

    // -- Player state --
    public static native float[] getPlayerPosition();
    public static native float   getPlayerYaw();
    public static native float   getPlayerPitch();
    public static native boolean isOnGround();

    // -- Game state --
    public static native int     getCps();
    public static native float   getServerPing();
    public static native String  getDimensionName();

    // -- Module effects (called from native tick) --
    public static native void    setFullbright(boolean on);
    public static native void    setFpsBoost(boolean on);
    public static native void    setNoFog(boolean on);

    // -- Fallbacks when native lib is not loaded --
    static {
        // These will be overridden by the native implementations
        // once you fill in the offsets and rebuild
    }

    // Safe Java fallbacks (used until native lib is wired up)
    public static float[] getPlayerPositionSafe() {
        try { return getPlayerPosition(); }
        catch (UnsatisfiedLinkError e) { return new float[]{0f, 64f, 0f}; }
    }

    public static int getCpsSafe() {
        try { return getCps(); }
        catch (UnsatisfiedLinkError e) { return 0; }
    }

    public static float getServerPingSafe() {
        try { return getServerPing(); }
        catch (UnsatisfiedLinkError e) { return 0f; }
    }
}
