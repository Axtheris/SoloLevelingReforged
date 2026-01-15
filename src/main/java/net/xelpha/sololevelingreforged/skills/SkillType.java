package net.xelpha.sololevelingreforged.skills;

import com.mojang.serialization.Codec;

/**
 * Types of skills in the Solo Leveling system
 */
public enum SkillType {
    ACTIVE_OFFENSIVE("active_offensive", "Active Offensive"),
    ACTIVE_DEFENSIVE("active_defensive", "Active Defensive"),
    ACTIVE_UTILITY("active_utility", "Active Utility"),
    PASSIVE_BUFF("passive_buff", "Passive Buff"),
    PASSIVE_UTILITY("passive_utility", "Passive Utility");

    private final String id;
    private final String displayName;

    SkillType(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }

    public static final Codec<SkillType> CODEC = Codec.STRING.xmap(
        SkillType::byId,
        SkillType::getId
    );

    public static SkillType byId(String id) {
        for (SkillType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return ACTIVE_OFFENSIVE; // Default fallback
    }
}