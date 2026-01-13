package net.xelpha.sololevelingreforged.ui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.xelpha.sololevelingreforged.ui.core.UIColors;
import net.xelpha.sololevelingreforged.ui.core.UIRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Styled tooltip component for the Solo Leveling System UI
 * Supports multi-line text, custom formatting, and smooth animations
 */
public class SLTooltip {
    
    private static final int PADDING = 6;
    private static final int LINE_HEIGHT = 10;
    private static final int MAX_WIDTH = 200;
    
    /**
     * Renders a tooltip at the mouse position
     */
    public static void render(GuiGraphics graphics, int mouseX, int mouseY, String text) {
        render(graphics, mouseX, mouseY, List.of(text));
    }
    
    /**
     * Renders a multi-line tooltip at the mouse position
     */
    public static void render(GuiGraphics graphics, int mouseX, int mouseY, List<String> lines) {
        if (lines == null || lines.isEmpty()) return;
        
        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        
        // Calculate dimensions
        int maxTextWidth = 0;
        List<String> wrappedLines = new ArrayList<>();
        
        for (String line : lines) {
            // Word wrap long lines
            List<String> wrapped = wordWrap(line, MAX_WIDTH);
            wrappedLines.addAll(wrapped);
            for (String wrappedLine : wrapped) {
                maxTextWidth = Math.max(maxTextWidth, UIRenderer.getTextWidth(wrappedLine));
            }
        }
        
        int tooltipWidth = maxTextWidth + PADDING * 2;
        int tooltipHeight = wrappedLines.size() * LINE_HEIGHT + PADDING * 2 - 2;
        
        // Position tooltip to avoid screen edges
        int tooltipX = mouseX + 12;
        int tooltipY = mouseY - 12;
        
        if (tooltipX + tooltipWidth > screenWidth) {
            tooltipX = mouseX - tooltipWidth - 4;
        }
        if (tooltipY + tooltipHeight > screenHeight) {
            tooltipY = screenHeight - tooltipHeight;
        }
        if (tooltipY < 0) {
            tooltipY = 0;
        }
        
        // Draw tooltip
        renderTooltipBox(graphics, tooltipX, tooltipY, tooltipWidth, tooltipHeight, wrappedLines);
    }
    
    /**
     * Renders a tooltip with a title
     */
    public static void renderWithTitle(GuiGraphics graphics, int mouseX, int mouseY, 
                                        String title, List<String> lines) {
        List<String> allLines = new ArrayList<>();
        allLines.add("§l" + title); // Bold title
        allLines.addAll(lines);
        render(graphics, mouseX, mouseY, allLines);
    }
    
    /**
     * Renders a stat tooltip
     */
    public static void renderStatTooltip(GuiGraphics graphics, int mouseX, int mouseY, 
                                          String statName, int value, String description) {
        List<String> lines = new ArrayList<>();
        lines.add(statName + ": " + value);
        if (description != null && !description.isEmpty()) {
            lines.add("");
            lines.add(description);
        }
        render(graphics, mouseX, mouseY, lines);
    }
    
    private static void renderTooltipBox(GuiGraphics graphics, int x, int y, int width, int height, 
                                          List<String> lines) {
        // Outer glow
        int glowColor = UIColors.withAlpha(UIColors.PRIMARY, 20);
        graphics.fill(x - 2, y - 2, x + width + 2, y + height + 2, glowColor);
        
        // Background
        UIRenderer.fill(graphics, x, y, width, height, UIColors.BG_DARK);
        
        // Border
        UIRenderer.horizontalLine(graphics, x, y, width, UIColors.PRIMARY_DIM);
        UIRenderer.horizontalLine(graphics, x, y + height - 1, width, UIColors.PRIMARY_DIM);
        UIRenderer.verticalLine(graphics, x, y, height, UIColors.PRIMARY_DIM);
        UIRenderer.verticalLine(graphics, x + width - 1, y, height, UIColors.PRIMARY_DIM);
        
        // Corner accents
        graphics.fill(x, y, x + 2, y + 2, UIColors.PRIMARY);
        graphics.fill(x + width - 2, y, x + width, y + 2, UIColors.PRIMARY);
        graphics.fill(x, y + height - 2, x + 2, y + height, UIColors.PRIMARY);
        graphics.fill(x + width - 2, y + height - 2, x + width, y + height, UIColors.PRIMARY);
        
        // Text
        int textY = y + PADDING;
        for (String line : lines) {
            int color = UIColors.TEXT;
            String displayLine = line;
            
            // Check for formatting codes
            if (line.startsWith("§l")) {
                // Bold - render as title color
                displayLine = line.substring(2);
                color = UIColors.TEXT_TITLE;
            } else if (line.startsWith("§7")) {
                // Gray - muted text
                displayLine = line.substring(2);
                color = UIColors.TEXT_MUTED;
            } else if (line.startsWith("§c")) {
                // Red - warning/error
                displayLine = line.substring(2);
                color = UIColors.TEXT_ERROR;
            } else if (line.startsWith("§a")) {
                // Green - success
                displayLine = line.substring(2);
                color = UIColors.TEXT_SUCCESS;
            } else if (line.startsWith("§e")) {
                // Yellow - warning
                displayLine = line.substring(2);
                color = UIColors.TEXT_WARNING;
            } else if (line.startsWith("§b")) {
                // Cyan - accent
                displayLine = line.substring(2);
                color = UIColors.PRIMARY;
            }
            
            UIRenderer.drawText(graphics, displayLine, x + PADDING, textY, color);
            textY += LINE_HEIGHT;
        }
    }
    
    /**
     * Word wraps text to fit within a maximum width
     */
    private static List<String> wordWrap(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        
        if (UIRenderer.getTextWidth(text) <= maxWidth) {
            lines.add(text);
            return lines;
        }
        
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        
        for (String word : words) {
            String testLine = currentLine.length() > 0 
                ? currentLine + " " + word 
                : word;
            
            if (UIRenderer.getTextWidth(testLine) <= maxWidth) {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            } else {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    // Word is too long, force break
                    lines.add(word);
                }
            }
        }
        
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        
        return lines;
    }
    
    /**
     * Builder class for creating complex tooltips
     */
    public static class Builder {
        private String title;
        private final List<Line> lines = new ArrayList<>();
        
        public Builder title(String title) {
            this.title = title;
            return this;
        }
        
        public Builder line(String text) {
            lines.add(new Line(text, UIColors.TEXT));
            return this;
        }
        
        public Builder line(String text, int color) {
            lines.add(new Line(text, color));
            return this;
        }
        
        public Builder separator() {
            lines.add(new Line("", UIColors.TEXT));
            return this;
        }
        
        public Builder stat(String name, Object value) {
            lines.add(new Line(name + ": §b" + value, UIColors.TEXT_SECONDARY));
            return this;
        }
        
        public void render(GuiGraphics graphics, int mouseX, int mouseY) {
            List<String> textLines = new ArrayList<>();
            if (title != null) {
                textLines.add("§l" + title);
            }
            for (Line line : lines) {
                textLines.add(line.text);
            }
            SLTooltip.render(graphics, mouseX, mouseY, textLines);
        }
        
        private record Line(String text, int color) {}
    }
}
