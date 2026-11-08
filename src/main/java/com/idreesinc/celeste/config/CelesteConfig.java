package com.idreesinc.celeste.config;

import com.idreesinc.celeste.utilities.WeightedRandomBag;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Locale;
import java.util.logging.Logger;

public class CelesteConfig {
    private final Logger logger;

    public boolean newMoonMeteorShower;
    public int beginSpawningStarsTime;
    public int endSpawningStarsTime;

    public boolean shootingStarsEnabled;
    public double shootingStarsPerMinute;
    public double shootingStarsPerMinuteMeteorShower;
    public int shootingStarsMinHeight;
    public int shootingStarsMaxHeight;

    public boolean fallingStarsEnabled;
    public double fallingStarsPerMinute;
    public double fallingStarsPerMinuteMeteorShower;
    public int fallingStarsRadius;
    public boolean fallingStarsSoundEnabled;
    public double fallingStarsVolume;
    public int fallingStarsSparkTime;
    public int fallingStarsExperience;
    public WeightedRandomBag<Material> fallingStarSimpleLoot;
    public String fallingStarLootTable;

    public CelesteConfig(ConfigurationSection section, Logger logger) {
        // Used to build the global config
        this.logger = logger;
        buildFromConfigurationSection(section);
    }

    public CelesteConfig(ConfigurationSection section, CelesteConfig globalConfig) {
        // Used to build per-world configs
        this.logger = globalConfig.logger;
        buildFromConfigurationSectionWithGlobal(section, globalConfig);
    }

    private void buildFromConfigurationSection(ConfigurationSection section) {
        newMoonMeteorShower = section.getBoolean("new-moon-meteor-shower");
        beginSpawningStarsTime = section.getInt("begin-spawning-stars-time");
        endSpawningStarsTime = section.getInt("end-spawning-stars-time");

        shootingStarsEnabled = section.getBoolean("shooting-stars-enabled");
        shootingStarsPerMinute = section.getDouble("shooting-stars-per-minute");
        shootingStarsPerMinuteMeteorShower = section.getDouble("shooting-stars-per-minute-during-meteor-showers");
        shootingStarsMinHeight = section.getInt("shooting-stars-min-height");
        shootingStarsMaxHeight = section.getInt("shooting-stars-max-height");

        fallingStarsEnabled = section.getBoolean("falling-stars-enabled");
        fallingStarsPerMinute = section.getDouble("falling-stars-per-minute");
        fallingStarsPerMinuteMeteorShower = section.getDouble("falling-stars-per-minute-during-meteor-showers");
        fallingStarsRadius = section.getInt("falling-stars-radius");
        fallingStarsSoundEnabled = section.getBoolean("falling-stars-sound-enabled");
        fallingStarsVolume = section.getDouble("falling-stars-volume");
        fallingStarsSparkTime = section.getInt("falling-stars-spark-time");
        fallingStarsExperience = section.getInt("falling-stars-experience");

        fallingStarLootTable = section.getString("falling-stars-loot-table");
        if (section.isSet("falling-stars-loot")) {
            fallingStarSimpleLoot = calculateSimpleLoot(section);
        }
    }

    private void buildFromConfigurationSectionWithGlobal(ConfigurationSection section, CelesteConfig globalConfig) {
        newMoonMeteorShower = section.getBoolean("new-moon-meteor-shower", globalConfig.newMoonMeteorShower);
        beginSpawningStarsTime = section.getInt("begin-spawning-stars-time", globalConfig.beginSpawningStarsTime);
        endSpawningStarsTime = section.getInt("end-spawning-stars-time", globalConfig.endSpawningStarsTime);

        shootingStarsEnabled = section.getBoolean("shooting-stars-enabled", globalConfig.shootingStarsEnabled);
        shootingStarsPerMinute = section.getDouble("shooting-stars-per-minute", globalConfig.shootingStarsPerMinute);
        shootingStarsPerMinuteMeteorShower = section.getDouble("shooting-stars-per-minute-during-meteor-showers", globalConfig.shootingStarsPerMinuteMeteorShower);
        shootingStarsMinHeight = section.getInt("shooting-stars-min-height", globalConfig.shootingStarsMinHeight);
        shootingStarsMaxHeight = section.getInt("shooting-stars-max-height", globalConfig.shootingStarsMaxHeight);

        fallingStarsEnabled = section.getBoolean("falling-stars-enabled", globalConfig.fallingStarsEnabled);
        fallingStarsPerMinute = section.getDouble("falling-stars-per-minute", globalConfig.fallingStarsPerMinute);
        fallingStarsPerMinuteMeteorShower = section.getDouble("falling-stars-per-minute-during-meteor-showers", globalConfig.fallingStarsPerMinuteMeteorShower);
        fallingStarsRadius = section.getInt("falling-stars-radius", globalConfig.fallingStarsRadius);
        fallingStarsSoundEnabled = section.getBoolean("falling-stars-sound-enabled", globalConfig.fallingStarsSoundEnabled);
        fallingStarsVolume = section.getDouble("falling-stars-volume", globalConfig.fallingStarsVolume);
        fallingStarsSparkTime = section.getInt("falling-stars-spark-time", globalConfig.fallingStarsSparkTime);
        fallingStarsExperience = section.getInt("falling-stars-experience", globalConfig.fallingStarsExperience);

        if (section.isSet("falling-stars-loot") || section.isSet("falling-stars-loot-table")) {
            // Ensure that neither type of loot is inherited from the global config if any overrides are used
            // This means that even if the global config sets simple loot and the world config overrides just loot
            // table, the end result will be a config with the loot table set and the simple table empty
            fallingStarLootTable = section.getString("falling-stars-loot-table");
            if (section.isSet("falling-stars-loot")) {
                fallingStarSimpleLoot = calculateSimpleLoot(section);
            }
        } else {
            fallingStarLootTable = globalConfig.fallingStarLootTable;
            fallingStarSimpleLoot = globalConfig.fallingStarSimpleLoot;
        }
    }

    private WeightedRandomBag<Material> calculateSimpleLoot(ConfigurationSection section) {
        ConfigurationSection loot = section.getConfigurationSection("falling-stars-loot");
        WeightedRandomBag<Material> fallingStarDrops = new WeightedRandomBag<>();
        if (loot == null) {
            logger.warning("Expected a mapping of item names to weights in falling-stars-loot");
            return fallingStarDrops;
        }

        for (String key : loot.getKeys(false)) {
            Material material = Material.getMaterial(key.toUpperCase(Locale.ROOT));
            double weight = loot.getDouble(key, 0);
            if (material == null || material.isLegacy() || !material.isItem() || material.isAir()) {
                logger.warning("Skipping falling star loot '" + key + "' because it is not an item on this server version");
            } else if (weight > 0 && Double.isFinite(weight)) {
                fallingStarDrops.addEntry(material, weight);
            }
        }
        return fallingStarDrops;
    }
}
