package net.xelpha.sololevelingreforged.ui.tabs;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.xelpha.sololevelingreforged.core.PlayerCapability;
import net.xelpha.sololevelingreforged.network.AllocateStatPacket;
import net.xelpha.sololevelingreforged.network.ModNetworkRegistry;
import net.xelpha.sololevelingreforged.ui.components.SLPanel;
import net.xelpha.sololevelingreforged.ui.components.SLProgressBar;
import net.xelpha.sololevelingreforged.ui.components.SLStatDisplay;
import net.xelpha.sololevelingreforged.ui.core.UIColors;
import net.xelpha.sololevelingreforged.ui.core.UIRenderer;
import org.joml.Quaternionf;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Status tab showing player model, level, resources, and stat allocation
 */
public class StatusTab extends BaseTab {
    
    // Layout constants
    private static final int PLAYER_PANEL_WIDTH = 140;
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
        
        // Left side: Player model panel
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
    protected void renderContent(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Render player model in the player panel
        renderPlayerModel(graphics, mouseX, mouseY);
        
        // Render AP indicator
        if (capability != null) {
            int ap = capability.getAvailableAP();
            String apText = "AP: " + ap;
            int apColor = ap > 0 ? UIColors.TEXT_SUCCESS : UIColors.TEXT_SECONDARY;
            
            // Draw AP below player model
            int apX = playerPanel.getX() + playerPanel.getWidth() / 2;
            int apY = playerPanel.getY() + playerPanel.getHeight() - 24;
            UIRenderer.drawCenteredText(graphics, apText, apX, apY, apColor);
            
            if (ap > 0) {
                UIRenderer.drawCenteredText(graphics, "Points Available!", apX, apY + 10, UIColors.TEXT_SUCCESS);
            }
        }
        
        // Render title/rank if available
        if (capability != null) {
            String title = capability.getCurrentTitle();
            String levelText = "Lv. " + capability.getLevel();
            
            int titleX = playerPanel.getX() + playerPanel.getWidth() / 2;
            int titleY = playerPanel.getY() + 30;
            
            UIRenderer.drawCenteredText(graphics, levelText, titleX, titleY, UIColors.PRIMARY);
            UIRenderer.drawCenteredText(graphics, title, titleX, titleY + 12, UIColors.TEXT_SECONDARY);
        }
    }
    
    private void renderPlayerModel(GuiGraphics graphics, int mouseX, int mouseY) {
        if (player == null) return;
        
        int modelX = playerPanel.getX() + playerPanel.getWidth() / 2;
        int modelY = playerPanel.getY() + playerPanel.getHeight() - 50;
        int scale = 50;
        
        // Draw background glow
        int glowColor = UIColors.withAlpha(UIColors.PRIMARY, 20);
        int glowRadius = 45;
        graphics.fill(modelX - glowRadius, modelY - glowRadius * 2, 
                     modelX + glowRadius, modelY + 10, glowColor);
        
        // Calculate rotation based on mouse or auto-rotate
        float rotation;
        if (draggingModel) {
            rotation = modelRotation;
        } else {
            // Subtle auto-rotation when not dragging
            rotation = (float) (System.currentTimeMillis() / 100.0 % 360);
            rotation = (float) Math.sin(rotation * 0.02) * 15;
        }
        
        drawEntity(graphics, modelX, modelY, scale, rotation, 0, player);
    }
    
    private void drawEntity(GuiGraphics guiGraphics, int x, int y, int scale, 
                           float rotationY, float rotationX, LocalPlayer entity) {
        PoseStack posestack = guiGraphics.pose();
        posestack.pushPose();
        posestack.translate((float) x, (float) y, 1050.0F);
        posestack.scale(1.0F, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();
        
        PoseStack modelPose = new PoseStack();
        modelPose.translate(0.0F, 0.0F, 1000.0F);
        modelPose.scale((float) scale, (float) scale, (float) scale);
        
        // Apply rotation
        Quaternionf quaternionf = (new Quaternionf()).rotateZ((float) Math.PI);
        Quaternionf rotationQuat = (new Quaternionf()).rotateX(rotationX * ((float) Math.PI / 180.0F));
        quaternionf.mul(rotationQuat);
        modelPose.mulPose(quaternionf);
        
        // Save original rotations
        float f2 = entity.yBodyRot;
        float f3 = entity.getYRot();
        float f4 = entity.getXRot();
        float f5 = entity.yHeadRotO;
        float f6 = entity.yHeadRot;
        
        // Apply custom rotation
        entity.yBodyRot = 180.0F + rotationY;
        entity.setYRot(180.0F + rotationY);
        entity.setXRot(-rotationX);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();
        
        var entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        rotationQuat.conjugate();
        entityrenderdispatcher.overrideCameraOrientation(rotationQuat);
        entityrenderdispatcher.setRenderShadow(false);
        
        RenderSystem.runAsFancy(() -> {
            entityrenderdispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, 
                                         modelPose, guiGraphics.bufferSource(), 15728880);
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
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Check if clicking on player model area for rotation
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
            modelRotation += (float) (mouseX - lastMouseX) * 0.5f;
            lastMouseX = mouseX;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
    
    private boolean isMouseOverPlayerModel(double mouseX, double mouseY) {
        if (playerPanel == null) return false;
        
        int modelX = playerPanel.getX() + playerPanel.getWidth() / 2;
        int modelY = playerPanel.getY() + playerPanel.getHeight() / 2;
        int radius = 60;
        
        return mouseX >= modelX - radius && mouseX <= modelX + radius 
            && mouseY >= modelY - radius && mouseY <= modelY + radius;
    }
}
