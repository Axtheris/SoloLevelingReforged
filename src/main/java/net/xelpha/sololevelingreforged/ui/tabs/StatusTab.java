package net.xelpha.sololevelingreforged.ui.tabs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.xelpha.sololevelingreforged.core.PlayerCapability;
import net.xelpha.sololevelingreforged.network.AllocateStatPacket;
import net.xelpha.sololevelingreforged.network.ModNetworkRegistry;
import net.xelpha.sololevelingreforged.ui.components.SLPanel;
import net.xelpha.sololevelingreforged.ui.components.SLProgressBar;
import net.xelpha.sololevelingreforged.ui.components.SLStatDisplay;
import net.xelpha.sololevelingreforged.ui.core.UIAnimator;
import net.xelpha.sololevelingreforged.ui.core.UIColors;
import net.xelpha.sololevelingreforged.ui.core.UIRenderer;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Status tab showing player model, level, resources, and stat allocation
 * Uses Minecraft's built-in entity rendering for proper player display
 */
public class StatusTab extends BaseTab {
    
    // Layout constants
    private static final int PLAYER_PANEL_WIDTH = 160;
    private static final int STATS_PANEL_MARGIN = 12;
    
    // Components
    private SLPanel playerPanel;
    private SLPanel statsPanel;
    private SLPanel resourcePanel;
    private SLProgressBar healthBar;
    private SLProgressBar manaBar;
    private SLProgressBar xpBar;
    
    private final Map<String, SLStatDisplay> statDisplays = new LinkedHashMap<>();
    
    // Player model rendering
    private LocalPlayer player;
    private float modelRotation = 0;
    private boolean draggingModel = false;
    private double lastMouseX;
    private long animationTick = 0;
    
    public StatusTab(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.player = Minecraft.getInstance().player;
    }
    
    @Override
    public String getTabName() {
        return "STATUS";
    }
    
    @Override
    protected void initComponents() {
        int padding = 12;
        
        // Left side: Player model panel (wider)
        playerPanel = new SLPanel(x + padding, y + padding, PLAYER_PANEL_WIDTH, height - padding * 2)
            .withTitle("PLAYER")
            .withCornerDecorations(true);
        addComponent(playerPanel);
        
        // Right side: Stats and resources
        int rightPanelX = x + PLAYER_PANEL_WIDTH + padding + STATS_PANEL_MARGIN;
        int rightPanelWidth = width - PLAYER_PANEL_WIDTH - padding * 2 - STATS_PANEL_MARGIN;
        
        // Resource bars panel (top right)
        int resourcePanelHeight = 80;
        resourcePanel = new SLPanel(rightPanelX, y + padding, rightPanelWidth, resourcePanelHeight)
            .withTitle("RESOURCES")
            .withCornerDecorations(true);
        addComponent(resourcePanel);
        
        // Initialize resource bars
        int barWidth = rightPanelWidth - 24;
        int barY = resourcePanel.getContentStartY() + 4;
        
        healthBar = SLProgressBar.healthBar(rightPanelX + 12, barY, barWidth, 14);
        manaBar = SLProgressBar.manaBar(rightPanelX + 12, barY + 22, barWidth, 14);
        xpBar = SLProgressBar.xpBar(rightPanelX + 12, barY + 44, barWidth, 10)
            .withPercentage(true);
        
        addComponent(healthBar);
        addComponent(manaBar);
        addComponent(xpBar);
        
        // Stats panel (bottom right)
        int statsPanelY = y + padding + resourcePanelHeight + 8;
        int statsPanelHeight = height - padding * 2 - resourcePanelHeight - 8;
        statsPanel = new SLPanel(rightPanelX, statsPanelY, rightPanelWidth, statsPanelHeight)
            .withTitle("ATTRIBUTES")
            .withScrolling(true)
            .withCornerDecorations(true);
        addComponent(statsPanel);
        
        // Initialize stat displays
        String[] stats = {"Strength", "Agility", "Sense", "Vitality", "Intelligence"};
        String[] keys = {"strength", "agility", "sense", "vitality", "intelligence"};
        
        int statDisplayY = statsPanel.getContentStartY() + 4;
        int statHeight = 28;
        
        for (int i = 0; i < stats.length; i++) {
            SLStatDisplay display = new SLStatDisplay(
                rightPanelX + 8, 
                statDisplayY + i * statHeight, 
                rightPanelWidth - 16, 
                statHeight,
                stats[i].toUpperCase(), 
                keys[i]
            );
            display.setOnAllocate(this::onStatAllocate);
            display.setTooltipConsumer(this::showTooltip);
            statDisplays.put(keys[i], display);
            addComponent(display);
        }
    }
    
    @Override
    public void updateData(PlayerCapability capability) {
        this.capability = capability;
        if (capability == null) return;
        
        this.player = Minecraft.getInstance().player;
        
        // Update health bar
        if (player != null) {
            healthBar.setValues(player.getHealth(), player.getMaxHealth());
        }
        
        // Update mana bar
        manaBar.setValues(capability.getCurrentMana(), capability.getMaxMana());
        
        // Update XP bar
        xpBar.setValues(capability.getExperience(), capability.getExperienceToNext());
        xpBar.withCustomText("Level " + capability.getLevel() + " - " + 
            capability.getExperience() + "/" + capability.getExperienceToNext() + " XP");
        
        // Update stat displays
        boolean hasAP = capability.getAvailableAP() > 0;
        
        for (Map.Entry<String, SLStatDisplay> entry : statDisplays.entrySet()) {
            int value = capability.getStatValue(entry.getKey());
            entry.getValue().setValue(value);
            entry.getValue().setCanAllocate(hasAP);
        }
    }
    
    private void onStatAllocate(String statKey) {
        ModNetworkRegistry.CHANNEL.sendToServer(new AllocateStatPacket(statKey));
    }
    
    
    @Override
    public void tick() {
        super.tick();
        animationTick++;
        
        // Auto-rotate slowly when not dragging
        if (!draggingModel) {
            modelRotation = (float) Math.sin(animationTick * 0.015) * 20;
        }
    }
    
    @Override
    protected void renderContent(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Render player model using Minecraft's built-in renderer
        renderPlayerModel(graphics, mouseX, mouseY);
        
        // Render player info
        renderPlayerInfo(graphics);
    }
    
    private void renderPlayerModel(GuiGraphics graphics, int mouseX, int mouseY) {
        if (player == null) return;
        
        int centerX = playerPanel.getX() + playerPanel.getWidth() / 2;
        int modelY = playerPanel.getY() + playerPanel.getHeight() - 60;
        
        // Draw decorative platform
        drawPlatform(graphics, centerX, modelY + 5);
        
        // Draw glow behind player
        drawPlayerGlow(graphics, centerX, modelY - 50);
        
        // Use Minecraft's built-in entity rendering
        int scale = 50;
        
        // Calculate look direction based on mouse or rotation
        float lookX, lookY;
        if (draggingModel) {
            // Use manual rotation
            lookX = centerX + modelRotation * 3;
            lookY = modelY - 60;
        } else {
            // Use auto-rotation
            lookX = centerX + modelRotation * 3;
            lookY = modelY - 60;
        }
        
        // Use Minecraft's InventoryScreen helper - proper signature for 1.20.x
        InventoryScreen.renderEntityInInventoryFollowsMouse(
            graphics,
            centerX,        // x position
            modelY,         // y position
            scale,          // scale
            lookX - mouseX, // look direction X offset
            lookY - mouseY, // look direction Y offset  
            player          // entity to render
        );
    }
    
    /**
     * Draws a glowing platform under the player
     */
    private void drawPlatform(GuiGraphics graphics, int centerX, int centerY) {
        int baseWidth = 60;
        int baseHeight = 8;
        
        // Glow layers
        for (int i = 3; i >= 0; i--) {
            int w = baseWidth + i * 10;
            int h = baseHeight + i * 3;
            int alpha = 20 - i * 4;
            graphics.fill(centerX - w / 2, centerY - h / 2, 
                         centerX + w / 2, centerY + h / 2, 
                         UIColors.withAlpha(UIColors.PRIMARY, alpha));
        }
        
        // Core platform
        graphics.fill(centerX - baseWidth / 2, centerY - baseHeight / 2, 
                     centerX + baseWidth / 2, centerY + baseHeight / 2, 
                     UIColors.withAlpha(UIColors.PRIMARY_DIM, 80));
        
        // Top edge highlight
        UIRenderer.horizontalLine(graphics, centerX - baseWidth / 2, centerY - baseHeight / 2, 
                                 baseWidth, UIColors.withAlpha(UIColors.PRIMARY, 150));
    }
    
    /**
     * Draws glow effect behind player based on power level
     */
    private void drawPlayerGlow(GuiGraphics graphics, int centerX, int centerY) {
        int glowColor = UIColors.PRIMARY;
        
        if (capability != null) {
            String title = capability.getCurrentTitle();
            if (title.toLowerCase().contains("shadow") || title.toLowerCase().contains("monarch")) {
                glowColor = UIColors.SHADOW_ENERGY;
            } else if (capability.getLevel() >= 50) {
                glowColor = UIColors.TERTIARY;
            }
        }
        
        float pulse = UIAnimator.pulse(animationTick, 0.05f);
        int baseAlpha = (int)(15 + 10 * pulse);
        
        for (int i = 4; i >= 0; i--) {
            int radius = 40 + i * 12;
            int alpha = baseAlpha / (i + 1);
            graphics.fill(centerX - radius, centerY - radius, 
                         centerX + radius, centerY + radius, 
                         UIColors.withAlpha(glowColor, alpha));
        }
    }
    
    /**
     * Renders player info (level, title, AP)
     */
    private void renderPlayerInfo(GuiGraphics graphics) {
        if (capability == null) return;
        
        int centerX = playerPanel.getX() + playerPanel.getWidth() / 2;
        int titleY = playerPanel.getY() + 28;
        
        // Level with glow effect
        String levelText = "Lv. " + capability.getLevel();
        int levelWidth = UIRenderer.getTextWidth(levelText);
        
        graphics.fill(centerX - levelWidth / 2 - 4, titleY - 2, 
                     centerX + levelWidth / 2 + 4, titleY + 10, 
                     UIColors.withAlpha(UIColors.PRIMARY, 30));
        
        UIRenderer.drawCenteredText(graphics, levelText, centerX, titleY, UIColors.PRIMARY);
        
        // Title
        String title = capability.getCurrentTitle();
        UIRenderer.drawCenteredText(graphics, title, centerX, titleY + 14, UIColors.TEXT_SECONDARY);
        
        // AP indicator
        int ap = capability.getAvailableAP();
        int apY = playerPanel.getY() + playerPanel.getHeight() - 45;
        
        if (ap > 0) {
            float pulse = UIAnimator.pulse(animationTick, 0.1f);
            int apColor = UIColors.lerp(UIColors.TEXT_SUCCESS, UIColors.brighten(UIColors.TEXT_SUCCESS, 1.5f), pulse);
            
            graphics.fill(centerX - 45, apY - 4, centerX + 45, apY + 26, 
                         UIColors.withAlpha(UIColors.TEXT_SUCCESS, 25));
            
            UIRenderer.drawCenteredText(graphics, "AP: " + ap, centerX, apY, apColor);
            UIRenderer.drawCenteredText(graphics, "Allocate Points!", centerX, apY + 12, apColor);
        } else {
            UIRenderer.drawCenteredText(graphics, "AP: 0", centerX, apY + 6, UIColors.TEXT_SECONDARY);
        }
        
        // Drag hint
        UIRenderer.drawCenteredText(graphics, "Drag to rotate", centerX, 
                                   playerPanel.getY() + playerPanel.getHeight() - 14, UIColors.TEXT_MUTED);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isMouseOverPlayerModel(mouseX, mouseY)) {
            draggingModel = true;
            lastMouseX = mouseX;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggingModel) {
            draggingModel = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingModel) {
            modelRotation += (float) (mouseX - lastMouseX) * 1.5f;
            if (modelRotation > 180) modelRotation -= 360;
            if (modelRotation < -180) modelRotation += 360;
            lastMouseX = mouseX;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
    
    private boolean isMouseOverPlayerModel(double mouseX, double mouseY) {
        if (playerPanel == null) return false;
        
        return mouseX >= playerPanel.getX() + 10 && mouseX <= playerPanel.getX() + playerPanel.getWidth() - 10
            && mouseY >= playerPanel.getContentStartY() + 10 && mouseY <= playerPanel.getY() + playerPanel.getHeight() - 70;
    }
}
