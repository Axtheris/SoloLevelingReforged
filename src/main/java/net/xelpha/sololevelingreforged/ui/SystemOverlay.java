package net.xelpha.sololevelingreforged.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.xelpha.sololevelingreforged.core.PlayerCapability;
import net.xelpha.sololevelingreforged.ui.core.UIAnimator;
import net.xelpha.sololevelingreforged.ui.core.UIColors;
import net.xelpha.sololevelingreforged.ui.core.UIRenderer;

/**
 * System UI Overlay - Modern, sleek HUD for the Solo Leveling System
 * Displays health, mana, XP, level, and quick stats in a non-intrusive design
 * 
 * Design inspired by modern MMO interfaces with a Solo Leveling aesthetic
 */
public class SystemOverlay implements IGuiOverlay {
    
    // Layout
    private static final int MARGIN = 8;
    private static final int PANEL_WIDTH = 180;
    private static final int BAR_HEIGHT = 10;
    private static final int BAR_SPACING = 4;
    private static final int XP_BAR_HEIGHT = 6;
    
    // Animation state
    private final UIAnimator.AnimatedValue healthBarProgress = new UIAnimator.AnimatedValue(1);
    private final UIAnimator.AnimatedValue manaBarProgress = new UIAnimator.AnimatedValue(1);
    private final UIAnimator.AnimatedValue xpBarProgress = new UIAnimator.AnimatedValue(0);
    
    private float lastHealth = 0;
    private float lastMana = 0;
    private int lastXP = 0;
    
    private long animationTick = 0;
    
    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        
        if (player == null) return;
        
        // Hide during screen displays (except inventory)
        if (minecraft.screen != null && !(minecraft.screen instanceof net.minecraft.client.gui.screens.inventory.InventoryScreen)) {
            return;
        }
        
        // Get capability
        PlayerCapability cap = player.getCapability(PlayerCapability.PLAYER_SYSTEM_CAP).orElse(null);
        if (cap == null) return;
        
        animationTick++;
        
        // Update animated values
        updateAnimatedValues(player, cap);
        
        // Render the HUD
        renderPlayerHUD(graphics, cap, player);
    }
    
    private void updateAnimatedValues(LocalPlayer player, PlayerCapability cap) {
        // Health animation
        float healthPercent = player.getHealth() / player.getMaxHealth();
        if (Math.abs(healthPercent - lastHealth) > 0.001f) {
            healthBarProgress.animateTo(healthPercent, 300, UIAnimator::easeOutCubic);
            lastHealth = healthPercent;
        }
        
        // Mana animation
        float manaPercent = cap.getCurrentMana() / cap.getMaxMana();
        if (Math.abs(manaPercent - lastMana) > 0.001f) {
            manaBarProgress.animateTo(manaPercent, 300, UIAnimator::easeOutCubic);
            lastMana = manaPercent;
        }
        
        // XP animation
        float xpPercent = cap.getExperienceToNext() > 0 
            ? (float) cap.getExperience() / cap.getExperienceToNext() : 0;
        if (cap.getExperience() != lastXP) {
            xpBarProgress.animateTo(xpPercent, 500, UIAnimator::easeOutCubic);
            lastXP = cap.getExperience();
        }
    }
    
    private void renderPlayerHUD(GuiGraphics graphics, PlayerCapability cap, LocalPlayer player) {
        int startX = MARGIN;
        int startY = MARGIN;
        
        // Main panel background (subtle)
        int panelHeight = 70;
        int bgColor = UIColors.withAlpha(UIColors.BG_DARK, 200);
        UIRenderer.fill(graphics, startX, startY, PANEL_WIDTH, panelHeight, bgColor);
        
        // Panel border
        UIRenderer.horizontalLine(graphics, startX, startY, PANEL_WIDTH, UIColors.BORDER);
        UIRenderer.horizontalLine(graphics, startX, startY + panelHeight - 1, PANEL_WIDTH, UIColors.BORDER);
        UIRenderer.verticalLine(graphics, startX, startY, panelHeight, UIColors.BORDER);
        UIRenderer.verticalLine(graphics, startX + PANEL_WIDTH - 1, startY, panelHeight, UIColors.BORDER);
        
        // Corner accents
        int accentColor = UIColors.PRIMARY_DIM;
        graphics.fill(startX, startY, startX + 3, startY + 1, accentColor);
        graphics.fill(startX, startY, startX + 1, startY + 3, accentColor);
        graphics.fill(startX + PANEL_WIDTH - 3, startY, startX + PANEL_WIDTH, startY + 1, accentColor);
        graphics.fill(startX + PANEL_WIDTH - 1, startY, startX + PANEL_WIDTH, startY + 3, accentColor);
        
        int contentX = startX + 8;
        int contentY = startY + 6;
        int barWidth = PANEL_WIDTH - 16;
        
        // Level and Title row
        String levelText = "Lv." + cap.getLevel();
        String titleText = cap.getCurrentTitle();
        
        UIRenderer.drawText(graphics, levelText, contentX, contentY, UIColors.PRIMARY);
        UIRenderer.drawRightAlignedText(graphics, titleText, contentX + barWidth, contentY, UIColors.TEXT_SECONDARY);
        
        contentY += 14;
        
        // Health bar
        renderResourceBar(graphics, contentX, contentY, barWidth, BAR_HEIGHT,
                         healthBarProgress.get(), player.getHealth(), player.getMaxHealth(),
                         UIColors.BAR_HEALTH, UIColors.BAR_HEALTH_BG, "HP");
        
        contentY += BAR_HEIGHT + BAR_SPACING;
        
        // Mana bar
        renderResourceBar(graphics, contentX, contentY, barWidth, BAR_HEIGHT,
                         manaBarProgress.get(), cap.getCurrentMana(), cap.getMaxMana(),
                         UIColors.BAR_MANA, UIColors.BAR_MANA_BG, "MP");
        
        contentY += BAR_HEIGHT + BAR_SPACING;
        
        // XP bar
        renderXPBar(graphics, contentX, contentY, barWidth, XP_BAR_HEIGHT,
                   xpBarProgress.get(), cap.getExperience(), cap.getExperienceToNext());
        
        // AP indicator (if available)
        if (cap.getAvailableAP() > 0) {
            renderAPIndicator(graphics, startX + PANEL_WIDTH + 4, startY, cap.getAvailableAP());
        }
        
        // Render mini stat indicators below panel
        renderQuickStats(graphics, startX, startY + panelHeight + 4, cap);
    }
    
    private void renderResourceBar(GuiGraphics graphics, int x, int y, int width, int height,
                                   float progress, float current, float max,
                                   int barColor, int bgColor, String label) {
        // Background
        UIRenderer.fill(graphics, x, y, width, height, bgColor);
        
        // Progress fill with gradient
        int fillWidth = (int) (width * progress);
        if (fillWidth > 0) {
            int gradientEnd = UIColors.brighten(barColor, 1.3f);
            UIRenderer.fillGradientH(graphics, x, y, fillWidth, height, barColor, gradientEnd);
            
            // Shine effect
            int shineColor = UIColors.withAlpha(0xFFFFFFFF, 30);
            UIRenderer.fill(graphics, x, y, fillWidth, Math.max(1, height / 3), shineColor);
        }
        
        // Border
        UIRenderer.horizontalLine(graphics, x, y, width, UIColors.darken(barColor, 2f));
        UIRenderer.horizontalLine(graphics, x, y + height - 1, width, UIColors.darken(barColor, 2f));
        UIRenderer.verticalLine(graphics, x, y, height, UIColors.darken(barColor, 2f));
        UIRenderer.verticalLine(graphics, x + width - 1, y, height, UIColors.darken(barColor, 2f));
        
        // Text overlay
        String valueText = String.format("%.0f/%.0f", current, max);
        int textY = y + (height - 7) / 2;
        
        // Label on left
        UIRenderer.drawText(graphics, label, x + 2, textY, UIColors.TEXT);
        
        // Value on right
        UIRenderer.drawRightAlignedText(graphics, valueText, x + width - 2, textY, UIColors.TEXT);
    }
    
    private void renderXPBar(GuiGraphics graphics, int x, int y, int width, int height,
                             float progress, int current, int max) {
        // Background
        UIRenderer.fill(graphics, x, y, width, height, UIColors.BAR_XP_BG);
        
        // Progress fill
        int fillWidth = (int) (width * progress);
        if (fillWidth > 0) {
            UIRenderer.fillGradientH(graphics, x, y, fillWidth, height, 
                                    UIColors.BAR_XP, UIColors.PRIMARY_GLOW);
        }
        
        // Border
        UIRenderer.horizontalLine(graphics, x, y, width, UIColors.darken(UIColors.BAR_XP, 2f));
        UIRenderer.horizontalLine(graphics, x, y + height - 1, width, UIColors.darken(UIColors.BAR_XP, 2f));
        UIRenderer.verticalLine(graphics, x, y, height, UIColors.darken(UIColors.BAR_XP, 2f));
        UIRenderer.verticalLine(graphics, x + width - 1, y, height, UIColors.darken(UIColors.BAR_XP, 2f));
        
        // XP text below bar
        String xpText = current + "/" + max + " XP";
        UIRenderer.drawText(graphics, xpText, x, y + height + 2, UIColors.TEXT_MUTED);
    }
    
    private void renderAPIndicator(GuiGraphics graphics, int x, int y, int ap) {
        // Pulsing background
        float pulse = UIAnimator.pulse(animationTick, 0.15f);
        int bgAlpha = (int) (150 + 50 * pulse);
        int bgColor = UIColors.withAlpha(UIColors.BG_DARK, bgAlpha);
        
        int size = 24;
        UIRenderer.fill(graphics, x, y, size, size, bgColor);
        
        // Border with pulse
        int borderColor = UIColors.lerp(UIColors.TEXT_SUCCESS, UIColors.brighten(UIColors.TEXT_SUCCESS, 1.5f), pulse);
        UIRenderer.horizontalLine(graphics, x, y, size, borderColor);
        UIRenderer.horizontalLine(graphics, x, y + size - 1, size, borderColor);
        UIRenderer.verticalLine(graphics, x, y, size, borderColor);
        UIRenderer.verticalLine(graphics, x + size - 1, y, size, borderColor);
        
        // AP text
        String apText = String.valueOf(ap);
        UIRenderer.drawCenteredText(graphics, apText, x + size / 2, y + (size - 8) / 2, UIColors.TEXT_SUCCESS);
        
        // Label below
        UIRenderer.drawCenteredText(graphics, "AP", x + size / 2, y + size + 2, UIColors.TEXT_SUCCESS);
    }
    
    private void renderQuickStats(GuiGraphics graphics, int x, int y, PlayerCapability cap) {
        // Small stat indicators in a row
        String[] statLabels = {"STR", "AGI", "SEN", "VIT", "INT"};
        int[] statValues = {
            cap.getStrength(), cap.getAgility(), cap.getSense(), 
            cap.getVitality(), cap.getIntelligence()
        };
        int[] statColors = {
            UIColors.STAT_STRENGTH, UIColors.STAT_AGILITY, UIColors.STAT_SENSE,
            UIColors.STAT_VITALITY, UIColors.STAT_INTELLIGENCE
        };
        
        int statWidth = 34;
        int totalWidth = statWidth * statLabels.length;
        
        // Background
        UIRenderer.fill(graphics, x, y, totalWidth, 18, UIColors.withAlpha(UIColors.BG_DARK, 180));
        UIRenderer.horizontalLine(graphics, x, y, totalWidth, UIColors.BORDER);
        UIRenderer.horizontalLine(graphics, x, y + 17, totalWidth, UIColors.BORDER);
        
        for (int i = 0; i < statLabels.length; i++) {
            int statX = x + i * statWidth;
            
            // Separator line
            if (i > 0) {
                UIRenderer.verticalLine(graphics, statX, y, 18, UIColors.BORDER);
            }
            
            // Stat indicator color bar
            UIRenderer.fill(graphics, statX + 1, y + 1, 2, 16, statColors[i]);
            
            // Value
            String valueText = String.valueOf(statValues[i]);
            UIRenderer.drawCenteredText(graphics, valueText, statX + statWidth / 2 + 2, y + 5, UIColors.TEXT);
        }
    }
}
