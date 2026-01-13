package net.xelpha.sololevelingreforged.ui.tabs;

import net.minecraft.client.gui.GuiGraphics;
import net.xelpha.sololevelingreforged.core.PlayerCapability;
import net.xelpha.sololevelingreforged.ui.components.SLPanel;
import net.xelpha.sololevelingreforged.ui.components.SLProgressBar;
import net.xelpha.sololevelingreforged.ui.core.UIAnimator;
import net.xelpha.sololevelingreforged.ui.core.UIColors;
import net.xelpha.sololevelingreforged.ui.core.UIRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Skills tab showing active and passive skills with cooldowns and level progress
 * This is a framework for the skills system - actual skills will be added in Priority 1
 */
public class SkillsTab extends BaseTab {
    
    // Layout
    private static final int SKILL_SLOT_SIZE = 48;
    private static final int SKILL_SPACING = 8;
    private static final int SKILLS_PER_ROW = 5;
    
    // Panels
    private SLPanel activeSkillsPanel;
    private SLPanel passiveSkillsPanel;
    private SLPanel skillDetailsPanel;
    
    // Placeholder skill data (to be replaced with actual Skill system)
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
        int padding = 12;
        int panelWidth = (width - padding * 3) / 2;
        
        // Active skills panel (left)
        activeSkillsPanel = new SLPanel(x + padding, y + padding, panelWidth, height / 2 - padding)
            .withTitle("ACTIVE SKILLS")
            .withCornerDecorations(true)
            .withScrolling(true);
        addComponent(activeSkillsPanel);
        
        // Passive skills panel (right)
        passiveSkillsPanel = new SLPanel(x + padding + panelWidth + padding, y + padding, 
                                        panelWidth, height / 2 - padding)
            .withTitle("PASSIVE SKILLS")
            .withCornerDecorations(true)
            .withScrolling(true);
        addComponent(passiveSkillsPanel);
        
        // Skill details panel (bottom)
        skillDetailsPanel = new SLPanel(x + padding, y + height / 2 + padding / 2, 
                                       width - padding * 2, height / 2 - padding * 2)
            .withTitle("SKILL DETAILS")
            .withCornerDecorations(true);
        addComponent(skillDetailsPanel);
        
        // Initialize placeholder skills
        initPlaceholderSkills();
    }
    
    private void initPlaceholderSkills() {
        // Active skills (placeholder data - will be replaced by actual skill system)
        activeSkills.add(new SkillSlot("Shadow Extraction", "active", 1, false, 
            "Extract shadows from defeated enemies to add to your army."));
        activeSkills.add(new SkillSlot("Dash", "active", 1, false, 
            "Move at high speed, becoming temporarily invulnerable."));
        activeSkills.add(new SkillSlot("Dagger Throw", "active", 1, false, 
            "Throw a homing dagger that deals damage based on your stats."));
        activeSkills.add(new SkillSlot("Shadow Prison", "active", 0, true, 
            "Immobilize enemies in a dark sphere. Requires Level 10."));
        activeSkills.add(new SkillSlot("Berserk Shadows", "active", 0, true, 
            "Boost all shadow soldiers' attack power. Requires Level 20."));
        
        // Passive skills
        passiveSkills.add(new SkillSlot("Will to Recover", "passive", 1, false, 
            "Automatically heal when HP drops below 30%."));
        passiveSkills.add(new SkillSlot("Detoxification", "passive", 0, true, 
            "Auto-neutralize poisons and debuffs. Requires Level 15."));
        passiveSkills.add(new SkillSlot("Shadow Affinity", "passive", 0, true, 
            "Boost shadow ability effectiveness by 10-30%. Requires Level 25."));
        passiveSkills.add(new SkillSlot("Predator", "passive", 0, true, 
            "Gain 10-50% more XP from monsters. Requires Level 30."));
    }
    
    @Override
    public void updateData(PlayerCapability capability) {
        this.capability = capability;
        // In the future, this will update skill levels, cooldowns, etc.
    }
    
    @Override
    protected void renderContent(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        hoveredSkill = null;
        
        // Render active skill slots
        renderSkillGrid(graphics, activeSkills, activeSkillsPanel, mouseX, mouseY, true);
        
        // Render passive skill slots
        renderSkillGrid(graphics, passiveSkills, passiveSkillsPanel, mouseX, mouseY, false);
        
        // Render skill details
        renderSkillDetails(graphics);
        
        // Render tooltip for hovered skill
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
        int bgColor = skill.locked ? UIColors.BG_HEADER : UIColors.BG_PANEL;
        if (selected) {
            bgColor = UIColors.BG_ACTIVE;
        } else if (hovered && !skill.locked) {
            bgColor = UIColors.BG_HOVER;
        }
        
        UIRenderer.fill(graphics, x, y, SKILL_SLOT_SIZE, SKILL_SLOT_SIZE, bgColor);
        
        // Border
        int borderColor = skill.locked ? UIColors.SKILL_LOCKED : 
                         (isActive ? UIColors.SKILL_ACTIVE : UIColors.SKILL_PASSIVE);
        if (selected) {
            borderColor = UIColors.PRIMARY;
        } else if (hovered && !skill.locked) {
            borderColor = UIColors.brighten(borderColor, 1.5f);
        }
        
        UIRenderer.horizontalLine(graphics, x, y, SKILL_SLOT_SIZE, borderColor);
        UIRenderer.horizontalLine(graphics, x, y + SKILL_SLOT_SIZE - 1, SKILL_SLOT_SIZE, borderColor);
        UIRenderer.verticalLine(graphics, x, y, SKILL_SLOT_SIZE, borderColor);
        UIRenderer.verticalLine(graphics, x + SKILL_SLOT_SIZE - 1, y, SKILL_SLOT_SIZE, borderColor);
        
        // Skill icon placeholder (first letter)
        String initial = skill.name.substring(0, 1);
        int textColor = skill.locked ? UIColors.TEXT_DISABLED : UIColors.TEXT;
        UIRenderer.drawCenteredText(graphics, initial, x + SKILL_SLOT_SIZE / 2, 
                                   y + SKILL_SLOT_SIZE / 2 - 4, textColor);
        
        // Skill level indicator
        if (skill.level > 0) {
            String levelText = "Lv." + skill.level;
            UIRenderer.drawCenteredText(graphics, levelText, x + SKILL_SLOT_SIZE / 2, 
                                       y + SKILL_SLOT_SIZE - 10, UIColors.TEXT_SECONDARY);
        }
        
        // Lock overlay
        if (skill.locked) {
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
        
        if (skill == null) {
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
        int nameColor = skill.locked ? UIColors.TEXT_DISABLED : UIColors.TEXT_TITLE;
        UIRenderer.drawText(graphics, skill.name + (skill.locked ? " (LOCKED)" : ""), 
                           detailX, detailY, nameColor);
        
        // Skill type
        String typeText = skill.type.toUpperCase() + " SKILL";
        int typeColor = skill.type.equals("active") ? UIColors.SKILL_ACTIVE : UIColors.SKILL_PASSIVE;
        UIRenderer.drawText(graphics, typeText, detailX, detailY + 14, typeColor);
        
        // Skill level
        if (!skill.locked) {
            UIRenderer.drawText(graphics, "Level: " + skill.level, detailX, detailY + 28, UIColors.TEXT_SECONDARY);
        }
        
        // Description
        UIRenderer.drawTruncatedText(graphics, skill.description, detailX, detailY + 46, 
                                    skillDetailsPanel.getWidth() - 32, UIColors.TEXT);
        
        // Mana cost / Cooldown placeholders
        if (!skill.locked && skill.type.equals("active")) {
            int infoY = detailY + 66;
            UIRenderer.drawText(graphics, "Mana Cost: 20", detailX, infoY, UIColors.BAR_MANA);
            UIRenderer.drawText(graphics, "Cooldown: 5s", detailX + 120, infoY, UIColors.TEXT_SECONDARY);
        }
    }
    
    private void renderSkillTooltip(GuiGraphics graphics, int mouseX, int mouseY, SkillSlot skill) {
        // Simple tooltip
        List<String> lines = new ArrayList<>();
        lines.add("§l" + skill.name);
        lines.add("§7" + (skill.type.equals("active") ? "Active" : "Passive") + " Skill");
        if (skill.locked) {
            lines.add("§cLOCKED");
        } else {
            lines.add("Level " + skill.level);
        }
        lines.add("");
        lines.add(skill.description);
        
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
            
            if (clicked != null && !clicked.locked) {
                selectedSkill = (selectedSkill == clicked) ? null : clicked;
                return true;
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
    
    // Placeholder skill data class
    private static class SkillSlot {
        String name;
        String type; // "active" or "passive"
        int level;
        boolean locked;
        String description;
        
        SkillSlot(String name, String type, int level, boolean locked, String description) {
            this.name = name;
            this.type = type;
            this.level = level;
            this.locked = locked;
            this.description = description;
        }
    }
}
