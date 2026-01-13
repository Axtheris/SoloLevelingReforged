package net.xelpha.sololevelingreforged.core;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import net.xelpha.sololevelingreforged.network.ModNetworkRegistry;
import net.xelpha.sololevelingreforged.network.SyncCapabilityPacket;

/**
 * Player Capability for Solo Leveling System
 * Handles custom stats, leveling, and persistent data storage
 */
public class PlayerCapability implements ICapabilitySerializable<CompoundTag> {

    // Capability instance
    public static final Capability<PlayerCapability> PLAYER_SYSTEM_CAP = CapabilityManager.get(new CapabilityToken<>() {});

    // Core Stats
    private int level = 1;
    private int experience = 0;
    private int experienceToNext = 100;

    // Ability Points
    private int availableAP = 0;

    // Base Stats (allocated points)
    private int strength = 0;      // Physical damage, carry weight
    private int agility = 0;       // Movement speed, attack speed, dodge
    private int sense = 0;         // Accuracy, perception, enemy detection
    private int vitality = 0;      // Max HP, defense, stamina
    private int intelligence = 0;  // Max Mana, magic damage

    // Current Resources
    private float currentMana = 100f;
    private float maxMana = 100f;

    // Titles and Progression
    private String currentTitle = "Hunter";
    private boolean isShadowMonarch = false;

    // System Messages
    private String lastSystemMessage = "";
    private long messageTimestamp = 0;

    // Reference to player for calculations
    private Player player;

    public PlayerCapability() {}

    public void setPlayer(Player player) {
        this.player = player;
    }

    // ===== LEVELING SYSTEM =====

    public void addExperience(int xp) {
        this.experience += xp;

        // Check for level up
        while (this.experience >= this.experienceToNext) {
            levelUp();
        }

        // Send system message
        setSystemMessage("Experience gained: " + xp);

        // Sync to client - pass the player parameter
        syncToClient(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer ? serverPlayer : null);
    }

    private void levelUp() {
        this.level++;
        this.experience -= this.experienceToNext;
        this.experienceToNext = calculateExperienceForLevel(this.level);
        this.availableAP += 5; // 5 AP per level

        setSystemMessage("LEVEL UP! You are now level " + this.level + ". +5 Ability Points available.");

        // Play level up sound and show toast notification
        playLevelUpEffects();

        // Sync to client
        syncToClient(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer ? serverPlayer : null);
    }

    private void playLevelUpEffects() {
        if (player != null && player.level() != null) {
            // Play ding sound
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP,
                net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);

            // Show toast notification (if on client side)
            if (player.level().isClientSide) {
                net.minecraft.client.Minecraft.getInstance().getToasts().addToast(
                    new net.minecraft.client.gui.components.toasts.SystemToast(
                        net.minecraft.client.gui.components.toasts.SystemToast.SystemToastIds.TUTORIAL_HINT,
                        net.minecraft.network.chat.Component.literal("LEVEL UP!"),
                        net.minecraft.network.chat.Component.literal("You reached level " + this.level)
                    )
                );
            }
        }
    }

    private int calculateExperienceForLevel(int level) {
        // Much harder progression for Solo Leveling authenticity
        // Level 100 should be extremely difficult to reach through extensive grinding
        // Similar to Sung Jin-Woo's journey requiring constant dungeon runs and boss fights
        if (level <= 10) {
            // Early game: Gentle curve to help new players learn
            return 500 + (level - 1) * 200;
        } else if (level <= 30) {
            // Mid game: Moderate scaling
            return 2500 + (int)((level - 10) * 800 * Math.pow(1.15, level - 10));
        } else if (level <= 60) {
            // Late game: Harder scaling
            return 15000 + (int)((level - 30) * 2500 * Math.pow(1.25, level - 30));
        } else if (level <= 90) {
            // Very hard: Approaching level 100
            return 80000 + (int)((level - 60) * 8000 * Math.pow(1.35, level - 60));
        } else {
            // Level 90+: Extremely difficult, level 100 milestone
            return 250000 + (int)((level - 90) * 15000 * Math.pow(1.45, level - 90));
        }
    }

    // ===== STAT ALLOCATION =====

    public boolean allocateStatPoint(String statName) {
        if (availableAP <= 0) return false;

        switch (statName.toLowerCase()) {
            case "strength":
                strength++;
                break;
            case "agility":
                agility++;
                break;
            case "sense":
                sense++;
                break;
            case "vitality":
                vitality++;
                updateMaxHealth();
                break;
            case "intelligence":
                intelligence++;
                updateMaxMana();
                break;
            default:
                return false;
        }

        availableAP--;
        setSystemMessage(statName.toUpperCase() + " increased to " + getStatValue(statName) + "!");

        // Sync to client
        syncToClient(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer ? serverPlayer : null);

        return true;
    }

    public void updateMaxHealth() {
        if (player != null) {
            // Base HP is 20, +2 HP per vitality point
            float newMaxHealth = 20.0f + (vitality * 2.0f);
            player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH)
                  .setBaseValue(newMaxHealth);
        }
    }

    public void updateMaxMana() {
        this.maxMana = 100.0f + (intelligence * 10.0f);
        if (this.currentMana > this.maxMana) {
            this.currentMana = this.maxMana;
        }
    }

    // ===== MANA SYSTEM =====

    public float getCurrentMana() {
        return currentMana;
    }

    public float getMaxMana() {
        return maxMana;
    }

    public boolean consumeMana(float amount) {
        if (currentMana >= amount) {
            currentMana -= amount;
            syncToClient(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer ? serverPlayer : null);
            return true;
        }
        return false;
    }

    public void regenerateMana(float amount) {
        currentMana = Math.min(currentMana + amount, maxMana);
        syncToClient(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer ? serverPlayer : null);
    }

    // ===== GETTERS =====

    public int getLevel() { return level; }
    public int getExperience() { return experience; }
    public int getExperienceToNext() { return experienceToNext; }
    public int getAvailableAP() { return availableAP; }

    public int getStrength() { return strength; }
    public int getAgility() { return agility; }
    public int getSense() { return sense; }
    public int getVitality() { return vitality; }
    public int getIntelligence() { return intelligence; }

    public int getStatValue(String statName) {
        switch (statName.toLowerCase()) {
            case "strength": return strength;
            case "agility": return agility;
            case "sense": return sense;
            case "vitality": return vitality;
            case "intelligence": return intelligence;
            default: return 0;
        }
    }

    public String getCurrentTitle() { return currentTitle; }
    public String getLastSystemMessage() { return lastSystemMessage; }
    public long getMessageTimestamp() { return messageTimestamp; }

    // ===== SETTERS =====

    public void setSystemMessage(String message) {
        this.lastSystemMessage = message;
        this.messageTimestamp = System.currentTimeMillis();
    }

    // Admin setter methods for commands
    public void setLevel(int level) {
        this.level = Math.max(1, level);
    }

    public void setExperience(int experience) {
        this.experience = Math.max(0, experience);
    }

    public void setExperienceToNext(int experienceToNext) {
        this.experienceToNext = Math.max(1, experienceToNext);
    }

    public void addAvailableAP(int amount) {
        this.availableAP = Math.max(0, this.availableAP + amount);
    }

    public void setAvailableAP(int amount) {
        this.availableAP = Math.max(0, amount);
    }

    public void setStrength(int strength) {
        this.strength = Math.max(0, strength);
    }

    public void setAgility(int agility) {
        this.agility = Math.max(0, agility);
    }

    public void setSense(int sense) {
        this.sense = Math.max(0, sense);
    }

    public void setVitality(int vitality) {
        this.vitality = Math.max(0, vitality);
        updateMaxHealth();
    }

    public void setIntelligence(int intelligence) {
        this.intelligence = Math.max(0, intelligence);
        updateMaxMana();
    }

    // ===== SYNCHRONIZATION =====

    /**
     * Sync capability data to the client
     * Only works when called on the server side
     */
    private void syncToClient(ServerPlayer serverPlayer) {
        if (serverPlayer != null) {
            ModNetworkRegistry.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                new SyncCapabilityPacket(serializeNBT()));
        }
    }

    // ===== CAPABILITY INTERFACE =====

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        // Leveling
        tag.putInt("level", level);
        tag.putInt("experience", experience);
        tag.putInt("experienceToNext", experienceToNext);
        tag.putInt("availableAP", availableAP);

        // Stats
        tag.putInt("strength", strength);
        tag.putInt("agility", agility);
        tag.putInt("sense", sense);
        tag.putInt("vitality", vitality);
        tag.putInt("intelligence", intelligence);

        // Resources
        tag.putFloat("currentMana", currentMana);
        tag.putFloat("maxMana", maxMana);

        // Progression
        tag.putString("currentTitle", currentTitle);
        tag.putBoolean("isShadowMonarch", isShadowMonarch);

        // Messages
        tag.putString("lastSystemMessage", lastSystemMessage);
        tag.putLong("messageTimestamp", messageTimestamp);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        // Leveling
        level = tag.getInt("level");
        experience = tag.getInt("experience");
        experienceToNext = tag.getInt("experienceToNext");
        availableAP = tag.getInt("availableAP");

        // Stats
        strength = tag.getInt("strength");
        agility = tag.getInt("agility");
        sense = tag.getInt("sense");
        vitality = tag.getInt("vitality");
        intelligence = tag.getInt("intelligence");

        // Resources
        currentMana = tag.getFloat("currentMana");
        maxMana = tag.getFloat("maxMana");

        // Progression
        currentTitle = tag.getString("currentTitle");
        isShadowMonarch = tag.getBoolean("isShadowMonarch");

        // Messages
        lastSystemMessage = tag.getString("lastSystemMessage");
        messageTimestamp = tag.getLong("messageTimestamp");
    }

    // ===== CAPABILITY PROVIDER =====

    private final LazyOptional<PlayerCapability> lazyOptional = LazyOptional.of(() -> this);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == PLAYER_SYSTEM_CAP) {
            return lazyOptional.cast();
        }
        return LazyOptional.empty();
    }

    public void invalidate() {
        lazyOptional.invalidate();
    }
}