package net.xelpha.sololevelingreforged.ui.tabs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.xelpha.sololevelingreforged.core.PlayerCapability;
import net.xelpha.sololevelingreforged.network.ModNetworkRegistry;
import net.xelpha.sololevelingreforged.network.WithdrawItemPacket;
import net.xelpha.sololevelingreforged.ui.components.SLPanel;
import net.xelpha.sololevelingreforged.ui.core.UIAnimator;
import net.xelpha.sololevelingreforged.ui.core.UIColors;
import net.xelpha.sololevelingreforged.ui.core.UIRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * System Inventory Tab - Solo Leveling dimensional storage
 * 
 * Items are stored via keybind (V) while playing.
 * This tab is for viewing and withdrawing items only.
 * 
 * Design inspired by the System's interface in the manhwa.
 */
public class InventoryTab extends BaseTab {
    
    // Layout
    private static final int SLOT_SIZE = 38;
    private static final int SLOT_SPACING = 2;
    private static final int GRID_COLS = 7;
    private static final int GRID_ROWS = 6;
    
    // Panels
    private SLPanel inventoryPanel;
    private SLPanel detailsPanel;
    private SLPanel infoPanel;
    
    // State
    private int selectedIndex = -1;
    private int hoveredIndex = -1;
    private int scrollOffset = 0;
    private long animationTick = 0;
    
    // Cached inventory for display
    private List<ItemStack> displayedItems = new ArrayList<>();
    
    public InventoryTab(int x, int y, int width, int height) {
        super(x, y, width, height);
    }
    
    @Override
    public String getTabName() {
        return "INVENTORY";
    }
    
    @Override
    protected void initComponents() {
        int padding = 8;
        
        // Info panel at top (storage info and gold)
        int infoPanelHeight = 40;
        infoPanel = new SLPanel(x + padding, y + padding, width - padding * 2, infoPanelHeight)
            .withTitle("SYSTEM STORAGE")
            .withCornerDecorations(true);
        addComponent(infoPanel);
        
        // Main inventory grid (left/center)
        int gridPanelWidth = (int)(width * 0.65);
        int gridPanelY = y + padding + infoPanelHeight + padding;
        int gridPanelHeight = height - padding * 3 - infoPanelHeight;
        
        inventoryPanel = new SLPanel(x + padding, gridPanelY, gridPanelWidth, gridPanelHeight)
            .withTitle("STORED ITEMS")
            .withCornerDecorations(true)
            .withScrolling(true);
        addComponent(inventoryPanel);
        
        // Details panel (right side)
        int detailsPanelX = x + padding + gridPanelWidth + padding;
        int detailsPanelWidth = width - gridPanelWidth - padding * 3;
        
        detailsPanel = new SLPanel(detailsPanelX, gridPanelY, detailsPanelWidth, gridPanelHeight)
            .withTitle("ITEM DETAILS")
            .withCornerDecorations(true);
        addComponent(detailsPanel);
    }
    
    @Override
    public void updateData(PlayerCapability capability) {
        this.capability = capability;
        if (capability != null) {
            // Update displayed items from capability
            displayedItems = new ArrayList<>(capability.getSystemInventory());
        }
    }
    
    @Override
    public void tick() {
        super.tick();
        animationTick++;
    }
    
    @Override
    protected void renderContent(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        hoveredIndex = -1;
        
        // Render storage info
        renderStorageInfo(graphics);
        
        // Render inventory grid with clipping
        graphics.enableScissor(
            inventoryPanel.getX() + 1,
            inventoryPanel.getContentStartY(),
            inventoryPanel.getX() + inventoryPanel.getWidth() - 1,
            inventoryPanel.getY() + inventoryPanel.getHeight() - 1
        );
        renderInventoryGrid(graphics, mouseX, mouseY);
        graphics.disableScissor();
        
        // Render item details
        renderItemDetails(graphics, mouseX, mouseY);
        
        // Render hint text at bottom
        renderHints(graphics);
    }
    
    private void renderStorageInfo(GuiGraphics graphics) {
        int infoX = infoPanel.getX() + 12;
        int infoY = infoPanel.getContentStartY() + 2;
        
        if (capability != null) {
            // Storage count
            int itemCount = capability.getInventorySize();
            String storageText = "Items: " + itemCount + " / ∞";
            UIRenderer.drawText(graphics, storageText, infoX, infoY, UIColors.TEXT);
            
            // Gold display
            int gold = capability.getGold();
            String goldText = "Gold: " + formatNumber(gold);
            int goldX = infoPanel.getX() + infoPanel.getWidth() - UIRenderer.getTextWidth(goldText) - 12;
            UIRenderer.drawText(graphics, goldText, goldX, infoY, UIColors.TERTIARY);
            
            // Store keybind hint
            String hintText = "Press [V] while holding an item to store";
            int hintX = infoX + 150;
            UIRenderer.drawText(graphics, hintText, hintX, infoY, UIColors.TEXT_MUTED);
        }
    }
    
    private void renderInventoryGrid(GuiGraphics graphics, int mouseX, int mouseY) {
        int startX = inventoryPanel.getX() + 8;
        int startY = inventoryPanel.getContentStartY() + 4;
        
        // Calculate visible range based on scroll
        int visibleSlots = GRID_COLS * GRID_ROWS;
        int startIndex = scrollOffset * GRID_COLS;
        int endIndex = Math.min(startIndex + visibleSlots, displayedItems.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            int localIndex = i - startIndex;
            int col = localIndex % GRID_COLS;
            int row = localIndex / GRID_COLS;
            
            int slotX = startX + col * (SLOT_SIZE + SLOT_SPACING);
            int slotY = startY + row * (SLOT_SIZE + SLOT_SPACING);
            
            ItemStack stack = displayedItems.get(i);
            boolean hovered = UIRenderer.isMouseOver(mouseX, mouseY, slotX, slotY, SLOT_SIZE, SLOT_SIZE);
            boolean selected = selectedIndex == i;
            
            if (hovered) {
                hoveredIndex = i;
            }
            
            renderInventorySlot(graphics, slotX, slotY, stack, hovered, selected, i);
        }
        
        // Draw empty slots to fill grid
        for (int i = endIndex - startIndex; i < visibleSlots; i++) {
            int col = i % GRID_COLS;
            int row = i / GRID_COLS;
            
            int slotX = startX + col * (SLOT_SIZE + SLOT_SPACING);
            int slotY = startY + row * (SLOT_SIZE + SLOT_SPACING);
            
            renderEmptySlot(graphics, slotX, slotY);
        }
        
        // Scroll indicator if needed
        if (displayedItems.size() > visibleSlots) {
            int totalRows = (displayedItems.size() + GRID_COLS - 1) / GRID_COLS;
            int visibleRows = GRID_ROWS;
            float scrollProgress = (float) scrollOffset / Math.max(1, totalRows - visibleRows);
            
            int scrollBarX = inventoryPanel.getX() + inventoryPanel.getWidth() - 8;
            int scrollBarY = inventoryPanel.getContentStartY() + 4;
            int scrollBarHeight = GRID_ROWS * (SLOT_SIZE + SLOT_SPACING) - 8;
            
            // Track
            UIRenderer.fill(graphics, scrollBarX, scrollBarY, 4, scrollBarHeight, UIColors.BG_HEADER);
            
            // Thumb
            int thumbHeight = Math.max(20, scrollBarHeight / totalRows * visibleRows);
            int thumbY = scrollBarY + (int)((scrollBarHeight - thumbHeight) * scrollProgress);
            UIRenderer.fill(graphics, scrollBarX, thumbY, 4, thumbHeight, UIColors.PRIMARY_DIM);
        }
    }
    
    private void renderInventorySlot(GuiGraphics graphics, int x, int y, ItemStack stack, 
                                     boolean hovered, boolean selected, int index) {
        // Background
        int bgColor = selected ? UIColors.BG_ACTIVE : (hovered ? UIColors.BG_HOVER : UIColors.BG_HEADER);
        UIRenderer.fill(graphics, x, y, SLOT_SIZE, SLOT_SIZE, bgColor);
        
        // Border with rarity color
        int borderColor = UIColors.BORDER;
        if (!stack.isEmpty()) {
            borderColor = getItemRarityColor(stack);
        }
        if (selected) {
            borderColor = UIColors.PRIMARY;
        } else if (hovered) {
            borderColor = UIColors.brighten(borderColor, 1.3f);
        }
        
        UIRenderer.horizontalLine(graphics, x, y, SLOT_SIZE, borderColor);
        UIRenderer.horizontalLine(graphics, x, y + SLOT_SIZE - 1, SLOT_SIZE, borderColor);
        UIRenderer.verticalLine(graphics, x, y, SLOT_SIZE, borderColor);
        UIRenderer.verticalLine(graphics, x + SLOT_SIZE - 1, y, SLOT_SIZE, borderColor);
        
        // Render item
        if (!stack.isEmpty()) {
            // Item icon
            int itemX = x + (SLOT_SIZE - 16) / 2;
            int itemY = y + (SLOT_SIZE - 16) / 2 - 2;
            graphics.renderItem(stack, itemX, itemY);
            
            // Stack count
            if (stack.getCount() > 1) {
                String count = String.valueOf(stack.getCount());
                int countX = x + SLOT_SIZE - UIRenderer.getTextWidth(count) - 2;
                UIRenderer.drawText(graphics, count, countX, y + SLOT_SIZE - 10, UIColors.TEXT);
            }
        }
        
        // Selection glow
        if (selected) {
            float pulse = UIAnimator.pulse(animationTick, 0.1f);
            int glowAlpha = (int)(20 + 15 * pulse);
            graphics.fill(x - 1, y - 1, x + SLOT_SIZE + 1, y + SLOT_SIZE + 1, 
                         UIColors.withAlpha(UIColors.PRIMARY, glowAlpha));
        }
    }
    
    private void renderEmptySlot(GuiGraphics graphics, int x, int y) {
        UIRenderer.fill(graphics, x, y, SLOT_SIZE, SLOT_SIZE, UIColors.withAlpha(UIColors.BG_HEADER, 100));
        
        int borderColor = UIColors.withAlpha(UIColors.BORDER, 80);
        UIRenderer.horizontalLine(graphics, x, y, SLOT_SIZE, borderColor);
        UIRenderer.horizontalLine(graphics, x, y + SLOT_SIZE - 1, SLOT_SIZE, borderColor);
        UIRenderer.verticalLine(graphics, x, y, SLOT_SIZE, borderColor);
        UIRenderer.verticalLine(graphics, x + SLOT_SIZE - 1, y, SLOT_SIZE, borderColor);
    }
    
    private void renderItemDetails(GuiGraphics graphics, int mouseX, int mouseY) {
        int detailX = detailsPanel.getX() + 10;
        int detailY = detailsPanel.getContentStartY() + 8;
        int maxWidth = detailsPanel.getWidth() - 20;
        
        int displayIndex = selectedIndex >= 0 ? selectedIndex : hoveredIndex;
        
        if (displayIndex >= 0 && displayIndex < displayedItems.size()) {
            ItemStack stack = displayedItems.get(displayIndex);
            
            if (!stack.isEmpty()) {
                // Item name with rarity color
                int rarityColor = getItemRarityColor(stack);
                String name = stack.getHoverName().getString();
                UIRenderer.drawTruncatedText(graphics, name, detailX, detailY, maxWidth, rarityColor);
                
                // Item type/category
                String type = getItemType(stack);
                UIRenderer.drawText(graphics, type, detailX, detailY + 14, UIColors.TEXT_SECONDARY);
                
                // Stack count
                if (stack.getCount() > 1) {
                    UIRenderer.drawText(graphics, "Quantity: " + stack.getCount(), 
                                       detailX, detailY + 28, UIColors.TEXT_SECONDARY);
                }
                
                // Action hint
                int hintY = detailsPanel.getY() + detailsPanel.getHeight() - 30;
                UIRenderer.drawCenteredText(graphics, "Click to select", 
                    detailsPanel.getX() + detailsPanel.getWidth() / 2, hintY, UIColors.TEXT_MUTED);
                UIRenderer.drawCenteredText(graphics, "Double-click to withdraw", 
                    detailsPanel.getX() + detailsPanel.getWidth() / 2, hintY + 12, UIColors.PRIMARY_DIM);
                
                return;
            }
        }
        
        // Empty state
        int centerX = detailsPanel.getX() + detailsPanel.getWidth() / 2;
        int centerY = detailsPanel.getContentStartY() + 40;
        
        UIRenderer.drawCenteredText(graphics, "Select an item", centerX, centerY, UIColors.TEXT_MUTED);
        UIRenderer.drawCenteredText(graphics, "to view details", centerX, centerY + 12, UIColors.TEXT_MUTED);
    }
    
    private void renderHints(GuiGraphics graphics) {
        if (displayedItems.isEmpty()) {
            // No items hint
            int centerX = inventoryPanel.getX() + inventoryPanel.getWidth() / 2;
            int centerY = inventoryPanel.getContentStartY() + 60;
            
            UIRenderer.drawCenteredText(graphics, "Your System Inventory is empty", 
                                       centerX, centerY, UIColors.TEXT_MUTED);
            UIRenderer.drawCenteredText(graphics, "Hold an item and press [V] to store it", 
                                       centerX, centerY + 14, UIColors.PRIMARY_DIM);
        }
    }
    
    private int getItemRarityColor(ItemStack stack) {
        // Check for custom rarity in NBT or use vanilla rarity
        if (stack.hasTag()) {
            var tag = stack.getTag();
            if (tag != null && tag.contains("SoloLevelingRank")) {
                String rank = tag.getString("SoloLevelingRank");
                return UIColors.getRarityColor(rank);
            }
        }
        
        // Fallback to vanilla rarity
        return switch (stack.getRarity()) {
            case COMMON -> UIColors.RARITY_COMMON;
            case UNCOMMON -> UIColors.RARITY_UNCOMMON;
            case RARE -> UIColors.RARITY_RARE;
            case EPIC -> UIColors.RARITY_EPIC;
            default -> UIColors.TEXT;
        };
    }
    
    private String getItemType(ItemStack stack) {
        // Get item category
        if (stack.getItem() instanceof net.minecraft.world.item.SwordItem) return "Weapon - Sword";
        if (stack.getItem() instanceof net.minecraft.world.item.AxeItem) return "Weapon - Axe";
        if (stack.getItem() instanceof net.minecraft.world.item.BowItem) return "Weapon - Bow";
        if (stack.getItem() instanceof net.minecraft.world.item.ArmorItem armor) {
            return "Armor - " + armor.getType().getName();
        }
        if (stack.getItem().isEdible()) return "Consumable - Food";
        if (stack.getItem() instanceof net.minecraft.world.item.PotionItem) return "Consumable - Potion";
        return "Item";
    }
    
    private String formatNumber(int number) {
        if (number >= 1000000) return String.format("%.1fM", number / 1000000.0);
        if (number >= 1000) return String.format("%.1fK", number / 1000.0);
        return String.valueOf(number);
    }
    
    // Track double click
    private long lastClickTime = 0;
    private int lastClickIndex = -1;
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && hoveredIndex >= 0) {
            long currentTime = System.currentTimeMillis();
            
            // Check for double click (withdraw)
            if (hoveredIndex == lastClickIndex && currentTime - lastClickTime < 400) {
                // Double click - withdraw item
                withdrawItem(hoveredIndex);
                selectedIndex = -1;
                lastClickIndex = -1;
            } else {
                // Single click - select
                selectedIndex = (selectedIndex == hoveredIndex) ? -1 : hoveredIndex;
                lastClickIndex = hoveredIndex;
                lastClickTime = currentTime;
            }
            return true;
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        // Scroll the inventory grid
        if (displayedItems.size() > GRID_COLS * GRID_ROWS) {
            int totalRows = (displayedItems.size() + GRID_COLS - 1) / GRID_COLS;
            int maxScroll = Math.max(0, totalRows - GRID_ROWS);
            
            scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - delta));
            return true;
        }
        
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
    
    private void withdrawItem(int index) {
        if (index >= 0 && index < displayedItems.size()) {
            // Send withdraw packet to server
            ModNetworkRegistry.CHANNEL.sendToServer(new WithdrawItemPacket(index));
            
            // Optimistic UI update (will be corrected by server sync)
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§b[System] §7Withdrawing item..."),
                    true
                );
            }
        }
    }
}
