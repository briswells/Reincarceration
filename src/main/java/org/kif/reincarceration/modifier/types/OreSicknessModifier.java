package org.kif.reincarceration.modifier.types;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.modifier.core.AbstractModifier;
import org.kif.reincarceration.util.ConsoleUtil;

import java.util.*;
import java.util.stream.Collectors;

public class OreSicknessModifier extends AbstractModifier implements Listener {
    private final Reincarceration plugin;
    private final Map<Material, OreEffect> oreEffects = new HashMap<>();
    private final boolean effectOnBreak;
    private final boolean effectOnSight;
    private final int effectDuration;
    private final int sightCheckRadius;
    private final Map<UUID, BukkitRunnable> activeTasks = new HashMap<>();

    public OreSicknessModifier(Reincarceration plugin) {
        super("ore_sickness", "Ore Sickness", "Applies various effects when breaking or seeing certain ores");
        this.plugin = plugin;

        ConfigurationSection config = plugin.getConfig().getConfigurationSection("modifiers.ore_sickness");
        this.effectOnBreak = config.getBoolean("effect_on_break", true);
        this.effectOnSight = config.getBoolean("effect_on_sight", true);
        this.effectDuration = config.getInt("effect_duration", 200);
        this.sightCheckRadius = config.getInt("sight_check_radius", 5);

        loadOreEffects(config.getConfigurationSection("ore_effects"));
    }

    private void loadOreEffects(ConfigurationSection oresConfig) {
        for (String oreName : oresConfig.getKeys(false)) {
            Material oreMaterial = Material.getMaterial(oreName);
            if (oreMaterial != null) {
                String effectType = oresConfig.getString(oreName + ".type");
                ConfigurationSection effectConfig = oresConfig.getConfigurationSection(oreName);
                OreEffect effect = createOreEffect(effectType, effectConfig);
                if (effect != null) {
                    oreEffects.put(oreMaterial, effect);
                }
            }
        }
    }

    private OreEffect createOreEffect(String effectType, ConfigurationSection config) {
        switch (effectType.toLowerCase()) {
            case "potion":
                return new PotionOreEffect(
                        PotionEffectType.getByName(config.getString("effect")),
                        config.getInt("duration", effectDuration),
                        config.getInt("amplifier", 0)
                );
            case "teleport":
                return new TeleportOreEffect(
                        config.getInt("min_distance", 3),
                        config.getInt("max_distance", 10),
                        config.getStringList("allowed_blocks")
                );
            case "gravity_flip":
                return new GravityFlipOreEffect(
                        config.getInt("duration", 100)
                );
            case "block_transform":
                return new BlockTransformOreEffect(
                        Material.valueOf(config.getString("from_material")),
                        Material.valueOf(config.getString("to_material")),
                        config.getInt("radius", 3)
                );
            case "inventory_shuffle":
                return new InventoryShuffleOreEffect();
            case "magnet":
                return new MagnetOreEffect(
                        config.getDouble("radius", 5),
                        config.getBoolean("attract", true),
                        config.getInt("duration", 100)
                );
            case "ender_vision":
                return new EnderVisionOreEffect(
                        config.getInt("duration", 200)
                );
            case "bouncy_blocks":
                return new BouncyBlocksOreEffect(
                        config.getInt("radius", 5),
                        config.getInt("duration", 200)
                );
            case "vertigo":
                return new VertigoOreEffect(
                        config.getInt("duration", 100),
                        (float) config.getDouble("max_rotation_per_tick", 15.0)
                );
            case "inventory_weight":
                return new InventoryWeightOreEffect(
                        config.getInt("duration", 200)
                );
            case "randomized_drops":
                return new RandomizedDropsOreEffect(
                        config.getInt("duration", 200)
                );
            case "hunger":
                return new HungerOreEffect(config.getInt("amount", 2));
            case "item_drop":
                return new ItemDropOreEffect(
                        Material.valueOf(config.getString("item")),
                        config.getInt("amount", 1)
                );
            case "sound":
                return new SoundOreEffect(
                        Sound.valueOf(config.getString("sound")),
                        (float) config.getDouble("volume", 1.0),
                        (float) config.getDouble("pitch", 1.0)
                );
            default:
                ConsoleUtil.sendError("Unknown effect type: " + effectType);
                return null;
        }
    }

    @Override
    public void apply(Player player) {
        super.apply(player);
        if (effectOnSight) {
            BukkitRunnable task = new BukkitRunnable() {
                @Override
                public void run() {
                    checkPlayerSight(player);
                }
            };
            task.runTaskTimer(plugin, 0L, 20L);
            activeTasks.put(player.getUniqueId(), task);
        }
    }

    @Override
    public void remove(Player player) {
        super.remove(player);
        BukkitRunnable task = activeTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!effectOnBreak) return;

        Player player = event.getPlayer();
        if (!isActive(player)) return;

        Block block = event.getBlock();
        OreEffect effect = oreEffects.get(block.getType());
        if (effect != null) {
            effect.apply(player);
        }
    }

    private boolean hasLineOfSight(Player player, Block block) {
        Location eyeLocation = player.getEyeLocation();
        Vector direction = block.getLocation().add(0.5, 0.5, 0.5).subtract(eyeLocation).toVector().normalize();
        double maxDistance = eyeLocation.distance(block.getLocation());

        for (double d = 0; d <= maxDistance; d += 0.5) {
            Location checkLocation = eyeLocation.clone().add(direction.clone().multiply(d));
            Block checkBlock = checkLocation.getBlock();

            if (checkBlock.equals(block)) {
                return true;
            }

            if (checkBlock.getType().isOccluding() && !checkBlock.equals(block)) {
                return false;
            }
        }

        return false;
    }

    private void checkPlayerSight(Player player) {
        for (int x = -sightCheckRadius; x <= sightCheckRadius; x++) {
            for (int y = -sightCheckRadius; y <= sightCheckRadius; y++) {
                for (int z = -sightCheckRadius; z <= sightCheckRadius; z++) {
                    Block block = player.getLocation().getBlock().getRelative(x, y, z);
                    OreEffect effect = oreEffects.get(block.getType());

                    if (effect != null && hasLineOfSight(player, block)) {
                        effect.apply(player);
                    }
                }
            }
        }
    }

    private interface OreEffect {
        void apply(Player player);
    }

    private static class PotionOreEffect implements OreEffect {
        private final PotionEffectType effectType;
        private final int duration;
        private final int amplifier;

        PotionOreEffect(PotionEffectType effectType, int duration, int amplifier) {
            this.effectType = effectType;
            this.duration = duration;
            this.amplifier = amplifier;
        }

        @Override
        public void apply(Player player) {
            player.addPotionEffect(new PotionEffect(effectType, duration, amplifier));
        }
    }

    private static class TeleportOreEffect implements OreEffect {
        private final int minDistance;
        private final int maxDistance;
        private final List<Material> allowedBlocks;

        TeleportOreEffect(int minDistance, int maxDistance, List<String> allowedBlocksStrings) {
            this.minDistance = minDistance;
            this.maxDistance = maxDistance;
            this.allowedBlocks = new ArrayList<>();
            for (String blockName : allowedBlocksStrings) {
                Material material = Material.getMaterial(blockName);
                if (material != null) {
                    allowedBlocks.add(material);
                }
            }
        }

        @Override
        public void apply(Player player) {
            Location originalLoc = player.getLocation();
            World world = player.getWorld();
            Random random = new Random();

            for (int attempts = 0; attempts < 50; attempts++) {
                double angle = random.nextDouble() * 2 * Math.PI;
                int distance = random.nextInt(maxDistance - minDistance + 1) + minDistance;

                int x = (int) (Math.cos(angle) * distance);
                int z = (int) (Math.sin(angle) * distance);

                int y = world.getHighestBlockYAt(originalLoc.getBlockX() + x, originalLoc.getBlockZ() + z);

                Location newLoc = new Location(world, originalLoc.getBlockX() + x, y, originalLoc.getBlockZ() + z);

                if (isValidTeleportLocation(newLoc)) {
                    player.teleport(newLoc.add(0.5, 0, 0.5));
                    return;
                }
            }

            Location surfaceLoc = world.getHighestBlockAt(originalLoc).getLocation().add(0, 1, 0);
            player.teleport(surfaceLoc);
        }

        private boolean isValidTeleportLocation(Location loc) {
            Block feetBlock = loc.getBlock();
            Block headBlock = feetBlock.getRelative(BlockFace.UP);

            return allowedBlocks.contains(feetBlock.getType()) &&
                    allowedBlocks.contains(headBlock.getType()) &&
                    feetBlock.getType().isSolid() &&
                    !headBlock.getType().isSolid();
        }
    }

    private static class GravityFlipOreEffect implements OreEffect {
        private final int duration;

        GravityFlipOreEffect(int duration) {
            this.duration = duration;
        }

        @Override
        public void apply(Player player) {
            new BukkitRunnable() {
                int ticks = 0;
                @Override
                public void run() {
                    if (ticks >= duration) {
                        this.cancel();
                        return;
                    }
                    player.setVelocity(player.getVelocity().setY(0.1));
                    ticks++;
                }
            }.runTaskTimer(Reincarceration.getPlugin(Reincarceration.class), 0L, 1L);
        }
    }

    private static class BlockTransformOreEffect implements OreEffect {
        private final Material fromMaterial;
        private final Material toMaterial;
        private final int radius;

        BlockTransformOreEffect(Material fromMaterial, Material toMaterial, int radius) {
            this.fromMaterial = fromMaterial;
            this.toMaterial = toMaterial;
            this.radius = radius;
        }

        @Override
        public void apply(Player player) {
            Location center = player.getLocation();
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        Block block = center.getBlock().getRelative(x, y, z);
                        if (block.getType() == fromMaterial) {
                            block.setType(toMaterial);
                        }
                    }
                }
            }
        }
    }

    private static class InventoryShuffleOreEffect implements OreEffect {
        @Override
        public void apply(Player player) {
            ItemStack[] contents = player.getInventory().getContents();
            List<ItemStack> items = new ArrayList<>(Arrays.asList(contents));
            Collections.shuffle(items);
            player.getInventory().setContents(items.toArray(new ItemStack[0]));
            player.updateInventory();
        }
    }

    private static class MagnetOreEffect implements OreEffect {
        private final double radius;
        private final boolean attract;
        private final int duration;

        MagnetOreEffect(double radius, boolean attract, int duration) {
            this.radius = radius;
            this.attract = attract;
            this.duration = duration;
        }

        @Override
        public void apply(Player player) {
            new BukkitRunnable() {
                int ticks = 0;
                @Override
                public void run() {
                    if (ticks >= duration) {
                        this.cancel();
                        return;
                    }
                    for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                        if (entity instanceof Item || entity instanceof FallingBlock) {
                            Vector direction = attract ?
                                    player.getLocation().toVector().subtract(entity.getLocation().toVector()) :
                                    entity.getLocation().toVector().subtract(player.getLocation().toVector());
                            entity.setVelocity(direction.normalize().multiply(0.5));
                        }
                    }
                    ticks++;
                }
            }.runTaskTimer(Reincarceration.getPlugin(Reincarceration.class), 0L, 1L);
        }
    }

    private static class EnderVisionOreEffect implements OreEffect {
        private final int duration;

        EnderVisionOreEffect(int duration) {
            this.duration = duration;
        }

        @Override
        public void apply(Player player) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, duration, 0));
        }
    }

    private static class BouncyBlocksOreEffect implements OreEffect {
        private final int radius;
        private final int duration;

        BouncyBlocksOreEffect(int radius, int duration) {
            this.radius = radius;
            this.duration = duration;
        }

        @Override
        public void apply(Player player) {
            new BukkitRunnable() {
                int ticks = 0;
                @Override
                public void run() {
                    if (ticks >= duration) {
                        this.cancel();
                        return;
                    }
                    Location playerLoc = player.getLocation();
                    for (int x = -radius; x <= radius; x++) {
                        for (int y = -radius; y <= radius; y++) {
                            for (int z = -radius; z <= radius; z++) {
                                Block block = playerLoc.getBlock().getRelative(x, y, z);
                                if (block.getType().isSolid()) {
                                    player.getWorld().spawnParticle(Particle.SLIME, block.getLocation().add(0.5, 1, 0.5), 1);
                                }
                            }
                        }
                    }
                    ticks++;
                }
            }.runTaskTimer(Reincarceration.getPlugin(Reincarceration.class), 0L, 1L);

            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, duration, 3));
        }
    }

    private static class VertigoOreEffect implements OreEffect {
        private final int duration;
        private final float maxRotationPerTick;

        VertigoOreEffect(int duration, float maxRotationPerTick) {
            this.duration = duration;
            this.maxRotationPerTick = maxRotationPerTick;
        }

        @Override
        public void apply(Player player) {
            new BukkitRunnable() {
                int ticks = 0;
                @Override
                public void run() {
                    if (ticks >= duration) {
                        this.cancel();
                        return;
                    }
                    Location loc = player.getLocation();
                    float currentYaw = loc.getYaw();
                    float rotationAmount = Math.min(15, maxRotationPerTick); // Cap the rotation speed
                    loc.setYaw((currentYaw + rotationAmount) % 360);
                    player.teleport(loc);
                    ticks++;
                }
            }.runTaskTimer(Reincarceration.getPlugin(Reincarceration.class), 0L, 1L);
        }
    }

    private static class InventoryWeightOreEffect implements OreEffect {
        private final int duration;

        InventoryWeightOreEffect(int duration) {
            this.duration = duration;
        }

        @Override
        public void apply(Player player) {
            new BukkitRunnable() {
                int ticks = 0;
                @Override
                public void run() {
                    if (ticks >= duration) {
                        player.removePotionEffect(PotionEffectType.SLOW);
                        this.cancel();
                        return;
                    }
                    int filledSlots = (int) Arrays.stream(player.getInventory().getContents())
                            .filter(item -> item != null && item.getType() != Material.AIR)
                            .count();
                    int slowness = filledSlots / 9;  // 1 level of slowness for every 9 filled slots
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, slowness, true));
                    ticks += 20;
                }
            }.runTaskTimer(Reincarceration.getPlugin(Reincarceration.class), 0L, 20L);
        }
    }

    private static class RandomizedDropsOreEffect implements OreEffect {
        private final int duration;
        private final List<Material> possibleDrops;
        private final Reincarceration plugin;

        RandomizedDropsOreEffect(int duration) {
            this.duration = duration;
            this.possibleDrops = Arrays.stream(Material.values())
                    .filter(Material::isItem)
                    .collect(Collectors.toList());
            this.plugin = Reincarceration.getPlugin(Reincarceration.class);
        }

        @Override
        public void apply(Player player) {
            Listener listener = new Listener() {
                @EventHandler
                public void onBlockBreak(BlockBreakEvent event) {
                    if (event.getPlayer().equals(player)) {
                        event.setCancelled(true);
                        Material randomMaterial = possibleDrops.get(new Random().nextInt(possibleDrops.size()));
                        event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(randomMaterial));
                        event.getBlock().setType(Material.AIR);
                    }
                }
            };

            Bukkit.getPluginManager().registerEvents(listener, plugin);

            // Schedule a task to unregister the listener after the duration
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                BlockBreakEvent.getHandlerList().unregister(listener);
            }, duration);
        }
    }

    private static class HungerOreEffect implements OreEffect {
        private final int amount;

        HungerOreEffect(int amount) {
            this.amount = amount;
        }

        @Override
        public void apply(Player player) {
            int newFoodLevel = Math.max(0, player.getFoodLevel() - amount);
            player.setFoodLevel(newFoodLevel);
        }
    }

    private static class ItemDropOreEffect implements OreEffect {
        private final Material material;
        private final int amount;

        ItemDropOreEffect(Material material, int amount) {
            this.material = material;
            this.amount = amount;
        }

        @Override
        public void apply(Player player) {
            player.getWorld().dropItemNaturally(player.getLocation(), new ItemStack(material, amount));
        }
    }

    private static class SoundOreEffect implements OreEffect {
        private final Sound sound;
        private final float volume;
        private final float pitch;

        SoundOreEffect(Sound sound, float volume, float pitch) {
            this.sound = sound;
            this.volume = volume;
            this.pitch = pitch;
        }

        @Override
        public void apply(Player player) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }
}