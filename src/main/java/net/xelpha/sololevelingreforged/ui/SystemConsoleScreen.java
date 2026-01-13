package net.xelpha.sololevelingreforged.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.xelpha.sololevelingreforged.ModSounds;
import net.xelpha.sololevelingreforged.core.PlayerCapability;
import net.xelpha.sololevelingreforged.ui.components.SLTabBar;
import net.xelpha.sololevelingreforged.ui.core.UIAnimator;
import net.xelpha.sololevelingreforged.ui.core.UIColors;
import net.xelpha.sololevelingreforged.ui.core.UIRenderer;
import net.xelpha.sololevelingreforged.ui.tabs.*;

import java.util.ArrayList;
import java.util.List;

/**
 * System Console - The main UI hub for the Solo Leveling System
 * 
 * Features:
 * - Tab-based navigation (Status, Skills, Quests, Inventory)
 * - Smooth animations and transitions
 * - Static centered window (no dragging for performance)
 * - Modern, immersive design with particles
 * - Scrollable content areas
 */
public class SystemConsoleScreen extends Screen {
    
    // Dimensions
    private static final int WINDOW_WIDTH = 550;
    private static final int WINDOW_HEIGHT = 400;
    private static final int HEADER_HEIGHT = 28;
    private static final int TAB_BAR_HEIGHT = 32;
    private static final int BORDER_SIZE = 1;
    
    // Position (always centered)
    private int guiLeft, guiTop;
    
    // Animation
    private final UIAnimator.AnimatedValue scaleProgress = new UIAnimator.AnimatedValue(0);
    private final UIAnimator.AnimatedValue fadeProgress = new UIAnimator.AnimatedValue(0);
    private long animationTick = 0;
    
    // Components
    private SLTabBar tabBar;
    private final List<BaseTab> tabs = new ArrayList<>();
    private int activeTabIndex = 0;
    
    // Player data
    private PlayerCapability capability;
    private LocalPlayer player;
    
    // Effects
    private final RandomSource random = RandomSource.create();
    private final List<GlowParticle> particles = new ArrayList<>();
    
    // Tooltip
    private List<String> currentTooltip = null;
    private int tooltipX, tooltipY;
    
    public SystemConsoleScreen() {
        super(Component.literal("System Console"));
        this.player = Minecraft.getInstance().player;
        if (player != null) {
            this.capability = player.getCapability(PlayerCapability.PLAYER_SYSTEM_CAP).orElse(null);
        }
    }
    
    @Override
    protected void init() {
        super.init();
        
        // Always center the window
        this.guiLeft = (this.width - WINDOW_WIDTH) / 2;
        this.guiTop = (this.height - WINDOW_HEIGHT) / 2;
        
        // Start opening animation
        scaleProgress.animateTo(1f, 300, UIAnimator::easeOutBack);
        fadeProgress.animateTo(1f, 200);
        
        // Initialize tab bar
        int tabBarY = guiTop + HEADER_HEIGHT;
        tabBar = new SLTabBar(guiLeft + BORDER_SIZE, tabBarY, WINDOW_WIDTH - BORDER_SIZE * 2, TAB_BAR_HEIGHT)
            .addTab("STATUS")
            .addTab("SKILLS")
            .addTab("QUESTS")
            .addTab("INVENTORY")
            .withEqualWidth(true)
            .onTabChanged(this::onTabChanged);
        
        // Initialize tabs
        initializeTabs();
        
        // Play opening sound
        playOpenSound();
        
        // Generate initial particles
        generateParticles(10);
    }
    
    private void initializeTabs() {
        tabs.clear();
        
        int contentX = guiLeft + BORDER_SIZE;
        int contentY = guiTop + HEADER_HEIGHT + TAB_BAR_HEIGHT;
        int contentWidth = WINDOW_WIDTH - BORDER_SIZE * 2;
        int contentHeight = WINDOW_HEIGHT - HEADER_HEIGHT - TAB_BAR_HEIGHT - BORDER_SIZE;
        
        // Create tab instances
        tabs.add(new StatusTab(contentX, contentY, contentWidth, contentHeight));
        tabs.add(new SkillsTab(contentX, contentY, contentWidth, contentHeight));
        tabs.add(new QuestsTab(contentX, contentY, contentWidth, contentHeight));
        tabs.add(new InventoryTab(contentX, contentY, contentWidth, contentHeight));
        
        // Initialize active tab with data
        updateTabData();
    }
    
    private void onTabChanged(int newIndex) {
        // Play tab switch sound
        playClickSound();
        
        // Deactivate old tab
        if (activeTabIndex >= 0 && activeTabIndex < tabs.size()) {
            tabs.get(activeTabIndex).onTabDeactivated();
        }
        
        // Activate new tab
        activeTabIndex = newIndex;
        if (activeTabIndex >= 0 && activeTabIndex < tabs.size()) {
            tabs.get(activeTabIndex).onTabActivated();
            updateTabData();
        }
    }
    
    private void updateTabData() {
        if (capability != null && activeTabIndex >= 0 && activeTabIndex < tabs.size()) {
            BaseTab tab = tabs.get(activeTabIndex);
            tab.ensureInitialized();
            tab.updateData(capability);
        }
    }
    
    /**
     * Set a tooltip to be rendered this frame
     */
    public void setTooltip(List<String> lines, int x, int y) {
        this.currentTooltip = lines;
        this.tooltipX = x;
        this.tooltipY = y;
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        animationTick++;
        currentTooltip = null; // Reset tooltip each frame
        
        // Render darkened background
        renderBackground(graphics);
        
        // Get animation progress
        float scale = scaleProgress.get();
        float fade = fadeProgress.get();
        
        // Calculate transform center
        int centerX = guiLeft + WINDOW_WIDTH / 2;
        int centerY = guiTop + WINDOW_HEIGHT / 2;
        
        // Apply scaling transform for opening animation
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        
        if (scale < 1.0f) {
            poseStack.translate(centerX, centerY, 0);
            poseStack.scale(scale, scale, 1.0f);
            poseStack.translate(-centerX, -centerY, 0);
        }
        
        // Render window
        if (fade > 0.1f) {
            // Render particles behind window
            renderParticles(graphics);
            
            // Render main window
            renderWindow(graphics, mouseX, mouseY, partialTick, fade);
        }
        
        poseStack.popPose();
        
        // Render tooltip on top of everything (outside pose transform)
        if (currentTooltip != null && !currentTooltip.isEmpty()) {
            net.xelpha.sololevelingreforged.ui.components.SLTooltip.render(graphics, tooltipX, tooltipY, currentTooltip);
        }
        
        super.render(graphics, mouseX, mouseY, partialTick);
    }
    
    private void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, float fade) {
        int alpha = (int) (fade * 255);
        
        // Outer glow effect
        int glowAlpha = (int) (30 * fade);
        for (int i = 5; i >= 1; i--) {
            graphics.fill(guiLeft - i, guiTop - i, 
                         guiLeft + WINDOW_WIDTH + i, guiTop + WINDOW_HEIGHT + i, 
                         UIColors.withAlpha(UIColors.PRIMARY, glowAlpha / (i + 1)));
        }
        
        // Main background
        UIRenderer.fill(graphics, guiLeft, guiTop, WINDOW_WIDTH, WINDOW_HEIGHT, 
                       UIColors.withAlpha(UIColors.BG_DARK, (int)(0xF0 * fade)));
        
        // Border
        int borderColor = UIColors.withAlpha(UIColors.PRIMARY_DIM, alpha);
        UIRenderer.horizontalLine(graphics, guiLeft, guiTop, WINDOW_WIDTH, borderColor);
        UIRenderer.horizontalLine(graphics, guiLeft, guiTop + WINDOW_HEIGHT - 1, WINDOW_WIDTH, borderColor);
        UIRenderer.verticalLine(graphics, guiLeft, guiTop, WINDOW_HEIGHT, borderColor);
        UIRenderer.verticalLine(graphics, guiLeft + WINDOW_WIDTH - 1, guiTop, WINDOW_HEIGHT, borderColor);
        
        // Corner decorations
        int cornerColor = UIColors.withAlpha(UIColors.PRIMARY, alpha);
        UIRenderer.drawCornerDecorations(graphics, guiLeft, guiTop, WINDOW_WIDTH, WINDOW_HEIGHT, cornerColor, 12);
        
        // Render header
        renderHeader(graphics, mouseX, mouseY, fade);
        
        // Render tab bar
        tabBar.render(graphics, mouseX, mouseY, partialTick);
        
        // Render active tab content
        if (activeTabIndex >= 0 && activeTabIndex < tabs.size()) {
            BaseTab tab = tabs.get(activeTabIndex);
            tab.clearPendingTooltip(); // Clear tooltip before render
            tab.render(graphics, mouseX, mouseY, partialTick);
            
            // Check for tab tooltip
            if (tab.getPendingTooltip() != null) {
                setTooltip(tab.getPendingTooltip(), tab.getTooltipX(), tab.getTooltipY());
            }
        }
        
        // Subtle scanline effect
        int scanlineColor = UIColors.withAlpha(0xFF000000, 6);
        UIRenderer.drawScanlines(graphics, guiLeft + 1, guiTop + 1, WINDOW_WIDTH - 2, WINDOW_HEIGHT - 2, scanlineColor, 4);
    }
    
    private void renderHeader(GuiGraphics graphics, int mouseX, int mouseY, float fade) {
        int headerY = guiTop;
        int alpha = (int) (fade * 255);
        
        // Header background gradient
        UIRenderer.fillGradientH(graphics, guiLeft + 1, headerY + 1, WINDOW_WIDTH - 2, HEADER_HEIGHT - 1,
                                UIColors.withAlpha(UIColors.BG_HEADER, alpha),
                                UIColors.withAlpha(UIColors.BG_PANEL, alpha));
        
        // Header separator line
        UIRenderer.horizontalLine(graphics, guiLeft, headerY + HEADER_HEIGHT - 1, WINDOW_WIDTH, 
                                 UIColors.withAlpha(UIColors.PRIMARY_DIM, alpha));
        
        // Title with glow effect
        String title = "SYSTEM CONSOLE";
        int titleX = guiLeft + 14;
        int titleY = headerY + (HEADER_HEIGHT - 8) / 2;
        
        // Draw title glow
        int titleWidth = UIRenderer.getTextWidth(title);
        int glowAlpha = (int) (30 * fade);
        graphics.fill(titleX - 2, titleY - 2, titleX + titleWidth + 2, titleY + 10, 
                     UIColors.withAlpha(UIColors.PRIMARY, glowAlpha));
        
        // Draw title text
        UIRenderer.drawText(graphics, title, titleX, titleY, UIColors.withAlpha(UIColors.TEXT_TITLE, alpha));
        
        // System status indicator (pulsing)
        float pulse = UIAnimator.pulse(animationTick, 0.1f);
        int statusColor = UIColors.lerp(UIColors.PRIMARY_DIM, UIColors.PRIMARY, pulse);
        UIRenderer.fill(graphics, titleX + titleWidth + 10, titleY + 2, 6, 6, UIColors.withAlpha(statusColor, alpha));
        
        // Close button with hover effect
        int closeX = guiLeft + WINDOW_WIDTH - 22;
        int closeY = headerY + (HEADER_HEIGHT - 10) / 2;
        boolean closeHovered = UIRenderer.isMouseOver(mouseX, mouseY, closeX - 2, closeY - 2, 14, 14);
        
        if (closeHovered) {
            // Hover glow
            graphics.fill(closeX - 4, closeY - 4, closeX + 12, closeY + 12, 
                         UIColors.withAlpha(UIColors.TEXT_ERROR, 30));
            // Tooltip
            setTooltip(List.of("Close (ESC)"), mouseX, mouseY);
        }
        
        int closeColor = closeHovered ? UIColors.TEXT_ERROR : UIColors.TEXT_MUTED;
        UIRenderer.drawCenteredText(graphics, "×", closeX + 5, closeY, UIColors.withAlpha(closeColor, alpha));
        
        // Level display (right side of header)
        if (capability != null) {
            String levelText = "Lv. " + capability.getLevel();
            int levelX = closeX - UIRenderer.getTextWidth(levelText) - 20;
            UIRenderer.drawText(graphics, levelText, levelX, titleY, UIColors.withAlpha(UIColors.PRIMARY, alpha));
        }
        
        // Keybind hint
        String hint = "[1-4] Switch Tabs  |  [ESC] Close";
        int hintWidth = UIRenderer.getTextWidth(hint);
        UIRenderer.drawText(graphics, hint, guiLeft + WINDOW_WIDTH - hintWidth - 30, 
                           guiTop + WINDOW_HEIGHT - 12, UIColors.withAlpha(UIColors.TEXT_MUTED, (int)(alpha * 0.6f)));
    }
    
    private void renderParticles(GuiGraphics graphics) {
        // Update and render glow particles
        particles.removeIf(p -> {
            p.update();
            return p.isDead();
        });
        
        for (GlowParticle particle : particles) {
            particle.render(graphics, guiLeft, guiTop);
        }
        
        // Generate new particles occasionally
        if (random.nextFloat() < 0.03f) {
            generateParticles(1);
        }
    }
    
    private void generateParticles(int count) {
        for (int i = 0; i < count; i++) {
            particles.add(new GlowParticle(
                random.nextFloat() * WINDOW_WIDTH,
                WINDOW_HEIGHT + random.nextFloat() * 20,
                random.nextFloat() * 0.3f - 0.15f,
                -0.3f - random.nextFloat() * 1.0f,
                2 + random.nextFloat() * 3,
                UIColors.PRIMARY,
                80 + random.nextInt(100)
            ));
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            // Check close button
            int closeX = guiLeft + WINDOW_WIDTH - 22;
            int closeY = guiTop + (HEADER_HEIGHT - 10) / 2;
            if (UIRenderer.isMouseOver((int) mouseX, (int) mouseY, closeX - 2, closeY - 2, 14, 14)) {
                onClose();
                return true;
            }
            
            // Forward to tab bar
            if (tabBar.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
            
            // Forward to active tab
            if (activeTabIndex >= 0 && activeTabIndex < tabs.size()) {
                if (tabs.get(activeTabIndex).mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // Forward to active tab
        if (activeTabIndex >= 0 && activeTabIndex < tabs.size()) {
            if (tabs.get(activeTabIndex).mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }
        
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        // Forward to active tab for scrolling
        if (activeTabIndex >= 0 && activeTabIndex < tabs.size()) {
            if (tabs.get(activeTabIndex).mouseScrolled(mouseX, mouseY, delta)) {
                return true;
            }
        }
        
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // ESC to close
        if (keyCode == 256) {
            onClose();
            return true;
        }
        
        // Tab switching with number keys
        if (keyCode >= 49 && keyCode <= 52) { // 1-4 keys
            int tabIndex = keyCode - 49;
            if (tabIndex < tabs.size() && tabIndex != activeTabIndex) {
                tabBar.selectTab(tabIndex);
            }
            return true;
        }
        
        // Forward to active tab
        if (activeTabIndex >= 0 && activeTabIndex < tabs.size()) {
            if (tabs.get(activeTabIndex).keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public void tick() {
        super.tick();
        
        // Update tab bar
        tabBar.tick();
        
        // Update active tab
        if (activeTabIndex >= 0 && activeTabIndex < tabs.size()) {
            tabs.get(activeTabIndex).tick();
        }
        
        // Refresh capability data periodically (every 5 ticks for responsive updates)
        if (player != null && animationTick % 5 == 0) {
            capability = player.getCapability(PlayerCapability.PLAYER_SYSTEM_CAP).orElse(null);
            updateTabData();
        }
    }
    
    /**
     * Force refresh capability data and update UI immediately.
     * Called after network sync packets are received.
     */
    public void forceRefresh() {
        if (player != null) {
            capability = player.getCapability(PlayerCapability.PLAYER_SYSTEM_CAP).orElse(null);
            updateTabData();
        }
    }
    
    /**
     * Get the current screen instance if open
     */
    public static SystemConsoleScreen getOpenScreen() {
        if (Minecraft.getInstance().screen instanceof SystemConsoleScreen screen) {
            return screen;
        }
        return null;
    }
    
    private void playOpenSound() {
        if (player != null) {
            Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(ModSounds.INTERFACE_OPEN.get(), 
                    0.9f + random.nextFloat() * 0.2f, 1.0f)
            );
        }
    }
    
    private void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(
            SimpleSoundInstance.forUI(ModSounds.UI_CLICK.get(), 1.0f, 0.8f)
        );
    }
    
    @Override
    public void onClose() {
        // Play close sound
        if (player != null) {
            Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(ModSounds.UI_CLICK.get(), 0.8f, 0.7f)
            );
        }
        
        super.onClose();
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    // Particle class for background effects
    private static class GlowParticle {
        float x, y, vx, vy, size;
        int color;
        int life, maxLife;
        
        GlowParticle(float x, float y, float vx, float vy, float size, int color, int maxLife) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.size = size;
            this.color = color;
            this.maxLife = maxLife;
            this.life = 0;
        }
        
        void update() {
            x += vx;
            y += vy;
            life++;
        }
        
        boolean isDead() {
            return life >= maxLife || y < -size;
        }
        
        void render(GuiGraphics graphics, int offsetX, int offsetY) {
            float progress = (float) life / maxLife;
            float alpha = 1.0f - progress;
            int particleColor = UIColors.withAlpha(color, (int) (35 * alpha));
            
            int px = offsetX + (int) x;
            int py = offsetY + (int) y;
            int halfSize = (int) (size / 2);
            
            graphics.fill(px - halfSize, py - halfSize, 
                         px + halfSize, py + halfSize, particleColor);
        }
    }
}
