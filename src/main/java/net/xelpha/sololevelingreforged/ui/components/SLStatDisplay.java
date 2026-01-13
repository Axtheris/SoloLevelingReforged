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
 * Stat display component with allocation button for the Solo Leveling System UI
 * Shows stat name, value, and provides a button to allocate ability points
 */
public class SLStatDisplay implements UIComponent {
    
    private int x, y, width, height;
    private boolean visible = true;
    
    // Stat data
    private String statName;
    private String statKey;
    private int value;
    private int bonusValue = 0;
    private int statColor;
    
    // Allocation
    private boolean canAllocate = false;
    private Consumer<String> onAllocate;
    
    // Animation
    private final UIAnimator.AnimatedValue hoverProgress = new UIAnimator.AnimatedValue(0);
    private boolean wasHovered = false;
    private boolean buttonHovered = false;
    private long lastValueChange = 0;
    
    // Tooltip callback
    private TooltipConsumer tooltipConsumer;
    
    // Layout
    private static final int BUTTON_SIZE = 18;
    private static final int PADDING = 8;
    
    public SLStatDisplay(int x, int y, int width, int height, String statName, String statKey) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.statName = statName;
        this.statKey = statKey;
        this.statColor = UIColors.getStatColor(statKey);
    }
    
    @FunctionalInterface
    public interface TooltipConsumer {
        void showTooltip(List<String> lines, int x, int y);
    }
    
    public void setTooltipConsumer(TooltipConsumer consumer) {
        this.tooltipConsumer = consumer;
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) return;
        
        boolean hovered = isMouseOver(mouseX, mouseY);
        buttonHovered = canAllocate && isMouseOverButton(mouseX, mouseY);
        
        // Update hover animation
        if (hovered != wasHovered) {
            hoverProgress.animateTo(hovered ? 1f : 0f, 200);
            wasHovered = hovered;
        }
        
        float hover = hoverProgress.get();
        
        // Draw background on hover
        if (hover > 0) {
            int bgAlpha = (int) (30 * hover);
            UIRenderer.fill(graphics, x, y, width, height, UIColors.withAlpha(UIColors.BG_HOVER, bgAlpha));
        }
        
        // Draw stat color indicator bar on left
        int indicatorColor = UIColors.lerp(UIColors.darken(statColor, 2f), statColor, hover);
        UIRenderer.fill(graphics, x, y + 2, 3, height - 4, indicatorColor);
        
        // Draw stat name
        int nameColor = UIColors.lerp(UIColors.TEXT_SECONDARY, UIColors.TEXT, hover);
        UIRenderer.drawText(graphics, statName, x + PADDING + 4, y + (height - 8) / 2, nameColor);
        
        // Calculate value text position (right-aligned before button)
        int buttonX = x + width - BUTTON_SIZE - PADDING;
        String valueText = String.valueOf(value);
        if (bonusValue > 0) {
            valueText += " (+" + bonusValue + ")";
        }
        
        // Draw value with potential animation effect
        int valueColor = statColor;
        if (System.currentTimeMillis() - lastValueChange < 500) {
            // Flash effect on value change
            float flash = 1f - ((System.currentTimeMillis() - lastValueChange) / 500f);
            valueColor = UIColors.lerp(statColor, UIColors.TEXT, flash);
        }
        
        int valueX = buttonX - UIRenderer.getTextWidth(valueText) - 8;
        UIRenderer.drawText(graphics, valueText, valueX, y + (height - 8) / 2, valueColor);
        
        // Draw allocation button if available
        if (canAllocate) {
            drawAllocationButton(graphics, buttonX, y + (height - BUTTON_SIZE) / 2);
        }
        
        // Draw bottom separator line
        int lineAlpha = (int) (40 + 40 * hover);
        UIRenderer.horizontalLine(graphics, x + PADDING, y + height - 1, 
                                 width - PADDING * 2, UIColors.withAlpha(UIColors.BORDER, lineAlpha));
        
        // Show tooltip on hover (not over button)
        if (hovered && !buttonHovered && tooltipConsumer != null) {
            tooltipConsumer.showTooltip(getTooltipLines(), mouseX + 8, mouseY + 8);
        } else if (buttonHovered && tooltipConsumer != null) {
            List<String> buttonTooltip = new ArrayList<>();
            buttonTooltip.add("§a+ Allocate Point");
            buttonTooltip.add("§7Click to add 1 point to " + statName);
            tooltipConsumer.showTooltip(buttonTooltip, mouseX + 8, mouseY + 8);
        }
    }
    
    private List<String> getTooltipLines() {
        List<String> lines = new ArrayList<>();
        
        // Stat name with color code
        String colorCode = getStatColorCode();
        lines.add(colorCode + statName);
        lines.add("");
        
        // Stat description
        switch (statKey.toLowerCase()) {
            case "strength":
                lines.add("§7• Increases physical damage");
                lines.add("§7• Enhances carry weight");
                lines.add("§7• Powers up melee attacks");
                break;
            case "agility":
                lines.add("§7• Increases movement speed");
                lines.add("§7• Boosts attack speed");
                lines.add("§7• Improves dodge chance");
                break;
            case "sense":
                lines.add("§7• Enhances accuracy");
                lines.add("§7• Improves enemy detection");
                lines.add("§7• Reveals hidden threats");
                break;
            case "vitality":
                lines.add("§7• Increases max health (+2 HP)");
                lines.add("§7• Boosts defense");
                lines.add("§7• Improves stamina regen");
                break;
            case "intelligence":
                lines.add("§7• Increases max mana (+10 MP)");
                lines.add("§7• Boosts magic damage");
                lines.add("§7• Enhances skill effects");
                break;
        }
        
        lines.add("");
        lines.add("§8Current: §f" + value);
        
        return lines;
    }
    
    private String getStatColorCode() {
        return switch (statKey.toLowerCase()) {
            case "strength" -> "§c";      // Red
            case "agility" -> "§a";       // Green  
            case "sense" -> "§e";         // Yellow
            case "vitality" -> "§d";      // Pink
            case "intelligence" -> "§9";  // Blue
            default -> "§f";              // White
        };
    }
    
    private void drawAllocationButton(GuiGraphics graphics, int bx, int by) {
        // Button background
        int bgColor = buttonHovered ? UIColors.PRIMARY_DIM : UIColors.BG_PANEL;
        int borderColor = buttonHovered ? UIColors.PRIMARY : UIColors.BORDER;
        
        UIRenderer.fill(graphics, bx, by, BUTTON_SIZE, BUTTON_SIZE, bgColor);
        
        // Border
        UIRenderer.horizontalLine(graphics, bx, by, BUTTON_SIZE, borderColor);
        UIRenderer.horizontalLine(graphics, bx, by + BUTTON_SIZE - 1, BUTTON_SIZE, borderColor);
        UIRenderer.verticalLine(graphics, bx, by, BUTTON_SIZE, borderColor);
        UIRenderer.verticalLine(graphics, bx + BUTTON_SIZE - 1, by, BUTTON_SIZE, borderColor);
        
        // Plus icon
        int iconColor = buttonHovered ? UIColors.TEXT : UIColors.TEXT_SECONDARY;
        int centerX = bx + BUTTON_SIZE / 2;
        int centerY = by + BUTTON_SIZE / 2;
        
        // Horizontal line of plus
        UIRenderer.fill(graphics, centerX - 4, centerY - 1, 9, 2, iconColor);
        // Vertical line of plus
        UIRenderer.fill(graphics, centerX - 1, centerY - 4, 2, 9, iconColor);
        
        // Glow effect on hover
        if (buttonHovered) {
            int glowColor = UIColors.withAlpha(UIColors.PRIMARY, 40);
            graphics.fill(bx - 2, by - 2, bx + BUTTON_SIZE + 2, by + BUTTON_SIZE + 2, glowColor);
        }
    }
    
    private boolean isMouseOverButton(int mouseX, int mouseY) {
        int buttonX = x + width - BUTTON_SIZE - PADDING;
        int buttonY = y + (height - BUTTON_SIZE) / 2;
        return mouseX >= buttonX && mouseX < buttonX + BUTTON_SIZE 
            && mouseY >= buttonY && mouseY < buttonY + BUTTON_SIZE;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible || button != 0) return false;
        
        if (canAllocate && isMouseOverButton((int) mouseX, (int) mouseY)) {
            // Play click sound
            Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(ModSounds.UI_CLICK.get(), 1.0f + (float) Math.random() * 0.2f)
            );
            
            // Trigger allocation callback
            if (onAllocate != null) {
                onAllocate.accept(statKey);
            }
            
            return true;
        }
        
        return false;
    }
    
    // Data setters
    
    public void setValue(int value) {
        if (this.value != value) {
            this.lastValueChange = System.currentTimeMillis();
        }
        this.value = value;
    }
    
    public void setBonusValue(int bonusValue) {
        this.bonusValue = bonusValue;
    }
    
    public void setCanAllocate(boolean canAllocate) {
        this.canAllocate = canAllocate;
    }
    
    public void setOnAllocate(Consumer<String> onAllocate) {
        this.onAllocate = onAllocate;
    }
    
    // Getters
    
    public String getStatKey() { return statKey; }
    public int getValue() { return value; }
    
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
}
