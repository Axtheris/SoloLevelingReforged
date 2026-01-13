package net.xelpha.sololevelingreforged.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.xelpha.sololevelingreforged.core.PlayerCapability;

/**
 * System UI Overlay - Sleek, modern, top-left status bar (MMO style)
 * Always visible when capability exists, displays essential stats
 */
public class SystemOverlay implements IGuiOverlay {

    // Colors for the sleek theme
    private static final int TEXT_WHITE = 0xFFFFFFFF;       // White text
    private static final int TEXT_SHADOW = 0xFF000000;      // Black shadow
    private static final int BAR_RED = 0xFFFF0000;          // Red for health
    private static final int BAR_BLUE = 0xFF0088FF;         // Blue for mana
    private static final int BAR_CYAN = 0xFF00FFFF;         // Cyan for XP
    private static final int BAR_BACKGROUND = 0xFF333333;   // Dark gray background
    private static final int BAR_BORDER = 0xFF000000;       // Black border

    // Layout constants for the top-left status bar
    private static final int BAR_WIDTH = 100;
    private static final int BAR_HEIGHT = 8;        // Health/Mana bars
    private static final int XP_BAR_HEIGHT = 4;     // XP bar (thinner)
    private static final int START_X = 10;
    private static final int START_Y = 10;
    private static final int LINE_SPACING = 12;

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        if (player == null) return;

        // Get player capability - always render if it exists (no level check)
        PlayerCapability cap = player.getCapability(PlayerCapability.PLAYER_SYSTEM_CAP).orElse(null);
        if (cap == null) return;

        renderModernStatusBar(guiGraphics, cap, player);
    }

    private void renderModernStatusBar(GuiGraphics guiGraphics, PlayerCapability cap, LocalPlayer player) {
        Font font = Minecraft.getInstance().font;

        int currentX = START_X;
        int currentY = START_Y;

        // Line 1: Name/Title (White text with shadow)
        String titleText = "Level " + cap.getLevel() + " " + cap.getCurrentTitle();
        guiGraphics.drawString(font, titleText, currentX + 1, currentY + 1, TEXT_SHADOW, false); // Shadow
        guiGraphics.drawString(font, titleText, currentX, currentY, TEXT_WHITE, false); // Text

        currentY += LINE_SPACING;

        // Line 2: HP Bar (Red, height: 8px, width: 100px) with "100/100" text overlay
        renderBarWithText(guiGraphics, currentX, currentY, player.getHealth(), player.getMaxHealth(),
                         BAR_RED, String.format("%.0f/%.0f", player.getHealth(), player.getMaxHealth()));

        currentY += LINE_SPACING;

        // Line 3: Mana Bar (Blue, height: 8px, width: 100px) with "100/100" text overlay
        renderBarWithText(guiGraphics, currentX, currentY, cap.getCurrentMana(), cap.getMaxMana(),
                         BAR_BLUE, String.format("%.0f/%.0f", cap.getCurrentMana(), cap.getMaxMana()));

        currentY += LINE_SPACING;

        // Line 4: XP Bar (Cyan/Green, height: 4px, width: 100px). No text, just the bar.
        renderXPBar(guiGraphics, currentX, currentY, cap.getExperience(), cap.getExperienceToNext());
    }

    private void renderBarWithText(GuiGraphics guiGraphics, int x, int y, float current, float max, int barColor, String text) {
        Font font = Minecraft.getInstance().font;

        // Draw bar background with black border
        guiGraphics.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, BAR_BORDER); // Border
        guiGraphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, BAR_BACKGROUND); // Background

        // Draw bar progress
        if (max > 0) {
            float progress = Math.min(current / max, 1.0f);
            int progressWidth = (int) (BAR_WIDTH * progress);
            guiGraphics.fill(x, y, x + progressWidth, y + BAR_HEIGHT, barColor);
        }

        // Draw text overlay (centered on the bar)
        int textWidth = font.width(text);
        int textX = x + (BAR_WIDTH - textWidth) / 2;
        int textY = y + (BAR_HEIGHT - 8) / 2; // Center vertically in bar
        guiGraphics.drawString(font, text, textX, textY, TEXT_WHITE, false);
    }

    private void renderXPBar(GuiGraphics guiGraphics, int x, int y, int current, int max) {
        // Draw bar background with black border
        guiGraphics.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + XP_BAR_HEIGHT + 1, BAR_BORDER); // Border
        guiGraphics.fill(x, y, x + BAR_WIDTH, y + XP_BAR_HEIGHT, BAR_BACKGROUND); // Background

        // Draw bar progress (no text overlay)
        if (max > 0) {
            float progress = Math.min((float) current / max, 1.0f);
            int progressWidth = (int) (BAR_WIDTH * progress);
            guiGraphics.fill(x, y, x + progressWidth, y + XP_BAR_HEIGHT, BAR_CYAN);
        }
    }
}