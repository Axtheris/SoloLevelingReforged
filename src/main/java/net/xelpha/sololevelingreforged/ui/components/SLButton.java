package net.xelpha.sololevelingreforged.ui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.xelpha.sololevelingreforged.ModSounds;
import net.xelpha.sololevelingreforged.ui.core.UIAnimator;
import net.xelpha.sololevelingreforged.ui.core.UIColors;
import net.xelpha.sololevelingreforged.ui.core.UIRenderer;

import java.util.function.Consumer;

/**
 * Styled button component for the Solo Leveling System UI
 * Features hover animations, click effects, and customizable appearance
 */
public class SLButton implements UIComponent {
    
    private int x, y, width, height;
    private String text;
    private Consumer<SLButton> onClick;
    private boolean enabled = true;
    private boolean visible = true;
    
    // Styling
    private int bgColor = UIColors.BG_PANEL;
    private int hoverColor = UIColors.BG_HOVER;
    private int borderColor = UIColors.BORDER;
    private int textColor = UIColors.TEXT;
    private int disabledColor = UIColors.TEXT_DISABLED;
    private int accentColor = UIColors.PRIMARY;
    
    // Animation state
    private final UIAnimator.AnimatedValue hoverProgress = new UIAnimator.AnimatedValue(0);
    private boolean wasHovered = false;
    private float clickScale = 1.0f;
    private long clickTime = 0;
    
    // Button styles
    public enum Style {
        DEFAULT,
        PRIMARY,
        SECONDARY,
        ACCENT,
        DANGER,
        SUCCESS,
        ICON_ONLY
    }
    
    private Style style = Style.DEFAULT;
    
    public SLButton(int x, int y, int width, int height, String text, Consumer<SLButton> onClick) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
        this.onClick = onClick;
    }
    
    /**
     * Creates a button with a specific style
     */
    public SLButton withStyle(Style style) {
        this.style = style;
        applyStyle();
        return this;
    }
    
    private void applyStyle() {
        switch (style) {
            case DEFAULT -> {
                // Use default colors
            }
            case PRIMARY -> {
                bgColor = UIColors.PRIMARY_DIM;
                hoverColor = UIColors.PRIMARY;
                borderColor = UIColors.PRIMARY;
                accentColor = UIColors.PRIMARY_GLOW;
            }
            case SECONDARY -> {
                bgColor = UIColors.SECONDARY_DIM;
                hoverColor = UIColors.SECONDARY;
                borderColor = UIColors.SECONDARY;
                accentColor = UIColors.SECONDARY;
            }
            case ACCENT -> {
                bgColor = 0xFF1A3A4A;
                hoverColor = UIColors.PRIMARY_DIM;
                borderColor = UIColors.PRIMARY;
                accentColor = UIColors.PRIMARY;
            }
            case DANGER -> {
                bgColor = 0xFF3A1A1A;
                hoverColor = 0xFF5A2A2A;
                borderColor = UIColors.TEXT_ERROR;
                accentColor = UIColors.TEXT_ERROR;
            }
            case SUCCESS -> {
                bgColor = 0xFF1A3A2A;
                hoverColor = 0xFF2A5A3A;
                borderColor = UIColors.TEXT_SUCCESS;
                accentColor = UIColors.TEXT_SUCCESS;
            }
            case ICON_ONLY -> {
                bgColor = UIColors.withAlpha(UIColors.BG_PANEL, 180);
                hoverColor = UIColors.BG_HOVER;
                borderColor = UIColors.BORDER;
                accentColor = UIColors.PRIMARY;
            }
        }
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) return;
        
        boolean hovered = isMouseOver(mouseX, mouseY) && enabled;
        
        // Update hover animation
        if (hovered != wasHovered) {
            hoverProgress.animateTo(hovered ? 1f : 0f, 150);
            wasHovered = hovered;
        }
        
        float hover = hoverProgress.get();
        
        // Calculate click scale animation
        if (clickTime > 0) {
            long elapsed = System.currentTimeMillis() - clickTime;
            if (elapsed < 100) {
                clickScale = 1.0f - (elapsed / 100f) * 0.05f;
            } else if (elapsed < 200) {
                clickScale = 0.95f + ((elapsed - 100) / 100f) * 0.05f;
            } else {
                clickScale = 1.0f;
                clickTime = 0;
            }
        }
        
        // Apply scale transform for click effect
        int renderX = x;
        int renderY = y;
        int renderW = width;
        int renderH = height;
        
        if (clickScale != 1.0f) {
            int scaleDiffW = (int) (width * (1 - clickScale) / 2);
            int scaleDiffH = (int) (height * (1 - clickScale) / 2);
            renderX += scaleDiffW;
            renderY += scaleDiffH;
            renderW -= scaleDiffW * 2;
            renderH -= scaleDiffH * 2;
        }
        
        // Background color interpolation
        int currentBg = enabled ? UIColors.lerp(bgColor, hoverColor, hover) : UIColors.BG_PANEL;
        int currentBorder = enabled ? UIColors.lerp(borderColor, accentColor, hover) : UIColors.BORDER;
        
        // Draw button background
        UIRenderer.fill(graphics, renderX, renderY, renderW, renderH, currentBg);
        
        // Draw border
        UIRenderer.horizontalLine(graphics, renderX, renderY, renderW, currentBorder);
        UIRenderer.horizontalLine(graphics, renderX, renderY + renderH - 1, renderW, currentBorder);
        UIRenderer.verticalLine(graphics, renderX, renderY, renderH, currentBorder);
        UIRenderer.verticalLine(graphics, renderX + renderW - 1, renderY, renderH, currentBorder);
        
        // Draw accent glow on hover
        if (hover > 0 && enabled) {
            int glowAlpha = (int) (30 * hover);
            int glowColor = UIColors.withAlpha(accentColor, glowAlpha);
            graphics.fill(renderX - 1, renderY - 1, renderX + renderW + 1, renderY + renderH + 1, glowColor);
        }
        
        // Draw bottom accent line
        if (enabled) {
            int lineColor = UIColors.withAlpha(accentColor, 100 + (int) (155 * hover));
            UIRenderer.horizontalLine(graphics, renderX + 2, renderY + renderH - 2, renderW - 4, lineColor);
        }
        
        // Draw text
        int currentTextColor = enabled ? textColor : disabledColor;
        if (style != Style.ICON_ONLY && text != null && !text.isEmpty()) {
            UIRenderer.drawCenteredText(graphics, text, renderX + renderW / 2, 
                                        renderY + (renderH - 8) / 2, currentTextColor);
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible || !enabled) return false;
        
        if (button == 0 && isMouseOver(mouseX, mouseY)) {
            // Play click sound
            Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(ModSounds.UI_CLICK.get(), 0.8f + (float) Math.random() * 0.4f)
            );
            
            // Trigger click animation
            clickTime = System.currentTimeMillis();
            
            // Execute callback
            if (onClick != null) {
                onClick.accept(this);
            }
            
            return true;
        }
        
        return false;
    }
    
    @Override
    public void tick() {
        // Animation updates handled in render via AnimatedValue
    }
    
    // Getters and setters
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
    
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    @Override public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    @Override public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    
    public void setOnClick(Consumer<SLButton> onClick) { this.onClick = onClick; }
    
    public SLButton withColors(int bg, int hover, int border, int accent) {
        this.bgColor = bg;
        this.hoverColor = hover;
        this.borderColor = border;
        this.accentColor = accent;
        return this;
    }
    
    public SLButton withTextColor(int color) {
        this.textColor = color;
        return this;
    }
}
