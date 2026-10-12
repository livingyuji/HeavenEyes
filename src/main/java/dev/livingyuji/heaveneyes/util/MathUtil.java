package dev.livingyuji.heaveneyes.util;

/**
 * Mathematical calculations for anti-cheat detections.
 */
public final class MathUtil {

    private MathUtil() {
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    public static float wrapAngleTo180(float angle) {
        float wrapped = angle % 360.0F;
        if (wrapped >= 180.0F) {
            wrapped -= 360.0F;
        }
        if (wrapped < -180.0F) {
            wrapped += 360.0F;
        }
        return wrapped;
    }

    public static float getAngleDistance(float alpha, float beta) {
        float phi = Math.abs(beta - alpha) % 360.0F;
        return phi > 180.0F ? 360.0F - phi : phi;
    }
}
