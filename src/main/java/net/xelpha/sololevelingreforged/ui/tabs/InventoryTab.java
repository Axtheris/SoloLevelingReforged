package net.xelpha.sololevelingreforged.ui.tabs;

import net.minecraft.client.gui.GuiGraphics;
import net.xelpha.sololevelingreforged.core.PlayerCapability;
import net.xelpha.sololevelingreforged.ui.components.SLPanel;
import net.xelpha.sololevelingreforged.ui.core.UIColors;
import net.xelpha.sololevelingreforged.ui.core.UIRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Inventory tab showing system inventory and equipment slots
 * This is a framework for the inventory system - actual implementation in Priority 3
 */
public class InventoryTab extends BaseTab {
    
    // Layout
    private static final int SLOT_SIZE = 40;
    private static final int SLOT_SPACING = 4;
    private static final int INVENTORY_COLS = 8;
    private static final int INVENTORY_ROWS = 5;
    
    // Panels
    private SLPanel equipmentPanel;
    private SLPanel inventoryPanel;
    private SLPanel itemDetailsPanel;
    
    // Placeholder data
    private final List<ItemSlot> inventorySlots = new ArrayList<>();
    private final ItemSlot[] equipmentSlots = new ItemSlot[6]; // Weapon, Head, Chest, Legs, Feet, Accessory
    private ItemSlot selectedSlot = null;
    private ItemSlot hoveredSlot = null;
    
    public InventoryTab(int x, int y, int width, int height) {
        super(x, y, width, height);
    }
    
    @Override
    public String getTabName() {
        return "INVENTORY";
    }
    
    @Override
    protected void initComponents() {
        int padding = 12;
        
        // Equipment panel (left)
        int equipWidth = 120;
        equipmentPanel = new SLPanel(x + padding, y + padding, equipWidth, height - padding * 2)
            .withTitle("EQUIPMENT")
            .withCornerDecorations(true);
        addComponent(equipmentPanel);
        
        // Main inventory panel (center)
        int invX = x + padding + equipWidth + padding;
        int invWidth = width - equipWidth - padding * 3 - 150;
        inventoryPanel = new SLPanel(invX, y + padding, invWidth, height - padding * 2)
            .withTitle("SYSTEM INVENTORY")
            .withCornerDecorations(true)
            .withScrolling(true);
        addComponent(inventoryPanel);
        
        // Item details panel (right)
        int detailX = invX + invWidth + padding;
        itemDetailsPanel = new SLPanel(detailX, y + padding, 150 - padding, height - padding * 2)
            .withTitle("ITEM INFO")
            .withCornerDecorations(true);
        addComponent(itemDetailsPanel);
        
        // Initialize placeholder items
        initPlaceholderInventory();
    }
    
    private void initPlaceholderInventory() {
        // Initialize empty inventory slots
        for (int i = 0; i < INVENTORY_COLS * INVENTORY_ROWS; i++) {
            inventorySlots.add(null);
        }
        
        // Add some placeholder items
        inventorySlots.set(0, new ItemSlot("Knight Killer", "S", "weapon", 
            "A legendary dagger that killed a hundred knights."));
        inventorySlots.set(1, new ItemSlot("Kasaka's Venom Fang", "A", "weapon", 
            "A dagger dripping with deadly poison."));
        inventorySlots.set(2, new ItemSlot("Health Potion", "D", "consumable", 
            "Restores 50 HP when used."));
        inventorySlots.set(3, new ItemSlot("Mana Crystal", "C", "consumable", 
            "Restores 30 MP when used."));
        
        // Initialize equipment slots (all empty by default)
        
        // Equip one item
        equipmentSlots[0] = new ItemSlot("Knight Killer", "S", "weapon", 
            "A legendary dagger that killed a hundred knights.");
    }
    
    @Override
    public void updateData(PlayerCapability capability) {
        this.capability = capability;
        // In the future, this will sync with actual inventory data
    }
    
    @Override
    protected void renderContent(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        hoveredSlot = null;
        
        // Render equipment slots
        renderEquipmentSlots(graphics, mouseX, mouseY);
        
        // Render inventory grid
        renderInventoryGrid(graphics, mouseX, mouseY);
        
        // Render item details
        renderItemDetails(graphics);
        
        // Render currency display
        renderCurrency(graphics);
        
        // Render tooltip
        if (hoveredSlot != null) {
            renderItemTooltip(graphics, mouseX, mouseY, hoveredSlot);
        }
    }
    
    private void renderEquipmentSlots(GuiGraphics graphics, int mouseX, int mouseY) {
        String[] slotLabels = {"WPN", "HEAD", "BODY", "LEGS", "FEET", "ACC"};
        int startX = equipmentPanel.getX() + 12;
        int startY = equipmentPanel.getContentStartY() + 8;
        int slotHeight = 50;
        
        for (int i = 0; i < equipmentSlots.length; i++) {
            int slotY = startY + i * slotHeight;
            int slotWidth = equipmentPanel.getWidth() - 24;
            
            boolean hovered = UIRenderer.isMouseOver(mouseX, mouseY, startX, slotY, slotWidth, SLOT_SIZE);
            
            // Background
            int bgColor = hovered ? UIColors.BG_HOVER : UIColors.BG_HEADER;
            UIRenderer.fill(graphics, startX, slotY, slotWidth, SLOT_SIZE, bgColor);
            
            // Border
            int borderColor = hovered ? UIColors.PRIMARY : UIColors.BORDER;
            UIRenderer.horizontalLine(graphics, startX, slotY, slotWidth, borderColor);
            UIRenderer.horizontalLine(graphics, startX, slotY + SLOT_SIZE - 1, slotWidth, borderColor);
            UIRenderer.verticalLine(graphics, startX, slotY, SLOT_SIZE, borderColor);
            UIRenderer.verticalLine(graphics, startX + slotWidth - 1, slotY, SLOT_SIZE, borderColor);
            
            // Slot label
            UIRenderer.drawText(graphics, slotLabels[i], startX + 4, slotY + 4, UIColors.TEXT_MUTED);
            
            // Item or empty indicator
            ItemSlot item = equipmentSlots[i];
            if (item != null) {
                int rarityColor = UIColors.getRarityColor(item.rank);
                UIRenderer.drawTruncatedText(graphics, item.name, startX + 4, slotY + 16, slotWidth - 8, rarityColor);
                UIRenderer.drawText(graphics, "[" + item.rank + "]", startX + 4, slotY + 28, rarityColor);
                
                if (hovered) hoveredSlot = item;
            } else {
                UIRenderer.drawCenteredText(graphics, "Empty", startX + slotWidth / 2, slotY + 16, UIColors.TEXT_DISABLED);
            }
        }
    }
    
    private void renderInventoryGrid(GuiGraphics graphics, int mouseX, int mouseY) {
        int startX = inventoryPanel.getX() + 12;
        int startY = inventoryPanel.getContentStartY() + 8;
        
        for (int row = 0; row < INVENTORY_ROWS; row++) {
            for (int col = 0; col < INVENTORY_COLS; col++) {
                int index = row * INVENTORY_COLS + col;
                int slotX = startX + col * (SLOT_SIZE + SLOT_SPACING);
                int slotY = startY + row * (SLOT_SIZE + SLOT_SPACING);
                
                boolean hovered = UIRenderer.isMouseOver(mouseX, mouseY, slotX, slotY, SLOT_SIZE, SLOT_SIZE);
                ItemSlot item = index < inventorySlots.size() ? inventorySlots.get(index) : null;
                
                renderInventorySlot(graphics, slotX, slotY, item, hovered, selectedSlot == item);
                
                if (hovered && item != null) {
                    hoveredSlot = item;
                }
            }
        }
        
        // Show inventory capacity
        int usedSlots = (int) inventorySlots.stream().filter(s -> s != null).count();
        String capacityText = usedSlots + "/" + inventorySlots.size() + " slots used";
        UIRenderer.drawText(graphics, capacityText, startX, 
                           startY + INVENTORY_ROWS * (SLOT_SIZE + SLOT_SPACING) + 4, UIColors.TEXT_SECONDARY);
    }
    
    private void renderInventorySlot(GuiGraphics graphics, int x, int y, ItemSlot item, 
                                     boolean hovered, boolean selected) {
        // Background
        int bgColor = UIColors.BG_HEADER;
        if (selected) bgColor = UIColors.BG_ACTIVE;
        else if (hovered) bgColor = UIColors.BG_HOVER;
        
        UIRenderer.fill(graphics, x, y, SLOT_SIZE, SLOT_SIZE, bgColor);
        
        // Border
        int borderColor = UIColors.BORDER;
        if (item != null) {
            borderColor = UIColors.getRarityColor(item.rank);
        }
        if (selected) borderColor = UIColors.PRIMARY;
        else if (hovered && item != null) borderColor = UIColors.brighten(borderColor, 1.5f);
        
        UIRenderer.horizontalLine(graphics, x, y, SLOT_SIZE, borderColor);
        UIRenderer.horizontalLine(graphics, x, y + SLOT_SIZE - 1, SLOT_SIZE, borderColor);
        UIRenderer.verticalLine(graphics, x, y, SLOT_SIZE, borderColor);
        UIRenderer.verticalLine(graphics, x + SLOT_SIZE - 1, y, SLOT_SIZE, borderColor);
        
        if (item != null) {
            // Item icon placeholder (first letter)
            int rarityColor = UIColors.getRarityColor(item.rank);
            String initial = item.name.substring(0, 1);
            UIRenderer.drawCenteredText(graphics, initial, x + SLOT_SIZE / 2, y + SLOT_SIZE / 2 - 8, rarityColor);
            
            // Rank badge
            UIRenderer.drawText(graphics, item.rank, x + 2, y + 2, rarityColor);
        }
    }
    
    private void renderItemDetails(GuiGraphics graphics) {
        ItemSlot item = selectedSlot != null ? selectedSlot : hoveredSlot;
        
        if (item == null) {
            int centerX = itemDetailsPanel.getX() + itemDetailsPanel.getWidth() / 2;
            int centerY = itemDetailsPanel.getContentStartY() + 40;
            
            UIRenderer.drawCenteredText(graphics, "Select an", centerX, centerY, UIColors.TEXT_MUTED);
            UIRenderer.drawCenteredText(graphics, "item to view", centerX, centerY + 12, UIColors.TEXT_MUTED);
            UIRenderer.drawCenteredText(graphics, "details", centerX, centerY + 24, UIColors.TEXT_MUTED);
            return;
        }
        
        int detailX = itemDetailsPanel.getX() + 8;
        int detailY = itemDetailsPanel.getContentStartY() + 8;
        int maxWidth = itemDetailsPanel.getWidth() - 16;
        
        // Item name with rarity color
        int rarityColor = UIColors.getRarityColor(item.rank);
        UIRenderer.drawTruncatedText(graphics, item.name, detailX, detailY, maxWidth, rarityColor);
        
        // Rank badge
        String rankText = "[" + item.rank + " RANK]";
        UIRenderer.drawText(graphics, rankText, detailX, detailY + 14, rarityColor);
        
        // Type
        String typeText = item.type.toUpperCase();
        UIRenderer.drawText(graphics, typeText, detailX, detailY + 28, UIColors.TEXT_SECONDARY);
        
        // Description (word wrapped manually)
        int descY = detailY + 46;
        String[] words = item.description.split(" ");
        StringBuilder line = new StringBuilder();
        
        for (String word : words) {
            String test = line + (line.length() > 0 ? " " : "") + word;
            if (UIRenderer.getTextWidth(test) > maxWidth) {
                UIRenderer.drawText(graphics, line.toString(), detailX, descY, UIColors.TEXT);
                descY += 10;
                line = new StringBuilder(word);
            } else {
                if (line.length() > 0) line.append(" ");
                line.append(word);
            }
        }
        if (line.length() > 0) {
            UIRenderer.drawText(graphics, line.toString(), detailX, descY, UIColors.TEXT);
        }
    }
    
    private void renderCurrency(GuiGraphics graphics) {
        // Gold display at bottom of inventory panel
        int goldX = inventoryPanel.getX() + inventoryPanel.getWidth() - 12;
        int goldY = inventoryPanel.getY() + inventoryPanel.getHeight() - 20;
        
        String goldText = "Gold: 0"; // Placeholder - will be replaced with actual currency
        UIRenderer.drawRightAlignedText(graphics, goldText, goldX, goldY, UIColors.TERTIARY);
    }
    
    private void renderItemTooltip(GuiGraphics graphics, int mouseX, int mouseY, ItemSlot item) {
        List<String> lines = new ArrayList<>();
        lines.add("§l" + item.name);
        lines.add("§7[" + item.rank + " Rank] " + item.type);
        lines.add("");
        lines.add(item.description);
        
        net.xelpha.sololevelingreforged.ui.components.SLTooltip.render(graphics, mouseX, mouseY, lines);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            // Check inventory grid for clicked item
            int startX = inventoryPanel.getX() + 12;
            int startY = inventoryPanel.getContentStartY() + 8;
            
            for (int row = 0; row < INVENTORY_ROWS; row++) {
                for (int col = 0; col < INVENTORY_COLS; col++) {
                    int index = row * INVENTORY_COLS + col;
                    int slotX = startX + col * (SLOT_SIZE + SLOT_SPACING);
                    int slotY = startY + row * (SLOT_SIZE + SLOT_SPACING);
                    
                    if (UIRenderer.isMouseOver((int) mouseX, (int) mouseY, slotX, slotY, SLOT_SIZE, SLOT_SIZE)) {
                        ItemSlot item = index < inventorySlots.size() ? inventorySlots.get(index) : null;
                        selectedSlot = (selectedSlot == item) ? null : item;
                        return true;
                    }
                }
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    // Placeholder item data class
    private static class ItemSlot {
        String name;
        String rank; // E, D, C, B, A, S, SS, SSR
        String type; // weapon, armor, consumable, material
        String description;
        
        ItemSlot(String name, String rank, String type, String description) {
            this.name = name;
            this.rank = rank;
            this.type = type;
            this.description = description;
        }
    }
}
