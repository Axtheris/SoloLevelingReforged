package net.xelpha.sololevelingreforged.ui.components;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Base interface for all UI components in the Solo Leveling System
 * Provides a consistent API for rendering, interaction, and state management
 */
public interface UIComponent {
    
    /**
     * Renders the component
     * @param graphics The graphics context
     * @param mouseX Current mouse X position
     * @param mouseY Current mouse Y position
     * @param partialTick Partial tick for smooth animations
     */
    void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick);
    
    /**
     * Called when the mouse is clicked
     * @return true if the click was handled
     */
    default boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }
    
    /**
     * Called when the mouse is released
     * @return true if the release was handled
     */
    default boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }
    
    /**
     * Called when the mouse is dragged
     * @return true if the drag was handled
     */
    default boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return false;
    }
    
    /**
     * Called when the mouse is scrolled
     * @return true if the scroll was handled
     */
    default boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return false;
    }
    
    /**
     * Called when a key is pressed
     * @return true if the key was handled
     */
    default boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }
    
    /**
     * Called every tick to update animation state
     */
    default void tick() {}
    
    /**
     * Gets the X position of the component
     */
    int getX();
    
    /**
     * Gets the Y position of the component
     */
    int getY();
    
    /**
     * Gets the width of the component
     */
    int getWidth();
    
    /**
     * Gets the height of the component
     */
    int getHeight();
    
    /**
     * Sets the position of the component
     */
    void setPosition(int x, int y);
    
    /**
     * Checks if a point is within the component bounds
     */
    default boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseX < getX() + getWidth() 
            && mouseY >= getY() && mouseY < getY() + getHeight();
    }
    
    /**
     * Whether the component is visible
     */
    default boolean isVisible() {
        return true;
    }
    
    /**
     * Whether the component is enabled and can be interacted with
     */
    default boolean isEnabled() {
        return true;
    }
}
