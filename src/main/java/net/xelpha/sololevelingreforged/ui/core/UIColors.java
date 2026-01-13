package net.xelpha.sololevelingreforged.ui.core;

/**
 * Centralized color palette for the Solo Leveling System UI
 * Inspired by the dark, cybernetic aesthetic of the System from Solo Leveling
 * 
 * Color Format: 0xAARRGGBB (Alpha, Red, Green, Blue)
 */
public final class UIColors {
    
    private UIColors() {} // Prevent instantiation
    
    // ══════════════════════════════════════════════════════════════════════════
    // PRIMARY COLORS - The signature Solo Leveling aesthetic
    // ══════════════════════════════════════════════════════════════════════════
    
    /** Primary accent - Electric cyan glow */
    public static final int PRIMARY = 0xFF00D4FF;
    
    /** Primary dimmed - Subtle cyan for inactive elements */
    public static final int PRIMARY_DIM = 0xFF007A99;
    
    /** Primary glow - Bright cyan for highlights */
    public static final int PRIMARY_GLOW = 0xFF66E5FF;
    
    /** Secondary accent - Purple shadow energy */
    public static final int SECONDARY = 0xFF9D4EDD;
    
    /** Secondary dimmed */
    public static final int SECONDARY_DIM = 0xFF5A2D82;
    
    /** Tertiary accent - Gold for rare/important elements */
    public static final int TERTIARY = 0xFFFFD700;
    
    // ══════════════════════════════════════════════════════════════════════════
    // BACKGROUND COLORS - Dark, immersive panels
    // ══════════════════════════════════════════════════════════════════════════
    
    /** Main background - Near black with slight blue tint */
    public static final int BG_DARK = 0xF0080A10;
    
    /** Panel background - Slightly lighter */
    public static final int BG_PANEL = 0xF0101420;
    
    /** Header background - Darkest */
    public static final int BG_HEADER = 0xFF060810;
    
    /** Hover state background */
    public static final int BG_HOVER = 0xFF1A1E2E;
    
    /** Active/Selected background */
    public static final int BG_ACTIVE = 0xFF151A28;
    
    /** Transparent overlay */
    public static final int BG_OVERLAY = 0x80000000;
    
    // ══════════════════════════════════════════════════════════════════════════
    // BORDER COLORS - Defining edges and structure
    // ══════════════════════════════════════════════════════════════════════════
    
    /** Default border */
    public static final int BORDER = 0xFF1E2436;
    
    /** Border highlight on hover */
    public static final int BORDER_HOVER = 0xFF00D4FF;
    
    /** Border for focused elements */
    public static final int BORDER_FOCUS = 0xFF00D4FF;
    
    /** Subtle inner border */
    public static final int BORDER_INNER = 0xFF0D1018;
    
    // ══════════════════════════════════════════════════════════════════════════
    // TEXT COLORS - Readability and hierarchy
    // ══════════════════════════════════════════════════════════════════════════
    
    /** Primary text - White */
    public static final int TEXT = 0xFFFFFFFF;
    
    /** Secondary text - Light gray */
    public static final int TEXT_SECONDARY = 0xFFB0B8C8;
    
    /** Muted text - Gray */
    public static final int TEXT_MUTED = 0xFF6B7280;
    
    /** Disabled text */
    public static final int TEXT_DISABLED = 0xFF404654;
    
    /** Title text - Cyan accent */
    public static final int TEXT_TITLE = 0xFF00D4FF;
    
    /** Warning text */
    public static final int TEXT_WARNING = 0xFFFFB800;
    
    /** Error text */
    public static final int TEXT_ERROR = 0xFFFF4444;
    
    /** Success text */
    public static final int TEXT_SUCCESS = 0xFF00FF88;
    
    // ══════════════════════════════════════════════════════════════════════════
    // STAT COLORS - Each stat has a unique color identity
    // ══════════════════════════════════════════════════════════════════════════
    
    /** Strength - Red/Orange power */
    public static final int STAT_STRENGTH = 0xFFFF6B4A;
    
    /** Agility - Green speed */
    public static final int STAT_AGILITY = 0xFF00FF88;
    
    /** Sense - Yellow perception */
    public static final int STAT_SENSE = 0xFFFFE066;
    
    /** Vitality - Pink health */
    public static final int STAT_VITALITY = 0xFFFF6B9D;
    
    /** Intelligence - Blue magic */
    public static final int STAT_INTELLIGENCE = 0xFF66B3FF;
    
    // ══════════════════════════════════════════════════════════════════════════
    // BAR COLORS - Progress and resource indicators
    // ══════════════════════════════════════════════════════════════════════════
    
    /** Health bar */
    public static final int BAR_HEALTH = 0xFFE63946;
    
    /** Health bar background */
    public static final int BAR_HEALTH_BG = 0xFF4A1518;
    
    /** Mana bar */
    public static final int BAR_MANA = 0xFF4361EE;
    
    /** Mana bar background */
    public static final int BAR_MANA_BG = 0xFF161B38;
    
    /** Experience bar */
    public static final int BAR_XP = 0xFF00D4FF;
    
    /** Experience bar background */
    public static final int BAR_XP_BG = 0xFF0A2830;
    
    /** Cooldown bar */
    public static final int BAR_COOLDOWN = 0xFF9D4EDD;
    
    /** Generic progress */
    public static final int BAR_PROGRESS = 0xFF00FF88;
    
    // ══════════════════════════════════════════════════════════════════════════
    // RARITY COLORS - Equipment and item tiers
    // ══════════════════════════════════════════════════════════════════════════
    
    /** E-Rank - Common (Gray) */
    public static final int RARITY_E = 0xFF9CA3AF;
    
    /** D-Rank - Uncommon (Green) */
    public static final int RARITY_D = 0xFF22C55E;
    
    /** C-Rank - Rare (Blue) */
    public static final int RARITY_C = 0xFF3B82F6;
    
    /** B-Rank - Epic (Purple) */
    public static final int RARITY_B = 0xFF8B5CF6;
    
    /** A-Rank - Legendary (Orange) */
    public static final int RARITY_A = 0xFFF97316;
    
    /** S-Rank - Mythic (Red) */
    public static final int RARITY_S = 0xFFEF4444;
    
    /** SS-Rank - Divine (Gold) */
    public static final int RARITY_SS = 0xFFFFD700;
    
    /** SSR-Rank - Transcendent (Rainbow shimmer base) */
    public static final int RARITY_SSR = 0xFFFF00FF;
    
    // ══════════════════════════════════════════════════════════════════════════
    // QUEST COLORS - Quest type indicators
    // ══════════════════════════════════════════════════════════════════════════
    
    /** Daily quest - Cyan */
    public static final int QUEST_DAILY = 0xFF00D4FF;
    
    /** Main quest - Gold */
    public static final int QUEST_MAIN = 0xFFFFD700;
    
    /** Urgent/Emergency quest - Red */
    public static final int QUEST_URGENT = 0xFFFF4444;
    
    /** Side quest - Green */
    public static final int QUEST_SIDE = 0xFF00FF88;
    
    /** Completed quest */
    public static final int QUEST_COMPLETE = 0xFF00FF88;
    
    /** Failed quest */
    public static final int QUEST_FAILED = 0xFFFF4444;
    
    // ══════════════════════════════════════════════════════════════════════════
    // SKILL COLORS - Skill type indicators
    // ══════════════════════════════════════════════════════════════════════════
    
    /** Active skill - Cyan */
    public static final int SKILL_ACTIVE = 0xFF00D4FF;
    
    /** Passive skill - Purple */
    public static final int SKILL_PASSIVE = 0xFF9D4EDD;
    
    /** Ultimate skill - Gold */
    public static final int SKILL_ULTIMATE = 0xFFFFD700;
    
    /** Locked skill */
    public static final int SKILL_LOCKED = 0xFF404654;
    
    /** Skill on cooldown */
    public static final int SKILL_COOLDOWN = 0xFF6B7280;
    
    // ══════════════════════════════════════════════════════════════════════════
    // SHADOW COLORS - Shadow army theming
    // ══════════════════════════════════════════════════════════════════════════
    
    /** Shadow base color - Dark purple */
    public static final int SHADOW = 0xFF2D1B4E;
    
    /** Shadow energy - Purple glow */
    public static final int SHADOW_ENERGY = 0xFF9D4EDD;
    
    /** Shadow highlight */
    public static final int SHADOW_HIGHLIGHT = 0xFFB76EFF;
    
    // ══════════════════════════════════════════════════════════════════════════
    // UTILITY METHODS
    // ══════════════════════════════════════════════════════════════════════════
    
    /**
     * Interpolates between two colors based on a progress value (0.0 to 1.0)
     */
    public static int lerp(int colorA, int colorB, float progress) {
        progress = Math.max(0, Math.min(1, progress));
        
        int aA = (colorA >> 24) & 0xFF;
        int rA = (colorA >> 16) & 0xFF;
        int gA = (colorA >> 8) & 0xFF;
        int bA = colorA & 0xFF;
        
        int aB = (colorB >> 24) & 0xFF;
        int rB = (colorB >> 16) & 0xFF;
        int gB = (colorB >> 8) & 0xFF;
        int bB = colorB & 0xFF;
        
        int a = (int) (aA + (aB - aA) * progress);
        int r = (int) (rA + (rB - rA) * progress);
        int g = (int) (gA + (gB - gA) * progress);
        int b = (int) (bA + (bB - bA) * progress);
        
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
    
    /**
     * Adjusts the alpha value of a color
     */
    public static int withAlpha(int color, int alpha) {
        return (alpha << 24) | (color & 0x00FFFFFF);
    }
    
    /**
     * Brightens a color by a factor
     */
    public static int brighten(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = Math.min(255, (int) (((color >> 16) & 0xFF) * factor));
        int g = Math.min(255, (int) (((color >> 8) & 0xFF) * factor));
        int b = Math.min(255, (int) ((color & 0xFF) * factor));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
    
    /**
     * Darkens a color by a factor
     */
    public static int darken(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = (int) (((color >> 16) & 0xFF) / factor);
        int g = (int) (((color >> 8) & 0xFF) / factor);
        int b = (int) ((color & 0xFF) / factor);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
    
    /**
     * Gets the rarity color for a given rank string
     */
    public static int getRarityColor(String rank) {
        return switch (rank.toUpperCase()) {
            case "E" -> RARITY_E;
            case "D" -> RARITY_D;
            case "C" -> RARITY_C;
            case "B" -> RARITY_B;
            case "A" -> RARITY_A;
            case "S" -> RARITY_S;
            case "SS" -> RARITY_SS;
            case "SSR" -> RARITY_SSR;
            default -> TEXT_SECONDARY;
        };
    }
    
    /**
     * Gets the stat color for a given stat name
     */
    public static int getStatColor(String statName) {
        return switch (statName.toLowerCase()) {
            case "strength", "str" -> STAT_STRENGTH;
            case "agility", "agi" -> STAT_AGILITY;
            case "sense", "sen" -> STAT_SENSE;
            case "vitality", "vit" -> STAT_VITALITY;
            case "intelligence", "int" -> STAT_INTELLIGENCE;
            default -> TEXT_SECONDARY;
        };
    }
}
