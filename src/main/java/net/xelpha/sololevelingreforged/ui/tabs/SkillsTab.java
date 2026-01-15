package net.xelpha.sololevelingreforged.ui.tabs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.xelpha.sololevelingreforged.core.PlayerCapability;
import net.xelpha.sololevelingreforged.skills.Skill;
import net.xelpha.sololevelingreforged.skills.SkillRegistry;
import net.xelpha.sololevelingreforged.skills.SkillType;
import net.xelpha.sololevelingreforged.ui.components.SLPanel;
import net.xelpha.sololevelingreforged.ui.core.UIColors;
import net.xelpha.sololevelingreforged.ui.core.UIRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Skills tab showing active and passive skills with cooldowns and level progress
 * This is a framework for the skills system - actual skills will be added in Priority 1
 */
public class SkillsTab extends BaseTab {
    
    // Layout - adjusted for proper fitting
    private static final int SKILL_SLOT_SIZE = 48;
    private static final int SKILL_SPACING = 6;
    private static final int SKILLS_PER_ROW = 3; // Reduced to fit in half-width panels
    
    // Panels
    private SLPanel activeSkillsPanel;
    private SLPanel passiveSkillsPanel;
    private SLPanel skillDetailsPanel;
    
    // Real skill data from the backend
    private final List<SkillSlot> activeSkills = new ArrayList<>();
    private final List<SkillSlot> passiveSkills = new ArrayList<>();
    private SkillSlot selectedSkill = null;
    private SkillSlot hoveredSkill = null;
    
    public SkillsTab(int x, int y, int width, int height) {
        super(x, y, width, height);
    }
    
    @Override
    public String getTabName() {
        return "SKILLS";
    }
    
    @Override
    protected void initComponents() {
        int padding = 8;
        int panelWidth = (width - padding * 3) / 2;
        int topPanelHeight = (int)(height * 0.55); // 55% for skill panels
        
        // Active skills panel (left)
        activeSkillsPanel = new SLPanel(x + padding, y + padding, panelWidth, topPanelHeight)
            .withTitle("ACTIVE SKILLS")
            .withCornerDecorations(true)
            .withScrolling(true);
        addComponent(activeSkillsPanel);
        
        // Passive skills panel (right)
        passiveSkillsPanel = new SLPanel(x + padding * 2 + panelWidth, y + padding, 
                                        panelWidth, topPanelHeight)
            .withTitle("PASSIVE SKILLS")
            .withCornerDecorations(true)
            .withScrolling(true);
        addComponent(passiveSkillsPanel);
        
        // Skill details panel (bottom, full width)
        int detailsPanelY = y + padding + topPanelHeight + padding;
        int detailsPanelHeight = height - topPanelHeight - padding * 3;
        skillDetailsPanel = new SLPanel(x + padding, detailsPanelY, 
                                       width - padding * 2, detailsPanelHeight)
            .withTitle("SKILL DETAILS")
            .withCornerDecorations(true);
        addComponent(skillDetailsPanel);
        
        // Initialize skills from registry and player data
        initSkills();
    }
    
    private void initSkills() {
        // Clear existing skills
        activeSkills.clear();
        passiveSkills.clear();

        // Get all skills from the registry
        Map<net.minecraft.resources.ResourceLocation, Skill> allSkills = SkillRegistry.getAllSkills();

        // Categorize skills and create skill slots
        for (Map.Entry<net.minecraft.resources.ResourceLocation, Skill> entry : allSkills.entrySet()) {
            Skill skill = entry.getValue();
            boolean isLearned = capability != null && capability.hasSkill(entry.getKey());
            int skillLevel = capability != null ? capability.getSkillLevel(entry.getKey()) : 0;
            boolean isLocked = capability != null && !skill.canUnlock(capability);

            SkillSlot slot = new SkillSlot(skill, isLearned, skillLevel, isLocked);

            if (skill.getType() == SkillType.ACTIVE_OFFENSIVE ||
                skill.getType() == SkillType.ACTIVE_DEFENSIVE ||
                skill.getType() == SkillType.ACTIVE_UTILITY) {
                activeSkills.add(slot);
            } else {
                passiveSkills.add(slot);
            }
        }
    }
    
    @Override
    public void updateData(PlayerCapability capability) {
        this.capability = capability;

        // Refresh skill data from the updated capability
        initSkills();

        // Update cooldowns and levels for existing skills
        for (SkillSlot slot : activeSkills) {
            if (slot.skill != null && capability != null) {
                slot.currentLevel = capability.getSkillLevel(slot.skill.getId());
                slot.isLearned = capability.hasSkill(slot.skill.getId());
                slot.isLocked = !slot.skill.canUnlock(capability);
            }
        }

        for (SkillSlot slot : passiveSkills) {
            if (slot.skill != null && capability != null) {
                slot.currentLevel = capability.getSkillLevel(slot.skill.getId());
                slot.isLearned = capability.hasSkill(slot.skill.getId());
                slot.isLocked = !slot.skill.canUnlock(capability);
            }
        }
    }
    
    @Override
    protected void renderContent(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        hoveredSkill = null;
        
        // Render active skill slots with clipping
        graphics.enableScissor(
            activeSkillsPanel.getX() + 1, 
            activeSkillsPanel.getContentStartY(),
            activeSkillsPanel.getX() + activeSkillsPanel.getWidth() - 1,
            activeSkillsPanel.getY() + activeSkillsPanel.getHeight() - 1
        );
        renderSkillGrid(graphics, activeSkills, activeSkillsPanel, mouseX, mouseY, true);
        graphics.disableScissor();
        
        // Render passive skill slots with clipping
        graphics.enableScissor(
            passiveSkillsPanel.getX() + 1, 
            passiveSkillsPanel.getContentStartY(),
            passiveSkillsPanel.getX() + passiveSkillsPanel.getWidth() - 1,
            passiveSkillsPanel.getY() + passiveSkillsPanel.getHeight() - 1
        );
        renderSkillGrid(graphics, passiveSkills, passiveSkillsPanel, mouseX, mouseY, false);
        graphics.disableScissor();
        
        // Render skill details (no clipping needed - fits panel)
        renderSkillDetails(graphics);
        
        // Render tooltip for hovered skill (outside scissor so it's not clipped)
        if (hoveredSkill != null) {
            renderSkillTooltip(graphics, mouseX, mouseY, hoveredSkill);
        }
    }
    
    private void renderSkillGrid(GuiGraphics graphics, List<SkillSlot> skills, SLPanel panel, 
                                 int mouseX, int mouseY, boolean isActive) {
        int startX = panel.getX() + 12;
        int startY = panel.getContentStartY() + 8;
        
        for (int i = 0; i < skills.size(); i++) {
            SkillSlot skill = skills.get(i);
            int row = i / SKILLS_PER_ROW;
            int col = i % SKILLS_PER_ROW;
            
            int slotX = startX + col * (SKILL_SLOT_SIZE + SKILL_SPACING);
            int slotY = startY + row * (SKILL_SLOT_SIZE + SKILL_SPACING);
            
            boolean hovered = UIRenderer.isMouseOver(mouseX, mouseY, slotX, slotY, SKILL_SLOT_SIZE, SKILL_SLOT_SIZE);
            if (hovered) {
                hoveredSkill = skill;
            }
            
            renderSkillSlot(graphics, skill, slotX, slotY, hovered, selectedSkill == skill, isActive);
        }
    }
    
    private void renderSkillSlot(GuiGraphics graphics, SkillSlot skill, int x, int y, 
                                 boolean hovered, boolean selected, boolean isActive) {
        // Background
        int bgColor;
        if (skill.isLocked) {
            bgColor = UIColors.BG_HEADER;
        } else if (!skill.isLearned) {
            bgColor = UIColors.BG_HOVER; // Highlight for learnable skills
        } else {
            bgColor = UIColors.BG_PANEL; // Normal for learned skills
        }

        if (selected) {
            bgColor = UIColors.BG_ACTIVE;
        } else if (hovered && !skill.isLocked) {
            bgColor = UIColors.BG_HOVER;
        }
        
        UIRenderer.fill(graphics, x, y, SKILL_SLOT_SIZE, SKILL_SLOT_SIZE, bgColor);
        
        // Border
        int borderColor = skill.isLocked ? UIColors.SKILL_LOCKED :
                         (isActive ? UIColors.SKILL_ACTIVE : UIColors.SKILL_PASSIVE);
        if (selected) {
            borderColor = UIColors.PRIMARY;
        } else if (hovered && !skill.isLocked) {
            borderColor = UIColors.brighten(borderColor, 1.5f);
        }
        
        UIRenderer.horizontalLine(graphics, x, y, SKILL_SLOT_SIZE, borderColor);
        UIRenderer.horizontalLine(graphics, x, y + SKILL_SLOT_SIZE - 1, SKILL_SLOT_SIZE, borderColor);
        UIRenderer.verticalLine(graphics, x, y, SKILL_SLOT_SIZE, borderColor);
        UIRenderer.verticalLine(graphics, x + SKILL_SLOT_SIZE - 1, y, SKILL_SLOT_SIZE, borderColor);
        
        // Skill icon placeholder (first letter)
        String initial = skill.getName().substring(0, 1);
        int textColor = skill.isLocked ? UIColors.TEXT_DISABLED : UIColors.TEXT;
        UIRenderer.drawCenteredText(graphics, initial, x + SKILL_SLOT_SIZE / 2,
                                   y + SKILL_SLOT_SIZE / 2 - 4, textColor);

        // Skill state indicator
        if (!skill.isLocked) {
            if (skill.isLearned && skill.getLevel() > 0) {
                // Show level for learned skills
                String levelText = "Lv." + skill.getLevel();
                UIRenderer.drawCenteredText(graphics, levelText, x + SKILL_SLOT_SIZE / 2,
                                           y + SKILL_SLOT_SIZE - 10, UIColors.TEXT_SECONDARY);
            } else {
                // Show "LEARN" for skills that can be learned
                UIRenderer.drawCenteredText(graphics, "LEARN", x + SKILL_SLOT_SIZE / 2,
                                           y + SKILL_SLOT_SIZE - 10, UIColors.TEXT_SUCCESS);
            }
        }

        // Cooldown indicator for active skills
        if (isActive && skill.skill != null && skill.isLearned) {
            float cooldownProgress = skill.skill.getCooldownProgress();
            if (cooldownProgress < 1.0f) {
                // Draw cooldown overlay
                int overlayHeight = (int) (SKILL_SLOT_SIZE * (1.0f - cooldownProgress));
                int cooldownColor = UIColors.withAlpha(UIColors.BG_OVERLAY, 180);
                UIRenderer.fill(graphics, x + 1, y + 1, SKILL_SLOT_SIZE - 2, overlayHeight, cooldownColor);

                // Cooldown text
                String cdText = String.format("%.1fs", skill.skill.getRemainingCooldownTicks() / 20.0f);
                UIRenderer.drawCenteredText(graphics, cdText, x + SKILL_SLOT_SIZE / 2,
                                           y + SKILL_SLOT_SIZE / 2 - 4, UIColors.TEXT_WARNING);
            }
        }

        // Lock overlay
        if (skill.isLocked) {
            int lockOverlay = UIColors.withAlpha(0xFF000000, 150);
            UIRenderer.fill(graphics, x + 1, y + 1, SKILL_SLOT_SIZE - 2, SKILL_SLOT_SIZE - 2, lockOverlay);
            UIRenderer.drawCenteredText(graphics, "?", x + SKILL_SLOT_SIZE / 2, 
                                       y + SKILL_SLOT_SIZE / 2 - 4, UIColors.TEXT_MUTED);
        }
        
        // Glow effect for selected
        if (selected) {
            int glowColor = UIColors.withAlpha(UIColors.PRIMARY, 40);
            graphics.fill(x - 2, y - 2, x + SKILL_SLOT_SIZE + 2, y + SKILL_SLOT_SIZE + 2, glowColor);
        }
    }
    
    private void renderSkillDetails(GuiGraphics graphics) {
        SkillSlot skill = selectedSkill != null ? selectedSkill :
                         (hoveredSkill != null ? hoveredSkill : null);

        if (skill == null || skill.skill == null) {
            // Show placeholder message
            int centerX = skillDetailsPanel.getX() + skillDetailsPanel.getWidth() / 2;
            int centerY = skillDetailsPanel.getContentStartY() + skillDetailsPanel.getHeight() / 2 - 20;

            UIRenderer.drawCenteredText(graphics, "Select a skill to view details",
                                       centerX, centerY, UIColors.TEXT_MUTED);
            UIRenderer.drawCenteredText(graphics, "Skills will be unlocked as you progress",
                                       centerX, centerY + 14, UIColors.TEXT_MUTED);
            return;
        }

        int detailX = skillDetailsPanel.getX() + 16;
        int detailY = skillDetailsPanel.getContentStartY() + 8;

        // Skill name
        int nameColor = skill.isLocked ? UIColors.TEXT_DISABLED : UIColors.TEXT_TITLE;
        UIRenderer.drawText(graphics, skill.getName() + (skill.isLocked ? " (LOCKED)" : ""),
                           detailX, detailY, nameColor);

        // Skill type
        String typeText = skill.skill.getType().toString().replace("_", " ").toUpperCase() + " SKILL";
        int typeColor = skill.skill.getType().name().contains("ACTIVE") ? UIColors.SKILL_ACTIVE : UIColors.SKILL_PASSIVE;
        UIRenderer.drawText(graphics, typeText, detailX, detailY + 14, typeColor);

        // Skill level and progress
        if (skill.isLocked) {
            UIRenderer.drawText(graphics, "Required Level: " + skill.skill.getUnlockLevel(),
                               detailX, detailY + 28, UIColors.TEXT_WARNING);
        } else if (!skill.isLearned) {
            UIRenderer.drawText(graphics, "Available to Learn", detailX, detailY + 28, UIColors.TEXT_SUCCESS);
            UIRenderer.drawText(graphics, "Click to learn this skill!", detailX, detailY + 42, UIColors.TEXT_SECONDARY);
        } else {
            UIRenderer.drawText(graphics, "Level: " + skill.getLevel() + "/" + skill.skill.getMaxLevel(),
                               detailX, detailY + 28, UIColors.TEXT_SECONDARY);

            // Level progress bar
            if (skill.skill.getMaxLevel() > 1) {
                int barWidth = 100;
                int barHeight = 8;
                int barY = detailY + 42;

                UIRenderer.fill(graphics, detailX, barY, barWidth, barHeight, UIColors.BAR_XP_BG);
                int fillWidth = (int) (barWidth * skill.getLevel() / (float) skill.skill.getMaxLevel());
                UIRenderer.fill(graphics, detailX, barY, fillWidth, barHeight, UIColors.BAR_XP);
            }
        }

        // Description
        UIRenderer.drawTruncatedText(graphics, skill.getDescription(), detailX, detailY + 60,
                                    skillDetailsPanel.getWidth() - 32, UIColors.TEXT);

        // Skill stats for active skills
        if (!skill.isLocked && skill.skill.getType().name().contains("ACTIVE")) {
            int infoY = detailY + 80;
            UIRenderer.drawText(graphics, "Mana Cost: " + skill.skill.getManaCost(),
                               detailX, infoY, UIColors.BAR_MANA);
            UIRenderer.drawText(graphics, "Cooldown: " + String.format("%.1fs", skill.skill.getCooldownSeconds()),
                               detailX + 120, infoY, UIColors.TEXT_SECONDARY);

            // Damage multiplier if applicable
            if (skill.skill.getDamageMultiplier() > 1.0f) {
                UIRenderer.drawText(graphics, "Damage: " + String.format("%.1fx", skill.skill.getDamageMultiplier()),
                                   detailX, infoY + 14, UIColors.TEXT_SUCCESS);
            }

            // Effect duration if applicable
            if (skill.skill.getEffectDuration() > 0) {
                UIRenderer.drawText(graphics, "Duration: " + (skill.skill.getEffectDuration() / 20) + "s",
                                   detailX + 120, infoY + 14, UIColors.TEXT_SECONDARY);
            }
        }
    }
    
    private void renderSkillTooltip(GuiGraphics graphics, int mouseX, int mouseY, SkillSlot skill) {
        // Enhanced tooltip with real skill data
        List<String> lines = new ArrayList<>();
        lines.add("§l" + skill.getName());
        lines.add("§7" + skill.skill.getType().toString().replace("_", " "));

        if (skill.isLocked) {
            lines.add("§cLOCKED - Level " + skill.skill.getUnlockLevel() + " required");
        } else if (!skill.isLearned) {
            lines.add("§eCLICK TO LEARN");
            if (skill.skill.getType().name().contains("ACTIVE")) {
                lines.add("§bMana: " + skill.skill.getManaCost() + " §7Cooldown: " +
                         String.format("%.1fs", skill.skill.getCooldownSeconds()));
            }
        } else {
            lines.add("§aLevel " + skill.getLevel() + "/" + skill.skill.getMaxLevel());
            if (skill.skill.getType().name().contains("ACTIVE")) {
                lines.add("§bMana: " + skill.skill.getManaCost() + " §7Cooldown: " +
                         String.format("%.1fs", skill.skill.getCooldownSeconds()));
            }
        }
        lines.add("");
        lines.add(skill.getDescription());

        // Use SLTooltip
        net.xelpha.sololevelingreforged.ui.components.SLTooltip.render(graphics, mouseX, mouseY, lines);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            // Check active skills
            SkillSlot clicked = getSkillAtPosition(activeSkills, activeSkillsPanel, (int) mouseX, (int) mouseY);
            if (clicked == null) {
                clicked = getSkillAtPosition(passiveSkills, passiveSkillsPanel, (int) mouseX, (int) mouseY);
            }
            
            if (clicked != null && !clicked.isLocked) {
                // If skill is not learned yet, learn it
                if (!clicked.isLearned && capability != null) {
                    boolean learned = capability.learnSkill(clicked.skill.getId());
                    if (learned) {
                        // Update the skill slot to reflect it's now learned
                        clicked.isLearned = true;
                        clicked.currentLevel = 1;
                        // Refresh the UI to show the change
                        updateData(capability);
                        // Show success message
                        Minecraft minecraft = Minecraft.getInstance();
                        if (minecraft.player != null) {
                            minecraft.player.sendSystemMessage(
                                net.minecraft.network.chat.Component.literal(
                                    "§aLearned skill: " + clicked.skill.getName() + "!"));
                        }
                        return true;
                    }
                } else {
                    // Skill is already learned, select it for details
                    selectedSkill = (selectedSkill == clicked) ? null : clicked;
                    return true;
                }
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    private SkillSlot getSkillAtPosition(List<SkillSlot> skills, SLPanel panel, int mouseX, int mouseY) {
        int startX = panel.getX() + 12;
        int startY = panel.getContentStartY() + 8;
        
        for (int i = 0; i < skills.size(); i++) {
            int row = i / SKILLS_PER_ROW;
            int col = i % SKILLS_PER_ROW;
            
            int slotX = startX + col * (SKILL_SLOT_SIZE + SKILL_SPACING);
            int slotY = startY + row * (SKILL_SLOT_SIZE + SKILL_SPACING);
            
            if (UIRenderer.isMouseOver(mouseX, mouseY, slotX, slotY, SKILL_SLOT_SIZE, SKILL_SLOT_SIZE)) {
                return skills.get(i);
            }
        }
        
        return null;
    }
    
    // Skill slot wrapper for UI display
    private static class SkillSlot {
        Skill skill;
        boolean isLearned;
        int currentLevel;
        boolean isLocked;

        SkillSlot(Skill skill, boolean isLearned, int currentLevel, boolean isLocked) {
            this.skill = skill;
            this.isLearned = isLearned;
            this.currentLevel = currentLevel;
            this.isLocked = isLocked;
        }

        String getName() { return skill != null ? skill.getName() : "Unknown"; }
        String getType() { return skill != null ? skill.getType().toString().toLowerCase() : "unknown"; }
        String getDescription() { return skill != null ? skill.getDescription() : ""; }
        int getLevel() { return currentLevel; }
    }
}
