package net.xelpha.sololevelingreforged.item;

import net.xelpha.sololevelingreforged.ModToolTiers;

public class DemonKingDaggersItem extends SoloLevelingWeaponItem {

    public DemonKingDaggersItem(Properties properties) {
        super(ModToolTiers.S_RANK, 15, -1.2F, properties,
            "\"The legendary daggers wielded by the Demon King himself, forged from demonic essence.\"",
            "Two as One - Strength-based scaling provides exponential damage potential");
    }

    // Note: The strength-based scaling is handled by the base damage calculation
    // Each dagger grants bonus attack power based on the user's Strength stat
    // This is implemented through the Solo Leveling System's stat bonuses
}