package net.xelpha.sololevelingreforged.ui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.xelpha.sololevelingreforged.ModSounds;
import net.xelpha.sololevelingreforged.ui.core.UIAnimator;
import net.xelpha.sololevelingreforged.ui.core.UIColors;
import net.xelpha.sololevelingreforged.ui.core.UIRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Tab navigation bar component for the Solo Leveling System UI
 * Provides smooth animated tab switching with underline indicator
 */
public class SLTabBar implements UIComponent {
    
    private int x, y, width, height;
    private boolean visible = true;
    
    private final List<Tab> tabs = new ArrayList<>();
    private int selectedIndex = 0;
    private Consumer<Integer> onTabChanged;
    
    // Animation
    private final UIAnimator.AnimatedValue indicatorX = new UIAnimator.AnimatedValue(0);
    private final UIAnimator.AnimatedValue indicatorWidth = new UIAnimator.AnimatedValue(0);
    private final List<UIAnimator.AnimatedValue> tabHovers = new ArrayList<>();
    private int hoveredIndex = -1;
    
    // Styling
    private int bgColor = UIColors.BG_HEADER;
    private int textColor = UIColors.TEXT_SECONDARY;
    private int selectedTextColor = UIColors.TEXT;
    private int accentColor = UIColors.PRIMARY;
    private int hoverColor = UIColors.BG_HOVER;
    
    // Layout
    private int tabPadding = 20;
    private int indicatorHeight = 2;
    private boolean equalWidth = false;
    
    public SLTabBar(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    /**
     * Adds a tab to the bar
     */
    public SLTabBar addTab(String label) {
        return addTab(label, null);
    }
    
    /**
     * Adds a tab with an icon
     */
    public SLTabBar addTab(String label, String icon) {
        tabs.add(new Tab(label, icon));
        tabHovers.add(new UIAnimator.AnimatedValue(0));
        recalculateTabPositions();
        return this;
    }
    
    /**
     * Recalculates tab positions based on content
     */
    private void recalculateTabPositions() {
        if (tabs.isEmpty()) return;
        
        if (equalWidth) {
            int tabWidth = width / tabs.size();
            int currentX = x;
            
            for (Tab tab : tabs) {
                tab.x = currentX;
                tab.width = tabWidth;
                currentX += tabWidth;
            }
        } else {
            int totalTextWidth = 0;
            for (Tab tab : tabs) {
                tab.textWidth = UIRenderer.getTextWidth(tab.label);
                totalTextWidth += tab.textWidth + tabPadding * 2;
            }
            
            int currentX = x;
            for (Tab tab : tabs) {
                tab.x = currentX;
                tab.width = tab.textWidth + tabPadding * 2;
                currentX += tab.width;
            }
        }
        
        // Initialize indicator position
        if (!tabs.isEmpty()) {
            Tab selected = tabs.get(selectedIndex);
            indicatorX.set(selected.x);
            indicatorWidth.set(selected.width);
        }
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!visible || tabs.isEmpty()) return;
        
        // Draw background
        UIRenderer.fill(graphics, x, y, width, height, bgColor);
        
        // Draw bottom border
        UIRenderer.horizontalLine(graphics, x, y + height - 1, width, UIColors.BORDER);
        
        // Update hover states
        int newHoveredIndex = -1;
        for (int i = 0; i < tabs.size(); i++) {
            Tab tab = tabs.get(i);
            if (isMouseOverTab(mouseX, mouseY, tab)) {
                newHoveredIndex = i;
                break;
            }
        }
        
        // Animate hover transitions
        if (newHoveredIndex != hoveredIndex) {
            if (hoveredIndex >= 0 && hoveredIndex < tabHovers.size()) {
                tabHovers.get(hoveredIndex).animateTo(0, 150);
            }
            if (newHoveredIndex >= 0) {
                tabHovers.get(newHoveredIndex).animateTo(1, 150);
            }
            hoveredIndex = newHoveredIndex;
        }
        
        // Draw tabs
        for (int i = 0; i < tabs.size(); i++) {
            Tab tab = tabs.get(i);
            boolean isSelected = i == selectedIndex;
            float hoverProgress = tabHovers.get(i).get();
            
            // Draw hover background
            if (hoverProgress > 0 || isSelected) {
                int alpha = isSelected ? 40 : (int) (30 * hoverProgress);
                int hoverBg = UIColors.withAlpha(hoverColor, alpha);
                UIRenderer.fill(graphics, tab.x, y, tab.width, height - indicatorHeight, hoverBg);
            }
            
            // Draw tab text
            int color;
            if (isSelected) {
                color = selectedTextColor;
            } else {
                color = UIColors.lerp(textColor, selectedTextColor, hoverProgress);
            }
            
            int textX = tab.x + tab.width / 2;
            int textY = y + (height - 8) / 2;
            UIRenderer.drawCenteredText(graphics, tab.label, textX, textY, color);
        }
        
        // Draw animated selection indicator
        float indX = indicatorX.get();
        float indW = indicatorWidth.get();
        
        // Glow effect
        int glowAlpha = 40;
        for (int i = 3; i >= 1; i--) {
            int glowColor = UIColors.withAlpha(accentColor, glowAlpha / i);
            graphics.fill((int) indX, y + height - indicatorHeight - i, 
                         (int) (indX + indW), y + height + i, glowColor);
        }
        
        // Main indicator line
        UIRenderer.fill(graphics, (int) indX, y + height - indicatorHeight, 
                       (int) indW, indicatorHeight, accentColor);
    }
    
    private boolean isMouseOverTab(int mouseX, int mouseY, Tab tab) {
        return mouseX >= tab.x && mouseX < tab.x + tab.width 
            && mouseY >= y && mouseY < y + height;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible || button != 0) return false;
        
        for (int i = 0; i < tabs.size(); i++) {
            Tab tab = tabs.get(i);
            if (isMouseOverTab((int) mouseX, (int) mouseY, tab)) {
                selectTab(i);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Selects a tab by index
     */
    public void selectTab(int index) {
        if (index < 0 || index >= tabs.size() || index == selectedIndex) return;
        
        selectedIndex = index;
        Tab selected = tabs.get(index);
        
        // Animate indicator movement
        indicatorX.animateTo(selected.x, 200, UIAnimator::easeOutCubic);
        indicatorWidth.animateTo(selected.width, 200, UIAnimator::easeOutCubic);
        
        // Play sound
        Minecraft.getInstance().getSoundManager().play(
            SimpleSoundInstance.forUI(ModSounds.UI_CLICK.get(), 1.0f + (float) Math.random() * 0.2f)
        );
        
        // Notify listener
        if (onTabChanged != null) {
            onTabChanged.accept(index);
        }
    }
    
    /**
     * Gets the currently selected tab index
     */
    public int getSelectedIndex() {
        return selectedIndex;
    }
    
    /**
     * Sets the callback for tab changes
     */
    public SLTabBar onTabChanged(Consumer<Integer> callback) {
        this.onTabChanged = callback;
        return this;
    }
    
    /**
     * Sets whether tabs should have equal width
     */
    public SLTabBar withEqualWidth(boolean equal) {
        this.equalWidth = equal;
        recalculateTabPositions();
        return this;
    }
    
    /**
     * Sets the accent color for the indicator
     */
    public SLTabBar withAccentColor(int color) {
        this.accentColor = color;
        return this;
    }
    
    // Tab data class
    private static class Tab {
        String label;
        String icon;
        int x;
        int width;
        int textWidth;
        
        Tab(String label, String icon) {
            this.label = label;
            this.icon = icon;
        }
    }
    
    // UIComponent implementation
    
    @Override public int getX() { return x; }
    @Override public int getY() { return y; }
    @Override public int getWidth() { return width; }
    @Override public int getHeight() { return height; }
    
    @Override
    public void setPosition(int x, int y) {
        int deltaX = x - this.x;
        this.x = x;
        this.y = y;
        
        // Update tab positions
        for (Tab tab : tabs) {
            tab.x += deltaX;
        }
        
        // Update indicator position
        if (!tabs.isEmpty()) {
            indicatorX.set(indicatorX.get() + deltaX);
        }
    }
    
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        recalculateTabPositions();
    }
    
    @Override public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
}
