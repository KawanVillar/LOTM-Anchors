package com.kawan.lotmanchors.anchor;

/** Anchor pressure is a ceiling, not repeated damage or automatic healing. */
public final class AnchorSanityRules {
    private AnchorSanityRules() {}
    public static double deficit(long anchors, long required) {
        if (required <= 0 || anchors >= required) return 0;
        return Math.clamp(1.0 - (double) Math.max(0, anchors) / required, 0, 1);
    }
    public static float ceiling(float nativeMaximum, long anchors, long required, double maxPenalty) {
        if (!Float.isFinite(nativeMaximum) || nativeMaximum <= 0) return 0;
        double penalty = Double.isFinite(maxPenalty) ? Math.clamp(maxPenalty, 0, 1) : 0;
        return (float) (nativeMaximum * (1 - penalty * deficit(anchors, required)));
    }
    public static float reduceToward(float current, float ceiling, float nativeMaximum,
                                     double maxDropPerSecond, int elapsedSeconds) {
        if (!Float.isFinite(current) || !Float.isFinite(ceiling) || current <= ceiling
                || !Float.isFinite(nativeMaximum) || nativeMaximum <= 0
                || !Double.isFinite(maxDropPerSecond) || maxDropPerSecond <= 0 || elapsedSeconds <= 0) return current;
        double drop = Math.clamp(maxDropPerSecond, 0, 1) * nativeMaximum * elapsedSeconds;
        return (float) Math.max(ceiling, current - drop);
    }

    /** Timers use server ticks, so time spent offline never adds pressure. */
    public static final class Session {
        private long sinceTick = -1;
        private long lastCheck = -1;
        private int sequence = -1;
        private long deficitSince = -1;
        public long graceRemaining(long tick, int graceTicks) {
            return Math.max(0, graceTicks - (tick - sinceTick));
        }
        public long deficitGraceRemaining(long tick, int graceTicks) {
            return deficitSince < 0 ? graceTicks : Math.max(0, graceTicks - (tick - deficitSince));
        }
        public boolean ready(long tick, int currentSequence, int intervalTicks, int graceTicks) {
            if (sequence != currentSequence || tick < lastCheck) {
                sequence = currentSequence; sinceTick = tick; lastCheck = tick; deficitSince = -1;
                return graceTicks == 0;
            }
            if (lastCheck >= 0 && tick - lastCheck < intervalTicks) return false;
            lastCheck = tick;
            return tick - sinceTick >= graceTicks;
        }
        public boolean pressureReady(long tick, boolean deficit, int graceTicks) {
            if (!deficit) { deficitSince = -1; return false; }
            if (deficitSince < 0 || tick < deficitSince) deficitSince = tick;
            return tick - deficitSince >= graceTicks;
        }
    }

    /** One message per transition; fluctuating values within a stage do not spam chat. */
    public static final class Notices {
        private int sequence = -1;
        private int stage;
        public String update(int currentSequence, boolean deficit, long remainingTicks) {
            if (sequence != currentSequence) { sequence = currentSequence; stage = 0; }
            if (!deficit) {
                boolean recovered = stage != 0;
                stage = 0;
                return recovered ? "recovered" : "";
            }
            if (remainingTicks <= 0) {
                if (stage == 3) return "";
                stage = 3;
                return "active";
            }
            if (stage == 0) { stage = 1; return "deficit"; }
            if (remainingTicks <= 200 && stage == 1) { stage = 2; return "ending"; }
            return "";
        }
    }
}
