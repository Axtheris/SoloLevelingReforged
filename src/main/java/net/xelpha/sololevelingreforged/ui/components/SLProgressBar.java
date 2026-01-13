package net.xelpha.sololevelingreforged.ui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.xelpha.sololevelingreforged.ui.core.UIAnimator;
import net.xelpha.sololevelingreforged.ui.core.UIColors;
import net.xelpha.sololevelingreforged.ui.core.UIRenderer;

/**
 * Styled progress bar component for the Solo Leveling System UI
 * Features animated fill, gradient options, and text overlay
 */
public class SLProgressBar implements UIComponent {
    
    private int x, y, width, height;
    private float value = 0;
    private float maxValue = 100;
    private boolean visible = true;
    
    // Styling
    private int barColor = UIColors.BAR_XP;
    private int barColorEnd = -1; // -1 means no gradient
    private int bgColor = UIColors.BAR_XP_BG;
    private int borderColor = UIColors.BORDER;
    private int textColor = UIColors.TEXT;
    
    // Display options
    private boolean showText = true;
    private boolean showPercentage = false;
    private String customText = null;
    private TextPosition textPosition = TextPosition.CENTER;
    
    // Animation
    private final UIAnimator.AnimatedValue animatedProgress = new UIAnimator.AnimatedValue(0);
    private boolean animate = true;
    
    public enum TextPosition {
        CENTER,
        LEFT,
        RIGHT,
        ABOVE,
        BELOW
    }
    
    public SLProgressBar(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) return;
        
        // Get animated or direct progress value
        float displayProgress = animate ? animatedProgress.get() : (maxValue > 0 ? value / maxValue : 0);
        displayProgress = Math.max(0, Math.min(1, displayProgress));
        
        // Draw background
        UIRenderer.fill(graphics, x, y, width, height, bgColor);
        
        // Draw progress fill
        int fillWidth = (int) (width * displayProgress);
        if (fillWidth > 0) {
            if (barColorEnd != -1) {
                // Gradient fill
                UIRenderer.fillGradientH(graphics, x, y, fillWidth, height, barColor, barColorEnd);
            } else {
                // Solid fill
                UIRenderer.fill(graphics, x, y, fillWidth, height, barColor);
            }
        }
        
        // Draw border
        UIRenderer.horizontalLine(graphics, x, y, width, borderColor);
        UIRenderer.horizontalLine(graphics, x, y + height - 1, width, borderColor);
        UIRenderer.verticalLine(graphics, x, y, height, borderColor);
        UIRenderer.verticalLine(graphics, x + width - 1, y, height, borderColor);
        
        // Draw shine effect on the filled portion
        if (fillWidth > 2) {
            int shineColor = UIColors.withAlpha(0xFFFFFFFF, 30);
            UIRenderer.fill(graphics, x + 1, y + 1, fillWidth - 2, Math.max(1, height / 3), shineColor);
        }
        
        // Draw text
        if (showText) {
            String text = getDisplayText();
            int textX, textY;
            
            switch (textPosition) {
                case LEFT -> {
                    textX = x + 4;
                    textY = y + (height - 8) / 2;
                }
                case RIGHT -> {
                    textX = x + width - UIRenderer.getTextWidth(text) - 4;
                    textY = y + (height - 8) / 2;
                }
                case ABOVE -> {
                    textX = x + (width - UIRenderer.getTextWidth(text)) / 2;
                    textY = y - 10;
                }
                case BELOW -> {
                    textX = x + (width - UIRenderer.getTextWidth(text)) / 2;
                    textY = y + height + 2;
                }
                default -> { // CENTER
                    textX = x + (width - UIRenderer.getTextWidth(text)) / 2;
                    textY = y + (height - 8) / 2;
                }
            }
            
            // Draw text with shadow for better readability
            UIRenderer.drawTextWithShadow(graphics, text, textX, textY, textColor);
        }
    }
    
    private String getDisplayText() {
        if (customText != null) {
            return customText;
        }
        
        if (showPercentage) {
            float progress = maxValue > 0 ? value / maxValue : 0;
            return String.format("%.0f%%", progress * 100);
        }
        
        // Default format: current/max
        if (value == (int) value && maxValue == (int) maxValue) {
            return String.format("%d/%d", (int) value, (int) maxValue);
        }
        return String.format("%.1f/%.1f", value, maxValue);
    }
    
    /**
     * Sets the current value with optional animation
     */
    public void setValue(float value) {
        this.value = value;
        float progress = maxValue > 0 ? value / maxValue : 0;
        
        if (animate) {
            animatedProgress.animateTo(progress, 300);
        } else {
            animatedProgress.set(progress);
        }
    }
    
    /**
     * Sets the maximum value
     */
    public void setMaxValue(float maxValue) {
        this.maxValue = maxValue;
        setValue(this.value); // Recalculate progress
    }
    
    /**
     * Sets both current and max values
     */
    public void setValues(float current, float max) {
        this.maxValue = max;
        setValue(current);
    }
    
    // Builder methods for styling
    
    public SLProgressBar withColors(int bar, int bg) {
        this.barColor = bar;
        this.bgColor = bg;
        return this;
    }
    
    public SLProgressBar withGradient(int start, int end) {
        this.barColor = start;
        this.barColorEnd = end;
        return this;
    }
    
    public SLProgressBar withBorder(int color) {
        this.borderColor = color;
        return this;
    }
    
    public SLProgressBar withText(boolean show, TextPosition position) {
        this.showText = show;
        this.textPosition = position;
        return this;
    }
    
    public SLProgressBar withPercentage(boolean show) {
        this.showPercentage = show;
        return this;
    }
    
    public SLProgressBar withCustomText(String text) {
        this.customText = text;
        return this;
    }
    
    public SLProgressBar withAnimation(boolean animate) {
        this.animate = animate;
        return this;
    }
    
    // Preset configurations for common use cases
    
    public static SLProgressBar healthBar(int x, int y, int width, int height) {
        return new SLProgressBar(x, y, width, height)
            .withGradient(UIColors.BAR_HEALTH, UIColors.brighten(UIColors.BAR_HEALTH, 1.3f))
            .withColors(UIColors.BAR_HEALTH, UIColors.BAR_HEALTH_BG);
    }
    
    public static SLProgressBar manaBar(int x, int y, int width, int height) {
        return new SLProgressBar(x, y, width, height)
            .withGradient(UIColors.BAR_MANA, UIColors.brighten(UIColors.BAR_MANA, 1.3f))
            .withColors(UIColors.BAR_MANA, UIColors.BAR_MANA_BG);
    }
    
    public static SLProgressBar xpBar(int x, int y, int width, int height) {
        return new SLProgressBar(x, y, width, height)
            .withGradient(UIColors.BAR_XP, UIColors.PRIMARY_GLOW)
            .withColors(UIColors.BAR_XP, UIColors.BAR_XP_BG);
    }
    
    // UIComponent implementation
    
    @Override public int getX() { return x; }
    @Override public int getY() { return y; }
    @Override public int getWidth() { return width; }
    @Override public int getHeight() { return height; }
    
    @Override
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    @Override public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    
    public float getValue() { return value; }
    public float getMaxValue() { return maxValue; }
}
