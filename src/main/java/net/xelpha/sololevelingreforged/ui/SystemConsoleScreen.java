package net.xelpha.sololevelingreforged.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import org.joml.Quaternionf;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.xelpha.sololevelingreforged.ModSounds;
import net.xelpha.sololevelingreforged.core.PlayerCapability;
import net.xelpha.sololevelingreforged.network.AllocateStatPacket;
import net.xelpha.sololevelingreforged.network.ModNetworkRegistry;

/**
 * System Console - Advanced UI for stat allocation and player management
 * Features: Draggable, Animated, Interactive custom buttons, Audio feedback
 */
public class SystemConsoleScreen extends Screen {

    // Colors
    private static final int BG_COLOR = 0xCC000000; // Semi-transparent black
    private static final int BORDER_COLOR = 0xFF00BFFF; // Cyan border
    private static final int TEXT_COLOR = 0xFFFFFFFF; // White text
    private static final int BUTTON_NORMAL = 0xFF00BFFF; // Cyan buttons
    private static final int BUTTON_HOVER = 0xFFFFFFFF; // White on hover

    // Dimensions
    private static final int WINDOW_WIDTH = 300;
    private static final int WINDOW_HEIGHT = 220;
    private static final int BUTTON_SIZE = 12;

    // Layout constants
    private static final int HEADER_HEIGHT = 20;
    private static final int STATS_START_Y = 40;
    private static final int LEFT_COLUMN_WIDTH = 120;
    private static final int STAT_SPACING = 18;

    // Dragging variables
    private int guiLeft, guiTop;
    private boolean isDragging = false;
    private int dragOffsetX, dragOffsetY;

    // Animation variables
    private float scaleProgress = 0.0f;
    private int animationTicks = 0;

    // Player data
    private PlayerCapability capability;
    private LocalPlayer player;

    // Random for sound pitch variation
    private final RandomSource random = RandomSource.create();

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

        // Center the GUI initially
        this.guiLeft = (this.width - WINDOW_WIDTH) / 2;
        this.guiTop = (this.height - WINDOW_HEIGHT) / 2;

        // Reset animation
        this.scaleProgress = 0.0f;
        this.animationTicks = 0;

        // Play opening sound
        if (player != null) {
            player.playSound(ModSounds.INTERFACE_OPEN.get(),
                           1.0F, 0.9F + random.nextFloat() * 0.2F);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Update animation progress
        if (scaleProgress < 1.0f) {
            animationTicks++;
            scaleProgress = Math.min(1.0f, animationTicks * 0.2f); // 5 ticks to reach full scale
        }

        // Calculate scaled dimensions for centering
        float scaledWidth = WINDOW_WIDTH * scaleProgress;
        float scaledHeight = WINDOW_HEIGHT * scaleProgress;
        int centerX = guiLeft + WINDOW_WIDTH / 2;
        int centerY = guiTop + WINDOW_HEIGHT / 2;

        // Apply animation scaling
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(centerX, centerY, 0);
        poseStack.scale(scaleProgress, scaleProgress, 1.0f);
        poseStack.translate(-centerX, -centerY, 0);

        // Render background and border
        renderBackground(guiGraphics, (int)(centerX - scaledWidth/2), (int)(centerY - scaledHeight/2),
                        (int)scaledWidth, (int)scaledHeight);

        // Only render content when fully scaled
        if (scaleProgress >= 1.0f) {
            renderContent(guiGraphics, mouseX, mouseY, guiLeft, guiTop);
        }

        poseStack.popPose();

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderBackground(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // Background
        guiGraphics.fill(x, y, x + width, y + height, BG_COLOR);

        // Border
        guiGraphics.fill(x, y, x + width, y + 1, BORDER_COLOR); // Top
        guiGraphics.fill(x, y + height - 1, x + width, y + height, BORDER_COLOR); // Bottom
        guiGraphics.fill(x, y, x + 1, y + height, BORDER_COLOR); // Left
        guiGraphics.fill(x + width - 1, y, x + width, y + height, BORDER_COLOR); // Right
    }

    private void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y) {
        Font font = Minecraft.getInstance().font;

        // Header
        String headerText = "SYSTEM CONSOLE";
        int headerWidth = font.width(headerText);
        int headerX = x + (WINDOW_WIDTH - headerWidth) / 2;
        guiGraphics.drawString(font, headerText, headerX, y + 8, TEXT_COLOR, false);

        // Left column - Player model
        renderPlayerModel(guiGraphics, mouseX, mouseY, x + 10, y + STATS_START_Y);

        // Right column - Stats
        renderStats(guiGraphics, mouseX, mouseY, x + LEFT_COLUMN_WIDTH + 20, y + STATS_START_Y);
    }

    private void renderPlayerModel(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y) {
        if (player == null) return;

        // Background box
        guiGraphics.fill(x, y, x + 100, y + 120, 0xFF222222);

        // Center coordinates
        int playerX = x + 50;
        int playerY = y + 110; // Slightly higher to fit feet
        int scale = 40;        // Slightly larger

        // Calculate look direction
        // The values are negated/adjusted to make the player look AT the cursor
        float lookX = (float)(playerX - mouseX);
        float lookY = (float)(playerY - mouseY - 60); // Eye height adjustment

        drawEntity(guiGraphics, playerX, playerY, scale, lookX, lookY, player);
    }

    private void drawEntity(GuiGraphics guiGraphics, int x, int y, int scale, float mouseX, float mouseY, LocalPlayer entity) {
        float f = (float)Math.atan((double)(mouseX / 40.0F));
        float f1 = (float)Math.atan((double)(mouseY / 40.0F));

        PoseStack posestack = guiGraphics.pose();
        posestack.pushPose();
        posestack.translate((float)x, (float)y, 1050.0F);
        posestack.scale(1.0F, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();

        PoseStack modelPose = new PoseStack();
        modelPose.translate(0.0F, 0.0F, 1000.0F);
        modelPose.scale((float)scale, (float)scale, (float)scale);

        // --- THE FIX: Flip the Z axis to turn the model upright ---
        Quaternionf quaternionf = (new Quaternionf()).rotateZ((float)Math.PI);
        Quaternionf quaternionf1 = (new Quaternionf()).rotateX(f1 * 20.0F * ((float)Math.PI / 180.0F));
        quaternionf.mul(quaternionf1);
        modelPose.mulPose(quaternionf);
        // ---------------------------------------------------------

        float f2 = entity.yBodyRot;
        float f3 = entity.getYRot();
        float f4 = entity.getXRot();
        float f5 = entity.yHeadRotO;
        float f6 = entity.yHeadRot;

        entity.yBodyRot = 180.0F + f * 20.0F;
        entity.setYRot(180.0F + f * 40.0F);
        entity.setXRot(-f1 * 20.0F);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();

        net.minecraft.client.renderer.entity.EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        quaternionf1.conjugate();
        entityrenderdispatcher.overrideCameraOrientation(quaternionf1);
        entityrenderdispatcher.setRenderShadow(false);

        // Render
        com.mojang.blaze3d.systems.RenderSystem.runAsFancy(() -> {
            entityrenderdispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, modelPose, guiGraphics.bufferSource(), 15728880);
        });

        guiGraphics.flush();
        entityrenderdispatcher.setRenderShadow(true);

        // Restore original rotations
        entity.yBodyRot = f2;
        entity.setYRot(f3);
        entity.setXRot(f4);
        entity.yHeadRotO = f5;
        entity.yHeadRot = f6;

        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    private void renderStats(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y) {
        if (capability == null) return;

        Font font = Minecraft.getInstance().font;

        String[] statNames = {"STRENGTH", "AGILITY", "SENSE", "VITALITY", "INTELLIGENCE"};
        int[] statValues = {
            capability.getStrength(),
            capability.getAgility(),
            capability.getSense(),
            capability.getVitality(),
            capability.getIntelligence()
        };

        String[] statKeys = {"strength", "agility", "sense", "vitality", "intelligence"};

        for (int i = 0; i < statNames.length; i++) {
            int statY = y + i * STAT_SPACING;

            // Stat name and value
            String statText = statNames[i] + ": " + statValues[i];
            guiGraphics.drawString(font, statText, x, statY + 2, TEXT_COLOR, false);

            // Plus button
            int buttonX = x + 140;
            int buttonY = statY;
            boolean isHovered = isMouseOverButton(mouseX, mouseY, buttonX, buttonY);
            int buttonColor = isHovered ? BUTTON_HOVER : BUTTON_NORMAL;

            // Button background
            guiGraphics.fill(buttonX, buttonY, buttonX + BUTTON_SIZE, buttonY + BUTTON_SIZE, buttonColor);

            // Plus symbol
            String plusText = "+";
            int plusWidth = font.width(plusText);
            guiGraphics.drawString(font, plusText,
                                  buttonX + (BUTTON_SIZE - plusWidth) / 2,
                                  buttonY + (BUTTON_SIZE - 8) / 2, 0xFF000000, false);
        }

        // Available AP
        String apText = "AP: " + capability.getAvailableAP();
        guiGraphics.drawString(font, apText, x, y + 5 * STAT_SPACING + 10, TEXT_COLOR, false);
    }

    private boolean isMouseOverButton(int mouseX, int mouseY, int buttonX, int buttonY) {
        return mouseX >= buttonX && mouseX <= buttonX + BUTTON_SIZE &&
               mouseY >= buttonY && mouseY <= buttonY + BUTTON_SIZE;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left click
            // Check if clicking on header for dragging
            if (mouseX >= guiLeft && mouseX <= guiLeft + WINDOW_WIDTH &&
                mouseY >= guiTop && mouseY <= guiTop + HEADER_HEIGHT) {
                isDragging = true;
                dragOffsetX = (int)(mouseX - guiLeft);
                dragOffsetY = (int)(mouseY - guiTop);
                return true;
            }

            // Check stat buttons
            if (capability != null && capability.getAvailableAP() > 0) {
                int statsX = guiLeft + LEFT_COLUMN_WIDTH + 20;
                int statsY = guiTop + STATS_START_Y;

                String[] statKeys = {"strength", "agility", "sense", "vitality", "intelligence"};

                for (int i = 0; i < statKeys.length; i++) {
                    int buttonX = statsX + 140;
                    int buttonY = statsY + i * STAT_SPACING;

                    if (isMouseOverButton((int)mouseX, (int)mouseY, buttonX, buttonY)) {
                        // Allocate stat
                        ModNetworkRegistry.CHANNEL.sendToServer(new AllocateStatPacket(statKeys[i]));

                        // Play click sound
                        if (player != null) {
                            player.playSound(ModSounds.UI_CLICK.get(),
                                           1.0F, 0.8F + random.nextFloat() * 0.4F);
                        }

                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging && button == 0) {
            guiLeft = (int)(mouseX - dragOffsetX);
            guiTop = (int)(mouseY - dragOffsetY);

            // Keep window on screen
            guiLeft = Math.max(0, Math.min(width - WINDOW_WIDTH, guiLeft));
            guiTop = Math.max(0, Math.min(height - WINDOW_HEIGHT, guiTop));

            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isDragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC key
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}