package net.xelpha.sololevelingreforged.ui.core;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

/**
 * Advanced rendering utilities for the Solo Leveling System UI
 * Provides methods for drawing styled shapes, gradients, and effects
 */
public final class UIRenderer {
    
    private UIRenderer() {} // Prevent instantiation
    
    private static final Minecraft MC = Minecraft.getInstance();
    
    // ══════════════════════════════════════════════════════════════════════════
    // BASIC SHAPES
    // ══════════════════════════════════════════════════════════════════════════
    
    /**
     * Draws a filled rectangle
     */
    public static void fill(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + height, color);
    }
    
    /**
     * Draws a rectangle with a border
     */
    public static void fillWithBorder(GuiGraphics graphics, int x, int y, int width, int height, 
                                       int fillColor, int borderColor, int borderWidth) {
        // Border
        graphics.fill(x, y, x + width, y + borderWidth, borderColor); // Top
        graphics.fill(x, y + height - borderWidth, x + width, y + height, borderColor); // Bottom
        graphics.fill(x, y, x + borderWidth, y + height, borderColor); // Left
        graphics.fill(x + width - borderWidth, y, x + width, y + height, borderColor); // Right
        
        // Fill
        graphics.fill(x + borderWidth, y + borderWidth, 
                     x + width - borderWidth, y + height - borderWidth, fillColor);
    }
    
    /**
     * Draws a horizontal line
     */
    public static void horizontalLine(GuiGraphics graphics, int x, int y, int width, int color) {
        graphics.fill(x, y, x + width, y + 1, color);
    }
    
    /**
     * Draws a vertical line
     */
    public static void verticalLine(GuiGraphics graphics, int x, int y, int height, int color) {
        graphics.fill(x, y, x + 1, y + height, color);
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // GRADIENT RENDERING
    // ══════════════════════════════════════════════════════════════════════════
    
    /**
     * Draws a horizontal gradient rectangle
     */
    public static void fillGradientH(GuiGraphics graphics, int x, int y, int width, int height, 
                                      int colorLeft, int colorRight) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        
        Matrix4f matrix = graphics.pose().last().pose();
        
        float aL = ((colorLeft >> 24) & 0xFF) / 255f;
        float rL = ((colorLeft >> 16) & 0xFF) / 255f;
        float gL = ((colorLeft >> 8) & 0xFF) / 255f;
        float bL = (colorLeft & 0xFF) / 255f;
        
        float aR = ((colorRight >> 24) & 0xFF) / 255f;
        float rR = ((colorRight >> 16) & 0xFF) / 255f;
        float gR = ((colorRight >> 8) & 0xFF) / 255f;
        float bR = (colorRight & 0xFF) / 255f;
        
        buffer.vertex(matrix, x, y + height, 0).color(rL, gL, bL, aL).endVertex();
        buffer.vertex(matrix, x + width, y + height, 0).color(rR, gR, bR, aR).endVertex();
        buffer.vertex(matrix, x + width, y, 0).color(rR, gR, bR, aR).endVertex();
        buffer.vertex(matrix, x, y, 0).color(rL, gL, bL, aL).endVertex();
        
        BufferUploader.drawWithShader(buffer.end());
        RenderSystem.disableBlend();
    }
    
    /**
     * Draws a vertical gradient rectangle
     */
    public static void fillGradientV(GuiGraphics graphics, int x, int y, int width, int height, 
                                      int colorTop, int colorBottom) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        
        Matrix4f matrix = graphics.pose().last().pose();
        
        float aT = ((colorTop >> 24) & 0xFF) / 255f;
        float rT = ((colorTop >> 16) & 0xFF) / 255f;
        float gT = ((colorTop >> 8) & 0xFF) / 255f;
        float bT = (colorTop & 0xFF) / 255f;
        
        float aB = ((colorBottom >> 24) & 0xFF) / 255f;
        float rB = ((colorBottom >> 16) & 0xFF) / 255f;
        float gB = ((colorBottom >> 8) & 0xFF) / 255f;
        float bB = (colorBottom & 0xFF) / 255f;
        
        buffer.vertex(matrix, x, y + height, 0).color(rB, gB, bB, aB).endVertex();
        buffer.vertex(matrix, x + width, y + height, 0).color(rB, gB, bB, aB).endVertex();
        buffer.vertex(matrix, x + width, y, 0).color(rT, gT, bT, aT).endVertex();
        buffer.vertex(matrix, x, y, 0).color(rT, gT, bT, aT).endVertex();
        
        BufferUploader.drawWithShader(buffer.end());
        RenderSystem.disableBlend();
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // STYLED PANELS
    // ══════════════════════════════════════════════════════════════════════════
    
    /**
     * Draws a styled panel with the Solo Leveling aesthetic
     */
    public static void drawPanel(GuiGraphics graphics, int x, int y, int width, int height) {
        drawPanel(graphics, x, y, width, height, UIColors.BG_PANEL, UIColors.BORDER);
    }
    
    /**
     * Draws a styled panel with custom colors
     */
    public static void drawPanel(GuiGraphics graphics, int x, int y, int width, int height, 
                                  int bgColor, int borderColor) {
        // Main background
        fill(graphics, x, y, width, height, bgColor);
        
        // Border
        horizontalLine(graphics, x, y, width, borderColor);
        horizontalLine(graphics, x, y + height - 1, width, borderColor);
        verticalLine(graphics, x, y, height, borderColor);
        verticalLine(graphics, x + width - 1, y, height, borderColor);
        
        // Inner shadow (top-left)
        fill(graphics, x + 1, y + 1, width - 2, 1, UIColors.BORDER_INNER);
        fill(graphics, x + 1, y + 1, 1, height - 2, UIColors.BORDER_INNER);
    }
    
    /**
     * Draws a panel with a glowing border effect
     */
    public static void drawGlowPanel(GuiGraphics graphics, int x, int y, int width, int height, 
                                      int glowColor, float glowIntensity) {
        // Outer glow layers
        int glowAlpha = (int) (30 * glowIntensity);
        for (int i = 3; i >= 1; i--) {
            int layerColor = UIColors.withAlpha(glowColor, glowAlpha * i);
            graphics.fill(x - i, y - i, x + width + i, y + height + i, layerColor);
        }
        
        // Main panel
        drawPanel(graphics, x, y, width, height, UIColors.BG_PANEL, glowColor);
    }
    
    /**
     * Draws a header bar with gradient
     */
    public static void drawHeader(GuiGraphics graphics, int x, int y, int width, int height, String title) {
        // Background gradient
        fillGradientH(graphics, x, y, width, height, UIColors.BG_HEADER, UIColors.BG_PANEL);
        
        // Bottom accent line
        fillGradientH(graphics, x, y + height - 1, width, 1, UIColors.PRIMARY, UIColors.PRIMARY_DIM);
        
        // Title text
        Font font = MC.font;
        int textWidth = font.width(title);
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height - 8) / 2;
        graphics.drawString(font, title, textX, textY, UIColors.TEXT_TITLE, false);
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // PROGRESS BARS
    // ══════════════════════════════════════════════════════════════════════════
    
    /**
     * Draws a styled progress bar
     */
    public static void drawProgressBar(GuiGraphics graphics, int x, int y, int width, int height, 
                                        float progress, int barColor, int bgColor) {
        progress = Math.max(0, Math.min(1, progress));
        
        // Background
        fill(graphics, x, y, width, height, bgColor);
        
        // Progress fill
        int fillWidth = (int) (width * progress);
        if (fillWidth > 0) {
            fill(graphics, x, y, fillWidth, height, barColor);
        }
        
        // Border
        horizontalLine(graphics, x, y, width, UIColors.BORDER);
        horizontalLine(graphics, x, y + height - 1, width, UIColors.BORDER);
        verticalLine(graphics, x, y, height, UIColors.BORDER);
        verticalLine(graphics, x + width - 1, y, height, UIColors.BORDER);
    }
    
    /**
     * Draws a styled progress bar with gradient fill
     */
    public static void drawProgressBarGradient(GuiGraphics graphics, int x, int y, int width, int height, 
                                                float progress, int colorStart, int colorEnd, int bgColor) {
        progress = Math.max(0, Math.min(1, progress));
        
        // Background
        fill(graphics, x, y, width, height, bgColor);
        
        // Progress fill with gradient
        int fillWidth = (int) (width * progress);
        if (fillWidth > 0) {
            fillGradientH(graphics, x, y, fillWidth, height, colorStart, colorEnd);
        }
        
        // Border
        horizontalLine(graphics, x, y, width, UIColors.BORDER);
        horizontalLine(graphics, x, y + height - 1, width, UIColors.BORDER);
        verticalLine(graphics, x, y, height, UIColors.BORDER);
        verticalLine(graphics, x + width - 1, y, height, UIColors.BORDER);
    }
    
    /**
     * Draws a segmented progress bar (for discrete values)
     */
    public static void drawSegmentedBar(GuiGraphics graphics, int x, int y, int width, int height, 
                                         int current, int max, int barColor, int bgColor) {
        if (max <= 0) return;
        
        int segmentWidth = width / max;
        int gap = 2;
        
        for (int i = 0; i < max; i++) {
            int segX = x + i * segmentWidth;
            int segW = segmentWidth - gap;
            
            int color = i < current ? barColor : bgColor;
            fill(graphics, segX, y, segW, height, color);
        }
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // TEXT RENDERING
    // ══════════════════════════════════════════════════════════════════════════
    
    /**
     * Draws text with shadow
     */
    public static void drawText(GuiGraphics graphics, String text, int x, int y, int color) {
        graphics.drawString(MC.font, text, x, y, color, false);
    }
    
    /**
     * Draws text with drop shadow
     */
    public static void drawTextWithShadow(GuiGraphics graphics, String text, int x, int y, int color) {
        graphics.drawString(MC.font, text, x + 1, y + 1, 0xFF000000, false);
        graphics.drawString(MC.font, text, x, y, color, false);
    }
    
    /**
     * Draws centered text
     */
    public static void drawCenteredText(GuiGraphics graphics, String text, int centerX, int y, int color) {
        int textWidth = MC.font.width(text);
        graphics.drawString(MC.font, text, centerX - textWidth / 2, y, color, false);
    }
    
    /**
     * Draws right-aligned text
     */
    public static void drawRightAlignedText(GuiGraphics graphics, String text, int rightX, int y, int color) {
        int textWidth = MC.font.width(text);
        graphics.drawString(MC.font, text, rightX - textWidth, y, color, false);
    }
    
    /**
     * Draws text that fits within a maximum width, truncating with ellipsis if needed
     */
    public static void drawTruncatedText(GuiGraphics graphics, String text, int x, int y, 
                                          int maxWidth, int color) {
        Font font = MC.font;
        if (font.width(text) <= maxWidth) {
            graphics.drawString(font, text, x, y, color, false);
            return;
        }
        
        String ellipsis = "...";
        int ellipsisWidth = font.width(ellipsis);
        
        StringBuilder truncated = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (font.width(truncated.toString() + c) + ellipsisWidth > maxWidth) {
                break;
            }
            truncated.append(c);
        }
        
        graphics.drawString(font, truncated + ellipsis, x, y, color, false);
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // SPECIAL EFFECTS
    // ══════════════════════════════════════════════════════════════════════════
    
    /**
     * Draws a scanline effect over an area
     */
    public static void drawScanlines(GuiGraphics graphics, int x, int y, int width, int height, 
                                      int lineColor, int spacing) {
        for (int i = 0; i < height; i += spacing) {
            horizontalLine(graphics, x, y + i, width, lineColor);
        }
    }
    
    /**
     * Draws a pulsing glow effect (call with game tick for animation)
     */
    public static void drawPulsingGlow(GuiGraphics graphics, int x, int y, int width, int height, 
                                        int color, long tick, float speed) {
        float pulse = (float) (Math.sin(tick * speed) * 0.5 + 0.5);
        int alpha = (int) (50 + pulse * 80);
        
        for (int i = 4; i >= 1; i--) {
            int layerAlpha = alpha / (i + 1);
            int layerColor = UIColors.withAlpha(color, layerAlpha);
            graphics.fill(x - i, y - i, x + width + i, y + height + i, layerColor);
        }
    }
    
    /**
     * Draws corner decorations for a panel
     */
    public static void drawCornerDecorations(GuiGraphics graphics, int x, int y, int width, int height, 
                                              int color, int size) {
        // Top-left
        horizontalLine(graphics, x, y, size, color);
        verticalLine(graphics, x, y, size, color);
        
        // Top-right
        horizontalLine(graphics, x + width - size, y, size, color);
        verticalLine(graphics, x + width - 1, y, size, color);
        
        // Bottom-left
        horizontalLine(graphics, x, y + height - 1, size, color);
        verticalLine(graphics, x, y + height - size, size, color);
        
        // Bottom-right
        horizontalLine(graphics, x + width - size, y + height - 1, size, color);
        verticalLine(graphics, x + width - 1, y + height - size, size, color);
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // ICONS AND SYMBOLS
    // ══════════════════════════════════════════════════════════════════════════
    
    /**
     * Draws a simple plus icon
     */
    public static void drawPlusIcon(GuiGraphics graphics, int centerX, int centerY, int size, int color) {
        int halfSize = size / 2;
        int thickness = Math.max(1, size / 4);
        
        // Horizontal bar
        fill(graphics, centerX - halfSize, centerY - thickness / 2, size, thickness, color);
        // Vertical bar
        fill(graphics, centerX - thickness / 2, centerY - halfSize, thickness, size, color);
    }
    
    /**
     * Draws a simple minus icon
     */
    public static void drawMinusIcon(GuiGraphics graphics, int centerX, int centerY, int size, int color) {
        int halfSize = size / 2;
        int thickness = Math.max(1, size / 4);
        
        // Horizontal bar
        fill(graphics, centerX - halfSize, centerY - thickness / 2, size, thickness, color);
    }
    
    /**
     * Draws an arrow pointing in a direction
     */
    public static void drawArrow(GuiGraphics graphics, int x, int y, int size, int color, Direction dir) {
        // Simple triangle arrow using fills
        int half = size / 2;
        
        switch (dir) {
            case UP -> {
                for (int i = 0; i < half; i++) {
                    horizontalLine(graphics, x + half - i, y + half - i, i * 2 + 1, color);
                }
            }
            case DOWN -> {
                for (int i = 0; i < half; i++) {
                    horizontalLine(graphics, x + half - i, y + i, i * 2 + 1, color);
                }
            }
            case LEFT -> {
                for (int i = 0; i < half; i++) {
                    verticalLine(graphics, x + half - i, y + half - i, i * 2 + 1, color);
                }
            }
            case RIGHT -> {
                for (int i = 0; i < half; i++) {
                    verticalLine(graphics, x + i, y + half - i, i * 2 + 1, color);
                }
            }
        }
    }
    
    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // UTILITY
    // ══════════════════════════════════════════════════════════════════════════
    
    /**
     * Checks if a point is within a rectangle
     */
    public static boolean isMouseOver(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
    
    /**
     * Gets the current font
     */
    public static Font getFont() {
        return MC.font;
    }
    
    /**
     * Gets the width of a string
     */
    public static int getTextWidth(String text) {
        return MC.font.width(text);
    }
}
