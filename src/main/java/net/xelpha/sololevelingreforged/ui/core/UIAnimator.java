package net.xelpha.sololevelingreforged.ui.core;

/**
 * Animation utilities for smooth UI transitions
 * Provides easing functions and animation state management
 */
public final class UIAnimator {
    
    private UIAnimator() {} // Prevent instantiation
    
    // ══════════════════════════════════════════════════════════════════════════
    // EASING FUNCTIONS
    // ══════════════════════════════════════════════════════════════════════════
    
    /**
     * Linear interpolation (no easing)
     */
    public static float linear(float t) {
        return t;
    }
    
    /**
     * Ease in (slow start)
     */
    public static float easeIn(float t) {
        return t * t;
    }
    
    /**
     * Ease out (slow end)
     */
    public static float easeOut(float t) {
        return 1 - (1 - t) * (1 - t);
    }
    
    /**
     * Ease in-out (slow start and end)
     */
    public static float easeInOut(float t) {
        return t < 0.5f ? 2 * t * t : 1 - (float) Math.pow(-2 * t + 2, 2) / 2;
    }
    
    /**
     * Cubic ease in
     */
    public static float easeInCubic(float t) {
        return t * t * t;
    }
    
    /**
     * Cubic ease out
     */
    public static float easeOutCubic(float t) {
        return 1 - (float) Math.pow(1 - t, 3);
    }
    
    /**
     * Cubic ease in-out
     */
    public static float easeInOutCubic(float t) {
        return t < 0.5f ? 4 * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 3) / 2;
    }
    
    /**
     * Elastic ease out (bouncy overshoot)
     */
    public static float easeOutElastic(float t) {
        if (t == 0 || t == 1) return t;
        
        float c4 = (2 * (float) Math.PI) / 3;
        return (float) Math.pow(2, -10 * t) * (float) Math.sin((t * 10 - 0.75) * c4) + 1;
    }
    
    /**
     * Back ease out (slight overshoot)
     */
    public static float easeOutBack(float t) {
        float c1 = 1.70158f;
        float c3 = c1 + 1;
        return 1 + c3 * (float) Math.pow(t - 1, 3) + c1 * (float) Math.pow(t - 1, 2);
    }
    
    /**
     * Bounce ease out
     */
    public static float easeOutBounce(float t) {
        float n1 = 7.5625f;
        float d1 = 2.75f;
        
        if (t < 1 / d1) {
            return n1 * t * t;
        } else if (t < 2 / d1) {
            t -= 1.5f / d1;
            return n1 * t * t + 0.75f;
        } else if (t < 2.5 / d1) {
            t -= 2.25f / d1;
            return n1 * t * t + 0.9375f;
        } else {
            t -= 2.625f / d1;
            return n1 * t * t + 0.984375f;
        }
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // ANIMATION STATE CLASS
    // ══════════════════════════════════════════════════════════════════════════
    
    /**
     * Represents an animated value that smoothly transitions between states
     */
    public static class AnimatedValue {
        private float current;
        private float target;
        private float startValue;
        private long startTime;
        private long duration;
        private EasingFunction easing;
        private boolean active;
        
        public AnimatedValue(float initialValue) {
            this.current = initialValue;
            this.target = initialValue;
            this.active = false;
            this.easing = UIAnimator::easeOutCubic;
        }
        
        /**
         * Starts an animation to a target value
         */
        public void animateTo(float target, long durationMs) {
            animateTo(target, durationMs, UIAnimator::easeOutCubic);
        }
        
        /**
         * Starts an animation with custom easing
         */
        public void animateTo(float target, long durationMs, EasingFunction easing) {
            this.startValue = this.current;
            this.target = target;
            this.startTime = System.currentTimeMillis();
            this.duration = durationMs;
            this.easing = easing;
            this.active = true;
        }
        
        /**
         * Updates the animation state and returns the current value
         */
        public float get() {
            if (!active) return current;
            
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed >= duration) {
                current = target;
                active = false;
                return current;
            }
            
            float progress = (float) elapsed / duration;
            float easedProgress = easing.ease(progress);
            current = startValue + (target - startValue) * easedProgress;
            return current;
        }
        
        /**
         * Sets the value immediately without animation
         */
        public void set(float value) {
            this.current = value;
            this.target = value;
            this.active = false;
        }
        
        /**
         * Returns whether an animation is in progress
         */
        public boolean isAnimating() {
            if (active && System.currentTimeMillis() - startTime >= duration) {
                active = false;
                current = target;
            }
            return active;
        }
        
        /**
         * Gets the target value
         */
        public float getTarget() {
            return target;
        }
    }
    
    /**
     * Functional interface for easing functions
     */
    @FunctionalInterface
    public interface EasingFunction {
        float ease(float t);
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // PULSING ANIMATIONS
    // ══════════════════════════════════════════════════════════════════════════
    
    /**
     * Returns a value that pulses between 0 and 1
     */
    public static float pulse(long tick, float speed) {
        return (float) (Math.sin(tick * speed) * 0.5 + 0.5);
    }
    
    /**
     * Returns a value that pulses between min and max
     */
    public static float pulseRange(long tick, float speed, float min, float max) {
        float pulse = (float) (Math.sin(tick * speed) * 0.5 + 0.5);
        return min + (max - min) * pulse;
    }
    
    /**
     * Returns an alpha value for a breathing/pulsing effect
     */
    public static int pulseAlpha(long tick, float speed, int minAlpha, int maxAlpha) {
        float pulse = (float) (Math.sin(tick * speed) * 0.5 + 0.5);
        return (int) (minAlpha + (maxAlpha - minAlpha) * pulse);
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // INTERPOLATION HELPERS
    // ══════════════════════════════════════════════════════════════════════════
    
    /**
     * Smoothly interpolates a value towards a target (frame-rate independent)
     */
    public static float smoothDamp(float current, float target, float smoothTime, float deltaTime) {
        if (smoothTime <= 0) return target;
        
        float omega = 2f / smoothTime;
        float x = omega * deltaTime;
        float exp = 1f / (1f + x + 0.48f * x * x + 0.235f * x * x * x);
        
        float change = current - target;
        float temp = (change + omega * change * deltaTime) * exp;
        
        return target + temp;
    }
    
    /**
     * Linear interpolation between two values
     */
    public static float lerp(float a, float b, float t) {
        return a + (b - a) * Math.max(0, Math.min(1, t));
    }
    
    /**
     * Inverse lerp - returns how far value is between a and b (0-1)
     */
    public static float inverseLerp(float a, float b, float value) {
        if (Math.abs(b - a) < 0.0001f) return 0;
        return (value - a) / (b - a);
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // COLOR ANIMATION
    // ══════════════════════════════════════════════════════════════════════════
    
    /**
     * Smoothly interpolates between two colors
     */
    public static int lerpColor(int colorA, int colorB, float t) {
        return UIColors.lerp(colorA, colorB, t);
    }
    
    /**
     * Creates a color that cycles through a rainbow
     */
    public static int rainbowColor(long tick, float speed) {
        float hue = (tick * speed) % 360;
        return hsbToRgb(hue / 360f, 1.0f, 1.0f);
    }
    
    /**
     * Converts HSB to RGB color
     */
    public static int hsbToRgb(float hue, float saturation, float brightness) {
        int r = 0, g = 0, b = 0;
        
        if (saturation == 0) {
            r = g = b = (int) (brightness * 255.0f + 0.5f);
        } else {
            float h = (hue - (float) Math.floor(hue)) * 6.0f;
            float f = h - (float) Math.floor(h);
            float p = brightness * (1.0f - saturation);
            float q = brightness * (1.0f - saturation * f);
            float t = brightness * (1.0f - (saturation * (1.0f - f)));
            
            switch ((int) h) {
                case 0 -> { r = (int) (brightness * 255.0f + 0.5f); g = (int) (t * 255.0f + 0.5f); b = (int) (p * 255.0f + 0.5f); }
                case 1 -> { r = (int) (q * 255.0f + 0.5f); g = (int) (brightness * 255.0f + 0.5f); b = (int) (p * 255.0f + 0.5f); }
                case 2 -> { r = (int) (p * 255.0f + 0.5f); g = (int) (brightness * 255.0f + 0.5f); b = (int) (t * 255.0f + 0.5f); }
                case 3 -> { r = (int) (p * 255.0f + 0.5f); g = (int) (q * 255.0f + 0.5f); b = (int) (brightness * 255.0f + 0.5f); }
                case 4 -> { r = (int) (t * 255.0f + 0.5f); g = (int) (p * 255.0f + 0.5f); b = (int) (brightness * 255.0f + 0.5f); }
                case 5 -> { r = (int) (brightness * 255.0f + 0.5f); g = (int) (p * 255.0f + 0.5f); b = (int) (q * 255.0f + 0.5f); }
            }
        }
        
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}
