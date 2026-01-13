package net.xelpha.sololevelingreforged.ui.tabs;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.xelpha.sololevelingreforged.core.PlayerCapability;
import net.xelpha.sololevelingreforged.network.ModNetworkRegistry;
import net.xelpha.sololevelingreforged.network.WithdrawItemPacket;
import net.xelpha.sololevelingreforged.ui.components.SLButton;
import net.xelpha.sololevelingreforged.ui.components.SLPanel;
import net.xelpha.sololevelingreforged.ui.core.UIAnimator;
import net.xelpha.sololevelingreforged.ui.core.UIColors;
import net.xelpha.sololevelingreforged.ui.core.UIRenderer;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * System Inventory Tab - Solo Leveling dimensional storage
 * 
 * Features:
 * - Unlimited stack sizes (system storage)
 * - Sorting by name, amount, mod ID
 * - Large 3D item preview
 */
public class InventoryTab extends BaseTab {
    
    // Layout
    private static final int SLOT_SIZE = 38;
    private static final int SLOT_SPACING = 2;
    private static final int GRID_COLS = 7;
    private static final int GRID_ROWS = 5;
    
    // Panels
    private SLPanel inventoryPanel;
    private SLPanel detailsPanel;
    private SLPanel infoPanel;
    
    // Sorting buttons
    private SLButton sortNameBtn;
    private SLButton sortAmountBtn;
    private SLButton sortModBtn;
    
    // State
    private int selectedIndex = -1;
    private int hoveredIndex = -1;
    private int scrollOffset = 0;
    private long animationTick = 0;
    
    // Sorting
    private SortMode currentSort = SortMode.NONE;
    private boolean sortAscending = true;
    
    // 3D Item Preview
    private float itemRotation = 0;
    private boolean draggingItem = false;
    private double lastMouseX;
    
    // Cached inventory for display
    private List<ItemStack> displayedItems = new ArrayList<>();
    
    private enum SortMode { NONE, NAME, AMOUNT, MOD_ID }
    
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
        
        // Info panel at top (storage info, gold, sort buttons)
        int infoPanelHeight = 50;
        infoPanel = new SLPanel(x + padding, y + padding, width - padding * 2, infoPanelHeight)
            .withTitle("SYSTEM STORAGE")
            .withCornerDecorations(true);
        addComponent(infoPanel);
        
        // Sort buttons
        int btnY = infoPanel.getContentStartY() + 16;
        int btnWidth = 55;
        int btnSpacing = 4;
        int sortStartX = infoPanel.getX() + 12;
        
        sortNameBtn = new SLButton(sortStartX, btnY, btnWidth, 16, "Name", btn -> applySorting(SortMode.NAME));
        sortNameBtn.withStyle(SLButton.Style.SECONDARY);
        addComponent(sortNameBtn);
        
        sortAmountBtn = new SLButton(sortStartX + btnWidth + btnSpacing, btnY, btnWidth, 16, "Amount", btn -> applySorting(SortMode.AMOUNT));
        sortAmountBtn.withStyle(SLButton.Style.SECONDARY);
        addComponent(sortAmountBtn);
        
        sortModBtn = new SLButton(sortStartX + (btnWidth + btnSpacing) * 2, btnY, btnWidth, 16, "Mod", btn -> applySorting(SortMode.MOD_ID));
        sortModBtn.withStyle(SLButton.Style.SECONDARY);
        addComponent(sortModBtn);
        
        // Main inventory grid (left/center)
        int gridPanelWidth = (int)(width * 0.60);
        int gridPanelY = y + padding + infoPanelHeight + padding;
        int gridPanelHeight = height - padding * 3 - infoPanelHeight;
        
        inventoryPanel = new SLPanel(x + padding, gridPanelY, gridPanelWidth, gridPanelHeight)
            .withTitle("STORED ITEMS")
            .withCornerDecorations(true)
            .withScrolling(true);
        addComponent(inventoryPanel);
        
        // Details panel (right side) - wider for better preview
        int detailsPanelX = x + padding + gridPanelWidth + padding;
        int detailsPanelWidth = width - gridPanelWidth - padding * 3;
        
        detailsPanel = new SLPanel(detailsPanelX, gridPanelY, detailsPanelWidth, gridPanelHeight)
            .withTitle("ITEM PREVIEW")
            .withCornerDecorations(true);
        addComponent(detailsPanel);
    }
    
    private void applySorting(SortMode mode) {
        if (currentSort == mode) {
            sortAscending = !sortAscending; // Toggle direction
        } else {
            currentSort = mode;
            sortAscending = true;
        }
        sortInventory();
    }
    
    private void sortInventory() {
        if (currentSort == SortMode.NONE || displayedItems.isEmpty()) return;
        
        Comparator<ItemStack> comparator = switch (currentSort) {
            case NAME -> Comparator.comparing(s -> s.getHoverName().getString());
            case AMOUNT -> Comparator.comparingInt(ItemStack::getCount);
            case MOD_ID -> Comparator.comparing(s -> {
                var key = ForgeRegistries.ITEMS.getKey(s.getItem());
                return key != null ? key.getNamespace() : "";
            });
            default -> null;
        };
        
        if (comparator != null) {
            if (!sortAscending) {
                comparator = comparator.reversed();
            }
            displayedItems.sort(comparator);
        }
    }
    
    @Override
    public void updateData(PlayerCapability capability) {
        this.capability = capability;
        if (capability != null) {
            displayedItems = new ArrayList<>(capability.getSystemInventory());
            sortInventory(); // Re-apply current sort
        }
    }
    
    @Override
    public void tick() {
        super.tick();
        animationTick++;
        
        // Auto-rotate item when not dragging
        if (!draggingItem) {
            itemRotation += 0.8f;
            if (itemRotation >= 360) itemRotation -= 360;
        }
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
        
        // Render item details with large preview
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
            
            // Sort indicator
            if (currentSort != SortMode.NONE) {
                String sortText = "Sorted: " + currentSort.name() + (sortAscending ? " ↑" : " ↓");
                int sortX = infoX + 200;
                UIRenderer.drawText(graphics, sortText, sortX, infoY, UIColors.PRIMARY_DIM);
            }
        }
    }
    
    private void renderInventoryGrid(GuiGraphics graphics, int mouseX, int mouseY) {
        int startX = inventoryPanel.getX() + 8;
        int startY = inventoryPanel.getContentStartY() + 4;
        
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
            
            if (hovered) hoveredIndex = i;
            
            renderInventorySlot(graphics, slotX, slotY, stack, hovered, selected);
        }
        
        // Empty slots
        for (int i = endIndex - startIndex; i < visibleSlots; i++) {
            int col = i % GRID_COLS;
            int row = i / GRID_COLS;
            int slotX = startX + col * (SLOT_SIZE + SLOT_SPACING);
            int slotY = startY + row * (SLOT_SIZE + SLOT_SPACING);
            renderEmptySlot(graphics, slotX, slotY);
        }
        
        // Scroll indicator
        if (displayedItems.size() > visibleSlots) {
            int totalRows = (displayedItems.size() + GRID_COLS - 1) / GRID_COLS;
            int visibleRows = GRID_ROWS;
            float scrollProgress = (float) scrollOffset / Math.max(1, totalRows - visibleRows);
            
            int scrollBarX = inventoryPanel.getX() + inventoryPanel.getWidth() - 8;
            int scrollBarY = inventoryPanel.getContentStartY() + 4;
            int scrollBarHeight = GRID_ROWS * (SLOT_SIZE + SLOT_SPACING) - 8;
            
            UIRenderer.fill(graphics, scrollBarX, scrollBarY, 4, scrollBarHeight, UIColors.BG_HEADER);
            int thumbHeight = Math.max(20, scrollBarHeight / totalRows * visibleRows);
            int thumbY = scrollBarY + (int)((scrollBarHeight - thumbHeight) * scrollProgress);
            UIRenderer.fill(graphics, scrollBarX, thumbY, 4, thumbHeight, UIColors.PRIMARY_DIM);
        }
    }
    
    private void renderInventorySlot(GuiGraphics graphics, int x, int y, ItemStack stack, 
                                     boolean hovered, boolean selected) {
        int bgColor = selected ? UIColors.BG_ACTIVE : (hovered ? UIColors.BG_HOVER : UIColors.BG_HEADER);
        UIRenderer.fill(graphics, x, y, SLOT_SIZE, SLOT_SIZE, bgColor);
        
        int borderColor = UIColors.BORDER;
        if (!stack.isEmpty()) borderColor = getItemRarityColor(stack);
        if (selected) borderColor = UIColors.PRIMARY;
        else if (hovered) borderColor = UIColors.brighten(borderColor, 1.3f);
        
        UIRenderer.horizontalLine(graphics, x, y, SLOT_SIZE, borderColor);
        UIRenderer.horizontalLine(graphics, x, y + SLOT_SIZE - 1, SLOT_SIZE, borderColor);
        UIRenderer.verticalLine(graphics, x, y, SLOT_SIZE, borderColor);
        UIRenderer.verticalLine(graphics, x + SLOT_SIZE - 1, y, SLOT_SIZE, borderColor);
        
        if (!stack.isEmpty()) {
            int itemX = x + (SLOT_SIZE - 16) / 2;
            int itemY = y + (SLOT_SIZE - 16) / 2 - 2;
            graphics.renderItem(stack, itemX, itemY);
            
            // Stack count (supports >64)
            if (stack.getCount() > 1) {
                String count = formatStackCount(stack.getCount());
                int countX = x + SLOT_SIZE - UIRenderer.getTextWidth(count) - 2;
                UIRenderer.drawText(graphics, count, countX, y + SLOT_SIZE - 10, UIColors.TEXT);
            }
        }
        
        if (selected) {
            float pulse = UIAnimator.pulse(animationTick, 0.1f);
            int glowAlpha = (int)(20 + 15 * pulse);
            graphics.fill(x - 1, y - 1, x + SLOT_SIZE + 1, y + SLOT_SIZE + 1, 
                         UIColors.withAlpha(UIColors.PRIMARY, glowAlpha));
        }
    }
    
    private String formatStackCount(int count) {
        if (count >= 1000000) return String.format("%.1fM", count / 1000000.0);
        if (count >= 10000) return String.format("%.1fK", count / 1000.0);
        if (count >= 1000) return String.format("%.1fK", count / 1000.0);
        return String.valueOf(count);
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
        int displayIndex = selectedIndex >= 0 ? selectedIndex : hoveredIndex;
        
        if (displayIndex >= 0 && displayIndex < displayedItems.size()) {
            ItemStack stack = displayedItems.get(displayIndex);
            
            if (!stack.isEmpty()) {
                // Large 3D item preview - centered in panel
                renderLargeItemPreview(graphics, stack);
                
                // Item info below preview
                int detailX = detailsPanel.getX() + 10;
                int detailY = detailsPanel.getY() + detailsPanel.getHeight() / 2 + 30;
                int maxWidth = detailsPanel.getWidth() - 20;
                int centerX = detailsPanel.getX() + detailsPanel.getWidth() / 2;
                
                // Item name with rarity color
                int rarityColor = getItemRarityColor(stack);
                String name = stack.getHoverName().getString();
                UIRenderer.drawCenteredText(graphics, name, centerX, detailY, rarityColor);
                
                // Separator
                UIRenderer.horizontalLine(graphics, detailX, detailY + 14, maxWidth, UIColors.BORDER);
                
                // Details
                UIRenderer.drawText(graphics, getItemType(stack), detailX, detailY + 22, UIColors.TEXT_SECONDARY);
                UIRenderer.drawText(graphics, "Quantity: " + formatNumber(stack.getCount()), detailX, detailY + 36, UIColors.TEXT_SECONDARY);
                UIRenderer.drawText(graphics, "Rarity: " + getRarityText(stack), detailX, detailY + 50, rarityColor);
                
                // Mod ID
                var key = ForgeRegistries.ITEMS.getKey(stack.getItem());
                if (key != null) {
                    UIRenderer.drawText(graphics, "Mod: " + key.getNamespace(), detailX, detailY + 64, UIColors.TEXT_MUTED);
                }
                
                // Hints at bottom
                int hintY = detailsPanel.getY() + detailsPanel.getHeight() - 30;
                UIRenderer.drawCenteredText(graphics, "Drag to rotate | Double-click to withdraw", centerX, hintY, UIColors.TEXT_MUTED);
                
                return;
            }
        }
        
        renderEmptyPreview(graphics);
    }
    
    /**
     * Renders a LARGE item preview using proper Minecraft rendering
     */
    private void renderLargeItemPreview(GuiGraphics graphics, ItemStack stack) {
        int centerX = detailsPanel.getX() + detailsPanel.getWidth() / 2;
        int centerY = detailsPanel.getContentStartY() + 55;
        
        // Background glow
        int rarityColor = getItemRarityColor(stack);
        float pulse = UIAnimator.pulse(animationTick, 0.08f);
        int glowAlpha = (int)(25 + 15 * pulse);
        
        // Large glow circle
        for (int i = 4; i >= 0; i--) {
            int radius = 50 + i * 8;
            graphics.fill(centerX - radius, centerY - radius, centerX + radius, centerY + radius, 
                         UIColors.withAlpha(rarityColor, glowAlpha / (i + 1)));
        }
        
        // Dark background
        graphics.fill(centerX - 45, centerY - 45, centerX + 45, centerY + 45, 
                     UIColors.withAlpha(UIColors.BG_DARK, 220));
        
        // Border
        int borderColor = UIColors.withAlpha(rarityColor, 200);
        UIRenderer.horizontalLine(graphics, centerX - 45, centerY - 45, 90, borderColor);
        UIRenderer.horizontalLine(graphics, centerX - 45, centerY + 44, 90, borderColor);
        UIRenderer.verticalLine(graphics, centerX - 45, centerY - 45, 90, borderColor);
        UIRenderer.verticalLine(graphics, centerX + 44, centerY - 45, 90, borderColor);
        
        // Render the item BIG
        PoseStack pose = graphics.pose();
        pose.pushPose();
        
        // Move to center
        pose.translate(centerX, centerY, 100);
        
        // Scale up significantly - 4x normal size
        float scale = 4.0f;
        pose.scale(scale, scale, scale);
        
        // Apply rotation
        pose.mulPose(new Quaternionf().rotateY((float) Math.toRadians(itemRotation)));
        pose.mulPose(new Quaternionf().rotateX((float) Math.toRadians(15)));
        
        // Offset so item is centered
        pose.translate(-8, -8, 0);
        
        // Setup lighting
        Lighting.setupForFlatItems();
        
        // Render item at scaled size
        var itemRenderer = Minecraft.getInstance().getItemRenderer();
        var model = itemRenderer.getModel(stack, null, null, 0);
        
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        
        itemRenderer.render(
            stack, 
            ItemDisplayContext.GUI, 
            false, 
            pose, 
            graphics.bufferSource(), 
            LightTexture.FULL_BRIGHT, 
            OverlayTexture.NO_OVERLAY, 
            model
        );
        
        graphics.flush();
        Lighting.setupFor3DItems();
        
        pose.popPose();
    }
    
    private void renderEmptyPreview(GuiGraphics graphics) {
        int centerX = detailsPanel.getX() + detailsPanel.getWidth() / 2;
        int centerY = detailsPanel.getContentStartY() + 55;
        
        graphics.fill(centerX - 45, centerY - 45, centerX + 45, centerY + 45, 
                     UIColors.withAlpha(UIColors.BG_HEADER, 150));
        
        int borderColor = UIColors.withAlpha(UIColors.BORDER, 100);
        UIRenderer.horizontalLine(graphics, centerX - 45, centerY - 45, 90, borderColor);
        UIRenderer.horizontalLine(graphics, centerX - 45, centerY + 44, 90, borderColor);
        UIRenderer.verticalLine(graphics, centerX - 45, centerY - 45, 90, borderColor);
        UIRenderer.verticalLine(graphics, centerX + 44, centerY - 45, 90, borderColor);
        
        UIRenderer.drawCenteredText(graphics, "?", centerX, centerY - 4, UIColors.TEXT_MUTED);
        UIRenderer.drawCenteredText(graphics, "Select an item", centerX, centerY + 60, UIColors.TEXT_MUTED);
    }
    
    private void renderHints(GuiGraphics graphics) {
        if (displayedItems.isEmpty()) {
            int centerX = inventoryPanel.getX() + inventoryPanel.getWidth() / 2;
            int centerY = inventoryPanel.getContentStartY() + 60;
            UIRenderer.drawCenteredText(graphics, "Your System Inventory is empty", centerX, centerY, UIColors.TEXT_MUTED);
            UIRenderer.drawCenteredText(graphics, "Hold an item and press [V] to store it", centerX, centerY + 14, UIColors.PRIMARY_DIM);
        }
    }
    
    private int getItemRarityColor(ItemStack stack) {
        if (stack.hasTag()) {
            var tag = stack.getTag();
            if (tag != null && tag.contains("SoloLevelingRank")) {
                return UIColors.getRarityColor(tag.getString("SoloLevelingRank"));
            }
        }
        return switch (stack.getRarity()) {
            case COMMON -> UIColors.RARITY_COMMON;
            case UNCOMMON -> UIColors.RARITY_UNCOMMON;
            case RARE -> UIColors.RARITY_RARE;
            case EPIC -> UIColors.RARITY_EPIC;
            default -> UIColors.TEXT;
        };
    }
    
    private String getRarityText(ItemStack stack) {
        if (stack.hasTag()) {
            var tag = stack.getTag();
            if (tag != null && tag.contains("SoloLevelingRank")) {
                return tag.getString("SoloLevelingRank") + "-Rank";
            }
        }
        return switch (stack.getRarity()) {
            case COMMON -> "Common";
            case UNCOMMON -> "Uncommon";
            case RARE -> "Rare";
            case EPIC -> "Epic";
            default -> "Unknown";
        };
    }
    
    private String getItemType(ItemStack stack) {
        if (stack.getItem() instanceof net.minecraft.world.item.SwordItem) return "Weapon - Sword";
        if (stack.getItem() instanceof net.minecraft.world.item.AxeItem) return "Weapon - Axe";
        if (stack.getItem() instanceof net.minecraft.world.item.BowItem) return "Weapon - Bow";
        if (stack.getItem() instanceof net.minecraft.world.item.ArmorItem armor) return "Armor - " + armor.getType().getName();
        if (stack.getItem().isEdible()) return "Consumable - Food";
        if (stack.getItem() instanceof net.minecraft.world.item.PotionItem) return "Consumable - Potion";
        return "Item";
    }
    
    private String formatNumber(int number) {
        if (number >= 1000000) return String.format("%.1fM", number / 1000000.0);
        if (number >= 1000) return String.format("%.1fK", number / 1000.0);
        return String.valueOf(number);
    }
    
    private boolean isMouseOver3DPreview(int mouseX, int mouseY) {
        int centerX = detailsPanel.getX() + detailsPanel.getWidth() / 2;
        int centerY = detailsPanel.getContentStartY() + 55;
        return mouseX >= centerX - 50 && mouseX <= centerX + 50 && mouseY >= centerY - 50 && mouseY <= centerY + 50;
    }
    
    private long lastClickTime = 0;
    private int lastClickIndex = -1;
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isMouseOver3DPreview((int) mouseX, (int) mouseY)) {
            int displayIndex = selectedIndex >= 0 ? selectedIndex : hoveredIndex;
            if (displayIndex >= 0 && displayIndex < displayedItems.size()) {
                draggingItem = true;
                lastMouseX = mouseX;
                return true;
            }
        }
        
        if (button == 0 && hoveredIndex >= 0) {
            long currentTime = System.currentTimeMillis();
            if (hoveredIndex == lastClickIndex && currentTime - lastClickTime < 400) {
                withdrawItem(hoveredIndex);
                selectedIndex = -1;
                lastClickIndex = -1;
            } else {
                selectedIndex = (selectedIndex == hoveredIndex) ? -1 : hoveredIndex;
                lastClickIndex = hoveredIndex;
                lastClickTime = currentTime;
            }
            return true;
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggingItem) {
            draggingItem = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingItem) {
            itemRotation += (float) (mouseX - lastMouseX) * 2.0f;
            if (itemRotation >= 360) itemRotation -= 360;
            if (itemRotation < 0) itemRotation += 360;
            lastMouseX = mouseX;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
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
            ModNetworkRegistry.CHANNEL.sendToServer(new WithdrawItemPacket(index));
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§b[System] §7Withdrawing item..."), true);
            }
        }
    }
}
