package net.xelpha.sololevelingreforged.ui.tabs;

import net.minecraft.client.gui.GuiGraphics;
import net.xelpha.sololevelingreforged.core.PlayerCapability;
import net.xelpha.sololevelingreforged.ui.components.SLPanel;
import net.xelpha.sololevelingreforged.ui.core.UIAnimator;
import net.xelpha.sololevelingreforged.ui.core.UIColors;
import net.xelpha.sololevelingreforged.ui.core.UIRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Quests tab showing daily quests, main quests, and objectives
 * This is a framework for the quest system - actual quests will be added in Priority 2
 */
public class QuestsTab extends BaseTab {
    
    // Panels
    private SLPanel dailyQuestsPanel;
    private SLPanel mainQuestsPanel;
    private SLPanel urgentQuestPanel;
    
    // Placeholder quest data
    private final List<QuestEntry> dailyQuests = new ArrayList<>();
    private final List<QuestEntry> mainQuests = new ArrayList<>();
    private QuestEntry urgentQuest = null;
    
    // Animation
    private long animationTick = 0;
    
    public QuestsTab(int x, int y, int width, int height) {
        super(x, y, width, height);
    }
    
    @Override
    public String getTabName() {
        return "QUESTS";
    }
    
    @Override
    protected void initComponents() {
        int padding = 12;
        int halfWidth = (width - padding * 3) / 2;
        
        // Urgent quest panel (top, full width) - only shows when there's an urgent quest
        int urgentHeight = 60;
        urgentQuestPanel = new SLPanel(x + padding, y + padding, width - padding * 2, urgentHeight)
            .withTitle("⚠ URGENT QUEST")
            .withGlow(UIColors.QUEST_URGENT)
            .withCornerDecorations(true);
        addComponent(urgentQuestPanel);
        
        // Daily quests panel (left)
        int questPanelY = y + padding + urgentHeight + padding;
        int questPanelHeight = height - padding * 2 - urgentHeight - padding;
        
        dailyQuestsPanel = new SLPanel(x + padding, questPanelY, halfWidth, questPanelHeight)
            .withTitle("DAILY QUESTS")
            .withCornerDecorations(true)
            .withScrolling(true);
        addComponent(dailyQuestsPanel);
        
        // Main quests panel (right)
        mainQuestsPanel = new SLPanel(x + padding * 2 + halfWidth, questPanelY, halfWidth, questPanelHeight)
            .withTitle("MAIN QUESTS")
            .withCornerDecorations(true)
            .withScrolling(true);
        addComponent(mainQuestsPanel);
        
        // Initialize placeholder quests
        initPlaceholderQuests();
    }
    
    private void initPlaceholderQuests() {
        // Daily quests (Solo Leveling style)
        dailyQuests.add(new QuestEntry("Pushups", "Complete 100 pushups", 
            QuestType.DAILY, 0, 100, "5 XP per completion"));
        dailyQuests.add(new QuestEntry("Sit-ups", "Complete 100 sit-ups", 
            QuestType.DAILY, 0, 100, "5 XP per completion"));
        dailyQuests.add(new QuestEntry("Squats", "Complete 100 squats", 
            QuestType.DAILY, 0, 100, "5 XP per completion"));
        dailyQuests.add(new QuestEntry("10km Run", "Run 10 kilometers", 
            QuestType.DAILY, 0, 10000, "10 XP on completion"));
        dailyQuests.add(new QuestEntry("Monster Hunt", "Defeat 10 monsters", 
            QuestType.DAILY, 0, 10, "25 XP + Bonus rewards"));
        
        // Main quests
        mainQuests.add(new QuestEntry("Awakening", "Reach Level 10 to awaken your true power", 
            QuestType.MAIN, 1, 10, "Class Selection Unlocked"));
        mainQuests.add(new QuestEntry("Change Your Class", "Complete the class change dungeon", 
            QuestType.MAIN, 0, 1, "Necromancer Class"));
        mainQuests.add(new QuestEntry("Shadow Army", "Extract your first shadow soldier", 
            QuestType.MAIN, 0, 1, "Shadow Extraction Skill"));
        
        // No urgent quest by default
        urgentQuest = null;
    }
    
    @Override
    public void updateData(PlayerCapability capability) {
        this.capability = capability;
        
        // Update quest progress based on capability data
        if (capability != null) {
            // Example: Update awakening quest progress based on level
            for (QuestEntry quest : mainQuests) {
                if (quest.name.equals("Awakening")) {
                    quest.current = capability.getLevel();
                }
            }
        }
    }
    
    @Override
    public void tick() {
        super.tick();
        animationTick++;
    }
    
    @Override
    protected void renderContent(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Render urgent quest (if any)
        renderUrgentQuest(graphics);
        
        // Render daily quests
        renderQuestList(graphics, dailyQuests, dailyQuestsPanel, mouseX, mouseY);
        
        // Render main quests
        renderQuestList(graphics, mainQuests, mainQuestsPanel, mouseX, mouseY);
        
        // Render time remaining for daily quests
        renderDailyTimer(graphics);
    }
    
    private void renderUrgentQuest(GuiGraphics graphics) {
        if (urgentQuest == null) {
            // No urgent quest - show placeholder
            int centerX = urgentQuestPanel.getX() + urgentQuestPanel.getWidth() / 2;
            int centerY = urgentQuestPanel.getContentStartY() + 8;
            
            UIRenderer.drawCenteredText(graphics, "No urgent quests active", 
                                       centerX, centerY, UIColors.TEXT_MUTED);
            UIRenderer.drawCenteredText(graphics, "Urgent quests trigger on death threats", 
                                       centerX, centerY + 12, UIColors.TEXT_MUTED);
            return;
        }
        
        // Render urgent quest with pulsing effect
        float pulse = UIAnimator.pulse(animationTick, 0.1f);
        int textColor = UIColors.lerp(UIColors.TEXT, UIColors.QUEST_URGENT, pulse);
        
        int questX = urgentQuestPanel.getX() + 16;
        int questY = urgentQuestPanel.getContentStartY() + 4;
        
        UIRenderer.drawText(graphics, urgentQuest.name, questX, questY, textColor);
        UIRenderer.drawText(graphics, urgentQuest.description, questX, questY + 12, UIColors.TEXT_SECONDARY);
        
        // Progress bar
        float progress = urgentQuest.max > 0 ? (float) urgentQuest.current / urgentQuest.max : 0;
        UIRenderer.drawProgressBar(graphics, questX, questY + 28, 
                                  urgentQuestPanel.getWidth() - 32, 8, 
                                  progress, UIColors.QUEST_URGENT, UIColors.BG_HEADER);
    }
    
    private void renderQuestList(GuiGraphics graphics, List<QuestEntry> quests, SLPanel panel, 
                                 int mouseX, int mouseY) {
        int questX = panel.getX() + 12;
        int questY = panel.getContentStartY() + 8;
        int questHeight = 60;
        
        for (int i = 0; i < quests.size(); i++) {
            QuestEntry quest = quests.get(i);
            int currentY = questY + i * questHeight;
            
            boolean hovered = UIRenderer.isMouseOver(mouseX, mouseY, 
                questX - 4, currentY - 2, panel.getWidth() - 24, questHeight - 4);
            
            renderQuestEntry(graphics, quest, questX, currentY, panel.getWidth() - 24, hovered);
        }
    }
    
    private void renderQuestEntry(GuiGraphics graphics, QuestEntry quest, int x, int y, int width, boolean hovered) {
        // Background on hover
        if (hovered) {
            UIRenderer.fill(graphics, x - 4, y - 2, width, 58, UIColors.BG_HOVER);
        }
        
        // Quest type indicator
        int typeColor = getQuestTypeColor(quest.type);
        UIRenderer.fill(graphics, x - 4, y, 3, 50, typeColor);
        
        // Quest name
        boolean completed = quest.current >= quest.max;
        int nameColor = completed ? UIColors.QUEST_COMPLETE : UIColors.TEXT;
        String nameText = quest.name + (completed ? " ✓" : "");
        UIRenderer.drawText(graphics, nameText, x + 4, y, nameColor);
        
        // Quest description
        UIRenderer.drawTruncatedText(graphics, quest.description, x + 4, y + 12, width - 8, UIColors.TEXT_SECONDARY);
        
        // Progress bar
        float progress = quest.max > 0 ? (float) quest.current / quest.max : 0;
        int barColor = completed ? UIColors.QUEST_COMPLETE : typeColor;
        UIRenderer.drawProgressBar(graphics, x + 4, y + 28, width - 8, 8, 
                                  progress, barColor, UIColors.BG_HEADER);
        
        // Progress text
        String progressText = quest.current + "/" + quest.max;
        UIRenderer.drawText(graphics, progressText, x + 4, y + 40, UIColors.TEXT_SECONDARY);
        
        // Reward
        UIRenderer.drawRightAlignedText(graphics, "Reward: " + quest.reward, x + width - 4, y + 40, UIColors.TERTIARY);
    }
    
    private void renderDailyTimer(GuiGraphics graphics) {
        // Calculate time until midnight reset
        java.time.LocalTime now = java.time.LocalTime.now();
        java.time.LocalTime midnight = java.time.LocalTime.MAX;
        java.time.Duration duration = java.time.Duration.between(now, midnight);
        
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;
        
        String timeText = String.format("Resets in: %02d:%02d:%02d", hours, minutes, seconds);
        
        int timerX = dailyQuestsPanel.getX() + dailyQuestsPanel.getWidth() - 8;
        int timerY = dailyQuestsPanel.getY() + 8;
        
        UIRenderer.drawRightAlignedText(graphics, timeText, timerX, timerY, UIColors.TEXT_WARNING);
    }
    
    private int getQuestTypeColor(QuestType type) {
        return switch (type) {
            case DAILY -> UIColors.QUEST_DAILY;
            case MAIN -> UIColors.QUEST_MAIN;
            case URGENT -> UIColors.QUEST_URGENT;
            case SIDE -> UIColors.QUEST_SIDE;
        };
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Quest clicking can be implemented later for quest details/tracking
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    // Quest type enum
    private enum QuestType {
        DAILY, MAIN, URGENT, SIDE
    }
    
    // Placeholder quest data class
    private static class QuestEntry {
        String name;
        String description;
        QuestType type;
        int current;
        int max;
        String reward;
        
        QuestEntry(String name, String description, QuestType type, int current, int max, String reward) {
            this.name = name;
            this.description = description;
            this.type = type;
            this.current = current;
            this.max = max;
            this.reward = reward;
        }
    }
}
