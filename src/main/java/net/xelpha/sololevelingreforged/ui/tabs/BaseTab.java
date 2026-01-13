package net.xelpha.sololevelingreforged.ui.tabs;

import net.minecraft.client.gui.GuiGraphics;
import net.xelpha.sololevelingreforged.core.PlayerCapability;
import net.xelpha.sololevelingreforged.ui.components.UIComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Base class for all tab content panels in the Solo Leveling System UI
 * Provides common functionality for child component management and rendering
 */
public abstract class BaseTab implements UIComponent {
    
    protected int x, y, width, height;
    protected boolean visible = true;
    protected boolean initialized = false;
    
    protected final List<UIComponent> components = new ArrayList<>();
    protected PlayerCapability capability;
    
    // Tooltip system - allows tabs to show tooltips via parent screen
    protected BiConsumer<List<String>, int[]> tooltipProvider;
    protected List<String> pendingTooltip = null;
    protected int tooltipX, tooltipY;
    
    public BaseTab(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    /**
     * Initializes the tab's components. Called once when the tab is first shown.
     */
    protected abstract void initComponents();
    
    /**
     * Updates the tab with fresh capability data
     */
    public abstract void updateData(PlayerCapability capability);
    
    /**
     * Gets the display name of this tab
     */
    public abstract String getTabName();
    
    /**
     * Ensures the tab is initialized
     */
    public void ensureInitialized() {
        if (!initialized) {
            initComponents();
            initialized = true;
        }
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) return;
        
        ensureInitialized();
        
        // Render all child components
        for (UIComponent component : components) {
            component.render(graphics, mouseX, mouseY, partialTick);
        }
        
        // Allow subclasses to render additional content
        renderContent(graphics, mouseX, mouseY, partialTick);
    }
    
    /**
     * Override to render additional content after components
     */
    protected void renderContent(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Default implementation does nothing
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return false;
        
        for (UIComponent component : components) {
            if (component.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!visible) return false;
        
        for (UIComponent component : components) {
            if (component.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!visible) return false;
        
        for (UIComponent component : components) {
            if (component.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!visible) return false;
        
        for (UIComponent component : components) {
            if (component.mouseScrolled(mouseX, mouseY, delta)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!visible) return false;
        
        for (UIComponent component : components) {
            if (component.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public void tick() {
        if (!visible) return;
        
        for (UIComponent component : components) {
            component.tick();
        }
    }
    
    /**
     * Called when this tab becomes active
     */
    public void onTabActivated() {
        ensureInitialized();
        if (capability != null) {
            updateData(capability);
        }
    }
    
    /**
     * Called when this tab becomes inactive
     */
    public void onTabDeactivated() {
        // Default implementation does nothing
    }
    
    /**
     * Adds a component to this tab
     */
    protected void addComponent(UIComponent component) {
        components.add(component);
    }
    
    /**
     * Removes all components from this tab
     */
    protected void clearComponents() {
        components.clear();
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
        
        // Update child component positions
        for (UIComponent component : components) {
            component.setPosition(component.getX() + deltaX, component.getY() + deltaY);
        }
    }
    
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    @Override public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    
    /**
     * Refreshes the layout - call when size changes
     */
    public void refreshLayout() {
        components.clear();
        initialized = false;
        ensureInitialized();
    }
    
    /**
     * Sets the tooltip provider callback
     */
    public void setTooltipProvider(BiConsumer<List<String>, int[]> provider) {
        this.tooltipProvider = provider;
    }
    
    /**
     * Shows a tooltip via the parent screen
     */
    protected void showTooltip(List<String> lines, int x, int y) {
        this.pendingTooltip = lines;
        this.tooltipX = x;
        this.tooltipY = y;
    }
    
    /**
     * Gets the pending tooltip if any
     */
    public List<String> getPendingTooltip() {
        return pendingTooltip;
    }
    
    /**
     * Gets the tooltip X position
     */
    public int getTooltipX() {
        return tooltipX;
    }
    
    /**
     * Gets the tooltip Y position
     */
    public int getTooltipY() {
        return tooltipY;
    }
    
    /**
     * Clears any pending tooltip
     */
    public void clearPendingTooltip() {
        this.pendingTooltip = null;
    }
}
