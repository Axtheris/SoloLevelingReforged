package net.xelpha.sololevelingreforged.ui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.xelpha.sololevelingreforged.ui.core.UIAnimator;
import net.xelpha.sololevelingreforged.ui.core.UIColors;
import net.xelpha.sololevelingreforged.ui.core.UIRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Container panel component for the Solo Leveling System UI
 * Can hold child components and provides scrolling, styling, and layout management
 */
public class SLPanel implements UIComponent {
    
    private int x, y, width, height;
    private boolean visible = true;
    
    // Child components
    private final List<UIComponent> children = new ArrayList<>();
    
    // Styling
    private int bgColor = UIColors.BG_PANEL;
    private int borderColor = UIColors.BORDER;
    private boolean drawBackground = true;
    private boolean drawBorder = true;
    private int padding = 8;
    
    // Scrolling
    private boolean scrollable = false;
    private int contentHeight = 0;
    private final UIAnimator.AnimatedValue scrollOffset = new UIAnimator.AnimatedValue(0);
    private float targetScroll = 0;
    private boolean showScrollbar = false;
    
    // Header
    private String title = null;
    private int headerHeight = 24;
    
    // Effects
    private boolean drawCornerDecorations = false;
    private boolean drawGlow = false;
    private int glowColor = UIColors.PRIMARY;
    
    public SLPanel(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) return;
        
        // Draw glow effect
        if (drawGlow) {
            int glowAlpha = 30;
            for (int i = 4; i >= 1; i--) {
                int layerColor = UIColors.withAlpha(glowColor, glowAlpha / i);
                graphics.fill(x - i, y - i, x + width + i, y + height + i, layerColor);
            }
        }
        
        // Draw background
        if (drawBackground) {
            UIRenderer.fill(graphics, x, y, width, height, bgColor);
        }
        
        // Draw border
        if (drawBorder) {
            UIRenderer.horizontalLine(graphics, x, y, width, borderColor);
            UIRenderer.horizontalLine(graphics, x, y + height - 1, width, borderColor);
            UIRenderer.verticalLine(graphics, x, y, height, borderColor);
            UIRenderer.verticalLine(graphics, x + width - 1, y, height, borderColor);
        }
        
        // Draw corner decorations
        if (drawCornerDecorations) {
            UIRenderer.drawCornerDecorations(graphics, x, y, width, height, UIColors.PRIMARY, 8);
        }
        
        // Draw header
        int contentY = y;
        if (title != null) {
            UIRenderer.fillGradientH(graphics, x + 1, y + 1, width - 2, headerHeight - 1, 
                                    UIColors.BG_HEADER, UIColors.BG_PANEL);
            UIRenderer.horizontalLine(graphics, x, y + headerHeight, width, borderColor);
            UIRenderer.drawText(graphics, title, x + padding, y + (headerHeight - 8) / 2, UIColors.TEXT_TITLE);
            contentY += headerHeight;
        }
        
        // Set up scissor for content area if scrollable
        int contentAreaHeight = height - (title != null ? headerHeight : 0);
        
        if (scrollable && contentHeight > contentAreaHeight) {
            // Enable scissoring
            graphics.enableScissor(x + 1, contentY, x + width - 1, contentY + contentAreaHeight);
            
            // Calculate scroll offset
            float scroll = scrollOffset.get();
            
            // Render children with scroll offset
            for (UIComponent child : children) {
                int childY = child.getY() - (int) scroll;
                int originalY = child.getY();
                child.setPosition(child.getX(), childY);
                child.render(graphics, mouseX, mouseY, partialTick);
                child.setPosition(child.getX(), originalY);
            }
            
            // Disable scissoring
            graphics.disableScissor();
            
            // Draw scrollbar
            if (showScrollbar) {
                drawScrollbar(graphics, contentY, contentAreaHeight);
            }
        } else {
            // Render children normally
            for (UIComponent child : children) {
                child.render(graphics, mouseX, mouseY, partialTick);
            }
        }
    }
    
    private void drawScrollbar(GuiGraphics graphics, int contentY, int contentAreaHeight) {
        int scrollbarWidth = 4;
        int scrollbarX = x + width - scrollbarWidth - 2;
        
        // Track
        UIRenderer.fill(graphics, scrollbarX, contentY + 2, scrollbarWidth, 
                       contentAreaHeight - 4, UIColors.BG_HEADER);
        
        // Calculate thumb size and position
        float visibleRatio = (float) contentAreaHeight / contentHeight;
        int thumbHeight = Math.max(20, (int) (contentAreaHeight * visibleRatio));
        
        float scrollRatio = targetScroll / (contentHeight - contentAreaHeight);
        int thumbY = contentY + 2 + (int) ((contentAreaHeight - thumbHeight - 4) * scrollRatio);
        
        // Thumb
        UIRenderer.fill(graphics, scrollbarX, thumbY, scrollbarWidth, thumbHeight, UIColors.PRIMARY_DIM);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible || !isMouseOver(mouseX, mouseY)) return false;
        
        // Adjust mouse Y for scroll offset
        double adjustedMouseY = mouseY;
        if (scrollable) {
            adjustedMouseY += scrollOffset.get();
        }
        
        // Forward to children
        for (UIComponent child : children) {
            if (child.mouseClicked(mouseX, adjustedMouseY, button)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!visible || !scrollable || !isMouseOver(mouseX, mouseY)) return false;
        
        int contentAreaHeight = height - (title != null ? headerHeight : 0);
        if (contentHeight <= contentAreaHeight) return false;
        
        // Update scroll target
        float scrollAmount = (float) (delta * -30);
        targetScroll = Math.max(0, Math.min(contentHeight - contentAreaHeight, targetScroll + scrollAmount));
        scrollOffset.animateTo(targetScroll, 150, UIAnimator::easeOutCubic);
        
        return true;
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!visible) return false;
        
        for (UIComponent child : children) {
            if (child.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!visible) return false;
        
        for (UIComponent child : children) {
            if (child.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public void tick() {
        for (UIComponent child : children) {
            child.tick();
        }
    }
    
    // Child management
    
    public SLPanel addChild(UIComponent child) {
        children.add(child);
        recalculateContentHeight();
        return this;
    }
    
    public SLPanel removeChild(UIComponent child) {
        children.remove(child);
        recalculateContentHeight();
        return this;
    }
    
    public void clearChildren() {
        children.clear();
        contentHeight = 0;
    }
    
    public List<UIComponent> getChildren() {
        return children;
    }
    
    private void recalculateContentHeight() {
        contentHeight = 0;
        for (UIComponent child : children) {
            int childBottom = child.getY() + child.getHeight() - y;
            if (title != null) {
                childBottom -= headerHeight;
            }
            contentHeight = Math.max(contentHeight, childBottom + padding);
        }
    }
    
    // Configuration builders
    
    public SLPanel withTitle(String title) {
        this.title = title;
        return this;
    }
    
    public SLPanel withBackground(int color) {
        this.bgColor = color;
        this.drawBackground = true;
        return this;
    }
    
    public SLPanel withBorder(int color) {
        this.borderColor = color;
        this.drawBorder = true;
        return this;
    }
    
    public SLPanel withoutBackground() {
        this.drawBackground = false;
        return this;
    }
    
    public SLPanel withoutBorder() {
        this.drawBorder = false;
        return this;
    }
    
    public SLPanel withScrolling(boolean scrollable) {
        this.scrollable = scrollable;
        this.showScrollbar = scrollable;
        return this;
    }
    
    public SLPanel withCornerDecorations(boolean draw) {
        this.drawCornerDecorations = draw;
        return this;
    }
    
    public SLPanel withGlow(int color) {
        this.drawGlow = true;
        this.glowColor = color;
        return this;
    }
    
    public SLPanel withPadding(int padding) {
        this.padding = padding;
        return this;
    }
    
    // UIComponent implementation
    
    @Override public int getX() { return x; }
    @Override public int getY() { return y; }
    @Override public int getWidth() { return width; }
    @Override public int getHeight() { return height; }
    
    @Override
    public void setPosition(int x, int y) {
        int deltaX = x - this.x;
        int deltaY = y - this.y;
        this.x = x;
        this.y = y;
        
        // Update children positions
        for (UIComponent child : children) {
            child.setPosition(child.getX() + deltaX, child.getY() + deltaY);
        }
    }
    
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    @Override public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    
    public int getPadding() { return padding; }
    public int getContentStartY() { return y + (title != null ? headerHeight : 0) + padding; }
    public int getContentWidth() { return width - padding * 2; }
}
